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
import services.TAJobService;
import utils.Common;

import javax.inject.Inject;
import java.util.*;

import static utils.Constants.*;

public class TAJobController extends Controller {
    public static final String TAJOB_DEFAULT_SORT_CRITERIA = "title";
    public static final String TAJOB_DESCRIPTION_IMAGE_KEY = "tajobDescriptionImage-";
    public static final String TAJOB_IMAGE_KEY = "jobImage-";

    private final TAJobService tajobService;

    @Inject
    Config config;

    @Inject
    public TAJobController(TAJobService tajobService) {
        this.tajobService = tajobService;
    }

    /************************************************* Add TAJob *******************************************************/
    /**
     * This method intends to add an TA job into database.
     *
     * @return created status with TA job id created
     */
    public Result addTAJob() {
        try {
            JsonNode json = request().body().asJson();
            if (json == null) {
                Logger.debug("TA job information not saved, expecting Json data");
                return badRequest("TA job information not saved, expecting Json data");
            }

            TAJob tajob = Json.fromJson(json, TAJob.class);
            tajob.setIsActive("True");
            tajob.setStatus("open");

            tajob.save();
            System.out.println("Finish Add a new TA Job in Backend. " + tajob.toString());
            return ok(Json.toJson(tajob.getId()).toString());
        } catch (Exception e) {
            Logger.debug("TA job cannot be added: " + e.toString());
            return badRequest("TA job not saved: ");
        }
    }
    /************************************************* End of Add TAJob ***********************************************/

    /************************************************* Apply TAJob ****************************************************/
    /**
     * This method intends to apply a job into database.
     *
     * @return created status with job id created
     */
    public Result applyTAJob(Long tajobId) {
        try {
            JsonNode json = request().body().asJson();
            if (json == null) {
                Logger.debug("TAJob information not saved, expecting Json data");
                return badRequest("TAJob information not saved, expecting Json data");
            }

            TAJobApplication tajobApplication = Json.fromJson(json, TAJobApplication.class);
            tajobApplication.setIsActive("True");
            tajobApplication.setCreatedTime(new Date().toString());
            System.out.println("backend TA job application info: " + json);
            tajobApplication.save();
            return ok(Json.toJson(tajobApplication.getId()).toString());
        } catch (Exception e) {
            Logger.debug("Job cannot be added: " + e.toString());
            return badRequest("Job not applied: ");
        }
    }
    /************************************************* End of Apply Job ***********************************************/


