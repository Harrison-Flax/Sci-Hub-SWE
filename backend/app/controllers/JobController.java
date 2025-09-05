package controllers;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.typesafe.config.Config;
import io.ebean.Expr;
import models.*;
import models.rest.RESTResponse;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import services.JobService;
import utils.Common;

import javax.inject.Inject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static utils.Constants.*;

public class JobController extends Controller {
    public static final String JOB_DEFAULT_SORT_CRITERIA = "title";
    public static final String JOB_DESCRIPTION_IMAGE_KEY = "jobDescriptionImage-";
    public static final String JOB_IMAGE_KEY = "jobImage-";

    private final JobService jobService;

    @Inject
    Config config;

    @Inject
    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    /************************************************* Add Job *********************************************************/
    /**
     * This method intends to add a job into database.
     *
     * @return created status with project id created
     */
    public Result addJob() {
        try {
            JsonNode json = request().body().asJson();
            if (json == null) {
                Logger.debug("Job information not saved, expecting Json data");
                return badRequest("Job information not saved, expecting Json data");
            }

            Job job = Json.fromJson(json, Job.class);
            job.setStatus("open");
            job.setIsActive("True");
            job.save();

            String folderName = "job/";
            Long applicationId = job.getId();
            String tableName = "job";
            return ok(Json.toJson(job.getId()).toString());
        } catch (Exception e) {
            Logger.debug("Job cannot be added: " + e.toString());
            return badRequest("Job not saved: ");
        }
    }
    /************************************************* End of Add Job **************************************************/

    /************************************************* Apply Job ******************************************************/
    /**
     * This method intends to apply a job into database.
     *
     * @return created status with job id created
     */
    public Result applyJob(Long jobId) {
        try {
            JsonNode json = request().body().asJson();
            if (json == null) {
                Logger.debug("Job information not saved, expecting Json data");
                return badRequest("Job information not saved, expecting Json data");
            }

            JobApplication jobApplication = Json.fromJson(json, JobApplication.class);
            jobApplication.setIsActive("True");
            jobApplication.setCreatedTime(new Date().toString());

            System.out.println("backend job application info: " + json);

            jobApplication.save();

            String folderName = "jobApplication/";
            String tableName = "job_application";

            return ok(Json.toJson(jobApplication.getId()).toString());
        } catch (Exception e) {
            Logger.debug("Job cannot be added: " + e.toString());
            return badRequest("Job not applied: ");
        }
    }
    /************************************************* End of Apply Job ***********************************************/


    /************************************************* Update Job *************************************************/
    /**
     * This method intends to update job information except picture.
     *
     * @param jobId
     * @return
     */
    public Result updateJob(Long jobId) {
        try {
            JsonNode json = request().body().asJson();
            if (json == null) {
                Logger.debug("Job information not saved, expecting Json data from JobController.updateJob");
                return badRequest("Job information not saved, expecting Json data");
            }

            Job updatedJob = Json.fromJson(json, Job.class);
//            String newHtml = updatedChallenge.getShortDescription();
//            Set<String> newImageSet = getImageSet(newHtml);
//
//            Challenge oldChallenge = Challenge.find.byId(challengeId);
//            String oldHtml = oldChallenge.getShortDescription();
//            Set<String> oldImageSet = getImageSet(oldHtml);

//            for (String imageName : oldImageSet) {
//                if (!newImageSet.contains(imageName)) {
//                    Common.deleteFileFromS3(config, imageName);
//                }
//            }

            String folderName = "job/";
            String tableName = "job";
            updatedJob.update();
            return ok(Json.toJson(updatedJob));
        } catch (Exception e) {
            Logger.debug("Job Profile not saved with id: " + jobId + " with exception: " + e.toString());
            return badRequest("Job Profile not saved: " + jobId);
        }
    }

