package models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import play.libs.Json;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = TAJob.class)

public class TAJob {

    private long id;
    private String isActive;
    private String pdf;
    private String title = "";
    private String goals = "";



    private String status;
    private int minSalary;
    private int maxSalary;
    private int taTypes;
    private int workTime;
    

    private String fields = "";
    private String shortDescription = "";
    private String longDescription = "";
    private String publishDate = "";
    private String publishYear = "";
    private String publishMonth = "";
    private String imageURL = "";
    private String url = "";
    private String organization = "";
    private String location = "";
    private String requiredExpertise = "";
    private String preferredExpertise = "";
    private String numberOfPositions = "";
    private String expectedStartDate = "";
    private String taCoursesSelectionHidden = "";
    private User tajobPublisher;



    private int numberOfApplicants;
    private int taJobSemesterTypes;
    private int taJobCourseSelections;


    /*********************************************** Constructors *****************************************************/
    public TAJob() {
    }

    public TAJob(long id) {
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
    public static TAJob deserialize(JsonNode json) throws Exception {
        if (json == null) {
            throw new NullPointerException("TAJob node should not be null to be serialized.");
        }
        TAJob oneTAJob = Json.fromJson(json, TAJob.class);

        return oneTAJob;
    }

    /**
     * This utility method intends to return a list of TA jobs from JsonNode based on starting and ending index.
     *
     * @patam tajobsJson
     * @patam startIndex
     * @patam endIndex
     * @return: a list of TA jobs
     */
    public static List<TAJob> deserializeJsonToTAJobList(JsonNode tajobsJson, int startIndex, int endIndex)
            throws Exception {
        List<TAJob> tajobsList = new ArrayList<TAJob>();
        for (int i = startIndex; i <= endIndex; i++) {
            JsonNode json = tajobsJson.path(i);
            TAJob tajob = TAJob.deserialize(json);
            tajobsList.add(tajob);
        }
        return tajobsList;
    }

    /*********************************************** Getters and Setters ***********************************************/
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getTaJobSemesterTypes() {
        return taJobSemesterTypes;
    }

    public void setTaJobSemesterTypes(int taJobSemesterTypes) {
        this.taJobSemesterTypes = taJobSemesterTypes;
    }
    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getMaxSalary() {
        return maxSalary;
    }

    public void setMaxSalary(int maxSalary) {
        this.maxSalary = maxSalary;
    }
    public int getMinSalary() {
        return minSalary;
    }

    public void setMinSalary(int minSalary) {
        this.minSalary = minSalary;
    }

    public int getTaTypes() {
        return taTypes;
    }

    public void setTaTypes(int taTypes) {
        this.taTypes = taTypes;
    }

    public int getWorkTime() {
        return workTime;
    }

    public void setWorkTime(int workTime) {
        this.workTime = workTime;
    }

    public String getPdf() {
        return pdf;
    }

    public void setPdf(String pdf) {
        this.pdf = pdf;
    }

    public String getTaCoursesSelectionHidden() {
        return taCoursesSelectionHidden;
    }

    public void setTaCoursesSelectionHidden(String taCoursesSelectionHidden) {
        this.taCoursesSelectionHidden = taCoursesSelectionHidden;
    }

    public int getTaJobCourseSelections() {
        return taJobCourseSelections;
    }

    public void setTaJobCourseSelections(int taJobCourseSelections) {
        this.taJobCourseSelections = taJobCourseSelections;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGoals() {
        return goals;
    }

    public void setGoals(String goals) {
        this.goals = goals;
    }

    public String getFields() {
        return fields;
    }

    public void setFields(String fields) {
        this.fields = fields;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    public String getPublishYear() {
        return publishYear;
    }

    public void setPublishYear(String publishYear) {
        this.publishYear = publishYear;
    }

    public String getPublishMonth() {
        return publishMonth;
    }

    public void setPublishMonth(String publishMonth) {
        this.publishMonth = publishMonth;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getRequiredExpertise() {
        return requiredExpertise;
    }

    public void setRequiredExpertise(String requiredExpertise) {
        this.requiredExpertise = requiredExpertise;
    }

    public String getPreferredExpertise() {
        return preferredExpertise;
    }

    public void setPreferredExpertise(String preferredExpertise) {
        this.preferredExpertise = preferredExpertise;
    }

    public String getNumberOfPositions() {
        return numberOfPositions;
    }

    public void setNumberOfPositions(String numberOfPositions) {
        this.numberOfPositions = numberOfPositions;
    }

    public String getExpectedStartDate() {
        return expectedStartDate;
    }

    public void setExpectedStartDate(String expectedStartDate) {
        this.expectedStartDate = expectedStartDate;
    }

    public User getTajobPublisher() {
        return tajobPublisher;
    }

    public void setTajobPublisher(User tajobPublisher) {
        this.tajobPublisher = tajobPublisher;
    }

    public int getNumberOfApplicants() {
        return numberOfApplicants;
    }

    public void setNumberOfApplicants(int numberOfApplicants) {
        this.numberOfApplicants = numberOfApplicants;
    }
    /*********************************************** Getters & Setters *************************************************/
}