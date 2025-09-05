package controllers;

import actions.OperationLoggingAction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import models.*;
import models.Challenge;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import services.*;
import services.ChallengeService;
import services.ChallengeApplicationService;
import utils.Common;
import utils.Constants;
import utils.RESTfulCalls;
import views.html.*;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import play.libs.Files.TemporaryFile;
import static controllers.Application.checkLoginStatus;
import static utils.Common.beginIndexForPagination;
import static utils.Common.endIndexForPagination;

public class ChallengeController extends Controller {

    @Inject
    Config config;

    private final ChallengeService challengeService;
    private final ChallengeApplicationService challengeApplicationService;

    private final UserService userService;
    private final AccessTimesService accessTimesService;

    private Form<Challenge> challengeFormTemplate;
    private Form<ChallengeApplication> challengeApplicationFormTemplate;
    private FormFactory myFactory;
    private final FileService fileService;


    /******************************* Constructor **********************************************************************/
    @Inject
    public ChallengeController(FormFactory factory,
                               ChallengeService challengeService,
                               ChallengeApplicationService challengeApplicationService,
                               UserService userService, AccessTimesService accessTimesService,
                               FileService fileService) {
        challengeFormTemplate = factory.form(Challenge.class);
        challengeApplicationFormTemplate = factory.form(ChallengeApplication.class);
        myFactory = factory;
        this.challengeApplicationService = challengeApplicationService;
        this.challengeService = challengeService;
        this.userService = userService;
        this.accessTimesService = accessTimesService;
        this.fileService = fileService;
    }


    /************************************************** Challenge Registration ******************************************/

    /**
     * This method intends to render the challenge registration page.
     *
     * @return
     */
    @With(OperationLoggingAction.class)
    public Result challengeRegisterPage() {
        checkLoginStatus();
        return ok(challengeRegister.render());
    }

    /**
     * This method intends to gather challenge registration information and create a challenge in database.
     *
     * @return
     */
    public Result challengeRegisterPOST() {
        checkLoginStatus();
        try {
            Form<Challenge> challengeForm = challengeFormTemplate.bindFromRequest();
            Http.MultipartFormData body = request().body().asMultipartFormData();

            ObjectNode jsonData = (ObjectNode) challengeService.serializeFormToJson(challengeForm);

            JsonNode createResp = RESTfulCalls.postAPI(
                    RESTfulCalls.getBackendAPIUrl(config, Constants.CHALLENGE_REGISTER_POST),
                    jsonData
            );
            if (createResp == null || createResp.has("error")) {
                Logger.debug("ChallengeController.challengeRegisterPOST: cannot create challenge");
                return ok(registrationError.render("Challenge"));
            }
            long challengeId = createResp.asLong();
            Logger.debug("✔ Created Challenge with ID = " + challengeId);

            // 4) upload the PDF (if present) against this new challengeId
            if (body != null) {
                fileService.uploadFile(
                        body,
                        "challengePdf",
                        "challenge",
                        "challenge",
                        challengeId
                );
            }

            // 5) render confirmation page
            return ok(registerConfirmation.render(challengeId, "Challenge"));

        } catch (Exception e) {
            Logger.error("ChallengeController.challengeRegisterPOST failed", e);
            return ok(registrationError.render("Challenge"));
        }
    }

    /************************************************** End of Challenge Registration ************************************/


    /************************************************** Challenge Edit ***************************************************/