    private Set<String> getImageSet(String html) {
        Set<String> set = new HashSet<>();
        if (html == null || html.length() == 0)
            return set;
        int startIndex = html.indexOf("projectDescriptionImage");
        while (startIndex >= 0) {
            int endIndex = html.indexOf("width=\"50%\"", startIndex);
            String imageName = html.substring(startIndex, endIndex);
            set.add(imageName);
            startIndex = html.indexOf("projectDescriptionImage", startIndex + 1);
        }
        return set;
    }

    public Result jobUpdateStatue(Long jobId) {
        try {
            System.out.println("get Job update info..");
            JsonNode json = request().body().asJson();
            System.out.println("get Job update info...." + jobId);
            if (json == null) {
                Logger.debug("Job Status did not updated, expecting Json data from JobController.updateJob");
                return badRequest("Job Status did not updated, expecting Json data");
            }

            Job updatedJob = Job.find.byId(jobId);
//            String newHtml = updatedChallenge.getShortDescription();
//            Set<String> newImageSet = getImageSet(newHtml);
//
//            Challenge oldChallenge = Challenge.find.byId(challengeId);
//            String oldHtml = oldChallenge.getShortDescription();
//            Set<String> oldImageSet = getImageSet(oldHtml);

//            for (String imageName : oldImageSet) {
//                if (!newImageSet.contains(imageName)) {
//                    Common.deleteFileFromS3(config, imageName);
//                }
//            }
            System.out.println("status update::: to ::::"+json.get("status").toString());
            updatedJob.setStatus(json.get("status").asText());
            updatedJob.update();
            return ok(Json.toJson(updatedJob));
        } catch (Exception e) {
            Logger.debug("Job Profile not saved with id: " + jobId + " with exception: " + e.toString());
            return badRequest("Job Profile not saved: " + jobId);
        }
    }

    /**
     * Delete job image by job id.
     *
     * @param jobId
     * @return
     */
    public Result deleteJobImage(Long jobId) {
        if (jobId == null) {
            return Common.badRequestWrapper("job id is null thus cannot delete image for it.");
        }
        try {
            Job job = Job.find.byId(jobId);
            if (job != null) {
                Common.deleteFileFromS3(config, "job", "Image", jobId);
                job.setImageURL("");
                job.save();
                return ok("Job image deleted successfully for project id: " + jobId);
            } else {
                return Common.badRequestWrapper("Cannot find project thus cannot delete image for it.");
            }
        } catch (Exception e) {
            Logger.debug("Cannot delete job image for exception:" + e.toString());
            return Common.badRequestWrapper("Cannot delete job picture.");
        }
    }

    /**
     * This method intends to delete job pdf by job id
     *
     * @param jobId
     * @return
     */
    public Result deleteJobPDF(Long jobId) {
        try {
            Job tJ = Job.find.byId(jobId);
            Common.deleteFileFromS3(config, "job", "Pdf", jobId);
            tJ.setPdf("");
            tJ.save();
        } catch (Exception e) {
            Logger.debug("Failed to set pdf for job: " + e.toString());
            return Common.badRequestWrapper("Failed to add pdf to the job");
        }

        return ok("success");
    }
    /************************************************* End of Update Job ***********************************************/

