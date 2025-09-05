package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import controllers.routes;
import models.Job;
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

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static play.mvc.Controller.session;
import static play.mvc.Results.ok;
import static play.mvc.Results.redirect;
import static utils.Common.beginIndexForPagination;
import static utils.Common.endIndexForPagination;

/**
 * This class intends to provide support for JobController.
 */
public class JobService {
    @Inject
    Config config;

    private final UserService userService;
    private Form<Job> JobForm;

    @Inject
    public JobService(UserService userService) {
        this.userService = userService;
    }


    /**
     * This method intends to get Job by id by calling backend APIs.
     *
     * @param jobId
     * @return Job
     */
    public Job getJobById(Long jobId) {
        Job job = null;
        String userId = session("id");
        try {
            JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.GET_JOB_BY_ID + jobId));
            if (response.has("error")) {
                Logger.debug("JobService.getJobById() did not get job from backend with error.");
                return null;
            }

            job = Job.deserialize(response);
        } catch (Exception e) {
            Logger.debug("JobService.getJobById() exception: " + e.toString());
            return null;
        }
        return job;
    }


    /**
     * This method intends to save a pdf to job.
     *
     * @param body
     * @param jobId: job id
     * @throws Exception
     */
    public void savePDFToJob(Http.MultipartFormData body, Long jobId) throws Exception {
        try {
            if (body.getFile("pdf") != null) {
                Http.MultipartFormData.FilePart pdf = body.getFile("pdf");
                if (pdf != null && !pdf.getFilename().equals("")) {
                    File file = (File) pdf.getFile();
                    JsonNode pdfResponse = RESTfulCalls.postAPIWithFile(RESTfulCalls.getBackendAPIUrl(config,
                            Constants.SET_JOB_PDF + jobId), file);
                }
            }
        } catch (Exception e) {
            Logger.debug("JobService.savePDFToJob exception: " + e.toString());
            throw e;
        }
    }


    /************************************************ Page Render Preparation *****************************************/
    /**
     * This private method renders the job list page.
     * Note that for performance consideration, the backend only passes back the jobs for the needed page stored in
     * the JobListJsonNode, together with the offset/count/total/sortCriteria information.
     *
     * @param jobListJsonNode
     * @param pageLimit
     * @param searchBody
     * @param listType        : "all"; "search" (draw this page from list function or from search function)
     * @param username
     * @param userId
     * @return render challenge list page; If exception happened then render the homepage
     */
    public Result renderJobListPage(JsonNode jobListJsonNode,
                                    int pageLimit,
                                    String searchBody,
                                    String listType,
                                    String username,
                                    Long userId) {
        try {
            // if no value is returned or error
            if (jobListJsonNode == null || jobListJsonNode.has("error")) {
                Logger.debug("Job list is empty or error!");
                return redirect(routes.Application.home());
            }

            JsonNode jobsJsonArray = jobListJsonNode.get("items");
            if (!jobsJsonArray.isArray()) {
                Logger.debug("Job list is not array!");
                return redirect(routes.Application.home());
            }

            List<Job> jobs = new ArrayList<>();
            for (int i = 0; i < jobsJsonArray.size(); i++) {
                JsonNode json = jobsJsonArray.path(i);
                Job job = Job.deserialize(json);
                jobs.add(job);
            }

            // offset: starting index of the pageNum; count: the number of items in the pageNum;
            // total: the total number of items in all pages; sortCriteria: the sorting criteria (field)
            // pageNum: the current page number
            String sortCriteria = jobListJsonNode.get("sort").asText();

            int total = jobListJsonNode.get("total").asInt();
            int count = jobListJsonNode.get("count").asInt();
            int offset = jobListJsonNode.get("offset").asInt();
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

    /************************************************ Page Render Preparation *****************************************/
    /**
     * This private method renders the job list page.
     * Note that for performance consideration, the backend only passes back the jobs for the needed page stored in
     * the JobListJsonNode, together with the offset/count/total/sortCriteria information.
     *
     * @param jobListJsonNode
     * @param pageLimit
     * @param searchBody
     * @param listType        : "all"; "search" (draw this page from list function or from search function)
     * @param username
     * @param userId
     * @return render challenge list page; If exception happened then render the homepage
     */
    public Result renderJobApplicationListPage(JsonNode jobListJsonNode,
                                    int pageLimit,
                                    String searchBody,
                                    String listType,
                                    String username,
                                    Long userId) {
        try {
            // if no value is returned or error
            if (jobListJsonNode == null || jobListJsonNode.has("error")) {
                Logger.debug("Job list is empty or error!");
                return redirect(routes.Application.home());
            }

            JsonNode jobsJsonArray = jobListJsonNode.get("items");
            if (!jobsJsonArray.isArray()) {
                Logger.debug("Job list is not array!");
                return redirect(routes.Application.home());
            }

            List<Job> jobs = new ArrayList<>();
            for (int i = 0; i < jobsJsonArray.size(); i++) {
                JsonNode json = jobsJsonArray.path(i);
                Job job = Job.deserialize(json);
                jobs.add(job);
            }

            // offset: starting index of the pageNum; count: the number of items in the pageNum;
            // total: the total number of items in all pages; sortCriteria: the sorting criteria (field)
            // pageNum: the current page number
            String sortCriteria = jobListJsonNode.get("sort").asText();

            int total = jobListJsonNode.get("total").asInt();
            int count = jobListJsonNode.get("count").asInt();
            int offset = jobListJsonNode.get("offset").asInt();
            int pageNum = offset / pageLimit + 1;

            int beginIndexPagination = beginIndexForPagination(pageLimit, total, pageNum);
            int endIndexPagination = endIndexForPagination(pageLimit, total, pageNum);

            return ok(jobApplicationList.render(null, jobs, pageNum, sortCriteria, offset, total, count,
                    listType, pageLimit, searchBody, userId, beginIndexPagination, endIndexPagination));
        } catch (Exception e) {
            Logger.debug("Exception in renderJobListPage:" + e.toString());
            e.printStackTrace();
            return redirect(routes.Application.home());
        }
    }


    /**************************************************** (De)Serialization *******************************************/
    /**
     * This method intends to prepare a json job from Job form.
     *
     * @param jobForm: job registration form
     * @return
     * @throws Exception
     */
    public JsonNode serializeFormToJson(Form<Job> jobForm) throws Exception {
        JsonNode jsonData = null;
        try {
            Job job = jobForm.get();
            String longDescription = job.getLongDescription();
            if (longDescription != null) {
                longDescription.replaceAll(
                        "\n", "").replaceAll("\r", "");
            }

            if (job.getJobPublisher()==null) {
                User user = new User(Long.parseLong(session("id")));
                job.setJobPublisher(user);
            }
            return Json.toJson(job);
        } catch (Exception e) {
            Logger.debug("JobService.serializeFormToJson exception: " + e.toString());
            throw e;
        }
    }

}
