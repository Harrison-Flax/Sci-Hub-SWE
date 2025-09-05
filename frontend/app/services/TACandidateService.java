package services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;

import controllers.routes;
import models.*;

import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import utils.Constants;
import utils.RESTfulCalls;
import views.html.tacandidateList;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import static play.mvc.Controller.session;
import static play.mvc.Results.ok;
import static play.mvc.Results.redirect;
import static utils.Common.beginIndexForPagination;
import static utils.Common.endIndexForPagination;


/**
 * @author LUO, QIUYU
 * @version 1.0
 */
public class TACandidateService {
    @Inject
    Config config;


    private Form<TACandidate> TACandidateForm;

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
    public Result renderTACandidateListPage(JsonNode tacandidateListJsonNode,
                                      int pageLimit,
                                      String searchBody,
                                      String listType,
                                      String username,
                                      Long userId) {
        try {
            // if no value is returned or error
            if (tacandidateListJsonNode == null || tacandidateListJsonNode.has("error")) {
                Logger.debug("TAPool list is empty or error!");
                return redirect(routes.Application.home());
            }

            JsonNode tacandidateJsonArtay = tacandidateListJsonNode.get("items");
            if (!tacandidateJsonArtay.isArray()) {
                Logger.debug("TAPool list is not artay!");
                return redirect(routes.Application.home());
            }

            List<TACandidate> taCandidates = new ArrayList<>();
            for (int i = 0; i < tacandidateJsonArtay.size(); i++) {
                JsonNode json = tacandidateJsonArtay.path(i);
                TACandidate taCandidate = TACandidate.deserialize(json);
                taCandidates.add(taCandidate);
            }

            // offset: starting index of the pageNum; count: the number of items in the pageNum;
            // total: the total number of items in all pages; sortCriteria: the sorting criteria (field)
            // pageNum: the current page number
            String sortCriteria = tacandidateListJsonNode.get("sort").asText();

            int total = tacandidateListJsonNode.get("total").asInt();
            int count = tacandidateListJsonNode.get("count").asInt();
            int offset = tacandidateListJsonNode.get("offset").asInt();
            int pageNum = offset / pageLimit + 1;

            int beginIndexPagination = beginIndexForPagination(pageLimit, total, pageNum);
            int endIndexPagination = endIndexForPagination(pageLimit, total, pageNum);

            return ok(tacandidateList.render(taCandidates, pageNum, sortCriteria, offset, total, count,
                    listType, pageLimit, searchBody, userId, beginIndexPagination, endIndexPagination));
        } catch (Exception e) {
            Logger.debug("Exception in renderTAPoolListPage:" + e.toString());
            e.printStackTrace();
            return redirect(routes.Application.home());
        }
    }

     /*******************  (de)serialization  ***************************/

    public JsonNode serializeFormToJson(Form<TACandidate> taCandidateForm) throws Exception {
        JsonNode jsonData = null;
        Logger.debug("serializeFormToJson print input taCandidateForm" + taCandidateForm);
        try {
            TACandidate taCandidate = taCandidateForm.get();
            Logger.debug("serializeFormToJson print taCandidate" + taCandidate);


            if (taCandidate.getApplicant() == null) {
                User user = new User(Long.parseLong(session("id")));
                taCandidate.setApplicant(user);
            }
            return Json.toJson(taCandidate);
        } catch (Exception e) {
            Logger.debug("TACandidateService.serializeFormToJson exception: " + e.toString());
            throw e;
        }
    }

    /**
     * This method intends to get TAJob by id by calling backend APIs.
     *
     * @patam tajobId
     * @return TAJob
     */
    public TACandidate getTACandidateById(Long tacandidateId) {
        TACandidate tacandidate = null;
        try {
            JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.GET_TACANDIDATE_BY_ID + tacandidateId));
            if (response.has("error")) {
                Logger.debug("TAJobService.getTAJobById() did not get TA job from backend with error.");
                return null;
            }

            tacandidate = TACandidate.deserialize(response);
        } catch (Exception e) {
            Logger.debug("TAJobService.getTAJobById() exception: " + e.toString());
            return null;
        }
        return tacandidate;
    }

    public List<SimpleCourseTAAssignment> getAssignmentsByUser(Long userId) {
        List<SimpleCourseTAAssignment> assignments = null;
        ObjectMapper mapper = new ObjectMapper();  // ObjectMapper instance for JSON deserialization

        try {
            JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.GET_ASSIGNMENTS_BY_USER_ID + userId));
            if (response.has("error")) {
                Logger.debug("TACandidateService.getAssignmentsByUser() did not get assignments from backend with error.");
                return null;
            }

            // Assuming the actual data is in the root of the response or within a specific node
            if (response.isObject()) {
                assignments = mapper.readValue(response.traverse(), new TypeReference<List<SimpleCourseTAAssignment>>() {});
            } else {
                Logger.debug("Unexpected JSON structure.");
            }
        } catch (Exception e) {
            Logger.debug("TACandidateService.getAssignmentsByUser() exception: " + e.toString());
            e.printStackTrace();  // Print stack trace for better error diagnosis
            return null;
        }
        return assignments;
    }


}