    /************************************************* Job List ********************************************************/
    /**
     * Gets a list of all the jobs based on optional offset and limit and sort
     *
     * @param pageLimit    shows the number of rows we want to receive
     * @param pageNum      shows the page number
     * @param sortCriteria shows based on what column we want to sort the data
     * @return a list of projects
     * // TODO: Clean Common utitlity class for getSortCriteria(), not always register_time_stamp
     */
    public Result jobList(Long userId, Integer pageLimit, Integer pageNum, Optional<String> sortCriteria) {
        List<Job> activeJobs = new ArrayList<>();

        Set<Long> jobIds = new HashSet<>();
        List<Job> jobs;
        String sortOrder = Common.getSortCriteria(sortCriteria, JOB_DEFAULT_SORT_CRITERIA);

        int offset = pageLimit * (pageNum - 1);
        try {
            User user = User.find.byId(userId);


            activeJobs = Job.find.query().where().eq("is_active", ACTIVE).ne("status", "closed").findList();
            for (Job job : activeJobs) {
                jobIds.add(job.getId());
            }

            if (sortOrder.equals("id") || sortOrder.equals("access_times"))
                jobs = Job.find.query().where().in("id", jobIds).order().desc(sortOrder)
                        .findList();
            else
                jobs = Job.find.query().where().in("id", jobIds).orderBy(sortOrder)
                        .findList();
            // **add updatetime 20250207 wx**
            SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEE MMM dd yyyy", Locale.ENGLISH);

            for (Job job : jobs) {
                String rawUpdateTime = job.getUpdateTime();
                if (rawUpdateTime != null && !rawUpdateTime.isEmpty()) {
                    try {
                        Date parsedDate = inputFormat.parse(rawUpdateTime);
                        String formattedTime = outputFormat.format(parsedDate);
                        job.setUpdateTime(formattedTime);
                    } catch (ParseException e) {
                        Logger.warn("Error parsing updateTime: " + rawUpdateTime);
                    }
                }
            }
            RESTResponse response = jobService.paginateResults(jobs, Optional.of(offset), Optional.
                    of(pageLimit), sortOrder);
            return ok(response.response());
        } catch (Exception e) {
            Logger.debug("JobController.jobList() exception: " + e.toString());
            return internalServerError("JobController.jobList() exception: " + e.toString());
        }
    }

    /************************************************* End of Job List *************************************************/

    /************************************************* Get Job *********************************************************/
    /**
     * Get a job detail by the job id
     *
     * @param jobId job Id
     * @return ok if the job is found; badRequest if the project is not found
     */
    public Result getJobById(Long jobId) {
        if (jobId == null) {
            return Common.badRequestWrapper("jobId is null or empty.");
        }

        if (jobId == 0) return ok(Json.toJson(null));  // jobId=0 means SMU-Sci-Hub job, not stored in DB

        try {
            Job job = Job.find.query().where().eq("id", jobId).findOne();
            ObjectNode response = (ObjectNode) Json.toJson(job);

            List<JobApplication> applications = JobApplication.find.query()
                    .where().eq("appliedJob.id", jobId)
                    .findList();


            Set<Long> uniqueApplicantIds = applications.stream()
                    .map(app -> app.getApplicant() != null ? app.getApplicant().getId() : null)
                    .filter(id -> id != null)
                    .collect(Collectors.toSet());

            response.putPOJO("jobApplicationIdList", uniqueApplicantIds);

            return ok(response);
        } catch (Exception e) {
            Logger.debug("JobController.getJobById() exception : " + e.toString());
            return internalServerError("Internal Server Error JobController.getJobById() exception: " +
                    e.toString());
        }
    }