    /************************************************* Update TAJob ***************************************************/
    /**
     * This method intends to update TA job information except picture.
     *
     * @patam tajobId
     * @return
     */
    public Result updateTAJob(Long tajobId) {
        try {
            JsonNode json = request().body().asJson();
            if (json == null) {
                Logger.debug("TA job information not saved, expecting Json data from TAJobController.updateTAJob");
                return badRequest("TA job information not saved, expecting Json data");
            }

            TAJob updatedTAJob = Json.fromJson(json, TAJob.class);
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

            updatedTAJob.update();
            return ok(Json.toJson(updatedTAJob));
        } catch (Exception e) {
            Logger.debug("TAJob Profile not saved with id: " + tajobId + " with exception: " + e.toString());
            return badRequest("TAJob Profile not saved: " + tajobId);
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

    public Result tajobUpdateStatue(Long tajobId) {
        try {
            System.out.println("get TA Job update info..");
            JsonNode json = request().body().asJson();
            System.out.println("get TA Job update info...." + tajobId);
            if (json == null) {
                Logger.debug("Job Status did not updated, expecting Json data from JobController.updateJob");
                return badRequest("Job Status did not updated, expecting Json data");
            }

            TAJob updatedTAJob = TAJob.find.byId(tajobId);
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
            updatedTAJob.setStatus(json.get("status").asText());
            updatedTAJob.update();
            return ok(Json.toJson(updatedTAJob));
        } catch (Exception e) {
            Logger.debug("Job Profile not saved with id: " + tajobId + " with exception: " + e.toString());
            return badRequest("Job Profile not saved: " + tajobId);
        }
    }

    /**
     * Delete job image by job id.
     *
     * @patam tajobId
     * @return
     */
    public Result deleteTAJobImage(Long tajobId) {
        if (tajobId == null) {
            return Common.badRequestWrapper("TA job id is null thus cannot delete image for it.");
        }
        try {
            TAJob tajob = TAJob.find.byId(tajobId);
            if (tajob != null) {
                Common.deleteFileFromS3(config, "tajob", "Image", tajobId);
                tajob.setImageURL("");
                tajob.save();
                return ok("TAJob image deleted successfully for TA job id: " + tajobId);
            } else {
                return Common.badRequestWrapper("Cannot find TA job thus cannot delete image for it.");
            }
        } catch (Exception e) {
            Logger.debug("Cannot delete TA job image for exception:" + e.toString());
            return Common.badRequestWrapper("Cannot delete TA job picture.");
        }
    }

    /**
     * This method intends to delete job pdf by job id
     *
     * @patam tajobId
     * @return
     */
    public Result deleteTAJobPDF(Long tajobId) {
        try {
            TAJob tJ = TAJob.find.byId(tajobId);
            Common.deleteFileFromS3(config, "job", "Pdf", tajobId);
            tJ.setPdf("");
            tJ.save();
        } catch (Exception e) {
            Logger.debug("Failed to set pdf for TA job: " + e.toString());
            return Common.badRequestWrapper("Failed to add pdf to the TA job");
        }

        return ok("success");
    }
    /************************************************* End of Update TA Job ********************************************/

    /************************************************* TAJob List ******************************************************/
    /**
     * Gets a list of all the TA jobs based on optional offset and limit and sort
     *
     * @patam pageLimit    shows the number of rows we want to receive
     * @patam pageNum      shows the page number
     * @patam sortCriteria shows based on what column we want to sort the data
     * @return a list of TA jobs
     * // TODO: Clean Common utitlity class for getSortCriteria(), not always register_time_stamp
     */
    public Result tajobList(Long userId, Integer pageLimit, Integer pageNum, Optional<String> sortCriteria) {
        List<TAJob> activeTAJobs = new ArrayList<>();

        Set<Long> tajobIds = new HashSet<>();
        List<TAJob> tajobs;
        String sortOrder = Common.getSortCriteria(sortCriteria, TAJOB_DEFAULT_SORT_CRITERIA);

        int offset = pageLimit * (pageNum - 1);
        try {
            User user = User.find.byId(userId);


            activeTAJobs = TAJob.find.query().where().eq("is_active", ACTIVE).findList();
            for (TAJob tajob : activeTAJobs) {
                tajobIds.add(tajob.getId());
            }

            if (sortOrder.equals("id") || sortOrder.equals("access_times"))
                tajobs = TAJob.find.query().where().in("id", tajobIds).order().desc(sortOrder)
                        .findList();
            else
                tajobs = TAJob.find.query().where().in("id", tajobIds).orderBy(sortOrder)
                        .findList();
            RESTResponse response = tajobService.paginateResults(tajobs, Optional.of(offset), Optional.
                    of(pageLimit), sortOrder);
            return ok(response.response());
        } catch (Exception e) {
            Logger.debug("TAJobController.jobList() exception: " + e.toString());
            return internalServerError("TAJobController.jobList() exception: " + e.toString());
        }
    }

    /************************************************* End of TAJob List ***********************************************/

    /************************************************* Get TAJob *******************************************************/
    /**
     * Get an TA job detail by the TA job id
     *
     * @patam tajobId TA job Id
     * @return ok if the TA job is found; badRequest if the TA job is not found
     */
    public Result getTAJobById(Long tajobId) {
        if (tajobId == null) {
            return Common.badRequestWrapper("TA jobId is null or empty.");
        }

        if (tajobId == 0) return ok(Json.toJson(null));

        try {
            TAJob tajob = TAJob.find.query().where().eq("id", tajobId).findOne();
            return ok(Json.toJson(tajob));
        } catch (Exception e) {
            Logger.debug("TAJobController.getTAJobById() exception : " + e.toString());
            return internalServerError("Internal Server Error TAJobController.getTAJobById() exception: " +
                    e.toString());
        }
    }

    /**
     * This method returns specific TA job application from TA job application info table given the application id (the application of the TA job)
     *
     * @param tajobApplicationId the ta job application Id
     * @return specific ra job application.
     */
    public Result getTAJobApplicationById(Long tajobApplicationId) {
        if (tajobApplicationId == null) {
            return Common.badRequestWrapper("tajobApplicationId is null or empty.");
        }

        if (tajobApplicationId == 0) return ok(Json.toJson(null));  // jobId=0 means SMU-Sci-Hub job, not stored in DB

        try {
            TAJobApplication tajobApplication = TAJobApplication.find.query().where().eq("id", tajobApplicationId).findOne();
            return ok(Json.toJson(tajobApplication));
        } catch (Exception e) {
            Logger.debug("RAJobController.getRAJobApplicationById() exception : " + e.toString());
            return internalServerError("Internal Server Error JobController.getRAJobApplicationById() exception: " +
                    e.toString());
        }
    }

    /**
     * This method returns all TA jobs from TA job info table given the publisher id (the publisher of the TA job)
     *
     * @patam userId the publisher Id
     * @return all TA jobs.
     */
    public Result getTAJobsByPublisher(Long userId) {
        try {

            List<TAJob> tajobs = TAJob.find.query().where().eq("tajob_publisher_id", userId).findList();

            for(TAJob tajob : tajobs){
                int numOfApplicants = TAJobApplication.find.query().where().eq("tajob_id", tajob.getId()).findCount();
                tajob.setNumberOfApplicants(numOfApplicants);
            }
            ArrayNode jobArtay = Common.objectList2JsonArray(tajobs);
            return ok(jobArtay);
        } catch (Exception e) {
            Logger.debug("TAJobController.getTAJobsByPublisher exception: " + e.toString());
            return internalServerError("TAJobController.getTAJobsByPublisher exception: " + e.toString());
        }
    }
    /************************************************* End of Get TAJob ************************************************/

    /**
     * Checks if a TA job name can be used.
     *
     * @return this TA job title is valid
     */
    public Result checkTAJobNameAvailability() {
        JsonNode json = request().body().asJson();
        if (json == null) {
            Logger.info("Cannot check TA job name, expecting Json data");
            return badRequest("Cannot check TA job name, expecting Json data");
        }
        String title = json.path("title").asText();
        if (title == null || title.isEmpty()) {
            Logger.info("TA job title is null or empty");
            return Common.badRequestWrapper("TA job title is null or empty.");
        }
        try {
            List<TAJob> tajobs = TAJob.find.query().where().eq("title", title).findList();
            if (tajobs == null || tajobs.size() == 0) {
                return ok("This new TA job name can be used");
            } else {
                return Common.badRequestWrapper("This TA job name has been used.");
            }
        } catch (Exception e) {
            return internalServerError("TAJobController.checkTAJobNameAvailability exception: " +
                    e.toString());
        }
    }


    /**
     * This method intends to return the publisher of a TA job.
     *
     * @patam tajobId TA job id
     * @return json of the publisher of the job posting
     */
    public Result getTAJobPublisher(Long tajobId) {
        try {
            TAJob tajob = TAJob.find.byId(tajobId);
            if (tajob == null) {
                return Common.badRequestWrapper("No TA Job found with the given TA job id");
            }
            return ok(Json.toJson(tajob.getTajobPublisher()));
        } catch (Exception e) {
            Logger.debug("TAJobController.getRaJobPublisher() exception: " + e.toString());
            return internalServerError("TAJobController.getRaJobPublisher() exception: " + e.toString());
        }
    }


    /**
     * Check if the TA job is search result
     *
     * @patam tajob            TA Job being checked
     * @patam title
     * @patam goals
     * @patam location
     * @patam shortDescription
     * @return if the TA job is search result.
     */
    private boolean isMatchedTAJob(TAJob tajob, String title, String goals, String location, String shortDescription) {
        boolean titleInTitle = false;
        boolean goalInGoal = false;
        boolean locationInLocation = false;
        boolean descriptionInDescription = false;
        for (String titleSubWord : title.split(" ")) {
            titleSubWord = titleSubWord.trim();
            titleInTitle = title.equals("") || (tajob.getTitle() != null && tajob.getTitle().toLowerCase().
                    indexOf(titleSubWord.toLowerCase()) >= 0);
            if (titleInTitle)
                break;
        }
        for (String goalSubWord : goals.split(" ")) {
            goalSubWord = goalSubWord.trim();
            goalInGoal = goals.equals("") || (tajob.getGoals() != null && tajob.getGoals().toLowerCase().
                    indexOf(goalSubWord.toLowerCase()) >= 0);
            if (goalInGoal)
                break;
        }
        for (String locationSubWord : location.split(" ")) {
            locationSubWord = locationSubWord.trim();
            locationInLocation = location.equals("") || (tajob.getLocation() != null && tajob.getLocation().
                    toLowerCase().indexOf(locationSubWord.toLowerCase()) >= 0);
            if (locationInLocation)
                break;
        }
        for (String descriptionSubWord : shortDescription.split(" ")) {
            descriptionSubWord = descriptionSubWord.trim();
            descriptionInDescription = shortDescription.equals("") || (tajob.getShortDescription() != null &&
                    tajob.getShortDescription().toLowerCase().indexOf(descriptionSubWord.toLowerCase()) >= 0);
            if (descriptionInDescription)
                break;
        }
        return titleInTitle && goalInGoal && locationInLocation && descriptionInDescription;
    }

    /**
     * Filter the TA jobs based on title, goal, location, short description
     *
     * @patam title            TA Job list being filtered
     * @patam goals
     * @patam location
     * @patam shortDescription
     * @return the list of filtered TA jobs.
     */
    private List<TAJob> matchedTAJobList(List<TAJob> tajobList, String title, String goals, String location,
                                         String shortDescription) {
        List<TAJob> results = new ArrayList<>();
        for (TAJob tajob : tajobList) {
            if (isMatchedTAJob(tajob, title, goals, location, shortDescription))
                results.add(tajob);
        }
        return results;
    }


    /**
     * Find TA jobs by multiple condition, including title, goal, location, etc.
     *
     * @return jobs that match the condition
     * TODO: How to handle more conditions???
     */
    public Result searchTAJobsByCondition() {
        String result = null;
        try {
            JsonNode json = request().body().asJson();
            List<TAJob> tajobs = new ArrayList<>();
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
                List<TAJob> potentialTAJobs = TAJob.find.query().where().eq("is_active", ACTIVE).
                        findList();
                tajobs = matchedTAJobList(potentialTAJobs, title, goals, location, description);

            } else {
                List<TAJob> tmpTAJobs = TAJob.find.query().where().eq("is_active", ACTIVE).findList();
                String[] keywordList = keywords.toLowerCase().trim().split(" ");

                for (String keyword : keywordList) {
                    keyword = keyword.toLowerCase();
                    for (TAJob tajob : tmpTAJobs) {
                        if ((tajob.getTitle() != null && tajob.getTitle().toLowerCase().contains(keyword)) ||
                                (tajob.getGoals() != null && tajob.getGoals().toLowerCase().contains(keyword)) ||
                                (tajob.getLocation() != null && tajob.getLocation().toLowerCase().contains(keyword))
                                || (tajob.getShortDescription() != null && tajob.getShortDescription().toLowerCase().contains
                                (keyword))) {
                            tajobs.add(tajob);
                        }
                    }
                }
            }
            //If not found
            if (tajobs == null || tajobs.size() == 0) {
                Logger.info("TA Jobs not found with search conditions");
                return notFound("TA Jobs not found with conditions");
            }
            Set<Long> tajobsIdSet = new HashSet<>();
            List<TAJob> filteredJobs = new ArrayList<>();
            for (TAJob tajob : tajobs) {
                if (!tajobsIdSet.contains(tajob.getId())) {
                    filteredJobs.add(tajob);
                    tajobsIdSet.add(tajob.getId());
                }
            }
            JsonNode jsonNode = Json.toJson(filteredJobs);
            result = jsonNode.toString();
        } catch (Exception e) {
            Logger.debug("TAJobController.searchTAJobsByCondition() exception: " + e.toString());
            return internalServerError("TAJobController.searchTAJobsByCondition() exception: " +
                    e.toString());
        }

        return ok(result);
    }


    /**
     * This method intends to set TA job image by job id
     *
     * @patam tajobId
     * @return TODO: change?
     */
    public Result setImage(Long tajobId) {
        if (request().body() == null || request().body().asRaw() == null) {
            return Common.badRequestWrapper("The request cannot be empty");
        }
        try {
            TAJob tJ = TAJob.find.byId(tajobId);
            String url = Common.uploadFile(config, "tajob", "Image", tajobId, request());
            //tJ.setImageUrl(url);
            tJ.save();
        } catch (Exception e) {
            Logger.debug("Failed to set image for TA job: " + e.toString());
            return Common.badRequestWrapper("Failed to add image to the TA job");
        }

        // Return the app image.
        return ok("success");
    }

    /**
     * This method intends to set TA job pdf by TA job id
     *
     * @patam tajobId
     * @return
     */
    public Result setPDF(Long tajobId) {
        if (request().body() == null || request().body().asRaw() == null) {
            return Common.badRequestWrapper("The request cannot be empty");
        }
        try {
            TAJob tJ = TAJob.find.byId(tajobId);
            String url = Common.uploadFile(config, "tajob", "Pdf", tajobId, request());
            tJ.setPdf(url);
            tJ.save();
        } catch (Exception e) {
            Logger.debug("Failed to set pdf for TA job: " + e.toString());
            return Common.badRequestWrapper("Failed to add pdf to the TA job");
        }

        // Return the app pdf.
        return ok("success");
    }


    /**
     * This method receives an TA job id and the number of images in the project's description and checks the s3 bucket
     * to remove the images having id more than the project's description image count (This is because we can remove the
     * deleted description images from S3)
     *
     * @patam projectId                project ID
     * @patam countImagesInDescription number of images in the project's description
     */
    private void removeDeletedImagesInDescriptionFromS3(long projectId, int countImagesInDescription) {
        try {
            countImagesInDescription++;
            String keyName = TAJOB_DESCRIPTION_IMAGE_KEY + projectId + "-" + countImagesInDescription;
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
                keyName = TAJOB_DESCRIPTION_IMAGE_KEY + projectId + "-" + countImagesInDescription;
                exists = s3.doesObjectExist(config.getString(AWS_BUCKET), keyName);
            }
        } catch (Exception e) {
            Logger.debug("Could not remove the rest of description images from S3 bucket.");
            Logger.debug("" + e.getStackTrace());
        }
    }


