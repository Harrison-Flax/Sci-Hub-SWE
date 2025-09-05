package controllers;

import actions.OperationLoggingAction;
import ch.qos.logback.core.net.SyslogOutputStream;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import models.Organization;
import models.User;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import utils.Constants;
import utils.RESTfulCalls;
import views.html.*;
import services.OrganizationService;

import javax.inject.Inject;

import java.util.List;
import java.util.Map;

import static controllers.Application.checkLoginStatus;
import static controllers.Application.isPrivateProjectZone;
import static utils.Constants.CALLER_IS_NOT_MY_SPACE_PAGE;

public class OrganizationController extends Controller {

    @Inject
    Config config;


    private final OrganizationService organizationService;
    private Form<Organization> organizationForm;
    private Organization organization;
    private FormFactory myFactory;

    /******************************* Constructor **********************************************************************/
    @Inject
    public OrganizationController(FormFactory factory,
                                  OrganizationService organizationService) {
        organizationForm = factory.form(Organization.class);
        myFactory = factory;

        this.organizationService = organizationService;
    }

    private boolean checkAdminPermission() {
        String userLevel = session().get("userTypes");
        return "0".equals(userLevel);
    }

    /************************************************** Organization Registration Pages ***********************************/

    public Result organizationRegisterPage(){return ok(organizationRegister.render(organizationForm, Constants.PATTERN_RULES));}