    /**
     * This method intends to prepare to edit a challenge.
     *
     * @param challengeId: challenge id
     * @return
     */
    @With(OperationLoggingAction.class)
    public Result challengeEditPage(Long challengeId) {
        try {
            Challenge challenge = challengeService.getChallengeById(challengeId);
            if (challenge == null) {
                Logger.debug("ChallengeController.challengeEditPage exception: cannot get challenge by id");
                Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
                return ok(generalError.render());
            }
            System.out.println("edit page challenge info: "+challenge);
//            JsonNode teamMembersNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
//                    Constants.GET_TEAM_MEMBERS_BY_PROJECT_ID + challengeId));
//            challenge.setTeamMembers(Common.deserializeJsonToList(teamMembersNode, User.class));

            accessTimesService.AddOneTime("challenge", challengeId);
            String userIdStr = session("id");
            String tableName = "challenge";
            String challengeFileType = "challenge";
            String tableRecorderId = challengeId.toString();
            String backendPort = Constants.CMU_BACKEND_PORT;
            Boolean challengeDocument = fileService.checkFile(tableName, challengeFileType, tableRecorderId);
            return ok(challengeEdit.render(challenge,
                    tableName,
                    challengeFileType,
                    tableRecorderId,
                    backendPort,
                    challengeDocument));
        } catch (Exception e) {
            Logger.debug("ChallengeController.challengeEditPage() exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }

    @With(OperationLoggingAction.class)
    public Result challengeEditPageAdmin(Long challengeId) {
        try {
            Challenge challenge = challengeService.getChallengeById(challengeId);
            if (challenge == null) {
                Logger.debug("ChallengeController.challengeEditPage exception: cannot get challenge by id");
                Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
                return ok(generalError.render());
            }
            System.out.println("edit page challenge info: " + challenge);

            accessTimesService.AddOneTime("challenge", challengeId);

            JsonNode challengeJson = Json.toJson(challenge);

            if (challengeJson.has("challengePublisher") && challengeJson.get("challengePublisher").isObject()) {
                ObjectNode publisherNode = (ObjectNode) challengeJson.get("challengePublisher");
                if (publisherNode.has("id")) {
                    long publisherId = publisherNode.get("id").asLong();
                    ((ObjectNode) challengeJson).remove("challengePublisher");
                    ((ObjectNode) challengeJson).put("challengePublisher", publisherId);
                }
            }

            return ok(challengeEditAdmin.render(challengeJson));
        } catch (Exception e) {
            Logger.debug("ChallengeController.challengeEditPage() exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }

    /**
     * This method intends to submit the edit in the challenge edit page.
     *
     * @param challengeId challenge id
     * @return
     */
    public Result challengeEditPOST(Long challengeId) {
        checkLoginStatus();

        try {
            Form<Challenge> challengeForm = challengeFormTemplate.bindFromRequest();
            Http.MultipartFormData<?> body = request().body().asMultipartFormData();

            JsonNode jsonNode = challengeService.serializeFormToJson(challengeForm);
            ObjectNode jsonData = (ObjectNode) jsonNode;

            JsonNode response = RESTfulCalls.postAPI(
                    RESTfulCalls.getBackendAPIUrl(config, Constants.CHALLENGE_EDIT_POST + challengeId),
                    jsonData
            );
            if (response == null || response.has("error")) {
                Logger.debug("Cannot update the challenge");
                return redirect(routes.ChallengeController.challengeEditPage(challengeId));
            }

            if (body != null && body.getFile("challengePdf") != null) {
                fileService.uploadFile(
                        body,
                        "challengePdf",
                        "challenge",
                        "challenge",
                        challengeId
                );
            }

            // 5. render confirmation
            return ok(editConfirmation.render(challengeId, 0L, "Challenge"));
        } catch (Exception e) {
            Logger.debug("ChallengeController.challengeEditPOST exception: " + e.toString());
            return ok(editError.render("Challenge"));
        }
    }

    @With(OperationLoggingAction.class)
    public Result challengeEditPOSTAdmin(Long challengeId) {
        checkLoginStatus();
        try {
            Http.MultipartFormData<File> body = request().body().asMultipartFormData();
            Map<String, String[]> formData = request().body().asFormUrlEncoded();

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode jsonData = mapper.createObjectNode();

            if (formData != null) {
                for (Map.Entry<String, String[]> entry : formData.entrySet()) {
                    String key = entry.getKey();
                    String[] values = entry.getValue();
                    if (values != null && values.length > 0) {
                        if ("challengePublisher".equals(key)) {
                            ObjectNode publisherNode = mapper.createObjectNode();
                            try {
                                publisherNode.put("id", Long.parseLong(values[0]));
                            } catch (NumberFormatException nfe) {
                                publisherNode.put("id", values[0]);
                            }
                            jsonData.set(key, publisherNode);
                        } else {
                            jsonData.put(key, values[0]);
                        }
                    }
                }
            }
            if (!jsonData.has("id")) {
                jsonData.put("id", challengeId);
            }

            JsonNode response = RESTfulCalls.postAPI(
                    RESTfulCalls.getBackendAPIUrl(config, Constants.CHALLENGE_EDIT_POST_ADMIN + challengeId),
                    jsonData);
            System.out.println("send edit post request: " + response.toString());
            if (response == null || response.has("error")) {
                Logger.debug("Cannot update the challenge");
                return redirect(routes.ChallengeController.challengeEditPage(challengeId));
            }

            return ok(editConfirmation.render(challengeId, 0L, "Challenge"));
        } catch (Exception e) {
            Logger.debug("ProjectController.challengeEditPOSTAdmin exception: " + e.toString());
            return ok(editError.render("Challenge"));
        }
    }

    /************************************************** End of Challenge Edit ********************************************/

    /************************************************** Challenge Apply **************************************************/
    /**
     * This method intends to prepare to edit a challenge.
     *
     * @param challengeId: challenge id
     * @return
     */
    @With(OperationLoggingAction.class)
    public Result challengeApplyPage(Long challengeId) {
        try {
            Challenge challenge = challengeService.getChallengeById(challengeId);
            if (challenge == null) {
                Logger.debug("ChallengeController.challengeApplyPage exception: cannot get challenge by id");
                Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
                return ok(generalError.render());
            }
            System.out.println("Apply page challenge info: "+ challenge);
//            JsonNode teamMembersNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
//                    Constants.GET_TEAM_MEMBERS_BY_PROJECT_ID + challengeId));
//            challenge.setTeamMembers(Common.deserializeJsonToList(teamMembersNode, User.class));


            return ok(challengeApplication.render(challenge));
        } catch (Exception e) {
            Logger.debug("ChallengeController.challengeApplyPage() exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }

    /**
     * This method intends to submit the edit in the challenge edit page.
     *
     * @param challengeId challenge id
     * @return
     */
    public Result challengeApplyPOST(Long challengeId) {
        checkLoginStatus();

        try {
            Form<ChallengeApplication> form = challengeApplicationFormTemplate.bindFromRequest();
            Http.MultipartFormData<?> body = request().body().asMultipartFormData();

            ObjectNode jsonData = challengeApplicationService.serializeFormToJson(form, challengeId);

            JsonNode createResp = RESTfulCalls.postAPI(
                    RESTfulCalls.getBackendAPIUrl(config, Constants.CHALLENGE_APPLY_POST + challengeId),
                    jsonData
            );
            if (createResp == null || createResp.has("error")) {
                Logger.debug("Cannot apply for the challenge");
                return redirect(routes.ChallengeController.challengeDetail(challengeId));
            }
            long applicationId = createResp.asLong();

            if (body != null) {
                fileService.uploadFile(
                        body,
                        "resumePdf",
                        "challenge_application",
                        "resume",
                        applicationId
                );
                fileService.uploadFile(
                        body,
                        "coverLetterPdf",
                        "challenge_application",
                        "coverLetter",
                        applicationId
                );
            }

            return ok(editConfirmation.render(challengeId, applicationId, "BidChallenge"));

        } catch (Exception e) {
            Logger.error("ChallengeController.challengeApplyPOST failed", e);
            return ok(editError.render("Challenge"));
        }
    }

    /**
     * This method intends to submit the edit in the challenge status page.
     *
     * @param challengeId challenge id
     * @param challengeStatus: open, pending, close
     * @return
     */
    public Result challengeStatusChange(Long challengeId, String challengeStatus){
        checkLoginStatus();
        try {

            ObjectNode jsonData = JsonNodeFactory.instance.objectNode();
            jsonData.put("status", challengeStatus);
            System.out.println("Challenge id:"+ challengeId + " jsonData: " + jsonData.toString());
            JsonNode response = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.CHALLENGE_STATUS_UPDATE + challengeId), jsonData);

            if (response == null || response.has("error")) {
                Logger.debug("Cannot change status of this challenge");
                return redirect(routes.ChallengeController.challengeList(1, ""));
            }

            return ok(editConfirmation.render(challengeId, Long.parseLong("0"), "ChallengeOffer"));//TODO: 0 is entryId
        } catch (Exception e) {
            Logger.debug("ChallengeController challenge status update exception: " + e.toString());
            return ok(editError.render("Challenge"));
        }
    }

    public Result challengeApplicationStatusChange(Long challengeApplicationId, String challengeApplicationStatus) {
        checkLoginStatus();
        String userId = session("id");
        try {
            System.out.println("Updating ChallengeApplication status...");

            ObjectNode jsonData = JsonNodeFactory.instance.objectNode();
            jsonData.put("status", challengeApplicationStatus);

            System.out.println("ChallengeApplication ID: " + challengeApplicationId + " | New Status: " + jsonData.toString());

            JsonNode response = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.CHALLENGE_APPLICATION_STATUS_UPDATE + challengeApplicationId), jsonData);

            if (response == null || response.has("error")) {
                Logger.debug("Cannot update status of the Challenge job application.");
                return redirect(routes.ChallengeController.challengeListPostedByUser(1));
            } else if (response != null && !response.has("error")) {
                return sendOfferEmail(challengeApplicationId);
            }

            return ok(editConfirmation.render(challengeApplicationId, Long.parseLong("0"), "ChallengeOffer"));//TODO: 0 is entryId
        } catch (Exception e) {
            Logger.debug("challengeController challenge status update exception: " + e.toString());
            return ok(editError.render("Challengeapplication"));
        }
    }
    /************************************************** End of Challenge Apply *******************************************/



    /************************************************** Challenge List ***************************************************/

    /**
     * This method intends to prepare data for all challenges.
     *
     * @param pageNum
     * @param sortCriteria: sortCriteria on some fields. Could be empty if not specified at the first time.
     * @return: data for projectList.scala.html
     */
    @With(OperationLoggingAction.class)
    public Result challengeList(Integer pageNum, String sortCriteria) {
        checkLoginStatus();

        // If session contains the current projectId, set it.
        Challenge currentChallengeZone = challengeService.getCurrentChallengeZone();
        //Challenge currentProjectZone = challengeService.getCurrentProjectZone();

        // Set the offset and pageLimit.
        int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
        try {
            System.out.println(Constants.CHALLENGE_LIST + session("id") + "?pageNum=" +
                    pageNum + "&pageLimit=" + pageLimit + "&sortCriteria=" + sortCriteria);
            JsonNode challengeListJsonNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.CHALLENGE_LIST + session("id") + "?pageNum=" +
                            pageNum + "&pageLimit=" + pageLimit + "&sortCriteria=" + sortCriteria));
            System.out.println("challengeList: + : " + challengeListJsonNode);
            return challengeService.renderChallengeListPage(challengeListJsonNode,
                    currentChallengeZone, pageLimit, null, "all", session("username"),
                    Long.parseLong(session("id")));
        } catch (Exception e) {
            Logger.debug("ChallengeController.challengeList() exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }
    @With(OperationLoggingAction.class)
    public Result challengeListAppliedByUser(Integer pageNum) {
        checkLoginStatus();
        String userId = session("id");
        try {
            JsonNode challengesNode = RESTfulCalls.getAPI(
                    RESTfulCalls.getBackendAPIUrl(config, Constants.CHALLENGE_APPLIED_BY_USER + userId)
            );

            List<Challenge> challenges = new ArrayList<>();
            List<String> applicationStatuses = new ArrayList<>();
            int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
            int startIndex = ((int) pageNum - 1) * pageLimit;
            int endIndex = ((int) pageNum - 1) * pageLimit + pageLimit - 1;
            if (pageNum == (challengesNode.size() - 1) / pageLimit + 1) {
                // minus 1 for endIndex for preventing ArrayOutOfBoundException
                endIndex = challengesNode.size() - 1;
            }
            int count = endIndex - startIndex + 1;

            challenges = Challenge.deserializeJsonToChallengeList(challengesNode, startIndex, endIndex);

            for (int i = startIndex; i <= endIndex && i < challengesNode.size(); i++) {
                JsonNode challengeJson = challengesNode.get(i);
                applicationStatuses.add(challengeJson.get("challengeApplicationStatus").asText());

            }

            int beginIndexPagination = beginIndexForPagination(pageLimit, challengesNode.size(), (int) pageNum);
            int endIndexPagination = endIndexForPagination(pageLimit, challengesNode.size(), (int) pageNum);
            System.out.println(challengesNode);
            return ok(challengeListAppliedByUser.render(
                    challenges,
                    applicationStatuses,
                    (int) pageNum,
                    startIndex,
                    challengesNode.size(),
                    count,
                    pageLimit,
                    Long.parseLong(userId),
                    beginIndexPagination,
                    endIndexPagination));
        } catch (Exception e) {
            Logger.debug("ChallengeController.challengePostedByUser() exception: " + e.toString());

            e.printStackTrace();
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }
    @With(OperationLoggingAction.class)
    public Result challengeListAdmin(Integer pageNum, String sortCriteria) {
        checkLoginStatus();

        // If session contains the current projectId, set it.
        Challenge currentChallengeZone = challengeService.getCurrentChallengeZone();
        //Challenge currentProjectZone = challengeService.getCurrentProjectZone();

        // Set the offset and pageLimit.
        int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
        try {
            System.out.println(Constants.CHALLENGE_LIST_ADMIN + session("id") + "?pageNum=" +
                    pageNum + "&pageLimit=" + pageLimit + "&sortCriteria=" + sortCriteria);
            JsonNode challengeListJsonNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.CHALLENGE_LIST_ADMIN + session("id") + "?pageNum=" +
                            pageNum + "&pageLimit=" + pageLimit + "&sortCriteria=" + sortCriteria));
            System.out.println("challengeList: + : " + challengeListJsonNode);
            return challengeService.renderChallengeListPageAdmin(challengeListJsonNode,
                    currentChallengeZone, pageLimit, null, "all", session("username"),
                    Long.parseLong(session("id")));
        } catch (Exception e) {
            Logger.debug("ChallengeController.challengeList() exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }

    @With(OperationLoggingAction.class)
    public Result challengeApplicationList(Integer pageNum, String sortCriteria) {
        checkLoginStatus();

        // If session contains the current projectId, set it.
        Challenge currentChallengeZone = challengeService.getCurrentChallengeZone();
        //Challenge currentProjectZone = challengeService.getCurrentProjectZone();

        // Set the offset and pageLimit.
        int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
        try {
            System.out.println(Constants.CHALLENGE_LIST + session("id") + "?pageNum=" +
                    pageNum + "&pageLimit=" + pageLimit + "&sortCriteria=" + sortCriteria);
            JsonNode challengeListJsonNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.CHALLENGE_LIST + session("id") + "?pageNum=" +
                            pageNum + "&pageLimit=" + pageLimit + "&sortCriteria=" + sortCriteria));
            System.out.println("challengeList: + : " + challengeListJsonNode);
            return challengeService.renderChallengeListPage(challengeListJsonNode,
                    currentChallengeZone, pageLimit, null, "all", session("username"),
                    Long.parseLong(session("id")));
        } catch (Exception e) {
            Logger.debug("ChallengeController.challengeList() exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }

    /**
     * This method intends to prepare data for all challenge applications.
     *
     * @param pageNum
     * @param sortCriteria: sortCriteria on some fields. Could be empty if not specified at the first time.
     * @return: data for challengeApplicationsList.scala.html
     */
    @With(OperationLoggingAction.class)
    public Result challengeApplicationsList(String challengeType, Long challengeId, Integer pageNum, String sortCriteria) {
        checkLoginStatus();

        int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
        int offset = pageLimit * (pageNum - 1);

        if (null == challengeType) challengeType = "general";
        challengeType = challengeType.toLowerCase();
        JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                Constants.STR_BACKEND_URL_CHALLENGE_APPLICATIONS + challengeType + "/" + challengeId + "?offset=" + offset + "&pageLimit=" +
                        pageLimit + "&sortCriteria=" + sortCriteria + "&pageLimit=" + pageLimit + "&pageNum=" + pageNum));

        return challengeApplicationService.renderChallengeApplicationListPage(challengeType, response, pageLimit);
    }

    /************************************************** End of Challenge List *****************************************/

    /************************************************** My Posted Challenge *******************************************/
    /**
     * get all jobs posted by user
     * @param pageNum
     * @return Job list posted by current user
     */
    @With(OperationLoggingAction.class)
    public Result challengeListPostedByUser(Integer pageNum){
        checkLoginStatus();
        String userId = session("id");
        try{
            JsonNode jobsNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.CHALLENGE_POSTED_BY_USER + userId));
            List<Challenge> challenges = new ArrayList<Challenge>();
//            if (jobsNode.isNull() || jobsNode.has("error") || !jobsNode.isArray()) {
//
//                return ok(jobList.render(jobs, (int) pageNum,
//                        0, jobsNode.size(), 0, "search", 20,
//                        Long.parseLong(session("id")), 0, 0));
//            }
            int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
            int startIndex = ((int) pageNum - 1) * pageLimit;
            int endIndex = ((int) pageNum - 1) * pageLimit + pageLimit - 1;
            // Handle the last page, where there might be less than 50 item
            if (pageNum == (jobsNode.size() - 1) / pageLimit + 1) {
                // minus 1 for endIndex for preventing ArrayOutOfBoundException
                endIndex = jobsNode.size() - 1;
            }
            int count = endIndex - startIndex + 1;

            challenges = Challenge.deserializeJsonToChallengeList(jobsNode, startIndex, endIndex);

            int beginIndexPagination = beginIndexForPagination(pageLimit, jobsNode.size(), (int) pageNum);
            int endIndexPagination = endIndexForPagination(pageLimit, jobsNode.size(), (int) pageNum);
            return ok(challengeListPostedByUser.render(challenges,
                    (int) pageNum,
                    startIndex,
                    jobsNode.size(),
                    count,
                    pageLimit, Long.parseLong(session("id")), beginIndexPagination, endIndexPagination));
        }catch(Exception e){
            Logger.debug("ChallengeController.challengePostedByUser() exception: " + e.toString());
            e.printStackTrace();
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }

    /************************************************** End of My Posted Challenge ************************************/


    /************************************************** Challenge Detail *************************************************/

    /**
     * Ths method intends to return details of a challenge. If a challenge is not found, return to the all challenge page (page 1?).
     *
     * @param challengeId: challenge id
     * @return: Challenge, a list of team members to projectDetail.scala.html
     */
    @With(OperationLoggingAction.class)
    public Result challengeDetail(Long challengeId) {
        try {
            String userTypes = session("userTypes");
            if (userTypes == null) {
                return unauthorized("Unknown role or not authorized.");
            }
            Challenge challenge = challengeService.getChallengeById(challengeId);

            if (challenge == null) {
                Logger.debug("ChallengeController.challengeDetail() get null from backend");
                Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
                return ok(generalError.render());
            }

//        try {
//            Challenge parentProject = null;
//            Challenge challenge = challengeService.getChallengeById(challengeId);
////            Long parentProjectId = project.getParentChallengeId();
////            if (parentProjectId != null) {
////                if (parentProjectId != 0) {
////                    parentProject = challengeService.getChallengeById(parentProjectId);
////                }
////            }
//            System.out.println("challenge::::----" + challenge);
//            if (challenge == null) {
//                Logger.debug("ChallengeController.challengeDetail() get null from backend");
//                Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
//                return ok(generalError.render());
//            }

            // JsonNode teamMembersNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.GET_TEAM_MEMBERS_BY_PROJECT_ID + projectId));
            // challenge.setTeamMembers(Common.deserializeJsonToList(teamMembersNode, User.class));

            accessTimesService.AddOneTime("challenge", challengeId);
            Long userId = Long.parseLong(session("id"));
            String userIdStr = session("id");
            String tableName = "challenge";
            String challengeFileType = "challenge";
            String tableRecorderId = challengeId.toString();
            String backendPort = Constants.CMU_BACKEND_PORT;
            Boolean challengeDocument = fileService.checkFile(tableName, challengeFileType, tableRecorderId);
            return ok(challengeDetail.render(
                    challenge,
                    userTypes,
                    userId,
                    userIdStr,
                    tableName,
                    challengeFileType,
                    tableRecorderId,
                    backendPort,
                    challengeDocument));
        } catch (Exception e) {
            System.out.println("WTF is that");
            Logger.debug("ChallengeController.challengeDetail() exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }

    /**
     * Ths method intends to return details of a challenge application. If a challenge application is not found, return to the all challenge application page (page 1?).
     *
     * @param challengeApplicationId: challenge application id
     * @return: ChallengeJobApplication, a list of challenge application to challengeApplicationDetail.scala.html
     */
    @With(OperationLoggingAction.class)
    public Result challengeApplicationDetail(Long challengeApplicationId) {
        try {
            String userTypes = session("userTypes");
            if (userTypes == null) {
                return unauthorized("Unknown role or not authorized.");
            }
            ChallengeApplication challengeApplication = challengeApplicationService.getChallengeApplicationById(challengeApplicationId);

            if (challengeApplication == null) {
                Logger.debug("ChallengeController.challengeApplicationDetail() get null from backend");
                Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
                return ok(generalError.render());
            }

            accessTimesService.AddOneTime("challengeApplication", challengeApplicationId);
            String tableName = "challenge_application";
            String resumeFileType = "resume";
            String coverFileType  = "coverLetter";
            String tableRecorderId = challengeApplicationId.toString();
            String backendPort = Constants.CMU_BACKEND_PORT;
            Boolean resume = fileService.checkFile(tableName, resumeFileType, tableRecorderId);
            Boolean coverLetter = fileService.checkFile(tableName, coverFileType, tableRecorderId);
            return ok(challengeApplicationDetail.render(
                    challengeApplication,
                    userTypes,
                    tableName,
                    resumeFileType,
                    coverFileType,
                    tableRecorderId,
                    backendPort,
                    resume,
                    coverLetter));
        } catch (Exception e) {
            Logger.debug("ChallengeController.challengeApplicationDetail() exception: " + e.toString());
            e.printStackTrace();
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }
    @With(OperationLoggingAction.class)
    public Result challengeApplicationsDetail(Long challengeApplicationId) {
        try {
            String userTypes = session("userTypes");
            if (userTypes == null) {
                return unauthorized("Unknown role or not authorized.");
            }
            ChallengeApplication challengeApplication = challengeApplicationService.getChallengeApplicationIdById(challengeApplicationId);

            if (challengeApplication == null) {
                Logger.debug("ChallengeController.challengeApplicationDetail() get null from backend");
                Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
                return ok(generalError.render());
            }

            accessTimesService.AddOneTime("challengeApplication", challengeApplicationId);
            String tableName = "challenge_application";
            String resumeFileType = "resume";
            String coverFileType  = "coverLetter";
            String tableRecorderId = challengeApplicationId.toString();
            String backendPort = Constants.CMU_BACKEND_PORT;
            Boolean resume = fileService.checkFile(tableName, resumeFileType, tableRecorderId);
            Boolean coverLetter = fileService.checkFile(tableName, coverFileType, tableRecorderId);
            return ok(challengeApplicationDetail.render(
                    challengeApplication,
                    userTypes,
                    tableName,
                    resumeFileType,
                    coverFileType,
                    tableRecorderId,
                    backendPort,
                    resume,
                    coverLetter));
        } catch (Exception e) {
            Logger.debug("ChallengeController.challengeApplicationDetail() exception: " + e.toString());
            e.printStackTrace();
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }
    /************************************************** End of Challenge Detail *****************************************/

    /**
     * This method intends to render an empty search.scala.html page
     *
     * @return
     */
    public Result searchPage() {
        checkLoginStatus();

        return ok(challengeSearch.render());
    }

    public Result searchPOST(Integer pageNum, String sortCriteria) {
        checkLoginStatus();

        Challenge currentChallengeZone = challengeService.getCurrentChallengeZone();
        int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
        try {
            Map<String, String[]> formData = request().body().asFormUrlEncoded();

            String keywords = getFormValue(formData, "keywords");
            String name = getFormValue(formData, "name");
            String description = getFormValue(formData, "description");
            String location = getFormValue(formData, "location");
            String goals = getFormValue(formData, "goals");

            if (keywords == null || keywords.isEmpty()) {
                keywords = "";
            }
            if (name == null || name.isEmpty()) {
                name = "";
            }
            if (description == null || description.isEmpty()) {
                description = "";
            }
            if (location == null || location.isEmpty()) {
                location = "";
            }
            if (goals == null || goals.isEmpty()) {
                goals = "";
            }

            ObjectNode searchJson = Json.newObject();
            searchJson.put("keywords", keywords);
            searchJson.put("name", name);
            searchJson.put("description", description);
            searchJson.put("location", location);
            searchJson.put("goals", goals);

            Logger.info("Search Request JSON: " + searchJson);


            JsonNode challengeListJsonNode = RESTfulCalls.postAPI(
                    RESTfulCalls.getBackendAPIUrl(config, Constants.CHALLENGE_SEARCH_API + "?pageNum=" +
                            pageNum + "&pageLimit=" + pageLimit + "&sortCriteria=" + sortCriteria), searchJson
            );

            Logger.info("API response: " + challengeListJsonNode);
            return challengeService.renderChallengeListPage(challengeListJsonNode,
                    currentChallengeZone, pageLimit, null, "all", session("username"),
                    Long.parseLong(session("id")));

        } catch (Exception e) {
            Logger.error("ChallengeController.searchPOST() exception: ", e.toString());
            return redirect(routes.Application.home());
        }
    }
    private String getFormValue(Map<String, String[]> formData, String key) {
        return formData.getOrDefault(key, new String[]{""})[0].trim();
    }



/*************************************** Private Methods **************************************************************/

    /**
     * This method intends to inactivate the challenge by calling the backend
     *
     * @param projectId
     * @return redirect to the challenge list page
     */
    public Result deleteProject(long projectId) {
        checkLoginStatus();

        try {
            JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.DELETE_PROJECT_BY_ID + projectId));
            //Todo We have to decide what to do if for some reason the challenge could not get deactivated???
            return redirect(routes.ProjectController.projectList(1, ""));
        } catch (Exception e) {
            Logger.debug("ProjectController challenge delete exception: " + e.toString());
            return redirect(routes.ProjectController.projectList(1, ""));
        }
    }
    public Result closeChallenge(long challengeId) {
        checkLoginStatus();
        try {
            String userTypes = session("userTypes");
            if (userTypes == null) {
                return unauthorized("Unknown role or not authorized.");
            }
            JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.CLOSE_CHALLENGE_BY_ID + challengeId));
            // when the job is closed, redirect to the job list page
            return redirect(routes.ChallengeController.challengeList(1, ""));
        } catch (Exception e) {
            Logger.debug("ChallengeController Challenge close exception: " + e.toString());
            return redirect(routes.ChallengeController.challengeList(1, ""));
        }
    }
    public Result isProjectNameExisted() {
        JsonNode json = request().body().asJson();
        String title = json.path("title").asText();

        ObjectNode jsonData = Json.newObject();
        JsonNode response = null;

        try {
            jsonData.put("title", title);
            response = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.CHECK_PROJECT_NAME), jsonData);
            Application.flashMsg(response);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            Application.flashMsg(RESTfulCalls
                    .createResponse(RESTfulCalls.ResponseType.CONVERSIONERROR));
        } catch (Exception e) {
            e.printStackTrace();
            Application.flashMsg(RESTfulCalls.createResponse(RESTfulCalls.ResponseType.UNKNOWN));
        }
        return ok(response);
    }

    /**
     * This method receives a challenge Id and the image number in the description of the challenge and uploads
     * this image to aws by calling backen and return the received URL for the uploaded image
     *
     * @param projectId   challenge Id
     * @param imageNumber image number in the description of the challenge
     * @return the received URL for the uploaded image if the upload is successful
     */
    public Result uploadDescriptionImage(long projectId, int imageNumber) {
        Http.MultipartFormData<File> body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart<File> picture = body.getFile("file");
        try {
            if (picture != null) {
                File f = picture.getFile();
                JsonNode response =
                        RESTfulCalls.postAPIWithFile(RESTfulCalls.getBackendAPIUrl(config,
                                Constants.SAVE_PROJECT_DESCRIPTION_IMG + projectId + "/" + imageNumber), f);
                return ok(response.asText());
            } else return null;
        } catch (Exception e) {
            Logger.debug(e.getStackTrace() + "");
            return badRequest();
        }
    }

    /**
     * This method receives a challenge Id and the image number in the description of the challenge along with the
     * current image index in the description and renames the file on S3 bucket to have the new imageNumber as
     * the index by calling the backend and return the received URL for the uploaded image to be replaced for
     * the src in the img tag in description
     *
     * @param projectId          challenge Id
     * @param imageNumber        image number in the description of the challenge
     * @param currentImageNumber current image index number in the description of the challenge
     * @return the received URL for the uploaded image if the upload is successful
     */
    public Result renameDescriptionImage(long projectId, int imageNumber, int currentImageNumber) {
        try {
            Logger.debug("rename challenge image description ");
            JsonNode response =
                    RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                            Constants.RENAME_PROJECT_DESCRIPTION_IMG + projectId + "/" + imageNumber + "/" +
                                    currentImageNumber));
            Logger.debug(response + "");
            return ok(response.asText());
        } catch (Exception e) {
            Logger.debug(e.getStackTrace() + "");
            return badRequest();
        }
    }

    /*********************************** END Basic refactoring ********************************************************/

    /**
     *
     * @return
     */
    public Result getProjectLists() {
        checkLoginStatus();
        ArrayNode projectsList = Json.newArray();
        JsonNode projectsNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                Constants.GET_ALL_ACTIVE_PROJECTS));
        // if no value is returned or error or is not json array

        ObjectMapper mapper = new ObjectMapper();
        // parse the json string into object
        for (int i = 0; i < projectsNode.size(); i++) {
            JsonNode json = projectsNode.path(i);
            ObjectNode jsonData = mapper.createObjectNode();
            jsonData.put("id", json.findPath("id").asLong());
            jsonData.put("text", json.findPath("title").asText());
            projectsList.add(jsonData);
        }

        return ok(projectsList);
    }

    /**
     *
     * @param id
     * @return
     */
    public Result isProjectExist(String id) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        if (pattern.matcher(id).matches()) {
            JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.GET_PROJECT_BY_ID + Long.parseLong(id)));
            if (response == null || response.has("error")) {
                return badRequest("cannot find challenge");
            } else return ok(response.findPath("id"));
        } else return badRequest("cannot find challenge");
    }


    /***
     * This method receives a notebook id together with a list of projects from the creator of them and associate them to the given notebook
     * @param notebookId given notebook id
     * json parameters in the request body:
     *           projects: array of challenge Ids
     * @return
     */
    public Result associateProjectsToNotebook(Long notebookId) {
        try {
            JsonNode json = request().body().asJson();
            JsonNode res = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.ASSOCIATE_PROJECT_TO_NOTEBOOK + notebookId + "/" + session("id")), json);

            return ok(res);
        } catch (Exception e) {
            e.printStackTrace();
            return redirect(routes.Application.home());
        }
    }

    /**
     *
     * @param pageNum
     * @param sortCriteria
     * @return
     */
