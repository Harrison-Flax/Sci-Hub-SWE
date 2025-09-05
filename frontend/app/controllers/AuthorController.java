package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import models.*;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import services.AuthorService;
import services.UserService;
import services.ProjectService;
import utils.Constants;
import utils.RESTfulCalls;
import views.html.*;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static controllers.Application.checkLoginStatus;
import static utils.Constants.CALLER_IS_NOT_MY_SPACE_PAGE;

public class AuthorController extends Controller {

    @Inject
    Config config;

    private final ProjectService projectService;
    private final AuthorService authorService;
    private final UserService userService;

    private Form <Author> userForm;
    private Form<User> userFormAdmin;
    private FormFactory myFactory;


    @Inject
    public AuthorController(FormFactory factory,
                            ProjectService projectService,
                            AuthorService authorService,
                            UserService userService) {
        userForm = factory.form(Author.class);
        userFormAdmin = factory.form(User.class);
        myFactory = factory;

        this.projectService = projectService;
        this.authorService = authorService;
        this.userService = userService;
    }
    private List<Organization> fetchOrganizationsList() {
        JsonNode organizationsJsonNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.ORGANIZATION_LIST));
        Logger.debug(organizationsJsonNode.asText());
        JsonNode organizationJsonArray = organizationsJsonNode.get("items");
        List<Organization> organizations = new ArrayList<>();
        if (null == organizationJsonArray) return organizations;
        for (JsonNode json : organizationJsonArray) {
            try {
                Organization organization = Organization.deserialize(json);
                organizations.add(organization);
            } catch (Exception e) {

            }
        }
        return organizations;
    }

    /**
     * This method goes to the author regsistration page.
     *
     * @return
     */
    public Result authorRegisterPage() {
        return ok(authorRegister.render(userForm, Constants.PATTERN_RULES));
    }

    /**
     * This method gather author registration in page input and creates a author.
     *
     * @return
     */
    public Result authorRegisterPOST() {
        try {
            Form < Author > userForm = this.userForm.bindFromRequest();
            ObjectNode jsonData = authorService.createJsonFromAuthorForm(userForm);
            jsonData.put("level", "normal");

            JsonNode response = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.AUTHOR_REGISTER_POST), jsonData);
            String newAuthorId = response.get("id").asText();
            if (newAuthorId != null) {
                return ok(registerConfirmation.render(new Long(newAuthorId), "Author"));
            } else {
                Logger.debug("AuthorController user sign on backend error");
                return ok(registrationError.render("Author"));
            }
        } catch (Exception e) {
            Logger.debug("AuthorController user sign on exception: " + e.toString());
            return ok(registrationError.render("Author"));
        }
    }


    /**
     * This method renders the author edit page
     *
     * @return render user edit page
     */
    public Result authorEditPage() {
        checkLoginStatus();


        String userId = session("id");
        try {
            JsonNode userNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.AUTHOR_DETAIL + userId));
            if (userNode == null || userNode.has("error")) {
                Logger.debug("AuthorController.userEditPage user cannot be found from backend: " + userId);
                Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
                return ok(generalError.render());
            }
            Author user = Author.deserialize(userNode);
            return ok(authorEdit.render(userId, userForm, user));
        } catch (Exception e) {
            Logger.debug("AuthorController.userEditPage() exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }
    public Result userEditPageAdmin(Long userId) {
        checkLoginStatus();

        try {
            JsonNode userNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.USER_DETAIL + userId));
            if (userNode == null || userNode.has("error")) {
                Logger.debug("UserController.userEditPage user cannot be found from backend: " + userId);
                Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
                return ok(generalError.render());
            }
            User user = User.deserialize(userNode);
            ResearcherInfo researcherInfo = null;
            StudentInfo studentInfo = null;

            if(user.getUserType() != null){
                if (user.getUserType() == 1) {
                    JsonNode researcherNode = RESTfulCalls.getAPI(
                            RESTfulCalls.getBackendAPIUrl(config, Constants.RESEARCHER_DETAIL + userId));
                    Logger.info("Queried researcherInfo: " + researcherNode);
                    if (researcherNode != null && !researcherNode.has("error")) {
                        researcherInfo = ResearcherInfo.deserialize(researcherNode);
                        user.setResearcherInfo(researcherInfo);
                    }
                }
                if (user.getUserType() == 4) {
                    JsonNode studentNode = RESTfulCalls.getAPI(
                            RESTfulCalls.getBackendAPIUrl(config, Constants.STUDENT_DETAIL + userId));
                    Logger.info("Queried studentInfo: " + studentNode);
                    if (studentNode != null && !studentNode.has("error")) {
                        studentInfo = StudentInfo.deserialize(studentNode);
                        user.setStudentInfo(studentInfo);
                    }
                }
            }

            List<Organization> organizations = fetchOrganizationsList();
            Organization org = new Organization();
            org.setId(-1);
            org.setOrganizationName("Other");
            organizations.add(org);

            return ok(userEditAdmin.render(Long.valueOf(userId), userFormAdmin, user, organizations, researcherInfo, studentInfo));
        } catch (Exception e) {
            Logger.debug("UserController.userEditPage() exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }

    /**
     * This method receives authors's information and updates authors's profile
     */
    public Result authorEditPOST() {
        checkLoginStatus();

        try {
            Form < Author > userForm = this.userForm.bindFromRequest();

            ObjectNode jsonData = authorService.createJsonFromAuthorForm(userForm);
            jsonData.put("id", session("id"));

            //find default project user selected
            Long projectId = null;
            if (!StringUtils.isEmpty(userForm.field("projectId").value())) {
                String project = userForm.field("projectId").value();
                try {
                    projectId = Long.parseLong(project.trim());
                } catch (Exception e) {
                    Logger.debug("Project Id passed is not a number: " + e.toString());
                }
            }
            jsonData.put("projectId", projectId);
            JsonNode response = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.AUTHOR_EDIT_POST), jsonData);
            if (response == null || response.has("error")) {
                Logger.debug("Author edit failed!");
                return ok(editError.render("Author"));
            } else {
                //if the user updated successfully,then we update the session's default project id also (project zone)
                if (projectId != null) {
                    session("projectId", projectId.toString());
                } else {
                    session("projectId", Integer.toString(Constants.OPENNEX_PROJECT_ZONE_ID));
                }
                //update image in session as well
                Author currentAuthor = Author.deserialize(response);
                session("avatar", currentAuthor.getAvatar());
                return redirect(routes.AuthorController.authorDetailPage(Long.parseLong(session("id"))));

            }
        } catch (Exception e) {
            Logger.debug("AuthorController.userEditPOST exception: " + e.toString());
            return ok(editError.render("Author"));
        }
    }

    public Result userEditPOSTAdmin(Long userId) {
        Logger.info("Entering userEditPOST() for userId=" + userId);
        checkLoginStatus();

        try {
            Form<User> userFormAdmin = this.userFormAdmin.bindFromRequest();
            Logger.debug("Form binding complete. Errors: " + userFormAdmin.errorsAsJson());
            Logger.debug("Form Data: " + userFormAdmin.data());

            if (userFormAdmin.hasErrors()) {
                Logger.warn("userEditPOST: Form has errors: " + userForm.errorsAsJson());
                return badRequest(login.render(userFormAdmin, userService.getPublicRecaptchaKey()));
            }

            ObjectNode jsonData = userService.createJsonFromUserForm(userFormAdmin);
            Logger.debug("Created JSON data from form: " + jsonData);
            // use the userId parameter
            jsonData.put("id", userId);
            Logger.debug("Added userId (" + userId + ") to JSON data.");

            // projectId parsing remains the same
            Long projectId = null;
            String projectField = userFormAdmin.field("projectId").value();
            if (!StringUtils.isEmpty(projectField)) {
                try {
                    projectId = Long.parseLong(projectField.trim());
                    Logger.debug("Parsed projectId: " + projectId);
                } catch (Exception e) {
                    Logger.debug("Project Id not a number: " + e);
                }
            }
            jsonData.put("projectId", projectId);
            Logger.debug("Final JSON data with projectId: " + jsonData);

            JsonNode imageUpdateResponse = userService.updateUserImage(userFormAdmin, request().body());
            Logger.debug("Image update response: " +
                    (imageUpdateResponse != null ? imageUpdateResponse.toString() : "null"));

            String backendUrl = RESTfulCalls.getBackendAPIUrl(config, Constants.USER_EDIT_POST);
            Logger.debug("POST to backend URL: " + backendUrl);
            JsonNode response = RESTfulCalls.postAPI(backendUrl, jsonData);
            Logger.debug("Response from POST API: " +
                    (response != null ? response.toString() : "null"));

            if (response == null || response.has("error")) {
                Logger.error("User edit failed! Response: " + response);
                return ok(editError.render("User"));
            }

            // update projectId in session if you still want to track that
            if (projectId != null) {
                session("projectId", projectId.toString());
            } else {
                session("projectId", Integer.toString(Constants.OPENNEX_PROJECT_ZONE_ID));
            }

            User updatedUser = User.deserialize(response);
            session("avatar", updatedUser.getAvatar());

            Logger.info("Redirecting to userDetailPage for userId=" + userId);
            return redirect(routes.AdminController.userDetail(userId));

        } catch (Exception e) {
            Logger.error("UserController.userEditPOST exception: " + e, e);
            return ok(editError.render("User"));
        }
    }

    /**
     * This method intends to prepare data to render the pageNum of listing all users with
     * pagination (authorList.scala.html)
     *
     * @param pageNum:      currrent page number
     * @param sortCriteria: sort column
     * @return: data for authorList.scala.html
     */
    public Result authorList(Integer pageNum, String sortCriteria) {
        checkLoginStatus();

        try {

            // Set the offset and pageLimit.
            int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
            int offset = pageLimit * (pageNum - 1);

            JsonNode usersJsonNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.AUTHOR_LIST + "?offset=" + offset + "&pageLimit=" +
                            pageLimit + "&sortCriteria=" + sortCriteria));
            return authorService.renderAuthorListPage(usersJsonNode, CALLER_IS_NOT_MY_SPACE_PAGE, pageLimit, null,
                    "all", session("username"), Long.parseLong(session("id")));
        } catch (Exception e) {
            Logger.debug("AuthorController.authorList exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }

    public Result userListAdmin(Integer pageNum, String sortCriteria) {
        checkLoginStatus();

        try {

            // Set the offset and pageLimit.
            int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
            int offset = pageLimit * (pageNum - 1);

            JsonNode usersJsonNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.USER_LIST_ADMIN + "?offset=" + offset + "&pageLimit=" +
                            pageLimit + "&sortCriteria=" + sortCriteria));
            return userService.renderUserListPageAdmin(usersJsonNode, CALLER_IS_NOT_MY_SPACE_PAGE, pageLimit, null,
                    "all", session("username"), Long.parseLong(session("id")));
        } catch (Exception e) {
            Logger.debug("AuthorController.authorList exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }
    /**
     * This page intends to render the author detail page given a user id
     *
     * @param userId given author id
     * @return render the authorDetail.scala.html page
     */
    public Result authorDetailPage(Long userId) {
        checkLoginStatus();

        try {
            JsonNode userNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.AUTHOR_DETAIL + userId));
//            Author user = Author.deserialize(userNode);
            User user = User.deserialize(userNode);
//            return ok(authorDetail.render(user));
            return ok(authorDetail.render(user));
        } catch (Exception e) {
            Logger.debug("AuthorController.userDetailPage exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }

    public Result userDetailPageAdmin(Long userId) {
        checkLoginStatus();
        Logger.debug("Entered userDetailPageAdmin with userId: " + userId);
        try {
            String userUrl = RESTfulCalls.getBackendAPIUrl(config, Constants.USER_DETAIL_ADMIN + userId);
            Logger.debug("Fetching user from URL: " + userUrl);
            JsonNode userNode = RESTfulCalls.getAPI(userUrl);
            Logger.debug("Fetched userNode: " + userNode);

            User user = User.deserialize(userNode);
            Logger.debug("Deserialized user: " + user);

            ResearcherInfo researcherInfo = null;
            StudentInfo studentInfo = null;

            if(user.getUserType() != null){
                Logger.debug("User type is: " + user.getUserType());

                if (user.getUserType() == 1) {
                    String researcherUrl = RESTfulCalls.getBackendAPIUrl(config, Constants.RESEARCHER_DETAIL + userId);
                    Logger.debug("Fetching researcher from URL: " + researcherUrl);
                    JsonNode researcherNode = RESTfulCalls.getAPI(researcherUrl);
                    Logger.debug("Queried researcherInfo: " + researcherNode);

                    if (researcherNode != null && !researcherNode.has("error")) {
                        researcherInfo = ResearcherInfo.deserialize(researcherNode);
                        user.setResearcherInfo(researcherInfo);
                        Logger.debug("Deserialized and set researcherInfo");
                    } else {
                        Logger.debug("researcherNode is null or has error");
                    }
                }

                if (user.getUserType() == 4) {
                    String studentUrl = RESTfulCalls.getBackendAPIUrl(config, Constants.STUDENT_DETAIL + userId);
                    Logger.debug("Fetching student from URL: " + studentUrl);
                    JsonNode studentNode = RESTfulCalls.getAPI(studentUrl);
                    Logger.debug("Queried studentInfo: " + studentNode);

                    if (studentNode != null && !studentNode.has("error")) {
                        studentInfo = StudentInfo.deserialize(studentNode);
                        user.setStudentInfo(studentInfo);
                        Logger.debug("Deserialized and set studentInfo");
                    } else {
                        Logger.debug("studentNode is null or has error");
                    }
                }
            } else {
                Logger.debug("user.getUserType() is null");
            }

            Logger.debug("Fetching organization list");
            List<Organization> organizations = fetchOrganizationsList();
            Logger.debug("Fetched organizations: " + organizations);

            Organization org = new Organization();
            org.setId(-1);
            org.setOrganizationName("Other");
            organizations.add(org);
            Logger.debug("Added default organization 'Other'");

            Logger.debug("Rendering userDetailAdmin page");
            return ok(userDetailAdmin.render(userId, userFormAdmin, user, organizations, researcherInfo, studentInfo));
        } catch (Exception e) {
            Logger.debug("AuthorController.userDetailPageAdmin exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }




    /****************************************** End of Author Login Checking ******************************************/


    /**
     * This method intends to render an empty search.scala.html page
     *
     * @return
     */
    public Result searchPage() {
        checkLoginStatus();

        return ok(search.render("author"));
    }

    /**
     * This method intends to prepare data for rending author list result page
     *
     * @param pageNum
     * @param sortCriteria
     * @return: data prepared for authorList.scala.html (same as show all user list page)
     */
    public Result searchPOST(Integer pageNum, String sortCriteria) {
        checkLoginStatus();

        try {
            Form <Author> tmpForm = this.userForm.bindFromRequest();

            Map < String, String > tmpMap = tmpForm.rawData();


            JsonNode searchJson = Json.toJson(tmpMap);
            String searchString = "";



            int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
            int offset = pageLimit * (pageNum - 1);
            JsonNode usersJsonNode = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.GET_AUTHOR_BY_CONDITION + "?offset=" + offset + "&pageLimit=" +
                            pageLimit + "&sortCriteria=" + sortCriteria), searchJson);
            return authorService.renderAuthorListPage(usersJsonNode, CALLER_IS_NOT_MY_SPACE_PAGE,
                    pageLimit, searchString, "search", session("username"),
                    Long.parseLong(session("id")));
        } catch (Exception e) {
            Logger.debug("AuthorController.searchPOST exception: " + e.toString());
            return redirect(routes.Application.home());
        }
    }


    /**
     * This method intends to delete a user from authorEdit.scala.html page.
     *
     * @return
     */
    public Result authorDelete() {
        checkLoginStatus();
        Form < Author > user = userForm.bindFromRequest();

        String id = session("id");
        ObjectNode jsonData = Json.newObject();
        jsonData.put("id", id);

        JsonNode response = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                Constants.AUTHOR_DELETE), jsonData);
        if (response == null || response.has("error")) {
            Logger.debug("Author cannot be deleted and you have been logged out");
            return redirect(routes.Application.login());
        } else {
            session().clear();
            Logger.info("Author has been deleted and you have been logged out");
            return redirect(routes.Application.login());
        }

    }

    /**
     *
     * @param hashcode
     * @return
     */
    public Result authorSaved(String hashcode) {
        try {
            URLCodec urlCodec = new URLCodec();

            JsonNode response = RESTfulCalls
                    .getAPI(RESTfulCalls.getBackendAPIUrl(config, "/user/" + urlCodec.encode(hashcode, "UTF-8")));
            if (response == null || response.has("msg")) {
                String msg = response.get("msg").asText();
                if (msg.equals("Link is used.")) {
                    return redirect(routes.Application.showLinkIsAlreadyClick());
                }
                return redirect(routes.Application.showVerificationEmailIsExpired());
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return redirect(routes.Application.createUserSuccess());
    }

    /**
     *
     * @return
     * @throws Exception
     */
    public List < Author > getAllAuthors() throws Exception {
        JsonNode userJson = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.GET_ALL_AUTHORS));
        List < Author > users = new ArrayList < > ();
        for (JsonNode userNode: userJson) {
            users.add(Author.deserialize(userNode));
        }
        return users;
    }

    /**
     *
     * @return
     * @throws Exception
     */
    public Result topAuthors() throws Exception {
        JsonNode authorJson = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.GET_TOP_AUTHORS));
//        List < Author > authors = new ArrayList < > ();
        List<User> researchers = new ArrayList<>();
        for (JsonNode author: authorJson) {
//            authors.add(Author.deserialize(author));
            researchers.add(User.deserialize(author));
        }
//        return ok(topAuthors.render(authors));
        return ok(topAuthors.render(researchers));
    }

    /**
     *
     * @return
     */
    public Result allAuthorIds() {
        checkLoginStatus();
        try {
            JsonNode usersNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.GET_ALL_AUTHORS));
            ArrayNode res = Json.newArray();

            for (int i = 0; i < usersNode.size(); i++) {
                res.add(usersNode.get(i).get("id").asLong());
            }

            return ok(res);

        } catch (Exception e) {
            return ok("error");
        }
    }

}
