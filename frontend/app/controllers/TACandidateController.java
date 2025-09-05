package controllers;

import actions.OperationLoggingAction;
import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import models.CourseTAAssignment;
import models.SimpleCourseTAAssignment;
import models.TACandidate;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
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
import java.util.List;

import static controllers.Application.checkLoginStatus;
/**
 * @author LUO, QIUYU
 * @version 1.0
 */
public class TACandidateController extends Controller {

    @Inject
    Config config;

    private final TACandidateService taCandidateService;

    private final AccessTimesService accessTimesService;
    private Form<TACandidate> tacandidateFormTemplate;



    private String [] preference = null;
    private String [] unwanted = null;



    /******************************* Constructor **********************************************************************/
    @Inject
    public TACandidateController(FormFactory factory,
                                 AccessTimesService accessTimesService,
                                 TACandidateService taCandidateService) {

        this.tacandidateFormTemplate = factory.form(TACandidate.class);
        this.taCandidateService = taCandidateService;
        this.accessTimesService = accessTimesService;

        this.preference = new String[]{
                "CS 1340 Introduction to Computing Concepts Python",
                "CS 1341 Principles of Computer Science Java",
                "CS 1342 Programming Concepts C++",
                "CS 2353 Discrete Math",
                "CS 2240 Assembly Language ARM",
                "CS 2341 Data Structures",
                "CS 3330 Database Concepts",
                "CS 3339 Information Assurance and Security",
                "CS 3342 Programming Language",
                "CS 3345 Graphical User Interface Design and Implementation",
                "CS 3353 Fundamentals of Algorithms",
                "CS 4344 Computer Networks and Distributed Systems",
                "CS 4345 Software Engineering Principles",
                "CS 4351/4352 Senior Design",
                "CS 4381 Digital Computer Design",
                "CS 5/7314 Software Testing and Quality Assurance",
                "CS 5/7315 Software Project Planning and Management",
                "CS 5/7316 Software Requirements",
                "CS 5/7317 Leadership for Architecting Software Systems",
                "CS 5/7319 Software Architecture and Design",
                "CS 5/7320 Artificial Intelligence",
                "CS 5/7323 Mobile Applications for Sensing and Learning",
                "CS 5/7331 Data Mining",
                "CS 5/7339 Computer System Security",
                "CS 5/7343 Operating Systems and System Software",
                "CS 5/7345 Advanced Application Programming",
                "CS 5/7350 Algorithm Engineering",
                "CS 5/7383 Computer Graphics",
                "CS 8313 Object Oriented Analysis and Design"
        };

        this.unwanted = new String[]{
                "CS 5/7323 Mobile Applications for Sensing and Learning",
                "CS 5/7331 Data Mining",
                "CS 5/7339 Computer System Security",
                "CS 5/7343 Operating Systems and System Software",
                "CS 5/7345 Advanced Application Programming",
                "CS 5/7350 Algorithm Engineering",
                "CS 5/7383 Computer Graphics",
                "CS 8313 Object Oriented Analysis and Design"
        };
    }



        /**
         * Render the TA candidate registration page.
         */
        @With(OperationLoggingAction.class)
        public Result tacandidateRegisterPage() {
            checkLoginStatus();
            return ok(tacandidateRegister.render());
        }
        /**
         * Handles the POST request for TA candidate registration.
         */
        public Result tacandidateRegisterPOST() {
            checkLoginStatus();
            try {
                Form<TACandidate> tacandidateForm = tacandidateFormTemplate.bindFromRequest();
                Logger.debug("tacandidateRegisterPOST print tacandidateForm:" + tacandidateForm);
                Http.MultipartFormData body = request().body().asMultipartFormData();
                JsonNode jsonData = taCandidateService.serializeFormToJson(tacandidateForm);
                Logger.debug("tacandidateRegisterPOST print jsonData:" + jsonData);
                JsonNode response = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                        Constants.TACANDIDATE_REGISTER_POST), jsonData);
                if (response == null || response.has("error")) {
                    Logger.debug("TACandidateController.tacandidateRegisterPOST: Cannot create the TA candidate in backend");
                    return ok(registrationError.render("TACandidate"));
                }

