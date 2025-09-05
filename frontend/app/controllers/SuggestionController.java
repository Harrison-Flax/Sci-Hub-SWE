package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import models.BugReport;
import models.Suggestion;
import models.User;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import services.SuggestionService;
import utils.Constants;
import utils.RESTfulCalls;
import utils.UserPathRecorder;
import views.html.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static controllers.Application.checkLoginStatus;
import static utils.Common.beginIndexForPagination;
import static utils.Common.endIndexForPagination;

@With(UserPathRecorder.class)
public class SuggestionController extends Controller {
    @Inject
    Config config;

    private final SuggestionService suggestionService;

    FormFactory factory;
    private Form<Suggestion> suggestionFormTemplate;

    /******************************* Constructor ***********************************************************************/
    @Inject
    public SuggestionController(FormFactory factory, SuggestionService suggestionService) {
        this.factory = factory;
        suggestionFormTemplate = factory.form(Suggestion.class);

        this.suggestionService = suggestionService;
    }
    /******************************* End of Constructor ****************************************************************/


    /************************************************** Suggestion Registration ****************************************/
    /**
     * This method prepares for the suggestionRegister.scala.html page.
     *
     * @return
     */
    public Result suggestionRegisterPage() {
        checkLoginStatus();
        return ok(suggestionRegister.render());
    }

