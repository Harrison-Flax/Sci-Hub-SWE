package controllers;

import actions.OperationLoggingAction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import models.Project;
import models.User;
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
import services.AccessTimesService;
import services.ProjectService;
import services.UserService;
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

import static controllers.Application.checkLoginStatus;
import static utils.Common.beginIndexForPagination;
import static utils.Common.endIndexForPagination;

public class ProjectController extends Controller {

    @Inject
    Config config;

    private final ProjectService projectService;
    private final UserService userService;
    private final AccessTimesService accessTimesService;

    private Form<Project> projectFormTemplate;
    private FormFactory myFactory;


    /******************************* Constructor **********************************************************************/
    @Inject
    public ProjectController(FormFactory factory,
                             ProjectService projectService,
                             UserService userService, AccessTimesService accessTimesService) {
        projectFormTemplate = factory.form(Project.class);
        myFactory = factory;

        this.projectService = projectService;
        this.userService = userService;
        this.accessTimesService = accessTimesService;
    }


    /************************************************** Project Registration ******************************************/

    /**
     * This method intends to render the project registration page.
     *
     * @return
     */
    @With(OperationLoggingAction.class)
    public Result projectRegisterPage() {
        checkLoginStatus();
        return ok(projectRegister.render());
    }

    /**
     * This method intends to gather project registration information and create a project in database.
     *
     * @return
     */
    public Result projectRegisterPOST() {
        checkLoginStatus();

        try {
            Form<Project> projectForm = projectFormTemplate.bindFromRequest();
            Http.MultipartFormData body = request().body().asMultipartFormData();

            ObjectNode jsonData = projectService.serializeFormToJson(projectForm);

            JsonNode response = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.PROJECT_REGISTER_POST), jsonData);

            if (response == null || response.has("error")) {
                Logger.debug("ProjectController.projectRegisterPOST: Cannot create the project in backend");
                return ok(registrationError.render("Project"));
            }

            long projectId = response.asLong();
//            projectService.savePictureToProject(body, projectId);
//            projectService.savePDFToProject(body, projectId);
//            projectService.addTeamMembersToProject(projectForm, body, projectId);

