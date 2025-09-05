package controllers;

import actions.OperationLoggingAction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import models.ChallengeApplication;
import models.Job;
import models.JobApplication;
import models.Job;
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
import java.util.regex.Pattern;

import static controllers.Application.checkLoginStatus;
import static utils.Common.beginIndexForPagination;
import static utils.Common.endIndexForPagination;

public class JobController extends Controller {

    @Inject
    Config config;

    private final JobService jobService;
    private final JobApplicationService jobApplicationService;

    private final UserService userService;
    private final AccessTimesService accessTimesService;

    private Form<Job> jobFormTemplate;
    private Form<JobApplication> jobApplicationFormTemplate;
    private FormFactory myFactory;
    private final FileService fileService;


    /******************************* Constructor **********************************************************************/
    @Inject
    public JobController(FormFactory factory,
                         JobService jobService,
                         UserService userService,
                         JobApplicationService jobApplicationService,
                         AccessTimesService accessTimesService,
                         FileService fileService) {
        jobFormTemplate = factory.form(Job.class);
        myFactory = factory;
        jobApplicationFormTemplate = factory.form(JobApplication.class);
        this.jobApplicationService = jobApplicationService;
        this.jobService = jobService;
        this.userService = userService;
        this.accessTimesService = accessTimesService;
        this.fileService = fileService;
    }


    /************************************************** Job Registration **********************************************/

    /**
     * This method intends to render the job registration page.
     *
     * @return
     */
    @With(OperationLoggingAction.class)
    public Result jobRegisterPage() {
        checkLoginStatus();
        return ok(jobRegister.render());
    }

    /**
     * This method intends to gather job registration information and create a job in database.
     *
     * @return
     */
    public Result jobRegisterPOST() {
        checkLoginStatus();
        try {
            Form<Job> jobForm = jobFormTemplate.bindFromRequest();
            Http.MultipartFormData body = request().body().asMultipartFormData();

            JsonNode jsonNode = jobService.serializeFormToJson(jobForm);
            ObjectNode jsonData = (ObjectNode) jsonNode;

            JsonNode response = RESTfulCalls.postAPI(
                    RESTfulCalls.getBackendAPIUrl(config, Constants.JOB_REGISTER_POST),
                    jsonData
            );
            if (response == null || response.has("error")) {
                Logger.debug("JobController.jobRegisterPOST: Cannot create the job in backend");
                return ok(registrationError.render("Job"));
            }
            long jobId = response.asLong();

            if (body != null) {
                fileService.uploadFile(body, "jobPdf", "job", "job", jobId);
            }

            return ok(registerConfirmation.render(jobId, "Job"));
        } catch (Exception e) {
            Logger.debug("JobController jobRegisterPOST exception: " + e.toString());
            return ok(registrationError.render("Job"));
        }
    }

    /************************************************** End of Job Registration ****************************************/


