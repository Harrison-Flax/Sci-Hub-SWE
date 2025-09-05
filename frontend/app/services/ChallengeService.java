package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import controllers.routes;
import models.TAJob;
import models.User;
import models.Challenge;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import utils.Constants;
import utils.RESTfulCalls;
import views.html.challengeList;
import views.html.challengeListAdmin;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static play.mvc.Controller.session;
import static play.mvc.Results.ok;
import static play.mvc.Results.redirect;
import static utils.Common.beginIndexForPagination;
import static utils.Common.endIndexForPagination;

/**
 * This class intends to provide support for ChallengeController.
 */
public class ChallengeService {
    @Inject
    Config config;

    private final UserService userService;
    private Form<Challenge> challengeForm;
    private Exception exception;

    @Inject
    public ChallengeService(UserService userService) {
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

            challenge = Challenge.deserialize(response);

            System.out.println("challenge: "+ challenge);
            if (challenge.getChallengePublisher() == null) {
                Logger.debug("ChallengeService.getChallengeById() creator is null");
                throw exception;
            }

            File responseImg = RESTfulCalls.getAPIAsFile(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.CHALLENGE_PICTURE_GET + challengeId));

            if (responseImg == null) {
                Logger.debug("challenge does not get its img.");
                challenge.setChallengeImage(challenge.getAvatar());
            }else{
                String fileSeparator = File.separator;

                String filePath =   "."+ fileSeparator + "public" + fileSeparator + "images" + fileSeparator + "challenges" + fileSeparator + "img" + fileSeparator;
                File fileDirs= new File(filePath);
                if(!fileDirs.exists()){
                    fileDirs.mkdirs();
                }
                filePath = filePath + challengeId + ".jpg";


                // String newImgPath = responseImg.getAbsolutePath()+".jpg";
                responseImg.renameTo(new File(filePath));
                challenge.setChallengeImage(filePath);
                System.out.println("my current file path" + responseImg.getAbsolutePath());
            }


        } catch (Exception e) {
            Logger.debug("ChallengeService.getChallengeById() exception: " + e.toString());
            return null;
        }
        return challenge;
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
    public Result renderChallengeListPageAdmin(JsonNode ChallengeListJsonNode,
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

            return ok(challengeListAdmin.render(challenges, currentChallengeZone, pageNum, sortCriteria, offset, total, count,
                    listType, pageLimit, searchBody, username, userId, beginIndexPagination, endIndexPagination));
        } catch (Exception e) {
            Logger.debug("Exception in renderChallengeListPage:" + e.toString());
            e.printStackTrace();
            return redirect(routes.Application.home());
        }
    }

    /**
     * This method add challenge form and add a new challenge picture accordingly
     *
     * @param requestBody request body
     * @return json node showing the result got from backend regarding the image update
     * or null if not image update was requested in the form or the image update doesn't go through
     * TODO: sometime we have to distinguish between the case that image was not updated because of an
     * error or no image update was requested
     */
    public JsonNode uploadChallengeFile(long challengeId, String fileType, Http.RequestBody requestBody) {
        try {
            Http.MultipartFormData body = requestBody.asMultipartFormData();
            if (fileType == "Image" && body.getFile("challengeImage") != null) {
                Http.MultipartFormData.FilePart image = body.getFile("challengeImage");
                if (image != null && !image.getFilename().equals("")) {
                    // Logger.info("Received Image File: " + image.getFilename());
                    File file = (File) image.getFile();
                    return RESTfulCalls.postAPIWithFile(RESTfulCalls.getBackendAPIUrl(config,
                                Constants.CHALLENGE_PICTURE_POST + challengeId), file);

                }
            }
            if ( fileType == "Pdf" && body.getFile("challengePdf") != null) {
                Http.MultipartFormData.FilePart pdf = body.getFile("challengePdf");
                if (pdf != null && !pdf.getFilename().equals("")) {
                    // Logger.info("Received Image File: " + image.getFilename());
                    File file = (File) pdf.getFile();
                    return RESTfulCalls.postAPIWithFile(RESTfulCalls.getBackendAPIUrl(config,
                            Constants.CHALLENGE_PDF_POST + challengeId), file);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**************************************************** (De)Serialization *******************************************/
    /**
     * This method intends to prepare a json object from Challenge form.
     *
     * @param ChallengeForm: challenge registration form
     * @return
     * @throws Exception
     */
    public JsonNode serializeFormToJson(Form<Challenge> ChallengeForm) throws Exception {
        JsonNode jsonData = null;
        try {
            Challenge challenge = ChallengeForm.get();
            String longDescription = challenge.getLongDescription();
            if (longDescription != null) {
                longDescription.replaceAll(
                        "\n", "").replaceAll("\r", "");
            }

            if (challenge.getChallengePublisher() == null) {
                User user = new User(Long.parseLong(session("id")));
                challenge.setChallengePublisher(user);
            }
            return Json.toJson(challenge);

        } catch (Exception e) {
            Logger.debug("ChallengeService.serializeFormToJson exception: " + e.toString());
            throw e;
        }
    }

}