    public Result getApplicationsByJobId(String jobType, Long jobId, Integer pageLimit, Integer pageNum, Optional<String> sortCriteria) {
        jobType = jobType.toLowerCase();

        String sortOrder = Common.getSortCriteria(sortCriteria, JOB_DEFAULT_SORT_CRITERIA);
        int offset = pageLimit * (pageNum - 1);
        try {
            List<Object> applications = new ArrayList<Object>();
            SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEE MMM dd yyyy", Locale.ENGLISH);

            if ("rajob".equals(jobType)) {
                for (RAJobApplication jobApplication : RAJobApplication.find.query().where().eq("rajob_id", jobId).findList()) {
                    String rawCreatedTime = jobApplication.getCreatedTime();
                    if (rawCreatedTime != null && !rawCreatedTime.isEmpty()) {
                        try {
                            Date parsedDate = inputFormat.parse(rawCreatedTime);
                            String formattedTime = outputFormat.format(parsedDate);
                            jobApplication.setCreatedTime(formattedTime); // 直接更新为格式化后的日期
                        } catch (ParseException e) {
                            Logger.warn("Error parsing createdTime: " + rawCreatedTime);
                        }
                    }
                    applications.add(jobApplication);
                }
            } else if ("tajob".equals(jobType)) {
                for (TAJobApplication jobApplication : TAJobApplication.find.query().where().eq("tajob_id", jobId).findList()) {
                    String rawCreatedTime = jobApplication.getCreatedTime();
                    if (rawCreatedTime != null && !rawCreatedTime.isEmpty()) {
                        try {
                            Date parsedDate = inputFormat.parse(rawCreatedTime);
                            String formattedTime = outputFormat.format(parsedDate);
                            jobApplication.setCreatedTime(formattedTime);
                        } catch (ParseException e) {
                            Logger.warn("Error parsing createdTime: " + rawCreatedTime);
                        }
                    }
                    applications.add(jobApplication);
                }
            } else {
                for (JobApplication jobApplication : JobApplication.find.query().where().eq("job_id", jobId).findList()) {
                    String rawCreatedTime = jobApplication.getCreatedTime();
                    if (rawCreatedTime != null && !rawCreatedTime.isEmpty()) {
                        try {
                            Date parsedDate = inputFormat.parse(rawCreatedTime);
                            String formattedTime = outputFormat.format(parsedDate);
                            jobApplication.setCreatedTime(formattedTime);
                        } catch (ParseException e) {
                            Logger.warn("Error parsing createdTime: " + rawCreatedTime);
                        }
                    }
                    applications.add(jobApplication);
                }
            }
            List<StudentInfo> studentInfos = StudentInfo.find.query().findList();
            List<ResearcherInfo> researcherInfos = ResearcherInfo.find.query().findList();
            for(Object jobApplication: applications) {
                User applicant = null;
                if ("rajob".equals(jobType)) applicant = ((RAJobApplication)jobApplication).getApplicant();
                else if ("tajob".equals(jobType)) applicant = ((TAJobApplication)jobApplication).getApplicant();
                else applicant = ((JobApplication)jobApplication).getApplicant();
                for (StudentInfo studentInfo: studentInfos) {
                    if (studentInfo.getUser().getId() == applicant.getId()) applicant.setStudentInfo(studentInfo);
                }
                for (ResearcherInfo researcherInfo: researcherInfos) {
                    if (researcherInfo.getUser().getId() == applicant.getId()) applicant.setResearcherInfo(researcherInfo);
                }
            }

            RESTResponse response = jobService.paginateJobApplications(jobType, applications, Optional.of(offset), Optional.
                    of(pageLimit), sortOrder);
            return ok(response.response());
        } catch (Exception e) {
            Logger.debug("JobController.getApplicationsByJobId() exception : " + e.toString());
            return internalServerError("Internal Server Error JobController.getApplicationsByJobId() exception: " +
                    e.toString());
        }
    }