    /**
     * This method gathers input from suggestionRegister.scala.html page and registers the suggestion to backend API.
     *
     * @return
     */
    public Result suggestionRegisterPOST() {
        checkLoginStatus();

        try {
            Form<Suggestion> suggestionForm = suggestionFormTemplate.bindFromRequest();
            Suggestion suggestion = suggestionForm.get();
            suggestion.setSuggestionReporter(new User(Long.parseLong(session("id"))));

            ObjectNode jsonData = (ObjectNode) Json.toJson(suggestion);
            JsonNode response = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.SUGGESTION_REGISTER_POST), jsonData);
            if (response != null && !response.has("error")) {
                Suggestion createdSuggestion = Json.fromJson(response, Suggestion.class);
                long createdSuggestionId = createdSuggestion.getId();
                return ok(registerConfirmation.render(createdSuggestionId, "Suggestion"));
            } else {
                Logger.debug("SuggestionController.suggestionRegisterPOST() has error: " + response);
                throw new Exception("SuggestionController.suggestionRegisterPOST() has error: " + response);
            }
        } catch (Exception e) {
            Logger.debug("SuggestionController.suggestionRegisterPOST() exception: " + e.toString());
            return ok(registrationError.render("Suggestion"));
        }
    }
    /************************************************** End of Suggestion Registration *********************************/


    /************************************************** Suggestion Edit ************************************************/
    /**
     * This method  will direct you to the suggestion edit page
     *
     * @param
     * @return the update results
     */
    public Result suggestionEditPage(Long suggestionId) {
        checkLoginStatus();

        try {
            JsonNode jsonNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.GET_SUGGESTION_BY_ID + suggestionId));
            Suggestion suggestion = Json.fromJson(jsonNode, Suggestion.class);
            return ok(suggestionEdit.render(suggestion));
        } catch (Exception e) {
            Logger.debug("SuggestionController.suggestionEditPage() exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }


    /**
     * This method updates a suggestion information
     *
     * @param
     * @return the update results
     */
    public Result suggestionEditPOST(Long suggestionId) {
        Form<Suggestion> suggestionForm = suggestionFormTemplate.bindFromRequest();

        try {
            ObjectNode jsonData = suggestionService.prepareJsonNodeFromFormForEdit(suggestionForm);
            JsonNode response = RESTfulCalls.putAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.UPDATE_SUGGESTION + jsonData.get("id").asText()), jsonData);
            return ok(editConfirmation.render(suggestionId, null, "Suggestion"));
        } catch (Exception e) {
            Logger.debug("SuggestionController.suggestionEditPOST() exception: " + e.toString());
            return ok(editError.render("Suggestion"));
        }
    }
    /************************************************** End of Suggestion Edit *****************************************/


    /************************************************** Suggestion Detail **********************************************/
    /**
     * This page return the suggestion detail by suggestion id
     *
     * @param suggestionId
     * @return the page rendering the result
     */
    public Result suggestionDetail(Long suggestionId) {
        checkLoginStatus();

        try {
            JsonNode suggestionNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.GET_SUGGESTION_BY_ID + suggestionId));
            Suggestion suggestion = Json.fromJson(suggestionNode, Suggestion.class);
            return ok(suggestionDetail.render(suggestion));
        } catch (Exception e) {
            Logger.error("SuggestionController.suggestionDetail() cannot retrieve suggestionNode from backend: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }
    /************************************************** End of Suggestion Detail ***************************************/


    /************************************************** Suggestion List ************************************************/
    /**
     * This method receives a page number and shows all the suggestions in that page
     *
     * @param pageNum current page number
     * @return suggestions.scala.html or homepage
     */
    public Result suggestionList(long pageNum, String sort) {
        checkLoginStatus();

        // Set the offset and pageLimit.
        int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
        int initialOffset = pageLimit * ((int) pageNum - 1);
        try {
            // 1. get current user's email
            JsonNode userNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.GET_USER_PROFILE_BY_ID + session("id")));
            String currentUserEmail = userNode.findPath("email").asText();
            long currentUserId = Long.parseLong(session("id"));


            // 2. get all suggestions
            JsonNode suggestionsNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.SUGGESTION_LIST +
                    "?offset=" + initialOffset + "&pageLimit=" + pageLimit + "&sortCriteria=" + sort));
            List<Suggestion> suggestions = new ArrayList<Suggestion>();
            if (suggestionsNode.has("error") || !suggestionsNode.get("items").isArray()) {
                Logger.error("SuggestionController.suggestionList() cannot retrieve suggestionNode from backend: ");
                Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
                return ok(generalError.render());
            }
            for (JsonNode s : suggestionsNode.get("items")) {
                Suggestion suggestion = Json.fromJson(s, Suggestion.class);
                suggestions.add(suggestion);
            }
            // Offset
            int total = suggestionsNode.get("total").asInt();
            int count = suggestionsNode.get("count").asInt();
            int offset = suggestionsNode.get("offset").asInt();
            String retSort = suggestionsNode.get("sort").asText();
            int page = offset / pageLimit + 1;
            int beginIndexPagination = beginIndexForPagination(pageLimit, total, page);
            int endIndexPagination = endIndexForPagination(pageLimit, total, page);

            // 3. pass in user email (i.e. the user should not reolve/suggestionDelete other's registered suggestions)
            return ok(suggestionList.render(suggestions, currentUserEmail, currentUserId, pageNum, offset, total, count, pageLimit, beginIndexPagination, endIndexPagination, retSort));
        } catch (Exception e) {
            Logger.error("SuggestionController.suggestionList() exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }
    /************************************************** End of Suggestion List *****************************************/


    /************************************************** Suggestion Delete **********************************************/
    /**
     * This method receives a suggestion Id and deletes it
     *
     * @param suggestionId given suggestion Id
     * @return suggestion list
     */
    public Result suggestionDelete(long suggestionId) {
        checkLoginStatus();
        try {
            JsonNode response = RESTfulCalls.deleteAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.DELETE_ONE_SUGGESTION
                    + suggestionId));
            return redirect(routes.SuggestionController.suggestionList(1, "publish_time_stamp"));
        } catch (Exception e) {
            Logger.error("SuggestionController.suggestionDelete() exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }
    /************************************************** End of Suggestion Delete ***************************************/

    /************************************************** Mark Suggestion Solved *****************************************/
    /**
     * This method receives a suggestion Id and marks is at solved
     *
     * @param suggestionId given suggestion id
     * @param implementorId implementor user id
     * @return render suggestion list page
     */
    public Result markAsSolved(long suggestionId, long implementorId) {
        checkLoginStatus();

        try {
            JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.UPDATE_SUGGESTION_SOLVE + suggestionId + "/" + implementorId));
            return redirect(routes.SuggestionController.suggestionList(1, "publish_time_stamp"));
        } catch (Exception e) {
            Logger.error("SuggestionController.markAsSolved() exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }
    /************************************************** End of Mark Suggestion Solved **********************************/


}