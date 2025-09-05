package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import controllers.routes;
import models.RAJob;
import models.User;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import utils.Constants;
import utils.RESTfulCalls;
import views.html.rajobList;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static play.mvc.Controller.session;
import static play.mvc.Results.ok;
import static play.mvc.Results.redirect;
import static utils.Common.beginIndexForPagination;
import static utils.Common.endIndexForPagination;

/**
 * This class intends to provide support for RAJobController.
 */
public class RAJobService {
    @Inject
    Config config;

    private final UserService userService;
    private Form<RAJob> RAJobForm;

    @Inject
    public RAJobService(UserService userService) {
        this.userService = userService;
    }


    /**
     * This method intends to get RAJob by id by calling backend APIs.
     *
     * @param rajobId
     * @return RAJob
     */
    public RAJob getRAJobById(Long rajobId) {
        RAJob rajob = null;

        try {
            JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.GET_RAJOB_BY_ID + rajobId));
            if (response == null || response.has("error")) {
                Logger.debug("RAJobService.getRAJobById() did not get RA job from backend with error.");
                return null;
            }

            rajob = RAJob.deserialize(response);
        } catch (Exception e) {
            Logger.debug("RAJobService.getRAJobById() exception: " + e.toString());
            return null;
        }
        return rajob;
    }


    /**
     * This method intends to save a pdf to RA job.
     *
     * @param body
     * @param rajobId: RA job id
     * @throws Exception
     */
    public void savePDFToRAJob(Http.MultipartFormData body, Long rajobId) throws Exception {
        try {
            if (body.getFile("pdf") != null) {
                Http.MultipartFormData.FilePart pdf = body.getFile("pdf");
                if (pdf != null && !pdf.getFilename().equals("")) {
                    File file = (File) pdf.getFile();
                    JsonNode pdfResponse = RESTfulCalls.postAPIWithFile(RESTfulCalls.getBackendAPIUrl(config,
                            Constants.SET_RAJOB_PDF + rajobId), file);
                }
            }
        } catch (Exception e) {
            Logger.debug("RAJobService.savePDFToJob exception: " + e.toString());
            throw e;
        }
    }


    /************************************************ Page Render Preparation ******************************************/
    /**
     * This private method renders the RA job list page.
     * Note that for performance consideration, the backend only passes back the RA jobs for the needed page stored in
     * the RAJobListJsonNode, together with the offset/count/total/sortCriteria information.
     *
     * @param rajobListJsonNode
     * @param pageLimit
     * @param searchBody
     * @param listType          : "all"; "search" (draw this page from list function or from search function)
     * @param username
     * @param userId
     * @return render challenge list page; If exception happened then render the homepage
     */
    public Result renderRAJobListPage(JsonNode rajobListJsonNode,
                                      int pageLimit,
                                      String searchBody,
                                      String listType,
                                      String username,
                                      Long userId) {
        try {
            // if no value is returned or error
            if (rajobListJsonNode == null || rajobListJsonNode.has("error")) {
                Logger.debug("RAJob list is empty or error!");
                return redirect(routes.Application.home());
            }

            JsonNode rajobsJsonArray = rajobListJsonNode.get("items");
            if (!rajobsJsonArray.isArray()) {
                Logger.debug("RAJob list is not array!");
                return redirect(routes.Application.home());
            }

            List<RAJob> rajobs = new ArrayList<>();
            for (int i = 0; i < rajobsJsonArray.size(); i++) {
                JsonNode json = rajobsJsonArray.path(i);
                RAJob rajob = RAJob.deserialize(json);
                rajobs.add(rajob);
            }

            // offset: starting index of the pageNum; count: the number of items in the pageNum;
            // total: the total number of items in all pages; sortCriteria: the sorting criteria (field)
            // pageNum: the current page number
            String sortCriteria = rajobListJsonNode.get("sort").asText();

            int total = rajobListJsonNode.get("total").asInt();
            int count = rajobListJsonNode.get("count").asInt();
            int offset = rajobListJsonNode.get("offset").asInt();
            int pageNum = offset / pageLimit + 1;

            int beginIndexPagination = beginIndexForPagination(pageLimit, total, pageNum);
            int endIndexPagination = endIndexForPagination(pageLimit, total, pageNum);

            return ok(rajobList.render(rajobs, pageNum, sortCriteria, offset, total, count,
                    listType, pageLimit, searchBody, userId, beginIndexPagination, endIndexPagination));
        } catch (Exception e) {
            Logger.debug("Exception in renderRAJobListPage:" + e.toString());
            e.printStackTrace();
            return redirect(routes.Application.home());
        }
    }


    /**************************************************** (De)Serialization *******************************************/
    /**
     * This method intends to prepare a json RA job from RA Job form.
     *
     * @param rajobForm: RA job registration form
     * @return
     * @throws Exception
     */
    public JsonNode serializeFormToJson(Form<RAJob> rajobForm) throws Exception {
        JsonNode jsonData = null;
        try {
            RAJob rajob = rajobForm.get();
            String longDescription = rajob.getLongDescription();
            if (longDescription != null) {
                longDescription.replaceAll(
                        "\n", "").replaceAll("\r", "");
            }

            if (rajob.getRajobPublisher() == null) {
                User user = new User(Long.parseLong(session("id")));
                rajob.setRajobPublisher(user);
            }
            return Json.toJson(rajob);
        } catch (Exception e) {
            Logger.debug("RAJobService.serializeFormToJson exception: " + e.toString());
            throw e;
        }
    }

}
