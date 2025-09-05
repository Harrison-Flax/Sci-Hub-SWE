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
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.typesafe.config.Config;
import io.ebean.Expr;
import models.Project;
import models.Technology;
import models.User;
import models.rest.RESTResponse;

import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import services.TechnologyService;

import utils.Common;
import utils.EmailUtils;
import utils.S3Utils;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

import static utils.Constants.*;

public class TechnologyController extends Controller {
    public static final String TECHNOLOGY_DEFAULT_SORT_CRITERIA = "technologyTitle";
    public static final String TECHNOLOGY_DESCRIPTION_IMAGE_KEY = "technology-DescriptionImage-";
    public static final String TECHNOLOGY_IMAGE_KEY = "technologyImage-";

    private final TechnologyService technologyService;

    @Inject
    Config config;

    @Inject
    public TechnologyController(TechnologyService technologyService) {
        this.technologyService = technologyService;
    }

    /************************************************* Add Technology **************************************************/
    /**
     * This method intends to add a technology into database.
     *
     * @return created status with technology id created
     */
    public Result addTechnology() {
        try {
            JsonNode json = request().body().asJson();
            if (json == null) {
                Logger.debug("Technology information not saved, expecting Json data");
                return badRequest("Technology information not saved, expecting Json data");
            }
            Technology technology = Json.fromJson(json, Technology.class);
            technology.setIsActive("True");
            technology.save();
            return ok(Json.toJson(technology.getId()).toString());
        } catch (Exception e) {
            Logger.debug("Technology cannot be added: " + e.toString());
            return badRequest("Technology not saved: ");
        }
    }
    /************************************************* End of Add Technology *******************************************/


    /************************************************* Update Technology ***********************************************/
    /**
     * This method intends to update technology information.
     *
     * @param technologyId
     * @return
     */
    public Result updateTechnology(Long technologyId) {
        try {
            JsonNode json = request().body().asJson();
            if (json == null) {
                Logger.debug("Technology information not saved, expecting Json data from TechnologyController.updateTechnology");
                return badRequest("Technology information not saved, expecting Json data");
            }
            Technology updatedTechnology = Json.fromJson(json, Technology.class);
            updatedTechnology.update();
            return ok(Json.toJson(updatedTechnology));
        } catch (Exception e) {
            Logger.debug("Technology Profile not saved with id: " + technologyId + " with exception: " + e.toString());
            return badRequest("Technology Profile not saved: " + technologyId);
        }
    }


    /**
     * This method intends to delete technology pdf by technology id
     *
     * @param technologyId
     * @return
     */
    public Result deleteTechnologyPDF(Long technologyId) {
        try {
            Technology tT = Technology.find.byId(technologyId);
            Common.deleteFileFromS3(config, "project", "Pdf", technologyId);
            tT.setPdf("");
            tT.save();
        } catch (Exception e) {
            Logger.debug("Failed to set pdf for technology: " + e.toString());
            return Common.badRequestWrapper("Failed to add pdf to the technology");
        }

        return ok("success");
    }
    /************************************************* End of Update Technology ******************************************/

    /************************************************* Technology List ***************************************************/
    /**
     * Gets a list of all the technology based on optional offset and limit and sort
     *
     * @param pageLimit    shows the number of rows we want to receive
     * @param pageNum      shows the page number
     * @param sortCriteria shows based on what column we want to sort the data
     * @return a list of technologies
     * // TODO: Clean Common utitlity class for getSortCriteria(), not always register_time_stamp
     */
    public Result technologyList(Long userId, Integer pageLimit, Integer pageNum, Optional<String> sortCriteria) {
        List<Technology> activeTechnologies = new ArrayList<>();
        Set<Long> technologyIds = new HashSet<>();
        List<Technology> technologies;
        String sortOrder = Common.getSortCriteria(sortCriteria, TECHNOLOGY_DEFAULT_SORT_CRITERIA);

        int offset = pageLimit * (pageNum - 1);
        try {
            User user = User.find.byId(userId);


            activeTechnologies = Technology.find.query().where().eq("is_active", ACTIVE).findList();
            for (Technology technology : activeTechnologies) {
                technologyIds.add(technology.getId());
            }

            if (sortOrder.equals("id") || sortOrder.equals("access_times"))
                technologies = Technology.find.query().where().in("id", technologyIds).order().desc(sortOrder)
                        .findList();
            else
                technologies = Technology.find.query().where().in("id", technologyIds).orderBy(sortOrder)
                        .findList();
            RESTResponse response = technologyService.paginateResults(technologies, Optional.of(offset), Optional.
                    of(pageLimit), sortOrder);
            return ok(response.response());
        } catch (Exception e) {
            Logger.debug("TechnologyController.technologyList() exception: " + e.toString());
            return internalServerError("TechnologyController.technologyList() exception: " + e.toString());
        }
    }
    /************************************************* End of Technology List ********************************************/

