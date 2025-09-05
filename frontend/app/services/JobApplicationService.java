package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import controllers.routes;
import models.Job;
import models.JobApplication;
import models.RAJobApplication;
import models.TAJobApplication;
import models.User;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import utils.Constants;
import utils.RESTfulCalls;
import views.html.jobList;
import views.html.jobApplicationList;
import views.html.raJobApplicationList;
import views.html.taJobApplicationList;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static controllers.Application.isPrivateProjectZone;
import static play.mvc.Controller.session;
import static play.mvc.Results.ok;
import static play.mvc.Results.redirect;
import static utils.Common.beginIndexForPagination;
import static utils.Common.endIndexForPagination;

/**
 * This class intends to provide support for JobController.
 */
public class JobApplicationService {
    @Inject
    Config config;

    private final UserService userService;
    private Form<Job> jobForm;

    @Inject
    public JobApplicationService(UserService userService) {
        this.userService = userService;
    }

    /**
     * This method returns the current JobZone. Default job zone is OpenNEX (0).
     * OpenNEX job id = 0; private zone job id < 0
     *
     * @return Job current JobZone
     */
    public Job getCurrentJobZone() {
        Job currentJobZone = null;
        if (session("jobId") != null && Long.parseLong(session("jobId")) > 0) {
            currentJobZone = getJobById(Long.parseLong(session("jobId")));
        }
        return currentJobZone;
    }


    /**
     * This method intends to get Job by id by calling backend APIs.
     *
     * @param jobId
     * @return Job
     */
    public Job getJobById(Long jobId) {
        Job job = null;
        try {

            JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.GET_JOB_BY_ID + jobId));
            if (response.has("error")) {
                Logger.debug("JobService.getJobById() did not get job from backend with error.");
                return null;
            }
            System.out.println("response: ==" + response.toString());

            job = Job.deserialize(response);

            System.out.println("job: "+ job);
            if (job.getJobPublisher() == null) {
                Logger.debug("JobService.getJobById() creator is null");
                throw new Exception("JobService.getJobById() creator is null");
            }
        } catch (Exception e) {
            Logger.debug("JobService.getJobById() exception: " + e.toString());
            return null;
        }
        return job;
    }

    /**
     * This method intends to get Job application by id by calling backend APIs.
     *
     * @param jobApplicationId
     * @return Job
     */
    public JobApplication getJobApplicationById(Long jobApplicationId) {
        JobApplication jobApplication = null;
        try {

            JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.GET_JOB_APPLICATION_BY_ID + jobApplicationId));
            if (response.has("error")) {
                Logger.debug("JobService.getJobById() did not get job from backend with error.");
                return null;
            }
            System.out.println("response: ==" + response.toString());

            jobApplication = jobApplication.deserialize(response);

            System.out.println("jobApplication: "+ jobApplication);
            if (jobApplication.getApplicant() == null) {
                Logger.debug("JobService.getApplicant() creator is null");
                throw new Exception("JobService.getJobById() creator is null");
            }

            if (jobApplication.getAppliedJob() == null) {
                Logger.debug("JobService.getAppliedJob() creator is null");
                throw new Exception("JobService.getAppliedJob() creator is null");
            }

        } catch (Exception e) {
            Logger.debug("JobService.getJobById() exception: " + e.toString());
            return null;
        }
        return jobApplication;
    }

    /**
     * This method intends to get all jobs by a creator logged into the system.
     *
     * @return
     */
//    public ArrayList<Job> getJobsByCreator() {
//        try {
//            JsonNode jobs = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
//                    Constants.GET_JOB_BY_CREATOR
//                    + session("id")));
//            if (jobs == null || jobs.has("error")) return null;
//            return Job.deserializeJsonArrayToJobList(jobs);
//        } catch (Exception e) {
//            Logger.debug("JobService.getJobsByCreator exception: " + e.toString());
//            return null;
//        }
//    }


    /**
     * This method intends to save a picture to job.
     *
     * @param body
     * @param jobId: job id
     * @throws Exception
     */
//    public void savePictureToJob(Http.MultipartFormData body, Long jobId) throws Exception {
//        try {
//            if (body.getFile("picture") != null) {
//                Http.MultipartFormData.FilePart image = body.getFile("picture");
//                if (image != null && !image.getFilename().equals("")) {
//                    File file = (File) image.getFile();
//                    JsonNode imgResponse = RESTfulCalls.postAPIWithFile(RESTfulCalls.getBackendAPIUrl(config,
//                            Constants.SET_Job_IMAGE + jobId), file);
//                }
//            }
//        } catch (Exception e) {
//            Logger.debug("JobService.savePictureToJob exception: " + e.toString());
//            throw e;
//        }
//    }

    /**
     * This method intends to save a pdf to job.
     *
     * @param body
     * @param jobId: job id
     * @throws Exception
     */
