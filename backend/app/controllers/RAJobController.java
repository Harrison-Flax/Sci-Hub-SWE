package controllers;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import io.ebean.Expr;
import models.*;
import models.rest.RESTResponse;

import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import services.RAJobService;
import utils.Common;
import utils.EmailUtils;

import javax.inject.Inject;
import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.util.*;

import static utils.Constants.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

public class RAJobController extends Controller {
    public static final String RAJOB_DEFAULT_SORT_CRITERIA = "title";
    public static final String RAJOB_DESCRIPTION_IMAGE_KEY = "rajobDescriptionImage-";
    public static final String RAJOB_IMAGE_KEY = "jobImage-";

    private final RAJobService rajobService;

    @Inject
    Config config;

    @Inject
    public RAJobController(RAJobService rajobService) {
        this.rajobService = rajobService;
    }

    /************************************************* Add RAJob *******************************************************/
    /**
     * This method intends to add an RA job into database.
     *
     * @return created status with RA job id created
     */
    public Result addRAJob() {
        try {
            JsonNode json = request().body().asJson();
            if (json == null) {
                Logger.debug("RA job information not saved, expecting Json data");
                return badRequest("RA job information not saved, expecting Json data");
            }

            RAJob rajob = Json.fromJson(json, RAJob.class);
            System.out.println("<<<<,,1.1 rajob to create:" + rajob.toString());
            rajob.setIsActive("True");
            rajob.setStatus("open");
            rajob.setCreateTime(new Date().toString());
            rajob.setUpdateTime(new Date().toString());
            rajob.save();

            String folderName = "rajob/";
            Long rajobId = rajob.getId();
            String tableName = "rajob";

            return ok(Json.toJson(rajob.getId()).toString());
        } catch (Exception e) {
            Logger.debug("RA job cannot be added: " + e.toString());
            return badRequest("RA job not saved: ");
        }
    }
    /************************************************* End of Add RAJob ************************************************/

    /************************************************* Apply RAJob ******************************************************/
    /**
     * This method intends to apply a job into database.
     *
     * @return created status with job id created
     */
    public Result applyRAJob(Long rajobId) {
        try {
            JsonNode json = request().body().asJson();
            if (json == null) {
                Logger.debug("RAJob information not saved, expecting Json data");
                return badRequest("RAJob information not saved, expecting Json data");
            }

            RAJobApplication rajobApplication = Json.fromJson(json, RAJobApplication.class);
            rajobApplication.setIsActive("True");
            rajobApplication.setStatus("open");
            rajobApplication.setCreatedTime(new Date().toString());
            System.out.println("backend RA job application info: " + json);
            rajobApplication.save();

            String folderName = "rajobApplication/";
            String tableName = "rajob_application";

            return ok(Json.toJson(rajobApplication.getId()).toString());
        } catch (Exception e) {
            Logger.debug("Job cannot be added: " + e.toString());
            return badRequest("Job not applied: ");
        }
    }
    /************************************************* End of Apply Job ***********************************************/

/************************************************* offer student a RAJob ******************************************************/
    /**
     * This method intends to apply a job into database.
     *
     * @return created status with job id created
     */

    public Result giveRAJobOffertoStudent(Long rajobApplicationId) {
        try {
            System.out.println("Updating RA Job Application status...");
            JsonNode json = request().body().asJson();
            System.out.println("RAJobApplicationId: " + rajobApplicationId);

            if (json == null || !json.has("status")) {
                System.out.println("Job Status did not update, expecting Json data with 'status'");
                Logger.debug("Job Status did not update, expecting Json data with 'status'");
                return badRequest("Job Status did not update, expecting Json data with 'status'");
            }

            RAJobApplication rajobApplication = RAJobApplication.find.byId(rajobApplicationId);
            if (rajobApplication == null) {
                Logger.debug("RAJobApplication not found with id: " + rajobApplicationId);
                return notFound("RAJobApplication not found.");
            }
            System.out.println("Status update to: " + json.get("status").asText());
            rajobApplication.setStatus(json.get("status").asText());
            rajobApplication.update();
            return ok(Json.toJson(rajobApplication));
        } catch (Exception e) {
            Logger.debug("Error updating RAJobApplication with id: " + rajobApplicationId + " - " + e.toString());
            e.printStackTrace();
            return badRequest("Error updating RAJobApplication with id: " + rajobApplicationId);
        }
    }

    /************************************************* End of offer student a Job ***********************************************/