//    public Result getMyEnrolledProjects(int pageNum, String sortCriteria) {
//        try {
//
//            Long userId = Long.parseLong(session("id"));
//
//            int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
//            JsonNode projectListJsonNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.GET_MY_ENROLLED_PROJECTS +
//                    "?pageNum=" + pageNum + "&pageLimit=" + pageLimit + "&sortCriteria=" + sortCriteria + "&userId=" + userId));
//            Challenge currentProjectZone = challengeService.getCurrentProjectZone();
//            return challengeService.renderProjectListPage(projectListJsonNode, currentProjectZone, pageLimit, null,
//                    "my enroll", session("username"), Long.parseLong(session("id")));
//        } catch (Exception e) {
//            Logger.debug("ProjectController.getMyEnrolledProjects: " + e.toString());
//            return redirect(routes.Application.home());
//        }
//    }

    /**
     * This method aims to set GeoNEX as challenge zone. (e.g., when clicking on GeoNEX at the top menu)
     * @return
     */
    public Result setProjectZoneAsGeoNEX() {
        try {
            JsonNode geoNEXId = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.GET_PROJECT_ID_BY_NAME +
                    "GeoNEX"));
            session("projectId", String.valueOf(geoNEXId));

            return redirect(routes.Application.home());
        } catch (Exception e) {
            Logger.debug("Application.updateProjectZone exception: " + e.toString());
            session("projectId", "0");
            return redirect(routes.Application.home());
        }
    }

    /**
     *
     * @param id
     * @param eventListString
     * @return
     */
    public Result followedByUser(Long id, String eventListString) {
        checkLoginStatus();
        String projectId = String.valueOf(id);
        List<String> eventTypes = new ArrayList<>();
        String[] eventList = eventListString.split(";");

        for (String event : eventList) {
            eventTypes.add(event);
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < eventTypes.size() - 1; i++) {
            sb.append(eventTypes.get(i)).append(";");
        }

        if (!eventTypes.isEmpty()) {
            sb.append(eventTypes.get(eventTypes.size() - 1));
        }

        String url = RESTfulCalls.getBackendAPIUrl(config, Constants.PROJECT_FOLLOWED_BY_USER
                + "/" + projectId + "/" + session("id") + "/" + sb.toString());
        JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.PROJECT_FOLLOWED_BY_USER
                + "/" + projectId + "/" + session("id") + "/" + sb.toString()));
        if (response.has("error")) {
            Application.flashMsg(RESTfulCalls.createResponse(RESTfulCalls.ResponseType.UNKNOWN));
            return challengeList(1, "");
        }


        return challengeList(1, "");
    }

    /**
     *
     * @param id
     * @param eventListString
     * @return
     */
    public Result unFollowedByUser(Long id, String eventListString) {
        checkLoginStatus();

        String projectId = String.valueOf(id);
        List<String> eventTypes = new ArrayList<>();
        String[] eventList = eventListString.split(";");

        for (String event : eventList) {
            eventTypes.add(event);
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < eventTypes.size() - 1; i++) {
            sb.append(eventTypes.get(i)).append(";");
        }

        if (!eventTypes.isEmpty()) {
            sb.append(eventTypes.get(eventTypes.size() - 1));
        }

        String url = RESTfulCalls.getBackendAPIUrl(config, Constants.PROJECT_UNFOLLOWED_BY_USER
                + "/" + projectId + "/" + session("id") + "/" + sb.toString());
        JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                Constants.PROJECT_UNFOLLOWED_BY_USER
                        + "/" + projectId + "/" + session("id") + "/" + sb.toString()));

        if (response.has("error")) {
            Application.flashMsg(RESTfulCalls.createResponse(RESTfulCalls.ResponseType.UNKNOWN));
            return challengeList(1, "");
        }

        return challengeList(1, "");
    }

    /**
     * This method intends to prepare data to render the page of listing my followed projects with pagination
     *
     * @param page:         current page number
     * @param sortCriteria: sort criteria
     * @return: data for projectList.scala.html
     */