    /**
     * This method receives an TA job id and deletes the TA job by inactivating it (set is_active field to be false).
     *
     * @patam tajobId given TA job Id
     * @return ok or not found
     */
    public Result deleteTAJob(Long tajobId) {
        try {
            TAJob tajob = TAJob.find.query().where(Expr.and(Expr.or(Expr.isNull("is_active"),
                    Expr.eq("is_active", "True")), Expr.eq("id", tajobId))).findOne();
            if (tajob == null) {
                Logger.debug("In TAJobController deleteTAJob(), cannot find TA job: " + tajobId);
                return notFound("From backend TAJobController, TA job not found with id: " + tajobId);
            }

            tajob.setIsActive("False");
            tajob.save();
            return ok();
        } catch (Exception e) {
            Logger.debug("TA Job cannot be deleted for exception: " + e.toString());
            return Common.badRequestWrapper("Cannot delete TA job for id: " + tajobId);
        }
    }


    public Result getIdByName(String name) {
        try {
            List<TAJob> tajobList = TAJob.find.query().where().eq("title", name).findList();
            return ok(Json.toJson(tajobList.get(0).getId()));
        } catch (Exception e) {
            Logger.debug("TAJobController.getIdByName() exception: " + e.toString());
            return internalServerError("TAJobController.getIdByName() exception: " + e.toString());
        }
    }


    public Result checkTAJobExist(Long tajobId) {
        try {
            TAJob tajob = TAJob.find.byId(tajobId);
            ObjectNode objectNode = Json.newObject();

            if (tajob == null) {
                objectNode.put("notExisted", "TA Job does not exist");
            } else {
                objectNode.put("existed", tajobId);
            }
            return ok(objectNode);
        } catch (Exception e) {
            Logger.debug("TAJobController.checkJobExist exception: " + e.toString());
            return internalServerError("TAJobController.checkJobExist exception: " + e.toString());
        }
    }
//
//
//    /**
//     * Convert a JSON string to pretty print version
//     *
//     * @patam jsonString
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

}