//    public void savePDFToJob(Http.MultipartFormData body, Long jobId) throws Exception {
//        try {
//            if (body.getFile("pdf") != null) {
//                Http.MultipartFormData.FilePart pdf = body.getFile("pdf");
//                if (pdf != null && !pdf.getFilename().equals("")) {
//                    File file = (File) pdf.getFile();
//                    JsonNode pdfResponse = RESTfulCalls.postAPIWithFile(RESTfulCalls.getBackendAPIUrl(config,
//                            Constants.SET_Job_PDF + jobId), file);
//                }
//            }
//        } catch (Exception e) {
//            Logger.debug("JobService.savePDFToJob exception: " + e.toString());
//            throw e;
//        }
//    }

    /**
     * This method intends to add a list of team members to a job, from job registration form.
     *
     * @param JobForm: job registration form
     * @param body
     * @param jobId:   job id
     */
    public void addTeamMembersToJob(Form<Job> JobForm, Http.MultipartFormData body, Long jobId) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            int count = Integer.parseInt(JobForm.field("count").value()); //the number of team members in the job
            for (int i = 0; i < count; i++) {
                if (JobForm.field("member" + i) != null) {
                    ObjectNode memberData = mapper.createObjectNode();
                    memberData.put("name", JobForm.field("member" + i).value());
                    memberData.put("email", JobForm.field("email" + i).value());
                    JsonNode memberRes = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                            Constants.ADD_TEAM_MEMBER + jobId), memberData);
                    if (body.getFile("photo" + i) != null) {
                        Http.MultipartFormData.FilePart photo = body.getFile("photo" + i);
                        if (photo != null && !photo.getFilename().equals("")) {
                            File memphoto = (File) photo.getFile();
                            JsonNode photoRes =
                                    RESTfulCalls.postAPIWithFile(RESTfulCalls.getBackendAPIUrl(config,
                                            Constants.SET_TEAM_MEMBER_PHOTO
                                            + memberRes.asLong()), memphoto);
                        }
                    }
                    userService.createUserbyAddingTeamMember(JobForm.field("member" + i).value(),
                            JobForm.field("email" + i).value());
                }
            }
        } catch (Exception e) {
            Logger.debug("JobService.addTeamMembersToJob exception: " + e.toString());
            throw e;
        }
    }

    /**
     * This method intends to add a list of team members to a job, from job registration form.
     *
     * @param JobForm: job registration form
     */
    public void deleteTeamMembersToJob(Form<Job> JobForm) {
        try {
            int deleteCount = 0;
            if (JobForm.field("delc").value() != null && JobForm.field("delc").value().trim() != "")
                deleteCount = Integer.parseInt(JobForm.field("delc").value());
            //delete chosen team members
            for (int i = 0; i < deleteCount; i++) {
                Long deleteTeamMemberId = Long.parseLong(JobForm.field("delete" + i).value());
                JsonNode deleteResponse = RESTfulCalls.deleteAPI(RESTfulCalls.getBackendAPIUrl(config,
                        Constants.DELETE_TEAM_MEMBER + deleteTeamMemberId));
            }
        } catch (Exception e) {
            Logger.debug("JobService.deleteTeamMembersToJob exception: " + e.toString());
            throw e;
        }
    }





    /************************************************ Page Render Preparation *****************************************/
    /**
     * This private method renders the job list page.
     * Note that for performance consideration, the backend only passes back the jobs for the needed page stored in
     * the JobListJsonNode, together with the offset/count/total/sortCriteria information.
     *
     * @param JobListJsonNode
     * @param currentJobZone
     * @param pageLimit
     * @param searchBody
     * @param listType            : "all"; "search" (draw this page from list function or from search function)
     * @param username
     * @param userId
     * @return render job list page; If exception happened then render the homepage
     */
    public Result renderJobListPage(JsonNode JobListJsonNode,
                                        Job currentJobZone,
                                        int pageLimit,
                                        String searchBody,
                                        String listType,
                                        String username,
                                        Long userId) {
        try {
            // if no value is returned or error
            if (JobListJsonNode == null || JobListJsonNode.has("error")) {
                Logger.debug("Job list is empty or error!");
                return redirect(routes.Application.home());
            }

            JsonNode JobsJsonArray = JobListJsonNode.get("items");
            if (!JobsJsonArray.isArray()) {
                Logger.debug("Job list is not array!");
                return redirect(routes.Application.home());
            }

            List<Job> jobs = new ArrayList<>();
            for (int i = 0; i < JobsJsonArray.size(); i++) {
                JsonNode json = JobsJsonArray.path(i);
                Job job = Job.deserialize(json);
                jobs.add(job);
            }

            // offset: starting index of the pageNum; count: the number of items in the pageNum;
            // total: the total number of items in all pages; sortCriteria: the sorting criteria (field)
            // pageNum: the current page number
            String sortCriteria = JobListJsonNode.get("sort").asText();

            int total = JobListJsonNode.get("total").asInt();
            int count = JobListJsonNode.get("count").asInt();
            int offset = JobListJsonNode.get("offset").asInt();
            int pageNum = offset / pageLimit + 1;

            int beginIndexPagination = beginIndexForPagination(pageLimit, total, pageNum);
            int endIndexPagination = endIndexForPagination(pageLimit, total, pageNum);

            return ok(jobList.render(jobs, pageNum, sortCriteria, offset, total, count,
                    listType, pageLimit, searchBody, userId, beginIndexPagination, endIndexPagination));
        } catch (Exception e) {
            Logger.debug("Exception in renderJobListPage:" + e.toString());
            e.printStackTrace();
            return redirect(routes.Application.home());
        }
    }


    /**************************************************** (De)Serialization *******************************************/
    /**
     * This method intends to prepare a json object from Job form.
     *
     * @param JobApplicationForm: job registration form
     * @return
     * @throws Exception
     */
    public ObjectNode serializeFormToJson(Form<JobApplication> JobApplicationForm, Long jobId) throws Exception {
        ObjectNode jsonData = null;
        System.out.println("job application form: " + JobApplicationForm.toString());
        try {
            Map<String, String> tmpMap = JobApplicationForm.data();
            jsonData = (ObjectNode) (Json.toJson(tmpMap));

            if (JobApplicationForm.field("markAsPrivate").value() != null && JobApplicationForm.field(
                    "markAsPrivate").value().equals("on")) {
                jsonData.put("authentication", "private");
            } else {
                jsonData.put("authentication", "public");

            }

            User user = new User(Long.parseLong(session("id")));
            jsonData.put("applicant", Json.toJson(user));

            Job job = new Job(jobId);
            jsonData.put("appliedJob", Json.toJson(job));

        } catch (Exception e) {
            Logger.debug("JobApplicationService.serializeFormToJson exception: " + e.toString());
            throw e;
        }

        return jsonData;
    }

    public Result renderJobApplicationListPage(String jobType, JsonNode jobApplicationsNode, int pageLimit) {
        try {
            if (jobApplicationsNode == null || jobApplicationsNode.has("error")) {
                Logger.debug("Job application list is empty or error!");
                return redirect(routes.Application.home());
            }

            JsonNode jobApplicationJsonArray = jobApplicationsNode.get("items");

            if (!jobApplicationJsonArray.isArray()) {
                Logger.debug("Job application list is not array!");
                return redirect(routes.Application.home());
            }

            // Offset
            String retSort = jobApplicationsNode.get("sort").asText();
            int total = jobApplicationsNode.get("total").asInt();
            int count = jobApplicationsNode.get("count").asInt();
            int offset = jobApplicationsNode.get("offset").asInt();
            int page = offset/pageLimit + 1;
            int beginIndexPagination = beginIndexForPagination(pageLimit, total, page);
            int endIndexPagination = endIndexForPagination(pageLimit, total, page);

            List<JobApplication> jobApplications = new ArrayList<>();
            List<RAJobApplication> raJobApplications = new ArrayList<>();
            List<TAJobApplication> taJobApplications = new ArrayList<>();

            for (int i = 0; i < jobApplicationJsonArray.size(); i++) {
                JsonNode json = jobApplicationJsonArray.path(i);
                if ("rajob".equals(jobType))
                    raJobApplications.add(RAJobApplication.deserialize(json));
                else if ("tajob".equals(jobType))
                    taJobApplications.add(TAJobApplication.deserialize(json));
                else
                    jobApplications.add(JobApplication.deserialize(json));
            }

            if ("rajob".equals(jobType))
                return ok(raJobApplicationList.render(raJobApplications, page, retSort, offset, total, count, "", pageLimit, "", Long.valueOf(session("id")), beginIndexPagination, endIndexPagination));
            else if ("tajob".equals(jobType))
                return ok(taJobApplicationList.render(taJobApplications, page, retSort, offset, total, count, "", pageLimit, "", Long.valueOf(session("id")), beginIndexPagination, endIndexPagination));
            else
                return ok(jobApplicationList.render(jobApplications, null, page, retSort, offset, total, count, "", pageLimit, "", Long.valueOf(session("id")), beginIndexPagination, endIndexPagination));
        } catch (Exception e) {
            Logger.debug("UserService.renderUserListPage exception: " + e.toString());
            return redirect(routes.Application.home());
        }
    }
}
