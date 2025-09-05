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
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = TAJobApplication.class)
@JsonIgnoreProperties({"project", "sentMail", "receivedMail", "comments",
        "candidacies", "followers", "friendRequestSender", "friends",
        "createdProjects"})

public class TAJobApplication {
    public static final String DEFAULT_CHALLENGE_IMAGE = "../../../../assets/images/challenge.jpg";
    private long id;

    // applicant basic info
    private String applyDate;
    private int smuID;
    private int taSemesterTypes;
    private int taStudentTypes;
    private int taStudentAdmissionTypes;
    private int taUSCitizen;
    private int taNativeLanguage;
    private int taEnglishProficiencyTest;
    private String taEnglishProficiencyTestName;
    private double taEnglishProficiencyTestScore;
    private String taEnglishProficiencyTestDate;

    // GRE info
    private double greV;
    private double greQ;
    private double greA;
    private String greDate;



    // ta score info
    private double undergraduateGPA;
    private String undergraduateSchool;
    private double graduateGPA;
    private String graduateSchool;
    private int classRankNoGPA;
    private double scorePercentageNoGPA;
    private double gradeNoGPA;
    private String otherInfoNoGPA;
    private int enrolledDegree;
    private int enrolledPhdDegree;

    // ta interest area research info
    private String areasResearchInterest1;
    private String areasResearchInterest2;
    private String areasResearchInterest3;
    private String areasResearchInterest4;

    //ra smu exp info
    private int raSMU;
    private String raSMUTime;
    private String raSMUAdvisorName;
    private String raSMUAdvisorEmail;

    // ta smu exp info
    private int taSMU;
    private String taSMUTime;
    private String taSMUAdvisorName;
    private String taSMUAdvisorEmail;

    // programming language master info
    private int programmingLanguageCpp;
    private int programmingLanguageJava;
    private int programmingLanguagePython;
    private int programmingLanguageR;
    private int programmingLanguageSQL;
    private int programmingLanguageJavascript;
    private int programmingLanguageVerilog;
    private int programmingLanguageAssembler;
    private String programmingLanguageAssemblerType;
    private String computerSystemsType;

    // ta course preference info
    private String taCoursesPreference;
    private String taCoursesPreferenceHidden;
    private String taCoursesNotPreference;
    private String taCoursesNotPreferenceHidden;

    // ta teaching exp info
    private String previousTeachingExp1Title;
    private String previousTeachingExp1Where;
    private String previousTeachingExp1Date;
    private String previousTeachingExp2Title;
    private String previousTeachingExp2Where;
    private String previousTeachingExp2Date;
    private String previousTeachingExp3Title;
    private String previousTeachingExp3Where;
    private String previousTeachingExp3Date;



    // previous info
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

    private TAJob appliedTAJob;
    private User applicant;
    // Roles:(multiple roles can be sepatated by semicolon)
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

    /*********************************************** Constructors *****************************************************/
    public TAJobApplication() {
    }

