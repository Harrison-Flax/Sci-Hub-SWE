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
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = RAJobApplication.class)
@JsonIgnoreProperties({"project", "sentMail", "receivedMail", "comments",
        "candidacies", "followers", "friendRequestSender", "friends",
        "createdProjects"})

public class RAJobApplication {
    public static final String DEFAULT_CHALLENGE_IMAGE = "../../../../assets/images/challenge.jpg";
    private long id;

    private String applyHeadline;
    private String applyCoverLetter;

    // 3 referees info
    private String referee1Title;
    private String referee1LastName;


    private String referee1FirstName;
    private String referee1Email;
    private String referee1Phone;

    private String referee2Title;
    private String referee2LastName;
    private String referee2FirstName;
    private String referee2Email;
    private String referee2Phone;

    private String referee3Title;

    private String referee3LastName;
    private String referee3FirstName;
    private String referee3Email;
    private String referee3Phone;

    private RAJob appliedRAJob;
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

    // rajob application status
    private String status;


    /*********************************************** Constructors *****************************************************/
    public RAJobApplication() {
    }

    public RAJobApplication(long id) {
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
    public static RAJobApplication deserialize(JsonNode node) throws Exception {
        try {
            if (node == null) {
                throw new NullPointerException("RAjob node should not be empty for RAJobApplication.deserialize()");
            }
            if (node.get("id") == null) {
                return null;
            }

            RAJobApplication rajob = Json.fromJson(node, RAJobApplication.class);
            return rajob;

        } catch (Exception e) {
            Logger.debug("RAJobApplication.deserialize() exception: " + e.toString());
            throw new Exception("RAJobApplication.deserialize() exception: " + e.toString());
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

    public String getReferee1Title() {
        return referee1Title;
    }

    public void setReferee1Title(String referee1Title) {
        this.referee1Title = referee1Title;
    }

    public String getReferee1LastName() {
        return referee1LastName;
    }

    public void setReferee1LastName(String referee1LastName) {
        this.referee1LastName = referee1LastName;
    }

    public String getReferee1FirstName() {
        return referee1FirstName;
    }

    public void setReferee1FirstName(String referee1FirstName) {
        this.referee1FirstName = referee1FirstName;
    }

    public String getReferee1Email() {
        return referee1Email;
    }

    public void setReferee1Email(String referee1Email) {
        this.referee1Email = referee1Email;
    }

    public String getReferee1Phone() {
        return referee1Phone;
    }

    public void setReferee1Phone(String referee1Phone) {
        this.referee1Phone = referee1Phone;
    }

    public String getReferee2Title() {
        return referee2Title;
    }

    public void setReferee2Title(String referee2Title) {
        this.referee2Title = referee2Title;
    }

    public String getReferee2LastName() {
        return referee2LastName;
    }

    public void setReferee2LastName(String referee2LastName) {
        this.referee2LastName = referee2LastName;
    }

    public String getReferee2FirstName() {
        return referee2FirstName;
    }

    public void setReferee2FirstName(String referee2FirstName) {
        this.referee2FirstName = referee2FirstName;
    }

    public String getReferee2Email() {
        return referee2Email;
    }

    public void setReferee2Email(String referee2Email) {
        this.referee2Email = referee2Email;
    }

    public String getReferee2Phone() {
        return referee2Phone;
    }

    public void setReferee2Phone(String referee2Phone) {
        this.referee2Phone = referee2Phone;
    }

    public String getReferee3Title() {
        return referee3Title;
    }

    public void setReferee3Title(String referee3Title) {
        this.referee3Title = referee3Title;
    }

    public String getReferee3LastName() {
        return referee3LastName;
    }

    public void setReferee3LastName(String referee3LastName) {
        this.referee3LastName = referee3LastName;
    }

    public String getReferee3FirstName() {
        return referee3FirstName;
    }

    public void setReferee3FirstName(String referee3FirstName) {
        this.referee3FirstName = referee3FirstName;
    }

    public String getReferee3Email() {
        return referee3Email;
    }

    public void setReferee3Email(String referee3Email) {
        this.referee3Email = referee3Email;
    }

    public String getReferee3Phone() {
        return referee3Phone;
    }

    public void setReferee3Phone(String referee3Phone) {
        this.referee3Phone = referee3Phone;
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

    public RAJob getAppliedRAJob() {
        return appliedRAJob;
    }

    public void setAppliedRAJob(RAJob appliedRAJob) {
        this.appliedRAJob= appliedRAJob;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