    /************************************************** Job Edit *******************************************************/
    /**
     * This method intends to prepare to edit a job.
     *
     * @param jobId: job id
     * @return
     */
    @With(OperationLoggingAction.class)
    public Result jobEditPage(Long jobId) {
        try {
            // define usertype to show different button
            String userTypes = session("userTypes");
            if (userTypes == null) {
                return unauthorized("Unknown role or not authorized.");
            }
            //
            Job job = jobService.getJobById(jobId);
            if (job == null) {
                Logger.debug("Job.jobEditPage exception: cannot get job by id");
                Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
                return ok(generalError.render());
            }
            accessTimesService.AddOneTime("job", jobId);
            Long userId = Long.parseLong(session("id"));
            String tableName = "job";
            String jobFileType = "job";
            String tableRecorderId = jobId.toString();
            String backendPort = Constants.CMU_BACKEND_PORT;
            Boolean jobDocument = fileService.checkFile(tableName, jobFileType, tableRecorderId);

            return ok(jobEdit.render(job,userTypes,
                    tableName,
                    jobFileType,
                    tableRecorderId,
                    backendPort,
                    jobDocument));
        } catch (Exception e) {
            Logger.debug("JobController.jobEditPage() exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }

    /**
     * This method intends to submit the edit in the job edit page.
     *
     * @param jobId job id
     * @return
     */
    public Result jobEditPOST(Long jobId) {
        checkLoginStatus();

        try {
            Form<Job> jobForm = jobFormTemplate.bindFromRequest();
            Http.MultipartFormData body = request().body().asMultipartFormData();

            JsonNode jsonNode = jobService.serializeFormToJson(jobForm);
            ObjectNode jsonData = (ObjectNode) jsonNode;

            JsonNode response = RESTfulCalls.postAPI(
                    RESTfulCalls.getBackendAPIUrl(config, Constants.JOB_EDIT_POST + jobId),
                    jsonData
            );
            if (response == null || response.has("error")) {
                Logger.debug("JobController.jobEditPOST: Cannot update the job");
                return redirect(routes.JobController.jobEditPage(jobId));
            }

            if (body != null) {
                fileService.uploadFile(body,
                        "jobPdf",
                        "job",
                        "job",
                        jobId);
            }

            return ok(editConfirmation.render(jobId, 0L, "Job"));
        } catch (Exception e) {
            Logger.debug("JobController.jobEditPOST exception: " + e.toString());
            return ok(editError.render("Job"));
        }
    }
    /************************************************** End of Job Edit ************************************************/

    /************************************************** Job List *******************************************************/

    /**
     * This method intends to prepare data for all jobs.
     *
     * @param pageNum
     * @param sortCriteria: sortCriteria on some fields. Could be empty if not specified at the first time.
     * @return: data for jobList.scala.html
     */
    @With(OperationLoggingAction.class)
    public Result jobList(Integer pageNum, String sortCriteria) {
        checkLoginStatus();

        // If session contains the current projectId, set it.

        // Set the offset and pageLimit.
        int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
        try {
            JsonNode technologyListJsonNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.JOB_LIST + session("id") + "?pageNum=" +
                            pageNum + "&pageLimit=" + pageLimit + "&sortCriteria=" + sortCriteria));
            return jobService.renderJobListPage(technologyListJsonNode,
                    pageLimit, null, "all", session("username"),
                    Long.parseLong(session("id")));
        } catch (Exception e) {
            Logger.debug("JobController.jobList() exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }

    @With(OperationLoggingAction.class)
    public Result jobApplicationList(Integer pageNum, String sortCriteria) {
        checkLoginStatus();

        // If session contains the current projectId, set it.

        // Set the offset and pageLimit.
        int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
        try {
            JsonNode technologyListJsonNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.JOB_LIST + session("id") + "?pageNum=" +
                            pageNum + "&pageLimit=" + pageLimit + "&sortCriteria=" + sortCriteria));
            return jobService.renderJobApplicationListPage(technologyListJsonNode,
                    pageLimit, null, "all", session("username"),
                    Long.parseLong(session("id")));
        } catch (Exception e) {
            Logger.debug("JobController.jobList() exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }
    /************************************************** End of Job List ************************************************/

    /************************************************** Job Detail *****************************************************/
    /**
     * Ths method intends to return details of a job. If a job is not found, return to the all job page (page 1?).
     *
     * @param jobId: technology id
     * @return: Technology, a list of jobs to jobDetail.scala.html
     */
    @With(OperationLoggingAction.class)
    public Result jobDetail(Long jobId) {
        try {
            // define usertype to show different button
            String userTypes = session("userTypes");
            if (userTypes == null) {
                return unauthorized("Unknown role or not authorized.");
            }
            //

            Job job = jobService.getJobById(jobId);
            if (job == null) {
                Logger.debug("JobController.jobDetail() get null from backend");
                Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
                return ok(generalError.render());
            }
            accessTimesService.AddOneTime("job", jobId);
            Long userId = Long.parseLong(session("id"));
            String tableName = "job";
            // String jobDescriptionFileType = "jobDescription";
            String jobFileType = "job";
            String tableRecorderId = jobId.toString();
            String backendPort = Constants.CMU_BACKEND_PORT;
            Boolean jobDocument = fileService.checkFile(tableName, jobFileType, tableRecorderId);
//            return ok(jobDetail.render(job));
//            return ok(jobDetail.render(job, userTypes));
            return ok(jobDetail.render(
                    job,
                    userId,
                    userTypes,
                    tableName,
                    jobFileType,
                    tableRecorderId,
                    backendPort,
                    jobDocument));

        } catch (Exception e) {
            Logger.debug("JobController.jobDetail() exception: " + e.toString());
            e.printStackTrace();
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }

    @With(OperationLoggingAction.class)
    public Result jobApplicationDetail(Long jobApplicationId) {
        String userId = session("id");
        try {
            JobApplication jobApplication = jobApplicationService.getJobApplicationById(jobApplicationId);


            if (jobApplication == null) {
                Logger.debug("JobController.jobApplicationDetail() get null from backend");
                Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
                return ok(generalError.render());
            }
            accessTimesService.AddOneTime("jobApplication", jobApplicationId);
            String tableName = "job_application";
            String resumeFileType = "resume";
            String coverFileType = "coverLetter";
            String transcriptFileType = "transcript";
            String degreeCertificateFileType = "degreeCertificate";
            String tableRecorderId = jobApplicationId.toString();
            String backendPort = Constants.CMU_BACKEND_PORT;
            Boolean resume = fileService.checkFile(tableName, resumeFileType, tableRecorderId);
            Boolean coverLetter = fileService.checkFile(tableName, coverFileType, tableRecorderId);
            Boolean transcript = fileService.checkFile(tableName, transcriptFileType, tableRecorderId);
            Boolean degreeCertificate = fileService.checkFile(tableName, degreeCertificateFileType, tableRecorderId);

            // return ok(jobApplicationDetail.render(jobApplication,userId));
            return ok(jobApplicationDetail.render(
                    jobApplication,
                    userId,
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
            Logger.debug("JobController.jobApplicationDetail() exception: " + e.toString());
            e.printStackTrace();
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }

    /************************************************** End of Job Detail **********************************************/

    /************************************************** My Posted job *************************************************/
    /**
     * get all jobs posted by user
     * @param pageNum
     * @return Job list posted by current user
     */
    @With(OperationLoggingAction.class)
    public Result jobListPostedByUser(Integer pageNum){
        checkLoginStatus();
        String userId = session("id");
        try{
            JsonNode jobsNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.JOB_POSTED_BY_USER + userId));
            List<Job> jobs = new ArrayList<Job>();
//            if (jobsNode.isNull() || jobsNode.has("error") || !jobsNode.isArray()) {
//
//                return ok(jobList.render(jobs, (int) pageNum,
//                        0, jobsNode.size(), 0, "search", 20,
//                        Long.parseLong(session("id")), 0, 0));
//            }
            int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
            int startIndex = ((int) pageNum - 1) * pageLimit;
            int endIndex = ((int) pageNum - 1) * pageLimit + pageLimit - 1;
            // Handle the last page, where there might be less than 50 item
            if (pageNum == (jobsNode.size() - 1) / pageLimit + 1) {
                // minus 1 for endIndex for preventing ArrayOutOfBoundException
                endIndex = jobsNode.size() - 1;
            }
            int count = endIndex - startIndex + 1;
            jobs = Job.deserializeJsonToJobList(jobsNode, startIndex, endIndex);
            int beginIndexPagination = beginIndexForPagination(pageLimit, jobsNode.size(), (int) pageNum);
            int endIndexPagination = endIndexForPagination(pageLimit, jobsNode.size(), (int) pageNum);
            return ok(jobListPostedByUser.render(jobs,
                    (int) pageNum,
                    startIndex,
                    jobsNode.size(),
                    count,
                    pageLimit, Long.parseLong(session("id")), beginIndexPagination, endIndexPagination));
        }catch(Exception e){
            Logger.debug("JobController.jobPostedByUser() exception: " + e.toString());
            e.printStackTrace();
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }

    /************************************************** End of My Posted Job ******************************************/

    /************************************************** My Applied Job ******************************************/

    @With(OperationLoggingAction.class)
    public Result jobListAppliedByUser(Integer pageNum){
        checkLoginStatus();
        String userId = session("id");
        try{
            JsonNode jobsNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.JOB_APPLIED_BY_USER + userId));
            List<Job> jobs = new ArrayList<Job>();
            int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
            int startIndex = ((int) pageNum - 1) * pageLimit;
            int endIndex = ((int) pageNum - 1) * pageLimit + pageLimit - 1;
            // Handle the last page, where there might be less than 50 item
            if (pageNum == (jobsNode.size() - 1) / pageLimit + 1) {
                // minus 1 for endIndex for preventing ArrayOutOfBoundException
                endIndex = jobsNode.size() - 1;
            }
            int count = endIndex - startIndex + 1;
            jobs = Job.deserializeJsonToJobList(jobsNode, startIndex, endIndex);
            int beginIndexPagination = beginIndexForPagination(pageLimit, jobsNode.size(), (int) pageNum);
            int endIndexPagination = endIndexForPagination(pageLimit, jobsNode.size(), (int) pageNum);
            return ok(jobListAppliedByUser.render(
                    jobs,
                    (int) pageNum,
                    startIndex,
                    jobsNode.size(),
                    count,
                    pageLimit, Long.parseLong(session("id")), beginIndexPagination, endIndexPagination));
        }catch(Exception e){
            Logger.debug("JobController.jobPostedByUser() exception: " + e.toString());
            e.printStackTrace();
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }

    /************************************************** End of My Applied Job ******************************************/

    /************************************************** Job Apply **************************************************/
    /**
     * This method intends to prepare to edit a job.
     *
     * @param jobId: job id
     * @return
     */
    @With(OperationLoggingAction.class)
    public Result jobApplyPage(Long jobId) {
        try {
            String userTypes = session("userTypes");
            if (userTypes == null) {
                return unauthorized("Unknown role or not authorized.");
            }
            Job job = jobService.getJobById(jobId);
            if (job == null) {
                Logger.debug("jobController.jobApplyPage exception: cannot get job by id");
                Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
                return ok(generalError.render());
            }
            System.out.println("Apply page job info: "+ job);
//            JsonNode teamMembersNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
//                    Constants.GET_TEAM_MEMBERS_BY_PROJECT_ID + jobId));
//            job.setTeamMembers(Common.deserializeJsonToList(teamMembersNode, User.class));

            return ok(jobApplication.render(job,userTypes));
        } catch (Exception e) {
            Logger.debug("jobController.jobApplyPage() exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }

    /**
     * This method intends to submit the edit in the job edit page.
     *
     * @param jobId job id
     * @return
     */
    public Result jobApplyPOST(Long jobId) {
        checkLoginStatus();

        try {
            // bind form & parse multipart
            Form<JobApplication> jobApplicationForm = jobApplicationFormTemplate.bindFromRequest();
            Http.MultipartFormData body = request().body().asMultipartFormData();

            // serialize into JSON (excluding files)
            ObjectNode jsonData = jobApplicationService.serializeFormToJson(jobApplicationForm, jobId);

            // create the application record
            JsonNode response = RESTfulCalls.postAPI(
                    RESTfulCalls.getBackendAPIUrl(config, Constants.JOB_APPLY_POST + jobId),
                    jsonData
            );
            if (response == null || response.has("error")) {
                Logger.debug("JobController.jobApplyPOST: cannot create application");
                return redirect(routes.JobController.jobDetail(jobId));
            }
            long applicationId = response.asLong();

            if (body != null) {
                fileService.uploadFile(body, "resumePdf", "job_application", "resume", applicationId);
                fileService.uploadFile(body, "coverLetterPdf",       "job_application", "coverLetter", applicationId);
                fileService.uploadFile(body, "transcriptFile",       "job_application", "transcript", applicationId);
                fileService.uploadFile(body, "degreeCertificatePdf", "job_application", "degreeCertificate", applicationId);
            }

            return ok(editConfirmation.render(jobId, 0L, "Job"));
        } catch (Exception e) {
            Logger.debug("JobController.jobApplyPOST exception: " + e.toString());
            return ok(editError.render("Job"));
        }
    }

    /**
     * This method intends to submit the edit in the job edit page.
     *
     * @param jobId job id
     * @param jobStatus: open, pending, close
     * @return
     */
    public Result jobStatueChange(Long jobId, String jobStatus){
        checkLoginStatus();
        try {
            System.out.println(jobStatus);
            ObjectNode jsonData = JsonNodeFactory.instance.objectNode();
            jsonData.put("status", jobStatus);
            System.out.println("Job id:"+ jobId + " jsonData: " + jsonData.toString());
            JsonNode response = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.JOB_STATUS_UPDATE + jobId), jsonData);

            if (response == null || response.has("error")) {
                Logger.debug("Cannot change status of this job");
                return redirect(routes.JobController.jobApplicationList(1, ""));
            }

            return ok(editConfirmation.render(jobId, Long.parseLong("0"), "JobOffer"));//TODO: 0 is entryId
        } catch (Exception e) {
            Logger.debug("jobController job status update exception: " + e.toString());
            return ok(editError.render("Job"));
        }
    }

    /************************************************** End of Job Apply *******************************************/

    /**
     * This method intends to render an empty search.scala.html page
     *
     * @return
     */
    public Result searchPage() {
        checkLoginStatus();
        return ok(jobSearch.render());
    }

    /**
     * This method intends to prepare data for rending job research result page
     *
     * @param pageNum
     * @return: data prepared for jobList.scala.html (same as show all job list page)
     */
    public Result searchPOST(Integer pageNum, String sortCriteria) {
        checkLoginStatus();

        try {
            Form<Job> tmpForm = jobFormTemplate.bindFromRequest();
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

            List<Job> jobs = new ArrayList<Job>();
            JsonNode jobsNode = null;

            jobsNode = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.GET_JOBS_BY_CONDITION), searchJson);
            if (jobsNode.isNull() || jobsNode.has("error") || !jobsNode.isArray()) {

                return ok(jobList.render(jobs, (int) pageNum, sortCriteria,
                        0, jobsNode.size(), 0, "search", 20, searchString,
                        Long.parseLong(session("id")), 0, 0));
            }
            int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
            int startIndex = ((int) pageNum - 1) * pageLimit;
            int endIndex = ((int) pageNum - 1) * pageLimit + pageLimit - 1;
            // Handle the last page, where there might be less than 50 item
            if (pageNum == (jobsNode.size() - 1) / pageLimit + 1) {
                // minus 1 for endIndex for preventing ArrayOutOfBoundException
                endIndex = jobsNode.size() - 1;
            }
            int count = endIndex - startIndex + 1;

            jobs = Job.deserializeJsonToJobList(jobsNode, startIndex, endIndex);
            int beginIndexPagination = beginIndexForPagination(pageLimit, jobsNode.size(), (int) pageNum);
            int endIndexPagination = endIndexForPagination(pageLimit, jobsNode.size(), (int) pageNum);

            return ok(jobList.render(jobs,
                    (int) pageNum,
                    sortCriteria,
                    startIndex,
                    jobsNode.size(),
                    count,
                    "search",
                    pageLimit,
                    searchString,
                    Long.parseLong(session("id")),
                    beginIndexPagination,
                    endIndexPagination));
        } catch (Exception e) {
            Logger.debug("JobController.searchPOST() exception: " + e.toString());
            return redirect(routes.Application.home());
        }
    }


/*************************************** Private Methods **************************************************************/

    /**
     * This method intends to inactivate the job by calling the backend
     *
     * @param jobId
     * @return redirect to the job list page
     */
    public Result deleteJob(long jobId) {
        checkLoginStatus();


        try {
            JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.DELETE_JOB_BY_ID + jobId));
            //Todo We have to decide what to do if for some reason the job could not get deactivated???
            return redirect(routes.JobController.jobList(1, ""));
        } catch (Exception e) {
            Logger.debug("JobController job delete exception: " + e.toString());
            return redirect(routes.JobController.jobList(1, ""));
        }
    }

    public Result isJobNameExisted() {
        JsonNode json = request().body().asJson();
        String title = json.path("title").asText();

        ObjectNode jsonData = Json.newObject();
        JsonNode response = null;

        try {
            jsonData.put("title", title);
            response = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.CHECK_JOB_NAME), jsonData);
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
    public Result isJobExist(String id) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        if (pattern.matcher(id).matches()) {
            JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.GET_JOB_BY_ID + Long.parseLong(id)));
            if (response == null || response.has("error")) {
                return badRequest("cannot find job");
            } else return ok(response.findPath("id"));
        } else return badRequest("cannot find job");
    }


    /**
     * @param id
     * @param eventListString
     * @return
     */
    public Result followedByUser(Long id, String eventListString) {
        checkLoginStatus();
        String projectId = String.valueOf(id);
        List<String> eventTypes = new ArrayList<>();
        String[] eventList = eventListString.split(";");

        for (String event : eventList) {
            eventTypes.add(event);
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < eventTypes.size() - 1; i++) {
            sb.append(eventTypes.get(i)).append(";");
        }

        if (!eventTypes.isEmpty()) {
            sb.append(eventTypes.get(eventTypes.size() - 1));
        }

        String url = RESTfulCalls.getBackendAPIUrl(config, Constants.PROJECT_FOLLOWED_BY_USER
                + "/" + projectId + "/" + session("id") + "/" + sb.toString());
        JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.PROJECT_FOLLOWED_BY_USER
                + "/" + projectId + "/" + session("id") + "/" + sb.toString()));
        if (response.has("error")) {
            Application.flashMsg(RESTfulCalls.createResponse(RESTfulCalls.ResponseType.UNKNOWN));
            return jobList(1, "");
        }


