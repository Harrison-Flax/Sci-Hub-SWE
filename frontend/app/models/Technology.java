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
@ToString

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Technology.class)

public class Technology {
    private long id;

    private String technologyTitle; // has to be unique
    private String goals;
    private String shortDescription;
    private String longDescription;
    private String keywords;
    private String pIName;
    private String teamMembers;
    private String fields;
    private String organizations;
    private User contactPerson;
    private String pdf;
    private String representativePaperURL;

    private double rating;
    private long ratingCount;
    private double recommendRating;
    private long recommendRatingCount;
    private String homepage;

    private String registeredTime;
    private String isActive;

    private User technologyPublisher;
    private List<Project> participatedProjects;


    /*********************************************** Constructors *****************************************************/
    public Technology() {
    }

    public Technology(long id) {
        this.id = id;
    }

    /*********************************************** Utility methods **************************************************/

    /**
     * Deserializes the json to a technology.
     *
     * @param json the json to convert from.
     * @return the dataset object.
     * TODO: How to make sure all fields are checked???
     */
    public static Technology deserialize(JsonNode json) throws Exception {
        if (json == null) {
            throw new NullPointerException("Technology node should not be null to be serialized.");
        }
        Technology oneTechnology = Json.fromJson(json, Technology.class);

        return oneTechnology;
    }

    /**
     * This utility method intends to return a list of technologies from JsonNode based on starting and ending index.
     *
     * @param technologiesJson
     * @param startIndex
     * @param endIndex
     * @return: a list of technologies
     */
    public static List<Technology> deserializeJsonToTechnologyList(JsonNode technologiesJson, int startIndex, int endIndex)
            throws Exception {
        List<Technology> technologiesList = new ArrayList<Technology>();
        for (int i = startIndex; i <= endIndex; i++) {
            JsonNode json = technologiesJson.path(i);
            Technology technology = Technology.deserialize(json);
            technologiesList.add(technology);
        }
        return technologiesList;
    }

    /*********************************************** Getters and Setters ***********************************************/
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTechnologyTitle() {
        return technologyTitle;
    }

    public void setTechnologyTitle(String technologyTitle) {
        this.technologyTitle = technologyTitle;
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

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getPIName() {
        return pIName;
    }

    public void setPIName(String pIName) {
        this.pIName = pIName;
    }

    public String getTeamMembers() {
        return teamMembers;
    }

    public void setTeamMembers(String teamMembers) {
        this.teamMembers = teamMembers;
    }

    public String getFields() {
        return fields;
    }

    public void setFields(String fields) {
        this.fields = fields;
    }

    public String getOrganizations() {
        return organizations;
    }

    public void setOrganizations(String organizations) {
        this.organizations = organizations;
    }

    public User getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(User contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getPdf() {
        return pdf;
    }

    public void setPdf(String pdf) {
        this.pdf = pdf;
    }

    public String getRepresentativePaperURL() {
        return representativePaperURL;
    }

    public void setRepresentativePaperURL(String representativePaperURL) {
        this.representativePaperURL = representativePaperURL;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public long getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(long ratingCount) {
        this.ratingCount = ratingCount;
    }

    public double getRecommendRating() {
        return recommendRating;
    }

    public void setRecommendRating(double recommendRating) {
        this.recommendRating = recommendRating;
    }

    public long getRecommendRatingCount() {
        return recommendRatingCount;
    }

    public void setRecommendRatingCount(long recommendRatingCount) {
        this.recommendRatingCount = recommendRatingCount;
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public String getRegisteredTime() {
        return registeredTime;
    }

    public void setRegisteredTime(String registeredTime) {
        this.registeredTime = registeredTime;
    }

    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }

    public List<Project> getParticipatedProjects() {
        return participatedProjects;
    }

    public void setParticipatedProjects(List<Project> participatedProjects) {
        this.participatedProjects = participatedProjects;
    }

    public User getTechnologyPublisher() {
        return technologyPublisher;
    }

    public void setTechnologyPublisher(User technologyPublisher) {
        this.technologyPublisher = technologyPublisher;
    }
}
/*********************************************** Getters & Setters ************************************************/