    public Result organizationRegisterPOST() {
        checkLoginStatus();
        try{
            Long registrarId = Long.valueOf(session("id"));
            System.out.println("Registrar id :" + registrarId);
            Form<Organization> organizationForm = this.organizationForm.bindFromRequest();
            ObjectNode jsonData = organizationService.createJsonFromOrganizationForm(organizationForm);
            jsonData.put("registrarId", registrarId);
            jsonData.put("level", "normal");
            System.out.println(jsonData);
            JsonNode response = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.ORGANIZATION_REGISTER_POST), jsonData);
            String newOrganizationId = response.get("id").asText();
            if (newOrganizationId != null) {
                return ok(registerConfirmation.render(new Long(newOrganizationId), "Organization"));
            } else {
                Logger.debug("OrganizationController create organization backend error");
                return ok(registrationError.render("Organization"));
            }
        } catch (Exception e) {
            Logger.debug("OrganizationController create on exception: " + e.toString());
            return ok(registrationError.render("Organization"));
        }
    }


    /**
     * This method intends to render an empty search.scala.html page
     *
     * @return
     */
    public Result searchPage() {
        checkLoginStatus();
        return ok(search.render("organization"));
    }

    /**
     * This method intends to prepare data for rending organization research result page
     *
     * @param pageNum
     * @param sortCriteria
     * @return: data prepared for organizationList.scala.html (same as show all organization list page)
     */
    public Result searchPOST(Integer pageNum, String sortCriteria) {
        checkLoginStatus();

        try {
            Form<Organization> tmpForm = organizationForm.bindFromRequest();
            Map<String, String> tmpMap = tmpForm.data();

            if(isPrivateProjectZone()){
                tmpMap.put("organzationId", session("id"));
            }

            JsonNode searchJson = Json.toJson(tmpMap);
            String searchString = "";

            // if not coming from search input page, then should fetch searchJson from the form from key "searchString"
            if (tmpMap.get("searchString") != null) {
                searchString = tmpMap.get("searchString");
                searchJson = Json.parse(searchString);
            } else {
                searchString = Json.stringify(searchJson);
            }
            int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
            int offset = pageLimit * (pageNum - 1);
            JsonNode organizationsJsonNode = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.ORGANIZATION_LIST_BY_NAME + "?offset=" + offset + "&pageLimit=" + pageLimit +
                            "&sortCriteria=" + sortCriteria), searchJson);
            return organizationService.renderOrganizationListPage(organizationsJsonNode, CALLER_IS_NOT_MY_SPACE_PAGE, pageLimit, searchString,
                    "search", session("organzationname"), Long.parseLong(session("id")));
        } catch (Exception e) {
            Logger.debug("OrganizationController.searchPOST exception: " + e.toString());
            return redirect(routes.Application.home());
        }
    }


    /************************************************** Organization Details Page *********************************************/
    /**
     * This method for get details of organization from backend
     * @param organizationId
     * @return organization
     */
    public Result organizationDetailPage(Long organizationId){
        checkLoginStatus();
        try{
            JsonNode organizationNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.ORGANIZATION_DETAIL+organizationId));
            organization = Organization.deserialize(organizationNode);
            String userOrganization = session("organization");
            return ok(organizationDetail.render(organization, userOrganization));
        }catch (Exception e) {
            Logger.debug("OrganizationController.organizationDetailPage exception: " + e.toString());
            return ok(generalError.render());
        }
    }

    public Result organizationDetailPageAdmin(Long organizationId){
        checkLoginStatus();
        if (!checkAdminPermission()) {
            return forbidden("you do not have permission to access this page");
        }

        try{
            JsonNode organizationNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.ORGANIZATION_DETAIL+organizationId));
            organization = Organization.deserialize(organizationNode);
            String userOrganization = session("organization");
            return ok(organizationDetaiAdmin.render(organization, userOrganization));
        }catch (Exception e) {
            Logger.debug("OrganizationController.organizationDetailPage exception: " + e.toString());
            return ok(generalError.render());
        }
    }

    public Result organizationEditPage(){
        return ok(organizationEditPage.render(organization, organizationForm, Constants.PATTERN_RULES));
    }

    /**
     * send updated organization to Backend
     * TODO: add editor verification
     * @return
     */
    public Result organizationEditPOST(Long organizationId) {
        checkLoginStatus();
        try{
            Long registrarId = Long.valueOf(session("id"));
            Form<Organization> organizationForm = this.organizationForm.bindFromRequest();
            ObjectNode jsonData = organizationService.createJsonFromOrganizationForm(organizationForm);
            jsonData.put("id", organizationId);
            jsonData.put("level", "normal");
            // System.out.println(jsonData);
            Logger.info("JSON data to be sent to backend: " + jsonData.toString());
            JsonNode response = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.ORGANIZATION_EDIT_POST), jsonData);
            String edittedOrganizationId = response.get("id").asText();
            if (edittedOrganizationId != null) {
                return ok(registerConfirmation.render(new Long(edittedOrganizationId), "Organization"));
            } else {
                Logger.debug("OrganizationController create organization backend error");
                return ok(registrationError.render("Organization"));
            }
        } catch (Exception e) {
            Logger.debug("OrganizationController create on exception: " + e.toString());
            return ok(registrationError.render("Organization"));
        }
    }
    /**
     * This method intends to prepare data to render the pageNum of listing all organzations with
     * pagination (authorList.scala.html)
     * @param pageNum:      currrent page number
     * @param sortCriteria: sort column
     * @return: data for authorList.scala.html
     */
    @With(OperationLoggingAction.class)
    public Result organizationListPage(Integer pageNum, String sortCriteria) {
        checkLoginStatus();
        try {
            // Set the offset and pageLimit.
            int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
            int offset = pageLimit * (pageNum - 1);

            JsonNode organizationsJsonNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.ORGANIZATION_LIST_PAGE + "?offset=" + offset + "&pageLimit=" +
                            pageLimit + "&sortCriteria=" + sortCriteria));
            return organizationService.renderOrganizationListPage(organizationsJsonNode, CALLER_IS_NOT_MY_SPACE_PAGE, pageLimit, null,
                    "all", session("organzationname"), Long.parseLong(session("id")));
        } catch (Exception e) {
            Logger.debug("OrganizationController.organzationList exception: " + e.toString());
//            Application.flashMsg(RESTfulCalls.createOrganizationResponse(RESTfulCalls.OrganizationResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }




}