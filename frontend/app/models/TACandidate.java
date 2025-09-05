package models;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;




import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author LUO, QIUYU
 * @version 1.0
 */
public class TACandidate {

    private long id;
    private boolean isActive;
    private int isResumeSent;


    private String smuId = "";

    private String semester;

    private String year;


    private String status = "";


    private int hours;

    private String courses = "";

    private User applicant;


    private String preference = "";

    private String unwanted = "";

    // private String courseHired = ""; // Deprecated. has been replaced by CourseTAAssignment

    private List<CourseTAAssignment> assignments;

    private String comment = "";



    /*********************************************** Constructors *****************************************************/
    public TACandidate() {
    }

    public TACandidate(long id) {
        this.id = id;
    }

    /*********************************************** Utility methods **************************************************/

    /**
     * Deserializes the json to an TA job.
     *
     * @patam json the json to convert from.
     * @return the dataset object.
     * TODO: How to make sure all fields are checked???
     */
    public static TACandidate deserialize(JsonNode json) throws Exception {
        if (json == null) {
            throw new NullPointerException("TAJob node should not be null to be serialized.");
        }
        TACandidate oneTACandidate = Json.fromJson(json, TACandidate.class);

        return oneTACandidate;
    }

    /**
     * This utility method intends to return a list of TA candidates from JsonNode based on starting and ending index.
     *
     * @patam tacandidatesJson
     * @patam startIndex
     * @patam endIndex
     * @return: a list of TA jobs
     */
    public static List<TACandidate> deserializeJsonToTACandidateList(JsonNode tacandidatesJson, int startIndex, int endIndex)
            throws Exception {
        List<TACandidate> tacandidatesList = new ArrayList<TACandidate>();
        for (int i = startIndex; i <= endIndex; i++) {
            JsonNode json = tacandidatesJson.path(i);
            TACandidate tajob = TACandidate.deserialize(json);
            tacandidatesList.add(tajob);
        }
        return tacandidatesList;
    }

    /************************************* GETTER AND SETTER *******************************/
    public User getApplicant() {
        return applicant;
    }

    public void setApplicant(User applicant) {
        this.applicant = applicant;
    }

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

    public int getIsResumeSent() {
        return isResumeSent;
    }

    public void setIsResumeSent(int isResumeSent) {
        this.isResumeSent = isResumeSent;
    }

    public String getSmuId() {
        return smuId;
    }

    public void setSmuId(String smuId) {
        this.smuId = smuId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public String getCourses() {
        return courses;
    }

    public void setCourses(String courses) {
        this.courses = courses;
    }

    public String getPreference() {
        return preference;
    }

    public void setPreference(String preference) {
        this.preference = preference;
    }

    public String getUnwanted() {
        return unwanted;
    }

    public void setUnwanted(String unwanted) {
        this.unwanted = unwanted;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<CourseTAAssignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<CourseTAAssignment> assignments) {
        this.assignments = assignments;
    }


    public int computeTotalApprovedHours() {
        int approvedHours = 0;
        List<CourseTAAssignment> assignments = this.getAssignments();
        for (CourseTAAssignment assignment : assignments) {
            if (assignment.getCourse().isActive() && assignment.getSemester().equals("spring") && assignment.getYear().equals("2024")) {
                approvedHours += assignment.getApprovedHours();
            }
        }
        return approvedHours;
    }
}
