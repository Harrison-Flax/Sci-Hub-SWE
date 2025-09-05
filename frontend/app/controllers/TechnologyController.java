package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
import services.*;
import services.ChallengeService;
import utils.Common;
import utils.Constants;
import utils.RESTfulCalls;
import views.html.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static controllers.Application.checkLoginStatus;
import static utils.Common.beginIndexForPagination;
import static utils.Common.endIndexForPagination;

public class TechnologyController extends Controller {

    @Inject
    Config config;

    private final TechnologyService technologyService;

    private final UserService userService;
    private final AccessTimesService accessTimesService;

    private Form<Technology> technologyFormTemplate;
    private FormFactory myFactory;



    /******************************* Constructor **********************************************************************/
    @Inject
    public TechnologyController(FormFactory factory,
                                TechnologyService challengeService,
                               UserService userService, AccessTimesService accessTimesService) {
        technologyFormTemplate = factory.form(Technology.class);
        myFactory = factory;

        this.technologyService = challengeService;
        this.userService = userService;
        this.accessTimesService = accessTimesService;
    }


    /************************************************** Challenge Registration ******************************************/

    /**
     * This method intends to render the challenge registration page.
     *
     * @return
     */
    public Result technologyRegisterPage() {
        checkLoginStatus();
        return ok(technologyRegister.render());
    }

    /**
     * This method intends to gather technology registration information and create a technology in database.
     *
     * @return
     */
    public Result technologyRegisterPOST() {
        checkLoginStatus();
        try {
            Form<Technology> technologyForm = technologyFormTemplate.bindFromRequest();
            Http.MultipartFormData<File> body = request().body().asMultipartFormData();
            Http.MultipartFormData.FilePart filePart = body.getFile("pdf");
            JsonNode jsonData = technologyService.serializeFormToJson(technologyForm);
            System.out.println(">>>>1.5:" + jsonData.toString());
            JsonNode response = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.TECHNOLOGY_REGISTER_POST), jsonData);
            if (response == null || response.has("error")) {
                Logger.debug("TechnologyController.technologyRegisterPOST: Cannot create the technology in backend");
                return ok(registrationError.render("Technology"));
            }
            long serviceId = response.asLong();
            if (filePart != null) {
                String fileName = filePart.getFilename();
                String fileType = fileName.substring(fileName.lastIndexOf('.') + 1);
                java.io.File file = (java.io.File) filePart.getFile();
                RESTfulCalls.postAPIWithFile(RESTfulCalls.getBackendAPIUrl(config, Constants.TECHNOLOGY_REGISTER_FILE_POST + serviceId + "?fileName=" + fileName + "&fileType=" + fileType), file);
            }

            long technologyId = response.asLong();
//            challengeService.savePDFToProject(body, projectId);

