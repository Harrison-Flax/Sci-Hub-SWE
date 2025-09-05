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
import utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@ToString
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = ChallengeApplication.class)
@JsonIgnoreProperties({"project", "sentMail", "receivedMail", "comments",
        "candidacies", "followers", "friendRequestSender", "friends",
        "createdProjects"})

public class ChallengeApplication {
    public static final String DEFAULT_CHALLENGE_IMAGE = "../../../../assets/images/challenge.jpg";
    private long id;

    // challenge application info
    private String applyDescription; // has to be unique
    private String applyHeadline;
    private String applyCoverLetter;

    private long challengeApplicationId;
    private Challenge appliedChallenge;
    private User applicant;
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
    private String status;
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

    private List<Project> participatedProjects;


    private List<User> teamMembers;

    protected Set<Author> friendRequestSender;
    protected Set<Author> friends;

    //TODO: TO remove
    private boolean unreadMention;

    /*********************************************** Constructors *****************************************************/
    public ChallengeApplication() {
    }

    public ChallengeApplication(long id) {
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
    public static ChallengeApplication deserialize(JsonNode node) throws Exception {
        try {
            if (node == null) {
                throw new NullPointerException("Challenge node should not be empty for Challenge.deserialize()");
            }
            if (node.get("id") == null) {
                return null;
            }

            ChallengeApplication challenge = Json.fromJson(node, ChallengeApplication.class);
            return challenge;

        } catch (Exception e) {
            Logger.debug("Challenge.deserialize() exception: " + e.toString());
            throw new Exception("Challenge.deserialize() exception: " + e.toString());
        }
    }

    /**
     * This utility method intends to return a list of users from JsonNode based on starting and ending index.
     *
     * @param usersJson
     * @param startIndex
     * @param endIndex
     * @return: a list of users
     */
    public static List<Author> deserializeJsonToUserList(JsonNode usersJson, int startIndex, int endIndex)
            throws Exception {
        List<Author> usersList = new ArrayList<Author>();
        for (int i = startIndex; i <= endIndex; i++) {
            JsonNode json = usersJson.path(i);
            Author user = Author.deserialize(json);
            usersList.add(user);
        }
        return usersList;
    }


    /*********************************************** Getters & Setters ************************************************/
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getApplyDescription() {
        return applyDescription;
    }

    public void setApplyDescription(String applyDescription) {
        this.applyDescription = applyDescription;
    }

    public String getApplyHeadline() {
        return applyHeadline;
    }

    public void setApplyHeadline(String applyHeadline) {
        this.applyHeadline = applyHeadline;
    }

    public String getApplyCoverLetter() {
        return applyCoverLetter;
    }

    public void setApplyCoverLetter(String applyCoverLetter) {
        this.applyCoverLetter = applyCoverLetter;
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
    public long getChallengeApplicationId() {
        return challengeApplicationId;
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



    public String getStatus() {
        return status;
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

    public String getChallengeDescription() {
        return applyDescription;
    }

    public void setChallengeDescription(String applyDescription) {
        this.applyDescription = applyDescription;
    }

    public Challenge getAppliedChallenge() {
        return appliedChallenge;
    }

    public void setAppliedChallenge(Challenge appliedChallenge) {
        this.appliedChallenge = appliedChallenge;
    }

    public User getApplicant() {
        return applicant;
    }

    public void setApplicant(User applicant) {
        this.applicant = applicant;
    }

    public String getApplicantTypeInfo() {
        String typeInfo = "";
        if (this.applicant.isStudent()) typeInfo += Constants.USER_TYPE.STUDENT.name() + "(" + this.applicant.getStudentInfo().getStudentYear() + " year " + this.applicant.getStudentInfo().getStudentType() + ")";
        if (this.applicant.isResearcher()) typeInfo += Constants.USER_TYPE.RESEARCHER.name() + "(" + this.applicant.getResearchFields() + ")";
        return typeInfo;
    }

}