        return jobList(1, "");
    }

    /**
     * @param id
     * @param eventListString
     * @return
     */
    public Result unFollowedByUser(Long id, String eventListString) {
        checkLoginStatus();

        String projectId = String.valueOf(id);
        List<String> eventTypes = new ArrayList<>();
        String[] eventList = eventListString.split(";");

        for (String event : eventList) {
            eventTypes.add(event);
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < eventTypes.size() - 1; i++) {
            sb.append(eventTypes.get(i)).append(";");
        }

        if (!eventTypes.isEmpty()) {
            sb.append(eventTypes.get(eventTypes.size() - 1));
        }

        String url = RESTfulCalls.getBackendAPIUrl(config, Constants.PROJECT_UNFOLLOWED_BY_USER
                + "/" + projectId + "/" + session("id") + "/" + sb.toString());
        JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                Constants.PROJECT_UNFOLLOWED_BY_USER
                        + "/" + projectId + "/" + session("id") + "/" + sb.toString()));

        if (response.has("error")) {
            Application.flashMsg(RESTfulCalls.createResponse(RESTfulCalls.ResponseType.UNKNOWN));
            return jobList(1, "");
        }

        return jobList(1, "");
    }

    /**
     * This method intends to prepare data to render the page of listing my followed projects with pagination
     *
     * @param page:         current page number
     * @param sortCriteria: sort criteria
     * @return: data for projectList.scala.html
     */
