package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import controllers.routes;
import models.Fellowship;
import models.Project;
import models.User;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import utils.Constants;
import utils.RESTfulCalls;
import views.html.projectList;
import views.html.fellowshipList;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
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
 * This class intends to provide support for ProjectController.
 */
public class ProjectService {
    @Inject
    Config config;

    private final UserService userService;
    private Form<Project> projectForm;

    @Inject
    public ProjectService(UserService userService) {
        this.userService = userService;
    }

    /**
     * This method returns the current ProjectZone. Default project zone is OpenNEX (0).
     * OpenNEX project id = 0; private zone project id < 0
     *
     * @return Project current ProjectZone
     */
    public Project getCurrentProjectZone() {
        Project currentProjectZone = null;
        if (session("projectId") != null && Long.parseLong(session("projectId")) > 0) {
            currentProjectZone = getProjectById(Long.parseLong(session("projectId")));
        }
        return currentProjectZone;
    }

    /**
     * This method intends to get Project by id by calling backend APIs.
     *
     * @param projectId
     * @return Project
     */
    public Project getProjectById(Long projectId) {
        Project project = null;
        try {
            JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.GET_PROJECT_BY_ID + projectId));
            if (response.has("error")) {
                Logger.debug("ProjectService.getProjectById() did not get project from backend with error.");
                return null;
            }

