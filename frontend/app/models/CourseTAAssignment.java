package models;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LUO, QIUYU
 * @version 1.0
 */
public class CourseTAAssignment {

    private long id;

    private Course course;

    private TACandidate taCandidate;

    private String semester;

    private String year;

    private List<TAWeeklyHours> usedHours = new ArrayList<>();


    private int approvedHours;

    private String f1Approved;


    /*********************************************** Constructors *****************************************************/
    public CourseTAAssignment() {
    }

    public CourseTAAssignment(long id) {
        this.id = id;
    }
/*********************************************** Utility methods **************************************************/
    public static CourseTAAssignment deserialize(JsonNode json) throws Exception {
        if (json == null) {
            throw new NullPointerException("TAJob node should not be null to be serialized.");
        }
        CourseTAAssignment oneAssignment = Json.fromJson(json, CourseTAAssignment.class);

        return oneAssignment;
    }


    public static List<CourseTAAssignment> deserializeJsonToCourseTAAssignmentList(JsonNode courseTAAssignmentJson, int startIndex, int endIndex)
            throws Exception {
        List<CourseTAAssignment> courseTAAssignmentList = new ArrayList<CourseTAAssignment>();
        for (int i = startIndex; i <= endIndex; i++) {
            JsonNode json = courseTAAssignmentJson.path(i);
            CourseTAAssignment taAssignment = CourseTAAssignment.deserialize(json);
            courseTAAssignmentList.add(taAssignment);
        }
        return courseTAAssignmentList;
    }



    /************************************* GETTER AND SETTER *******************************/

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public TACandidate getTaCandidate() {
        return taCandidate;
    }

    public void setTaCandidate(TACandidate taCandidate) {
        this.taCandidate = taCandidate;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public List<TAWeeklyHours> getUsedHours() {
        return usedHours;
    }

    public void setUsedHours(List<TAWeeklyHours> usedHours) {
        this.usedHours = usedHours;
    }

    public int getApprovedHours() {
        return approvedHours;
    }

    public void setApprovedHours(int approvedHours) {
        this.approvedHours = approvedHours;
    }

    public String getF1Approved() {
        return f1Approved;
    }

    public void setF1Approved(String f1Approved) {
        this.f1Approved = f1Approved;
    }


    public int computeUsedHoursByWeek(int week) {
        int usedHours = 0;
        List<TAWeeklyHours> weeklyHours = this.getUsedHours();
        for (TAWeeklyHours weeklyHour: weeklyHours) {
            if (weeklyHour.getWeek() == week) {
                usedHours += weeklyHour.getHours();
            }
        }
        return usedHours;
    }
}
