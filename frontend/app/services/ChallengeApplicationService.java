package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import controllers.routes;
import models.*;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import utils.Constants;
import utils.RESTfulCalls;
import views.html.challengeList;
import views.html.jobApplicationList;
import views.html.raJobApplicationList;
import views.html.challengeApplicationList;


import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static play.mvc.Controller.session;
import static play.mvc.Results.ok;
import static play.mvc.Results.redirect;
import static utils.Common.beginIndexForPagination;
import static utils.Common.endIndexForPagination;

/**
 * This class intends to provide support for ChallengeController.
 */
public class ChallengeApplicationService {
    @Inject
    Config config;

    private final UserService userService;
    private Form<Challenge> challengeForm;

    @Inject
    public ChallengeApplicationService(UserService userService) {
        this.userService = userService;
    }

    /**
     * This method returns the current ChallengeZone. Default challenge zone is OpenNEX (0).
     * OpenNEX challenge id = 0; private zone challenge id < 0
     *
     * @return Challenge current ChallengeZone
     */
    public Challenge getCurrentChallengeZone() {
        Challenge currentChallengeZone = null;
        if (session("challengeId") != null && Long.parseLong(session("challengeId")) > 0) {
            currentChallengeZone = getChallengeById(Long.parseLong(session("challengeId")));
        }
        return currentChallengeZone;
    }


    /**
     * This method intends to get Challenge by id by calling backend APIs.
     *
     * @param challengeId
     * @return Challenge
     */
    public Challenge getChallengeById(Long challengeId) {
        Challenge challenge = null;
        try {

            JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.GET_CHALLENGE_BY_ID + challengeId));
            if (response.has("error")) {
                Logger.debug("ChallengeService.getChallengeById() did not get challenge from backend with error.");
                return null;
            }
            System.out.println("response: ==" + response.toString());

            challenge = Challenge.deserialize(response);

            System.out.println("challenge: "+ challenge);
            if (challenge.getChallengePublisher() == null) {
                Logger.debug("ChallengeService.getChallengeById() creator is null");
                throw new Exception("ChallengeService.getChallengeById() creator is null");
            }
        } catch (Exception e) {
            Logger.debug("ChallengeService.getChallengeById() exception: " + e.toString());
            return null;
        }
        return challenge;
    }

    /**
     * This method intends to get Challenge application by id by calling backend APIs.
     *
     * @param challengeApplicationId: input the challenge application id
     * @return Job
     */
    public ChallengeApplication getChallengeApplicationById(Long challengeApplicationId) {
        ChallengeApplication challengeApplication = null;

        try {

            JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.GET_CHALLENGE_APPLICATION_BY_ID + challengeApplicationId));

            if (response.has("error")) {
                Logger.debug("ChallengeApplication.getChallengeApplicationById() did not get job from backend with error.");
                return null;
            }
            System.out.println("response: ==" + response.toString());

            challengeApplication = challengeApplication.deserialize(response);

            System.out.println("challengeApplication: "+ challengeApplication);
            if (challengeApplication.getApplicant() == null) {
                Logger.debug("ChallengeService.getApplicant() creator is null");
                throw new Exception("ChallengeService.getChallengeById() applicant is null");
            }

            if (challengeApplication.getAppliedChallenge() == null) {
                Logger.debug("ChallengeService.getAppliedChallenge() creator is null");
                throw new Exception("ChallengeService.getAppliedChallenge() creator is null");
            }

        } catch (Exception e) {
            Logger.debug("ChallengeService.getChallengeById() exception: " + e.toString());
            return null;
        }
        return challengeApplication;
    }

    public ChallengeApplication getChallengeApplicationIdById(Long challengeApplicationId) {
        ChallengeApplication challengeApplication = null;

        try {

            JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.GET_CHALLENGE_APPLICATION_ID_BY_ID + challengeApplicationId));

            if (response.has("error")) {
                Logger.debug("ChallengeApplication.getChallengeApplicationById() did not get job from backend with error.");
                return null;
            }
            System.out.println("response: ==" + response.toString());

            challengeApplication = challengeApplication.deserialize(response);

            System.out.println("challengeApplication: "+ challengeApplication);
            if (challengeApplication.getApplicant() == null) {
                Logger.debug("ChallengeService.getApplicant() creator is null");
                throw new Exception("ChallengeService.getChallengeById() applicant is null");
            }

            if (challengeApplication.getAppliedChallenge() == null) {
                Logger.debug("ChallengeService.getAppliedChallenge() creator is null");
                throw new Exception("ChallengeService.getAppliedChallenge() creator is null");
            }

        } catch (Exception e) {
            Logger.debug("ChallengeService.getChallengeById() exception: " + e.toString());
            return null;
        }
        return challengeApplication;
    }

    /**
     * This method intends to get all challenges by a creator logged into the system.
     *
     * @return
     */
