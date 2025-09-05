package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import models.BugReport;
import models.Suggestion;
import models.User;
import models.rest.RESTResponse;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import services.BugReportService;
import services.SuggestionService;
import utils.Common;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.PersistenceException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;


@Named
@Singleton
public class SuggestionController extends Controller {

    static private int solved = 1;
    static private int unsolved = 0;
    static private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private final SuggestionService suggestionService;

    @Inject
    public SuggestionController(SuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }

    /********************************************** Add Suggestion *****************************************************/
    /**
     * This method intends to register a suggestion into database.
     * @return
     */
    public Result addSuggestion() {
        try {
            JsonNode json = request().body().asJson();
            Suggestion suggestion = Json.fromJson(json, Suggestion.class);

            Date date = new Date();
            suggestion.setCreateTime(date);

            suggestion.save();
            return ok(Json.toJson(suggestion));
            //return created(new Json().toJson(suggestion.getId()).toString());
        } catch (Exception e) {
            Logger.debug("Failed to add suggestion exception: " + e.toString());
            return Common.badRequestWrapper("Failed to add suggestion.");
        }
    }
    /********************************************** End of Add Suggestion **********************************************/

    /********************************************** Get Suggestion *****************************************************/
    /**
     * This method should return all suggestions given suggestion id
     * @param suggestionId suggestion id
     * @param format json format
     * @return suggestions
     */
    public Result getSuggestion(long suggestionId, String format) {
        try {
            Suggestion suggestion = Suggestion.find.query().where().eq("id", suggestionId).findOne();
            if (suggestion == null) {
                System.out.println("Suggestion not found with name: " + suggestionId);
                return notFound("Suggestion not found with name: " + suggestionId);
            }

            String result = new String();
            if (format.equals("json")) {
                result = new Json().toJson(suggestion).toString();
            }

            return ok(result);
        } catch (Exception e) {
            Logger.debug("Failed to get suggestion exception: " + e.toString());
            return Common.badRequestWrapper("Failed to get suggestion.");
        }
    }
    /********************************************** End of Get Suggestion **********************************************/

    /********************************************** Update Suggestion **************************************************/
    public Result updateSuggestion(long suggestionId) {
        try {
            if (suggestionId < 0) {
                return badRequest("id is negative!");
            }
            JsonNode json = request().body().asJson();
            if (json == null) {
                return badRequest("Suggestion not saved, expecting Json data");
            }

            Suggestion newSuggestion = Json.fromJson(json, Suggestion.class);

            Suggestion oldSuggestion = Suggestion.find.query().where().eq("id", suggestionId).findOne();

            newSuggestion.setSolved(oldSuggestion.getSolved());
            newSuggestion.setCreateTime(oldSuggestion.getCreateTime());
            newSuggestion.setSolveTime(oldSuggestion.getSolveTime());

            newSuggestion.update();
            return created("Suggestion updated: " + newSuggestion.getId());
        } catch (Exception e) {
            Logger.debug("Failed to update suggestion exception: " + e.toString());
            return Common.badRequestWrapper("Failed to update suggestion.");
        }
    }
    /********************************************** End of Update Suggestion *******************************************/

    /********************************************** Suggestion List ****************************************************/
    /**
     * This method get all suggestions
     * @return all suggestions
     */
    public Result suggestionList(Optional<Integer> pageLimit, Optional<Integer> offset, Optional<String> sortCriteria) {
        String sortOrder = "date_created";
        if (sortCriteria.isPresent() && !sortCriteria.get().equals("")) {
            // If it is popularity, for now check its latest news's followers.
            sortOrder = sortCriteria.get();
        }
        try {
            List<Suggestion> suggestions = Suggestion.find.all();

            sortSuggestionList(suggestions, sortOrder);
            RESTResponse response = suggestionService.paginateResults(suggestions, offset, pageLimit, sortOrder);
            return ok(response.response());
        } catch (Exception e) {
            Logger.debug("SuggestionController.suggestionList() exception: " + e.toString());
            return badRequest("Suggestion list not found");
        }
    }
    /********************************************** End of Suggestion List *********************************************/

    /********************************************** Delete Suggestion **************************************************/
    public Result deleteSuggestion(long suggestionId) {
        try {
            if (suggestionId < 0) {
                System.out.println("id is negative!");
                return badRequest("id is negative!");
            }
            Suggestion suggestion = Suggestion.find.query().where().eq("id", suggestionId).findOne();
            if (suggestion == null) {
                System.out.println("Suggestion not found with id: " + suggestionId);
                return notFound("Suggestion not found with id: " + suggestionId);
            }

            suggestion.delete();
            return ok("Suggestion is deleted: " + suggestionId);
        } catch (Exception e) {
            Logger.debug("SuggestionController.deleteSuggestion() exception: " + e.toString());
            return badRequest("Suggestion note deleted.");
        }
    }
    /********************************************** End of Delete Suggestion *******************************************/

    /********************************************** Mark Suggestion Solved *********************************************/
    /**
     * This method receives a suggestion Id and marks is at solved
     * @param suggestionId given sugesssion id
     * @param implementorId implementor user id
     * @return ok or badRequest
     */
    public Result updateSuggestionSolved(long suggestionId, long implementorId) {
        if (suggestionId < 0) {
            return badRequest("suggestionId is negative!");
        }
        try {
            User implementor = User.find.byId(implementorId);
            Suggestion suggestion = Suggestion.find.query().where().eq("id", suggestionId).findOne();
            suggestion.setSolved(solved);
            suggestion.setSuggestionImplementor(implementor);
            suggestion.setSolveTime(new Date());
            suggestion.save();
            return created("Suggestion updated: " + suggestion.getId());
        } catch (Exception e) {
            Logger.debug("SuggestionController.updateSuggestionSolved() exception: " + e.toString());
            return badRequest("Suggestion not marked as solved.");
        }
    }
    /********************************************** End of Mark Suggestion Solved **************************************/









    /**
     * Sort the given list of bugReports
     *
     * @param suggestions  List of bugReports to be sorted
     * @param sortOrder sort criteria
     * @return sorted list of bugReports.
     */
    public void sortSuggestionList(List<Suggestion> suggestions, String sortOrder) {
        suggestions.sort((suggestion1, suggestion2) -> {
            if (sortOrder.equals("date_created"))
                return suggestion2.getCreateTime().compareTo(suggestion1.getCreateTime());
            else if (sortOrder.equals("reporter_name"))
                return suggestion1.getSuggestionReporter().getUserName().compareTo(suggestion2.getSuggestionReporter().getUserName());
            else if (sortOrder.equals("title"))
                return suggestion1.getTitle().compareTo(suggestion2.getTitle());
            else
                return suggestion2.getCreateTime().compareTo(suggestion1.getCreateTime());
        });
    }




}