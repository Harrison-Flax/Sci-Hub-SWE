package controllers;

import actions.OperationLoggingAction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import models.*;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import services.AccessTimesService;
import services.FileService;
import services.JobService;
import services.UserService;
import utils.Constants;
import utils.RESTfulCalls;
import views.html.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static controllers.Application.checkLoginStatus;

public class AdminController extends Controller {

    @Inject
    Config config;

    private final UserService userService;
    private final FileService fileService;
    private final JobService jobService;
    private final AccessTimesService accessTimesService;

    @Inject
    public AdminController(UserService userService, JobService jobService, AccessTimesService accessTimesService,
                           FileService fileService) {
        this.userService = userService;
        this.jobService = jobService;
        this.accessTimesService = accessTimesService;
        this.fileService = fileService;
    }

    /**
     * checkAdminPermission
     */
    private boolean checkAdminPermission() {
        String userLevel = session().get("userTypes");
        return "0".equals(userLevel);
    }

    /***********************************************User Management*********************************************************/
    /**
     * user management page
     */
    @With(OperationLoggingAction.class)
    public Result userManagement(Integer pageNum, String sortCriteria) {
        checkLoginStatus();

        if (!checkAdminPermission()) {
            return forbidden("you do not have permission to access this page");
        }

        if (pageNum == null) pageNum = 1;
        if (sortCriteria == null) sortCriteria = "id";

        try {
            String apiUrl = RESTfulCalls.getBackendAPIUrl(config, "/admin/users") +
                    "?pageNum=" + pageNum + "&pageLimit=20&sortCriteria=" + sortCriteria;

            JsonNode response = RESTfulCalls.getAPI(apiUrl);

            if (response != null && response.has("users")) {
                List<User> users = new ArrayList<>();
                ArrayNode userArray = (ArrayNode) response.get("users");

                for (JsonNode userNode : userArray) {
                    User user = new User();
                    user.setId(userNode.get("id").asLong());
                    user.setUserName(userNode.get("userName").asText());
                    user.setFirstName(userNode.get("firstName").asText());
                    user.setLastName(userNode.get("lastName").asText());
                    user.setEmail(userNode.get("email").asText());
                    user.setOrganization(userNode.get("organization").asText());
                    user.setPhoneNumber(userNode.get("phoneNumber").asText());
                    user.setLevel(userNode.get("level").asText());
                    user.setIsActive(userNode.get("isActive").asText());
                    users.add(user);
                }

                int totalCount = response.get("totalCount").asInt();
                int totalPages = response.get("totalPages").asInt();

                return ok(userManagement.render(users, pageNum, sortCriteria, totalCount, totalPages));
            }

            return ok(userManagement.render(new ArrayList<>(), pageNum, sortCriteria, 0, 0));
        } catch (Exception e) {
            Logger.error("failed to get user list", e);
            return ok(userManagement.render(new ArrayList<>(), pageNum, sortCriteria, 0, 0));
        }
    }

    /**
     * user detail page
     */
    @With(OperationLoggingAction.class)
    public Result userDetail(Long userId) {
        checkLoginStatus();

        if (!checkAdminPermission()) {
            return forbidden("you do not have permission to access this page");
        }

        try {
            String apiUrl = RESTfulCalls.getBackendAPIUrl(config, "/admin/users/" + userId);
            JsonNode response = RESTfulCalls.getAPI(apiUrl);

            if (response != null) {
                return ok(userDetailAdminDash.render(response));
            }

            return notFound("user not exists");
        } catch (Exception e) {
            Logger.error("failed to get user detail", e);
            return notFound("user not exists");
        }
    }

