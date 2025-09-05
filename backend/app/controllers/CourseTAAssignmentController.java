package controllers;

/**
 * @author LUO, QIUYU
 * @version 1.0
 */


import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import models.*;
import models.rest.RESTResponse;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import services.CourseTAAssignmentService;
import utils.Common;

import javax.inject.Inject;
import java.util.*;

import static play.mvc.Controller.request;
import static play.mvc.Results.*;

public class CourseTAAssignmentController extends Controller {

    public static final String ASSG_DEFAULT_SORT_CRITERIA = "id";

    private final CourseTAAssignmentService assignmentService;

    @Inject
    Config config;

    @Inject
    public CourseTAAssignmentController(CourseTAAssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }


    /************************************************* Add Assignments *******************************************************/
    /**
     * This method intends to add an TA Assignment into database.
     *
     * @return created status with TA Assignment id created
     */
    public Result addTAAssignment() {
        try {
            JsonNode json = request().body().asJson();
            if (json == null) {
                Logger.debug("TA Assignment information not saved, expecting Json data");
                return badRequest("TA Assignment information not saved, expecting Json data");
            }

            CourseTAAssignment taAssignment = Json.fromJson(json, CourseTAAssignment.class);

            taAssignment.save();
            System.out.println("Finish Add a new CourseTAAssignment in Backend. " + taAssignment.toString());
            return ok(Json.toJson(taAssignment.getId()).toString());
        } catch (Exception e) {
            Logger.debug("CourseTAAssignment cannot be added: " + e.toString());
            return badRequest("CourseTAAssignment not saved: ");
        }
    }



    /************************************************* CourseTAAssignment List ******************************************************/
    /**
     * Gets a list of all the TA jobs based on optional offset and limit and sort
     *
     * @patam pageLimit    shows the number of rows we want to receive
     * @patam pageNum      shows the page number
     * @patam sortCriteria shows based on what column we want to sort the data
     * @return a list of TA jobs
     * // TODO: Clean Common utitlity class for getSortCriteria(), not always register_time_stamp
     */

    public Result courseTAAssignmentList(Long Id, Integer pageLimit, Integer pageNum, Optional<String> sortCriteria) {
        String sortOrder = Common.getSortCriteria(sortCriteria, ASSG_DEFAULT_SORT_CRITERIA);
        int offset = pageLimit * (pageNum - 1);
        try {
            List<CourseTAAssignment> courseTAAssignments;
            if (sortOrder.equals("id") || sortOrder.equals("access_times")) {
                courseTAAssignments = CourseTAAssignment.find.query().order().desc(sortOrder).findList();
            } else {
                courseTAAssignments = CourseTAAssignment.find.query().orderBy(sortOrder).findList();
            }

            RESTResponse response = assignmentService.paginateResults(courseTAAssignments, Optional.of(offset), Optional.of(pageLimit), sortOrder);
            return ok(response.response());
        } catch (Exception e) {
            Logger.debug("CourseTAAssignmentController.courseTAAssignmentList() exception: " + e.toString());
            return internalServerError("CourseTAAssignmentController.courseTAAssignmentList() exception: " + e.toString());
        }
    }

    /************************************************* End of CourseTAAssignment List ***********************************************/



    /*****************  GET CourseTAAssignment BY ID    ****************/

    public Result getCourseTAAssignmentById(Long assignmentId) {
        if (assignmentId == null) {
            return Common.badRequestWrapper("CourseTAAssignment is null or empty.");
        }

        if (assignmentId == 0) return ok(Json.toJson(null));

        try {
            CourseTAAssignment taAssignment = CourseTAAssignment.find.query().where().eq("id", assignmentId).findOne();
            return ok(Json.toJson(taAssignment));
        } catch (Exception e) {
            Logger.debug("CourseTAAssignmentController.getCourseTAAssignmentById() exception : " + e.toString());
            return internalServerError("Internal Server Error CourseTAAssignmentController.getCourseTAAssignmentById() exception: " +
                    e.toString());
        }
    }

    /***************** END OF GET CourseTAAssignment BY ID    ****************/

    public Result addAssignment() {
        try {
            JsonNode json = request().body().asJson();
            if (json == null) {
                Logger.debug("TA assignment information not saved, expecting Json data");
                return badRequest("TA assignment information not saved, expecting Json data");
            }

            Long courseId = json.has("courseId") ? json.get("courseId").asLong() : null;
            Logger.debug("courseId: " + courseId);
            Long taCandidateId = json.has("taCandidateId") ? json.get("taCandidateId").asLong() : null;
            Logger.debug("taCandidateId: " + taCandidateId);

            Course course = Course.find.byId(courseId);
            Logger.debug("course: " + course);
            TACandidate taCandidate = TACandidate.find.byId(taCandidateId);
            Logger.debug("taCandidate: " + taCandidate);

            if (course == null || taCandidate == null) {
                return badRequest("Invalid course or TA candidate ID");
            }

            CourseTAAssignment assignment = new CourseTAAssignment();
            assignment.setCourse(course);
            assignment.setTaCandidate(taCandidate);
            if(json.has("approvedHours")) {
                int approvedHours = json.get("approvedHours").asInt();
                assignment.setApprovedHours(approvedHours);
            }


            assignment.save();
            Logger.debug("Finish Add a new TA assignment in Backend. " + assignment.toString());
            return ok(Json.toJson(assignment.getId()).toString());
        } catch (Exception e) {
            Logger.debug("TA assignment cannot be added: " + e.toString());
            return badRequest("TA assignment not saved: ");
        }
    }

}
