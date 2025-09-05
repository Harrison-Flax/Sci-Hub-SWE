package controllers;

/**
 * @author LUO, QIUYU
 * @version 1.0
 */


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import models.*;
import models.rest.RESTResponse;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import services.TACandidateService;
import utils.Common;

import javax.inject.Inject;
import java.util.*;

import static utils.Constants.ACTIVE;


public class TACandidateController extends Controller {

    public static final String TAPOOL_DEFAULT_SORT_CRITERIA = "id";

    private final TACandidateService taCandidateService;

    @Inject
    Config config;

    @Inject
    public TACandidateController(TACandidateService taCandidateService) {
        this.taCandidateService = taCandidateService;
    }


    /************************************************* Add TACandidate *******************************************************/
    /**
     * This method intends to add an TA candidate into database.
     *
     * @return created status with TA candidate id created
     */
    public Result addTACandidate() {
        try {
            JsonNode json = request().body().asJson();
            if (json == null) {
                Logger.debug("TA candidate information not saved, expecting Json data");
                return badRequest("TA candidate information not saved, expecting Json data");
            }

            TACandidate taCandidate = Json.fromJson(json, TACandidate.class);
            taCandidate.setIsActive(true);



            taCandidate.save();
            System.out.println("Finish Add a new TA candidate in Backend. " + taCandidate.toString());
            return ok(Json.toJson(taCandidate.getId()).toString());
        } catch (Exception e) {
            Logger.debug("TA candidate cannot be added: " + e.toString());
            return badRequest("TA candidate not saved: ");
        }
    }



    /************************************************* TACandidate List ******************************************************/
    /**
     * Gets a list of all the TA jobs based on optional offset and limit and sort
     *
     * @patam pageLimit    shows the number of rows we want to receive
     * @patam pageNum      shows the page number
     * @patam sortCriteria shows based on what column we want to sort the data
     * @return a list of TA jobs
     * // TODO: Clean Common utitlity class for getSortCriteria(), not always register_time_stamp
     */
    public Result tacandidateList(Long userId, Integer pageLimit, Integer pageNum, Optional<String> sortCriteria) {
        List<TACandidate> availableTACandidates = new ArrayList<>();

        Set<Long> tacandidateIds = new HashSet<>();
        List<TACandidate> tacandidates;
        String sortOrder = Common.getSortCriteria(sortCriteria, TAPOOL_DEFAULT_SORT_CRITERIA);

        int offset = pageLimit * (pageNum - 1);
        try {
            User user = User.find.byId(userId);


            availableTACandidates = TACandidate.find.query().findList();

            for (TACandidate taCandidate : availableTACandidates) {
                tacandidateIds.add(taCandidate.getId());
            }

            if (sortOrder.equals("id") || sortOrder.equals("access_times"))
                tacandidates = TACandidate.find.query().where().in("id", tacandidateIds).order().desc(sortOrder)
                        .findList();
            else
                tacandidates = TACandidate.find.query().where().in("id", tacandidateIds).orderBy(sortOrder)
                        .findList();
            RESTResponse response = taCandidateService.paginateResults(tacandidates, Optional.of(offset), Optional.
                    of(pageLimit), sortOrder);
            return ok(response.response());
        } catch (Exception e) {
            Logger.debug("TAJobController.jobList() exception: " + e.toString());
            return internalServerError("TAJobController.jobList() exception: " + e.toString());
        }
    }

    /************************************************* End of TACandidate List ***********************************************/



    /*****************  GET TA CANDIDATE BY ID    ****************/

    public Result getTACandidateById(Long tacandidateId) {
        if (tacandidateId == null) {
            return Common.badRequestWrapper("TA jobId is null or empty.");
        }

        if (tacandidateId == 0) return ok(Json.toJson(null));

        try {
            TACandidate taCandidate = TACandidate.find.query().where().eq("id", tacandidateId).findOne();
            return ok(Json.toJson(taCandidate));
        } catch (Exception e) {
            Logger.debug("TACandidateController.getTACandidateById() exception : " + e.toString());
            return internalServerError("Internal Server Error TACandidateController.getTACandidateById() exception: " +
                    e.toString());
        }
    }

    /***************** END OF GET TA CANDIDATE BY ID    ****************/


    /***************** GET COURSE LIST OF LOGGED-IN TA CANDIDATE    ****************/

    public Result getAssignmentsByUserId(Long userId) {
        if (userId == null) {
            return Common.badRequestWrapper("TA is null or empty.");
        }

        if (userId == 0) {
            return ok(Json.toJson(null)); // Return empty JSON if userId is 0
        }

        try {
            TACandidate taCandidate = TACandidate.find.query().where().eq("ta_applicant_id", userId).findOne();
            if (taCandidate == null) {
                return notFound("TACandidate not found for userId: " + userId);
            }

            if (taCandidate.getAssignments() == null) {
                return notFound("Assignments not found for userId: " + userId);
            }

            // Create a JSON array to hold the simplified assignment data
            ArrayNode assignmentsArray = Json.newArray();
            for (CourseTAAssignment assignment : taCandidate.getAssignments()) {
                ObjectNode assignmentJson = Json.newObject();
                assignmentJson.put("id", assignment.getId());
                assignmentJson.put("courseId", assignment.getCourse().getId());
                assignmentJson.put("courseNum", assignment.getCourse().getCourseId());
                assignmentsArray.add(assignmentJson);
            }

            return ok(assignmentsArray); // Return the JSON array of simplified assignments
        } catch (Exception e) {
            Logger.debug("TACandidateController.getAssignmentsByUserId() exception : " + e.toString());
            return internalServerError("Internal Server Error TACandidateController.getAssignmentsByUserId() exception: " +
                    e.toString());
        }
    }

    /***************** END OF GET COURSE LIST OF LOGGED-IN TA CANDIDATE    ****************/


}