    /*********************************************** job management *********************************************************/
    /**
     * job management page
     */
    @With(OperationLoggingAction.class)
    public Result jobManagement(Integer pageNum, String sortCriteria) {
        checkLoginStatus();

        if (!checkAdminPermission()) {
            return forbidden("you do not have permission to access this page");
        }

        if (pageNum == null) pageNum = 1;
        if (sortCriteria == null) sortCriteria = "id";

        try {
            String apiUrl = RESTfulCalls.getBackendAPIUrl(config, "/admin/jobs") +
                    "?pageNum=" + pageNum + "&pageLimit=20&sortCriteria=" + sortCriteria;

            JsonNode response = RESTfulCalls.getAPI(apiUrl);

            if (response != null && response.has("jobs")) {
                List<ObjectNode> jobs = new ArrayList<>();
                ArrayNode jobArray = (ArrayNode) response.get("jobs");

                for (JsonNode jobNode : jobArray) {
                    jobs.add((ObjectNode) jobNode);
                }

                int totalCount = response.get("totalCount").asInt();
                int totalPages = response.get("totalPages").asInt();

                return ok(jobManagement.render(jobs, pageNum, sortCriteria, totalCount, totalPages));
            }

            return ok(jobManagement.render(new ArrayList<>(), pageNum, sortCriteria, 0, 0));
        } catch (Exception e) {
            Logger.error("failed to get job list", e);
            return ok(jobManagement.render(new ArrayList<>(), pageNum, sortCriteria, 0, 0));
        }
    }