//    public Result myFollowedProjects(Integer page, String sortCriteria) {
//        Challenge currentProjectZone = challengeService.getCurrentProjectZone();
//        try {
//            int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
//            int offset = pageLimit * (page - 1);
//            JsonNode projectsJsonNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
//                    Constants.MY_FOLLOWED_PROJECTS + "?offset=" + offset + "&pageLimit=" +
//                            pageLimit + "&sortCriteria=" + sortCriteria + "&uid=" + session("id")));
//            return challengeService.renderProjectListPage(projectsJsonNode, currentProjectZone, pageLimit,
//                    null, "my follow", session("username"), Long.parseLong(session("id")));
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return redirect(routes.Application.home());
//        }
//    }

    /**
     * This method intends to check whether the input parent challenge id is valid
     *
     * @param parentProjectId: parent challenge id
     * @return: json result
     */
    public Result checkParentProject(String parentProjectId) {

        if (!StringUtils.isNumeric(parentProjectId)) {
            ObjectNode jsonData = Json.newObject();
            jsonData.put("badFormat", "Bad input format");
            return ok(jsonData);
        }
        JsonNode result = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                Constants.CHECK_PROJECT_EXIST + Long.valueOf(parentProjectId)));
        return ok(result);
    }

    /**
     * This method intends to render addProjectFollowersPage
     *
     * @param id challenge id
     * @return: addProjectFollowersPage
     */
