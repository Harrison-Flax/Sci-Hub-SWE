package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import controllers.routes;
import models.TAJob;
import models.User;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import utils.Constants;
import utils.RESTfulCalls;
import views.html.tajobList;

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
 * This class intends to provide support for TAJobController.
 */
public class TAJobService {
    @Inject
    Config config;

    private final UserService userService;
    private Form<TAJob> TAJobForm;

    @Inject
    public TAJobService(UserService userService) {
        this.userService = userService;
    }


    /**
     * This method intends to get TAJob by id by calling backend APIs.
     *
     * @patam tajobId
     * @return TAJob
     */
    public TAJob getTAJobById(Long tajobId) {
        TAJob tajob = null;
        try {
            JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.GET_TAJOB_BY_ID + tajobId));
            if (response.has("error")) {
                Logger.debug("TAJobService.getTAJobById() did not get TA job from backend with error.");
                return null;
            }

            tajob = TAJob.deserialize(response);
        } catch (Exception e) {
            Logger.debug("TAJobService.getTAJobById() exception: " + e.toString());
            return null;
        }
        return tajob;
    }


    /**
     * This method intends to save a pdf to TA job.
     *
     * @patam body
     * @patam tajobId: TA job id
     * @throws Exception
     */
    public void savePDFToTAJob(Http.MultipartFormData body, Long tajobId) throws Exception {
        try {
            if (body.getFile("pdf") != null) {
                Http.MultipartFormData.FilePart pdf = body.getFile("pdf");
                if (pdf != null && !pdf.getFilename().equals("")) {
                    File file = (File) pdf.getFile();
                    JsonNode pdfResponse = RESTfulCalls.postAPIWithFile(RESTfulCalls.getBackendAPIUrl(config,
                            Constants.SET_TAJOB_PDF + tajobId), file);
                }
            }
        } catch (Exception e) {
            Logger.debug("TAJobService.savePDFToJob exception: " + e.toString());
            throw e;
        }
    }


    /************************************************ Page Render Prepatation ******************************************/
    /**
     * This private method renders the TA job list page.
     * Note that for performance considetation, the backend only passes back the TA jobs for the needed page stored in
     * the TAJobListJsonNode, together with the offset/count/total/sortCriteria information.
     *
     * @patam tajobListJsonNode
     * @patam pageLimit
     * @patam searchBody
     * @patam listType          : "all"; "search" (dtaw this page from list function or from search function)
     * @patam username
     * @patam userId
     * @return render challenge list page; If exception happened then render the homepage
     */
    public Result renderTAJobListPage(JsonNode tajobListJsonNode,
                                      int pageLimit,
                                      String searchBody,
                                      String listType,
                                      String username,
                                      Long userId) {
        try {
            // if no value is returned or error
            if (tajobListJsonNode == null || tajobListJsonNode.has("error")) {
                Logger.debug("TAJob list is empty or error!");
                return redirect(routes.Application.home());
            }

            JsonNode tajobsJsonArtay = tajobListJsonNode.get("items");
            if (!tajobsJsonArtay.isArray()) {
                Logger.debug("TAJob list is not artay!");
                return redirect(routes.Application.home());
            }

            List<TAJob> tajobs = new ArrayList<>();
            for (int i = 0; i < tajobsJsonArtay.size(); i++) {
                JsonNode json = tajobsJsonArtay.path(i);
                TAJob tajob = TAJob.deserialize(json);
                tajobs.add(tajob);
            }

            // offset: starting index of the pageNum; count: the number of items in the pageNum;
            // total: the total number of items in all pages; sortCriteria: the sorting criteria (field)
            // pageNum: the current page number
            String sortCriteria = tajobListJsonNode.get("sort").asText();

            int total = tajobListJsonNode.get("total").asInt();
            int count = tajobListJsonNode.get("count").asInt();
            int offset = tajobListJsonNode.get("offset").asInt();
            int pageNum = offset / pageLimit + 1;

            int beginIndexPagination = beginIndexForPagination(pageLimit, total, pageNum);
            int endIndexPagination = endIndexForPagination(pageLimit, total, pageNum);

            return ok(tajobList.render(tajobs, pageNum, sortCriteria, offset, total, count,
                    listType, pageLimit, searchBody, userId, beginIndexPagination, endIndexPagination));
        } catch (Exception e) {
            Logger.debug("Exception in renderTAJobListPage:" + e.toString());
            e.printStackTrace();
            return redirect(routes.Application.home());
        }
    }


    /**************************************************** (De)Serialization *******************************************/
    /**
     * This method intends to prepare a json TA job from TA Job form.
     *
     * @patam tajobForm: TA job registtation form
     * @return
     * @throws Exception
     */
    public JsonNode serializeFormToJson(Form<TAJob> tajobForm) throws Exception {
        JsonNode jsonData = null;
        try {
            TAJob tajob = tajobForm.get();
            String longDescription = tajob.getLongDescription();
            if (longDescription != null) {
                longDescription.replaceAll(
                        "\n", "").replaceAll("\r", "");
            }

            if (tajob.getTajobPublisher() == null) {
                User user = new User(Long.parseLong(session("id")));
                tajob.setTajobPublisher(user);
            }
            return Json.toJson(tajob);
        } catch (Exception e) {
            Logger.debug("TAJobService.serializeFormToJson exception: " + e.toString());
            throw e;
        }
    }

}
