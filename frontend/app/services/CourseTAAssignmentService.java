package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;

import controllers.routes;
import models.CourseTAAssignment;

import models.TACandidate;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;

import utils.Constants;
import utils.RESTfulCalls;
import views.html.taHiringStatus;

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
public class CourseTAAssignmentService {
    @Inject
    Config config;


    private Form<CourseTAAssignment> CourseTAAssignmentForm;

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
    public Result renderCourseTAAssignmentListPage(JsonNode assignmentListJsonNode,
                                            int pageLimit,
                                            String searchBody,
                                            String listType,
                                            String username,
                                            Long userId) {
        try {
            // if no value is returned or error
            if (assignmentListJsonNode == null || assignmentListJsonNode.has("error")) {
                Logger.debug("TAPool list is empty or error!");
                return redirect(routes.Application.home());
            }

            JsonNode assignmentJsonArtay = assignmentListJsonNode.get("items");
            if (!assignmentJsonArtay.isArray()) {
                Logger.debug("TAPool list is not artay!");
                return redirect(routes.Application.home());
            }

            List<CourseTAAssignment> courseTAAssignments = new ArrayList<>();
            for (int i = 0; i < assignmentJsonArtay.size(); i++) {
                JsonNode json = assignmentJsonArtay.path(i);
                CourseTAAssignment courseTAAssignment = CourseTAAssignment.deserialize(json);
                courseTAAssignments.add(courseTAAssignment);
            }

            // offset: starting index of the pageNum; count: the number of items in the pageNum;
            // total: the total number of items in all pages; sortCriteria: the sorting criteria (field)
            // pageNum: the current page number
            String sortCriteria = assignmentListJsonNode.get("sort").asText();

            int total = assignmentListJsonNode.get("total").asInt();
            int count = assignmentListJsonNode.get("count").asInt();
            int offset = assignmentListJsonNode.get("offset").asInt();
            int pageNum = offset / pageLimit + 1;

            int beginIndexPagination = beginIndexForPagination(pageLimit, total, pageNum);
            int endIndexPagination = endIndexForPagination(pageLimit, total, pageNum);

            return ok(taHiringStatus.render(courseTAAssignments, pageNum, sortCriteria, offset, total, count,
                    listType, pageLimit, searchBody, userId, beginIndexPagination, endIndexPagination));
        } catch (Exception e) {
            Logger.debug("Exception in renderTAPoolListPage:" + e.toString());
            e.printStackTrace();
            return redirect(routes.Application.home());
        }
    }

    /*******************  (de)serialization  ***************************/

//    public JsonNode serializeFormToJson(Form<CourseTAAssignment> courseTAAssignmentForm) throws Exception {
//        Logger.debug("serializeFormToJson print input CourseTAAssignment" + courseTAAssignmentForm);
//        if (courseTAAssignmentForm.hasErrors()) {
//            // Form Binding Error
//            JsonNode errors = courseTAAssignmentForm.errorsAsJson();
//            Logger.debug("Form Binding Error: " + errors);
//            throw new IllegalStateException("Form Binding Error: " + errors);
//        } else {
//            // Form bounded successfully, no verification error
//            CourseTAAssignment courseTAAssignment = courseTAAssignmentForm.get();
//            Logger.debug("serializeFormToJson print CourseTAAssignment" + courseTAAssignment);
//
//            courseTAAssignment.setSemester("spring");
//            courseTAAssignment.setYear("2024");
//
//            return Json.toJson(courseTAAssignment);
//        }
//    }

    public JsonNode serializeFormToJson(Form<CourseTAAssignment> assignmentForm) {
        if(assignmentForm.hasErrors()) {
            Logger.debug("Form data has errors: " + assignmentForm.errorsAsJson());
            throw new IllegalStateException("Error(s) binding form: " + assignmentForm.errorsAsJson());
        }

        String courseId = assignmentForm.field("courseId").getValue().orElse(null);
        Logger.debug("courseIds: " + courseId);
        String taCandidateId = assignmentForm.field("taCandidateId").getValue().orElse(null);
        Logger.debug("taCandidateId: " + taCandidateId);
        String approvedHours = assignmentForm.field("approvedHours").getValue().orElse(null);

        // 构造JsonNode
        ObjectNode jsonData = Json.newObject();
        jsonData.put("courseId", courseId);
        jsonData.put("taCandidateId", taCandidateId);
        jsonData.put("semester", "spring");
        jsonData.put("year", "2024");
        jsonData.put("approvedHours", approvedHours);
        jsonData.put("f1Approved", " ");

        return jsonData;
    }



    /**
     * This method intends to get CourseTA Assignment by id by calling backend APIs.
     *
     * @patam coursetaassignment
     * @return CourseTAAssignment
     */
    public CourseTAAssignment getTAAssignmentById(Long assignmentId) {
        CourseTAAssignment taAssignment = null;
        try {
            JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.GET_TAASSIGNMENT_BY_ID + assignmentId));
            if (response.has("error")) {
                Logger.debug("CourseTAAssignmentService.getTAAssignmentById() did not get an assignment from backend with error.");
                return null;
            }

            taAssignment = CourseTAAssignment.deserialize(response);
        } catch (Exception e) {
            Logger.debug("CourseTAAssignmentService.getTAAssignmentById() exception: " + e.toString());
            return null;
        }
        return taAssignment;
    }


}