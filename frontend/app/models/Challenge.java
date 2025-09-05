package models;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import play.Logger;
import play.libs.Json;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@ToString
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Challenge.class)
@JsonIgnoreProperties({"project", "sentMail", "receivedMail", "comments",
        "candidacies", "followers", "friendRequestSender", "friends",
        "createdProjects"})

public class Challenge {
    public static final String DEFAULT_CHALLENGE_IMAGE = "../../../../assets/images/challenge.jpg";
    private long id;

    private String challengeTitle; // has to be unique
    private String shortDescription;
    private String longDescription;
    private String requiredExpertise;
    private String preferredExpertise;
    private String preferredTime;
    private String time;
    private String tech;
    private String budget;
    private String location;
    private String organizations;

    private List<Long> challengeApplicationIdList;
    private int minBudget;
    private int maxBudget;

    private String status;

    private String challengePdf;
    private String updateTime = "";
    private String challengeImage;
    // Roles:(multiple roles can be separated by semicolon)
    // Admin: admin
    // Superuser: superuser
    // Normal: normal
    // Guest: guest
    // Tester: tester
    // Other: other

    private double rating;
    private long ratingCount;
    private double recommendRating;
    private long recommendRatingCount;
    private String homepage;
    private String avatar;

    // as a service provider (project participant)
    private boolean serviceProvider;
    private String expertises;
    private String categories;
    private String detail;
    private long service_execution_counts;

    // as a service user (project initiator)
    private boolean serviceUser;

    private String createdTime;
    private String isActive;

    private Project projectZone;

    private List<Project> createdProjects;


    private int numberOfApplicants;

    private List<Project> participatedProjects;
    private long challengeApplicationId;
    private User challengePublisher;
    private List<User> teamMembers;

    protected Set<Author> friendRequestSender;
    protected Set<Author> friends;

    //TODO: TO remove
    private boolean unreadMention;

    /*********************************************** Constructors *****************************************************/
    public Challenge() {
    }

    public Challenge(long id) {
        this.id = id;
    }

    /*********************************************** Utility methods **************************************************/
    public String getAvatar() {
        if (avatar == null || avatar.equals("")) {
            return DEFAULT_CHALLENGE_IMAGE;
        }
        return avatar;
    }


    /**
     * Deserializes the json to a Author.
     *
     * @param node the node to convert from.
     * @return the dataset object.
     * TODO: How to make sure all fields are checked???
     */
    public static Challenge deserialize(JsonNode node) throws Exception {
        try {
            if (node == null) {
                throw new NullPointerException("Challenge node should not be empty for Challenge.deserialize()");
            }
            if (node.get("id") == null) {
                return null;
            }

//            if (node.get("creator_id").get("id") == null){
//                throw new NullPointerException("Challenge node should have a creator.");
//            }
//            else{
//                Challenge challenge = Json.fromJson(node, Challenge.class);
//                User user = Json.fromJson(node.get("creator_id"), User.class);
//
//                if (user.getId() != 0){
//                    challenge.setCreator(user);
//                    System.out.println("deserialize challenge "+ challenge);
//                    return challenge;
//                }else
//                    return null;
//
//
//
//            }
            Challenge challenge = Json.fromJson(node, Challenge.class);
            return challenge;

        } catch (Exception e) {
            Logger.debug("Challenge.deserialize() exception: " + e.toString());
            throw new Exception("Challenge.deserialize() exception: " + e.toString());
        }
    }

    /**
     * This utility method intends to return a list of users from JsonNode based on starting and ending index.
     *
     * @param challengeJson
     * @param startIndex
     * @param endIndex
     * @return: a list of users
     */
    public static List<Challenge> deserializeJsonToChallengeList(JsonNode challengeJson, int startIndex, int endIndex)
            throws Exception {
        List<Challenge> challengeList = new ArrayList<Challenge>();
        for (int i = startIndex; i <= endIndex; i++) {
            JsonNode json = challengeJson.path(i);
            Challenge challenge = Challenge.deserialize(json);
            challengeList.add(challenge);
        }
        return challengeList;
    }