            return ok(registerConfirmation.render(new Long(projectId), "Project"));
        } catch (Exception e) {
            Logger.debug("ProjectController project registration exception: " + e.toString());
            return ok(registrationError.render("Project"));
        }
    }

    /************************************************** End of Project Registration ************************************/


    /************************************************** Project Edit ***************************************************/

    /**
     * This method intends to prepare to edit a project.
     *
     * @param projectId: project id
     * @return
     */
    @With(OperationLoggingAction.class)
    public Result projectEditPage(Long projectId) {
        try {
            Project project = projectService.getProjectById(projectId);
            if (project == null) {
                Logger.debug("ProjectController.projectEditPage exception: cannot get project by id");
                Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
                return ok(generalError.render());
            }

            JsonNode teamMembersNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.GET_TEAM_MEMBERS_BY_PROJECT_ID + projectId));
            project.setTeamMembers(Common.deserializeJsonToList(teamMembersNode, User.class));

            return ok(projectEdit.render(project));
        } catch (Exception e) {
            Logger.debug("ProjectController.projectEditPage() exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }

    /**
     * This method intends to submit the edit in the project edit page.
     *
     * @param projectId project id
     * @return
     */
    public Result projectEditPOST(Long projectId) {
        checkLoginStatus();

        try {
            Form<Project> projectForm = projectFormTemplate.bindFromRequest();
            Http.MultipartFormData body = request().body().asMultipartFormData();
            ObjectMapper mapper = new ObjectMapper();

            ObjectNode jsonData = projectService.serializeFormToJson(projectForm);
            JsonNode response = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.PROJECT_EDIT_POST + projectId), jsonData);
            if (response == null || response.has("error")) {
                Logger.debug("Cannot update the project");
                return redirect(routes.ProjectController.projectEditPage(projectId));
            }

            String record = projectForm.field("record").value();
            if (record.equals("delete")) {
                JsonNode imgResponse = RESTfulCalls.deleteAPI(RESTfulCalls.getBackendAPIUrl(config,
                        Constants.DELETE_PROJECT_IMAGE + projectId));
            } else if (record.equals("update")) {
                projectService.savePictureToProject(body, projectId);
            }

            String pdfRecord = projectForm.field("pdfRecord").value();
            if (pdfRecord.equals("delete")) {
                JsonNode imgResponse = RESTfulCalls.deleteAPI(RESTfulCalls.getBackendAPIUrl(config,
                        Constants.DELETE_PROJECT_PDF + projectId));
            }
            projectService.savePDFToProject(body, projectId);

            projectService.addTeamMembersToProject(projectForm, body, projectId);
            projectService.deleteTeamMembersToProject(projectForm);
            return ok(editConfirmation.render(projectId, Long.parseLong("0"), "Project"));//TODO: 0 is entryId
        } catch (Exception e) {
            Logger.debug("ProjectController project edit POST exception: " + e.toString());
            return ok(editError.render("Project"));
        }

    }

    /************************************************** End of Project Edit ********************************************/

    /************************************************** Project List ***************************************************/

    /**
     * This method intends to prepare data for all projects.
     *
     * @param pageNum
     * @param sortCriteria: sortCriteria on some fields. Could be empty if not specified at the first time.
     * @return: data for projectList.scala.html
     */
    @With(OperationLoggingAction.class)
    public Result projectList(Integer pageNum, String sortCriteria) {
        checkLoginStatus();

        // If session contains the current projectId, set it.
        Project currentProjectZone = projectService.getCurrentProjectZone();

        // Set the offset and pageLimit.
        int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
        try {
            JsonNode projectListJsonNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.PROJECT_LIST + session("id") + "?pageNum=" +
                            pageNum + "&pageLimit=" + pageLimit + "&sortCriteria=" + sortCriteria));
            System.out.println("projectListJsonNode :: " + projectListJsonNode);
            return projectService.renderProjectListPage(projectListJsonNode,
                    currentProjectZone, pageLimit, null, "all", session("username"),
                    Long.parseLong(session("id")));
        } catch (Exception e) {
            Logger.debug("ProjectController.projectList() exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }

    /************************************************** End of Project List ********************************************/

    /************************************************** Fellowship List ***************************************************/

    /**
     * This method intends to prepare data for all projects.
     *
     * @param pageNum
     * @param sortCriteria: sortCriteria on some fields. Could be empty if not specified at the first time.
     * @return: data for projectList.scala.html
     */
    public Result fellowshipList(Integer pageNum, String sortCriteria) {
        checkLoginStatus();

        // Set the offset and pageLimit.
        int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
        try {
            JsonNode projectListJsonNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.FELLOWSHIP_LIST + session("id") + "?pageNum=" +
                            pageNum + "&pageLimit=" + pageLimit + "&sortCriteria=" + sortCriteria));
            return projectService.renderFellowshipListPage(projectListJsonNode,
                    pageLimit, null, "all", session("username"),
                    Long.parseLong(session("id")));
        } catch (Exception e) {
            Logger.debug("ProjectController.projectList() exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }

    /************************************************** End of Fellowship List ********************************************/

    /************************************************** Project Detail *************************************************/

    /**
     * Ths method intends to return details of a project. If a project is not found, return to the all project page (page 1?).
     *
     * @param projectId: project id
     * @return: Project, a list of team members to projectDetail.scala.html
     */
    @With(OperationLoggingAction.class)
    public Result projectDetail(Long projectId) {

        try {

            Project parentProject = null;
            Project project = projectService.getProjectById(projectId);
            Long parentProjectId = project.getParentProjectId();
            if (parentProjectId != null) {
                if (parentProjectId != 0) {
                    parentProject = projectService.getProjectById(parentProjectId);
                }
            }
            System.out.println("detail project: " + project.getTitle());
            if (project == null) {
                Logger.debug("ProjectController.projectDetail() get null from backend");
                Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
                return ok(generalError.render());
            }

//            JsonNode teamMembersNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.GET_TEAM_MEMBERS_BY_PROJECT_ID + projectId));
//            project.setTeamMembers(Common.deserializeJsonToList(teamMembersNode, User.class));

            accessTimesService.AddOneTime("project", projectId);
            System.out.println("projectDetail:::" + project.toString());
            return ok(projectDetail.render(project));
        } catch (Exception e) {
            Logger.debug("ProjectController.projectDetail() exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }

    /************************************************** End of Project Detail *****************************************/

    /**
     * This method intends to render an empty search.scala.html page
     *
     * @return
     */
    public Result searchPage() {
        checkLoginStatus();

        return ok(projectSearch.render());
    }

    /**
     * This method intends to prepare data for rending user research result page
     *
     * @param pageNum
     * @return: data prepared for userList.scala.html (same as show all user list page)
     */
    public Result searchPOST(Integer pageNum, String sortCriteria) {
        checkLoginStatus();
        Project currentProjectZone = projectService.getCurrentProjectZone();

        try {
            Form<Project> tmpForm = projectFormTemplate.bindFromRequest();
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

            List<Project> projects = new ArrayList<Project>();
            JsonNode projectsNode = null;

            projectsNode = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.GET_PROJECTS_BY_CONDITION), searchJson);
            if (projectsNode.isNull() || projectsNode.has("error") || !projectsNode.isArray()) {

                return ok(projectList.render(projects, currentProjectZone, (int) pageNum, sortCriteria,
                        0, projectsNode.size(), 0, "search", 20, searchString,
                        session("username"), Long.parseLong(session("id")), 0, 0));
            }
            int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
            int startIndex = ((int) pageNum - 1) * pageLimit;
            int endIndex = ((int) pageNum - 1) * pageLimit + pageLimit - 1;
            // Handle the last page, where there might be less than 50 item
            if (pageNum == (projectsNode.size() - 1) / pageLimit + 1) {
                // minus 1 for endIndex for preventing ArrayOutOfBoundException
                endIndex = projectsNode.size() - 1;
            }
            int count = endIndex - startIndex + 1;

            projects = Project.deserializeJsonToProjectList(projectsNode, startIndex, endIndex, sortCriteria);
            int beginIndexPagination = beginIndexForPagination(pageLimit, projectsNode.size(), (int) pageNum);
            int endIndexPagination = endIndexForPagination(pageLimit, projectsNode.size(), (int) pageNum);

            return ok(projectList.render(projects,
                    currentProjectZone,
                    (int) pageNum,
                    sortCriteria,
                    startIndex,
                    projectsNode.size(),
                    count,
                    "search",
                    pageLimit,
                    searchString,
                    session("username"),
                    Long.parseLong(session("id")),
                    beginIndexPagination,
                    endIndexPagination));
        } catch (Exception e) {
            Logger.debug("ProjectController.searchPOST() exception: " + e.toString());
            return redirect(routes.Application.home());
        }
    }


    /*************************************** Private Methods **********************************************************/

    /**
     * This method intends to inactivate the project by calling the backend
     *
     * @param projectId
     * @return redirect to the project list page
     */
    @With(OperationLoggingAction.class)
    public Result deleteProject(long projectId) {
        checkLoginStatus();

        try {
            JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.DELETE_PROJECT_BY_ID + projectId));
            //Todo We have to decide what to do if for some reason the project could not get deactivated???
            return redirect(routes.ProjectController.projectList(1, ""));
        } catch (Exception e) {
            Logger.debug("ProjectController project delete exception: " + e.toString());
            return redirect(routes.ProjectController.projectList(1, ""));
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
     * This method receives a project Id and the image number in the description of the project and uploads
     * this image to aws by calling backen and return the received URL for the uploaded image
     *
     * @param projectId   project Id
     * @param imageNumber image number in the description of the project
     * @return the received URL for the uploaded image if the upload is successful
     */
    public Result uploadDescriptionImage(long projectId, int imageNumber) {
        play.mvc.Http.MultipartFormData<File> body = request().body().asMultipartFormData();
        play.mvc.Http.MultipartFormData.FilePart<File> picture = body.getFile("file");
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
     * This method receives a project Id and the image number in the description of the project along with the
     * current image index in the description and renames the file on S3 bucket to have the new imageNumber as
     * the index by calling the backend and return the received URL for the uploaded image to be replaced for
     * the src in the img tag in description
     *
     * @param projectId          project Id
     * @param imageNumber        image number in the description of the project
     * @param currentImageNumber current image index number in the description of the project
     * @return the received URL for the uploaded image if the upload is successful
     */
    public Result renameDescriptionImage(long projectId, int imageNumber, int currentImageNumber) {
        try {
            Logger.debug("rename project image description ");
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
    @With(OperationLoggingAction.class)
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
                return badRequest("cannot find project");
            } else return ok(response.findPath("id"));
        } else return badRequest("cannot find project");
    }


    /***
     * This method receives a notebook id together with a list of projects from the creator of them and associate them to the given notebook
     * @param notebookId given notebook id
     * json parameters in the request body:
     *           projects: array of project Ids
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
    public Result getMyEnrolledProjects(int pageNum, String sortCriteria) {
        try {

            Long userId = Long.parseLong(session("id"));

            int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
            JsonNode projectListJsonNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.GET_MY_ENROLLED_PROJECTS +
                    "?pageNum=" + pageNum + "&pageLimit=" + pageLimit + "&sortCriteria=" + sortCriteria + "&userId=" + userId));
            Project currentProjectZone = projectService.getCurrentProjectZone();
            return projectService.renderProjectListPage(projectListJsonNode, currentProjectZone, pageLimit, null,
                    "my enroll", session("username"), Long.parseLong(session("id")));
        } catch (Exception e) {
            Logger.debug("ProjectController.getMyEnrolledProjects: " + e.toString());
            return redirect(routes.Application.home());
        }
    }

    /**
     * This method aims to set GeoNEX as project zone. (e.g., when clicking on GeoNEX at the top menu)
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
            return projectList(1, "");
        }


        return projectList(1, "");
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
            return projectList(1, "");
        }

        return projectList(1, "");
    }

    /**
     * This method intends to prepare data to render the page of listing my followed projects with pagination
     *
     * @param page:         current page number
     * @param sortCriteria: sort criteria
     * @return: data for projectList.scala.html
     */
    public Result myFollowedProjects(Integer page, String sortCriteria) {
        Project currentProjectZone = projectService.getCurrentProjectZone();
        try {
            int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
            int offset = pageLimit * (page - 1);
            JsonNode projectsJsonNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.MY_FOLLOWED_PROJECTS + "?offset=" + offset + "&pageLimit=" +
                            pageLimit + "&sortCriteria=" + sortCriteria + "&uid=" + session("id")));
            return projectService.renderProjectListPage(projectsJsonNode, currentProjectZone, pageLimit,
                    null, "my follow", session("username"), Long.parseLong(session("id")));

        } catch (Exception e) {
            e.printStackTrace();
            return redirect(routes.Application.home());
        }
    }

    /**
     * This method intends to check whether the input parent project id is valid
     *
     * @param parentProjectId: parent project id
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
     * @param id project id
     * @return: addProjectFollowersPage
     */
    public Result addProjectFollowersPage(Long id) {
        try {
            Project project = projectService.getProjectById(id);
            JsonNode userNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(
                    config, Constants.GET_USER_PROFILE_BY_ID + project.getCreator()));
            User creator = User.deserialize(userNode);
            JsonNode followersNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(
                    config, Constants.GET_FOLLOWERS_FOR_PROJECT + id));
            List<User> followers = new ArrayList<>();
            for (JsonNode follower : followersNode) {
                followers.add(User.deserialize(follower));
            }
            return ok(addProjectFollowers.render(project, followers, creator));
        } catch (Exception e) {
            e.printStackTrace();
            return redirect(routes.ProjectController.projectDetail(id));
        }
    }

    /**
     * This method intends to add one follower to a private project
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
     * This method intends to delete one follower of a private project
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