    /************************************************* Update RAJob ****************************************************/
    /**
     * This method intends to update RA job information except picture.
     *
     * @param rajobId
     * @return
     */
    public Result updateRAJob(Long rajobId) {
        try {
            JsonNode json = request().body().asJson();
            if (json == null) {
                Logger.debug("RA job information not saved, expecting Json data from RAJobController.updateRAJob");
                return badRequest("RA job information not saved, expecting Json data");
            }

            RAJob existingRAJob = RAJob.find.byId(rajobId);
            if (existingRAJob == null) {
                Logger.debug("RAJob not found with id: " + rajobId);
                return notFound("RAJob not found with id: " + rajobId);
            }

            RAJob updatedRAJob = Json.fromJson(json, RAJob.class);

            String folderName = "rajob/";
            String tableName = "rajob";

            existingRAJob.setUpdateTime(new Date().toString());
            existingRAJob.setStatus("updated");

            existingRAJob.setTitle(updatedRAJob.getTitle());
            existingRAJob.setGoals(updatedRAJob.getGoals());
            existingRAJob.setMinSalary(updatedRAJob.getMinSalary());
            existingRAJob.setMaxSalary(updatedRAJob.getMaxSalary());
            existingRAJob.setRaTypes(updatedRAJob.getRaTypes());
            existingRAJob.setShortDescription(updatedRAJob.getShortDescription());
            existingRAJob.setLongDescription(updatedRAJob.getLongDescription());
            existingRAJob.setFields(updatedRAJob.getFields());
            existingRAJob.setPublishDate(updatedRAJob.getPublishDate());
            existingRAJob.setPublishYear(updatedRAJob.getPublishYear());
            existingRAJob.setPublishMonth(updatedRAJob.getPublishMonth());
            existingRAJob.setImageURL(updatedRAJob.getImageURL());
            existingRAJob.setUrl(updatedRAJob.getUrl());
            existingRAJob.setOrganization(updatedRAJob.getOrganization());
            existingRAJob.setLocation(updatedRAJob.getLocation());
            existingRAJob.setRequiredExpertise(updatedRAJob.getRequiredExpertise());
            existingRAJob.setPreferredExpertise(updatedRAJob.getPreferredExpertise());
            existingRAJob.setNumberOfPositions(updatedRAJob.getNumberOfPositions());
            existingRAJob.setExpectedStartDate(updatedRAJob.getExpectedStartDate());
            existingRAJob.setExpectedTimeDuration(updatedRAJob.getExpectedTimeDuration());

            existingRAJob.update();
            return ok(Json.toJson(existingRAJob));
        } catch (Exception e) {
            Logger.debug("RAJob not updated with id: " + rajobId + " due to exception: " + e.toString());
            return badRequest("RAJob not updated: " + rajobId);
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

    public Result rajobUpdateStatue(Long rajobId) {
        try {
            System.out.println("get RA Job update info..");
            JsonNode json = request().body().asJson();
            System.out.println("get RA Job update info...." + rajobId);
            if (json == null) {
                Logger.debug("Job Status did not updated, expecting Json data from JobController.updateJob");
                return badRequest("Job Status did not updated, expecting Json data");
            }

            RAJob updatedRAJob = RAJob.find.byId(rajobId);
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
            updatedRAJob.setStatus(json.get("status").asText());
            updatedRAJob.update();
            return ok(Json.toJson(updatedRAJob));
        } catch (Exception e) {
            Logger.debug("Job Profile not saved with id: " + rajobId + " with exception: " + e.toString());
            return badRequest("Job Profile not saved: " + rajobId);
        }
    }

    public Result closeRAJob(Long rajobId) {
        try {
            if (rajobId == null) {
                Logger.debug("RA job ID is null in RAJobController.closeRAJob");
                return badRequest("RA job ID cannot be null");
            }

            // Fetch the RAJob by ID
            RAJob raJob = RAJob.find.byId(rajobId);
            if (raJob == null) {
                Logger.debug("RA job not found with ID: " + rajobId);
                return notFound("RA job not found with ID: " + rajobId);
            }

            raJob.setStatus("closed");
            raJob.update();

            Logger.debug("RA job with ID: " + rajobId + " successfully closed.");
            return ok(Json.toJson(raJob));
        } catch (Exception e) {
            Logger.debug("Failed to close RA job with ID: " + rajobId + ", exception: " + e.toString());
            return internalServerError("Failed to close RA job: " + e.getMessage());
        }
    }

    /**
     * Delete job image by job id.
     *
     * @param rajobId
     * @return
     */
    public Result deleteRAJobImage(Long rajobId) {
        if (rajobId == null) {
            return Common.badRequestWrapper("RA job id is null thus cannot delete image for it.");
        }
        try {
            RAJob rajob = RAJob.find.byId(rajobId);
            if (rajob != null) {
                Common.deleteFileFromS3(config, "rajob", "Image", rajobId);
                rajob.setImageURL("");
                rajob.save();
                return ok("RAJob image deleted successfully for RA job id: " + rajobId);
            } else {
                return Common.badRequestWrapper("Cannot find RA job thus cannot delete image for it.");
            }
        } catch (Exception e) {
            Logger.debug("Cannot delete RA job image for exception:" + e.toString());
            return Common.badRequestWrapper("Cannot delete RA job picture.");
        }
    }

    /**
     * This method intends to delete job pdf by job id
     *
     * @param rajobId
     * @return
     */
    public Result deleteRAJobPDF(Long rajobId) {
        try {
            RAJob tJ = RAJob.find.byId(rajobId);
            Common.deleteFileFromS3(config, "job", "Pdf", rajobId);
            tJ.setPdf("");
            tJ.save();
        } catch (Exception e) {
            Logger.debug("Failed to set pdf for RA job: " + e.toString());
            return Common.badRequestWrapper("Failed to add pdf to the RA job");
        }

        return ok("success");
    }
    /************************************************* End of Update RA Job ********************************************/

    /************************************************* RAJob List ******************************************************/
    /**
     * Gets a list of all the RA jobs based on optional offset and limit and sort
     *
     * @param pageLimit    shows the number of rows we want to receive
     * @param pageNum      shows the page number
     * @param sortCriteria shows based on what column we want to sort the data
     * @return a list of RA jobs
     * // TODO: Clean Common utitlity class for getSortCriteria(), not always register_time_stamp
     */
    public Result rajobList(Long userId, Integer pageLimit, Integer pageNum, Optional<String> sortCriteria) {
        List<RAJob> activeRAJobs = new ArrayList<>();

        Set<Long> rajobIds = new HashSet<>();
        List<RAJob> rajobs;
        String sortOrder = Common.getSortCriteria(sortCriteria, RAJOB_DEFAULT_SORT_CRITERIA);

        int offset = pageLimit * (pageNum - 1);
        try {
            User user = User.find.byId(userId);


            activeRAJobs = RAJob.find.query().where().eq("is_active", ACTIVE).ne("status", "closed").findList();
            for (RAJob rajob : activeRAJobs) {
                rajobIds.add(rajob.getId());
            }

            if (sortOrder.equals("id") || sortOrder.equals("access_times"))
                rajobs = RAJob.find.query().where().in("id", rajobIds).order().desc(sortOrder)
                        .findList();
            else
                rajobs = RAJob.find.query().where().in("id", rajobIds).orderBy(sortOrder)
                        .findList();

            // **modify updatetime format 20250205 wx**
            SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEE MMM dd yyyy", Locale.ENGLISH);

            for (RAJob rajob : rajobs) {
                String rawUpdateTime = rajob.getUpdateTime();
                if (rawUpdateTime != null && !rawUpdateTime.isEmpty()) {
                    try {
                        Date parsedDate = inputFormat.parse(rawUpdateTime);
                        String formattedTime = outputFormat.format(parsedDate);
                        rajob.setUpdateTime(formattedTime);
                    } catch (ParseException e) {
                        Logger.warn("Error parsing updateTime: " + rawUpdateTime);
                    }
                }
            }

            RESTResponse response = rajobService.paginateResults(rajobs, Optional.of(offset), Optional.
                    of(pageLimit), sortOrder);
            return ok(response.response());
        } catch (Exception e) {
            Logger.debug("RAJobController.jobList() exception: " + e.toString());
            return internalServerError("RAJobController.jobList() exception: " + e.toString());
        }
    }

    /************************************************* End of RAJob List ***********************************************/

    /************************************************* Get RAJob *******************************************************/
    /**
     * Get an RA job detail by the RA job id
     *
     * @param rajobId RA job Id
     * @return ok if the RA job is found; badRequest if the RA job is not found
     */
    public Result getRAJobById(Long rajobId) {
        try {
            // Fetch the RA job details
            RAJob rajob = RAJob.find.query().where().eq("id", rajobId).findOne();
            if (rajob == null) {
                return notFound("RAJob not found with id: " + rajobId);
            }

            // Convert RAJob to JSON and add extra fields directly
            ObjectNode response = (ObjectNode) Json.toJson(rajob);

            // response rajobApplicationId list, avoid applying same rajob
            List<RAJobApplication> applications = RAJobApplication.find.query()
                    .where().eq("appliedRAJob.id", rajobId)
                    .findList();


            Set<Long> uniqueApplicantIds = applications.stream()
                    .map(app -> app.getApplicant() != null ? app.getApplicant().getId() : null)
                    .filter(id -> id != null)
                    .collect(Collectors.toSet());

            response.putPOJO("rajobApplicationIdList", uniqueApplicantIds);

            return ok(response);
        } catch (Exception e) {
            Logger.debug("RAJobController.getRAJobById() exception : " + e.toString());
            return internalServerError("Internal Server Error RAJobController.getRAJobById() exception: " +
                    e.toString());
        }
    }

    /**
     * This method returns specific RA job application from RA job application info table given the application id (the application of the RA job)
     *
     * @param rajobApplicationId the ra job application Id
     * @return specific ra job application.
     */
    public Result getRAJobApplicationById(Long rajobApplicationId) {
        if (rajobApplicationId == null) {
            return Common.badRequestWrapper("rajobApplicationId is null or empty.");
        }

        if (rajobApplicationId == 0) return ok(Json.toJson(null));  // jobId=0 means SMU-Sci-Hub job, not stored in DB

        try {
            RAJobApplication rajobApplication = RAJobApplication.find.query().where().eq("id", rajobApplicationId).findOne();
            return ok(Json.toJson(rajobApplication));
        } catch (Exception e) {
            Logger.debug("RAJobController.getRAJobApplicationById() exception : " + e.toString());
            return internalServerError("Internal Server Error JobController.getRAJobApplicationById() exception: " +
                    e.toString());
        }
    }


    /**
     * This method returns all RA jobs from RA job info table given the publisher id (the publisher of the RA job)
     *
     * @param userId the publisher Id
     * @return all RA jobs.
     */
    public Result getRAJobsByPublisher(Long userId) {
        try {

            List<RAJob> rajobs = RAJob.find.query().where().eq("rajob_publisher_id", userId).findList();
            for(RAJob rajob : rajobs){
                int numOfApplicants = RAJobApplication.find.query().where().eq("rajob_id", rajob.getId()).findCount();
                rajob.setNumberOfApplicants(numOfApplicants);
            }
            ArrayNode jobArray = Common.objectList2JsonArray(rajobs);
            return ok(jobArray);
        } catch (Exception e) {
            Logger.debug("RAJobController.getRAJobsByPublisher exception: " + e.toString());
            return internalServerError("RAJobController.getRAJobsByPublisher exception: " + e.toString());
        }
    }

    /**
     * This method returns all RA jobs applied by a specific applicant given the applicant's userId.
     *
     * @param userId the applicant's userId
     * @return all RA jobs applied by the applicant.
     */
    public Result getRAJobsByApplicant(Long userId) {
        try {
            List<RAJobApplication> applications = RAJobApplication.find.query()
                    .where().eq("applicant.id", userId).findList();

            ArrayNode resultArray = Json.newArray();
            for (RAJobApplication application : applications) {
                RAJob rajob = application.getAppliedRAJob();

                if (rajob != null) {
                    ObjectNode jobJson = Json.newObject();
                    jobJson.put("rajobApplicationId", application.getId());
                    jobJson.put("id", rajob.getId());
                    jobJson.put("title", rajob.getTitle());
                    jobJson.put("shortDescription", rajob.getShortDescription());
                    jobJson.put("fields", rajob.getFields());
                    jobJson.put("status", rajob.getStatus());
                    jobJson.put("organization", rajob.getOrganization());
                    jobJson.put("location", rajob.getLocation());
                    jobJson.put("rajobApplicationStatus", application.getStatus());

                    resultArray.add(jobJson);
                }
            }

            return ok(resultArray);
        } catch (Exception e) {
            Logger.debug("RAJobController.getRAJobsByApplicant exception: " + e.toString());
            return internalServerError("RAJobController.getRAJobsByApplicant exception: " + e.toString());
        }
    }


    /************************************************* End of Get RAJob By applicant's id **********************************************/


    /**
     * Checks if a RA job name can be used.
     *
     * @return this RA job title is valid
     */
    public Result checkRAJobNameAvailability() {
        JsonNode json = request().body().asJson();
        if (json == null) {
            Logger.info("Cannot check RA job name, expecting Json data");
            return badRequest("Cannot check RA job name, expecting Json data");
        }
        String title = json.path("title").asText();
        if (title == null || title.isEmpty()) {
            Logger.info("RA job title is null or empty");
            return Common.badRequestWrapper("RA job title is null or empty.");
        }
        try {
            List<RAJob> rajobs = RAJob.find.query().where().eq("title", title).findList();
            if (rajobs == null || rajobs.size() == 0) {
                return ok("This new RA job name can be used");
            } else {
                return Common.badRequestWrapper("This RA job name has been used.");
            }
        } catch (Exception e) {
            return internalServerError("RAJobController.checkRAJobNameAvailability exception: " +
                    e.toString());
        }
    }


    /**
     * This method intends to return the publisher of a RA job.
     *
     * @param rajobId RA job id
     * @return json of the publisher of the job posting
     */
    public Result getRAJobPublisher(Long rajobId) {
        try {
            RAJob rajob = RAJob.find.byId(rajobId);
            if (rajob == null) {
                return Common.badRequestWrapper("No RA Job found with the given RA job id");
            }
            return ok(Json.toJson(rajob.getRajobPublisher()));
        } catch (Exception e) {
            Logger.debug("RAJobController.getRaJobPublisher() exception: " + e.toString());
            return internalServerError("RAJobController.getRaJobPublisher() exception: " + e.toString());
        }
    }


    /**
     * Check if the RA job is search result
     *
     * @param rajob            RA Job being checked
     * @param title
     * @param goals
     * @param location
     * @param shortDescription
     * @return if the RA job is search result.
     */
    private boolean isMatchedRAJob(RAJob rajob, String title, String goals, String location, String shortDescription) {
        boolean titleInTitle = false;
        boolean goalInGoal = false;
        boolean locationInLocation = false;
        boolean descriptionInDescription = false;
        for (String titleSubWord : title.split(" ")) {
            titleSubWord = titleSubWord.trim();
            titleInTitle = title.equals("") || (rajob.getTitle() != null && rajob.getTitle().toLowerCase().
                    indexOf(titleSubWord.toLowerCase()) >= 0);
            if (titleInTitle)
                break;
        }
        for (String goalSubWord : goals.split(" ")) {
            goalSubWord = goalSubWord.trim();
            goalInGoal = goals.equals("") || (rajob.getGoals() != null && rajob.getGoals().toLowerCase().
                    indexOf(goalSubWord.toLowerCase()) >= 0);
            if (goalInGoal)
                break;
        }
//        for (String locationSubWord : location.split(" ")) {
//            locationSubWord = locationSubWord.trim();
//            locationInLocation = location.equals("") || (rajob.getLocation() != null && rajob.getLocation().
//                    toLowerCase().indexOf(locationSubWord.toLowerCase()) >= 0);
//            if (locationInLocation)
//                break;
//        }
        for (String descriptionSubWord : shortDescription.split(" ")) {
            descriptionSubWord = descriptionSubWord.trim();
            descriptionInDescription = shortDescription.equals("") ||
                (rajob.getShortDescription() != null && rajob.getShortDescription().toLowerCase().contains(descriptionSubWord.toLowerCase())) ||
                (rajob.getLongDescription() != null && rajob.getLongDescription().toLowerCase().contains(descriptionSubWord.toLowerCase()));
            if (descriptionInDescription)
                break;
        }
//        return titleInTitle && goalInGoal && locationInLocation && descriptionInDescription;
        return titleInTitle && goalInGoal && descriptionInDescription;

    }

    /**
     * Filter the RA jobs based on title, goal, location, short description
     *
     * @param title            RA Job list being filtered
     * @param goals
     * @param location
     * @param shortDescription
     * @return the list of filtered RA jobs.
     */
    private List<RAJob> matchedRAJobList(List<RAJob> rajobList, String title, String goals, String location,
                                         String shortDescription) {
        List<RAJob> results = new ArrayList<>();
        for (RAJob rajob : rajobList) {
            if (isMatchedRAJob(rajob, title, goals, location, shortDescription))
                results.add(rajob);
        }
        return results;
    }


    /**
     * Find RA jobs by multiple condition, including title, goal, location, etc.
     *
     * @return jobs that match the condition
     * TODO: How to handle more conditions???
     */
    public Result searchRAJobsByCondition() {
        String result = null;
        try {
            JsonNode json = request().body().asJson();
            List<RAJob> rajobs = new ArrayList<>();
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
                List<RAJob> potentialRAJobs = RAJob.find.query().where().eq("is_active", ACTIVE).
                        findList();
                rajobs = matchedRAJobList(potentialRAJobs, title, goals, location, description);

            } else {
                List<RAJob> tmpRAJobs = RAJob.find.query().where().eq("is_active", ACTIVE).ne("status", "closed").findList();
                String[] keywordList = keywords.toLowerCase().trim().split(" ");

                for (String keyword : keywordList) {
                    keyword = keyword.toLowerCase();
                    for (RAJob rajob : tmpRAJobs) {
                        if ((rajob.getTitle() != null && rajob.getTitle().toLowerCase().contains(keyword)) ||
                                (rajob.getGoals() != null && rajob.getGoals().toLowerCase().contains(keyword)) ||
                                (rajob.getLocation() != null && rajob.getLocation().toLowerCase().contains(keyword))
                                || (rajob.getShortDescription() != null && rajob.getShortDescription().toLowerCase().contains
                                (keyword))) {
                            rajobs.add(rajob);
                        }
                    }
                }
            }
            //If not found
            if (rajobs == null || rajobs.size() == 0) {
                Logger.info("RA Jobs not found with search conditions");
                return notFound("RA Jobs not found with conditions");
            }
            Set<Long> rajobsIdSet = new HashSet<>();
            List<RAJob> filteredJobs = new ArrayList<>();
            for (RAJob rajob : rajobs) {
                if (!rajobsIdSet.contains(rajob.getId())) {
                    filteredJobs.add(rajob);
                    rajobsIdSet.add(rajob.getId());
                }
            }
            JsonNode jsonNode = Json.toJson(filteredJobs);
            result = jsonNode.toString();
        } catch (Exception e) {
            Logger.debug("RAJobController.searchRAJobsByCondition() exception: " + e.toString());
            return internalServerError("RAJobController.searchRAJobsByCondition() exception: " +
                    e.toString());
        }

        return ok(result);
    }


    /**
     * This method intends to set RA job image by job id
     *
     * @param rajobId
     * @return TODO: change?
     */
    public Result setImage(Long rajobId) {
        if (request().body() == null || request().body().asRaw() == null) {
            return Common.badRequestWrapper("The request cannot be empty");
        }
        try {
            RAJob tJ = RAJob.find.byId(rajobId);
            String url = Common.uploadFile(config, "rajob", "Image", rajobId, request());
            //tJ.setImageUrl(url);
            tJ.save();
        } catch (Exception e) {
            Logger.debug("Failed to set image for RA job: " + e.toString());
            return Common.badRequestWrapper("Failed to add image to the RA job");
        }

        // Return the app image.
        return ok("success");
    }

    /**
     * This method intends to set RA job pdf by RA job id
     *
     * @param rajobId
     * @return
     */
    public Result setPDF(Long rajobId) {
        if (request().body() == null || request().body().asRaw() == null) {
            return Common.badRequestWrapper("The request cannot be empty");
        }
        try {
            RAJob tJ = RAJob.find.byId(rajobId);
            String url = Common.uploadFile(config, "rajob", "Pdf", rajobId, request());
            tJ.setPdf(url);
            tJ.save();
        } catch (Exception e) {
            Logger.debug("Failed to set pdf for RA job: " + e.toString());
            return Common.badRequestWrapper("Failed to add pdf to the RA job");
        }

        // Return the app pdf.
        return ok("success");
    }


    /**
     * This method receives an RA job id and the number of images in the project's description and checks the s3 bucket
     * to remove the images having id more than the project's description image count (This is because we can remove the
     * deleted description images from S3)
     *
     * @param projectId                project ID
     * @param countImagesInDescription number of images in the project's description
     */
    private void removeDeletedImagesInDescriptionFromS3(long projectId, int countImagesInDescription) {
        try {
            countImagesInDescription++;
            String keyName = RAJOB_DESCRIPTION_IMAGE_KEY + projectId + "-" + countImagesInDescription;
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
                keyName = RAJOB_DESCRIPTION_IMAGE_KEY + projectId + "-" + countImagesInDescription;
                exists = s3.doesObjectExist(config.getString(AWS_BUCKET), keyName);
            }
        } catch (Exception e) {
            Logger.debug("Could not remove the rest of description images from S3 bucket.");
            Logger.debug("" + e.getStackTrace());
        }
    }


