package controllers;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import services.ChallengeService;
import utils.Common;
import utils.EmailUtils;
import utils.S3Utils;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import io.ebean.Expression;
import io.ebean.Expr;


import static utils.Constants.*;

public class ChallengeController extends Controller {
    public static final String CHALLENGE_DEFAULT_SORT_CRITERIA = "id";
    public static final String PROJECT_DESCRIPTION_IMAGE_KEY = "projectDescriptionImage-";
    public static final String PROJECT_IMAGE_KEY = "projectImage-";
    public static final String TEAM_MEMBER_IMAGE_KEY = "teamMemberImage-";

    private final ChallengeService challengeService;

    @Inject
    Config config;

    @Inject
    public ChallengeController(ChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    /************************************************* Add Challenge **************************************************/
    /**
     * This method intends to add a project into database.
     *
     * @return created status with project id created
     */
    public Result addChallenge() {
        try {
            JsonNode json = request().body().asJson();
            if (json == null) {
                Logger.debug("Challenge information not saved, expecting Json data");
                return badRequest("Challenge information not saved, expecting Json data");
            }

            Challenge challenge = Json.fromJson(json, Challenge.class);
            challenge.setIsActive("True");

            challenge.setStatus("open");
            challenge.setCreateTime(new Date().toString());
            challenge.setUpdateTime(new Date().toString());
            challenge.save();

            String folderName = "challenge/";
            Long applicationId = challenge.getId();
            String tableName = "challenge";
            System.out.println("Finish Add a new Challenge in Backend. " + challenge.toString());
            return ok(Json.toJson(challenge.getId()).toString());
        } catch (Exception e) {
            Logger.debug("Challenge cannot be added: " + e.toString());
            return badRequest("Challenge not saved: ");
        }
    }


    /************************************************* End of Add Challenge *******************************************/

/************************************************* Apply Challenge ***********************************************/
    /**
     * This method intends to apply challenge information except picture.
     *
     * @param challengeId
     * @return
     */
    public Result applyChallenge(Long challengeId) {
        try {
            JsonNode json = request().body().asJson();
            if (json == null) {
                Logger.debug("Challenge information not saved, expecting Json data");
                return badRequest("Challenge information not saved, expecting Json data");
            }

            ChallengeApplication challengeApplication = Json.fromJson(json, ChallengeApplication.class);
            challengeApplication.setIsActive("True");
            challengeApplication.setStatus("open");
            challengeApplication.setCreatedTime(new Date().toString());

            System.out.println("backend challenge application info: " + json);

            challengeApplication.save();

            String folderName = "challengeApplication/";
            String tableName = "challenge_application";
            return ok(Json.toJson(challengeApplication.getId()).toString());
        } catch (Exception e) {
            Logger.debug("Challenge cannot be added: " + e.toString());
            return badRequest("Challenge not saved: ");
        }
    }

    /************************************************* End of Apply Challenge ****************************************/


    /************************************************* Update Challenge ***********************************************/
    /**
     * This method intends to update project information except picture.
     *
     * @param challengeId
     * @return
     */
    public Result updateChallenge(Long challengeId) {
        try {
            JsonNode json = request().body().asJson();
            if (json == null) {
                Logger.debug("Challenge information not saved, expecting Json data from ChallengeController.updateChallenge");
                return badRequest("Challenge information not saved, expecting Json data");
            }

            Challenge existingChallenge = Challenge.find.byId(challengeId);
            if (existingChallenge == null) {
                Logger.debug("Challenge not found with id: " + challengeId);
                return notFound("Challenge not found with id: " + challengeId);
            }
            Challenge updatedChallenge = Json.fromJson(json, Challenge.class);
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
            String folderName = "challenge/";
            String tableName = "challenge";
            updatedChallenge.update();
            existingChallenge.setUpdateTime(new Date().toString());
            existingChallenge.setStatus("updated");
//            existingChallenge.setTitle(updatedChallenge.getTitle());
//            existingChallenge.setGoals(updatedChallenge.getGoals());
//            existingChallenge.setMinSalary(updatedChallenge.getMinSalary());
//            existingChallenge.setMaxSalary(updatedChallenge.getMaxSalary());
//            existingChallenge.setRaTypes(updatedChallenge.getRaTypes());
//            existingChallenge.setShortDescription(updatedChallenge.getShortDescription());
//            existingChallenge.setLongDescription(updatedChallenge.getLongDescription());
//            existingChallenge.setFields(updatedChallenge.getFields());
//            existingChallenge.setPublishDate(updatedChallenge.getPublishDate());
//            existingChallenge.setPublishYear(updatedChallenge.getPublishYear());
//            existingChallenge.setPublishMonth(updatedChallenge.getPublishMonth());
//            existingChallenge.setImageURL(updatedChallenge.getImageURL());
//            existingChallenge.setUrl(updatedChallenge.getUrl());
//            existingChallenge.setOrganization(updatedChallenge.getOrganization());
            existingChallenge.setLocation(updatedChallenge.getLocation());
            existingChallenge.setRequiredExpertise(updatedChallenge.getRequiredExpertise());
            existingChallenge.setPreferredExpertise(updatedChallenge.getPreferredExpertise());
//            existingChallenge.setNumberOfPositions(updatedChallenge.getNumberOfPositions());
//            existingChallenge.setExpectedStartDate(updatedChallenge.getExpectedStartDate());
//            existingChallenge.setExpectedTimeDuration(updatedChallenge.getExpectedTimeDuration());

            return ok(Json.toJson(updatedChallenge));
        } catch (Exception e) {
            Logger.debug("Challenge Profile not saved with id: " + challengeId + " with exception: " + e.toString());
            return badRequest("Challenge Profile not saved: " + challengeId);
        }
    }
    public Result updateChallengeAdmin(Long challengeId) {
        try {
            JsonNode json = request().body().asJson();
            if (json == null) {
                Logger.debug("Challenge information not saved, expecting Json data from updateChallengeAdmin");
                return badRequest("Challenge information not saved, expecting Json data");
            }

            Challenge updatedChallenge = Json.fromJson(json, Challenge.class);

            updatedChallenge.setId(challengeId);

            updatedChallenge.update();

            return ok(Json.toJson(updatedChallenge));
        } catch (Exception e) {
            Logger.debug("Challenge update failed with id: " + challengeId + " exception: " + e.toString());
            return badRequest("Challenge update failed for id: " + challengeId);
        }
    }

    public Result challengeUpdateStatus(Long challengeId) {
        try {

            JsonNode json = request().body().asJson();

            if (json == null) {
                Logger.debug("Challenge Status did not updated, expecting Json data from ChallengeController.challengeUpdateStatus");
                return badRequest("Challenge Status did not updated, expecting Json data");
            }

            Challenge updatedChallenge = Challenge.find.byId(challengeId);
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

            updatedChallenge.setStatus(json.get("status").asText());
            updatedChallenge.update();
            return ok(Json.toJson(updatedChallenge));
        } catch (Exception e) {
            Logger.debug("Challenge Profile not saved with id: " + challengeId + " with exception: " + e.toString());
            return badRequest("Challenge Profile not saved: " + challengeId);
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

    /**
     * Delete project image by project id.
     *
     * @param projectId
     * @return
     */
    public Result deleteProjectImage(Long projectId) {
        if (projectId == null) {
            return Common.badRequestWrapper("projectId is null thus cannot delete image for it.");
        }
        try {
            Project project = Project.find.byId(projectId);
            if (project != null) {
                Common.deleteFileFromS3(config, "project", "Image", projectId);
                project.setImageUrl("");
                project.save();
                return ok("Project image deleted successfully for project id: " + projectId);
            } else {
                return Common.badRequestWrapper("Cannot find project thus cannot delete image for it.");
            }
        } catch (Exception e) {
            Logger.debug("Cannot delete project image for exception:" + e.toString());
            return Common.badRequestWrapper("Cannot delete project picture.");
        }
    }

    /**
     * This method intends to delete project pdf by project id
     *
     * @param projectId
     * @return
     */
    public Result deleteProjectPDF(Long projectId) {
        try {
            Project tp = Project.find.byId(projectId);
            Common.deleteFileFromS3(config, "project", "Pdf", projectId);
            tp.setPdf("");
            tp.save();
        } catch (Exception e) {
            Logger.debug("Failed to set pdf for project: " + e.toString());
            return Common.badRequestWrapper("Failed to add pdf to the project");
        }

        return ok("success");
    }
    /************************************************* End of Update Challenge ****************************************/

    /************************************************* Challenge List *************************************************/
    /**
     * Gets a list of all the projects based on optional offset and limit and sort
     *
     * @param pageLimit    shows the number of rows we want to receive
     * @param pageNum      shows the page number
     * @param sortCriteria shows based on what column we want to sort the data
     * @return a list of projects
     * // TODO: Clean Common utitlity class for getSortCriteria(), not always register_time_stamp
     */
    public Result challengeList(Long userId, Integer pageLimit, Integer pageNum, Optional<String> sortCriteria) {
        List<Challenge> activeChallenges = new ArrayList<>();

        Set<Long> challengeIds = new HashSet<>();
        List<Challenge> challenges;
        String sortOrder = Common.getSortCriteria(sortCriteria, CHALLENGE_DEFAULT_SORT_CRITERIA);

        int offset = pageLimit * (pageNum - 1);
        try {
            User user = User.find.byId(userId);


            activeChallenges = Challenge.find.query().where().eq("is_active", ACTIVE).ne("status", "closed").findList();
            for (Challenge challenge : activeChallenges) {
                challengeIds.add(challenge.getId());
            }

            if (sortOrder.equals("id") || sortOrder.equals("access_times"))
                challenges = Challenge.find.query().where().in("id", challengeIds).order().desc(sortOrder)
                        .findList();
            else
                challenges = Challenge.find.query().where().in("id", challengeIds).orderBy(sortOrder)
                        .findList();
            // **modify updatetime format 20250205 wx**
            SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEE MMM dd yyyy", Locale.ENGLISH);

            for (Challenge challenge : challenges) {
                String rawUpdateTime = challenge.getUpdateTime();
                if (rawUpdateTime != null && !rawUpdateTime.isEmpty()) {
                    try {
                        Date parsedDate = inputFormat.parse(rawUpdateTime);
                        String formattedTime = outputFormat.format(parsedDate);
                        challenge.setUpdateTime(formattedTime);
                    } catch (ParseException e) {
                        Logger.warn("Error parsing updateTime: " + rawUpdateTime);
                    }
                }
            }
            RESTResponse response = challengeService.paginateResults(challenges, Optional.of(offset), Optional.
                    of(pageLimit), sortOrder);
            return ok(response.response());
        } catch (Exception e) {
            Logger.debug("ChallengeController.challengeList() exception: " + e.toString());
            return internalServerError("ChallengeController.challengeList() exception: " + e.toString());
        }
    }
    public Result challengeListAdmin(Long userId, Integer pageLimit, Integer pageNum, Optional<String> sortCriteria) {
        String sortOrder = Common.getSortCriteria(sortCriteria, CHALLENGE_DEFAULT_SORT_CRITERIA);
        int offset = pageLimit * (pageNum - 1);
        List<Challenge> challenges;

        try {
            User user = User.find.byId(userId);

            if (sortOrder.equals("id") || sortOrder.equals("access_times")) {
                challenges = Challenge.find.query()
                        .order().desc(sortOrder)
                        .setFirstRow(offset)
                        .setMaxRows(pageLimit)
                        .findList();
            } else {
                challenges = Challenge.find.query()
                        .orderBy(sortOrder)
                        .setFirstRow(offset)
                        .setMaxRows(pageLimit)
                        .findList();
            }

            RESTResponse response = challengeService.paginateResults(challenges, Optional.of(offset), Optional.of(pageLimit), sortOrder);
            return ok(response.response());
        } catch (Exception e) {
            Logger.debug("ChallengeController.challengeListAdmin() exception: " + e.toString());
            return internalServerError("ChallengeController.challengeListAdmin() exception: " + e.toString());
        }
    }
    /************************************************* End of Challenge List ********************************************/

    /************************************************* Get Challenge ****************************************************/
    /**
     * Get a project detail by the project id
     *
     * @param challengeId project Id
     * @return ok if the project is found; badRequest if the project is not found
     */
    public Result getChallengeById(Long challengeId) {
        if (challengeId == null) {
            return Common.badRequestWrapper("challengeId is null or empty.");
        }

        if (challengeId == 0) return ok(Json.toJson(null));  // projectId=0 means OpenNEX project, not stored in DB

        try {
            Challenge challenge = Challenge.find.query().where().eq("id", challengeId).findOne();
            return ok(Json.toJson(challenge));
        } catch (Exception e) {
            Logger.debug("ChallengeController.getChallengeById() exception : " + e.toString());
            return internalServerError("Internal Server Error ChallengeController.getChallengeById() exception: " +
                    e.toString());
        }
    }

    /**
     * This method returns all TA jobs from TA job info table given the publisher id (the publisher of the TA job)
     *
     * @patam userId the publisher Id
     * @return all TA jobs.
     */
    public Result getChallengesByPublisher(Long userId) {
        try {

            List<Challenge> challenges = Challenge.find.query().where().eq("challenge_publisher_id", userId).findList();

            for(Challenge challenge : challenges){
                int numOfApplicants = ChallengeApplication.find.query().where().eq("challenge_id", challenge.getId()).findCount();
                challenge.setNumberOfApplicants(numOfApplicants);
            }
            ArrayNode challengeArray = Common.objectList2JsonArray(challenges);
            return ok(challengeArray);
        } catch (Exception e) {
            Logger.debug("ChallengeJobController.getChallengesByPublisher exception: " + e.toString());
            return internalServerError("ChallengeController.getChallengesByPublisher exception: " + e.toString());
        }
    }

    public Result getChallengesByApplicant(Long userId) {
        try {

            List<ChallengeApplication> applications = ChallengeApplication.find.query()
                    .where().eq("applicant.id", userId).findList();

            ArrayNode resultArray = Json.newArray();
            for (ChallengeApplication application : applications) {
                Challenge challenge = application.getAppliedChallenge();

                if (challenge != null) {
                    ObjectNode challengeJson = Json.newObject();
                    challengeJson.put("ChallengeApplicationId", application.getId());
                    challengeJson.put("id", challenge.getId());
                    challengeJson.put("challengeTitle", challenge.getChallengeTitle());
                    challengeJson.put("shortDescription", challenge.getShortDescription());
                     challengeJson.put("status", challenge.getStatus());
                    challengeJson.put("location", challenge.getLocation());
                    challengeJson.put("challengeApplicationStatus", application.getStatus());

                    resultArray.add(challengeJson);
                }
            }

            return ok(resultArray);
        } catch (Exception e) {
            Logger.debug("ChallengeController.getChallengesByApplicant exception: " + e.toString());
            return internalServerError("ChallengeController.getChallengesByApplicant exception: " + e.toString());
        }
    }
    public Result getChallengeApplicationById(Long challengeApplicationId) {
        if (challengeApplicationId == null) {
            return Common.badRequestWrapper("challengeApplicationId is null or empty.");
        }

        if (challengeApplicationId == 0) return ok(Json.toJson(null));  // challengeApplicationId=0 means SMU-Sci-Hub job, not stored in DB

        try {
            ChallengeApplication challengeApplication = ChallengeApplication.find.query().where().eq("challenge_id", challengeApplicationId).findOne();
            System.out.print(challengeApplication);
            return ok(Json.toJson(challengeApplication));
        } catch (Exception e) {
            Logger.debug("ChallengeController.getChallengeApplicationById() exception : " + e.toString());
            return internalServerError("Internal Server Error ChallengeController.getChallengeApplicationById() exception: " +
                    e.toString());
        }
    }
    public Result giveChallengeOffertoStudent(Long challengeApplicationId) {
        try {
            System.out.println("Updating Challenge Application status...");
            JsonNode json = request().body().asJson();
            System.out.println("challengeApplicationId: " + challengeApplicationId);

            if (json == null || !json.has("status")) {
                Logger.debug("challenge Status did not update, expecting Json data with 'status'");
                return badRequest("Challenge Status did not update, expecting Json data with 'status'");
            }

            ChallengeApplication challengeApplication = ChallengeApplication.find.byId(challengeApplicationId);
            if (challengeApplication == null) {
                Logger.debug("ChallengeApplication not found with id: " + challengeApplicationId);
                return notFound("ChallengeApplication not found.");
            }
            System.out.println("Status update to: " + json.get("status").asText());
            challengeApplication.setStatus(json.get("status").asText());
            challengeApplication.update();
            return ok(Json.toJson(challengeApplication));
        } catch (Exception e) {
            Logger.debug("Error updating ChallengeApplication with id: " + challengeApplicationId + " - " + e.toString());
            e.printStackTrace();
            return badRequest("Error updating ChallengeApplication with id: " + challengeApplicationId);
        }
    }
    public Result getChallengeApplicationIdById(Long challengeApplicationId) {
        if (challengeApplicationId == null) {
            return Common.badRequestWrapper("challengeApplicationId is null or empty.");
        }

        if (challengeApplicationId == 0) return ok(Json.toJson(null));  // challengeApplicationId=0 means SMU-Sci-Hub job, not stored in DB

        try {
            ChallengeApplication challengeApplication = ChallengeApplication.find.query().where().eq("id", challengeApplicationId).findOne();
            System.out.print(challengeApplication);
            return ok(Json.toJson(challengeApplication));
        } catch (Exception e) {
            Logger.debug("ChallengeController.getChallengeApplicationById() exception : " + e.toString());
            return internalServerError("Internal Server Error ChallengeController.getChallengeApplicationById() exception: " +
                    e.toString());
        }
    }
    public Result getApplicationsByChallengeId(String challengeType, Long challengeId, Integer pageLimit, Integer pageNum, Optional<String> sortCriteria) {
        challengeType = challengeType.toLowerCase();

        String sortOrder = Common.getSortCriteria(sortCriteria, CHALLENGE_DEFAULT_SORT_CRITERIA);
        int offset = pageLimit * (pageNum - 1);
        try {
            List<Object> applications = new ArrayList<Object>();
            if ("challenge".equals(challengeType)) {
                for (ChallengeApplication challengeApplication : ChallengeApplication.find.query().where().eq("challenge_id", challengeId).findList()) {
                    applications.add(challengeApplication);
                }
            }

            List<StudentInfo> studentInfos = StudentInfo.find.query().findList();
            List<ResearcherInfo> researcherInfos = ResearcherInfo.find.query().findList();

            for(Object challengeApplication: applications) {
                User applicant = null;
                if ("challenge".equals(challengeType)) applicant = ((ChallengeApplication)challengeApplication).getApplicant();

                else applicant = ((ChallengeApplication)challengeApplication).getApplicant();
                for (StudentInfo studentInfo: studentInfos) {
                    if (studentInfo.getUser().getId() == applicant.getId()) applicant.setStudentInfo(studentInfo);
                }
                for (ResearcherInfo researcherInfo: researcherInfos) {
                    if (researcherInfo.getUser().getId() == applicant.getId()) applicant.setResearcherInfo(researcherInfo);
                }
            }
            RESTResponse response = challengeService.paginateChallengeApplications(challengeType, applications, Optional.of(offset), Optional.
                    of(pageLimit), sortOrder);
            return ok(response.response());
        } catch (Exception e) {
            Logger.debug("JobController.getApplicationsByJobId() exception : " + e.toString());
            return internalServerError("Internal Server Error JobController.getApplicationsByJobId() exception: " +
                    e.toString());
        }
    }


    /************************************************* End of Get Challenge *********************************************/

    /**
     * Checks if a creator with the same email id as provided is already present.
     * Note: If an email address has been registered before, even if the creator has become inactive, the email address
     * cannot
     * be registered as new any longer.
     *
     * @return this email is valid message if email is not already used, else an error stating email has been used.
     */
    public Result checkProjectNameAvailability() {
        JsonNode json = request().body().asJson();
        if (json == null) {
            Logger.info("Cannot check project name, expecting Json data");
            return badRequest("Cannot check project name, expecting Json data");
        }
        String title = json.path("title").asText();
        if (title == null || title.isEmpty()) {
            Logger.info("title is null or empty");
            return Common.badRequestWrapper("title is null or empty.");
        }
        try {
            List<Project> projects = Project.find.query().where().eq("title", title).findList();
            if (projects == null || projects.size() == 0) {
                return ok("This new project name can be used");
            } else {
                return Common.badRequestWrapper("This project name has been used.");
            }
        } catch (Exception e) {
            return internalServerError("ProjectController.checkProjectNameAvailability exception: " +
                    e.toString());
        }
    }


    /**
     * This method intends to return the creator of a project.
     *
     * @param projectId project info id
     * @return json of the creator of the project
     * TODO: Merge Project and Project
     */
//    public Result getProjectCreator(Long projectId) {
//        try {
//            Project project = Project.find.byId(projectId);
//            if (project == null) {
//                return Common.badRequestWrapper("No Project found with the given project info id");
//            }
//            return ok(Json.toJson(project.getCreator()));
//        } catch (Exception e) {
//            Logger.debug("ProjectController.getProjectCreator() exception: " + e.toString());
//            return internalServerError("ProjectController.getProjectCreator() exception: " + e.toString());
//        }
//    }


    /**
     * Check if the project is search result
     *
     * @param project     Project being checked
     * @param title
     * @param goals
     * @param location
     * @param description
     * @return if the project is search result.
     */
    private boolean isMatchedProject(Project project, String title, String goals, String location, String description) {
        boolean titleInTitle = false;
        boolean goalInGoal = false;
        boolean locationInLocation = false;
        boolean descriptionInDescription = false;
        for (String titleSubWord : title.split(" ")) {
            titleSubWord = titleSubWord.trim();
            titleInTitle = title.equals("") || (project.getTitle() != null && project.getTitle().toLowerCase().
                    indexOf(titleSubWord.toLowerCase()) >= 0);
            if (titleInTitle)
                break;
        }
        for (String goalSubWord : goals.split(" ")) {
            goalSubWord = goalSubWord.trim();
            goalInGoal = goals.equals("") || (project.getGoals() != null && project.getGoals().toLowerCase().
                    indexOf(goalSubWord.toLowerCase()) >= 0);
            if (goalInGoal)
                break;
        }
        for (String locationSubWord : location.split(" ")) {
            locationSubWord = locationSubWord.trim();
            locationInLocation = location.equals("") || (project.getLocation() != null && project.getLocation().
                    toLowerCase().indexOf(locationSubWord.toLowerCase()) >= 0);
            if (locationInLocation)
                break;
        }
        for (String descriptionSubWord : description.split(" ")) {
            descriptionSubWord = descriptionSubWord.trim();
            descriptionInDescription = description.equals("") || (project.getDescription() != null &&
                    project.getDescription().toLowerCase().indexOf(descriptionSubWord.toLowerCase()) >= 0);
            if (descriptionInDescription)
                break;
        }
        return titleInTitle && goalInGoal && locationInLocation && descriptionInDescription;
    }

    /**
     * Filter the projects based on title, goal, location, description
     *
     * @param title       API list being filtered
     * @param goals
     * @param location
     * @param description
     * @return the list of filtered projects.
     */
    private List<Project> matchedProjectList(List<Project> projectList, String title, String goals, String location,
                                             String description) {
        List<Project> results = new ArrayList<>();
        for (Project project : projectList) {
            if (isMatchedProject(project, title, goals, location, description))
                results.add(project);
        }
        return results;
    }


    /**
     * Find projects by multiple condition, including title, goal, location, etc.
     *
     * @return projects that match the condition
     * TODO: How to handle more conditions???
     */
    public Result searchProjectsByCondition() {
        String result = null;
        try {
            JsonNode json = request().body().asJson();
            List<Project> projects = new ArrayList<>();
            if (json == null) {
                return badRequest("Condition cannot be null");
            }
            //Get condition value from Json data

            String title = json.path("name").asText();

            String goals = json.path("goals").asText();

            String location = json.path("location").asText();

            String description = json.path("description").asText();

            String keywords = json.path("keywords").asText();
            //Search projects by conditions
            if (keywords.trim().equals("")) {
                List<Project> potentialProjects = Project.find.query().where().eq("is_active", ACTIVE).
                        findList();
                projects = matchedProjectList(potentialProjects, title, goals, location, description);

            } else {
                List<Project> tmpProjects = Project.find.query().where().eq("is_active", ACTIVE).findList();
                String[] keywordList = keywords.toLowerCase().trim().split(" ");

                for (String keyword : keywordList) {
                    keyword = keyword.toLowerCase();
                    for (Project project : tmpProjects) {
                        if ((project.getTitle() != null && project.getTitle().toLowerCase().contains(keyword)) ||
                                (project.getGoals() != null && project.getGoals().toLowerCase().contains(keyword)) ||
                                (project.getLocation() != null && project.getLocation().toLowerCase().contains(keyword))
                                || (project.getDescription() != null && project.getDescription().toLowerCase().contains
                                (keyword))) {
                            projects.add(project);
                        }
                    }
                }
            }
            //If not found
            if (projects == null || projects.size() == 0) {
                Logger.info("Projects not found with search conditions");
                return notFound("Projects not found with conditions");
            }
            Set<Long> projectsIdSet = new HashSet<>();
            List<Project> filteredProjects = new ArrayList<>();
            for (Project project : projects) {
                if (!projectsIdSet.contains(project.getId())) {
                    filteredProjects.add(project);
                    projectsIdSet.add(project.getId());
                }
            }
            JsonNode jsonNode = Json.toJson(filteredProjects);
            result = jsonNode.toString();
        } catch (Exception e) {
            Logger.debug("ProjectController.searchProjectsByCondition() exception: " + e.toString());
            return internalServerError("ProjectController.searchProjectsByCondition() exception: " +
                    e.toString());
        }

        return ok(result);
    }


    /**
     * This method intends to set challenge image by challenge id
     *
     * @param challengeId
     * @return
     */
    public Result setImage(Long challengeId) {

        if (challengeId == null) {
            return Common.badRequestWrapper("challengeId is null thus cannot add image for it.");
        }

        try {

            if (request().body() == null || request().body().asRaw() == null) {
                return Common.badRequestWrapper("The request cannot be empty");
            }

            Challenge challenge = Challenge.find.byId(challengeId);

            if (challenge != null) {
                Logger.info("backend received challenge image upload; ");
                String url = Common.uploadFileOnServer("challenge", "Image", challengeId, request());
                challenge.setChallengeImage(url);
                challenge.save();
                return ok("challenge image upload successfully.");
            }
            return Common.badRequestWrapper("challenge with the given ID not found!");
        } catch (Exception e) {
            e.printStackTrace();
            return Common.badRequestWrapper("Image could not be updated.");
        }
    }

    /**
     * This method intends to set challenge image by challenge id
     *
     * @param challengeId
     * @return
     */
    public Result getImage(Long challengeId) {

        if (challengeId == null) {
            return Common.badRequestWrapper("challengeId is null thus cannot add image for it.");
        }

        try {
            System.out.println("Prepare send challenge img to frontend 1");
            Challenge challenge = Challenge.find.byId(challengeId);

            if (challenge != null) {
                Logger.info("backend receive get challenge image; ");
                File file = new File(challenge.getChallengeImage());
                System.out.println("Prepare send challenge img to frontend 2");
                return ok().sendFile(file);
            }
            return Common.badRequestWrapper("challenge with the given ID not found!");
        } catch (Exception e) {
            e.printStackTrace();
            return Common.badRequestWrapper("Image could not be updated.");
        }
    }

    public Result closeChallenge(Long challengeId) {
        try {
            if (challengeId == null) {
                Logger.debug("Challenge ID is null in ChallengeController.closeChallenge");
                return badRequest("Challenge ID cannot be null");
            }

            // Fetch the Challenge by ID
            Challenge challenge = Challenge.find.byId(challengeId);
            if (challenge == null) {
                Logger.debug("Challenge not found with ID: " + challengeId);
                return notFound("Challenge not found with ID: " + challengeId);
            }

            challenge.setStatus("closed");
            challenge.update();

            Logger.debug("Challenge with ID: " + challengeId + " successfully closed.");
            return ok(Json.toJson(challenge));
        } catch (Exception e) {
            Logger.debug("Failed to close RA job with ID: " + challengeId + ", exception: " + e.toString());
            return internalServerError("Failed to close Challenge: " + e.getMessage());
        }
    }

    /**
     * This method intends to set challenge image by challenge id
     *
     * @param challengeId
     * @return
     */
    public Result setPdf(Long challengeId) {

        if (challengeId == null) {
            return Common.badRequestWrapper("challengeId is null thus cannot add pdf for it.");
        }

        try {

            if (request().body() == null || request().body().asRaw() == null) {
                return Common.badRequestWrapper("The request cannot be empty");
            }

            Challenge challenge = Challenge.find.byId(challengeId);

            if (challenge != null) {
                Logger.info("backend received challenge pdf upload; ");
                String url = Common.uploadFileOnServer("challenge", "Pdf", challengeId, request());
                challenge.setChallengePdf(url);
                challenge.save();
                return ok("challenge pdf upload successfully.");
            }
            return Common.badRequestWrapper("challenge with the given ID not found!");
        } catch (Exception e) {
            e.printStackTrace();
            return Common.badRequestWrapper("Image could not be updated.");
        }
    }

    /**
     * This method intends to set project pdf by project id
     *
     * @param projectId
     * @return
     */
    public Result setPDF(Long projectId) {
        if (request().body() == null || request().body().asRaw() == null) {
            return Common.badRequestWrapper("The request cannot be empty");
        }
        try {
            Project tp = Project.find.byId(projectId);
            String url = Common.uploadFile(config, "project", "Pdf", projectId, request());
            tp.setPdf(url);
            tp.save();
        } catch (Exception e) {
            Logger.debug("Failed to set pdf for project: " + e.toString());
            return Common.badRequestWrapper("Failed to add pdf to the project");
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
            String keyName = PROJECT_DESCRIPTION_IMAGE_KEY + projectId + "-" + countImagesInDescription;
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
                keyName = PROJECT_DESCRIPTION_IMAGE_KEY + projectId + "-" + countImagesInDescription;
                exists = s3.doesObjectExist(config.getString(AWS_BUCKET), keyName);
            }
        } catch (Exception e) {
            Logger.debug("Could not remove the rest of description images from S3 bucket.");
            Logger.debug("" + e.getStackTrace());
        }
    }


    /**
     * This method intends to delete a team member by member id.
     *
     * @param memberId: team member id
     * @return status
     */
    public Result deleteTeamMember(Long memberId) {
        if (memberId == null) {
            return Common.badRequestWrapper("memberId is null.");
        }
        try {
            User tm = User.find.byId(memberId);
            if (tm != null) {
                Common.deleteFileFromS3(config, "teamMember", "Image", memberId);
                tm.delete();
                return ok("Team Member deleted successfully for member id:" + memberId);
            } else
                return Common.badRequestWrapper("memberId cannot be found thus not deleted.");
        } catch (Exception e) {
            Logger.debug("Team member cannot be deleted for exception: " + e.toString());
            return Common.badRequestWrapper("Cannot delete team member for id: " + memberId);
        }
    }

    /**
     * This method receives a project id and deletes the project by inactivating it (set is_active field to be false).
     *
     * @param projectId given notebook Id
     * @return ok or not found
     */
    public Result deleteProject(Long projectId) {
        try {
            Project project = Project.find.query().where(Expr.and(Expr.or(Expr.isNull("is_active"),
                    Expr.eq("is_active", "True")), Expr.eq("id", projectId))).findOne();
            if (project == null) {
                Logger.debug("In ProjectController deleteProject(), cannot find project" + projectId);
                return notFound("From backend ProjectController, Project not found with id: " + projectId);
            }

            project.setIsActive("False");
            project.save();
            return ok();
        } catch (Exception e) {
            Logger.debug("Project cannot be deleted for exception: " + e.toString());
            return Common.badRequestWrapper("Cannot delete project for id: " + projectId);
        }
    }


    /**
     * This method receives a project Id and the image number in the description of the project and uploads this image
     * to aws and return the received URL for the uploaded image
     *
     * @param projectId   project Id
     * @param imageNumber image number in the description of the project
     * @return the received URL for the uploaded image if the upload is successful
     */
    public Result saveDescriptionImage(long projectId, int imageNumber) {
        if (request().body() == null || request().body().asRaw() == null) {
            return Common.badRequestWrapper("The request cannot be empty");
        }
        File image = request().body().asRaw().asFile();
        try {
            Project project = Project.find.byId(projectId);
            if (project == null) {
                return Common.badRequestWrapper("No project was found with the given ID: " + projectId);
            }
            String keyName = PROJECT_DESCRIPTION_IMAGE_KEY + projectId + "-" + imageNumber;
            String awsAccessKey = config.getString(AWS_ACCESS_KEY);
            String awsSecretAccesskey = config.getString(AWS_SECRET_ACCESS_KEY);
            String awsRegion = config.getString(AWS_REGION);
            BasicAWSCredentials awsCreds = new BasicAWSCredentials(awsAccessKey, awsSecretAccesskey);
            final AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                    .withRegion(awsRegion)
                    .build();
            s3.putObject(new PutObjectRequest(config.getString(AWS_BUCKET), keyName, image).withCannedAcl
                    (CannedAccessControlList.PublicRead));
            String url = s3.getUrl(config.getString(AWS_BUCKET), keyName).toString();
            return ok(Json.toJson(url));
        } catch (Exception e) {
            Logger.debug("Failed to set description image for project: " + e.toString());
            return Common.badRequestWrapper("Failed to add description image for the project");
        }
    }

    /**
     * This method receives a project Id and the image number in the description of the project along with the current
     * image index in the description and renames the file on S3 bucket to have the new imageNumber as the index and
     * return the received URL for the uploaded image
     *
     * @param projectId          project Id
     * @param imageNumber        image number in the description of the project
     * @param currentImageNumber current image index number in the description of the project
     * @return the received URL for the uploaded image if the upload is successful
     */
    public Result renameDescriptionImage(long projectId, int imageNumber, int currentImageNumber) {
        try {
            Logger.debug("rename project description ");
            Project project = Project.find.byId(projectId);
            if (project == null) {
                return Common.badRequestWrapper("No project was found with the given ID: " + projectId);
            }
            String currentKeyName = PROJECT_DESCRIPTION_IMAGE_KEY + projectId + "-" + currentImageNumber;
            String newKeyName = PROJECT_DESCRIPTION_IMAGE_KEY + projectId + "-" + imageNumber;
            Logger.debug("Project Description Image Renamed from: " + currentKeyName + " to :" + newKeyName);
            String awsAccessKey = config.getString(AWS_ACCESS_KEY);
            String awsSecretAccesskey = config.getString(AWS_SECRET_ACCESS_KEY);
            String awsRegion = config.getString(AWS_REGION);
            BasicAWSCredentials awsCreds = new BasicAWSCredentials(awsAccessKey, awsSecretAccesskey);
            final AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                    .withRegion(awsRegion)
                    .build();
            CopyObjectRequest copyObjRequest = new CopyObjectRequest(config.getString(AWS_BUCKET),
                    currentKeyName, config.getString(AWS_BUCKET), newKeyName);
            s3.copyObject(copyObjRequest);
            String url = s3.getUrl(config.getString(AWS_BUCKET), newKeyName).toString();
            return ok(Json.toJson(url));
        } catch (Exception e) {
            e.printStackTrace();
            Logger.debug("Failed to set description image for project: " + e.toString());
            return Common.badRequestWrapper("Failed to add description image for the project");
        }
    }

    //**************************** END BASIC REFACTORING *************************************************************//
    //**************************** END BASIC REFACTORING *************************************************************//
    //**************************** END BASIC REFACTORING *************************************************************//


    /**
     * This method update project description
     * title
     * des
     *
     * @return status of the update
     */
    public Result updateProjectDes() {
        JsonNode json = request().body().asJson();

        if (json == null) {

            return badRequest("Project description infomation not saved, expecting Json data");
        }
        String title = json.findPath("title").asText();
        String des = json.findPath("des").asText();
        List<Project> pis = Project.find.query().where().eq("title", title).findList();
        for (Project pi : pis) {
            if (!pi.getTitle().equals(title)) continue;
            pi.setDescription(des);
            pi.update();
        }
        return ok(Json.toJson("success").toString());

    }

    /**
     * This method get all projects from project_info table.
     *
     * @return all projects.
     */
    public Result getAllProject() {
        Iterable<Project> projectInfo = Project.find.all();
        if (projectInfo == null) {
            System.out.println("No project info found");
        }

        String result = Json.toJson(projectInfo).toString();

        return ok(result);
    }


//    /**
//     * Gets a list of all the projects based on optional offset and limit and sort
//     *
//     * @param pageLimit    shows the number of rows we want to receive
//     * @param pageNum      shows the page number
//     * @param sortCriteria shows based on what column we want to sort the data
//     * @return a list of projects
//     * // TODO: Clean Common utitlity class for getSortCriteria(), not always register_time_stamp
//     */
//    public Result getMyEnrolledProjects(Integer pageLimit, Integer pageNum, Optional<String> sortCriteria,
//                                        Long userId) {
//        List<Project> projects = new ArrayList<>();
//        String sortOrder = Common.getSortCriteria(sortCriteria, PROJECT_DEFAULT_SORT_CRITERIA);
//        int offset = pageLimit * (pageNum - 1);
//        try {
//
//            User user = User.find.byId(userId);
//            String userName = user.getUserName();
//            List<User> teamMembers = User.find.query().where().eq("name", userName).findList();
//            //List<Long> projectIds = new ArrayList<>();
//            Set<Long> projectIds = new HashSet<>();
//            for (User teamMember : teamMembers) {
//                projectIds.add(teamMember.getProjectZone().getId());
//            }
//            //projects = Project.find.query().where().eq("is_active", ACTIVE).orderBy(sortOrder).findList();
//            projects = Project.find.query().where().in("id", projectIds).orderBy(sortOrder).findList();
//
//            RESTResponse response = projectService.paginateResults(projects, Optional.of(offset), Optional.
//                    of(pageLimit), sortOrder);
//            return ok(response.response());
//        } catch (Exception e) {
//            Logger.debug("ProjectController.projectList() exception: " + e.toString());
//            return internalServerError("ProjectController.projectList() exception: " + e.toString());
//        }
//    }


    public Result getIdByName(String name) {
        try {
            List<Project> projectList = Project.find.query().where().eq("title", name).findList();
            return ok(Json.toJson(projectList.get(0).getId()));
        } catch (Exception e) {
            Logger.debug("ProjectController.getIdByName() exception: " + e.toString());
            return internalServerError("ProjectController.getIdByName() exception: " + e.toString());
        }
    }


    public Result checkProjectExist(Long projectId) {
        try {
            Project project = Project.find.byId(projectId);
            ObjectNode objectNode = Json.newObject();

            if (project == null) {
                objectNode.put("notExisted", "Project does not exist");
            } else {
                objectNode.put("existed", projectId);
            }
            return ok(objectNode);
        } catch (Exception e) {
            Logger.debug("ProjectController.checkProjectExist exception: " + e.toString());
            return internalServerError("ProjectController.checkProjectExist exception: " + e.toString());
        }
    }



    public JsonNode readJsonFromFile(File file) {
        if (file == null) {
            System.out.println("null file");
            return null;
        }
        StringBuilder contentBuilder = new StringBuilder();
        try {

            BufferedReader br = new BufferedReader((new InputStreamReader(new FileInputStream(file),
                    "UTF-8")));
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {

                contentBuilder.append(sCurrentLine).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ObjectMapper mapper = new ObjectMapper();

        try {

            JsonNode datasetJsonNode = mapper.readTree(contentBuilder.toString());
            return datasetJsonNode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public Result searchChallenges(Integer pageLimit, Integer pageNum, Optional<String> sortCriteria) {
        try {
            JsonNode searchJson = request().body().asJson();
            if (searchJson == null) {
                Logger.debug("Invalid request: missing JSON body");
                return badRequest("Invalid request: missing JSON body");
            }

            String keywords = searchJson.path("keywords").asText("");
            String name = searchJson.path("name").asText("");
            String description = searchJson.path("description").asText("");
            String location = searchJson.path("location").asText("");
            String goals = searchJson.path("goals").asText("");

            Logger.info("Search criteria: keywords={}, name={}, description={}, location={}, goals={}",
                    keywords, name, description, location, goals);

            int offset = pageLimit * (pageNum - 1);

            List<Challenge> challenges = new ArrayList<>();
            Set<Long> challengeIds = new HashSet<>();
            String sortOrder = Common.getSortCriteria(sortCriteria, CHALLENGE_DEFAULT_SORT_CRITERIA);

            challenges = Challenge.find.query().where().eq("is_active", ACTIVE).ne("status", "closed").findList();

            if (keywords != null && !keywords.isEmpty()) {
                challenges = Challenge.find.query().where()
                    .and()
                    .or()
                    .ilike("challengeTitle", "%" + keywords + "%")
                    .ilike("shortDescription", "%" + keywords + "%")
                    .ilike("longDescription", "%" + keywords + "%")
                    .ilike("requiredExpertise", "%" + keywords + "%")
                    .ilike("preferredExpertise", "%" + keywords + "%")
                    .ilike("location", "%" + keywords + "%")
                    .endOr()
                    .endAnd()
                    .findList();

            } else {
                Expression cond = Expr.and(
                        Expr.and(
                                (name != null && !name.isEmpty()) ? Expr.eq("challengeTitle", name) : Expr.raw("1=1"),
                                (description != null && !description.isEmpty()) ? Expr.eq("shortDescription", description) : Expr.raw("1=1")
                        ),
                        Expr.and(
                                (location != null && !location.isEmpty()) ? Expr.eq("location", location) : Expr.raw("1=1"),
                                (goals != null && !goals.isEmpty()) ? Expr.eq("preferredExpertise", goals) : Expr.raw("1=1")
                        )
                );

                challenges = Challenge.find.query().where()
                        .add(cond)
                        .findList();
            }

            RESTResponse response = challengeService.paginateResults(challenges, Optional.of(offset), Optional.
                    of(pageLimit), sortOrder);

            return ok(response.response());
        } catch (Exception e) {
            Logger.error("Error during challenge search: ", e);
            return internalServerError("An error occurred while processing the request.");
        }
    }

}