    public Result getJobApplicationsByUser(String jobType, Long userId, Integer pageLimit, Integer pageNum, Optional<String> sortCriteria) {
        if (userId == null) {
            return badRequest("User ID cannot be null.");
        }

        jobType = jobType.toLowerCase();
        String sortOrder = Common.getSortCriteria(sortCriteria, JOB_DEFAULT_SORT_CRITERIA);
        int offset = pageLimit * (pageNum - 1);
        try {
            List<Object> applications = new ArrayList<>();

            if ("rajob".equals(jobType)) {
                for (RAJobApplication jobApplication : RAJobApplication.find.query().where().eq("applicant.id", userId).findList()) {
                    applications.add(jobApplication);
                }
            } else if ("tajob".equals(jobType)) {
                for (TAJobApplication jobApplication : TAJobApplication.find.query().where().eq("applicant.id", userId).findList()) {
                    applications.add(jobApplication);
                }
            } else {
                for (JobApplication jobApplication : JobApplication.find.query().where().eq("applicant.id", userId).findList()) {
                    applications.add(jobApplication);
                }
            }

            List<StudentInfo> studentInfos = StudentInfo.find.query().findList();
            List<ResearcherInfo> researcherInfos = ResearcherInfo.find.query().findList();

            for(Object jobApplication: applications) {
                User applicant = null;
                if ("rajob".equals(jobType)) applicant = ((RAJobApplication)jobApplication).getApplicant();
                else if ("tajob".equals(jobType)) applicant = ((TAJobApplication)jobApplication).getApplicant();
                else applicant = ((JobApplication)jobApplication).getApplicant();
                for (StudentInfo studentInfo: studentInfos) {
                    if (studentInfo.getUser().getId() == applicant.getId()) applicant.setStudentInfo(studentInfo);
                }
                for (ResearcherInfo researcherInfo: researcherInfos) {
                    if (researcherInfo.getUser().getId() == applicant.getId()) applicant.setResearcherInfo(researcherInfo);
                }
            }

            RESTResponse response = jobService.paginateJobApplications(jobType, applications, Optional.of(offset), Optional.of(pageLimit), sortOrder);
            return ok(response.response());
        } catch (Exception e) {
            Logger.debug("JobController.getJobApplicationsByUser() exception : " + e.toString());
            return internalServerError("Internal Server Error JobController.getJobApplicationsByUser() exception: " + e.toString());
        }
    }



    public Result getJobApplicationById(Long jobApplicationId) {
        if (jobApplicationId == null) {
            return Common.badRequestWrapper("jobApplicationId is null or empty.");
        }

        if (jobApplicationId == 0) return ok(Json.toJson(null));  // jobId=0 means SMU-Sci-Hub job, not stored in DB

        try {
            JobApplication jobApplication = JobApplication.find.query().where().eq("id", jobApplicationId).findOne();
            return ok(Json.toJson(jobApplication));
        } catch (Exception e) {
            Logger.debug("JobController.getJobApplicationById() exception : " + e.toString());
            return internalServerError("Internal Server Error JobController.getJobApplicationById() exception: " +
                    e.toString());
        }
    }

    /**
     * This method returns all jobs from job info table given the publisher id (the publisher of the job)
     *
     * @param userId the publisher Id
     * @return all jobs.
     */
    public Result getJobsByPublisher(Long userId) {
        try {

            List<Job> jobs = Job.find.query().where().eq("job_publisher_id", userId).findList();
            for(Job job : jobs){
                int numOfApplicants = JobApplication.find.query().where().eq("job_id", job.getId()).findCount();
                job.setNumberOfApplicants(numOfApplicants);
            }
            ArrayNode jobArray = Common.objectList2JsonArray(jobs);
            return ok(jobArray);
        } catch (Exception e) {
            Logger.debug("JobController.getJobsByPublisher exception: " + e.toString());
            return internalServerError("JobController.getJobsByPublisher exception: " + e.toString());
        }
    }
    /************************************************* End of Get Job **************************************************/
    /**
     * This method returns all jobs applied by a specific applicant given the applicant's userId.
     *
     * @param userId the applicant's userId
     * @return all jobs applied by the applicant.
     */
    public Result getJobsByApplicant(Long userId) {
        try {

            List<JobApplication> applications = JobApplication.find.query().where().eq("applicant.id", userId).findList();

            ArrayNode resultArray = Json.newArray();
            for (JobApplication application : applications) {
                Job job = application.getAppliedJob();

                if (job != null) {
                    ObjectNode jobJson = Json.newObject();
                    jobJson.put("jobApplicationId", application.getId());
                    jobJson.put("id", job.getId());
                    jobJson.put("title", job.getTitle());
                    jobJson.put("shortDescription", job.getShortDescription());
                    jobJson.put("fields", job.getFields());
                    jobJson.put("status", job.getStatus());
                    jobJson.put("organization", job.getOrganization());
                    jobJson.put("location", job.getLocation());

                    resultArray.add(jobJson);
                }
            }

            return ok(resultArray);
        } catch (Exception e) {
            Logger.debug("JobController.getJobsByApplicant exception: " + e.toString());
            return internalServerError("JobController.getJobsByApplicant exception: " + e.toString());
        }
    }
    /************************************************* End of Get Job **************************************************/