    /**
     * This method receives an RA job id and deletes the RA job by inactivating it (set is_active field to be false).
     *
     * @param rajobId given RA job Id
     * @return ok or not found
     */
    public Result deleteRAJob(Long rajobId) {
        try {
            RAJob rajob = RAJob.find.query().where(Expr.and(Expr.or(Expr.isNull("is_active"),
                    Expr.eq("is_active", "True")), Expr.eq("id", rajobId))).findOne();
            if (rajob == null) {
                Logger.debug("In RAJobController deleteRAJob(), cannot find RA job: " + rajobId);
                return notFound("From backend RAJobController, RA job not found with id: " + rajobId);
            }

            rajob.setIsActive("False");
            rajob.save();
            return ok();
        } catch (Exception e) {
            Logger.debug("RA Job cannot be deleted for exception: " + e.toString());
            return Common.badRequestWrapper("Cannot delete RA job for id: " + rajobId);
        }
    }


    public Result getIdByName(String name) {
        try {
            List<RAJob> rajobList = RAJob.find.query().where().eq("title", name).findList();
            return ok(Json.toJson(rajobList.get(0).getId()));
        } catch (Exception e) {
            Logger.debug("RAJobController.getIdByName() exception: " + e.toString());
            return internalServerError("RAJobController.getIdByName() exception: " + e.toString());
        }
    }


