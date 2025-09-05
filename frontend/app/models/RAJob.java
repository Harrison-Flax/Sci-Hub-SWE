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
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = RAJob.class)

public class RAJob {

    private long id;
    private String isActive;
    private String pdf;
    private String title = "";
    private String goals = "";

    private int minSalary;
    private int maxSalary;
    private int raTypes;


    private String status;
    private String updateTime = "";
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
    private User rajobPublisher;

    private int numberOfApplicants;

    private long rajobApplicationId;

    private List<Long> rajobApplicationIdList;



    /*********************************************** Constructors *****************************************************/
    public RAJob() {
    }

    public RAJob(long id) {
        this.id = id;
    }

    /*********************************************** Utility methods **************************************************/


    /**
     * Deserializes the json to an RA job.
     *
     * @param json the json to convert from.
     * @return the dataset object.
     * TODO: How to make sure all fields are checked???
     */
    public static RAJob deserialize(JsonNode json) throws Exception {
        if (json == null) {
            throw new NullPointerException("RAJob node should not be null to be serialized.");
        }
        RAJob oneRAJob = Json.fromJson(json, RAJob.class);

        return oneRAJob;
    }

    /**
     * This utility method intends to return a list of RA jobs from JsonNode based on starting and ending index.
     *
     * @param rajobsJson
     * @param startIndex
     * @param endIndex
     * @return: a list of RA jobs
     */
    public static List<RAJob> deserializeJsonToRAJobList(JsonNode rajobsJson, int startIndex, int endIndex)
            throws Exception {
        List<RAJob> rajobsList = new ArrayList<RAJob>();
        for (int i = startIndex; i <= endIndex; i++) {
            JsonNode json = rajobsJson.path(i);
            RAJob rajob = RAJob.deserialize(json);
            rajobsList.add(rajob);
        }
        return rajobsList;
    }

    /*********************************************** Getters and Setters ***********************************************/
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
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

    public int getRaTypes() {
        return raTypes;
    }

    public void setRaTypes(int raTypes) {
        this.raTypes = raTypes;
    }

    public String getStatus() {
        return status != null ? status : "";
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPdf() {
        return pdf;
    }

    public void setPdf(String pdf) {
        this.pdf = pdf;
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

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getUpdateTime() {
        return updateTime;
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

    public User getRajobPublisher() {
        return rajobPublisher;
    }

    public void setRajobPublisher(User rajobPublisher) {
        this.rajobPublisher = rajobPublisher;
    }

    public int getNumberOfApplicants() {
        return numberOfApplicants;
    }

    public void setNumberOfApplicants(int numberOfApplicants) {
        this.numberOfApplicants = numberOfApplicants;
    }

    public long getRajobApplicationId() {
        return rajobApplicationId;
    }
    public List<Long>  getRajobApplicationIdList() {
        return rajobApplicationIdList;
    }

    /*********************************************** Getters & Setters *************************************************/
}
