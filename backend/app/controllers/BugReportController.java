package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.*;
import models.rest.RESTResponse;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import services.BugReportService;
import services.UserService;
import utils.Common;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.PersistenceException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Named
@Singleton
public class BugReportController extends Controller {

    static private int solved = 1;
    static private int unsolved = 2;
    static private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private final BugReportService bugReportService;

    @Inject
    public BugReportController(BugReportService bugReportService) {
        this.bugReportService = bugReportService;
    }

    /************************************************ Add Bug Report ***************************************************/
    /**
     * This should add new bug report
     * @request body
     * {
     *      "title": String, (mandatory)
     *      "email": String, (mandatory)
     *      "name": String, (mandatory)
     *      "organization": String,
     *      "description": String, (mandatory)
     *      "longDescription": String,
     *      "solved": int or String that can be parsed into int
     * }
     * @return check if bug report is added
     */
    public Result addBugReport() {
        try {
            JsonNode json = request().body().asJson();
            BugReport bugReport = Json.fromJson(json, BugReport.class);
            Date date = new Date();
            bugReport.setCreateTime(date);

            bugReport.save();
            return created(Json.toJson(bugReport));
        } catch (Exception e) {
            Logger.debug("BugReportController.addBugReport() exception: " + e.toString());
            return badRequest("Bug report not created");
        }
    }
    /************************************************ End of Add Bug Report ********************************************/


    /************************************************ GET BUG REPORT **************************************************/
    /**
     * This method gets bug report by bug id
     * @param bugReportId bug id
     * @param format json format(other formats are not accepted)
     * @return bug report
     */
    public Result getBugReport(long bugReportId, String format) {
        try {
            BugReport bugReport = BugReport.find.query().where().eq("id", bugReportId).findOne();
            if (bugReport == null) {
                return notFound("Bug report not found with id: " + bugReportId);
            }

            String result = new String();
            if (format.equals("json")) {
                result = new Json().toJson(bugReport).toString();
            }

            return ok(result);
        } catch (Exception e) {
            Logger.debug("BugReportController.getBugReport() exception: " + e.toString());
            return badRequest("Bug report not found");
        }
    }

    /**
     * This method get all bug reports
     * @return all bug reports
     */
    public Result getAllBugReports(Optional<Integer> pageLimit, Optional<Integer> offset,Optional<String> sortCriteria) {
        String sortOrder = "date_created"; //by default
        if (sortCriteria.isPresent() && !sortCriteria.get().equals("")) {
            sortOrder = sortCriteria.get();
        }
        try {
            List<BugReport> bugReports = BugReport.find.all();
            bugReportService.sortBugReportList(bugReports, sortOrder);
            RESTResponse response = bugReportService.paginateResults(bugReports, offset, pageLimit, sortOrder);
            return ok(response.response());
        } catch (Exception e) {
            Logger.debug("BugReportController.getAllBugReports() exception: " + e.toString());
            return badRequest("Bug reports not found");
        }
    }
    /************************************************ End of GET Bug Report ********************************************/


    /************************************************ Update Bug Report ************************************************/
    /**
     * This method should update bug report by id
     * @param bugReportId bug report id
     * @request body
     * {
     *      "title": String, (mandatory)
     *      "email": String, (mandatory)
     *      "name": String, (mandatory)
     *      "organization": String,
     *      "description": String, (mandatory)
     *      "fixer": String,
     *      "registrationTime": String,
     *      "solved": int or String that can be parsed into int
     * }
     * @return check if bug report is updated.
     */
    public Result updateBugReport(long bugReportId) {
        try {
            if (bugReportId < 0) {
                Logger.debug("bug report id is negative!");
                return badRequest("id is negative!");
            }
            JsonNode jsonNode = request().body().asJson();
            if (jsonNode == null) {
                Logger.debug("Bug report not saved, expecting Json data");
                return badRequest("Bug report not saved, expecting Json data");
            }
            if(jsonNode.findPath("title").asText().equals("") || jsonNode.findPath("description").asText().equals(""))
                return badRequest("Bug Report not updated, missing some mandatory field");

            BugReport bugReport = BugReport.find.query().where().eq("id", bugReportId).findOne();
            bugReportService.updateBugReport(bugReport, jsonNode);
            bugReport.save();
            return created("Bug report updated: " + bugReport.getId());
        } catch (Exception e) {
            Logger.debug("BugReportController.updateBugReport() exception: " + e.toString());
            return badRequest("Bug report not updated: " + bugReportId);
        }
    }
    /************************************************ End of Update Bug Report *****************************************/


    /************************************************ Delete Bug Report ************************************************/
    /**
     * This method deletes bug report by bug report id
     * @param bugReportId bug report id
     * @return check if bug report is deleted
     */
    public Result deleteBugReport(long bugReportId) {
        if (bugReportId < 0) {
            return badRequest("bug report id is negative!");
        }
        try {
            BugReport bugReport = BugReport.find.query().where().eq("id", bugReportId).findOne();
            if (bugReport == null) {
                return notFound("Bug report not found with id: " + bugReportId);
            }

            bugReport.delete();
            return ok("Bug report is deleted: " + bugReportId);
        } catch (Exception e) {
            Logger.debug("BugReportController.deleteBugReport() exception: " + e.toString());
            return badRequest("Bug report not deleted: " + bugReportId);
        }
    }
    /************************************************ End of Delete Bug Report *****************************************/

    /************************************************ Mark Bug Report As Solved ****************************************/
    /**
     * This method receives a bug Id and marks it as solved
     * @param bugReportId given bug report id
     * @param fixerId   fixer user id
     * @return ok or badRequest
     */
    public Result updateBugReportSolved(long bugReportId, long fixerId) {
        if (bugReportId < 0) {
            return badRequest("bug report Id is negative!");
        }
        try {
            User fixer = User.find.byId(fixerId);
            BugReport bugReport = BugReport.find.query().where().eq("id", bugReportId).findOne();
            bugReport.setSolved(solved);
            bugReport.setFixer(fixer);
            Date date = new Date();
            bugReport.setSolveTime(date);

            bugReport.save();
            return created("Bug report updated: " + bugReport.getId());
        } catch (Exception e) {
            Logger.debug("BugReportController.updateBugReportSolved() exception: " + e.toString());
            return badRequest("Bug report not marked as solved: " + bugReportId);
        }
    }
    /************************************************ End of Mark Bug Report As Solved *********************************/








    /************************************************** Unused *********************************************************/
    /**
     * This method get all unsolved bug reports
     * @return all unsolved bug reports.
     */
    public Result getAllUnsolvedBugReports() {
        try {
            Iterable<BugReport> bugReports = BugReport.find.query().where().eq("solved", unsolved).findList();
            String result = new String();
            result = new Json().toJson(bugReports).toString();
            return ok(result);
        } catch (Exception e) {
            return badRequest("Unsolved bug reports not found");
        }
    }

    /**
     * This method get all solved bug reports
     * @return all solved bug reports.
     */
    public Result getAllSolvedBugReports() {
        try {
            Iterable<BugReport> bugReports = BugReport.find.query().where().eq("solved", solved).findList();
            String result = new String();
            result = new Json().toJson(bugReports).toString();
            return ok(result);
        } catch (Exception e) {
            return badRequest("Solved bug reports not found");
        }
    }


}