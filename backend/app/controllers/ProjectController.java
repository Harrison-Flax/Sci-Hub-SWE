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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.typesafe.config.Config;
import io.ebean.Expr;
import models.Project;
import models.User;
import models.rest.RESTResponse;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import services.ProjectService;
import utils.Common;
import utils.EmailUtils;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

import static utils.Constants.*;

public class ProjectController extends Controller {
    public static final String PROJECT_DEFAULT_SORT_CRITERIA = "title";
    public static final String PROJECT_DESCRIPTION_IMAGE_KEY = "projectDescriptionImage-";
    public static final String PROJECT_IMAGE_KEY = "projectImage-";
    public static final String TEAM_MEMBER_IMAGE_KEY = "teamMemberImage-";
    private final ProjectService projectService;

    @Inject
    Config config;

    @Inject
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /************************************************* Add Project ****************************************************/
    /**
     * This method intends to add a project into database.
     *
     * @return created status with project id created
     */
    public Result addProject() {
        try {
            JsonNode json = request().body().asJson();

            if (json == null) {
                Logger.debug("Project information not saved, expecting Json data");
                return badRequest("Project information not saved, expecting Json data");
            }

            Project project = Json.fromJson(json, Project.class);
            project.setIsActive("True");
            System.out.println("project: " + project.toString());
            project.save();
            return ok(Json.toJson(project.getId()).toString());
        } catch (Exception e) {
            Logger.debug("Project cannot be added: " + e.toString());
            return badRequest("Project not saved: ");
        }
    }

    /**
     * This method intends to add a team member for a project.
     *
     * @param projectId
     * @return TODO: Team member should be a User type.
     */
    public Result addTeamMember(Long projectId) {
        try {
            JsonNode json = request().body().asJson();
            Project project = new Project(projectId);

            if (json == null) {
                Logger.debug("Team Member not saved, expecting Json data");
                return badRequest("Team Member not saved, expecting Json data");
            }
            String name = json.findPath("name").asText();
            String email = json.findPath("email").asText();
            User user = User.find.query().where().eq("email", email).findOne();

            // If the team member does not have an account in OpenNEX
            if (user == null) {
                user = new User(name, email);

                List<Project> projectList = new ArrayList<Project>();
                projectList.add(project);
                user.setParticipatedProjects(projectList);
                user.save();
            } else {
                List<Project> projectList = user.getParticipatedProjects();
                if (projectList == null) {
                    projectList = new ArrayList<Project>();
                    projectList.add(project);
                    user.setParticipatedProjects(projectList);
                } else {
                    if (!projectList.contains(project))
                        projectList.add(project);
                }
                user.update();
            }
            return created(Json.toJson(user.getId()));
        } catch (Exception e) {
            Logger.debug("Cannot add team member for project: " + e.toString());
            return badRequest("Team Member not saved.");
        }
    }

    /**
     * This method intends to set a team member's photo by team member id
     *
     * @param userId
     * @return
     */
    public Result setTeamMemberPhoto(Long userId) {
        try {
            if (request().body() == null || request().body().asRaw() == null) {
                return Common.badRequestWrapper("The request of ProjectController.setTeamMemberPhoto cannot be " +
                        "empty");
            }

            User user = User.find.byId(userId);
            String imageUrl = Common.uploadFile(config, "teamMember", "Image", userId, request());
            user.setAvatar(imageUrl);
            user.save();
        } catch (Exception e) {
            Logger.debug("Failed to set image for team member: " + e.toString());
            return Common.badRequestWrapper("Failed to add image to the team member");
        }
        return ok("success");
    }

    /************************************************* End of Add Project *********************************************/