    /**
     *job detail page
     */
    @With(OperationLoggingAction.class)
    public Result jobDetail(String jobType, Long jobId) {
        checkLoginStatus();

        if (!checkAdminPermission()) {
            return forbidden("you do not have permission to access this page");
        }
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


    /*********************************************** organization management *********************************************************/
    /**
     * organization management page
     */
    @With(OperationLoggingAction.class)
    public Result organizationManagement(Integer pageNum, String sortCriteria) {
        checkLoginStatus();

        if (!checkAdminPermission()) {
            return forbidden("you do not have permission to access this page");
        }

        if (pageNum == null) pageNum = 1;
        if (sortCriteria == null) sortCriteria = "id";

        try {
            String apiUrl = RESTfulCalls.getBackendAPIUrl(config, "/admin/organizations") +
                    "?pageNum=" + pageNum + "&pageLimit=20&sortCriteria=" + sortCriteria;

            JsonNode response = RESTfulCalls.getAPI(apiUrl);

            if (response != null && response.has("organizations")) {
                List<Organization> organizations = new ArrayList<>();
                ArrayNode orgArray = (ArrayNode) response.get("organizations");

                for (JsonNode orgNode : orgArray) {
                    Organization org = new Organization();
                    org.setId(orgNode.get("id").asLong());
                    org.setOrganizationName(orgNode.get("organizationName").asText());
                    org.setAddress(orgNode.get("address").asText());
                    org.setFocuses(orgNode.get("focuses").asText());
                    org.setURL(orgNode.get("url").asText());
                    org.setNumberOfEmployees(orgNode.get("numberOfEmployees").asInt());
                    org.setContactPersonName(orgNode.get("contactPersonName").asText());
                    org.setContactPersonEmail(orgNode.get("contactPersonEmail").asText());
                    organizations.add(org);
                }

                int totalCount = response.get("totalCount").asInt();
                int totalPages = response.get("totalPages").asInt();

                return ok(organizationManagement.render(organizations, pageNum, sortCriteria, totalCount, totalPages));
            }

            return ok(organizationManagement.render(new ArrayList<>(), pageNum, sortCriteria, 0, 0));
        } catch (Exception e) {
            Logger.error("failed to get organization list", e);
            return ok(organizationManagement.render(new ArrayList<>(), pageNum, sortCriteria, 0, 0));
        }
    }

    /*********************************************** technology management *********************************************************/
    /**
     * technology management
     */
    @With(OperationLoggingAction.class)
    public Result technologyManagement(Integer pageNum, String sortCriteria) {
        checkLoginStatus();

        if (!checkAdminPermission()) {
            return forbidden("you do not have permission to access this page");
        }

        if (pageNum == null) pageNum = 1;
        if (sortCriteria == null) sortCriteria = "id";

        try {
            String apiUrl = RESTfulCalls.getBackendAPIUrl(config, "/admin/technologies") +
                    "?pageNum=" + pageNum + "&pageLimit=20&sortCriteria=" + sortCriteria;

            JsonNode response = RESTfulCalls.getAPI(apiUrl);

            if (response != null && response.has("technologies")) {
                List<Technology> technologies = new ArrayList<>();
                ArrayNode techArray = (ArrayNode) response.get("technologies");

                for (JsonNode techNode : techArray) {
                    Technology tech = new Technology();
                    tech.setId(techNode.get("id").asLong());
                    tech.setTechnologyTitle(techNode.get("technologyTitle").asText());
                    tech.setGoals(techNode.get("goals").asText());
                    tech.setShortDescription(techNode.get("shortDescription").asText());
                    tech.setKeywords(techNode.get("keywords").asText());
                    tech.setPIName(techNode.get("pIName").asText());
                    tech.setFields(techNode.get("fields").asText());
                    tech.setOrganizations(techNode.get("organizations").asText());
                    tech.setRegisteredTime(techNode.get("registeredTime").asText());
                    tech.setIsActive(techNode.get("isActive").asText());
                    technologies.add(tech);
                }

                int totalCount = response.get("totalCount").asInt();
                int totalPages = response.get("totalPages").asInt();

                return ok(technologyManagement.render(technologies, pageNum, sortCriteria, totalCount, totalPages));
            }

            return ok(technologyManagement.render(new ArrayList<>(), pageNum, sortCriteria, 0, 0));
        } catch (Exception e) {
            Logger.error("failed to get technology list", e);
            return ok(technologyManagement.render(new ArrayList<>(), pageNum, sortCriteria, 0, 0));
        }
    }


    /*********************************************** admin dashboard *********************************************************/
    /**
     * admin dashboard
     */
    @With(OperationLoggingAction.class)
    public Result dashboard() {
        checkLoginStatus();

        if (!checkAdminPermission()) {
            return forbidden("you do not have permission to access this page");
        }

        try {
            // get stats data
            ObjectNode stats = Json.newObject();

            // user count
            String userApiUrl = RESTfulCalls.getBackendAPIUrl(config, "/admin/users") + "?pageNum=1&pageLimit=1";
            JsonNode userResponse = RESTfulCalls.getAPI(userApiUrl);
            if (userResponse != null && userResponse.has("totalCount")) {
                stats.put("totalUsers", userResponse.get("totalCount").asInt());
            } else {
                stats.put("totalUsers", 0);
            }

            // job count
            String jobApiUrl = RESTfulCalls.getBackendAPIUrl(config, "/admin/jobs") + "?pageNum=1&pageLimit=1";
            JsonNode jobResponse = RESTfulCalls.getAPI(jobApiUrl);
            if (jobResponse != null && jobResponse.has("totalCount")) {
                stats.put("totalJobs", jobResponse.get("totalCount").asInt());
            } else {
                stats.put("totalJobs", 0);
            }

            // organization count
            String orgApiUrl = RESTfulCalls.getBackendAPIUrl(config, "/admin/organizations") + "?pageNum=1&pageLimit=1";
            JsonNode orgResponse = RESTfulCalls.getAPI(orgApiUrl);
            if (orgResponse != null && orgResponse.has("totalCount")) {
                stats.put("totalOrganizations", orgResponse.get("totalCount").asInt());
            } else {
                stats.put("totalOrganizations", 0);
            }

            // tech count
            String techApiUrl = RESTfulCalls.getBackendAPIUrl(config, "/admin/technologies") + "?pageNum=1&pageLimit=1";
            JsonNode techResponse = RESTfulCalls.getAPI(techApiUrl);
            if (techResponse != null && techResponse.has("totalCount")) {
                stats.put("totalTechnologies", techResponse.get("totalCount").asInt());
            } else {
                stats.put("totalTechnologies", 0);
            }

            return ok(adminDashboard.render(stats));
        } catch (Exception e) {
            Logger.error("failed to get admin dashboard", e);
            ObjectNode emptyStats = Json.newObject();
            emptyStats.put("totalUsers", 0);
            emptyStats.put("totalJobs", 0);
            emptyStats.put("totalPapers", 0);
            emptyStats.put("totalOrganizations", 0);
            emptyStats.put("totalTechnologies", 0);
            return ok(adminDashboard.render(emptyStats));
        }
    }
}