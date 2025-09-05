package models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import play.libs.Json;
import utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@ToString
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = User.class)
@JsonIgnoreProperties({"project", "sentMail", "receivedMail", "comments",
        "candidacies", "followers", "friendRequestSender", "friends",
        "createdProjects"})
public class User {
    private static final String AWS_FILE_NAME_PREFIX = Constants.AWS_FILE_NAME_PREFIX;
    public static final String DEFAULT_USER_IMAGE = "https://ecopro-aws-bucket.s3.amazonaws.com/" + AWS_FILE_NAME_PREFIX + "/user/user.png";
    protected long id;

    protected String userName;
    protected String password;
    protected String firstName;
    protected String lastName;
    protected String middleInitial;
//    private String affiliation;
    protected String email;
    protected String mailingAddress;
    protected String phoneNumber;
    protected String organization;
    protected String hiddenOrganization;
    protected String orcid;
    protected String school;
    protected String department;
//    protected String researchFields;
//    protected String highestDegree;

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
    private Integer userType;
    private Integer hiddenUserType;

    private String createdTime;
    private String isActive;

    private Project projectZone;

    private List<Project> createdProjects;

    private ResearcherInfo researcherInfo;
    private StudentInfo studentInfo;

    private List<Project> participatedProjects;
    private List<Project> principalInvestigatedProjects;

    private List<Project> sponsoredProjects;


    protected Set<User> friendRequestSender;
    protected Set<User> friends;

    //TODO: What is this??? protected List<GroupModel> group;
    //TODO: TO remove
    private boolean unreadMention;

    /*********************************************** Constructors *****************************************************/
    public User() {
    }

    public User(long id) {
        this.id = id;
    }

    /*********************************************** Utility methods **************************************************/
//    public String getAvatar() {
//        if (avatar == null || avatar.equals("")) {
//            return DEFAULT_USER_IMAGE;
//        }
//        return avatar;
//    }

    public String getAvatar() {
        if (avatar == null || avatar.equals("")) {
             return DEFAULT_USER_IMAGE;
        }
        System.out.println(avatar);
        System.out.println("current avatar: " + avatar);
        String newAvatar = avatar;
        try {
            int startIndex = avatar.indexOf(AWS_FILE_NAME_PREFIX + "user/");
            if (startIndex != -1) {
                String partial = avatar.substring(startIndex + (AWS_FILE_NAME_PREFIX + "user/").length());
                int endIndex = partial.indexOf("?");
                if (endIndex != -1) {
                    partial = partial.substring(0, endIndex);
                }

                newAvatar = "https://ecopro-aws-bucket.s3.amazonaws.com/"+ AWS_FILE_NAME_PREFIX + "user/" + partial;
            }
        } catch (Exception e) {
            System.out.println("Error processing avatar: " + e.getMessage());
        }

//        System.out.println("Modified avatar: " + newAvatar);

        return newAvatar;
    }

    /**
     * Deserializes the json to a User.
     *
     * @param json the json to convert from.
     * @return the dataset object.
     * TODO: How to make sure all fields are checked???
     */
    public static User deserialize(JsonNode json) throws Exception {
        if (json == null) {
            throw new NullPointerException("User node should not be null to be serialized.");
        }
        User oneUser = Json.fromJson(json, User.class);
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
    public static List<User> deserializeJsonToUserList(
            JsonNode usersJson, int startIndex, int endIndex) throws Exception {
        List<User> usersList = new ArrayList<User>();
        for (int i = startIndex; i <= endIndex; i++) {
            JsonNode json = usersJson.path(i);
            User user = User.deserialize(json);
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

    public Integer getUserType(){
        return userType;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public ResearcherInfo getResearcherInfo() {
        return researcherInfo;
    }

    public void setResearcherInfo(ResearcherInfo researcherInfo) {
        this.researcherInfo = researcherInfo;
    }

    public StudentInfo getStudentInfo() {
        return studentInfo;
    }

    public void setStudentInfo(StudentInfo studentInfo) {
        this.studentInfo = studentInfo;
    }

    public String getResearchFields() {
        if (null != this.researcherInfo) return this.researcherInfo.getResearchFields();
        return "";
    }

    public void setResearchFields(String researchFields) {
        if (null != this.researcherInfo) this.researcherInfo.setResearchFields(researchFields);
    }

    public String getHighestDegree() {
        if (null != this.researcherInfo) return this.researcherInfo.getHighestDegree();
        return "";
    }

    public void setHighestDegree(String highestDegree) {
        if (null != this.researcherInfo) this.researcherInfo.setHighestDegree(highestDegree);
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

    public Set<User> getFriendRequestSender() {
        return friendRequestSender;
    }

    public void setFriendRequestSender(Set<User> friendRequestSender) {
        this.friendRequestSender = friendRequestSender;
    }

    public Set<User> getFriends() {
        return friends;
    }

    public void setFriends(Set<User> friends) {
        this.friends = friends;
    }

    public boolean isUnreadMention() {
        return unreadMention;
    }

    public void setUnreadMention(boolean unreadMention) {
        this.unreadMention = unreadMention;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getHiddenOrganization() {
        return hiddenOrganization;
    }

    public void setHiddenOrganization(String hiddenOrganization) {
        this.hiddenOrganization = hiddenOrganization;
    }

    public String getOrcid() {
        if (null != this.researcherInfo) return this.researcherInfo.getOrcid();
        return "";
    }

    public void setOrcid(String orcid) {
        if (null != this.researcherInfo) this.researcherInfo.setOrcid(orcid);
    }

    public String getSchool() {
        if (null != this.researcherInfo) return this.researcherInfo.getSchool();
        return "";
    }

    public void setSchool(String school) {
        if (null != this.researcherInfo) this.researcherInfo.setSchool(school);
    }

    public String getDepartment() {
        if (null != this.researcherInfo) return this.researcherInfo.getDepartment();
        return "";
    }

    public void setDepartment(String department) {
        if (null != this.researcherInfo) this.researcherInfo.setDepartment(department);
    }

    public boolean isResearcher() {
        return this.userType != null && (this.userType & Constants.USER_TYPE.RESEARCHER.value()) == Constants.USER_TYPE.RESEARCHER.value();
    }

    public boolean isSponsor() {
        return this.userType != null && (this.userType & Constants.USER_TYPE.SPONSOR.value()) == Constants.USER_TYPE.SPONSOR.value();
    }

    public boolean isStudent() {
        return this.userType != null && (this.userType & Constants.USER_TYPE.STUDENT.value()) == Constants.USER_TYPE.STUDENT.value();
    }

//    public String getResearchFields() {
//        return researchFields;
//    }
//
//    public void setResearchFields(String researchFields) {
//        this.researchFields = researchFields;
//    }
//
//    public String getHighestDegree() {
//        return highestDegree;
//    }
//
//    public void setHighestDegree(String highestDegree) {
//        this.highestDegree = highestDegree;
//    }

    public List<Project> getPrincipalInvestigatedProjects() {
        return principalInvestigatedProjects;
    }

    public void setPrincipalInvestigatedProjects(List<Project> principalInvestigatedProjects) {
        this.principalInvestigatedProjects = principalInvestigatedProjects;
    }

    public List<Project> getSponsoredProjects() {
        return sponsoredProjects;
    }

    public void setSponsoredProjects(List<Project> sponsoredProjects) {
        this.sponsoredProjects = sponsoredProjects;
    }
}