            return ok(registerConfirmation.render(new Long(technologyId), "Technology"));
        } catch (Exception e) {
            Logger.debug("TechnologyController technology registration exception: " + e.toString());
            return ok(registrationError.render("Technology"));
        }
    }

    public Result downloadTechnologyFile(Long serviceId) {
        checkLoginStatus();
        try {
            JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.DOWNLOAD_TECHNOLOGY_FILE + serviceId));

            if (response.has("error")) {
                Logger.debug("FileService.getFileById() did not get file from backend with error.");
                return null;
            }

            if (response.has("fileContent")) {
                String base64Content = response.get("fileContent").asText();
                byte[] fileContent = java.util.Base64.getDecoder().decode(base64Content);

                if (response.has("fileDetails")) {
                    JsonNode fileDetails = response.get("fileDetails");
                    if (fileDetails.has("pdf")) {
                        String fileName = fileDetails.get("id").asText();
                        response().setHeader("Content-Disposition", "attachment; filename=\"" + fileName + ".pdf" + "\"");
                    }
                }
                return ok(fileContent).as("application/octet-stream");
            }

            return notFound("File content not found");
        } catch (Exception e) {
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }

    /************************************************** End of Technology Registration ************************************/


    /************************************************** Technology Edit ***************************************************/

    /**
     * This method intends to prepare to edit a technology.
     *
     * @param technologyId: technology id
     * @return
     */
    public Result technologyEditPage(Long technologyId) {
        try {
            Technology technology = technologyService.getTechnologyById(technologyId);
            if (technology == null) {
                Logger.debug("Technology.technologyEditPage exception: cannot get technology by id");
                Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
                return ok(generalError.render());
            }

            String currentUserId = session("id");
            if (currentUserId == null || !currentUserId.equals(String.valueOf(technology.getTechnologyPublisher().getId()))) {
                Logger.debug("Technology.technologyEditPage: User does not have permission to edit this technology.");
                return forbidden("You do not have permission to edit this technology.");
            }

            return ok(technologyEdit.render(technology));
        } catch (Exception e) {
            Logger.debug("TechnologyController.technologyEditPage() exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }

    /**
     * This method intends to submit the edit in the challenge edit page.
     *
     * @param technologyId technology id
     * @return
     */
    public Result technologyEditPOST(Long technologyId) {
        checkLoginStatus();

        try {
            Form<Technology> technologyForm = technologyFormTemplate.bindFromRequest();
            Http.MultipartFormData body = request().body().asMultipartFormData();
            ObjectMapper mapper = new ObjectMapper();

            JsonNode jsonData = technologyService.serializeFormToJson(technologyForm);
            JsonNode response = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.TECHNOLOGY_EDIT_POST + technologyId), jsonData);
            if (response == null || response.has("error")) {
                Logger.debug("Cannot update the technology");
                return redirect(routes.TechnologyController.technologyEditPage(technologyId));
            }

            String record = technologyForm.field("record").value();

            String pdfRecord = technologyForm.field("pdfRecord").value();
//            if (pdfRecord.equals("delete")) {
//                JsonNode imgResponse = RESTfulCalls.deleteAPI(RESTfulCalls.getBackendAPIUrl(config,
//                        Constants.DELETE_PROJECT_PDF + projectId));
//            }
//            challengeService.savePDFToProject(body, projectId);
//
//            challengeService.addTeamMembersToProject(projectForm, body, projectId);
//            challengeService.deleteTeamMembersToProject(projectForm);
            return ok(editConfirmation.render(technologyId, Long.parseLong("0"), "Technology"));//TODO: 0 is entryId
        } catch (Exception e) {
            Logger.debug("TechnologyController technology edit POST exception: " + e.toString());
            return ok(editError.render("Technology"));
        }

    }

    /************************************************** End of Technology Edit ********************************************/

    /************************************************** Technology List ***************************************************/

    /**
     * This method intends to prepare data for all technologies.
     *
     * @param pageNum
     * @param sortCriteria: sortCriteria on some fields. Could be empty if not specified at the first time.
     * @return: data for technologyList.scala.html
     */
    public Result technologyList(Integer pageNum, String sortCriteria) {
        checkLoginStatus();

        // If session contains the current projectId, set it.

        // Set the offset and pageLimit.
        int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
        try {
            JsonNode technologyListJsonNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.TECHNOLOGY_LIST + session("id") + "?pageNum=" +
                            pageNum + "&pageLimit=" + pageLimit + "&sortCriteria=" + sortCriteria));
            return technologyService.renderTechnologyListPage(technologyListJsonNode,
                    pageLimit, null, "all", session("username"),
                    Long.parseLong(session("id")));
        } catch (Exception e) {
            Logger.debug("ChallengeController.challengeList() exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }

    /************************************************** End of Technology List ********************************************/

    /************************************************** Technology Detail *************************************************/

    /**
     * Ths method intends to return details of a technology. If a technology is not found, return to the all technology page (page 1?).
     *
     * @param technologyId: technology id
     * @return: Technology, a list of technologies to technologyDetail.scala.html
     */
    public Result technologyDetail(Long technologyId) {
        try {
            Technology technology = technologyService.getTechnologyById(technologyId);
            if (technology == null) {
                Logger.debug("TechnologyController.technologyDetail() get null from backend");
                Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
                return ok(generalError.render());
            }

            accessTimesService.AddOneTime("technology", technologyId);
            String tableName = "technology";
            String technologyFileType = "technology";
            String tableRecorderId = technologyId.toString();
            String backendPort = Constants.CMU_BACKEND_PORT;
            String currentUserId = session("id");
            return ok(technologyDetail.render(
                    technology,
                    currentUserId,
                    backendPort,
                    tableName,
                    technologyFileType,
                    tableRecorderId));
        } catch (Exception e) {
            Logger.debug("TechnologyController.technologyDetail() exception: " + e.toString());
            e.printStackTrace();
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }

    /************************************************** End of Technology Detail *****************************************/

    /**
     * This method intends to render an empty search.scala.html page
     *
     * @return
     */
    public Result searchPage() {
        checkLoginStatus();

        return ok(technologySearch.render());
    }

    /**
     * This method intends to prepare data for rending technology research result page
     *
     * @param pageNum
     * @return: data prepared for technologyList.scala.html (same as show all technology list page)
     */
    public Result searchPOST(Integer pageNum, String sortCriteria) {
        checkLoginStatus();

        try {
            Form<Technology> tmpForm = technologyFormTemplate.bindFromRequest();
            Map<String, String> tmpMap = tmpForm.data();

            JsonNode searchJson = Json.toJson(tmpMap);
            String searchString = "";

            // if not coming from the search input page, then fetch searchJson from the form from key "searchString"
            if (tmpMap.get("searchString") != null) {
                searchString = tmpMap.get("searchString");
                searchJson = Json.parse(searchString);
            } else {
                searchString = Json.stringify(searchJson);
            }

            List<Technology> technologies = new ArrayList<Technology>();
            JsonNode technologiesNode = null;

            technologiesNode = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.GET_TECHNOLOGIES_BY_CONDITION), searchJson);
            if (technologiesNode.isNull() || technologiesNode.has("error") || !technologiesNode.isArray()) {

                return ok(technologyList.render(technologies, (int) pageNum, sortCriteria,
                        0, technologiesNode.size(), 0, "search", 20, searchString,
                        Long.parseLong(session("id")), 0, 0));
            }
            int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
            int startIndex = ((int) pageNum - 1) * pageLimit;
            int endIndex = ((int) pageNum - 1) * pageLimit + pageLimit - 1;
            // Handle the last page, where there might be less than 50 item
            if (pageNum == (technologiesNode.size() - 1) / pageLimit + 1) {
                // minus 1 for endIndex for preventing ArrayOutOfBoundException
                endIndex = technologiesNode.size() - 1;
            }
            int count = endIndex - startIndex + 1;

            technologies = Technology.deserializeJsonToTechnologyList(technologiesNode, startIndex, endIndex);
           int beginIndexPagination = beginIndexForPagination(pageLimit, technologiesNode.size(), (int) pageNum);
            int endIndexPagination = endIndexForPagination(pageLimit, technologiesNode.size(), (int) pageNum);

            return ok(technologyList.render(technologies,
                    (int) pageNum,
                    sortCriteria,
                    startIndex,
                    technologiesNode.size(),
                    count,
                    "search",
                    pageLimit,
                    searchString,
                    Long.parseLong(session("id")),
                    beginIndexPagination,
                    endIndexPagination));
        } catch (Exception e) {
            Logger.debug("TechnologyController.searchPOST() exception: " + e.toString());
            return redirect(routes.Application.home());
        }
    }


/*************************************** Private Methods **************************************************************/

    /**
     * This method intends to inactivate the challenge by calling the backend
     *
     * @param technologyId
     * @return redirect to the challenge list page
     */
    public Result deleteTechnology(long technologyId) {
        checkLoginStatus();


        try {
            JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.DELETE_TECHNOLOGY_BY_ID + technologyId));
            //Todo We have to decide what to do if for some reason the technology could not get deactivated???
            return redirect(routes.TechnologyController.technologyList(1, ""));
        } catch (Exception e) {
            Logger.debug("technologyController challenge delete exception: " + e.toString());
            return redirect(routes.TechnologyController.technologyList(1, ""));
        }
    }

    public Result isTechnologyNameExisted() {
        JsonNode json = request().body().asJson();
        String title = json.path("title").asText();

        ObjectNode jsonData = Json.newObject();
        JsonNode response = null;

        try {
            jsonData.put("title", title);
            response = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.CHECK_TECHNOLOGY_NAME), jsonData);
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



    /*********************************** END Basic refactoring ********************************************************/

    /**
     *
     * @return
     */
    public Result getTechnologyLists() {
        checkLoginStatus();
        ArrayNode technologyList = Json.newArray();
        JsonNode technologiesNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                Constants.GET_ALL_ACTIVE_TECHNOLOGY));
        // if no value is returned or error or is not json array

        ObjectMapper mapper = new ObjectMapper();
        // parse the json string into object
        for (int i = 0; i < technologiesNode.size(); i++) {
            JsonNode json = technologiesNode.path(i);
            ObjectNode jsonData = mapper.createObjectNode();
            jsonData.put("id", json.findPath("id").asLong());
            jsonData.put("text", json.findPath("title").asText());
            technologyList.add(jsonData);
        }

        return ok(technologyList);
    }

    /**
     *
     * @param id
     * @return
     */
    public Result isTechnologyExist(String id) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        if (pattern.matcher(id).matches()) {
            JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.GET_TECHNOLOGY_BY_ID + Long.parseLong(id)));
            if (response == null || response.has("error")) {
                return badRequest("cannot find technology");
            } else return ok(response.findPath("id"));
        } else return badRequest("cannot find technology");
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
            return technologyList(1, "");
        }


        return technologyList(1, "");
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
            return technologyList(1, "");
        }

        return technologyList(1, "");
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
}