    public Result checkRAJobExist(Long rajobId) {
        try {
            RAJob rajob = RAJob.find.byId(rajobId);
            ObjectNode objectNode = Json.newObject();

            if (rajob == null) {
                objectNode.put("notExisted", "RA Job does not exist");
            } else {
                objectNode.put("existed", rajobId);
            }
            return ok(objectNode);
        } catch (Exception e) {
            Logger.debug("RAJobController.checkJobExist exception: " + e.toString());
            return internalServerError("RAJobController.checkJobExist exception: " + e.toString());
        }
    }
//
//
//    /**
//     * Convert a JSON string to pretty print version
//     *
//     * @param jsonString
//     * @return
//     */
//    public String toPrettyFormat(String jsonString) {
//        JsonParser parser = new JsonParser();
//        JsonObject json = parser.parse(jsonString).getAsJsonObject();
//
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//        String prettyJson = gson.toJson(json);
//
//        return prettyJson;
//    }

    public Result sendOfferEmail() throws UnsupportedEncodingException {
        Logger.info("sendOfferEmail: Begin processing send offer email.");
        JsonNode json = request().body().asJson();
        String rajobApplicationId = json.get("rajobApplicationId").asText();
        String ccString = json.path("ccSelected").asText();
        String[] ccList = new String[0];
        if(ccString != null && !ccString.trim().isEmpty()) {
            ccList = ccString.split("\\s*,\\s*");
        }
        Logger.debug("sendOfferEmail: ccList contents - " + Arrays.toString(ccList));
        Logger.info("sendOfferEmail: Received parameters - rajobApplicationId: " + rajobApplicationId);

        RAJobApplication thisApplication = RAJobApplication.find.byId(Long.parseLong(rajobApplicationId));

        Long rajobId = thisApplication.getAppliedRAJob().getId();
        Long recipientId = thisApplication.getApplicant().getId();

        RAJob thisRajob = RAJob.find.byId(rajobId);
        User thisRecipient = User.find.byId(recipientId);

        if (thisRajob == null) {
            Logger.error("sendOfferEmail: No rajob found with id: " + rajobId);
            return badRequest("User not found");
        }
        if (thisRecipient == null) {
            Logger.error("sendOfferEmail: No user found with id: " + recipientId);
            return badRequest("User not found");
        }

        String position = thisRajob.getTitle();
        String email = thisRecipient.getEmail();

        String body = "Dear Applicant,\n\n"
                + "Your application for Position " + position + " has been reviewed and approved by the professor. Please reach out to the professor within five working days to discuss the details of the position.\n\n"
                + "This position will be reserved for you for five days. After that period, it will be made available to the public again.\n\n"
                + "Thank you for your interest. We look forward to your response.\n\n"
                + "Best Regards, \n\n"
                + "SMU-Lyle-Sci-Hub Group";
        Logger.info("sendOfferEmail: Email body constructed: " + body);

        String subject = "No-reply: Your [" + position + "] Application Has Been Approved";

        try {
            // Send individual mail.
            EmailUtils.sendMail(
                    config,
                    new String[]{ email },  // to
                    ccList,                 // cc
                    new String[]{},         // bcc
                    subject,
                    body
            );
            Logger.info("Send offer email email succeeded!");
        } catch (MessagingException e) {
            e.printStackTrace();
            Logger.error("Send offer email email failed!");
        }
        return ok();
    }

