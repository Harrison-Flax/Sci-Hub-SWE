package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import models.Project;
import models.Reviewer;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import services.ProjectService;
import services.ReviewerService;
import utils.Constants;
import utils.RESTfulCalls;
import views.html.*;

import javax.inject.Inject;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static controllers.Application.checkLoginStatus;
import static controllers.Application.isPrivateProjectZone;
import static utils.Constants.CALLER_IS_NOT_MY_SPACE_PAGE;

public class ReviewerController extends Controller {

    @Inject
    Config config;

    private final ProjectService projectService;
    private final ReviewerService reviewerService;

    private Form < Reviewer > userForm;
    private FormFactory myFactory;


    @Inject
    public ReviewerController(FormFactory factory,
                              ProjectService projectService,
                              ReviewerService reviewerService) {
        userForm = factory.form(Reviewer.class);
        myFactory = factory;

        this.projectService = projectService;
        this.reviewerService = reviewerService;
    }

    /**
     * This method goes to the reviewer regitration  page.
     * @return
     */
    public Result reviewerRegisterPage() {
        return ok(reviewerRegister.render(userForm, Constants.PATTERN_RULES));
    }

    /**
     * This method gather reviewer regitration page input and creates a user.
     * @return
     */
    public Result reviewerRegisterPOST() {
        try {
            Form < Reviewer > userForm = this.userForm.bindFromRequest();
            ObjectNode jsonData = reviewerService.createJsonFromReviewerForm(userForm);
            jsonData.put("level", "normal");

            JsonNode response = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.REVIEWER_REGISTER_POST), jsonData);
            String newAuthorId = response.get("id").asText();
            if (newAuthorId != null) {
                return ok(registerConfirmation.render(new Long(newAuthorId), "Reviewer"));
            } else {
                Logger.debug("AuthorController user sign on backend error");
                return ok(registrationError.render("Reviewer"));
            }
        } catch (Exception e) {
            Logger.debug("AuthorController user sign on exception: " + e.toString());
            return ok(registrationError.render("Reviewer"));
        }
    }

    /**
     * This method renders the reviewer edit page (called in the menu as account management)
     *
     * @return render reviewer edit page
     */
    public Result reviewerEditPage() {
        checkLoginStatus();

        // Make sure a normal user can only edit his/her own profile page.
        String userId = session("id");
        try {
            JsonNode userNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.REVIEWER_DETAIL + userId));
            if (userNode == null || userNode.has("error")) {
                Logger.debug("AuthorController.userEditPage user cannot be found from backend: " + userId);
                Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
                return ok(generalError.render());
            }
            Reviewer user = Reviewer.deserialize(userNode);
            return ok(reviewerEdit.render(userId, userForm, user));
        } catch (Exception e) {
            Logger.debug("AuthorController.userEditPage() exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }

    /**
     * This method receives reviewer's information and updates reviewer's profile
     *
     * @return redirect to mySpace if the update succeeds or to homepage if update failed
     */
    public Result reviewerEditPOST() {
        checkLoginStatus();

        try {
            Form < Reviewer > userForm = this.userForm.bindFromRequest();

            ObjectNode jsonData = reviewerService.createJsonFromReviewerForm(userForm);
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
                    Constants.REVIEWER_EDIT_POST), jsonData);
            if (response == null || response.has("error")) {
                Logger.debug("Reviewer edit failed!");
                return ok(editError.render("Reviewer"));
            } else {
                //if the user updated successfully, then update the session's default project id as well (project zone)
                if (projectId != null) {
                    session("projectId", projectId.toString());
                } else {
                    session("projectId", Integer.toString(Constants.OPENNEX_PROJECT_ZONE_ID));
                }
                //update image in session as well
                Reviewer currentAuthor = Reviewer.deserialize(response);
                session("avatar", currentAuthor.getAvatar());
                return redirect(routes.ReviewerController.reviewerDetailPage(Long.parseLong(session("id"))));

            }
        } catch (Exception e) {
            Logger.debug("AuthorController.userEditPOST exception: " + e.toString());
            return ok(editError.render("Reviewer"));
        }
    }

    /**
     * This method intends to prepare data to render the pageNum of listing all reviewers with pagination
     * (reviewerList.scala.html)
     *
     * @param pageNum:      currrent page number
     * @param sortCriteria: sort column
     * @return: data for reviewerList.scala.html
     */
    public Result reviewerList(Integer pageNum, String sortCriteria) {
        checkLoginStatus();

        try {

            // Set the offset and pageLimit.
            int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
            int offset = pageLimit * (pageNum - 1);

            JsonNode usersJsonNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.REVIEWER_LIST + "?offset=" + offset + "&pageLimit=" + pageLimit +
                            "&sortCriteria=" + sortCriteria));
            return reviewerService.renderReviewerListPage(usersJsonNode, CALLER_IS_NOT_MY_SPACE_PAGE, pageLimit, null,
                    "all", session("username"), Long.parseLong(session("id")));
        } catch (Exception e) {
            Logger.debug("AuthorController.reviewerList exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }

    /**
     * This page intends to render the reviewer detail page given a user id
     *
     * @param userId given user id
     * @return render the reviewerDetail.scala.html page or if failed show the homepage.
     */
    public Result reviewerDetailPage(Long userId) {
        checkLoginStatus();

        try {
            JsonNode userNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.REVIEWER_DETAIL + userId));
            Reviewer user = Reviewer.deserialize(userNode);
            return ok(reviewerDetail.render(user));
        } catch (Exception e) {
            Logger.debug("ReviewerController.userDetailPage exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }

    /**
     * This method intends to render an empty search.scala.html page
     *
     * @return
     */
    public Result searchPage() {
        checkLoginStatus();

        return ok(search.render("Reviewer"));
    }

    /**
     * This method intends to prepare data for rending reviewer research result page
     *
     * @param pageNum
     * @param sortCriteria
     * @return: data prepared for reviewerList.scala.html (same as show all user list page)
     */
    public Result searchPOST(Integer pageNum, String sortCriteria) {
        checkLoginStatus();
        Project currentProjectZone = projectService.getCurrentProjectZone();

        try {
            Form < Reviewer > tmpForm = userForm.bindFromRequest();
            Map < String, String > tmpMap = tmpForm.data();

            if (isPrivateProjectZone()) {
                tmpMap.put("userId", session("id"));
            }

            JsonNode searchJson = Json.toJson(tmpMap);
            String searchString = "";

            // if not coming from the search input page, then fetch searchJson from the form from key "searchString"
            if (tmpMap.get("searchString") != null) {
                searchString = tmpMap.get("searchString");
                searchJson = Json.parse(searchString);
            } else {
                searchString = Json.stringify(searchJson);
            }

            //TODO: Find user with one blank between first name and last name???
            // The users to search might be someone I am following.
            // Set the offset and limit.
            int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
            int offset = pageLimit * (pageNum - 1);
            JsonNode usersJsonNode = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.GET_REVIEWER_BY_CONDITION + "?offset=" + offset + "&pageLimit=" + pageLimit +
                            "&sortCriteria=" + sortCriteria), searchJson);
            return reviewerService.renderReviewerListPage(usersJsonNode, CALLER_IS_NOT_MY_SPACE_PAGE, pageLimit,
                    searchString, "search", session("username"), Long.parseLong(session("id")));
        } catch (Exception e) {
            Logger.debug("ReviewerController.searchPOST exception: " + e.toString());
            return redirect(routes.Application.home());
        }
    }

    /**
     * This method intends to delete a reviewer from userEdit.scala.html page.
     *
     * @return
     */
    public Result reviewerDelete() {
        checkLoginStatus();
        Form < Reviewer > user = userForm.bindFromRequest();

        String id = session("id");
        ObjectNode jsonData = Json.newObject();
        jsonData.put("id", id);

        JsonNode response = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                Constants.REVIEWER_DELETE), jsonData);
        if (response == null || response.has("error")) {
            Logger.debug("Reviewer cannot be deleted and you have been logged out");
            return redirect(routes.Application.login());
        } else {
            session().clear();
            Logger.info("Reviewer has been deleted and you have been logged out");
            return redirect(routes.Application.login());
        }

    }

    /**
     *
     * @param hashcode
     * @return
     */
    public Result reviewerSaved(String hashcode) {
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
    public List < Reviewer > getAllReviewers() throws Exception {
        JsonNode userJson = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.GET_ALL_REVIEWERS));
        List < Reviewer > users = new ArrayList < > ();
        for (JsonNode userNode: userJson) {
            users.add(Reviewer.deserialize(userNode));
        }
        return users;
    }

    /**
     *
     * @return
     */
    public Result allReviewerIds() {
        checkLoginStatus();
        try {
            JsonNode usersNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.GET_ALL_REVIEWERS));
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