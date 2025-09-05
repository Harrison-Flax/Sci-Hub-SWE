package controllers;

import actions.OperationLoggingAction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import models.Course;
import models.CourseTAAssignment;
import models.TACandidate;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import services.*;
import services.AccessTimesService;
import utils.Constants;
import utils.RESTfulCalls;
import views.html.*;


import javax.inject.Inject;
import static controllers.Application.checkLoginStatus;
/**
 * @author LUO, QIUYU
 * @version 1.0
 */
public class CourseTAAssignmentController extends Controller {

    @Inject
    Config config;

    private final CourseTAAssignmentService courseTAAssignmentService;

    private final AccessTimesService accessTimesService;
    private Form<CourseTAAssignment> courseTAAssignmentFormTemplate;



    private String [] preference = null;
    private String [] unwanted = null;



    /******************************* Constructor **********************************************************************/
    @Inject
    public CourseTAAssignmentController(FormFactory factory,
                                        AccessTimesService accessTimesService,
                                        CourseTAAssignmentService courseTAAssignmentService) {

        this.courseTAAssignmentFormTemplate = factory.form(CourseTAAssignment.class);
        this.courseTAAssignmentService = courseTAAssignmentService;
        this.accessTimesService = accessTimesService;


    }



    /**
     * Handles the POST request for TA-Course assignment registration.
     *
     * To be implemented.
     */


    /************************************************** TA Hiring Status List *****************************************************/

    @With(OperationLoggingAction.class)
    public Result taHiringStatusList(Integer pageNum, String sortCriteria) {
        checkLoginStatus();

        // If session contains the current projectId, set it.

        // Set the offset and pageLimit.
        int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
        try {
            JsonNode assignmentListJsonNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.TA_HIRING_STATUS_LIST + session("id") + "?pageNum=" +
                            pageNum + "&pageLimit=" + pageLimit + "&sortCriteria=" + sortCriteria));
            return courseTAAssignmentService.renderCourseTAAssignmentListPage(assignmentListJsonNode,
                    pageLimit, null, "all", session("username"),
                    Long.parseLong(session("id")));
        } catch (Exception e) {
            Logger.debug("CourseTAAssignmentController.taHiringStatusList() exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }


    /************************************************** End of TA Hiring Status List  **********************************************/


    /************************************************** CourseTAAssignment Detail ***************************************************/

    @With(OperationLoggingAction.class)
    public Result assignmentDetail(Long assignmentId) {
        try {
            CourseTAAssignment assignment = courseTAAssignmentService.getTAAssignmentById(assignmentId);


            if (assignment == null) {
                Logger.debug("CourseTAAssignmentController.assignmentDetail() get null from backend");
                Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
                return ok(generalError.render());
            }
            accessTimesService.AddOneTime("coursetaassignment", assignmentId);
            return ok(taAssignmentDetail.render(assignment));
        } catch (Exception e) {
            Logger.debug("CourseTAAssignmentController.assignmentDetail() exception: " + e.toString());
            e.printStackTrace();
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }

    /************************************************** End of CourseTAAssignment Detail ********************************************/


    public Result assignmentRegisterPOST() {
        checkLoginStatus();
        try {
            Form<CourseTAAssignment> assignmentForm = courseTAAssignmentFormTemplate.bindFromRequest();
            Logger.debug("assignmentRegisterPOST assignmentForm:" + assignmentForm);
            Http.MultipartFormData body = request().body().asMultipartFormData();
            JsonNode jsonData = courseTAAssignmentService.serializeFormToJson(assignmentForm);
            Logger.debug("assignmentRegisterPOST print jsonData:" + jsonData);
            JsonNode response = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.ASSIGNMENT_REGISTER_POST), jsonData);
            if (response == null || response.has("error")) {
                Logger.debug("CourseTAAssignmentController.assignmentRegisterPOST: Cannot create the TA assignment in backend");
                return ok(registrationError.render("TA Assignment"));
            }

            long assignmentId = response.asLong();

            return ok(registerConfirmation.render(new Long(assignmentId), "TA Assignment"));
        } catch (Exception e) {
            Logger.debug("CourseTAAssignmentController registration exception: " + e.toString());
            return ok(registrationError.render("TA Assignment"));
        }
    }




}
