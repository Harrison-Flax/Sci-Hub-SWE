package controllers;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.ebean.Ebean;
import models.Course;
import models.CourseTAAssignment;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.time.LocalDate;
import java.util.List;

/**
 * @author LUO, QIUYU
 * @version 1.0
 */

public class CourseController extends Controller {

    public Result listCourses() {
        try {
            List<Course> courses = Course.find.query().findList(); // get all the courses
            ArrayNode courseArray = Json.newArray();
            for (Course course : courses) {
                ObjectNode courseJson = Json.newObject();
                courseJson.put("id", course.getId());
                courseJson.put("courseId", course.getCourseId());
                courseArray.add(courseJson);
            }
            return ok(courseArray); // return only the ids and courseIds as JSON
        } catch (Exception e) {
            return internalServerError("CourseController listCourses() an error occurred: " + e.toString());
        }
    }


    public Result getCourseDetails(Long courseId) {
        // Course course = Course.find.byId(courseId);
        // Only transmit the active courses
        Course course = Course.find.query().where().eq("isActive", true).eq("id", courseId).findOne();
        if (course == null) {
            return notFound("Course not found");
        }

        ObjectNode courseDetails = Json.newObject();
        ObjectNode weeks = Json.newObject();
        LocalDate startDate = LocalDate.parse("2024-01-01"); // The start date of each semester should be updated
        for (int i = 1; i <= 12; i++) { // Assuming there are 12 weeks each semester
            ObjectNode weekDetails = Json.newObject();
            weekDetails.put("startDate", startDate.plusWeeks(i - 1).toString());
            weekDetails.put("totalApprovedHours", course.computeTotalApprovedHours());
            weekDetails.put("totalHoursUsed", course.computeTotalUsedHoursByWeek(i));

            ArrayNode taDetails = Json.newArray();
            boolean allApproved = true; // Assume initially that all TAs are not approved for this week
            for (CourseTAAssignment assignment : course.getAssignments()) {
                ObjectNode taDetail = Json.newObject();
                taDetail.put("name", assignment.getTaCandidate().getApplicant().getUserName());
                taDetail.put("usedHours", assignment.computeUsedHoursByWeek(i));
                boolean isApproved = assignment.isApproved(i); // Check if the TA's hours are approved for this week
                taDetail.put("approved", isApproved); // Add approval status
                if (!isApproved) {
                    allApproved = false; // If any TA is not approved, mark allApproved as false
                }
                taDetails.add(taDetail);
            }
            weekDetails.set("taDetails", taDetails);
            weekDetails.put("approved", allApproved); // Add the overall approval status for the week
            weeks.set("Week " + i, weekDetails);
        }
        courseDetails.set("weeks", weeks);

        return ok(courseDetails);
    }

    public Result approveTA(Long courseId, int week) {
        Course course = Course.find.query().where().eq("id", courseId).findOne();
        if (course == null) {
            return notFound("Course not found");
        }

        Ebean.beginTransaction();
        try {
            for (CourseTAAssignment assignment : course.getAssignments()) {
                if (!assignment.isApproved(week)) {
                    assignment.approveWeek(week);
                    Ebean.update(assignment);
                }
            }
            Ebean.commitTransaction();
            return ok("All TA hours approved for week: " + week);
        } catch (Exception e) {
            Ebean.rollbackTransaction();
            return internalServerError("Failed to approve TA hours: " + e.getMessage());
        } finally {
            Ebean.endTransaction();
        }
    }


}