    /************************************************* Update Project *************************************************/
    /**
     * This method intends to update project information except picture.
     *
     * @param projectId
     * @return
     */
    public Result updateProject(Long projectId) {
        try {
            JsonNode json = request().body().asJson();
            if (json == null) {
                Logger.debug("Project information not saved, expecting Json data from ProjectController.updateProject");
                return badRequest("Project information not saved, expecting Json data");
            }

            Project updatedProject = Json.fromJson(json, Project.class);
            String newHtml = updatedProject.getDescription();
            Set<String> newImageSet = getImageSet(newHtml);

            Project oldProject = Project.find.byId(projectId);
            String oldHtml = oldProject.getDescription();
            Set<String> oldImageSet = getImageSet(oldHtml);

            for (String imageName : oldImageSet) {
                if (!newImageSet.contains(imageName)) {
                    Common.deleteFileFromS3(config, imageName);
                }
            }
            updatedProject.update();
            return ok(Json.toJson(updatedProject));
        } catch (Exception e) {
            Logger.debug("Project Profile not saved with id: " + projectId + " with exception: " + e.toString());
            return badRequest("Project Profile not saved: " + projectId);
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
    /************************************************* End of Update Project ******************************************/

    /************************************************* Project List ***************************************************/
    /**
     * Gets a list of all the projects based on optional offset and limit and sort
     *
     * @param pageLimit    shows the number of rows we want to receive
     * @param pageNum      shows the page number
     * @param sortCriteria shows based on what column we want to sort the data
     * @return a list of projects
     * // TODO: Clean Common utitlity class for getSortCriteria(), not always register_time_stamp
     */
    public Result projectList(Long userId, Integer pageLimit, Integer pageNum, Optional<String> sortCriteria) {
        List<Project> activeProjects = new ArrayList<>();

        Set<Long> projectIds = new HashSet<>();
        List<Project> projects;
        String sortOrder = Common.getSortCriteria(sortCriteria, PROJECT_DEFAULT_SORT_CRITERIA);

        int offset = pageLimit * (pageNum - 1);
        try {
            User user = User.find.byId(userId);


            activeProjects = Project.find.query().where().eq("is_active", ACTIVE).findList();
            for (Project project : activeProjects) {
                    projectIds.add(project.getId());
            }

            if (sortOrder.equals("id") || sortOrder.equals("access_times"))
                projects = Project.find.query().where().in("id", projectIds).order().desc(sortOrder).
                        findList();
            else
                projects = Project.find.query().where().in("id", projectIds).orderBy(sortOrder).findList();
            RESTResponse response = projectService.paginateResults(projects, Optional.of(offset), Optional.
                    of(pageLimit), sortOrder);
            return ok(response.response());
        } catch (Exception e) {
            Logger.debug("ProjectController.projectList() exception: " + e.toString());
            return internalServerError("ProjectController.projectList() exception: " + e.toString());
        }
    }
    /************************************************* End of Project List ********************************************/

    /************************************************* Get Project ****************************************************/
    /**
     * Get a project detail by the project id
     *
     * @param projectId project Id
     * @return ok if the project is found; badRequest if the project is not found
     */
    public Result getProjectById(Long projectId) {
        if (projectId == null) {
            return Common.badRequestWrapper("projectId is null or empty.");
        }

        if (projectId == 0) return ok(Json.toJson(null));  // projectId=0 means OpenNEX project, not stored in DB

        try {
            Project project = Project.find.query().where().eq("id", projectId).findOne();
            return ok(Json.toJson(project));
        } catch (Exception e) {
            Logger.debug("ProjectController.getProjectById() exception : " + e.toString());
            return internalServerError("Internal Server Error ProjectController.getProjectById() exception: " +
                    e.toString());
        }
    }

    /**
     * This method returns all projects from project info table given the creator Id (the creator of the project)
     *
     * @param userId the creator Id
     * @return all projects.
     */
    public Result getProjectsByCreator(Long userId) {
        try {
//            Iterable<Project> projectInfo = Project.find.query().where().eq("creator", userId).findList();
//            if (projectInfo == null) {
//                Logger.info("No project info found");
//            }
//            String result = Json.toJson(projectInfo).toString();
//            return ok(result);
            //System.out.println("=======2.1: userId:" + userId);

            List<Project> projects = Project.find.query().where().eq("creator.id", userId).findList();
            //System.out.println("=======2.2: projects:" + (projects.size()));

            ArrayNode projectArray = Common.objectList2JsonArray(projects);
            return ok(projectArray);
        } catch (Exception e) {
            Logger.debug("ProjectController.getProjectsByCreator exception: " + e.toString());
            return internalServerError("ProjectController.getProjectsByCreator exception: " + e.toString());
        }
    }

    /**
     * This method intends to get team members by project id.
     *
     * @param projectId project Id(from project info table)
     * @return the team member when given valid Id. Otherwise, badRequest.
     */
    public Result getTeamMembersByProjectId(Long projectId) {
        if (projectId == null) {
            return Common.badRequestWrapper("projectId is null or empty.");
        }
        try {
            Project project = Project.find.byId(projectId);

            List<User> teamMembers = project.getTeamMembers();
            return ok(Common.objectList2JsonArray(teamMembers));
        } catch (Exception e) {
            Logger.debug("ProjectController.getTeamMembersByProjectId() exception: " + e.toString());
            return internalServerError("ProjectController.getTeamMembersByProjectId() exception: " +
                    e.toString());
        }
    }
    /************************************************* End of Get Project *********************************************/

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
    public Result getProjectCreator(Long projectId) {
        try {
            Project project = Project.find.byId(projectId);
            if (project == null) {
                return Common.badRequestWrapper("No Project found with the given project info id");
            }
            return ok(Json.toJson(project.getSponsorContact()));
        } catch (Exception e) {
            Logger.debug("ProjectController.getProjectCreator() exception: " + e.toString());
            return internalServerError("ProjectController.getProjectCreator() exception: " + e.toString());
        }
    }


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
     * This method intends to set project image by project id
     *
     * @param projectId
     * @return
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


    /**
     * Gets a list of all the projects based on optional offset and limit and sort
     *
     * @param pageLimit    shows the number of rows we want to receive
     * @param pageNum      shows the page number
     * @param sortCriteria shows based on what column we want to sort the data
     * @return a list of projects
     * // TODO: Clean Common utitlity class for getSortCriteria(), not always register_time_stamp
     */
    public Result getMyEnrolledProjects(Integer pageLimit, Integer pageNum, Optional<String> sortCriteria,
                                        Long userId) {
        List<Project> projects = new ArrayList<>();
        String sortOrder = Common.getSortCriteria(sortCriteria, PROJECT_DEFAULT_SORT_CRITERIA);
        int offset = pageLimit * (pageNum - 1);
        try {

            User user = User.find.byId(userId);
            String userName = user.getUserName();
            List<User> teamMembers = User.find.query().where().eq("name", userName).findList();
            //List<Long> projectIds = new ArrayList<>();
            Set<Long> projectIds = new HashSet<>();
            for (User teamMember : teamMembers) {
                projectIds.add(teamMember.getProjectZone().getId());
            }
            //projects = Project.find.query().where().eq("is_active", ACTIVE).orderBy(sortOrder).findList();
            projects = Project.find.query().where().in("id", projectIds).orderBy(sortOrder).findList();

            RESTResponse response = projectService.paginateResults(projects, Optional.of(offset), Optional.
                    of(pageLimit), sortOrder);
            return ok(response.response());
        } catch (Exception e) {
            Logger.debug("ProjectController.projectList() exception: " + e.toString());
            return internalServerError("ProjectController.projectList() exception: " + e.toString());
        }
    }


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


    public void sendResultEmail(Long userId, JsonNode contentJson, String actionType) {
        try {

            User thisUser = User.find.byId(Long.valueOf(userId));
            String email = thisUser.getEmail();

            String applicationLetterBody = "Your Project " + actionType + " action is done. This is your result:\n." +
                    toPrettyFormat(contentJson.toString());
            String subject = "Your batch action is done";
            EmailUtils.sendIndividualEmail(config, email, subject, applicationLetterBody);
        } catch (Exception e) {
            e.printStackTrace();
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