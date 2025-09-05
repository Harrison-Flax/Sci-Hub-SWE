package models;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;


import java.util.ArrayList;
import java.util.List;

/**
 * @author LUO, QIUYU
 * @version 1.0
 */
public class Course {
    private long id;

    private boolean isActive;

    private String courseId;

    private String name;

    private String description;

    private String prerequisite;

    private String start_semester;

    private String start_year;

    private String end_semester;

    private String end_year;

    private List<CourseTAAssignment> assignments = new ArrayList<>();

    private boolean approval;


    /*********************************************** Constructors *****************************************************/
    public Course() {
    }

    public Course(long id) {
        this.id = id;
    }

    /************************************* GETTER AND SETTER *******************************/

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrerequisite() {
        return prerequisite;
    }

    public void setPrerequisite(String prerequisite) {
        this.prerequisite = prerequisite;
    }

    public String getStart_semester() {
        return start_semester;
    }

    public void setStart_semester(String start_semester) {
        this.start_semester = start_semester;
    }

    public String getStart_year() {
        return start_year;
    }

    public void setStart_year(String start_year) {
        this.start_year = start_year;
    }

    public String getEnd_semester() {
        return end_semester;
    }

    public void setEnd_semester(String end_semester) {
        this.end_semester = end_semester;
    }

    public String getEnd_year() {
        return end_year;
    }

    public void setEnd_year(String end_year) {
        this.end_year = end_year;
    }

    public boolean isApproval() {
        return approval;
    }

    public void setApproval(boolean approval) {
        this.approval = approval;
    }

    public List<CourseTAAssignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<CourseTAAssignment> assignments) {
        this.assignments = assignments;
    }


    /****************************   deserialize   **********************************/

    public static Course deserialize(JsonNode json) throws Exception {
        if (json == null) {
            throw new NullPointerException("Course node should not be null to be serialized.");
        }
        Course course = Json.fromJson(json, Course.class);

        return course;
    }

}