    /**
     * Checks if a job name can be used.
     *
     * @return this job title is valid
     */
    public Result checkJobNameAvailability() {
        JsonNode json = request().body().asJson();
        if (json == null) {
            Logger.info("Cannot check job name, expecting Json data");
            return badRequest("Cannot check job name, expecting Json data");
        }
        String title = json.path("title").asText();
        if (title == null || title.isEmpty()) {
            Logger.info("job title is null or empty");
            return Common.badRequestWrapper("job title is null or empty.");
        }
        try {
            List<Job> jobs = Job.find.query().where().eq("title", title).findList();
            if (jobs == null || jobs.size() == 0) {
                return ok("This new job name can be used");
            } else {
                return Common.badRequestWrapper("This job name has been used.");
            }
        } catch (Exception e) {
            return internalServerError("JobController.checkJobNameAvailability exception: " +
                    e.toString());
        }
    }


    /**
     * This method intends to return the publisher of a job.
     *
     * @param jobId job id
     * @return json of the publisher of the job posting
     */
    public Result getJobPublisher(Long jobId) {
        try {
            Job job = Job.find.byId(jobId);
            if (job == null) {
                return Common.badRequestWrapper("No Job found with the given job id");
            }
            return ok(Json.toJson(job.getJobPublisher()));
        } catch (Exception e) {
            Logger.debug("JobController.getJobPublisher() exception: " + e.toString());
            return internalServerError("JobController.getJobPublisher() exception: " + e.toString());
        }
    }


    /**
     * Check if the job is search result
     *
     * @param job              Job being checked
     * @param title
     * @param goals
     * @param location
     * @param shortDescription
     * @return if the project is search result.
     */
    private boolean isMatchedJob(Job job, String title, String goals, String location, String shortDescription) {
        boolean titleInTitle = false;
        boolean goalInGoal = false;
        boolean locationInLocation = false;
        boolean descriptionInDescription = false;
        for (String titleSubWord : title.split(" ")) {
            titleSubWord = titleSubWord.trim();
            titleInTitle = title.equals("") || (job.getTitle() != null && job.getTitle().toLowerCase().
                    indexOf(titleSubWord.toLowerCase()) >= 0);
            if (titleInTitle)
                break;
        }
        for (String goalSubWord : goals.split(" ")) {
            goalSubWord = goalSubWord.trim();
            goalInGoal = goals.equals("") || (job.getGoals() != null && job.getGoals().toLowerCase().
                    indexOf(goalSubWord.toLowerCase()) >= 0);
            if (goalInGoal)
                break;
        }
        for (String locationSubWord : location.split(" ")) {
            locationSubWord = locationSubWord.trim();
            locationInLocation = location.equals("") || (job.getLocation() != null && job.getLocation().
                    toLowerCase().indexOf(locationSubWord.toLowerCase()) >= 0);
            if (locationInLocation)
                break;
        }
        for (String descriptionSubWord : shortDescription.split(" ")) {
            descriptionSubWord = descriptionSubWord.trim();
            descriptionInDescription = shortDescription.equals("") || (job.getShortDescription() != null &&
                    job.getShortDescription().toLowerCase().indexOf(descriptionSubWord.toLowerCase()) >= 0);
            if (descriptionInDescription)
                break;
        }
        return titleInTitle && goalInGoal && locationInLocation && descriptionInDescription;
    }

