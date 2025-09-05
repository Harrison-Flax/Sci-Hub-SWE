package models;


import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;


public class SimpleCourseTAAssignment {
    private Long id;
    private Long courseId;
    private Long courseNum;


    /*********************************************** Constructors *****************************************************/
    public SimpleCourseTAAssignment() {
    }

    public SimpleCourseTAAssignment(long id) {
        this.id = id;
    }

    public SimpleCourseTAAssignment(Long id, Long courseId, Long courseNum) {
        this.id = id;
        this.courseId = courseId;
        this.courseNum = courseNum;
    }

    /*********************************************** Utility methods **************************************************/
    public static SimpleCourseTAAssignment deserialize(JsonNode json) throws Exception {
        if (json == null) {
            throw new NullPointerException("TAJob node should not be null to be serialized.");
        }
        SimpleCourseTAAssignment oneAssignment = Json.fromJson(json, SimpleCourseTAAssignment.class);

        return oneAssignment;
    }

    /*********************************************** Getters and Setters *****************************************************/
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Long getCourseNum() {
        return courseNum;
    }

    public void setCourseNum(Long courseNum) {
        this.courseNum = courseNum;
    }
}
