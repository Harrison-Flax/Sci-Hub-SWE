package controllers;

import actions.OperationLoggingAction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import models.Job;
import models.JobApplication;
import models.RAJob;
import models.RAJobApplication;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import services.*;
import utils.Common;
import utils.Constants;
import utils.RESTfulCalls;
import views.html.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.Arrays;
import java.util.HashSet;

import static controllers.Application.checkLoginStatus;
import static utils.Common.beginIndexForPagination;
import static utils.Common.endIndexForPagination;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class RAJobController extends Controller {

    @Inject
    Config config;
    private FileController fileController;

    private final RAJobService rajobService;
    private final RAJobApplicationService rajobApplicationService;

    private final UserService userService;
    private final AccessTimesService accessTimesService;

    private Form<RAJob> rajobFormTemplate;
    private Form<RAJobApplication> rajobApplicationFormTemplate;
    private FormFactory myFactory;
    private final FileService fileService;


    /******************************* Constructor **********************************************************************/
    @Inject
    public RAJobController(FormFactory factory,
                           RAJobService rajobService,
                           UserService userService,
                           RAJobApplicationService rajobApplicationService,
                           AccessTimesService accessTimesService,
                           FileService fileService) {
        rajobFormTemplate = factory.form(RAJob.class);
        myFactory = factory;
        rajobApplicationFormTemplate = factory.form(RAJobApplication.class);

        this.rajobApplicationService = rajobApplicationService;
        this.rajobService = rajobService;
        this.userService = userService;
        this.accessTimesService = accessTimesService;
        this.fileService = fileService;
    }


    /************************************************** RA Job Registration ********************************************/

    /**
     * This method intends to render the RA job registration page.
     *
     * @return
     */
    @With(OperationLoggingAction.class)
    public Result rajobRegisterPage() {
        checkLoginStatus();
        String userTypes = session("userTypes");
        String userId    = session("id");
        String userEmail  = session("email");

        JsonNode professorsJson = Json.newArray();

        if ("0".equals(userTypes)) {
            String url = RESTfulCalls.getBackendAPIUrl(config, "/rajob/professors");
            professorsJson = RESTfulCalls.getAPI(url);
            if (professorsJson == null || !professorsJson.isArray()) {
                professorsJson = Json.newArray();
            }
        }

        return ok(views.html.rajobRegister.render(
                userTypes,
                userId,
                userEmail,
                professorsJson
        ));
    }

    /**
     * This method intends to gather RA job registration information and create an RA job in database.
     *
     * @return
     */
    public Result rajobRegisterPOST() {
        checkLoginStatus();
        Logger.debug("▶ Entering RAJobController.rajobRegisterPOST");
        try {
            Form<RAJob> rajobForm = rajobFormTemplate.bindFromRequest();
            if (rajobForm.hasErrors()) {
                Logger.error("Form binding errors: " + rajobForm.errorsAsJson());
                return badRequest(registrationError.render("RAJob"));
            }
            Logger.debug("Form bound successfully: " + rajobForm.toString());
            ObjectNode jsonData = (ObjectNode) rajobService.serializeFormToJson(rajobForm);
            Logger.debug("Dry-run form data: " + jsonData.toString());
            String posterEmail = jsonData
                    .path("rajobPublisher")
                    .path("email")
                    .asText();
            Logger.debug("Poster email (professor) from form = " + posterEmail);

            Http.MultipartFormData body = request().body().asMultipartFormData();
            if (body == null) {
                Logger.error("MultipartFormData body is null");
                return badRequest(registrationError.render("RAJob"));
            }

            Logger.debug("Serialized form to JSON: " + jsonData.toString());

            String createUrl = RESTfulCalls.getBackendAPIUrl(config, Constants.RAJOB_REGISTER_POST);
            JsonNode createResp = RESTfulCalls.postAPI(createUrl, jsonData);
            if (createResp == null || createResp.has("error")) {
                Logger.error("Backend failed to create RAJob: " + (createResp == null ? "null" : createResp.get("error").asText()));
                return ok(registrationError.render("RAJob"));
            }
            long rajobId = createResp.asLong();
            Logger.debug("Created RAJob with ID = " + rajobId);

            JsonNode fileResp = fileService.uploadFile(
                    body,
                    "rajobPdf",
                    "rajob",
                    "rajob",
                    rajobId
            );
            if (fileResp == null || fileResp.has("error")) {
                Logger.error("PDF upload failed: " + (fileResp == null ? "null" : fileResp.get("error").asText()));
            } else {
                Logger.debug("PDF upload succeeded: " + fileResp.toString());
            }

            ObjectMapper mapper = new ObjectMapper();
            java.io.File file = new java.io.File("public/data/student.json");
            ArrayNode studentArray = (ArrayNode) mapper.readTree(file);
            ObjectNode notifyData = Json.newObject();
            notifyData.put("rajobId", rajobId);
            ArrayNode students = notifyData.putArray("students");
            for (JsonNode student : studentArray) {
                if (student.has("email")) {
                    students.add(student.get("email").asText());
                }
            }
            Logger.debug("Loaded " + students.size() + " student emails for notification");

            // String posterEmail = session("email");
            ArrayNode ccNode = notifyData.putArray("ccSelected");
            if (posterEmail != null && !posterEmail.isEmpty()) {
                ccNode.add(posterEmail);
                Logger.debug("Auto-filled CC with session email: " + posterEmail);
            } else {
                Logger.warn("Poster email is null or empty, skipping CC");
            }

            String notifyUrl = RESTfulCalls.getBackendAPIUrl(config, "/rajob/sendPostedEmail");
            Logger.debug("POST to notify URL: " + notifyUrl);
            JsonNode notifyResponse = RESTfulCalls.postAPI(notifyUrl, notifyData);
            Logger.info("Email notify response = " + (notifyResponse == null ? "null" : notifyResponse.toString()));

            return ok(registerConfirmation.render(rajobId, "Rajob"));

        } catch (Exception e) {
            Logger.error("Exception in rajobRegisterPOST: ", e);
            return ok(registrationError.render("RAJob"));
        }
    }

    /************************************************** End of RAJob Registration **************************************/


    /************************************************** RAJob Edit *****************************************************/

    /**
     * This method intends to prepare to edit an RA job.
     *
     * @param rajobId: RA job id
     * @return
     */
    @With(OperationLoggingAction.class)
    public Result rajobEditPage(Long rajobId, String status) {
        try {
            RAJob rajob = rajobService.getRAJobById(rajobId);
            if (rajob == null) {
                Logger.debug("RAJob.rajobEditPage exception: cannot get RA job by id");
                Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
                return ok(generalError.render());
            }
            accessTimesService.AddOneTime("rajob", rajobId);
            Long userId = Long.parseLong(session("id"));
            String tableName = "rajob";
            String rajobFileType = "rajob";
            String tableRecorderId = rajobId.toString();
            String backendPort = Constants.CMU_BACKEND_PORT;
            Boolean rajobDocument = fileService.checkFile(tableName, rajobFileType, tableRecorderId);
            Logger.info("Result of checkFile: " + rajobDocument);
            return ok(rajobEdit.render(rajob, status,
                    tableName,
                    rajobFileType,
                    tableRecorderId,
                    backendPort,
                    rajobDocument));
        } catch (Exception e) {
            Logger.debug("RAJobController.rajobEditPage() exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }


    /**
     * This method intends to submit the edit in the RA job edit page.
     *
     * @param rajobId RA job id
     * @return
     */
    public Result rajobEditPOST(Long rajobId) {
        checkLoginStatus();
        try {
            Form<RAJob> rajobForm = rajobFormTemplate.bindFromRequest();
            if (rajobForm.hasErrors()) {
                return redirect(routes.RAJobController.rajobEditPage(rajobId, "ACTIVE"));
            }

            JsonNode jsonData = rajobService.serializeFormToJson(rajobForm);
            JsonNode response = RESTfulCalls.postAPI(
                    RESTfulCalls.getBackendAPIUrl(config, Constants.RAJOB_EDIT_POST + rajobId),
                    jsonData
            );
            if (response == null || response.has("error")) {
                return redirect(routes.RAJobController.rajobEditPage(rajobId, "ACTIVE"));
            }

            Http.MultipartFormData body = request().body().asMultipartFormData();
            if (body != null && body.getFile("rajobPdf") != null) {
                fileService.uploadFile(
                        body,
                        "rajobPdf",   // form field name
                        "rajob",      // tableName
                        "rajob",      // fileType prefix
                        rajobId       // recordId
                );
            }
            return ok(editConfirmation.render(rajobId, 0L, "Rajob"));
        } catch (Exception e) {
            Logger.error("rajobEditPOST failed", e);
            return ok(editError.render("RAJob"));
        }
    }

    /************************************************** End of RAJob Edit **********************************************/

    /************************************************** RAJob List *****************************************************/

    /**
     * This method intends to prepare data for all RA jobs.
     *
     * @param pageNum
     * @param sortCriteria: sortCriteria on some fields. Could be empty if not specified at the first time.
     * @return: data for jobList.scala.html
     */
    @With(OperationLoggingAction.class)
    public Result rajobList(Integer pageNum, String sortCriteria) {
        checkLoginStatus();

        // If session contains the current projectId, set it.

        // Set the offset and pageLimit.
        int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
        try {
            JsonNode rajobListJsonNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.RAJOB_LIST + session("id") + "?pageNum=" +
                            pageNum + "&pageLimit=" + pageLimit + "&sortCriteria=" + sortCriteria)); // default value 
            return rajobService.renderRAJobListPage(rajobListJsonNode,
                    pageLimit, null, "all", session("username"),
                    Long.parseLong(session("id")));
        } catch (Exception e) {
            Logger.debug("RAJobController.rajobList() exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }
    
    /**
     * Returns RA job data for RA type distribution visualization with optional filters.
     */
    public Result rajobPayRanges() {
        checkLoginStatus();

        String userId = session("id");
        String department = request().getQueryString("department");
        String mode = request().getQueryString("mode");
        String status = request().getQueryString("status");
        int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY) * 5;

        try {
            String url = RESTfulCalls.getBackendAPIUrl(config,
                    Constants.RAJOB_LIST + userId + "?pageNum=1&pageLimit=" + pageLimit + "&sortCriteria=");
            JsonNode rajobListJsonNode = RESTfulCalls.getAPI(url);
            if (rajobListJsonNode == null || rajobListJsonNode.has("error")) {
                return ok(emptyPayRangeResponse());
            }

            JsonNode items = rajobListJsonNode.path("items");
            if (items == null || !items.isArray()) {
                return ok(emptyPayRangeResponse());
            }

            ArrayNode filteredJobs = Json.newArray();
            Set<String> departments = new HashSet<>();
            Set<String> modes = new HashSet<>();
            Set<String> statuses = new HashSet<>();
            int hourlyCount = 0;
            int fullTimeCount = 0;
            for (JsonNode job : items) {
                if (matchesPayRangeFilters(job, department, mode, status)) {
                    filteredJobs.add(job);
                    String org = job.path("organization").asText("");
                    String loc = job.path("location").asText("");
                    String jobStatus = job.path("status").asText("");
                    if (!org.isEmpty()) {
                        departments.add(org);
                    }
                    if (!loc.isEmpty()) {
                        modes.add(loc);
                    }
                    if (!jobStatus.isEmpty()) {
                        statuses.add(jobStatus);
                    }
                    int raType = job.path("raTypes").asInt(0);
                    if (raType == 1) {
                        hourlyCount += 1;
                    } else if (raType == 2) {
                        fullTimeCount += 1;
                    }
                }
            }

            ObjectNode response = Json.newObject();
            ObjectNode counts = response.putObject("counts");
            counts.put("hourly", hourlyCount);
            counts.put("fullTime", fullTimeCount);
            counts.put("withType", hourlyCount + fullTimeCount);

            ObjectNode filters = response.putObject("filters");
            filters.set("departments", toArrayNode(departments));
            filters.set("modes", toArrayNode(modes));
            filters.set("statuses", toArrayNode(statuses));

            response.set("jobs", filteredJobs);

            return ok(response);
        } catch (Exception e) {
            Logger.debug("RAJobController.rajobPayRanges() exception: " + e.toString());
            return ok(emptyPayRangeResponse());
        }
    }


    /************************************************** End of RAJob List **********************************************/

    /************************************************** My Posted RAJob *************************************************/
    /**
     * get all rajobs posted by user
     *
     * @param pageNum
     * @return Job list posted by current user
     */
    @With(OperationLoggingAction.class)
    public Result rajobListPostedByUser(Integer pageNum) {
        checkLoginStatus();
        String userId = session("id");
        try {
            JsonNode rajobsNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.RAJOB_POSTED_BY_USER + userId));
            List<RAJob> rajobs = new ArrayList<RAJob>();
//            if (rajobsNode.isNull() || rajobsNode.has("error") || !rajobsNode.isArray()) {
//
//                return ok(jobList.render(rajobs, (int) pageNum,
//                        0, rajobsNode.size(), 0, "search", 20,
//                        Long.parseLong(session("id")), 0, 0));
//            }
            int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
            int startIndex = ((int) pageNum - 1) * pageLimit;
            int endIndex = ((int) pageNum - 1) * pageLimit + pageLimit - 1;
            // Handle the last page, where there might be less than 50 item
            if (pageNum == (rajobsNode.size() - 1) / pageLimit + 1) {
                // minus 1 for endIndex for preventing ArrayOutOfBoundException
                endIndex = rajobsNode.size() - 1;
            }
            int count = endIndex - startIndex + 1;
            rajobs = RAJob.deserializeJsonToRAJobList(rajobsNode, startIndex, endIndex);
            int beginIndexPagination = beginIndexForPagination(pageLimit, rajobsNode.size(), (int) pageNum);
            int endIndexPagination = endIndexForPagination(pageLimit, rajobsNode.size(), (int) pageNum);
            return ok(rajobListPostedByUser.render(rajobs,
                    (int) pageNum,
                    startIndex,
                    rajobsNode.size(),
                    count,
                    pageLimit, Long.parseLong(session("id")), beginIndexPagination, endIndexPagination));
        } catch (Exception e) {
            Logger.debug("JobController.jobPostedByUser() exception: " + e.toString());
            e.printStackTrace();
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }

    /************************************************** End of My Posted RAJob ******************************************/

    /************************************************** My Applied RAJob *************************************************/
    /**
     * get all ra jobs applied by user
     *
     * @param pageNum
     * @return Job list applied by current user
     */
    @With(OperationLoggingAction.class)
    public Result rajobListAppliedByUser(Integer pageNum) {
        checkLoginStatus();
        String userId = session("id");
        try {
            JsonNode rajobsNode = RESTfulCalls.getAPI(
                    RESTfulCalls.getBackendAPIUrl(config, Constants.RAJOB_APPLIED_BY_USER + userId)
            );

            List<RAJob> rajobs = new ArrayList<>();
            List<String> applicationStatuses = new ArrayList<>();
            int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
            int startIndex = ((int) pageNum - 1) * pageLimit;
            int endIndex = ((int) pageNum - 1) * pageLimit + pageLimit - 1;
            if (pageNum == (rajobsNode.size() - 1) / pageLimit + 1) {
                // minus 1 for endIndex for preventing ArrayOutOfBoundException
                endIndex = rajobsNode.size() - 1;
            }
            int count = endIndex - startIndex + 1;

            rajobs = RAJob.deserializeJsonToRAJobList(rajobsNode, startIndex, endIndex);

            for (int i = startIndex; i <= endIndex && i < rajobsNode.size(); i++) {
                JsonNode rajobJson = rajobsNode.get(i);
                applicationStatuses.add(rajobJson.get("rajobApplicationStatus").asText());
            }

            int beginIndexPagination = beginIndexForPagination(pageLimit, rajobsNode.size(), (int) pageNum);
            int endIndexPagination = endIndexForPagination(pageLimit, rajobsNode.size(), (int) pageNum);

            return ok(rajobListAppliedByUser.render(
                    rajobs,
                    applicationStatuses,
                    (int) pageNum,
                    startIndex,
                    rajobsNode.size(),
                    count,
                    pageLimit,
                    Long.parseLong(userId),
                    beginIndexPagination,
                    endIndexPagination));
        } catch (Exception e) {
            Logger.debug("RAJobController.jobPostedByUser() exception: " + e.toString());
            e.printStackTrace();
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }


    /************************************************** End of My Applied RAJob ******************************************/

    /************************************************** RAJob Detail ***************************************************/
    /**
     * Ths method intends to return details of an RA job. If an RA job is not found, return to the all job page (page 1?).
     *
     * @param rajobId: RA job id
     * @return: RAJob, a list of RA jobs to jobDetail.scala.html
     */
    @With(OperationLoggingAction.class)
    public Result rajobDetail(Long rajobId) {
        try {
            // define usertype to show different button
            String userTypes = session("userTypes");
            if (userTypes == null) {
                return unauthorized("Unknown role or not authorized.");
            }
            //

            RAJob rajob = rajobService.getRAJobById(rajobId);
            if (rajob == null) {
                Logger.debug("RAJobController.rajobDetail() get null from backend");
                Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
                return ok(generalError.render());
                // return ok(rajobDetail.render(rajob,userTypes));
            }
            accessTimesService.AddOneTime("rajob", rajobId);
            Long userId = Long.parseLong(session("id"));
            String tableName = "rajob";
            String rajobFileType = "rajob";
            String tableRecorderId = rajobId.toString();
            String backendPort = Constants.CMU_BACKEND_PORT;
            Boolean rajobDocument = fileService.checkFile(tableName, rajobFileType, tableRecorderId);
            Logger.info("Result of checkFile: " + rajobDocument);
            // return ok(rajobDetail.render(rajob,userTypes));
            return ok(rajobDetail.render(
                    rajob,
                    userId,
                    userTypes,
                    tableName,
                    rajobFileType,
                    tableRecorderId,
                    backendPort,
                    rajobDocument));
        } catch (Exception e) {
            Logger.debug("RAJobController.rajobDetail() exception: " + e.toString());
            e.printStackTrace();
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }

    /**
     * Ths method intends to return details of an RA job application. If an RA job application is not found, return to the all job application page (page 1?).
     *
     * @param rajobApplicationId: RA job application id
     * @return: RAJobApplication, a list of RA jobs application to rajobApplicationDetail.scala.html
     */
    @With(OperationLoggingAction.class)
    public Result rajobApplicationDetail(Long rajobApplicationId) {
        try {
            String userTypes = session("userTypes");
            if (userTypes == null) {
                return unauthorized("Unknown role or not authorized.");
            }
            RAJobApplication rajobApplication = rajobApplicationService.getRAJobApplicationById(rajobApplicationId);

            if (rajobApplication == null) {
                Logger.debug("RAJobController.rajobApplicationDetail() get null from backend");
                Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
                return ok(generalError.render());
            }
            accessTimesService.AddOneTime("rajobApplication", rajobApplicationId);
            String tableName = "rajob_application";
            String resumeFileType = "resume";
            String coverFileType = "coverLetter";
            String transcriptFileType = "transcript";
            String degreeCertificateFileType = "degreeCertificate";
            String tableRecorderId = rajobApplicationId.toString();
            String backendPort = Constants.CMU_BACKEND_PORT;
            Boolean resume = fileService.checkFile(tableName, resumeFileType, tableRecorderId);
            Boolean coverLetter = fileService.checkFile(tableName, coverFileType, tableRecorderId);
            Boolean transcript = fileService.checkFile(tableName, transcriptFileType, tableRecorderId);
            Boolean degreeCertificate = fileService.checkFile(tableName, degreeCertificateFileType, tableRecorderId);

            // return ok(rajobApplicationDetail.render(rajobApplication,userTypes));
            return ok(rajobApplicationDetail.render(
                    rajobApplication,
                    userTypes,
                    tableName,
                    resumeFileType,
                    coverFileType,
                    transcriptFileType,
                    degreeCertificateFileType,
                    tableRecorderId,
                    backendPort,
                    resume,
                    coverLetter,
                    transcript,
                    degreeCertificate));

        } catch (Exception e) {
            Logger.debug("RAJobController.rajobApplicationDetail() exception: " + e.toString());
            e.printStackTrace();
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }
    /************************************************** End of RAJob Detail ********************************************/

    /************************************************** RA Job Apply **************************************************/
    /**
     * This method intends to prepare to edit a job.
     *
     * @param rajobId: job id
     * @return
     */
    @With(OperationLoggingAction.class)
    public Result rajobApplyPage(Long rajobId) {
        try {
            String userTypes = session("userTypes");
            if (userTypes == null) {
                return unauthorized("Unknown role or not authorized.");
            }
            RAJob rajob = rajobService.getRAJobById(rajobId);
            if (rajob == null) {
                Logger.debug("RAJobController.rajobApplyPage exception: cannot get rajob by id");
                Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
                return ok(generalError.render());
            }
            System.out.println("Apply page job info: " + rajob);
//            JsonNode teamMembersNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
//                    Constants.GET_TEAM_MEMBERS_BY_PROJECT_ID + jobId));
//            job.setTeamMembers(Common.deserializeJsonToList(teamMembersNode, User.class));

            return ok(rajobApplication.render(rajob, userTypes));

        } catch (Exception e) {
            Logger.debug("RAJobController.rajobApplyPage() exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }


    /**
     * This method intends to submit the edit in the job edit page.
     *
     * @param rajobId job id
     * @return
     */
    public Result rajobApplyPOST(Long rajobId) {
        checkLoginStatus();

        try {
            // 1) bind form and parse multipart body
            Form<RAJobApplication> rajobApplicationForm = rajobApplicationFormTemplate.bindFromRequest();
            Http.MultipartFormData body = request().body().asMultipartFormData();

            // 2) serialize everything except files into JSON
            ObjectNode jsonData = rajobApplicationService.serializeFormToJson(rajobApplicationForm, rajobId);

            // 3) create the RAJobApplication record first
            JsonNode response = RESTfulCalls.postAPI(
                    RESTfulCalls.getBackendAPIUrl(config, Constants.RAJOB_APPLY_POST + rajobId),
                    jsonData
            );
            if (response == null || response.has("error")) {
                Logger.debug("Cannot apply for the RA job");
                return redirect(routes.RAJobController.rajobDetail(rajobId));
            }
            long applicationId = response.asLong();

            // 4) now upload each file, if present
            if (body != null) {
                fileService.uploadFile(body, "resumePdf",           "rajob_application", "resume",           applicationId);
                fileService.uploadFile(body, "coverLetterPdf",     "rajob_application", "coverLetter",      applicationId);
                fileService.uploadFile(body, "transcriptFile",      "rajob_application", "transcript",       applicationId);
                fileService.uploadFile(body, "degreeCertificatePdf","rajob_application", "degreeCertificate",applicationId);
            }

            // 5) send notification email
            ObjectNode notifyData = Json.newObject().put("rajobId", rajobId);
            JsonNode notifyResponse = RESTfulCalls.postAPI(
                    RESTfulCalls.getBackendAPIUrl(config, "/rajob/sendAppliedEmail"),
                    notifyData
            );
            Logger.info("RAJobController.rajobApplyPOST: Email notify response = " + notifyResponse);

            // 6) render confirmation (pass applicationId if you want to show it)
            return ok(editConfirmation.render(rajobId, applicationId, "Rajob"));
        } catch (Exception e) {
            Logger.error("rajobApplyPOST failed", e);
            return ok(editError.render("Rajob"));
        }
    }

    /**
     * This method intends to submit the edit in the job edit page.
     *
     * @param rajobId      job id
     * @param rajobStatus: open, pending, close
     * @return
     */
    public Result rajobStatueChange(Long rajobId, String rajobStatus) {
        checkLoginStatus();
        try {
            System.out.println(rajobStatus);
            ObjectNode jsonData = JsonNodeFactory.instance.objectNode();
            jsonData.put("status", rajobStatus);
            System.out.println("Job id:" + rajobId + " jsonData: " + jsonData.toString());
            JsonNode response = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.RAJOB_STATUS_UPDATE + rajobId), jsonData);

            if (response == null || response.has("error")) {
                Logger.debug("Cannot change status of this ra job");
                return redirect(routes.RAJobController.rajobList(1, ""));
            }

            return ok(editConfirmation.render(rajobId, Long.parseLong("0"), "RajobOffer"));//TODO: 0 is entryId
        } catch (Exception e) {
            Logger.debug("rajobController ra job status update exception: " + e.toString());
            return ok(editError.render("RAJob"));
        }
    }

    /**
     * This method intends to offer the APPLICATION to a student in the rajobApplicationDetails page.
     *
     * @param rajobApplicationId      job id
     * @param rajobApplicationStatus: open, pending, close
     * @return
     */
    public Result rajobApplicationStatusChange(Long rajobApplicationId, String rajobApplicationStatus) {
        checkLoginStatus();
        String userId = session("id");
        String sessionEmail = session("email");
        Logger.debug("▶ Entering rajobApplicationStatusChange");
        Logger.debug("User ID from session: " + userId);
        Logger.debug("RAJobApplication ID: " + rajobApplicationId + " | New Status: " + rajobApplicationStatus);
        Logger.debug("Session email: " + sessionEmail);

        try {
            ObjectNode jsonData = JsonNodeFactory.instance.objectNode();
            jsonData.put("status", rajobApplicationStatus);

            String statusUpdateUrl = RESTfulCalls.getBackendAPIUrl(config, Constants.RAJOB_APPLICATION_STATUS_UPDATE + rajobApplicationId);
            Logger.debug("Sending status update to: " + statusUpdateUrl);
            Logger.debug("Payload: " + jsonData.toString());

            JsonNode response = RESTfulCalls.postAPI(statusUpdateUrl, jsonData);
            Logger.debug("Status update response: " + (response == null ? "null" : response.toString()));

            if (response == null || response.has("error")) {
                Logger.warn("❌ Failed to update RA job application status. Redirecting to posted job list.");
                return redirect(routes.RAJobController.rajobListPostedByUser(1));
            }

            Map<String, String[]> formUrlEncoded = request().body().asFormUrlEncoded();
            Logger.debug("Parsed formUrlEncoded: " + formUrlEncoded);

            String[] ccArr = formUrlEncoded.get("ccSelected");
            Logger.debug("Raw ccSelected from form: " + Arrays.toString(ccArr));

            String ccString = "";
            if (ccArr != null && ccArr.length > 0 && ccArr[0] != null && !ccArr[0].isEmpty()) {
                if (sessionEmail != null && !sessionEmail.isEmpty()) {
                    ccString = ccArr[0] + "," + sessionEmail;
                } else {
                    ccString = ccArr[0];
                }
            } else {
                ccString = sessionEmail != null ? sessionEmail : "";
            }
            Logger.debug("Computed ccString to pass: " + ccString);

            Logger.debug("▶ Calling sendOfferEmail...");
            Result emailResult = sendOfferEmail(rajobApplicationId, ccString);
            Logger.debug("sendOfferEmail result: " + emailResult.toString());

            return emailResult;

        } catch (Exception e) {
            Logger.error("❌ Exception caught in rajobApplicationStatusChange", e);
            return ok(editError.render("RAJobapplication"));
        }
    }

    /************************************************** End of RA Job Apply *******************************************/

    /**
     * This method intends to render an empty search.scala.html page
     *
     * @return
     */
    public Result searchPage() {
        checkLoginStatus();

        return ok(rajobSearch.render());
    }

    /**
     * This method intends to prepare data for rending RA job research result page
     *
     * @param pageNum
     * @return: data prepared for rajobList.scala.html (same as show all job list page)
     */
    public Result searchPOST(Integer pageNum, String sortCriteria) {
        checkLoginStatus();

        try {
            Form<RAJob> tmpForm = rajobFormTemplate.bindFromRequest();
            Map<String, String> tmpMap = tmpForm.data();

            JsonNode searchJson = Json.toJson(tmpMap);
            String searchString = "";

            // if not coming from the search input page, then fetch searchJson from the form from key "searchString"
            if (tmpMap.get("searchString") != null) {
                searchString = tmpMap.get("searchString");
                searchJson = Json.parse(searchString);
            } else {
                searchString = Json.stringify(searchJson);
            }

            List<RAJob> rajobs = new ArrayList<RAJob>();
            JsonNode rajobsNode = null;

            rajobsNode = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.GET_RAJOBS_BY_CONDITION), searchJson);
            if (rajobsNode.isNull() || rajobsNode.has("error") || !rajobsNode.isArray()) {

                return ok(rajobList.render(rajobs, (int) pageNum, sortCriteria,
                        0, rajobsNode.size(), 0, "search", 20, searchString,
                        Long.parseLong(session("id")), 0, 0));
            }
            int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
            int startIndex = ((int) pageNum - 1) * pageLimit;
            int endIndex = ((int) pageNum - 1) * pageLimit + pageLimit - 1;
            // Handle the last page, where there might be less than 50 item
            if (pageNum == (rajobsNode.size() - 1) / pageLimit + 1) {
                // minus 1 for endIndex for preventing ArrayOutOfBoundException
                endIndex = rajobsNode.size() - 1;
            }
            int count = endIndex - startIndex + 1;

            rajobs = RAJob.deserializeJsonToRAJobList(rajobsNode, startIndex, endIndex);
            int beginIndexPagination = beginIndexForPagination(pageLimit, rajobsNode.size(), (int) pageNum);
            int endIndexPagination = endIndexForPagination(pageLimit, rajobsNode.size(), (int) pageNum);

            return ok(rajobList.render(rajobs,
                    (int) pageNum,
                    sortCriteria,
                    startIndex,
                    rajobsNode.size(),
                    count,
                    "search",
                    pageLimit,
                    searchString,
                    Long.parseLong(session("id")),
                    beginIndexPagination,
                    endIndexPagination));
        } catch (Exception e) {
            Logger.debug("RAJobController.searchPOST() exception: " + e.toString());
            return redirect(routes.Application.home());
        }
    }


    private boolean matchesPayRangeFilters(JsonNode job, String department, String mode, String status) {
        if (job == null || job.isMissingNode()) {
            return false;
        }

        String organization = job.path("organization").asText("");
        String location = job.path("location").asText("");
        String jobStatus = job.path("status").asText("");

        if (!isBlank(department) && !organization.equalsIgnoreCase(department.trim())) {
            return false;
        }

        if (!isBlank(mode) && !location.equalsIgnoreCase(mode.trim())) {
            return false;
        }

        if (!isBlank(status) && !jobStatus.equalsIgnoreCase(status.trim())) {
            return false;
        }

        return true;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private ArrayNode toArrayNode(Set<String> values) {
        ArrayNode arrayNode = Json.newArray();
        if (values == null || values.isEmpty()) {
            return arrayNode;
        }
        values.stream().sorted().forEach(arrayNode::add);
        return arrayNode;
    }

    private ObjectNode emptyPayRangeResponse() {
        ObjectNode response = Json.newObject();
        response.set("counts", Json.newObject());
        response.set("filters", Json.newObject());
        response.set("jobs", Json.newArray());
        return response;
    }


/*************************************** Private Methods **************************************************************/

    /**
     * This method intends to inactivate the RA job by calling the backend
     *
     * @param rajobId
     * @return redirect to the job list page
     */
    public Result closeRAJob(long rajobId) {
        checkLoginStatus();
        try {
            String userTypes = session("userTypes");
            if (userTypes == null) {
                return unauthorized("Unknown role or not authorized.");
            }
            JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.CLOSE_RAJOB_BY_ID + rajobId));
            // when the job is closed, redirect to the job list page
            return redirect(routes.RAJobController.rajobList(1, ""));
        } catch (Exception e) {
            Logger.debug("RAJobController RA job close exception: " + e.toString());
            return redirect(routes.RAJobController.rajobList(1, ""));
        }
    }

    public Result deleteRAJob(long rajobId) {
        checkLoginStatus();


        try {
            JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.DELETE_RAJOB_BY_ID + rajobId));
            //Todo We have to decide what to do if for some reason the RA job could not get deactivated???
            return redirect(routes.RAJobController.rajobList(1, ""));
        } catch (Exception e) {
            Logger.debug("RAJobController RA job delete exception: " + e.toString());
            return redirect(routes.RAJobController.rajobList(1, ""));
        }
    }

    public Result isRAJobNameExisted() {
        JsonNode json = request().body().asJson();
        String title = json.path("title").asText();

        ObjectNode jsonData = Json.newObject();
        JsonNode response = null;

        try {
            jsonData.put("title", title);
            response = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.CHECK_RAJOB_NAME), jsonData);
            Application.flashMsg(response);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            Application.flashMsg(RESTfulCalls
                    .createResponse(RESTfulCalls.ResponseType.CONVERSIONERROR));
        } catch (Exception e) {
            e.printStackTrace();
            Application.flashMsg(RESTfulCalls.createResponse(RESTfulCalls.ResponseType.UNKNOWN));
        }
        return ok(response);
    }

    /*********************************** END Basic refactoring ********************************************************/

