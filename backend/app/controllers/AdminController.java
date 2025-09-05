package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import models.*;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import services.UserService;
import utils.Common;
import utils.Constants;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class AdminController extends Controller {

    @Inject
    Config config;

    private final UserService userService;

    @Inject
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    /*********************************************** User Management *********************************************************/
    /**
     * get user list
     */
    public Result getUserList(Integer pageNum, Integer pageLimit, String sortCriteria) {
        if (pageNum == null) pageNum = 1;
        if (pageLimit == null) pageLimit = 20;
        if (sortCriteria == null) sortCriteria = "id";

        int offset = (pageNum - 1) * pageLimit;
        List<User> users;

        try {
            users = User.find.query()
                    .where().eq("is_active", "True")
                    .orderBy(sortCriteria)
                    .setFirstRow(offset)
                    .setMaxRows(pageLimit)
                    .findList();
            Logger.debug("AdminController.getUserList() fetched " + users.size() + " users");

            int totalCount = User.find.query().where().eq("is_active", "True").findCount();

            ArrayNode userArray = Json.newArray();
            for (User user : users) {
                ObjectNode userNode = Json.newObject();
                userNode.put("id", user.getId());
                userNode.put("userName", user.getUserName());
                userNode.put("firstName", user.getFirstName());
                userNode.put("lastName", user.getLastName());
                userNode.put("email", user.getEmail());
                userNode.put("organization", user.getOrganization() != null ? user.getOrganization() : "");
                userNode.put("phoneNumber", user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
                userNode.put("level", user.getLevel() != null ? user.getLevel() : "");
                userNode.put("createTime", user.getCreateTime() != null ? user.getCreateTime() : "");
                userNode.put("isActive", user.getIsActive() != null ? user.getIsActive() : "");

                // user type

                String userType = "";
                if (user.getUserType() == 4) userType += "Student ";
                if (user.getUserType() == 1) userType += "Researcher ";
                if (user.getUserType() == 0) userType += "Sponsor";
                userNode.put("userType", userType.trim());
                userArray.add(userNode);
            }

            ObjectNode result = Json.newObject();
            result.set("users", userArray);
            result.put("totalCount", totalCount);
            result.put("pageNum", pageNum);
            result.put("pageLimit", pageLimit);
            result.put("totalPages", (totalCount + pageLimit - 1) / pageLimit);

            return ok(result);
        } catch (Exception e) {
            Logger.error("failed to load user list", e);
            return Common.badRequestWrapper("failed to load user list");
        }
    }

    /**
     * get user detail
     */
    public Result getUserDetail(Long userId) {
        try {
            User user = User.find.byId(userId);
            if (user == null) {
                return notFound("user not exist");
            }

            ObjectNode result = Json.newObject();

            // General information
            ObjectNode generalInfo = Json.newObject();
            generalInfo.put("id", user.getId());
            generalInfo.put("userName", user.getUserName());
            generalInfo.put("firstName", user.getFirstName());
            generalInfo.put("lastName", user.getLastName());
            generalInfo.put("middleInitial", user.getMiddleInitial() != null ? user.getMiddleInitial() : "");
            generalInfo.put("email", user.getEmail());
            generalInfo.put("phoneNumber", user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
            generalInfo.put("organization", user.getOrganization() != null ? user.getOrganization() : "");
            generalInfo.put("mailingAddress", user.getMailingAddress() != null ? user.getMailingAddress() : "");
            generalInfo.put("homepage", user.getHomepage() != null ? user.getHomepage() : "");
            generalInfo.put("level", user.getLevel() != null ? user.getLevel() : "");
            generalInfo.put("createTime", user.getCreateTime() != null ? user.getCreateTime() : "");
            generalInfo.put("isActive", user.getIsActive() != null ? user.getIsActive() : "");

            result.set("generalInfo", generalInfo);

            // Specific information based on user type
            if (user.getUserType() == 4 && user.getStudentInfo() != null) {
                ObjectNode studentInfo = Json.newObject();
                studentInfo.put("idNumber", user.getStudentInfo().getIdNumber() != null ? user.getStudentInfo().getIdNumber() : "");
                studentInfo.put("studentYear", user.getStudentInfo().getStudentYear() != null ? user.getStudentInfo().getStudentYear() : "");
                studentInfo.put("studentType", user.getStudentInfo().getStudentType() != null ? user.getStudentInfo().getStudentType() : "");
                studentInfo.put("major", user.getStudentInfo().getMajor() != null ? user.getStudentInfo().getMajor() : "");
                studentInfo.put("firstEnrollDate", user.getStudentInfo().getFirstEnrollDate() != null ? user.getStudentInfo().getFirstEnrollDate() : "");
                result.set("studentInfo", studentInfo);
            }

            if (user.getUserType() == 1 && user.getResearcherInfo() != null) {
                ObjectNode researcherInfo = Json.newObject();
                researcherInfo.put("highestDegree", user.getResearcherInfo().getHighestDegree() != null ? user.getResearcherInfo().getHighestDegree() : "");
                researcherInfo.put("orcid", user.getResearcherInfo().getOrcid() != null ? user.getResearcherInfo().getOrcid() : "");
                researcherInfo.put("researchFields", user.getResearcherInfo().getResearchFields() != null ? user.getResearcherInfo().getResearchFields() : "");
                researcherInfo.put("school", user.getResearcherInfo().getSchool() != null ? user.getResearcherInfo().getSchool() : "");
                researcherInfo.put("department", user.getResearcherInfo().getDepartment() != null ? user.getResearcherInfo().getDepartment() : "");
                result.set("researcherInfo", researcherInfo);
            }

            if (user.getUserType() == 2) {
                ObjectNode sponsorInfo = Json.newObject();
                sponsorInfo.put("expertises", user.getExpertises() != null ? user.getExpertises() : "");
                sponsorInfo.put("categories", user.getCategories() != null ? user.getCategories() : "");
                sponsorInfo.put("detail", user.getDetail() != null ? user.getDetail() : "");
                result.set("sponsorInfo", sponsorInfo);
            }

            return ok(result);
        } catch (Exception e) {
            Logger.error("fail to get user detail", e);
            return Common.badRequestWrapper("fail to get user detail");
        }
    }

    /**
     * update user status
     */
    public Result updateUserStatus(Long userId) {
        try {
            JsonNode json = request().body().asJson();
            if (json == null) {
                return Common.badRequestWrapper("request can not be null");
            }

            User user = User.find.byId(userId);
            if (user == null) {
                return notFound("user not exsit");
            }

            if (json.has("isActive")) {
                user.setIsActive(json.get("isActive").asText());
            }
            if (json.has("level")) {
                user.setLevel(json.get("level").asText());
            }

            user.save();

            return ok(Json.toJson("user status updated"));
        } catch (Exception e) {
            Logger.error("failed to update user status", e);
            return Common.badRequestWrapper("failed to update user status");
        }
    }

    /*********************************************** job management *********************************************************/
    /**
     * get job list and ra job list
     */
    public Result getJobList(Integer pageNum, Integer pageLimit, String sortCriteria) {
        if (pageNum == null) pageNum = 1;
        if (pageLimit == null) pageLimit = 20;
        if (sortCriteria == null) sortCriteria = "id";

        int offset = (pageNum - 1) * pageLimit;

        try {
            ArrayNode jobArray = Json.newArray();

            // 获取Job列表
            List<Job> jobs = Job.find.query()
                    .orderBy(sortCriteria)
                    .setFirstRow(offset)
                    .setMaxRows(pageLimit)
                    .findList();

            for (Job job : jobs) {
                ObjectNode jobNode = Json.newObject();
                jobNode.put("id", job.getId());
                jobNode.put("type", "Job");
                jobNode.put("title", job.getTitle());
                jobNode.put("organization", job.getOrganization() != null ? job.getOrganization() : "");
                jobNode.put("location", job.getLocation() != null ? job.getLocation() : "");
                jobNode.put("publishDate", job.getPublishDate() != null ? job.getPublishDate() : "");
                jobNode.put("status", job.getStatus() != null ? job.getStatus() : "");
                jobNode.put("numberOfApplicants", job.getNumberOfApplicants());
                jobNode.put("publisher", job.getJobPublisher() != null ? job.getJobPublisher().getUserName() : "");
                jobNode.put("isActive", job.getIsActive());
                jobArray.add(jobNode);
            }

            // get ra job list
            List<RAJob> raJobs = RAJob.find.query()
                    .orderBy(sortCriteria)
                    .setFirstRow(offset)
                    .setMaxRows(pageLimit)
                    .findList();

            for (RAJob raJob : raJobs) {
                ObjectNode jobNode = Json.newObject();
                jobNode.put("id", raJob.getId());
                jobNode.put("type", "RAJob");
                jobNode.put("title", raJob.getTitle());
                jobNode.put("organization", raJob.getOrganization() != null ? raJob.getOrganization() : "");
                jobNode.put("location", raJob.getLocation() != null ? raJob.getLocation() : "");
                jobNode.put("publishDate", raJob.getPublishDate() != null ? raJob.getPublishDate() : "");
                jobNode.put("status", raJob.getStatus() != null ? raJob.getStatus() : "");
                jobNode.put("numberOfApplicants", raJob.getNumberOfApplicants());
                jobNode.put("publisher", raJob.getRajobPublisher() != null ? raJob.getRajobPublisher().getUserName() : "");
                jobNode.put("isActive", raJob.getIsActive());
                jobArray.add(jobNode);
            }

            int totalJobs = Job.find.query().findCount();
            int totalRAJobs = RAJob.find.query().findCount();
            int totalCount = totalJobs + totalRAJobs;

            ObjectNode result = Json.newObject();
            result.set("jobs", jobArray);
            result.put("totalCount", totalCount);
            result.put("pageNum", pageNum);
            result.put("pageLimit", pageLimit);
            result.put("totalPages", (totalCount + pageLimit - 1) / pageLimit);

            return ok(result);
        } catch (Exception e) {
            Logger.error("failed to get job list", e);
            return Common.badRequestWrapper("failed to get job list");
        }
    }

    /**
     * get job details
     */
    public Result getJobDetail(String jobType, Long jobId) {
        try {
            ObjectNode result = Json.newObject();

            if ("Job".equals(jobType)) {
                Job job = Job.find.byId(jobId);
                if (job == null) {
                    return notFound("job not exsits");
                }
                result = (ObjectNode) Json.toJson(job);
            } else if ("RAJob".equals(jobType)) {
                RAJob raJob = RAJob.find.byId(jobId);
                if (raJob == null) {
                    return notFound("ra job not exist");
                }
                result = (ObjectNode) Json.toJson(raJob);
            } else {
                return Common.badRequestWrapper("wrong job type");
            }

            return ok(result);
        } catch (Exception e) {
            Logger.error("failed to get job detail", e);
            return Common.badRequestWrapper("failed to get job detail");
        }
    }


    /*********************************************** organization management *********************************************************/
    /**
     * get organization list
     */
    public Result getOrganizationList(Integer pageNum, Integer pageLimit, String sortCriteria) {
        if (pageNum == null) pageNum = 1;
        if (pageLimit == null) pageLimit = 20;
        if (sortCriteria == null) sortCriteria = "id";

        int offset = (pageNum - 1) * pageLimit;

        try {
            List<Organization> organizations = Organization.find.query()
                    .orderBy(sortCriteria)
                    .setFirstRow(offset)
                    .setMaxRows(pageLimit)
                    .findList();

            ArrayNode orgArray = Json.newArray();
            for (Organization org : organizations) {
                ObjectNode orgNode = Json.newObject();
                orgNode.put("id", org.getId());
                orgNode.put("organizationName", org.getOrganizationName());
                orgNode.put("address", org.getAddress() != null ? org.getAddress() : "");
                orgNode.put("focuses", org.getFocuses() != null ? org.getFocuses() : "");
                orgNode.put("url", org.getURL() != null ? org.getURL() : "");
                orgNode.put("numberOfEmployees", org.getNumberOfEmployees());
                orgNode.put("contactPersonName", org.getContactPersonName() != null ? org.getContactPersonName() : "");
                orgNode.put("contactPersonEmail", org.getContactPersonEmail() != null ? org.getContactPersonEmail() : "");
                orgArray.add(orgNode);
            }

            int totalCount = Organization.find.query().findCount();

            ObjectNode result = Json.newObject();
            result.set("organizations", orgArray);
            result.put("totalCount", totalCount);
            result.put("pageNum", pageNum);
            result.put("pageLimit", pageLimit);
            result.put("totalPages", (totalCount + pageLimit - 1) / pageLimit);

            return ok(result);
        } catch (Exception e) {
            Logger.error("failed to get organization list", e);
            return Common.badRequestWrapper("failed to get organization list");
        }
    }

    /*********************************************** technology management *********************************************************/
    /**
     * get technologylist
     */
    public Result getTechnologyList(Integer pageNum, Integer pageLimit, String sortCriteria) {
        if (pageNum == null) pageNum = 1;
        if (pageLimit == null) pageLimit = 20;
        if (sortCriteria == null) sortCriteria = "id";

        int offset = (pageNum - 1) * pageLimit;

        try {
            List<Technology> technologies = Technology.find.query()
                    .where().eq("is_active", "True")
                    .orderBy(sortCriteria)
                    .setFirstRow(offset)
                    .setMaxRows(pageLimit)
                    .findList();

            ArrayNode techArray = Json.newArray();
            for (Technology tech : technologies) {
                ObjectNode techNode = Json.newObject();
                techNode.put("id", tech.getId());
                techNode.put("technologyTitle", tech.getTechnologyTitle());
                techNode.put("goals", tech.getGoals() != null ? tech.getGoals() : "");
                techNode.put("shortDescription", tech.getShortDescription() != null ? tech.getShortDescription() : "");
                techNode.put("keywords", tech.getKeywords() != null ? tech.getKeywords() : "");
                techNode.put("pIName", tech.getPIName() != null ? tech.getPIName() : "");
                techNode.put("fields", tech.getFields() != null ? tech.getFields() : "");
                techNode.put("organizations", tech.getOrganizations() != null ? tech.getOrganizations() : "");
                techNode.put("registeredTime", tech.getRegisteredTime() != null ? tech.getRegisteredTime() : "");
                techNode.put("isActive", tech.getIsActive());
                techArray.add(techNode);
            }

            int totalCount = Technology.find.query().where().eq("is_active", "True").findCount();

            ObjectNode result = Json.newObject();
            result.set("technologies", techArray);
            result.put("totalCount", totalCount);
            result.put("pageNum", pageNum);
            result.put("pageLimit", pageLimit);
            result.put("totalPages", (totalCount + pageLimit - 1) / pageLimit);

            return ok(result);
        } catch (Exception e) {
            Logger.error("failed to get technology list", e);
            return Common.badRequestWrapper("failed to get technology list");
        }
    }
}