    /**
     * Filter the jobs based on title, goal, location, short description
     *
     * @param title            Job list being filtered
     * @param goals
     * @param location
     * @param shortDescription
     * @return the list of filtered jobs.
     */
    private List<Job> matchedJobList(List<Job> jobList, String title, String goals, String location,
                                     String shortDescription) {
        List<Job> results = new ArrayList<>();
        for (Job job : jobList) {
            if (isMatchedJob(job, title, goals, location, shortDescription))
                results.add(job);
        }
        return results;
    }


    /**
     * Find jobs by multiple condition, including title, goal, location, etc.
     *
     * @return jobs that match the condition
     * TODO: How to handle more conditions???
     */
    public Result searchJobsByCondition() {
        String result = null;
        try {
            JsonNode json = request().body().asJson();
            List<Job> jobs = new ArrayList<>();
            if (json == null) {
                return badRequest("Condition cannot be null");
            }
            //Get condition value from Json data

            String title = json.path("title").asText();

            String goals = json.path("goals").asText();

            String location = json.path("location").asText();

            String description = json.path("description").asText();

            String keywords = json.path("keywords").asText();
            //Search projects by conditions
            if (keywords.trim().equals("")) {
                List<Job> potentialJobs = Job.find.query().where().eq("is_active", ACTIVE).
                        findList();
                jobs = matchedJobList(potentialJobs, title, goals, location, description);

            } else {
                List<Job> tmpJobs = Job.find.query().where().eq("is_active", ACTIVE).ne("status", "closed").findList();
                String[] keywordList = keywords.toLowerCase().trim().split(" ");

                for (String keyword : keywordList) {
                    keyword = keyword.toLowerCase();
                    for (Job job : tmpJobs) {
                        if ((job.getTitle() != null && job.getTitle().toLowerCase().contains(keyword)) ||
                                (job.getGoals() != null && job.getGoals().toLowerCase().contains(keyword)) ||
                                (job.getLocation() != null && job.getLocation().toLowerCase().contains(keyword))
                                || (job.getShortDescription() != null && job.getShortDescription().toLowerCase().contains
                                (keyword))) {
                            jobs.add(job);
                        }
                    }
                }
            }
            //If not found
            if (jobs == null || jobs.size() == 0) {
                Logger.info("Jobs not found with search conditions");
                return notFound("Jobs not found with conditions");
            }
            Set<Long> jobsIdSet = new HashSet<>();
            List<Job> filteredJobs = new ArrayList<>();
            for (Job job : jobs) {
                if (!jobsIdSet.contains(job.getId())) {
                    filteredJobs.add(job);
                    jobsIdSet.add(job.getId());
                }
            }
            JsonNode jsonNode = Json.toJson(filteredJobs);
            result = jsonNode.toString();
        } catch (Exception e) {
            Logger.debug("JobController.searchJobsByCondition() exception: " + e.toString());
            return internalServerError("JobController.searchJobsByCondition() exception: " +
                    e.toString());
        }

        return ok(result);
    }


    /**
     * This method intends to set job image by job id
     *
     * @param projectId
     * @return TODO: change?
     */
    public Result setImage(Long projectId) {
        if (request().body() == null || request().body().asRaw() == null) {
            return Common.badRequestWrapper("The request cannot be empty");
        }
        try {
            Project tp = Project.find.byId(projectId);
            String url = Common.uploadFile(config, "project", "Image", projectId, request());
            tp.setImageUrl(url);
            tp.save();
        } catch (Exception e) {
            Logger.debug("Failed to set image for project: " + e.toString());
            return Common.badRequestWrapper("Failed to add image to the project");
        }

        // Return the app image.
        return ok("success");
    }

    /**
     * This method intends to set job pdf by job id
     *
     * @param jobId
     * @return
     */
    public Result setPDF(Long jobId) {
        if (request().body() == null || request().body().asRaw() == null) {
            return Common.badRequestWrapper("The request cannot be empty");
        }
        try {
            Job tJ = Job.find.byId(jobId);
            String url = Common.uploadFile(config, "job", "Pdf", jobId, request());
            tJ.setPdf(url);
            tJ.save();
        } catch (Exception e) {
            Logger.debug("Failed to set pdf for job: " + e.toString());
            return Common.badRequestWrapper("Failed to add pdf to the job");
        }

        // Return the app pdf.
        return ok("success");
    }


