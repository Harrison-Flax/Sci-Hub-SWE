package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import controllers.routes;
import models.Job;
import models.Technology;
import models.User;
import models.Challenge;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import utils.Constants;
import utils.RESTfulCalls;
import views.html.challengeList;
import views.html.technologyList;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static play.mvc.Controller.session;
import static play.mvc.Results.ok;
import static play.mvc.Results.redirect;
import static utils.Common.beginIndexForPagination;
import static utils.Common.endIndexForPagination;

/**
 * This class intends to provide support for TechnologyController.
 */
public class TechnologyService {
    @Inject
    Config config;

    private final UserService userService;
    private Form<Technology> technologyForm;

    @Inject
    public TechnologyService(UserService userService) {
        this.userService = userService;
    }


    /**
     * This method intends to get Technology by id by calling backend APIs.
     *
     * @param technologyId
     * @return Technology
     */
    public Technology getTechnologyById(Long technologyId) {
        Technology technology = null;
        try {
            JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.GET_TECHNOLOGY_BY_ID + technologyId));
            if (response.has("error")) {
                Logger.debug("TechnologyService.getTechnologyById() did not get challenge from backend with error.");
                return null;
            }

            technology = Technology.deserialize(response);
System.out.println("<<<<<<1.1:" + technology.toString());
        } catch (Exception e) {
            Logger.debug("TechnologyService.getTechnologyById() exception: " + e.toString());
            return null;
        }
        return technology;
    }


    /**
     * This method intends to save a pdf to technology.
     *
     * @param body
     * @param technologyId: technology id
     * @throws Exception
     */
//    public void savePDFToChallenge(Http.MultipartFormData body, Long challengeId) throws Exception {
//        try {
//            if (body.getFile("pdf") != null) {
//                Http.MultipartFormData.FilePart pdf = body.getFile("pdf");
//                if (pdf != null && !pdf.getFilename().equals("")) {
//                    File file = (File) pdf.getFile();
//                    JsonNode pdfResponse = RESTfulCalls.postAPIWithFile(RESTfulCalls.getBackendAPIUrl(config,
//                            Constants.SET_Challenge_PDF + challengeId), file);
//                }
//            }
//        } catch (Exception e) {
//            Logger.debug("ChallengeService.savePDFToChallenge exception: " + e.toString());
//            throw e;
//        }
//    }




    /************************************************ Page Render Preparation *****************************************/
    /**
     * This private method renders the technology list page.
     * Note that for performance consideration, the backend only passes back the technologies for the needed page stored in
     * the TechnologyListJsonNode, together with the offset/count/total/sortCriteria information.
     *
     * @param technologyListJsonNode
     * @param currentTechnology
     * @param pageLimit
     * @param searchBody
     * @param listType            : "all"; "search" (draw this page from list function or from search function)
     * @param username
     * @param userId
     * @return render challenge list page; If exception happened then render the homepage
     */
    public Result renderTechnologyListPage(JsonNode technologyListJsonNode,
                                           int pageLimit,
                                          String searchBody,
                                          String listType,
                                          String username,
                                          Long userId) {
        try {
            // if no value is returned or error
            if (technologyListJsonNode == null || technologyListJsonNode.has("error")) {
                Logger.debug("Technology list is empty or error!");
                return redirect(routes.Application.home());
            }

            JsonNode technologiesJsonArray = technologyListJsonNode.get("items");
            if (!technologiesJsonArray.isArray()) {
                Logger.debug("Technology list is not array!");
                return redirect(routes.Application.home());
            }

            List<Technology> technologies = new ArrayList<>();
            for (int i = 0; i < technologiesJsonArray.size(); i++) {
                JsonNode json = technologiesJsonArray.path(i);
                Technology technology = Technology.deserialize(json);
                technologies.add(technology);
            }

            // offset: starting index of the pageNum; count: the number of items in the pageNum;
            // total: the total number of items in all pages; sortCriteria: the sorting criteria (field)
            // pageNum: the current page number
            String sortCriteria = technologyListJsonNode.get("sort").asText();

            int total = technologyListJsonNode.get("total").asInt();
            int count = technologyListJsonNode.get("count").asInt();
            int offset = technologyListJsonNode.get("offset").asInt();
            int pageNum = offset / pageLimit + 1;

            int beginIndexPagination = beginIndexForPagination(pageLimit, total, pageNum);
            int endIndexPagination = endIndexForPagination(pageLimit, total, pageNum);

            return ok(technologyList.render(technologies, pageNum, sortCriteria, offset, total, count,
                    listType, pageLimit, searchBody, userId, beginIndexPagination, endIndexPagination));
        } catch (Exception e) {
            Logger.debug("Exception in renderTechnologyListPage:" + e.toString());
            e.printStackTrace();
            return redirect(routes.Application.home());
        }
    }


    /**************************************************** (De)Serialization *******************************************/
    /**
     * This method intends to prepare a json object from Technology form.
     *
     * @param technologyForm: technology registration form
     * @return
     * @throws Exception
     */
    public JsonNode serializeFormToJson(Form<Technology> technologyForm) throws Exception {
        JsonNode jsonData = null;
        try {
            Technology technology = technologyForm.get();
            String longDescription = technology.getLongDescription();
            if (longDescription != null) {
                longDescription.replaceAll(
                        "\n", "").replaceAll("\r", "");
            }

            if (technology.getTechnologyPublisher()==null) {
                User user = new User(Long.parseLong(session("id")));
                technology.setTechnologyPublisher(user);
            }
            System.out.println("<<<1.2.2 after form from technology:" + technology.toString());
            return Json.toJson(technology);
        } catch (Exception e) {
            Logger.debug("TechnologyService.serializeFormToJson exception: " + e.toString());
            throw e;
        }
    }

}
