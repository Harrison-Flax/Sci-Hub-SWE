package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import controllers.routes;
import models.User;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import utils.Constants;
import utils.RESTfulCalls;
import views.html.userList;
import views.html.userListAdmin;

import javax.inject.Inject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static controllers.Application.isPrivateProjectZone;
import static play.mvc.Controller.request;
import static play.mvc.Results.ok;
import static play.mvc.Results.redirect;
import static utils.Common.beginIndexForPagination;
import static utils.Common.endIndexForPagination;

import static play.mvc.Controller.session;


/**
 * This class intends to provide support for UserController.
 */
public class UserService {
    @Inject
    Config config;

    /**
     * This method intends to create a json from a User form.
     *
     * @param userForm given user form
     * @return json of user's information
     */
    public ObjectNode createJsonFromUserForm(Form<User> userForm) {
        ObjectNode jsonData = null;
        try {
            Map<String, String> tmpMap = userForm.data();
            jsonData = (ObjectNode)(Json.toJson(tmpMap));

            // Explicitly ensure homepage is added, just in case it's missing from the form map
            if (userForm.field("homepage") != null && userForm.field("homepage").value() != null) {
                jsonData.put("homepage", userForm.field("homepage").value());
            } // else {
            // jsonData.put("homepage", ""); // Or null if backend prefers
            // }
            Logger.debug("Final JSON being sent to backend: " + jsonData.toString());

            StringBuffer userName = new StringBuffer();
            userName.append(userForm.field("firstName").value());
            userName.append(" ");
            if (userForm.field("middleInitial").value() != null && !userForm.field("middleInitial").value().equals("")) {
                userName.append(userForm.field("middleInitial").value());
                userName.append(" ");
            }
            userName.append(userForm.field("lastName").value());
            jsonData.put("userName", userName.toString());
        } catch (Exception e) {
            Logger.debug("ServiceService.generateJsonNodeFromForm exception: " + e.toString());
            throw e;
        }
        return jsonData;
    }

    /**
     * This method retrieves public recaptcha key.
     * @return
     */
    public String getPublicRecaptchaKey() {
        String publicKey = null;
        // Add the recaptcha based on the server type.
        if (config.getString("system.frontend.host").equals("hawking.sv.cmu.edu")) {
            publicKey = config.getString("recaptcha.public.hawking.key");
        } else if (config.getString("system.frontend.host").equals("opennex.org")) {
            publicKey = config.getString("recaptcha.public.opennex.key");
        } else {
            publicKey = config.getString("recaptcha.public.scihub.key");
        }
        return publicKey;
    }



