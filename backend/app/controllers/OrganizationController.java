package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import models.Challenge;
import models.User;
import models.Organization;
import models.rest.RESTResponse;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import services.OrganizationService;
import utils.Common;

import javax.crypto.KeyGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static utils.Constants.ACTIVE;

public class OrganizationController extends Controller {
    public static final String ORGANIZATION_DEFAULT_SORT_CRITERIA = "organization_name";
    @Inject
    Config config;

    private final OrganizationService organizationService;

    @Inject
    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }


    /*********************************************** Add Organization *********************************************************/
    /**
     * Registers a new organization provided the organization data in the request body.
     *
     * @return the organization id of the organization created.
     */
    public Result registerOrganization() {
        JsonNode json = request().body().asJson();
        if (json == null) {
            return Common.badRequestWrapper("Organization not created, expecting Json data");
        }

//        String organizationName = json.path("organizationName").asText();
//        String address = json.path("address").asText();
//        String focuses = json.path("focuses").asText();
//        String URL = json.path("URL").asText();
//        Long registrarId = json.path("registrarId").asLong();


        try {
            Organization organization = Json.fromJson(json, Organization.class);
            organization.setURL(json.path("URL").asText());
            String address = json.path("streetAddress1").asText() + "," + json.path("streetAddress2").asText();
            organization.setAddress(address);
            organization.save();
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode node = mapper.createObjectNode();
            node.put("id", organization.getId());
            return ok(node);
        } catch (Exception e) {
            Logger.debug("OrganizationController.addOrganization() exception " + e.toString());
            return notFound("Organization not added");
        }
    }
    /****************************************** End of Add Organization ***********************************************/

    /********************************************** Organization List *************************************************/
    /**
     * get all organizations
     * @return list of organization json node in RESTResponse
     */
    public Result organizationList(){
        try {
            List<Organization> organizations = Organization.find.query().findList();
            if(organizations.size() == 0) return notFound("Organization not found");
            RESTResponse response = new RESTResponse();
            ArrayNode organizationNodes = organizationService.organizationList2JsonArray(organizations);
            response.setItems(organizationNodes);
            return ok(response.response());
        } catch (Exception e) {
            e.printStackTrace();
            Logger.debug("OrganizationController.organizationList exception: " + e.toString());
            return notFound("Organization not found");
        }
    }


    /**
     * Gets a list of all the organizations based on optional offset and limit and sort
     *
     * @param pageLimit    shows the number of rows we want to receive
     * @param offset       shows the start index of the rows we want to receive
     * @param sortCriteria shows based on what column we want to sort the data
     * @return the list of organizations.
     */
    public Result organizationListPage(Optional<Integer> pageLimit, Optional<Integer> offset, Optional<String> sortCriteria) {
        String sortOrder = Common.getSortCriteria(sortCriteria, ORGANIZATION_DEFAULT_SORT_CRITERIA);

        try {
            List<Organization> organizations = Organization.find.query().orderBy(sortOrder).findList();
            if(organizations.size() == 0) return notFound("Organization not found");
            RESTResponse response = organizationService.paginateResults(organizations, offset, pageLimit, sortOrder);
            return ok(response.response());
        } catch (Exception e) {
            e.printStackTrace();
            Logger.debug("OrganizationController.organizationList exception: " + e.toString());
            return notFound("Organization not found");
        }
    }

    /********************************************** Organization List By Name *************************************************/
    /**
     * Gets a list of all the organizations based on name keywords
     * TODO
     * @param pageLimit    shows the number of rows we want to receive
     * @param offset       shows the start index of the rows we want to receive
     * @param sortCriteria shows based on what column we want to sort the data
     * @return the list of organizations.
     */
    public Result organizationListbyName(Optional<Integer> pageLimit, Optional<Integer> offset, Optional<String> sortCriteria) {
        String sortOrder = Common.getSortCriteria(sortCriteria, ORGANIZATION_DEFAULT_SORT_CRITERIA);
        try {
            JsonNode json = request().body().asJson();
            String queryName = json.get("organizationName").asText();
            List<Organization> organizations = Organization.find.query().where().like("organization_name", "%"+queryName+"%").findList();
            RESTResponse response = organizationService.paginateResults(organizations, offset, pageLimit, sortOrder);
            return ok(response.response());
        } catch (Exception e) {
            e.printStackTrace();
            Logger.debug("OrganizationController.organizationList exception: " + e.toString());
            return notFound("Organization not found");
        }
    }


    /********************************************* Organization Details ***********************************************/
    /**
     * get organization details by organization id
     * @param organizationId
     * @return organization
     */
    public Result organizationDetail(Long organizationId){
        try {

            Organization organization = Organization.find.query().where().eq("id", organizationId).findOne();
            if (organization == null) {
                Logger.info("User not found with organizationId: " + organizationId);
                return notFound("User not found with organizationId: " + organizationId);
            }
            JsonNode jsonNode = Json.toJson(organization);
            String result = jsonNode.toString();
            return ok(result);
        } catch (Exception e) {
            Logger.debug("UserController.organizationDetail exception: " + e.toString());
            return notFound("User was not found.");
        }
    }


    /********************************************* Organization update ***********************************************/
    /**
     * get organization details by organization id
     * @return organization
     */
    public Result organizationUpdate() {
        JsonNode json = request().body().asJson();
        Logger.debug("Received JSON data: " + json);

        if (json == null) {
            Logger.error("Request body is not in JSON format.");
            return Common.badRequestWrapper("Organization cannot be updated, expecting Json data");
        }

        try {
            Long organizationId = json.path("id").asLong();
            Logger.debug("Extracted organizationId: " + organizationId);

            Organization organization = Organization.find.query().where().eq("id", organizationId).findOne();
            if (organization == null) {
                Logger.error("No organization found with id: " + organizationId);
                return notFound("Organization not found");
            }
            Logger.debug("Organization found: " + organization);

            Logger.debug("Starting deserialization from JSON...");
            organization.deserializeFromJson(json);
            Logger.debug("Organization after deserialization: " + organization);
            Logger.debug("Deserialization completed.");

            Logger.debug("Saving organization...");
            organization.save();
            Logger.debug("Organization saved successfully with id: " + organization.getId());

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode node = mapper.createObjectNode();
            node.put("id", organization.getId());
            Logger.debug("Returning response JSON: " + node);

            return ok(node);
        } catch (Exception e) {
            Logger.error("OrganizationController.organizationUpdate() exception: " + e.toString(), e);
            return notFound("Organization not updated");
        }
    }


    /********************************************** Organization add users ********************************************/
    public Result addUsers() {
        try {
            JsonNode jsonData = request().body().asJson();
            if (jsonData == null) {
                Logger.debug("Users cannot be added to organization, expecting Json data");
                return badRequest("Users cannot be added to organization, expecting Json data");
            }

            JsonNode userIdArray = jsonData.get("userIdList");
            Long organizationId = jsonData.get("organizationId").asLong();
            List<User> userList = new ArrayList<>();
            for(JsonNode json : userIdArray){
                Long userId = json.asLong();
                User user = User.find.byId(userId);
                userList.add(user);
            }

            // If paper title, publicationChannel, and pages are exactly the same, it means the paper exists in the
            // database.
            Organization organization = Organization.find.query().where().eq("id", organizationId).findOne();
            organization.getUserPool().addAll(userList);
            organization.update();
            return ok(Json.toJson(organization.getId()).toString());
        } catch (Exception e) {
            e.printStackTrace();
            Logger.debug("Paper cannot be added: " + e.toString());
            return badRequest("Paper not saved: ");
        }
    }




}