//    public Result addProjectFollowersPage(Long id) {
//        try {
//            Challenge challenge = challengeService.getProjectById(id);
//            JsonNode userNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(
//                    config, Constants.GET_USER_PROFILE_BY_ID + challenge.getCreator()));
//            User creator = User.deserialize(userNode);
//            JsonNode followersNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(
//                    config, Constants.GET_FOLLOWERS_FOR_PROJECT + id));
//            List<User> followers = new ArrayList<>();
//            for (JsonNode follower : followersNode) {
//                followers.add(User.deserialize(follower));
//            }
//            return ok(addProjectFollowers.render(challenge, followers, creator));
//        } catch (Exception e) {
//            e.printStackTrace();
//            return redirect(routes.ProjectController.projectDetail(id));
//        }
//    }

    /**
     * This method intends to add one follower to a private challenge
     *
     * @return: addProjectFollowersPage
     */
    public Result addOneFollower(Long id, String event) {
        try {
            DynamicForm df = myFactory.form().bindFromRequest();
            Http.MultipartFormData body = request().body().asMultipartFormData();
            if (!Common.validate(df.field("email").value())) {
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode jsonData = mapper.createObjectNode();
                jsonData.put("error", "Invalid email format");

                return ok(jsonData);
            }

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode jsonData = mapper.createObjectNode();
            jsonData.put("firstName", df.field("firstName").value());
            jsonData.put("lastName", df.field("lastName").value());
            jsonData.put("email", df.field("email").value());

            JsonNode response = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.ADD_ONE_FOLLOWER_PROJECT + id + "/" + event), jsonData);

            return ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return redirect(routes.ProjectController.projectDetail(id));
        }
    }

    /**
     * This method intends to delete one follower of a private challenge
     *
     * @return: deleteProjectFollowersPage
     */
    public Result deleteOneFollower(Long followerId, Long projectId, String eventType) {
        try {

            JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.DELETE_ONE_FOLLOWER_PROJECT + followerId + "/" + projectId + "/" + eventType));
            return ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return redirect(routes.ProjectController.projectDetail(projectId));
        }
    }
    public Result sendOfferEmail(Long challengeApplicationId) {
        try {
            ObjectNode jsonData = Json.newObject();
            jsonData.put("challengeApplicationId", challengeApplicationId);

            JsonNode response = RESTfulCalls
                    .postAPI(RESTfulCalls.getBackendAPIUrl(config, "/challenge/offer"), jsonData);

//        Logger.info("sendRegisterEmail: Received response from validation API = " + response);
            return ok(editConfirmation.render(challengeApplicationId, Long.parseLong("0"), "SendOffer")); // TODO: 0 is entryId
        } catch (Exception e) {
            Logger.debug("challengeController challenge status update exception: " + e.toString());
            return ok(editError.render("Challengeapplication"));
        }
    }
}