    /**
     * This method intends to create a user by adding a team member.
     * As long as email is provided, the team member should be tried to be registered.
     * @param name
     * @param email
     * @return
     * TODO: If email is empty, should notify no user created?
     */
    public Result createUserbyAddingTeamMember(String name, String email) {
        if (email == null || email.equals("")) return null;

        ObjectNode jsonData = Json.newObject();
        JsonNode response = null;

        jsonData.put("email", email);
        response = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.CHECK_EMAIL), jsonData);

        if (response == null || response.has("error")) {
            Logger.debug("UserService.createUserbyAddingTeamMember email not available.");
            return null;
        }

        String[] names = name.trim().split(" ");
        String firstName = names[0];
        String middleInitial = "";
        String lastName = "";

        if (names.length == 3) {
            middleInitial = names[1];
            lastName = names[2];
        } else if (names.length == 2) {
            lastName = names[1];
        }

        String userName;
        if (middleInitial == null || middleInitial.equals("")) userName = firstName + " " + lastName;
        else userName = firstName + " " + middleInitial + " " + lastName;

        ObjectNode userNode = Json.newObject();
        String url = "http://" + request().host() + "/login";
        if (!request().host().contains("localhost")) {
            url = "https://" + request().host() + "/login";
        }
        userNode.put("username", userName);
        userNode.put("firstName", firstName);
        userNode.put("lastName", lastName);
        userNode.put("email", email);
        // for generating email to show log in page for automatically registered users
        userNode.put("url", url);
        //call create user automatically function
        JsonNode autoUserRes = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.USER_REGISTER_AUTO),
                userNode);

        return ok();
    }

    /**
     * This method renders the user list page.
     * @param userListJsonNode
     * @param isCallerMySpacePage shows if the user list page is rendered from my space page or not
     * @param pageLimit
     * @param searchBody
     * @param listType            : "all"; "my follow"; "user publish"; "search"
     * @param username
     * @param userId
     * @return render user list page; If exception happened render homepage
     */
    public Result renderUserListPage(JsonNode userListJsonNode, boolean isCallerMySpacePage, int pageLimit,
                                     String searchBody, String listType, String username, Long userId) {
        try {
            if (userListJsonNode == null || userListJsonNode.has("error")) {
                Logger.debug("User list is empty or error!");
                return redirect(routes.Application.home());
            }

            JsonNode userJsonArray = userListJsonNode.get("items");

            if (!userJsonArray.isArray()) {
                Logger.debug("User list is not array!");
                return redirect(routes.Application.home());
            }

            List<User> users = new ArrayList<>();
            for (int i = 0; i < userJsonArray.size(); i++) {
                JsonNode json = userJsonArray.path(i);
                User user = User.deserialize(json);
                users.add(user);
            }

            // Offset
            String retSort = userListJsonNode.get("sort").asText();
            int total = userListJsonNode.get("total").asInt();
            int count = userListJsonNode.get("count").asInt();
            int offset = userListJsonNode.get("offset").asInt();
            int page = offset/pageLimit + 1;
            int beginIndexPagination = beginIndexForPagination(pageLimit, total, page);
            int endIndexPagination = endIndexForPagination(pageLimit, total, page);
            return ok(userList.render(users,
                    isPrivateProjectZone(), isCallerMySpacePage, page, retSort, offset, total, count, listType,
                    pageLimit, searchBody, username, userId, beginIndexPagination, endIndexPagination));
        } catch (Exception e) {
            Logger.debug("UserService.renderUserListPage exception: " + e.toString());
            return redirect(routes.Application.home());
        }
    }

    public Result renderUserListPageAdmin(JsonNode userListJsonNode, boolean isCallerMySpacePage, int pageLimit,
                                          String searchBody, String listType, String username, Long userId) {
        try {
            if (userListJsonNode == null || userListJsonNode.has("error")) {
                Logger.debug("User list is empty or error!");
                return redirect(routes.Application.home());
            }

            JsonNode userJsonArray = userListJsonNode.get("items");

            if (!userJsonArray.isArray()) {
                Logger.debug("User list is not array!");
                return redirect(routes.Application.home());
            }

            List<User> users = new ArrayList<>();
            for (int i = 0; i < userJsonArray.size(); i++) {
                JsonNode json = userJsonArray.path(i);
                User user = User.deserialize(json);
                users.add(user);
            }

            // Offset
            String retSort = userListJsonNode.get("sort").asText();
            int total = userListJsonNode.get("total").asInt();
            int count = userListJsonNode.get("count").asInt();
            int offset = userListJsonNode.get("offset").asInt();
            int page = offset/pageLimit + 1;
            int beginIndexPagination = beginIndexForPagination(pageLimit, total, page);
            int endIndexPagination = endIndexForPagination(pageLimit, total, page);
            return ok(userListAdmin.render(users,
                    isPrivateProjectZone(), isCallerMySpacePage, page, retSort, offset, total, count, listType,
                    pageLimit, searchBody, username, userId, beginIndexPagination, endIndexPagination));
        } catch (Exception e) {
            Logger.debug("UserService.renderUserListPage exception: " + e.toString());
            return redirect(routes.Application.home());
        }
    }

    /**
     * This method retrieves user's image from backend and set it as his/her avatar
     *
     * @param user given user
     */
    public void getUserImageForUser(User user) {
        JsonNode image = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                Constants.GET_USER_IMAGE_BY_USER_ID + user.getId()));
        String path = "";
        if (image != null && image.get("id") != null) {
            //IMPORTANT: please note that this USER_IMAGE_BY_ID_PATH is specifying the frontend route for getting the
            // image (not for backend route)
            path = Constants.USER_IMAGE_BY_IMAGE_ID_PATH + image.get("id").asLong();
            user.setAvatar(path);
        } else {
            path = "../../../../assets/images/user.png";
            user.setAvatar(path);
        }
        session("avatar", path);
    }

    /**
     * This method receives user form and update user's profile image accordingly
     *
     * @param userForm    user form from userEdit.scala.html
     * @param requestBody request body
     * @return jsonnode showing the result got from backend regarding the image update
     * or null if not image update was requested in the form or the image update doesn't go through
     * TODO: sometime we have to distinguish between the case that image was not updated because of an
     * error or no image update was requested
     */
    public JsonNode updateUserImage(Form<User> userForm, Http.RequestBody requestBody) {
        try {
            Http.MultipartFormData body = requestBody.asMultipartFormData();

            String img_op = userForm.field("record").value();

            if (img_op.equals("delete")) {
                return RESTfulCalls.deleteAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.USER_DELETE_IMAGE +
                        session("id")));
            } else if (img_op.equals("update")) {
                if (body.getFile("avatar") != null) {
                    Logger.info("Received Image");
                    Http.MultipartFormData.FilePart image = body.getFile("avatar");
                    System.out.println();
                    if (image != null && !image.getFilename().equals("")) {
                        Logger.info("Received Image File: " + image.getFilename());
                        File file = (File) image.getFile();
                        return RESTfulCalls.postAPIWithFile(RESTfulCalls.getBackendAPIUrl(config,
                                Constants.USER_UPDATE_IMAGE + session("id")), file);
                    }
                } else if (body.getFile("avatar") == null){
                    Logger.info("No Received Image");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