    /************************************************* Get Technology ****************************************************/
    /**
     * Get a project detail by the technology id
     *
     * @param technologyId technology Id
     * @return ok if the technology is found; badRequest if the technology is not found
     */
    public Result getTechnologyById(Long technologyId) {
        if (technologyId == null) {
            return Common.badRequestWrapper("technologyId is null or empty.");
        }
        System.out.println("<112." + technologyId);

        if (technologyId == 0) return ok(Json.toJson(null));

        try {
            Technology technology = Technology.find.byId(technologyId);
System.out.println("<111 get technology by id:" + technology.toString());
            return ok(Json.toJson(technology));
        } catch (Exception e) {
            Logger.debug("TechnologyController.getTechnologyById() exception : " + e.toString());
            return internalServerError("Internal Server Error TechnologyController.getTechnologyById() exception: " +
                    e.toString());
        }
    }


    /************************************************* End of Get Technology *********************************************/



    /**
     * Check if the technology is search result
     *
     * @param technology     Technology being checked
     * @param title
     * @param goals
     * @param description
     * @return if the technology is search result.
     */
    private boolean isMatchedTechnology(Technology technology, String title, String goals, String description) {
        boolean titleInTitle = false;
        boolean goalInGoal = false;
        boolean locationInLocation = false;
        boolean descriptionInDescription = false;
        for (String titleSubWord : title.split(" ")) {
            titleSubWord = titleSubWord.trim();
            titleInTitle = title.equals("") || (technology.getTechnologyTitle() != null && technology.getTechnologyTitle().toLowerCase().
                    indexOf(titleSubWord.toLowerCase()) >= 0);
            if (titleInTitle)
                break;
        }
        for (String goalSubWord : goals.split(" ")) {
            goalSubWord = goalSubWord.trim();
            goalInGoal = goals.equals("") || (technology.getGoals() != null && technology.getGoals().toLowerCase().
                    indexOf(goalSubWord.toLowerCase()) >= 0);
            if (goalInGoal)
                break;
        }

        for (String descriptionSubWord : description.split(" ")) {
            descriptionSubWord = descriptionSubWord.trim();
            descriptionInDescription = description.equals("") || (technology.getShortDescription() != null &&
                    technology.getShortDescription().toLowerCase().indexOf(descriptionSubWord.toLowerCase()) >= 0);
            if (descriptionInDescription)
                return titleInTitle && goalInGoal && descriptionInDescription;
        }
        for (String descriptionSubWord : description.split(" ")) {
            descriptionSubWord = descriptionSubWord.trim();
            descriptionInDescription = description.equals("") || (technology.getLongDescription() != null &&
                    technology.getLongDescription().toLowerCase().indexOf(descriptionSubWord.toLowerCase()) >= 0);
            if (descriptionInDescription)
                return titleInTitle && goalInGoal && descriptionInDescription;
        }
        return titleInTitle && goalInGoal && descriptionInDescription;
    }

    /**
     * Filter the technologies based on title, goal, description
     *
     * @param title       technology list being filtered
     * @param goals
     * @param description
     * @return the list of filtered technologies.
     */
    private List<Technology> matchedTechnologyList(List<Technology> technologyList, String title, String goals,
                                             String description) {
        List<Technology> results = new ArrayList<>();
        for (Technology technology : technologyList) {
            if (isMatchedTechnology(technology, title, goals, description))
                results.add(technology);
        }
        return results;
    }


    /**
     * Find technologies by multiple condition, including title, goal, etc.
     *
     * @return technologies that match the condition
     * TODO: How to handle more conditions???
     */
    public Result searchTechnologiesByCondition() {
        String result = null;
        try {
            JsonNode json = request().body().asJson();
            List<Technology> technologies = new ArrayList<>();
            if (json == null) {
                return badRequest("Condition cannot be null");
            }
            //Get condition value from Json data

            String title = json.path("name").asText();

            String goals = json.path("goals").asText();

            String description = json.path("description").asText();

            String keywords = json.path("keywords").asText();
            //Search technologies by conditions
            if (keywords.trim().equals("")) {
                List<Technology> potentialTechnologies = Technology.find.query().where().eq("is_active", ACTIVE).
                        findList();
                technologies = matchedTechnologyList(potentialTechnologies, title, goals, description);

            } else {
                List<Technology> tmpTechnologies = Technology.find.query().where().eq("is_active", ACTIVE).findList();
                String[] keywordList = keywords.toLowerCase().trim().split(" ");

                for (String keyword : keywordList) {
                    keyword = keyword.toLowerCase();
                    for (Technology technology : tmpTechnologies) {
                        if ((technology.getTechnologyTitle() != null && technology.getTechnologyTitle().toLowerCase().contains(keyword)) ||
                                (technology.getGoals() != null && technology.getGoals().toLowerCase().contains(keyword))
                                || (technology.getShortDescription() != null && technology.getShortDescription().toLowerCase().contains
                                (keyword))
                                || (technology.getLongDescription() != null && technology.getLongDescription().toLowerCase().contains
                                (keyword))) {
                            technologies.add(technology);
                        }
                    }
                }
            }
            //If not found
            if (technologies == null || technologies.size() == 0) {
                Logger.info("Technologies not found with search conditions");
                return notFound("Technologies not found with conditions");
            }
            Set<Long> technologiesIdSet = new HashSet<>();
            List<Technology> filteredTechnologies = new ArrayList<>();
            for (Technology technology : technologies) {
                if (!technologiesIdSet.contains(technology.getId())) {
                    filteredTechnologies.add(technology);
                    technologiesIdSet.add(technology.getId());
                }
            }
            JsonNode jsonNode = Json.toJson(filteredTechnologies);
            result = jsonNode.toString();
        } catch (Exception e) {
            Logger.debug("TechnologyController.searchTechnologiesByCondition() exception: " + e.toString());
            return internalServerError("TechnologyController.searchTechnologiesByCondition() exception: " +
                    e.toString());
        }

        return ok(result);
    }