    public Result sendRAJobPostedEmail() {
        Logger.info("sendRAJobPostedEmail: Begin processing RA job posted email.");
        JsonNode json = request().body().asJson();

        if (json == null || !json.has("rajobId") || !json.has("students")) {
            Logger.error("sendRAJobPostedEmail: Missing required fields.");
            return badRequest("Missing required fields: rajobId and students");
        }

        Long rajobId = json.get("rajobId").asLong();
        RAJob thisRajob = RAJob.find.byId(rajobId);
        if (thisRajob == null) {
            Logger.error("sendRAJobPostedEmail: No RAJob found with id = " + rajobId);
            return badRequest("RAJob not found");
        }

        String jobTitle = thisRajob.getTitle();
        List<String> students = new ArrayList<>();
        for (JsonNode node : json.get("students")) {
            students.add(node.asText());
        }
        List<String> ccList = new ArrayList<>();
        Logger.debug("sendRAJobPostedEmail: raw JSON = {}", json);

        if (json.has("ccSelected") && json.get("ccSelected").isArray()) {
            Logger.debug("sendRAJobPostedEmail: Found ccSelected array of size {}",
                    json.get("ccSelected").size());

            for (JsonNode ccNode : json.get("ccSelected")) {
                String ccEmail = ccNode.asText();
                Logger.debug("sendRAJobPostedEmail: ccSelected item = {}", ccEmail);
                ccList.add(ccEmail);
            }
        } else {
            Logger.debug("sendRAJobPostedEmail: No ccSelected field or it's not an array.");
        }


        String subject = "New RA Position Available – " + jobTitle;
        String body = "Dear Students,\n\n"
                + "A new Research Assistant (RA) position, \"" + jobTitle + "\", has been posted on the SMU-Lyle-Sci-Hub portal.\n"
                + "To review the qualifications and apply, please log in to the system here:\n"
                + "https://scihub.smudsi.org/login\n"
                + "If you submit your application in the system, the portal will automatically email the corresponding faculty to review your materials.\n"
                + "If you don’t already have an account, please sign up using your SMU NET ID (e.g., Julia@smu.edu).\n"
                + "For any questions, feel free to reach out.\n\n"
                + "Best Regards,\nSMU-Lyle-Sci-Hub Group";

        Logger.info("sendRAJobPostedEmail: Email body constructed:\n" + body);

        try {
            EmailUtils.sendMail(
                    config,
                    new String[]{"noreply@smu.edu"},   // from
                    ccList.toArray(new String[0]),                   // cc
                    students.toArray(new String[0]),   // bcc
                    subject,
                    body
            );
            Logger.info("sendRAJobPostedEmail: Email sent successfully.");
            return ok("RA job posted email sent successfully");
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("sendRAJobPostedEmail: Failed to send email.");
            return internalServerError("Failed to send RA job posted email");
        }
    }

