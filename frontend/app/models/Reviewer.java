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
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Reviewer.class)
@JsonIgnoreProperties({"project", "sentMail", "receivedMail", "comments",
        "candidacies", "followers", "friendRequestSender", "friends",
        "createdProjects"})
public class Reviewer {
    public static final String DEFAULT_USER_IMAGE = "../../../../assets/images/user.png";
    private long id;

    private String userAccount; // has to be unique
    private String reviewerName;
    private String password;
    private String firstName;
    private String lastName;
    private String middleInitial;
    private String affiliation;
    private String title;
    private String email;
    private String mailingAddress;
    private String phoneNumber;
    private String researchFields;
    private String highestDegree;

    private String level;
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
    protected Set<Reviewer> friendRequestSender;
    protected Set<Reviewer> friends;

    //TODO: What is this??? protected List<GroupModel> group;
    //TODO: TO remove
    private boolean unreadMention;

    /*********************************************** Constructors *****************************************************/
    public Reviewer() {
    }

    public Reviewer(long id) {
        this.id = id;
    }

    /*********************************************** Utility methods **************************************************/
    public String getAvatar() {
        if (avatar == null || avatar.equals("")) {
            return DEFAULT_USER_IMAGE;
        }
        return avatar;
    }

    /**
     * Deserializes the json to a Reviewer.
     *
     * @param json the json to convert from.
     * @return the dataset object.
     * TODO: How to make sure all fields are checked???
     */
    public static Reviewer deserialize(JsonNode json) throws Exception {
        if (json == null) {
            throw new NullPointerException("Reviewer node should not be null to be serialized.");
        }
        Reviewer oneUser = Json.fromJson(json, Reviewer.class);
        if (oneUser.getAvatar() == null || oneUser.getAvatar().equals("null") || oneUser.getAvatar().trim().equals("")){
            oneUser.setAvatar(DEFAULT_USER_IMAGE);
        }
        oneUser.setProjectZone(Project.deserialize(json.findPath("project")));
        return oneUser;
    }

    /**
     * This utility method intends to return a list of users from JsonNode based on starting and ending index.
     *
     * @param usersJson
     * @param startIndex
     * @param endIndex
     * @return: a list of users
     */
    public static List<Reviewer> deserializeJsonToUserList(
            JsonNode usersJson, int startIndex, int endIndex) throws Exception {
        List<Reviewer> usersList = new ArrayList<Reviewer>();
        for (int i = startIndex; i <= endIndex; i++) {
            JsonNode json = usersJson.path(i);
            Reviewer user = Reviewer.deserialize(json);
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

    public String getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(String userAccount) {
        this.userAccount = userAccount;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleInitial() {
        return middleInitial;
    }

    public void setMiddleInitial(String middleInitial) {
        this.middleInitial = middleInitial;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMailingAddress() {
        return mailingAddress;
    }

    public void setMailingAddress(String mailingAddress) {
        this.mailingAddress = mailingAddress;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getResearchFields() {
        return researchFields;
    }

    public void setResearchFields(String researchFields) {
        this.researchFields = researchFields;
    }

    public String getHighestDegree() {
        return highestDegree;
    }

    public void setHighestDegree(String highestDegree) {
        this.highestDegree = highestDegree;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
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

    public Set<Reviewer> getFriendRequestSender() {
        return friendRequestSender;
    }

    public void setFriendRequestSender(Set<Reviewer> friendRequestSender) {
        this.friendRequestSender = friendRequestSender;
    }

    public Set<Reviewer> getFriends() {
        return friends;
    }

    public void setFriends(Set<Reviewer> friends) {
        this.friends = friends;
    }

    public boolean isUnreadMention() {
        return unreadMention;
    }

    public void setUnreadMention(boolean unreadMention) {
        this.unreadMention = unreadMention;
    }
}