                long tacandidateId = response.asLong();
//            challengeService.savePDFToProject(body, projectId);

                return ok(registerConfirmation.render(new Long(tacandidateId), "Tacandidate"));
            } catch (Exception e) {
                Logger.debug("TACandidateController registration exception: " + e.toString());
                return ok(registrationError.render("TACandidate"));
            }
        }


        /************************************************** TAPool List *****************************************************/

        /**
         * This method intends to prepare data for all TA candidates.
         *
         * @patam pageNum
         * @patam sortCriteria: sortCriteria on some fields. Could be empty if not specified at the first time.
         * @return: data for tacandidateList.scala.html
         */
        @With(OperationLoggingAction.class)
        public Result tacandidateList(Integer pageNum, String sortCriteria) {
            checkLoginStatus();

            // Set the offset and pageLimit.
            int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
            try {
                JsonNode tacandidateListJsonNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                        Constants.TACANDIDATE_LIST + session("id") + "?pageNum=" +
                                pageNum + "&pageLimit=" + pageLimit + "&sortCriteria=" + sortCriteria));
                return taCandidateService.renderTACandidateListPage(tacandidateListJsonNode,
                        pageLimit, null, "all", session("username"),
                        Long.parseLong(session("id")));
            } catch (Exception e) {
                Logger.debug("TACandidateController.tajobList() exception: " + e.toString());
                Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
                return ok(generalError.render());
            }
        }


        /************************************************** End of TAPool List **********************************************/


    /************************************************** TACandidate Detail ***************************************************/
    /**
     * Ths method intends to return details of an TA candidate. If an TA candidate is not found, return to the all candidates page (page 1?).
     *
     * @patam tacandidateId: TA candidate id
     * @return: TACcandidate, a list of TA candidates to candidateDetail.scala.html
     */
        @With(OperationLoggingAction.class)
        public Result tacandidateDetail(Long tacandidateId) {
            try {
                TACandidate taCandidate = taCandidateService.getTACandidateById(tacandidateId);


                if (taCandidate == null) {
                    Logger.debug("TACandidateController.tacandidateDetail() get null from backend");
                    Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
                    return ok(generalError.render());
                }
                accessTimesService.AddOneTime("tacandidate", tacandidateId);
                return ok(tacandidateDetail.render(taCandidate));
            } catch (Exception e) {
                Logger.debug("TACandidateController.tacandidateDetail() exception: " + e.toString());
                e.printStackTrace();
                Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
                return ok(generalError.render());
            }
        }

    /************************************************** End of TACandidate Detail ********************************************/


    /************************************************** Get Assignments of current TACandidate  ***************************************************/
    @With(OperationLoggingAction.class)
    public Result getCurrentUserAssignments() {
        try {
            // Get the ID of the currently logged-in user from the session
            String currentUserId = session("id");
            if (currentUserId == null) {
                Logger.debug("No user logged in");
                Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
                return ok(generalError.render());
            }

            // Parse the user ID to Long as requisred by getTACandidateById method
            Long tacandidateId = Long.parseLong(currentUserId);
            List<SimpleCourseTAAssignment> assignments = taCandidateService.getAssignmentsByUser(tacandidateId);

            if (assignments == null || assignments.isEmpty()) {
                Logger.debug("getCurrentUserAssignments() found no assignments for user ID " + tacandidateId);
                Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
                return ok(generalError.render());
            }

            // Log access time for this candidate
            accessTimesService.AddOneTime("tacandidate", tacandidateId);

            // Render the assignments of the logged-in TACandidate
            return ok(taHoursSubmit.render(assignments));
        } catch (Exception e) {
            Logger.debug("getCurrentUserAssignments() exception: " + e.toString());
            e.printStackTrace();
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return internalServerError(generalError.render());
        }
    }


    /************************************************** End of Get Assignments of current TACandidate  ***************************************************/

}