//    /**
//     *
//     * @return
//     */
//    public Result getJobLists() {
//        checkLoginStatus();
//        ArrayNode jobList = Json.newArray();
//        JsonNode jobsNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
//                Constants.GET_ALL_ACTIVE_JOB));
//        // if no value is returned or error or is not json array
//
//        ObjectMapper mapper = new ObjectMapper();
//        // parse the json string into object
//        for (int i = 0; i < jobsNode.size(); i++) {
//            JsonNode json = jobsNode.path(i);
//            ObjectNode jsonData = mapper.createObjectNode();
//            jsonData.put("id", json.findPath("id").asLong());
//            jsonData.put("text", json.findPath("title").asText());
//            jobList.add(jsonData);
//        }
//
//        return ok(jobList);
//    }

    /**
     * @param id
     * @return
     */
    public Result isRAJobExist(String id) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        if (pattern.matcher(id).matches()) {
            JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.GET_RAJOB_BY_ID + Long.parseLong(id)));
            if (response == null || response.has("error")) {
                return badRequest("cannot find RA job");
            } else return ok(response.findPath("id"));
        } else return badRequest("cannot find RA job");
    }

    public Result sendOfferEmail(Long rajobApplicationId, String ccString) {
        checkLoginStatus();
        try {
            Logger.debug("sendOfferEmail(...) invoked. rajobApplicationId = " + rajobApplicationId
                    + ", ccString = " + ccString);

            ObjectNode offerData = Json.newObject();
            offerData.put("rajobApplicationId", rajobApplicationId);
            offerData.put("ccSelected", ccString);

            JsonNode offerResp = RESTfulCalls.postAPI(
                    RESTfulCalls.getBackendAPIUrl(config, "/rajob/offer"),
                    offerData
            );
            Logger.debug("sendOfferEmail response = " + offerResp);

            return ok(editConfirmation.render(rajobApplicationId, 0L, "SendOffer"));

        } catch (Exception e) {
            Logger.error("sendOfferEmail failed: ", e);
            return ok(editError.render("RAJobapplication"));
        }
    }
}