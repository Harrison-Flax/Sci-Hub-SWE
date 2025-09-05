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
import java.util.Set;

@Getter
@Setter
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Job.class)
@JsonIgnoreProperties({"notebooks"})

public class Job {

    private long id;
    private String isActive;
    private String pdf;
    private String title = "";
    private String goals = "";

    private int minSalary;
    private int maxSalary;


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
    private User jobPublisher;

    private int numberOfApplicants;

    private long jobApplicationId;
    private List<Long> jobApplicationIdList;

    /*********************************************** Constructors *****************************************************/
    public Job() {
    }

    public Job(long id) {
        this.id = id;
    }

    /*********************************************** Utility methods **************************************************/

    /**
     * Deserializes the json to a job.
     *
     * @param json the json to convert from.
     * @return the dataset object.
     * TODO: How to make sure all fields are checked???
     */
    public static Job deserialize(JsonNode json) throws Exception {
        if (json == null) {
            throw new NullPointerException("Job node should not be null to be serialized.");
        }
        Job oneJob = Json.fromJson(json, Job.class);

        return oneJob;
    }

    /**
     * This utility method intends to return a list of jobs from JsonNode based on starting and ending index.
     *
     * @param jobsJson
     * @param startIndex
     * @param endIndex
     * @return: a list of technologies
     */
    public static List<Job> deserializeJsonToJobList(JsonNode jobsJson, int startIndex, int endIndex)
            throws Exception {
        List<Job> jobsList = new ArrayList<Job>();
        for (int i = startIndex; i <= endIndex; i++) {
            JsonNode json = jobsJson.path(i);
            Job job = Job.deserialize(json);
            jobsList.add(job);
        }
        return jobsList;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public int getMinSalary() {
        return minSalary;
    }

    public void setMinSalary(int minSalary) {
        this.minSalary = minSalary;
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

    public User getJobPublisher() {
        return jobPublisher;
    }

    public void setJobPublisher(User jobPublisher) {
        this.jobPublisher = jobPublisher;
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

    public int getNumberOfApplicants() {
        return numberOfApplicants;
    }

    public void setNumberOfApplicants(int numberOfApplicants) {
        this.numberOfApplicants = numberOfApplicants;
    }

    public long getjobApplicationId() {
        return jobApplicationId;
    }

    public List<Long>  getjobApplicationIdList() {
        return jobApplicationIdList;
    }
    /*********************************************** Getters & Setters ************************************************/
}