    public Result sendRAJobAppliedEmail() {
        Logger.info("sendRAJobAppliedEmail: Begin processing RA job applied email.");
        JsonNode json = request().body().asJson();

        if (json == null || !json.has("rajobId")) {
            Logger.error("sendRAJobAppliedEmail: Missing rajobId in request.");
            return badRequest("Missing rajobId");
        }

        Long rajobId = json.get("rajobId").asLong();
        RAJob thisRajob = RAJob.find.byId(rajobId);
        if (thisRajob == null) {
            Logger.error("sendRAJobAppliedEmail: No RAJob found with id = " + rajobId);
            return badRequest("RAJob not found");
        }

        String jobTitle = thisRajob.getTitle();
        User professor = thisRajob.getRajobPublisher(); // assuming getter is getRajobPublisher()
        if (professor == null) {
            Logger.error("sendRAJobAppliedEmail: RAJobPublisher is null for job id = " + rajobId);
            return badRequest("RAJob publisher not found");
        }

        String professorEmail = professor.getEmail();
        String professorLastName = professor.getLastName();  // assuming getter is getLastName()

        String subject = "[RA Application Notice] New applicants to your position: " + jobTitle;
        String body = "Dear Prof. " + professorLastName + ",\n\n"
                + "Students have responded to your RA position \"" + jobTitle + "\".\n"
                + "Please log in to the Lyle Sci-Hub system to see who have applied and review their qualifications.\n"
                + "https://scihub.smudsi.org/login\n"
                + "If you have any question, please send an email to Jennychen@smu.edu. We will be working hard to promote your RA positions.\n\n"
                + "Best Regards,\nSMU Lyle Sci-Hub Group";

        Logger.info("sendRAJobAppliedEmail: Email body constructed:\n" + body);

        try {
            EmailUtils.sendIndividualEmail(config, professorEmail, subject, body);
            Logger.info("sendRAJobAppliedEmail: Email sent to " + professorEmail);
            return ok("RA job applied email sent successfully");
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("sendRAJobAppliedEmail: Failed to send email.");
            return internalServerError("Failed to send RA job applied email");
        }
    }

    public Result listProfessorsJson() {
        List<ObjectNode> professors = User.find.query()
                .where().eq("userType", 1)
                .findList()
                .stream()
                .map(u -> {
                    ObjectNode o = Json.newObject();
                    o.put("id", u.getId());
                    o.put("userName", u.getUserName());
                    o.put("email", u.getEmail());
                    return o;
                })
                .collect(Collectors.toList());

        return ok(Json.toJson(professors));
    }
}