            project = Project.deserialize(response);
            if (project.getSponsorContact() == null) {
                Logger.debug("ProjectService.getProjectById() creator is null");
                throw new Exception("ProjectService.getProjectById() sponsor contact is null");
            }
        } catch (Exception e) {
            Logger.debug("ProjectService.getProjectById() exception: " + e.toString());
            return null;
        }
        return project;
    }

    /**
     * This method intends to get all projects by a creator logged into the system.
     *
     * @return
     */
    public ArrayList<Project> getProjectsByCreator() {
        try {
            JsonNode projects = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.GET_PROJECTS_BY_CREATOR
                    + session("id")));
            if (projects == null || projects.has("error")) return null;
            return Project.deserializeJsonArrayToProjectList(projects);
        } catch (Exception e) {
            Logger.debug("ProjectService.getProjectsByCreator exception: " + e.toString());
            return null;
        }
    }


    /**
     * This method intends to save a picture to project.
     *
     * @param body
     * @param projectId: project id
     * @throws Exception
     */
    public void savePictureToProject(Http.MultipartFormData body, Long projectId) throws Exception {
        try {
            if (body.getFile("picture") != null) {
                Http.MultipartFormData.FilePart image = body.getFile("picture");
                if (image != null && !image.getFilename().equals("")) {
                    File file = (File) image.getFile();
                    JsonNode imgResponse = RESTfulCalls.postAPIWithFile(RESTfulCalls.getBackendAPIUrl(config,
                            Constants.SET_PROJECT_IMAGE + projectId), file);
                }
            }
        } catch (Exception e) {
            Logger.debug("ProjectService.savePictureToProject exception: " + e.toString());
            throw e;
        }
    }

    /**
     * This method intends to save a pdf to project.
     *
     * @param body
     * @param projectId: project id
     * @throws Exception
     */
    public void savePDFToProject(Http.MultipartFormData body, Long projectId) throws Exception {
        try {
            if (body.getFile("pdf") != null) {
                Http.MultipartFormData.FilePart pdf = body.getFile("pdf");
                if (pdf != null && !pdf.getFilename().equals("")) {
                    File file = (File) pdf.getFile();
                    JsonNode pdfResponse = RESTfulCalls.postAPIWithFile(RESTfulCalls.getBackendAPIUrl(config,
                            Constants.SET_PROJECT_PDF + projectId), file);
                }
            }
        } catch (Exception e) {
            Logger.debug("ProjectService.savePDFToProject exception: " + e.toString());
            throw e;
        }
    }

    /**
     * This method intends to add a list of team members to a project, from project registration form.
     *
     * @param projectForm: project registration form
     * @param body
     * @param projectId:   project id
     */
    public void addTeamMembersToProject(Form<Project> projectForm, Http.MultipartFormData body, Long projectId) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            int count = Integer.parseInt(projectForm.field("count").value()); //the number of team members in the project
            for (int i = 0; i < count; i++) {
                if (projectForm.field("member" + i) != null) {
                    ObjectNode memberData = mapper.createObjectNode();
                    memberData.put("name", projectForm.field("member" + i).value());
                    memberData.put("email", projectForm.field("email" + i).value());
                    JsonNode memberRes = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                            Constants.ADD_TEAM_MEMBER + projectId), memberData);
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
                    userService.createUserbyAddingTeamMember(projectForm.field("member" + i).value(),
                            projectForm.field("email" + i).value());
                }
            }
        } catch (Exception e) {
            Logger.debug("ProjectService.addTeamMembersToProject exception: " + e.toString());
            throw e;
        }
    }

    /**
     * This method intends to add a list of team members to a project, from project registration form.
     *
     * @param projectForm: project registration form
     */
    public void deleteTeamMembersToProject(Form<Project> projectForm) {
        try {
            int deleteCount = 0;
            if (projectForm.field("delc").value() != null && projectForm.field("delc").value().trim() != "")
                deleteCount = Integer.parseInt(projectForm.field("delc").value());
            //delete chosen team members
            for (int i = 0; i < deleteCount; i++) {
                Long deleteTeamMemberId = Long.parseLong(projectForm.field("delete" + i).value());
                JsonNode deleteResponse = RESTfulCalls.deleteAPI(RESTfulCalls.getBackendAPIUrl(config,
                        Constants.DELETE_TEAM_MEMBER + deleteTeamMemberId));
            }
        } catch (Exception e) {
            Logger.debug("ProjectService.deleteTeamMembersToProject exception: " + e.toString());
            throw e;
        }
    }





    /************************************************ Page Render Preparation *****************************************/
    /**
     * This private method renders the project list page.
     * Note that for performance consideration, the backend only passes back the projects for the needed page stored in
     * the projectListJsonNode, together with the offset/count/total/sortCriteria information.
     *
     * @param projectListJsonNode
     * @param currentProjectZone
     * @param pageLimit
     * @param searchBody
     * @param listType            : "all"; "search" (draw this page from list function or from search function)
     * @param username
     * @param userId
     * @return render project list page; If exception happened then render the homepage
     */
    public Result renderProjectListPage(JsonNode projectListJsonNode,
                                        Project currentProjectZone,
                                        int pageLimit,
                                        String searchBody,
                                        String listType,
                                        String username,
                                        Long userId) {
        try {
            // if no value is returned or error
            if (projectListJsonNode == null || projectListJsonNode.has("error")) {
                Logger.debug("Project list is empty or error!");
                return redirect(routes.Application.home());
            }

            JsonNode projectsJsonArray = projectListJsonNode.get("items");
            if (!projectsJsonArray.isArray()) {
                Logger.debug("Project list is not array!");
                return redirect(routes.Application.home());
            }

            List<Project> projects = new ArrayList<>();
            for (int i = 0; i < projectsJsonArray.size(); i++) {
                JsonNode json = projectsJsonArray.path(i);
                Project project = Project.deserialize(json);
                projects.add(project);
            }

            // offset: starting index of the pageNum; count: the number of items in the pageNum;
            // total: the total number of items in all pages; sortCriteria: the sorting criteria (field)
            // pageNum: the current page number
            String sortCriteria = projectListJsonNode.get("sort").asText();

            int total = projectListJsonNode.get("total").asInt();
            int count = projectListJsonNode.get("count").asInt();
            int offset = projectListJsonNode.get("offset").asInt();
            int pageNum = offset / pageLimit + 1;

            int beginIndexPagination = beginIndexForPagination(pageLimit, total, pageNum);
            int endIndexPagination = endIndexForPagination(pageLimit, total, pageNum);

            return ok(projectList.render(projects, currentProjectZone, pageNum, sortCriteria, offset, total, count,
                    listType, pageLimit, searchBody, username, userId, beginIndexPagination, endIndexPagination));
        } catch (Exception e) {
            Logger.debug("Exception in renderProjectListPage:" + e.toString());
            e.printStackTrace();
            return redirect(routes.Application.home());
        }
    }

    /************************************************ Fellowship Render Preparation *****************************************/
    /**
     * This private method renders the fellowship list page.
     * Note that for performance consideration, the backend only passes back the projects for the needed page stored in
     * the fellowshipListJsonNode, together with the offset/count/total/sortCriteria information.
     *
     * @param fellowshipListJsonNode
     * @param pageLimit
     * @param searchBody
     * @param listType            : "all"; "search" (draw this page from list function or from search function)
     * @param username
     * @param userId
     * @return render fellowship list page; If exception happened then render the homepage
     */
    public Result renderFellowshipListPage(JsonNode fellowshipListJsonNode,
                                        int pageLimit,
                                        String searchBody,
                                        String listType,
                                        String username,
                                        Long userId) {
        try {
            // if no value is returned or error
            if (fellowshipListJsonNode == null || fellowshipListJsonNode.has("error")) {
                Logger.debug("Fellowship list is empty or error!");
                return redirect(routes.Application.home());
            }

            JsonNode fellowshipsJsonArray = fellowshipListJsonNode.get("items");
            if (!fellowshipsJsonArray.isArray()) {
                Logger.debug("Fellowship list is not array!");
                return redirect(routes.Application.home());
            }

            List<Fellowship> fellowships = new ArrayList<>();
            for (int i = 0; i < fellowshipsJsonArray.size(); i++) {
                JsonNode json = fellowshipsJsonArray.path(i);
                Fellowship fellowship = Fellowship.deserialize(json);
                fellowships.add(fellowship);
            }

            // offset: starting index of the pageNum; count: the number of items in the pageNum;
            // total: the total number of items in all pages; sortCriteria: the sorting criteria (field)
            // pageNum: the current page number
            String sortCriteria = fellowshipListJsonNode.get("sort").asText();

            int total = fellowshipListJsonNode.get("total").asInt();
            int count = fellowshipListJsonNode.get("count").asInt();
            int offset = fellowshipListJsonNode.get("offset").asInt();
            int pageNum = offset / pageLimit + 1;

            int beginIndexPagination = beginIndexForPagination(pageLimit, total, pageNum);
            int endIndexPagination = endIndexForPagination(pageLimit, total, pageNum);

            return ok(fellowshipList.render(fellowships, pageNum, sortCriteria, offset, total, count,
                    listType, pageLimit, searchBody, username, userId, beginIndexPagination, endIndexPagination));
        } catch (Exception e) {
            Logger.debug("Exception in renderProjectListPage:" + e.toString());
            e.printStackTrace();
            return redirect(routes.Application.home());
        }
    }


    /**************************************************** (De)Serialization *******************************************/
    /**
     * This method intends to prepare a json object from Project form.
     *
     * @param projectForm: project registration form
     * @return
     * @throws Exception
     */
    public ObjectNode serializeFormToJson(Form<Project> projectForm) throws Exception {
        ObjectNode jsonData = null;

        try {
            Map<String, String> tmpMap = projectForm.data();
            jsonData = (ObjectNode) (Json.toJson(tmpMap));

//            String video = projectForm.field("video").value();
//            int index = video.indexOf("youtube");
//            if (index >= 0) {
//                Matcher slashMatcher = Pattern.compile("/").matcher(video);
//                int mIdx = 0;
//                while (slashMatcher.find()) {
//                    mIdx++;
//                    if (mIdx == 3) {
//                        break;
//                    }
//                }
//                int insert = slashMatcher.start();
//                video = "https://www.youtube.com/embed" + video.substring(insert);
//            }
//            jsonData.put("video", video);

//            if (projectForm.field("description").value() != null) {
//                jsonData.put("description", projectForm.field("description").value().replaceAll(
//                        "\n", "").replaceAll("\r", ""));
//            }
//
//            if (projectForm.field("markAsPrivate").value() != null && projectForm.field(
//                    "markAsPrivate").value().equals("on")) {
//                jsonData.put("authentication", "private");
//            } else {
//                jsonData.put("authentication", "public");
//
//            }

            User user = new User(Long.parseLong(session("id")));
            jsonData.put("sponsorContact", Json.toJson(user));
            System.out.println("jsonData- - " + jsonData.toString());
        } catch (Exception e) {
            Logger.debug("ProjectService.serializeFormToJson exception: " + e.toString());
            throw e;
        }

        return jsonData;
    }

}