    /**
     * This method intends to set technology pdf by technology id
     *
     * @param technologyId
     * @return
     */
    public Result setPDF(Long technologyId) {
        if (request().body() == null || request().body().asRaw() == null) {
            return Common.badRequestWrapper("The request cannot be empty");
        }
        try {
            Technology tT = Technology.find.byId(technologyId);
            String url = Common.uploadFile(config, "technology", "Pdf", technologyId, request());
            tT.setPdf(url);
            tT.save();
        } catch (Exception e) {
            Logger.debug("Failed to set pdf for technology: " + e.toString());
            return Common.badRequestWrapper("Failed to add pdf to the technology");
        }

        // Return the app pdf.
        return ok("success");
    }

    public Result setFiles(Long serviceId, String fileName, String fileType) {
        if (request().body() == null || request().body().asRaw() == null) {
            return Common.badRequestWrapper("The request cannot be empty");
        }

        try {
            Technology service = Technology.find.byId(serviceId);
            if (service == null) {
                throw new RuntimeException("No file uploaded");
            }

            java.io.File file = request().body().asRaw().asFile();
            String url = S3Utils.uploadFile(AWS_BUCKET_NAME, file, AWS_FILE_NAME_PREFIX + "technology/" + serviceId + "." + fileType, fileType, "", "");
            if (url != null && !url.isEmpty()) {
                System.out.println("Uploaded file URL: " + url);
            } else {
                System.err.println("Failed to get the URL of the uploaded file.");
            }
            service.setPdf(url);
            service.update();
        } catch (Exception e) {
            Logger.debug("Failed to set file for serviceObject: " + e.toString());
            return Common.badRequestWrapper("Failed to add file to the service object");
        }
        return ok("success");
    }

    public Result getFileById(Long serviceId) {
        if (serviceId == null) {
            return Common.badRequestWrapper("serviceId is null or empty.");
        }

        if (serviceId == 0) return ok(Json.toJson(null));

        try {
            Technology service = Technology.find.query().where().eq("is_active", "True").where().eq("id", serviceId).findOne();
            if (service == null) {
                return notFound("File not found");
            }

            String fileUrl = service.getPdf();
            URL url = new URL(fileUrl);
            String bucketName = url.getHost().split("\\.")[0];
            String key = url.getPath().substring(1);

            String encodeContent = S3Utils.getObject(bucketName, key);

            Map<String, Object> result = new HashMap<>();
            result.put("fileDetails", Json.toJson(service));
            result.put("fileContent", encodeContent);

            return ok(Json.toJson(result));
        } catch (Exception e) {
            Logger.debug("BackendToolController.getFileById() exception : " + e.toString());
            return internalServerError("Internal Server Error BackendToolController.getFileById() exception: " + e.toString());
        }
    }



    /**
     * This method receives a technology id and deletes the technology by inactivating it (set is_active field to be false).
     *
     * @param technologyId given technology Id
     * @return ok or not found
     */
    public Result deleteTechnology(Long technologyId) {
        try {
            Technology technology = Technology.find.query().where(Expr.and(Expr.or(Expr.isNull("is_active"),
                    Expr.eq("is_active", "True")), Expr.eq("id", technologyId))).findOne();
            if (technology == null) {
                Logger.debug("In TechnologyController deleteTechnology(), cannot find technology" + technologyId);
                return notFound("From backend TechnologyController, technology not found with id: " + technologyId);
            }

            technology.setIsActive("False");
            technology.save();
            return ok();
        } catch (Exception e) {
            Logger.debug("Technology cannot be deleted for exception: " + e.toString());
            return Common.badRequestWrapper("Cannot delete technology for id: " + technologyId);
        }
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


    public Result checkTechnologyExist(Long technologyId) {
        try {
            Technology technology = Technology.find.byId(technologyId);
            ObjectNode objectNode = Json.newObject();

            if (technology == null) {
                objectNode.put("notExisted", "Technology does not exist");
            } else {
                objectNode.put("existed", technologyId);
            }
            return ok(objectNode);
        } catch (Exception e) {
            Logger.debug("TechnologyController.checkTechnologyExist exception: " + e.toString());
            return internalServerError("TechnologyController.checkTechnologyExist exception: " + e.toString());
        }
    }


}
