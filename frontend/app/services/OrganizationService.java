package services;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.typesafe.config.Config;
import controllers.routes;
import models.Organization;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import utils.Constants;
import utils.RESTfulCalls;
import views.html.authorList;
import views.html.organizationList;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static controllers.Application.checkLoginStatus;
import static controllers.Application.isPrivateProjectZone;
import static play.mvc.Results.ok;
import static play.mvc.Results.redirect;
import static utils.Common.beginIndexForPagination;
import static utils.Common.endIndexForPagination;

public class OrganizationService {

    @Inject
    Config config;

    /**
     * This method return current organization details
     *
     * @param organizationForm given organization form
     * @return organization's information in json format
     */
    public ObjectNode createJsonFromOrganizationForm(Form<Organization> organizationForm) {
        ObjectNode jsonData = null;
        try {
            Map<String, String> tmpMap = organizationForm.data();
            jsonData = (ObjectNode) (Json.toJson(tmpMap));
        } catch (Exception e) {
            Logger.debug("ServiceService.generateJsonNodeFromForm exception: " + e.toString());
            throw e;
        }
        return jsonData;
    }

    /**
     * This method used for adding users to organization
     * TODO: this function need to validate user's role, or we need to divide it into send request to target user
     * @param userIdList: list of users' id
     * @param organizationId
     * @return
     */
    public void addUsersToOrganization(List<Long> userIdList, Long organizationId) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            ArrayNode array = mapper.valueToTree(userIdList);
            ObjectNode userIdListData = mapper.createObjectNode();
            userIdListData.putArray("userIdList").addAll(array);
            userIdListData.put("organizationId", organizationId);
            JsonNode userListRes = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.ADD_USERS_ORGANIZATION), userIdListData);
        } catch (Exception e) {
            Logger.debug("OrganizationService.addUsersToOrganization exception:  " + e.toString());
            throw e;
        }
    }

    /**
     *
     * @return list of organizations
     */
    public List<Organization> organizationList(){
        List<Organization> organizations = new ArrayList<>();
        try {
            JsonNode organizationNodeList = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.ORGANIZATION_LIST));

            if (organizationNodeList == null || organizationNodeList.has("error")) {
                return organizations;
            }
            JsonNode organizationJsonArray = organizationNodeList.get("items");
            if (!organizationJsonArray.isArray()) {
                Logger.debug("Organization list is not array!");
                return organizations;
            }
            for (JsonNode json : organizationJsonArray) {
                Organization organization = Organization.deserialize(json);
                organizations.add(organization);
            }
        } catch (Exception e) {
            Logger.debug("UserService.renderUserListPage exception: " + e.toString());
        } finally {
            return organizations;
        }
    }

    /**
     * This method is used for getting user's organization
     * @param userId
     * @return: List of Organization items
     */
    public List<Organization> organizationListByUser(Long userId) {
        checkLoginStatus();
        List<Organization> organizations = new ArrayList<>();
        try {
            JsonNode organizationNodeList = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.ORGANIZATION_LIST_BY_USER + userId));

            if (organizationNodeList == null || organizationNodeList.has("error")) {
                return organizations;
            }
            JsonNode organizationJsonArray = organizationNodeList.get("organizations");
            if (!organizationJsonArray.isArray()) {
                Logger.debug("Organization list is not array!");
                return organizations;
            }
            for (JsonNode json : organizationJsonArray) {
                Organization organization = Organization.deserialize(json);
                organizations.add(organization);
            }
        } catch (Exception e) {
            Logger.debug("UserService.renderUserListPage exception: " + e.toString());
        } finally {
            return organizations;
        }
    }


    /**
     * This method renders the organization list page.
     *
     * @param organizationListJsonNode
     * @param isCallerMySpacePage shows if the user list page is rendered from my space page or not
     * @param pageLimit
     * @param searchBody
     * @param listType            : "all"; "my follow"; "user publish"; "search"
     * @param username
     * @param userId
     * @return render user list page; If exception happened render homepage
     */
    public Result renderOrganizationListPage(JsonNode organizationListJsonNode, boolean isCallerMySpacePage, int pageLimit,
                                       String searchBody, String listType, String username, Long userId) {
        try {
            if (organizationListJsonNode == null || organizationListJsonNode.has("error")) {
                Logger.debug("User list is empty or error!");
                return redirect(routes.Application.home());
            }

            JsonNode organizationJsonArray = organizationListJsonNode.get("items");

            if (!organizationJsonArray.isArray()) {
                Logger.debug("User list is not array!");
                return redirect(routes.Application.home());
            }

            List<Organization> organizations = new ArrayList<>();

            for (JsonNode json : organizationJsonArray) {
                Organization organization = Organization.deserialize(json);
                organizations.add(organization);
            }

            // Offset
            String retSort = organizationListJsonNode.get("sort").asText();
            int total = organizationListJsonNode.get("total").asInt();
            int count = organizationListJsonNode.get("count").asInt();
            int offset = organizationListJsonNode.get("offset").asInt();
            int page = offset / pageLimit + 1;
            int beginIndexPagination = beginIndexForPagination(pageLimit, total, page);
            int endIndexPagination = endIndexForPagination(pageLimit, total, page);
            return ok(organizationList.render(organizations,
                    isPrivateProjectZone(), isCallerMySpacePage, page, retSort, offset, total, count, listType,
                    pageLimit, searchBody, username, userId, beginIndexPagination, endIndexPagination));
        } catch (Exception e) {
            Logger.debug("AuthorService.renderUserListPage exception: " + e.toString());
            return redirect(routes.Application.home());
        }
    }

}
