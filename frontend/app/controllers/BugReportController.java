package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import models.BugReport;
import models.User;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import services.BugReportService;
import utils.Constants;
import utils.RESTfulCalls;
import utils.UserPathRecorder;
import views.html.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static controllers.Application.checkLoginStatus;
import static utils.Common.beginIndexForPagination;
import static utils.Common.endIndexForPagination;


public class BugReportController extends Controller {

    @Inject
    Config config;

    private final BugReportService bugReportService;

    private Form<BugReport> bugReportFormTemplate;

    /******************************* Constructor ***********************************************************************/
    @Inject
    public BugReportController(FormFactory factory, BugReportService bugReportService) {
        bugReportFormTemplate = factory.form(BugReport.class);

        this.bugReportService = bugReportService;
    }
    /******************************* End of Constructor ****************************************************************/


    /************************************************** Bug Report Registration ****************************************/
    /**
     * This method prepares to render bugReportRegister.scala.html
     *
     * @return
     */
    public Result bugReportRegisterPage() {
        checkLoginStatus();

        return ok(bugReportRegister.render());
    }


    /**
     * This method gathers bug report page input and calls backend to register a bug report.
     */
    public Result bugReportRegisterPOST() {
        checkLoginStatus();

        try {
            Form<BugReport> bugReportForm = bugReportFormTemplate.bindFromRequest();
            BugReport bugReport = bugReportForm.get();
            bugReport.setReporter(new User(Long.parseLong(session("id"))));

            ObjectNode jsonData = (ObjectNode)Json.toJson(bugReport);
            JsonNode response = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.BUG_REPORT_REGISTER_POST), jsonData);
            if (response != null && !response.has("error")) {
                BugReport createdBugReport = Json.fromJson(response, BugReport.class);
                long createdBugReportId = createdBugReport.getId();
                return ok(registerConfirmation.render(new Long(createdBugReportId), "BugReport"));
            } else {
                Logger.debug("BugReportController.bugReportRegisterPOST() bugReportIdJson has error: " + response);
                throw new Exception("BugReportController.bugReportRegisterPOST() bugReportIdJson has error: " + response);
            }
        } catch (Exception e) {
            Logger.debug("BugReportController.bugReportRegisterPOST() exception: " + e.toString());
            return ok(registrationError.render("BugReport"));
        }
    }
    /**************************************************End of Bug Report Registration **********************************/


    /************************************************** Bug Report Edit ***********************************************/
    /**
     * This method  will direct you to the bug report edit page
     *
     * @param
     * @return
     */
    public Result bugReportEditPage(Long bugReportId) {
        checkLoginStatus();

        try {
            JsonNode jsonNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.GET_BUG_REPORT + bugReportId));
            BugReport bugReport = BugReport.deserialize(jsonNode);
            return ok(bugReportEdit.render(bugReport));
        } catch (Exception e) {
            Logger.debug("BugReportController.bugReportEditPage exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }


    /**
     * This method sends the edited information about a bug report to backend.
     *
     * @param
     * @return the update results
     */
    public Result bugReportEditPOST(long bugReportId) {
        checkLoginStatus();

        try {
            Form<BugReport> bugReportForm = bugReportFormTemplate.bindFromRequest();
            Map<String, String> tmpMap = bugReportForm.data();
            ObjectNode jsonData = (ObjectNode) (Json.toJson(tmpMap));

            JsonNode response = RESTfulCalls.putAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.BUG_REPORT_EDIT_POST + jsonData.get("id").asText()), jsonData);
            return ok(editConfirmation.render(bugReportId, null, "BugReport"));
        } catch (Exception e) {
            Logger.debug("BugReportController.bugReportEditPOST() exception: " + e.toString());
            return ok(editError.render("BugReport"));
        }
    }
    /************************************************** End of Bug Report Edit *****************************************/


    /************************************************** Bug Report Detail **********************************************/
    /**
     * This method prepares a bug report by id
     *
     * @param bugReportId
     * @return the page with bug report id
     */
    public Result bugReportDetail(Long bugReportId) {
        checkLoginStatus();

        try {
            JsonNode bugReportNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.GET_BUG_REPORT + bugReportId));
            BugReport bugReport = BugReport.deserialize(bugReportNode);
            return ok(bugReportDetail.render(bugReport));
        } catch (Exception e) {
            Logger.error("BugReportController.bugReportDetail() cannot retrieve bugReportNode from backend: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }
    /************************************************** End of Bug Report Detail ***************************************/


    /************************************************** Bug Report List ************************************************/
    /**
     * This method receives a page number and shows all the bug reports in that page
     *
     * @param pageNum current page number
     * @param sort    sort criteria
     * @return bugs.scala.html or homepage
     */
    public Result bugReportList(long pageNum, String sort) {
        checkLoginStatus();

        // Set the offset and pageLimit.
        int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
        int initialOffset = pageLimit * ((int) pageNum - 1);
        try {
            // 1. get current user's email
            JsonNode userNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.GET_USER_PROFILE_BY_ID + session("id")));
            String currentUserEmail = userNode.findPath("email").asText();
            long currentUserId = Long.parseLong(session("id"));

            // 2. get all bug reports
            JsonNode bugReportsNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.GET_ALL_BUG_REPORTS +
                    "?offset=" + initialOffset + "&pageLimit=" + pageLimit + "&sortCriteria=" + sort));
            List<BugReport> bugReports = new ArrayList<BugReport>();
            if (bugReportsNode.has("error") || !bugReportsNode.get("items").isArray()) {
                Logger.error("BugReportController.bugReportList() cannot retrieve bugReportNode from backend: ");
                Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
                return ok(generalError.render());
            }
            for (JsonNode bug : bugReportsNode.get("items")) {
                BugReport bugReport = BugReport.deserialize(bug);
                bugReports.add(bugReport);
            }

            // Offset
            int total = bugReportsNode.get("total").asInt();
            int count = bugReportsNode.get("count").asInt();
            int offset = bugReportsNode.get("offset").asInt();
            String retSort = bugReportsNode.get("sort").asText();
            int page = offset / pageLimit + 1;
            int beginIndexPagination = beginIndexForPagination(pageLimit, total, page);
            int endIndexPagination = endIndexForPagination(pageLimit, total, page);

            // 3. pass in user email (i.e. the user should not reolved/delete other's registered bug reports)
            return ok(bugReportList.render(bugReports, currentUserEmail, currentUserId, pageNum, offset, total, count, pageLimit, beginIndexPagination, endIndexPagination, retSort));
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("BugReportController.bugReportList() exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }
    /************************************************** End of Bug Report List *****************************************/


    /************************************************** Bug Report Delete **********************************************/
    /**
     * This method receives a bug report Id and deletes it
     *
     * @param bugReportId given bug Id
     * @return bug list
     */
    public Result bugReportDelete(long bugReportId) {
        checkLoginStatus();

        try {
            JsonNode response = RESTfulCalls.deleteAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.DELETE_BUG_REPORT + bugReportId));

            return redirect(routes.BugReportController.bugReportList(1, "publish_time_stamp"));
        } catch (Exception e) {
            Logger.error("BugReportController.bugReportDelete() exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }
    /************************************************** End of Bug Report Delete ***************************************/


    /************************************************** Bug Report Solved **********************************************/
    /**
     * This method receives a bug report Id and marks it as solved
     *
     * @param bugReportId given bug report id
     * @param fixerId   fixer user id
     * @return render bug list page
     */
    public Result markAsSolved(long bugReportId, long fixerId) {
        checkLoginStatus();

        try {
            JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.UPDATE_BUG_SOLVED + bugReportId + "/" + fixerId));
            return redirect(routes.BugReportController.bugReportList(1, "publish_time_stamp"));
        } catch (Exception e) {
            Logger.error("BugReportController.markAsSolved() exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }
    /************************************************** End of Bug Report Solved ***************************************/

}