//    public Result myFollowedProjects(Integer page, String sortCriteria) {
//        Job currentProjectZone = jobService.getCurrentProjectZone();
//        try {
//            int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
//            int offset = pageLimit * (page - 1);
//            JsonNode projectsJsonNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
//                    Constants.MY_FOLLOWED_PROJECTS + "?offset=" + offset + "&pageLimit=" +
//                            pageLimit + "&sortCriteria=" + sortCriteria + "&uid=" + session("id")));
//            return jobService.renderProjectListPage(projectsJsonNode, currentProjectZone, pageLimit,
//                    null, "my follow", session("username"), Long.parseLong(session("id")));
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return redirect(routes.Application.home());
//        }
//    }


    /**
     * This method intends to render addProjectFollowersPage
     *
     * @param id job id
     * @return: addProjectFollowersPage
     */
//    public Result addProjectFollowersPage(Long id) {
//        try {
//            Job job = jobService.getProjectById(id);
//            JsonNode userNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(
//                    config, Constants.GET_USER_PROFILE_BY_ID + job.getCreator()));
//            User creator = User.deserialize(userNode);
//            JsonNode followersNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(
//                    config, Constants.GET_FOLLOWERS_FOR_PROJECT + id));
//            List<User> followers = new ArrayList<>();
//            for (JsonNode follower : followersNode) {
//                followers.add(User.deserialize(follower));
//            }
//            return ok(addProjectFollowers.render(job, followers, creator));
//        } catch (Exception e) {
//            e.printStackTrace();
//            return redirect(routes.ProjectController.projectDetail(id));
//        }
//    }

    /**
     * This method intends to add one follower to a private job
     *
     * @return: addProjectFollowersPage
     */
    public Result addOneFollower(Long id, String event) {
        try {
            DynamicForm df = myFactory.form().bindFromRequest();
            Http.MultipartFormData body = request().body().asMultipartFormData();
            if (!Common.validate(df.field("email").value())) {
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode jsonData = mapper.createObjectNode();
                jsonData.put("error", "Invalid email format");

                return ok(jsonData);
            }

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode jsonData = mapper.createObjectNode();
            jsonData.put("firstName", df.field("firstName").value());
            jsonData.put("lastName", df.field("lastName").value());
            jsonData.put("email", df.field("email").value());

            JsonNode response = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.ADD_ONE_FOLLOWER_PROJECT + id + "/" + event), jsonData);

            return ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return redirect(routes.ProjectController.projectDetail(id));
        }
    }

    /**
     * This method intends to delete one follower of a private job
     *
     * @return: deleteProjectFollowersPage
     */
    public Result deleteOneFollower(Long followerId, Long projectId, String eventType) {
        try {

            JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.DELETE_ONE_FOLLOWER_PROJECT + followerId + "/" + projectId + "/" + eventType));
            return ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return redirect(routes.ProjectController.projectDetail(projectId));
        }
    }

    public Result jobApplicationsList(String jobType, Long jobId, Integer pageNum, String sortCriteria) {
        checkLoginStatus();

        int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
        int offset = pageLimit * (pageNum - 1);

        if (null == jobType) jobType = "general";
        jobType = jobType.toLowerCase();
        JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                Constants.STR_BACKEND_URL_JOB_APPLICATIONS + jobType + "/" + jobId + "?offset=" + offset + "&pageLimit=" +
                        pageLimit + "&sortCriteria=" + sortCriteria + "&pageLimit=" + pageLimit + "&pageNum=" + pageNum));

        return jobApplicationService.renderJobApplicationListPage(jobType, response, pageLimit);
    }
}