//    public ArrayList<Challenge> getChallengesByCreator() {
//        try {
//            JsonNode challenges = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
//                    Constants.GET_CHALLENGE_BY_CREATOR
//                    + session("id")));
//            if (challenges == null || challenges.has("error")) return null;
//            return Challenge.deserializeJsonArrayToChallengeList(challenges);
//        } catch (Exception e) {
//            Logger.debug("ChallengeService.getChallengesByCreator exception: " + e.toString());
//            return null;
//        }
//    }


    /**
     * This method intends to save a picture to challenge.
     *
     * @param body
     * @param challengeId: challenge id
     * @throws Exception
     */
//    public void savePictureToChallenge(Http.MultipartFormData body, Long challengeId) throws Exception {
//        try {
//            if (body.getFile("picture") != null) {
//                Http.MultipartFormData.FilePart image = body.getFile("picture");
//                if (image != null && !image.getFilename().equals("")) {
//                    File file = (File) image.getFile();
//                    JsonNode imgResponse = RESTfulCalls.postAPIWithFile(RESTfulCalls.getBackendAPIUrl(config,
//                            Constants.SET_Challenge_IMAGE + challengeId), file);
//                }
//            }
//        } catch (Exception e) {
//            Logger.debug("ChallengeService.savePictureToChallenge exception: " + e.toString());
//            throw e;
//        }
//    }

    /**
     * This method intends to save a pdf to challenge.
     *
     * @param body
     * @param challengeId: challenge id
     * @throws Exception
     */
