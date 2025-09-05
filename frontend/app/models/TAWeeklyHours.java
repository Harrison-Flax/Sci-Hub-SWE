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
public class TAWeeklyHours {

    private long id;

    private int week;

    private int hours;

    private CourseTAAssignment courseTAAssignment;

    private boolean approval;

    public TAWeeklyHours() {
    }

    public TAWeeklyHours(long id) {
        this.id = id;
    }


    /*********************************************** Utility methods **************************************************/

    public static TAWeeklyHours deserialize(JsonNode json) throws Exception {
        if (json == null) {
            throw new NullPointerException("TAJob node should not be null to be serialized.");
        }
        TAWeeklyHours oneWeeklyHours = Json.fromJson(json, TAWeeklyHours.class);

        return oneWeeklyHours;
    }

    /************************************* GETTER AND SETTER *******************************/

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public boolean isApproval() {
        return approval;
    }

    public void setApproval(boolean approval) {
        this.approval = approval;
    }

    public CourseTAAssignment getCourseTAAssignment() {
        return courseTAAssignment;
    }

    public void setCourseTAAssignment(CourseTAAssignment courseTAAssignment) {
        this.courseTAAssignment = courseTAAssignment;
    }
}