    /**
     * This method receivs a project id and the number of images in the project's description and checks the s3 bucket
     * to remove the images having id more than the project's description image count (This is because we can remove the
     * deleted description images from S3)
     *
     * @param projectId                project ID
     * @param countImagesInDescription number of images in the project's description
     */
    private void removeDeletedImagesInDescriptionFromS3(long projectId, int countImagesInDescription) {
        try {
            countImagesInDescription++;
            String keyName = JOB_DESCRIPTION_IMAGE_KEY + projectId + "-" + countImagesInDescription;
            String awsAccessKey = config.getString(AWS_ACCESS_KEY);
            String awsSecretAccesskey = config.getString(AWS_SECRET_ACCESS_KEY);
            String awsRegion = config.getString(AWS_REGION);
            BasicAWSCredentials awsCreds = new BasicAWSCredentials(awsAccessKey, awsSecretAccesskey);
            final AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                    .withRegion(awsRegion)
                    .build();
            boolean exists = s3.doesObjectExist(config.getString(AWS_BUCKET), keyName);
            while (exists) {
                s3.deleteObject(new DeleteObjectRequest(config.getString(AWS_BUCKET), keyName));
                Logger.debug("This description image got deleted: " + keyName);
                countImagesInDescription++;
                keyName = JOB_DESCRIPTION_IMAGE_KEY + projectId + "-" + countImagesInDescription;
                exists = s3.doesObjectExist(config.getString(AWS_BUCKET), keyName);
            }
        } catch (Exception e) {
            Logger.debug("Could not remove the rest of description images from S3 bucket.");
            Logger.debug("" + e.getStackTrace());
        }
    }


    /**
     * This method receives a job id and deletes the job by inactivating it (set is_active field to be false).
     *
     * @param jobId given job Id
     * @return ok or not found
     */
    public Result deleteJob(Long jobId) {
        try {
            Job job = Job.find.query().where(Expr.and(Expr.or(Expr.isNull("is_active"),
                    Expr.eq("is_active", "True")), Expr.eq("id", jobId))).findOne();
            if (job == null) {
                Logger.debug("In JobController deleteJob(), cannot find job: " + jobId);
                return notFound("From backend JobController, job not found with id: " + jobId);
            }

            job.setIsActive("False");
            job.save();
            return ok();
        } catch (Exception e) {
            Logger.debug("Job cannot be deleted for exception: " + e.toString());
            return Common.badRequestWrapper("Cannot delete job for id: " + jobId);
        }
    }


    public Result getIdByName(String name) {
        try {
            List<Job> jobList = Job.find.query().where().eq("title", name).findList();
            return ok(Json.toJson(jobList.get(0).getId()));
        } catch (Exception e) {
            Logger.debug("JobController.getIdByName() exception: " + e.toString());
            return internalServerError("JobController.getIdByName() exception: " + e.toString());
        }
    }


    public Result checkJobExist(Long jobId) {
        try {
            Job job = Job.find.byId(jobId);
            ObjectNode objectNode = Json.newObject();

            if (job == null) {
                objectNode.put("notExisted", "Job does not exist");
            } else {
                objectNode.put("existed", jobId);
            }
            return ok(objectNode);
        } catch (Exception e) {
            Logger.debug("JobController.checkJobExist exception: " + e.toString());
            return internalServerError("JobController.checkJobExist exception: " + e.toString());
        }
    }


    /**
     * Convert a JSON string to pretty print version
     *
     * @param jsonString
     * @return
     */
    public String toPrettyFormat(String jsonString) {
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(jsonString).getAsJsonObject();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(json);

        return prettyJson;
    }

}