    public TAJobApplication(long id) {
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
     * @patam node the node to convert from.
     * @return the dataset object.
     * TODO: How to make sure all fields are checked???
     */
    public static TAJobApplication deserialize(JsonNode node) throws Exception {
        try {
            if (node == null) {
                throw new NullPointerException("TAjob node should not be empty for TAJobApplication.deserialize()");
            }
            if (node.get("id") == null) {
                return null;
            }

            TAJobApplication tajob = Json.fromJson(node, TAJobApplication.class);
            return tajob;

        } catch (Exception e) {
            Logger.debug("TAJobApplication.deserialize() exception: " + e.toString());
            throw new Exception("TAJobApplication.deserialize() exception: " + e.toString());
        }
    }



    /**
     * This utility method intends to return a list of users from JsonNode based on starting and ending index.
     *
     * @patam usersJson
     * @patam startIndex
     * @patam endIndex
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

    public String getApplyDate() {
        return applyDate;
    }

    public void setApplyDate(String applyDate) {
        this.applyDate = applyDate;
    }

    public int getSmuID() {
        return smuID;
    }

    public void setSmuID(int smuID) {
        this.smuID = smuID;
    }

    public int getTaSemesterTypes() {
        return taSemesterTypes;
    }

    public void setTaSemesterTypes(int taSemesterTypes) {
        this.taSemesterTypes = taSemesterTypes;
    }

    public int getTaStudentTypes() {
        return taStudentTypes;
    }

    public void setTaStudentTypes(int taStudentTypes) {
        this.taStudentTypes = taStudentTypes;
    }

    public int getTaStudentAdmissionTypes() {
        return taStudentAdmissionTypes;
    }

    public void setTaStudentAdmissionTypes(int taStudentAdmissionTypes) {
        this.taStudentAdmissionTypes = taStudentAdmissionTypes;
    }

    public String getTaEnglishProficiencyTestDate() {
        return taEnglishProficiencyTestDate;
    }

    public void setTaEnglishProficiencyTestDate(String taEnglishProficiencyTestDate) {
        this.taEnglishProficiencyTestDate = taEnglishProficiencyTestDate;
    }

    public int getTaUSCitizen() {
        return taUSCitizen;
    }

    public void setTaUSCitizen(int taUSCitizen) {
        this.taUSCitizen = taUSCitizen;
    }

    public int getTaNativeLanguage() {
        return taNativeLanguage;
    }

    public void setTaNativeLanguage(int taNativeLanguage) {
        this.taNativeLanguage = taNativeLanguage;
    }

    public int getTaEnglishProficiencyTest() {
        return taEnglishProficiencyTest;
    }

    public void setTaEnglishProficiencyTest(int taEnglishProficiencyTest) {
        this.taEnglishProficiencyTest = taEnglishProficiencyTest;
    }

    public String getTaEnglishProficiencyTestName() {
        return taEnglishProficiencyTestName;
    }

    public void setTaEnglishProficiencyTestName(String taEnglishProficiencyTestName) {
        this.taEnglishProficiencyTestName = taEnglishProficiencyTestName;
    }

    public double getTaEnglishProficiencyTestScore() {
        return taEnglishProficiencyTestScore;
    }

    public void setTaEnglishProficiencyTestScore(double taEnglishProficiencyTestScore) {
        this.taEnglishProficiencyTestScore = taEnglishProficiencyTestScore;
    }

    public double getGreV() {
        return greV;
    }

    public void setGreV(double greV) {
        this.greV = greV;
    }

    public double getGreQ() {
        return greQ;
    }

    public void setGreQ(double greQ) {
        this.greQ = greQ;
    }

    public double getGreA() {
        return greA;
    }

    public void setGreA(double greA) {
        this.greA = greA;
    }

    public String getGreDate() {
        return greDate;
    }

    public void setGreDate(String greDate) {
        this.greDate = greDate;
    }

    public double getUndergraduateGPA() {
        return undergraduateGPA;
    }

    public void setUndergraduateGPA(double undergraduateGPA) {
        this.undergraduateGPA = undergraduateGPA;
    }

    public String getUndergraduateSchool() {
        return undergraduateSchool;
    }

    public void setUndergraduateSchool(String undergraduateSchool) {
        this.undergraduateSchool = undergraduateSchool;
    }

    public double getGraduateGPA() {
        return graduateGPA;
    }

    public void setGraduateGPA(double graduateGPA) {
        this.graduateGPA = graduateGPA;
    }

    public String getGraduateSchool() {
        return graduateSchool;
    }

    public void setGraduateSchool(String graduateSchool) {
        this.graduateSchool = graduateSchool;
    }

    public int getClassRankNoGPA() {
        return classRankNoGPA;
    }

    public void setClassRankNoGPA(int classRankNoGPA) {
        this.classRankNoGPA = classRankNoGPA;
    }

    public double getScorePercentageNoGPA() {
        return scorePercentageNoGPA;
    }

    public void setScorePercentageNoGPA(double scorePercentageNoGPA) {
        this.scorePercentageNoGPA = scorePercentageNoGPA;
    }

    public double getGradeNoGPA() {
        return gradeNoGPA;
    }

    public void setGradeNoGPA(double gradeNoGPA) {
        this.gradeNoGPA = gradeNoGPA;
    }

    public String getOtherInfoNoGPA() {
        return otherInfoNoGPA;
    }

    public void setOtherInfoNoGPA(String otherInfoNoGPA) {
        this.otherInfoNoGPA = otherInfoNoGPA;
    }

    public int getEnrolledDegree() {
        return enrolledDegree;
    }

    public void setEnrolledDegree(int enrolledDegree) {
        this.enrolledDegree = enrolledDegree;
    }

    public int getEnrolledPhdDegree() {
        return enrolledPhdDegree;
    }

    public void setEnrolledPhdDegree(int enrolledPhdDegree) {
        this.enrolledPhdDegree = enrolledPhdDegree;
    }

    public String getAreasResearchInterest1() {
        return areasResearchInterest1;
    }

    public void setAreasResearchInterest1(String areasResearchInterest1) {
        this.areasResearchInterest1 = areasResearchInterest1;
    }

    public String getAreasResearchInterest2() {
        return areasResearchInterest2;
    }

    public void setAreasResearchInterest2(String areasResearchInterest2) {
        this.areasResearchInterest2 = areasResearchInterest2;
    }

    public String getAreasResearchInterest3() {
        return areasResearchInterest3;
    }

    public void setAreasResearchInterest3(String areasResearchInterest3) {
        this.areasResearchInterest3 = areasResearchInterest3;
    }

    public String getAreasResearchInterest4() {
        return areasResearchInterest4;
    }

    public void setAreasResearchInterest4(String areasResearchInterest4) {
        this.areasResearchInterest4 = areasResearchInterest4;
    }

    public int getRaSMU() {
        return raSMU;
    }

    public void setRaSMU(int raSMU) {
        this.raSMU = raSMU;
    }

    public String getRaSMUTime() {
        return raSMUTime;
    }

    public void setRaSMUTime(String raSMUTime) {
        this.raSMUTime = raSMUTime;
    }

    public String getRaSMUAdvisorName() {
        return raSMUAdvisorName;
    }

    public void setRaSMUAdvisorName(String raSMUAdvisorName) {
        this.raSMUAdvisorName = raSMUAdvisorName;
    }

    public String getRaSMUAdvisorEmail() {
        return raSMUAdvisorEmail;
    }

    public void setRaSMUAdvisorEmail(String raSMUAdvisorEmail) {
        this.raSMUAdvisorEmail = raSMUAdvisorEmail;
    }

    public int getTaSMU() {
        return taSMU;
    }

    public void setTaSMU(int taSMU) {
        this.taSMU = taSMU;
    }

    public String getTaSMUTime() {
        return taSMUTime;
    }

    public void setTaSMUTime(String taSMUTime) {
        this.taSMUTime = taSMUTime;
    }

    public String getTaSMUAdvisorName() {
        return taSMUAdvisorName;
    }

    public void setTaSMUAdvisorName(String taSMUAdvisorName) {
        this.taSMUAdvisorName = taSMUAdvisorName;
    }

    public String getTaSMUAdvisorEmail() {
        return taSMUAdvisorEmail;
    }

    public void setTaSMUAdvisorEmail(String taSMUAdvisorEmail) {
        this.taSMUAdvisorEmail = taSMUAdvisorEmail;
    }

    public int getProgrammingLanguageCpp() {
        return programmingLanguageCpp;
    }

    public void setProgrammingLanguageCpp(int programmingLanguageCpp) {
        this.programmingLanguageCpp = programmingLanguageCpp;
    }

    public int getProgrammingLanguageJava() {
        return programmingLanguageJava;
    }

    public void setProgrammingLanguageJava(int programmingLanguageJava) {
        this.programmingLanguageJava = programmingLanguageJava;
    }

    public int getProgrammingLanguagePython() {
        return programmingLanguagePython;
    }

    public void setProgrammingLanguagePython(int programmingLanguagePython) {
        this.programmingLanguagePython = programmingLanguagePython;
    }

    public int getProgrammingLanguageR() {
        return programmingLanguageR;
    }

    public void setProgrammingLanguageR(int programmingLanguageR) {
        this.programmingLanguageR = programmingLanguageR;
    }

    public int getProgrammingLanguageSQL() {
        return programmingLanguageSQL;
    }

    public void setProgrammingLanguageSQL(int programmingLanguageSQL) {
        this.programmingLanguageSQL = programmingLanguageSQL;
    }

    public int getProgrammingLanguageJavascript() {
        return programmingLanguageJavascript;
    }

    public void setProgrammingLanguageJavascript(int programmingLanguageJavascript) {
        this.programmingLanguageJavascript = programmingLanguageJavascript;
    }

    public int getProgrammingLanguageVerilog() {
        return programmingLanguageVerilog;
    }

    public void setProgrammingLanguageVerilog(int programmingLanguageVerilog) {
        this.programmingLanguageVerilog = programmingLanguageVerilog;
    }

    public int getProgrammingLanguageAssembler() {
        return programmingLanguageAssembler;
    }

    public void setProgrammingLanguageAssembler(int programmingLanguageAssembler) {
        this.programmingLanguageAssembler = programmingLanguageAssembler;
    }

    public String getProgrammingLanguageAssemblerType() {
        return programmingLanguageAssemblerType;
    }

    public void setProgrammingLanguageAssemblerType(String programmingLanguageAssemblerType) {
        this.programmingLanguageAssemblerType = programmingLanguageAssemblerType;
    }

    public String getComputerSystemsType() {
        return computerSystemsType;
    }

    public void setComputerSystemsType(String computerSystemsType) {
        this.computerSystemsType = computerSystemsType;
    }

    public String getTaCoursesPreference(){
        return taCoursesPreference;
    }

    public void setTaCoursesPreference(String taCoursesPreference) {
        this.taCoursesPreference = taCoursesPreference;
    }

    public String getTaCoursesPreferenceHidden() {
        return taCoursesPreferenceHidden;
    }

    public void setTaCoursesPreferenceHidden(String taCoursesPreferenceHidden) {
        this.taCoursesPreferenceHidden = taCoursesPreferenceHidden;
    }

    public String getTaCoursesNotPreference(){
        return taCoursesNotPreference;
    }

    public void setTaCoursesNotPreference(String taCoursesNotPreference) {
        this.taCoursesNotPreference = taCoursesNotPreference;
    }

    public String getTaCoursesNotPreferenceHidden() {
        return taCoursesNotPreferenceHidden;
    }

    public void setTaCoursesNotPreferenceHidden(String taCoursesNotPreferenceHidden) {
        this.taCoursesNotPreferenceHidden = taCoursesNotPreferenceHidden;
    }

    public String getPreviousTeachingExp1Title() {
        return previousTeachingExp1Title;
    }

    public void setPreviousTeachingExp1Title(String previousTeachingExp1Title) {
        this.previousTeachingExp1Title = previousTeachingExp1Title;
    }

    public String getPreviousTeachingExp1Where() {
        return previousTeachingExp1Where;
    }

    public void setPreviousTeachingExp1Where(String previousTeachingExp1Where) {
        this.previousTeachingExp1Where = previousTeachingExp1Where;
    }

    public String getPreviousTeachingExp1Date() {
        return previousTeachingExp1Date;
    }

    public void setPreviousTeachingExp1Date(String previousTeachingExp1Date) {
        this.previousTeachingExp1Date = previousTeachingExp1Date;
    }

    public String getPreviousTeachingExp2Title() {
        return previousTeachingExp2Title;
    }

    public void setPreviousTeachingExp2Title(String previousTeachingExp2Title) {
        this.previousTeachingExp2Title = previousTeachingExp2Title;
    }

    public String getPreviousTeachingExp2Where() {
        return previousTeachingExp2Where;
    }

    public void setPreviousTeachingExp2Where(String previousTeachingExp2Where) {
        this.previousTeachingExp2Where = previousTeachingExp2Where;
    }

    public String getPreviousTeachingExp2Date() {
        return previousTeachingExp2Date;
    }

    public void setPreviousTeachingExp2Date(String previousTeachingExp2Date) {
        this.previousTeachingExp2Date = previousTeachingExp2Date;
    }

    public String getPreviousTeachingExp3Title() {
        return previousTeachingExp3Title;
    }

    public void setPreviousTeachingExp3Title(String previousTeachingExp3Title) {
        this.previousTeachingExp3Title = previousTeachingExp3Title;
    }

    public String getPreviousTeachingExp3Where() {
        return previousTeachingExp3Where;
    }

    public void setPreviousTeachingExp3Where(String previousTeachingExp3Where) {
        this.previousTeachingExp3Where = previousTeachingExp3Where;
    }

    public String getPreviousTeachingExp3Date() {
        return previousTeachingExp3Date;
    }

    public void setPreviousTeachingExp3Date(String previousTeachingExp3Date) {
        this.previousTeachingExp3Date = previousTeachingExp3Date;
    }

    public List<User> getTeamMembers() {
        return teamMembers;
    }

    public void setTeamMembers(List<User> teamMembers) {
        this.teamMembers = teamMembers;
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

    public TAJob getAppliedTAJob() {
        return appliedTAJob;
    }

    public void setAppliedTAJob(TAJob appliedTAJob) {
        this.appliedTAJob= appliedTAJob;
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