//    public void savePDFToChallenge(Http.MultipartFormData body, Long challengeId) throws Exception {
//        try {
//            if (body.getFile("pdf") != null) {
//                Http.MultipartFormData.FilePart pdf = body.getFile("pdf");
//                if (pdf != null && !pdf.getFilename().equals("")) {
//                    File file = (File) pdf.getFile();
//                    JsonNode pdfResponse = RESTfulCalls.postAPIWithFile(RESTfulCalls.getBackendAPIUrl(config,
//                            Constants.SET_Challenge_PDF + challengeId), file);
//                }
//            }
//        } catch (Exception e) {
//            Logger.debug("ChallengeService.savePDFToChallenge exception: " + e.toString());
//            throw e;
//        }
//    }

    /**
     * This method intends to add a list of team members to a challenge, from challenge registration form.
     *
     * @param ChallengeForm: challenge registration form
     * @param body
     * @param challengeId:   challenge id
     */
    public void addTeamMembersToChallenge(Form<Challenge> ChallengeForm, Http.MultipartFormData body, Long challengeId) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            int count = Integer.parseInt(ChallengeForm.field("count").value()); //the number of team members in the challenge
            for (int i = 0; i < count; i++) {
                if (ChallengeForm.field("member" + i) != null) {
                    ObjectNode memberData = mapper.createObjectNode();
                    memberData.put("name", ChallengeForm.field("member" + i).value());
                    memberData.put("email", ChallengeForm.field("email" + i).value());
                    JsonNode memberRes = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                            Constants.ADD_TEAM_MEMBER + challengeId), memberData);
                    if (body.getFile("photo" + i) != null) {
                        Http.MultipartFormData.FilePart photo = body.getFile("photo" + i);
                        if (photo != null && !photo.getFilename().equals("")) {
                            File memphoto = (File) photo.getFile();
                            JsonNode photoRes =
                                    RESTfulCalls.postAPIWithFile(RESTfulCalls.getBackendAPIUrl(config,
                                            Constants.SET_TEAM_MEMBER_PHOTO
                                            + memberRes.asLong()), memphoto);
                        }
                    }
                    userService.createUserbyAddingTeamMember(ChallengeForm.field("member" + i).value(),
                            ChallengeForm.field("email" + i).value());
                }
            }
        } catch (Exception e) {
            Logger.debug("ChallengeService.addTeamMembersToChallenge exception: " + e.toString());
            throw e;
        }
    }

    /**
     * This method intends to add a list of team members to a challenge, from challenge registration form.
     *
     * @param ChallengeForm: challenge registration form
     */
    public void deleteTeamMembersToChallenge(Form<Challenge> ChallengeForm) {
        try {
            int deleteCount = 0;
            if (ChallengeForm.field("delc").value() != null && ChallengeForm.field("delc").value().trim() != "")
                deleteCount = Integer.parseInt(ChallengeForm.field("delc").value());
            //delete chosen team members
            for (int i = 0; i < deleteCount; i++) {
                Long deleteTeamMemberId = Long.parseLong(ChallengeForm.field("delete" + i).value());
                JsonNode deleteResponse = RESTfulCalls.deleteAPI(RESTfulCalls.getBackendAPIUrl(config,
                        Constants.DELETE_TEAM_MEMBER + deleteTeamMemberId));
            }
        } catch (Exception e) {
            Logger.debug("ChallengeService.deleteTeamMembersToChallenge exception: " + e.toString());
            throw e;
        }
    }





    /************************************************ Page Render Preparation *****************************************/
    /**
     * This private method renders the challenge list page.
     * Note that for performance consideration, the backend only passes back the challenges for the needed page stored in
     * the ChallengeListJsonNode, together with the offset/count/total/sortCriteria information.
     *
     * @param ChallengeListJsonNode
     * @param currentChallengeZone
     * @param pageLimit
     * @param searchBody
     * @param listType            : "all"; "search" (draw this page from list function or from search function)
     * @param username
     * @param userId
     * @return render challenge list page; If exception happened then render the homepage
     */
    public Result renderChallengeListPage(JsonNode ChallengeListJsonNode,
                                        Challenge currentChallengeZone,
                                        int pageLimit,
                                        String searchBody,
                                        String listType,
                                        String username,
                                        Long userId) {
        try {
            // if no value is returned or error
            if (ChallengeListJsonNode == null || ChallengeListJsonNode.has("error")) {
                Logger.debug("Challenge list is empty or error!");
                return redirect(routes.Application.home());
            }

            JsonNode ChallengesJsonArray = ChallengeListJsonNode.get("items");
            if (!ChallengesJsonArray.isArray()) {
                Logger.debug("Challenge list is not array!");
                return redirect(routes.Application.home());
            }

            List<Challenge> challenges = new ArrayList<>();
            for (int i = 0; i < ChallengesJsonArray.size(); i++) {
                JsonNode json = ChallengesJsonArray.path(i);
                Challenge challenge = Challenge.deserialize(json);
                challenges.add(challenge);
            }

            // offset: starting index of the pageNum; count: the number of items in the pageNum;
            // total: the total number of items in all pages; sortCriteria: the sorting criteria (field)
            // pageNum: the current page number
            String sortCriteria = ChallengeListJsonNode.get("sort").asText();

            int total = ChallengeListJsonNode.get("total").asInt();
            int count = ChallengeListJsonNode.get("count").asInt();
            int offset = ChallengeListJsonNode.get("offset").asInt();
            int pageNum = offset / pageLimit + 1;

            int beginIndexPagination = beginIndexForPagination(pageLimit, total, pageNum);
            int endIndexPagination = endIndexForPagination(pageLimit, total, pageNum);

            return ok(challengeList.render(challenges, currentChallengeZone, pageNum, sortCriteria, offset, total, count,
                    listType, pageLimit, searchBody, username, userId, beginIndexPagination, endIndexPagination));
        } catch (Exception e) {
            Logger.debug("Exception in renderChallengeListPage:" + e.toString());
            e.printStackTrace();
            return redirect(routes.Application.home());
        }
    }

    public Result renderChallengeApplicationListPage(String challengeType, JsonNode challengeApplicationsNode, int pageLimit) {
        try {
            if (challengeApplicationsNode == null || challengeApplicationsNode.has("error")) {
                Logger.debug("Challenge application list is empty or error!");
                return redirect(routes.Application.home());
            }

            JsonNode challengeApplicationJsonArray = challengeApplicationsNode.get("items");

            if (!challengeApplicationJsonArray.isArray()) {
                Logger.debug("Challenge application list is not array!");
                return redirect(routes.Application.home());
            }

            // Offset
            String retSort = challengeApplicationsNode.get("sort").asText();
            int total = challengeApplicationsNode.get("total").asInt();
            int count = challengeApplicationsNode.get("count").asInt();
            int offset = challengeApplicationsNode.get("offset").asInt();
            int page = offset/pageLimit + 1;
            int beginIndexPagination = beginIndexForPagination(pageLimit, total, page);
            int endIndexPagination = endIndexForPagination(pageLimit, total, page);

            List<ChallengeApplication> challengeApplications = new ArrayList<>();
            List<RAJobApplication> raJobApplications = new ArrayList<>();
//            List<TAJobApplication> taJobApplications = new ArrayList<>();

            for (int i = 0; i < challengeApplicationJsonArray.size(); i++) {
                JsonNode json = challengeApplicationJsonArray.path(i);
                if ("specialChallenge".equals(challengeType))
                    raJobApplications.add(RAJobApplication.deserialize(json));
                else
                    challengeApplications.add(ChallengeApplication.deserialize(json));
            }

            if ("rajob".equals(challengeType))
                return ok(raJobApplicationList.render(raJobApplications, page, retSort, offset, total, count, "", pageLimit, "", Long.valueOf(session("id")), beginIndexPagination, endIndexPagination));
            else
                return ok(challengeApplicationList.render(challengeApplications, page, retSort, offset, total, count, "", pageLimit, "", Long.valueOf(session("id")), beginIndexPagination, endIndexPagination));
        } catch (Exception e) {
            Logger.debug("UserService.renderUserListPage exception: " + e.toString());
            return redirect(routes.Application.home());
        }
    }


    /**************************************************** (De)Serialization *******************************************/
    /**
     * This method intends to prepare a json object from Challenge form.
     *
     * @param ChallengeApplicationForm: challenge registration form
     * @return
     * @throws Exception
     */
    public ObjectNode serializeFormToJson(Form<ChallengeApplication> ChallengeApplicationForm, Long challengeId) throws Exception {
        ObjectNode jsonData = null;
        System.out.println("challenge application form: " + ChallengeApplicationForm.toString());
        try {
            Map<String, String> tmpMap = ChallengeApplicationForm.data();
            jsonData = (ObjectNode) (Json.toJson(tmpMap));

            if (ChallengeApplicationForm.field("markAsPrivate").value() != null && ChallengeApplicationForm.field(
                    "markAsPrivate").value().equals("on")) {
                jsonData.put("authentication", "private");
            } else {
                jsonData.put("authentication", "public");

            }

            User user = new User(Long.parseLong(session("id")));
            jsonData.put("applicant", Json.toJson(user));

            Challenge challenge = new Challenge(challengeId);
            jsonData.put("appliedChallenge", Json.toJson(challenge));

        } catch (Exception e) {
            Logger.debug("ChallengeApplicationService.serializeFormToJson exception: " + e.toString());
            throw e;
        }

        return jsonData;
    }

}