    /*********************************************** Getters & Setters ************************************************/
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public int getMinBudget() {
        return minBudget;
    }

    public void setMinBudget(int minBudget) {
        this.minBudget = minBudget;
    }

    public int getMaxBudget() {
        return maxBudget;
    }

    public void setMaxBudget(int maxBudget) {
        this.maxBudget = maxBudget;
    }


    public String getChallengePdf() {
        return challengePdf;
    }

    public void setChallengePdf(String challengePdf) {
        this.challengePdf = challengePdf;
    }

    public String getChallengeImage() {
        if ( challengeImage == null ||  challengeImage.equals("")) {
            return DEFAULT_CHALLENGE_IMAGE;
        }
        return challengeImage;
    }

    public void setChallengeImage(String challengeImage) {
        this.challengeImage = challengeImage;
    }

    public int getNumberOfApplicants() {
        return numberOfApplicants;
    }

    public void setNumberOfApplicants(int numberOfApplicants) {
        this.numberOfApplicants = numberOfApplicants;
    }

    public String getChallengeTitle() {
        return challengeTitle;
    }
    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
    public String getUpdateTime() {
        return updateTime;
    }

    public void setChallengeTitle(String challengeTitle) {
        this.challengeTitle = challengeTitle;
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

    public String getPreferredTime() {
        return preferredTime;
    }

    public void setPreferredTime(String preferredTime) {
        this.preferredTime = preferredTime;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTech() {
        return tech;
    }

    public void setTech(String tech) {
        this.tech = tech;
    }

    public String getBudget() {
        this.budget = minBudget + " - " + maxBudget;
        return budget;
    }

    public void setBudget(String budget) {
        this.budget = budget;
    }

    public String getOrganizations() {
        return organizations;
    }

    public void setOrganizations(String organizations) {
        this.organizations = organizations;
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



    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public boolean isServiceProvider() {
        return serviceProvider;
    }

    public void setServiceProvider(boolean serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    public String getExpertises() {
        return expertises;
    }

    public void setExpertises(String expertises) {
        this.expertises = expertises;
    }

    public String getCategories() {
        return categories;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public long getService_execution_counts() {
        return service_execution_counts;
    }

    public void setService_execution_counts(long service_execution_counts) {
        this.service_execution_counts = service_execution_counts;
    }

    public boolean isServiceUser() {
        return serviceUser;
    }

    public void setServiceUser(boolean serviceUser) {
        this.serviceUser = serviceUser;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }

    public Project getProjectZone() {
        return projectZone;
    }

    public void setProjectZone(Project projectZone) {
        this.projectZone = projectZone;
    }

    public List<Project> getCreatedProjects() {
        return createdProjects;
    }

    public void setCreatedProjects(List<Project> createdProjects) {
        this.createdProjects = createdProjects;
    }






    public List<Project> getParticipatedProjects() {
        return participatedProjects;
    }

    public void setParticipatedProjects(List<Project> participatedProjects) {
        this.participatedProjects = participatedProjects;
    }



    public Set<Author> getFriendRequestSender() {
        return friendRequestSender;
    }

    public void setFriendRequestSender(Set<Author> friendRequestSender) {
        this.friendRequestSender = friendRequestSender;
    }

    public User getChallengePublisher() {
        return challengePublisher;
    }

    public long getChallengeApplicationId() {
        return challengeApplicationId;
    }
    public List<Long>  getChallengeApplicationIdList() {
        return challengeApplicationIdList;
    }
    public void setChallengePublisher(User challengePublisher) {
        this.challengePublisher = challengePublisher;
    }

    public Set<Author> getFriends() {
        return friends;
    }

    public void setFriends(Set<Author> friends) {
        this.friends = friends;
    }

    public boolean isUnreadMention() {
        return unreadMention;
    }

    public void setUnreadMention(boolean unreadMention) {
        this.unreadMention = unreadMention;
    }
}
