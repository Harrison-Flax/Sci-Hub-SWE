package controllers;

import java.io.File;

import actions.OperationLoggingAction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.mysql.cj.log.Log;
import com.typesafe.config.Config;
import io.ebean.Ebean;
import io.ebean.Expr;
import io.ebean.SqlUpdate;
import io.ebean.ExpressionList;
import models.*;
import models.rest.RESTResponse;
import org.apache.commons.codec.net.URLCodec;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import services.OperationLoggingService;
import services.UserService;
import utils.Common;
import utils.Constants;
import utils.EmailUtils;
import utils.S3Utils;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.MessagingException;
import javax.persistence.PersistenceException;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.activation.MimetypesFileTypeMap;

import static utils.Constants.*;

public class UserController extends Controller {

    public static final String USER_DEFAULT_SORT_CRITERIA = "user_name";
    @Inject
    Config config;

    private final UserService userService;
    private final OperationLoggingService operationLoggingService;

    @Inject
    public UserController(UserService userService, OperationLoggingService operationLoggingService) {
        this.userService = userService;
        this.operationLoggingService = operationLoggingService;
    }


    static byte[] raw;

    static {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128, new SecureRandom("AES".getBytes()));
            raw = kgen.generateKey().getEncoded();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    static private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    /*********************************************** Add User *********************************************************/
    /**
     * Registers a new user provided the user data in the request body.
     *
     * @return the user id of the user created.
     */
    public Result addUser() {
        JsonNode json = request().body().asJson();
        if (json == null) {
            return Common.badRequestWrapper("User not created, expecting Json data");
        }
        Gson gson = new Gson();

        String userName = json.path("userName").asText();
        String password = MD5Hashing(json.path("password").asText());
        String firstName = json.path("firstName").asText();
        String lastName = json.path("lastName").asText();
        String middleInitial = json.path("middleInitial").asText();
        String organization = json.path("organization").asText();
        String email = json.path("email").asText();
        String mailingAddress = json.path("mailingAddress").asText();
        String phoneNumber = json.path("phoneNumber").asText();
        String researchFields = json.path("researchFields").asText();
        String highestDegree = json.path("highestDegree").asText();

        Integer userType = Integer.parseInt(json.path("hiddenUserType").asText());
        String orcid = json.path("orcid").asText();
        String school = json.path("school").asText();
        String department = json.path("department").asText();

        String studentIdNumber = json.path("studentIdNumber").asText();
        String studentType = json.path("studentType").asText();
        String studentYear = json.path("studentYear").asText();
        String studentMajor = json.path("studentMajor").asText();
        String studentEnrollDate = json.path("studentEnrollDate").asText();

        String hiddenOrganization = json.path("hiddenOrganization").asText();

        User user = new User(userName, password, firstName,
                lastName, middleInitial, organization, email, mailingAddress,
                phoneNumber, "normal");

        String homepage = json.path("homepage").asText();
        user.setHomepage(homepage);
        Logger.debug("Homepage received from request: " + homepage);

        user.setUserType(userType);
        user.setIsActive("True");
        user.setCreateTime(new Date().toString());

        try {
            if ((User.find.query().where().eq("is_active", "True").eq("email",
                    user.getEmail()).findList()).size() != 0) {
                return Common.badRequestWrapper("Email has been used");
            }
            user.save();
            user.updateOrganization(organization, hiddenOrganization);
            user.save();

            if (user.isResearcher()) {
                ResearcherInfo researcherInfo = new ResearcherInfo(user, researchFields,
                        highestDegree, orcid, school, department);
                researcherInfo.save();
                user.setResearcherInfo(researcherInfo);
                researcherInfo.setUser(user);
                user.setResearcherFlag(true);
                user.save();
            }
            if (user.isStudent()) {
                StudentInfo studentInfo = new StudentInfo(user, studentIdNumber, studentYear, studentType, studentMajor, studentEnrollDate);
                studentInfo.save();
                user.setStudentInfo(studentInfo);
                studentInfo.setUser(user);
                user.setStudentFlag(true);
                user.save();
            }

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode node = mapper.createObjectNode();
            node.put("id", user.getId());
            return ok(node);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.debug("UserController.addUser() exception " + e.toString());
            return notFound("User not added");
        }
    }
    /********************************************** End of Add User ***************************************************/

    /*********************************************** Update User ******************************************************/
    /**
     * This method receives updated information about user and saves the changes
     *
     * @return ok or bad request
     */
    public Result updateUser() {
        JsonNode json = request().body().asJson();
        if (json == null) {
            return Common.badRequestWrapper("Cannot check user, expecting Json data");
        }
        try {
            Long userId = json.path("id").asLong();
            User user = User.find.query().where().eq("id", userId).eq("is_active", ACTIVE).
                    findOne();

            // if user found, then make the update.
            if (user != null) {
                user.updateFromJson(json, user);
                if (json.has("homepage")) {
                    String homepage = json.path("homepage").asText();
                    user.setHomepage(homepage);
                    Logger.debug("Homepage received from request: " + homepage);
                }
                user.save();
                if (user.getUserType() == 1) {
                    String highestDegree = null;
                    if (json.has("highestDegree")) {
                        highestDegree = json.path("highestDegree").asText().trim();
                        if (highestDegree.isEmpty() || highestDegree.equalsIgnoreCase("null")) {
                            highestDegree = null;
                        }
                        Logger.debug("Parsed highestDegree: " + highestDegree);
                    }

                    String orcid = null;
                    if (json.has("orcid")) {
                        orcid = json.path("orcid").asText().trim();
                        if (orcid.isEmpty() || orcid.equalsIgnoreCase("null")) {
                            orcid = null;
                        }
                        Logger.debug("Parsed orcid: " + orcid);
                    }

                    String researchFields = null;
                    if (json.has("researchFields")) {
                        researchFields = json.path("researchFields").asText().trim();
                        if (researchFields.isEmpty() || researchFields.equalsIgnoreCase("null")) {
                            researchFields = null;
                        }
                        Logger.debug("Parsed researchFields: " + researchFields);
                    }

                    String school = null;
                    if (json.has("school")) {
                        school = json.path("school").asText().trim();
                        if (school.isEmpty() || school.equalsIgnoreCase("null")) {
                            school = null;
                        }
                        Logger.debug("Parsed school: " + school);
                    }

                    String department = null;
                    if (json.has("department")) {
                        department = json.path("department").asText().trim();
                        if (department.isEmpty() || department.equalsIgnoreCase("null")) {
                            department = null;
                        }
                        Logger.debug("Parsed department: " + department);
                    }

                    String sql = "UPDATE researcher_info SET " +
                            "highest_degree = :highestDegree, " +
                            "orcid = :orcid, " +
                            "research_fields = :researchFields, " +
                            "school = :school, " +
                            "department = :department " +
                            "WHERE user_id = :userId";

                    SqlUpdate update = Ebean.createSqlUpdate(sql);
                    update.setParameter("highestDegree", highestDegree);
                    update.setParameter("orcid", orcid);
                    update.setParameter("researchFields", researchFields);
                    update.setParameter("school", school);
                    update.setParameter("department", department);

                    update.setParameter("userId", userId);

                    Logger.debug("Executing native SQL update: " + sql);
                    Logger.debug("With parameters: highestDegree=" + highestDegree +
                            ", orcid=" + orcid + ", researchFields=" + researchFields +
                            ", school=" + school + ", department=" + department +
                            ", userId=" + userId);

                    int rows = update.execute();
                    Logger.debug("Native SQL update executed for researcher_info, rows updated: " + rows);
                }
                if (user.getUserType() == 4) {
                    String idNumber = null;
                    if (json.has("studentIdNumber")) {
                        idNumber = json.path("studentIdNumber").asText().trim();
                        if (idNumber.isEmpty() || idNumber.equalsIgnoreCase("null")) {
                            idNumber = null;
                        }
                        Logger.debug("Parsed studentIdNumber: " + idNumber);
                    }

                    String studentType = null;
                    if (json.has("studentType")) {
                        studentType = json.path("studentType").asText().trim();
                        if (studentType.isEmpty() || studentType.equalsIgnoreCase("null")) {
                            studentType = null;
                        }
                        Logger.debug("Parsed studentType: " + studentType);
                    }

                    String studentYear = null;
                    if (json.has("studentYear")) {
                        studentYear = json.path("studentYear").asText().trim();
                        if (studentYear.isEmpty() || studentYear.equalsIgnoreCase("null")) {
                            studentYear = null;
                        }
                        Logger.debug("Parsed studentYear: " + studentYear);
                    }

                    String studentMajor = null;
                    if (json.has("studentMajor")) {
                        studentMajor = json.path("studentMajor").asText().trim();
                        if (studentMajor.isEmpty() || studentMajor.equalsIgnoreCase("null")) {
                            studentMajor = null;
                        }
                        Logger.debug("Parsed studentMajor: " + studentMajor);
                    }

                    String firstEnrollDate = null;
                    if (json.has("studentEnrollDate")) {
                        firstEnrollDate = json.path("studentEnrollDate").asText().trim();
                        if (firstEnrollDate.isEmpty() || firstEnrollDate.equalsIgnoreCase("null")) {
                            firstEnrollDate = null;
                        }
                        Logger.debug("Parsed firstEnrollDate: " + firstEnrollDate);
                    }

                    String sql = "UPDATE student_info SET " +
                            "id_number = :idNumber, " +
                            "student_type = :studentType, " +
                            "student_year = :studentYear, " +
                            "major = :studentMajor, " +
                            "first_enroll_date = :firstEnrollDate " +
                            "WHERE user_id = :userId";

                    SqlUpdate update = Ebean.createSqlUpdate(sql);
                    update.setParameter("idNumber", idNumber);
                    update.setParameter("studentType", studentType);
                    update.setParameter("studentYear", studentYear);
                    update.setParameter("studentMajor", studentMajor);
                    update.setParameter("firstEnrollDate", firstEnrollDate);
                    update.setParameter("userId", userId);

                    Logger.debug("Executing native SQL update: " + sql);
                    Logger.debug("With parameters: idNumber=" + idNumber + ", studentType=" + studentType +
                            ", studentYear=" + studentYear + ", studentMajor=" + studentMajor +
                            ", firstEnrollDate=" + firstEnrollDate + ", userId=" + userId);

                    int rows = update.execute();
                    Logger.debug("Native SQL update executed, rows updated: " + rows);
                }


                return created(Json.toJson(user));
            } else {
                return Common.badRequestWrapper("User not found in database");
            }
        } catch (Exception e) {
            Logger.debug("UserController.updateUser exception: " + e.toString());
            return Common.badRequestWrapper("User could not be updated.");
        }
    }
    /*********************************************** End of Update User ***********************************************/

    /*********************************************** User Detail ******************************************************/
    /**
     * This method intends to returns a user's information from a user id.
     *
     * @param userId user userId
     * @return user
     */
    public Result userDetail(Long userId) {
        if (userId == null) {
            Logger.debug("User userId is null or empty for UserController.userDetail");
            return Common.badRequestWrapper("User is not valid");
        }

        try {
//            User user = User.find.query().where().eq("is_active", ACTIVE).eq("id", userId).findOne();
            User user = User.find.query().where().eq("id", userId).findOne();

            if (user == null) {
                Logger.info("User not found with userId: " + userId);
                return notFound("User not found with userId: " + userId);
            }
            Logger.info("User found with userId: " + userId);
            Logger.info("User avatar before signing: " + user.getAvatar());

            String avatarKey = user.getAvatar();
            String s3Prefix = "https://ecopro-aws-bucket.s3.amazonaws.com/";
            String avatarURL = "";
            if (avatarKey == null || avatarKey.isEmpty()) {
                Logger.info("Avatar is null or empty, using default avatar if available.");
            } else {
                if (avatarKey.startsWith(s3Prefix)) {
                    avatarKey = avatarKey.substring(s3Prefix.length());
                }
                avatarURL = Common.getSignedURL(config, avatarKey, 60);
            }
            Logger.info("Signed avatar URL: " + avatarURL);

            user.setAvatar(avatarURL);

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode node = mapper.valueToTree(user);
            String result = node.toString();
            return ok(result);
        } catch (Exception e) {
            Logger.debug("UserController.userDetail exception: " + e.toString());
            return notFound("User was not found.");
        }
    }

    public class ResearcherInfoDTO {
        public String highestDegree;
        public String orcid;
        public String researchFields;
        public String school;
        public String department;
    }
    public Result getResearcherInfo(Long userId) {
        if (userId == null) {
            Logger.debug("UserId is null in getResearcherInfo");
            return Common.badRequestWrapper("UserId is not valid");
        }
        try {
            ResearcherInfo researcherInfo = ResearcherInfo.find.query()
                    .where().eq("user.id", userId)
                    .findOne();
            Logger.info("Queried ResearcherInfo: " + researcherInfo);

            if (researcherInfo == null) {
                return notFound("No ResearcherInfo found for userId: " + userId);
            }

            ResearcherInfoDTO dto = new ResearcherInfoDTO();
            dto.highestDegree = researcherInfo.getHighestDegree();
            dto.orcid = researcherInfo.getOrcid();
            dto.researchFields = researcherInfo.getResearchFields();
            dto.school = researcherInfo.getSchool();
            dto.department = researcherInfo.getDepartment();

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode researcherNode = mapper.valueToTree(dto);
            Logger.info("ResearcherInfo DTO: " + researcherNode);

            return ok(researcherNode);
        } catch (Exception e) {
            Logger.debug("Exception in getResearcherInfo: " + e.toString());
            return internalServerError("Error fetching researcher info");
        }
    }
    public class StudentInfoDTO {
        public String idNumber;
        public String studentYear;
        public String studentType;
        public String major;
        public String firstEnrollDate;
    }

    public Result getStudentInfo(Long userId) {
        if (userId == null) {
            Logger.debug("UserId is null in getStudentInfo");
            return Common.badRequestWrapper("UserId is not valid");
        }
        try {
            StudentInfo studentInfo = StudentInfo.find.query()
                    .where().eq("user.id", userId)
                    .findOne();
            Logger.info("Queried StudentInfo: " + studentInfo);

            if (studentInfo == null) {
                return notFound("No StudentInfo found for userId: " + userId);
            }

            StudentInfoDTO dto = new StudentInfoDTO();
            dto.idNumber = studentInfo.getIdNumber();
            dto.studentYear = studentInfo.getStudentYear();
            dto.studentType = studentInfo.getStudentType();
            dto.major = studentInfo.getMajor();
            dto.firstEnrollDate = studentInfo.getFirstEnrollDate();

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode studentNode = mapper.valueToTree(dto);
            Logger.info("StudentInfo DTO: " + studentNode);

            return ok(studentNode);
        } catch (Exception e) {
            Logger.debug("Exception in getStudentInfo: " + e.toString());
            return internalServerError("Error fetching student info");
        }
    }
    /*********************************************** End of User Detail ***********************************************/

    /*********************************************** User Login *******************************************************/
    /**
     * Logs an existing user in given the email and password of the user.
     *
     * @return the user data if valid, else an error.
     */
    public Result userLogin() {
        JsonNode json = request().body().asJson();
        if (json == null) {
            Logger.debug("Cannot check user, expecting Json data");
            return Common.badRequestWrapper("Cannot check user, expecting Json data");
        }

        try {
            String email = json.path("email").asText();
            String password = MD5Hashing(json.path("password").asText());
            String isResearcher = json.path("isResearcher").asText();
            /*
            List<User> users =
                    User.find.query().setMaxRows(1).where().eq("email", email).eq("is_active",
                            "True").findList();

             */
            List<User> users =
                    User.find.query().setMaxRows(1).where().eq("email", email).findList();
            if (users.size() == 0) {
                return Common.badRequestWrapper("User is not valid");
            }

            User user = users.get(0);
            if ("False".equalsIgnoreCase(user.getIsActive())) {
                return Common.badRequestWrapper("User has not been activated. Please check your email for the activation link.");
            }
            String result = new String();

            if (user.getPassword().equals(password)) {
                user.save();
                JsonNode jsonNode = Json.toJson(user);
                result = jsonNode.toString();

//                OperationLog operationLog = new OperationLog(
//                        null, OperationLog.CHANNEL_TYPE.BACKEND, user.getId(), System.currentTimeMillis(), request().remoteAddress(), "user/userLogin", "userLogin", "controllers.UserController", ""
//                );
//                operationLog.save();

                boolean recorded = operationLoggingService.recordOperationLog(
                        null, OperationLog.CHANNEL_TYPE.BACKEND, user.getId(), System.currentTimeMillis(), request().remoteAddress(), "user/userLogin", "userLogin", "controllers.UserController", ""
                );


                return ok(result);
            } else {
                Logger.debug("User is not valid");
                return Common.badRequestWrapper("User is not valid");

            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.debug("UserController.userLogin() exception: " + e.toString());
            return notFound("User cannot logged in.");
        }
    }

    /**
     * Checks if a user with the same email id as provided is already present.
     * Note: If an email address has been registered before, even if the user has become inactive, the email address
     * cannot
     * be registered as new any longer.
     *
     * @return this email is valid message if email is not already used, else an error stating email has been used.
     */
    public Result checkNewUserEmailAvailability() {
        JsonNode json = request().body().asJson();
        if (json == null) {
            Logger.debug("Cannot check email, expecting Json data");
            return badRequest("Cannot check email, expecting Json data");
        }
        String email = json.path("email").asText();
        if (email == null || email.isEmpty()) {
            Logger.debug("email is null or empty");
            return Common.badRequestWrapper("email is null or empty.");
        }
        System.out.println("<<<1.1 check email: " + email);
        try {
            List<User> query_users = User.find.query().where().eq("email", email).findList();
            if (query_users == null || query_users.size() == 0) {
                Logger.info("UserController.checkNewUserEmailAvailability the email can be used: " + email);
                return ok("This new email can be used");
            } else {
                Logger.info("UserController.checkNewUserEmailAvailability the email cannot be used: " + email);
                return Common.badRequestWrapper("This email address has been used by user: " +
                        query_users.get(0).getUserName());
            }
        } catch (Exception e) {
            Logger.debug("UserController.checkNewUserEmailAvailability() exception: " + e.toString());
            return internalServerError("UserController.checkNewUserEmailAvailability exception: " +
                    e.toString());
        }
    }

    /**
     * Validate the format of an email address.
     *
     * @return 200 if the email follows proper format or 400 if the email does not follow the format.
     */
    public Result validateEmail() {
        JsonNode json = request().body().asJson();
        String email = json.path("email").asText();
        String pattern = "^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,4})$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(email);
        if (!m.matches() || Constants.FORBIDDEN_EMAILS.contains(email.substring(email.indexOf("@"))))
            return badRequest("Invalid Email");
        return ok();
    }
    /*********************************************** End of User Login ************************************************/

    /*********************************************** User List ********************************************************/
    /**
     * Gets a list of all the users based on optional offset and limit and sort
     *
     * @param pageLimit    shows the number of rows we want to receive
     * @param offset       shows the start index of the rows we want to receive
     * @param sortCriteria shows based on what column we want to sort the data
     * @return the list of users.
     */
    public Result userList(Optional<Integer> pageLimit, Optional<Integer> offset, Optional<String> sortCriteria) {
        List<User> users = new ArrayList<>();
        String sortOrder = Common.getSortCriteria(sortCriteria, USER_DEFAULT_SORT_CRITERIA);

        try {
//            List<User> currentUsers = User.find.all();
//            System.out.println("=========# of current users:" + currentUsers.size());
//
//            List<TeamMember> teamMembers = TeamMember.find.all();
//            System.out.println("=========# of teammembers:" + teamMembers.size());
//            for (int i = 0; i<teamMembers.size(); i++) {
//                TeamMember teamMember = teamMembers.get(i);
//
//                System.out.println("===1.1: " + i + "::: id:" + teamMember.getId());
//                String name = teamMember.getName();
//                System.out.println("user:" + name);
//
//                String firstName = name.split(" ")[0];
//                String lastName;
//                Project project = teamMember.getProject();
//                if (name.trim().length() > firstName.trim().length())
//                    lastName = name.split(" ")[1];
//                else
//                    lastName = firstName;
//
//                List<User> users2 = User.find.query().where().eq("first_name", firstName).eq("last_name", lastName)
//                .findList();
//                if (users2 != null && users2.size()>0) {
//                    User currentUser = users2.get(0);
//                    if (currentUser != null) {
//                        System.out.println("$$$$$$$name: " + name);
//                        List<Project> projectList = currentUser.getParticipatedProjects();
//                        if (!projectList.contains(project))
//                            projectList.add(project);
//                        currentUser.setParticipatedProjects(projectList);
//                        currentUser.update();
//                        continue;
//                    }
//                }
//
//                User user = new User();
//                user.setFirstName(firstName);
//                user.setLastName(lastName);
//
//
//                user.setUserName(name);
//                user.setEmail("nasa-opennex@gmail.com");
//
//
//                String randomPassword = randomPassword();
//                String password = MD5Hashing(randomPassword);
//                user.setPassword(password);
//
//                user.setLevel("normal");
//                user.setIsActive("True");
//                List<Project> projectList = new ArrayList<Project>();
//                projectList.add(project);
//                user.setParticipatedProjects(projectList);
//                user.save();
//            }


            users = User.find.query().where().eq("is_active", ACTIVE).orderBy(sortOrder).findList();
            RESTResponse response = userService.paginateResults(users, offset, pageLimit, sortOrder);
            return ok(response.response());
        } catch (Exception e) {
            e.printStackTrace();
            Logger.debug("UserController.userList exception: " + e.toString());
            return notFound("User not found");
        }
    }


    /*********************************************** End of User List *************************************************/

    /**
     * Find user by multiple condition, including username, organization, email, mailing address, phone number,
     * research area, etc.
     *
     * @return users that match the condition
     * TODO: How to handle more conditions???
     */
    public Result searchUserByCondition(Optional<Integer> pageLimit, Optional<Integer> offset,
                                        Optional<String> sortCriteria) {
        String sortOrder = Common.getSortCriteria(sortCriteria, USER_DEFAULT_SORT_CRITERIA);
        JsonNode json = request().body().asJson();
        if (json == null) {
            Logger.debug("User cannot be queried, expecting Json data");
            return badRequest("User cannot be queried, expecting Json data");
        }
        try {
            String username = json.get("name").asText();
//            String organization = json.get("organization").asText();
            String email = json.get("Email").asText();
            String mailing_address = json.get("MailingAdd").asText();
//            String phone = json.get("PhoneNum").asText();
//            String research_area = json.get("Research Area").asText();
            //Search user by conditions
            ExpressionList<User> query = User.find.query().where().eq("is_active", "True").
//                    icontains("organization", organization.toLowerCase()).
                    icontains("email", email.toLowerCase()).
                    icontains("mailing_address", mailing_address.toLowerCase())
//                    icontains("phone_number", phone.toLowerCase()).
//                    icontains("research_fields", research_area.toLowerCase());
                    ;

            String[] names = username.split(" ");
            for (String name : names) {
                query = query.icontains("user_name", name.toLowerCase());
            }

            List<User> users = query.orderBy(sortOrder).findList();

            RESTResponse response = userService.paginateResults(users, offset, pageLimit, sortOrder);
            return ok(response.response());
        } catch (Exception e) {
            e.printStackTrace();
            Logger.debug("User cannot be queried, query is corrupted");
            return badRequest("User cannot be queried, query is corrupted");
        }
    }


    /**
     * Sort the given list of users
     *
     * @param users        List of users to be sorted
     * @param sortCriteria sort criteria
     * @return sorted list of users.
     */
    public static void sortUsers(List<User> users, String sortCriteria) {
        if (sortCriteria.equals("user_name")) {
            Comparator<User> com = new Comparator<User>() {
                @Override
                public int compare(User o1, User o2) {
                    String name1 = o1.getUserName();
                    String name2 = o2.getUserName();
                    return name1.toLowerCase().compareTo(name2.toLowerCase());
                }
            };
            Collections.sort(users, com);
        } else {
            Comparator<User> com = new Comparator<User>() {
                @Override
                public int compare(User o1, User o2) {
                    return new Long(o1.getId()).compareTo(new Long(o2.getId()));
                }
            };
            Collections.sort(users, com);
        }
    }


    /**
     * Gets the list of all "normal" users.
     * Related to: getAllUsersInAllRoles()
     * Notes: get all users including admin should use another method getAllUsersInAllRoles()
     *
     * @return the list of all normal users.
     */
    public Result getAllNormalUsers() {

        List<User> users = null;
        String result = new String();

        try {
            users = User.find.query().where(Expr.and(Expr.eq("is_active", "True"),
                    Expr.or(Expr.eq("level", "normal")
                    , Expr.isNull("level")))).findList();
            if (users == null) {
                System.out.println("No User found in getAllUsers() under UserController");
            }
            JsonNode jsonNode = Json.toJson(users);
            result = jsonNode.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ok(result);
    }


    /**
     * Delete user Image by user Id
     *
     * @param userId user id
     * @return delete the image if found
     */
    public Result deleteImageByUserId(Long userId) {
        if (userId == null) {
            return Common.badRequestWrapper("userId is null thus cannot delete image for it.");
        }
        try {
            User user = User.find.byId(userId);
            if (user != null) {
                Common.deleteFileFromS3(config, "user", "Image", userId);
                user.setAvatar("");
                user.save();
                return ok("User image deleted successfully for user id: " + userId);
            } else {
                return Common.badRequestWrapper("Cannot find user thus cannot delete image for it.");
            }
        } catch (Exception e) {
            return notFound("Image not deleted");
        }
    }

    /**
     * Update user Image by user Id
     *
     * @param userId user id
     * @return updating the user image if possible
     */
    public Result updateImageByUserId(Long userId) {
        if (userId == null) {
            return Common.badRequestWrapper("userId is null thus cannot delete image for it.");
        }
        try {
            User user = User.find.byId(userId);
            if (request().body() == null || request().body().asRaw() == null) {
                return Common.badRequestWrapper("The request cannot be empty");
            }
            if (user != null) {
//                String url = Common.uploadFile(config, "user", "Image", userId, request());
                java.io.File file = request().body().asRaw().asFile();
                String fileType = null;
                try {
                    Path filePath = Paths.get(file.getAbsolutePath());
                    String mimeType = Files.probeContentType(filePath);
                    if (mimeType != null && mimeType.contains("/")) {
                        fileType = mimeType.split("/")[1];
                    } else {
                        MimetypesFileTypeMap mimeMap = new MimetypesFileTypeMap();
                        mimeType = mimeMap.getContentType(file);
                        fileType = mimeType.contains("/") ? mimeType.split("/")[1] : "unknown";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    fileType = "unknown";
                }
                String url = S3Utils.uploadFile(AWS_BUCKET_NAME,file,AWS_FILE_NAME_PREFIX+"user/"+userId+"."+fileType,"Image","","");
//                System.out.println("image url:" + url);

                user.setAvatar(url);
                user.save();
                return ok("User image updated successfully.");
            }
            return Common.badRequestWrapper("user with the given ID not found!");
        } catch (Exception e) {
            e.printStackTrace();
            return Common.badRequestWrapper("Image could not be updated.");
        }
    }

    /**
     * This method intends to delete a user, to make it inactive.
     *
     * @return TODO: Make it a GET call.
     */
    public Result inactivateUserPOST() {
        JsonNode json = request().body().asJson();
        if (json == null) {
            return Common.badRequestWrapper("Cannot check user, expecting Json data");
        }

        try {
            Long id = Long.parseLong(json.path("id").asText());
            User user = User.find.query().select("id").where().eq("id", id).eq("is_active",
                    "True").findOne();

            if (user == null) {
                return notFound("User to be deleted not found");
            } else {
                user.setIsActive("False");
                user.save();
                return ok("User deleted");
            }
        } catch (Exception e) {
            Logger.debug("UserController.inactivateUserPOST exception: " + e.toString());
            return internalServerError("UserController.inactivateUserPOST exception: " + e.toString());
        }
    }


    /**
     * This method intends to register a user automatically.
     *
     * @return
     */
    public Result autoRegisterUser() {
        JsonNode json = request().body().asJson();
        if (json == null) {
            return Common.badRequestWrapper("User not created, expecting Json data");
        }
        try {
            User user = new User();
            user.deserializeFromJson(json);
            String randomPassword = randomPassword();
            String password = MD5Hashing(randomPassword);
            user.setPassword(password);

            String firstName = json.path("firstName").asText();
            String url = json.get("url").asText();
            String body = "Hi " + firstName + ",\n\nYour have been registered in OpenNEX automatically, and the " +
                    "temporary password is: " + randomPassword + "\n\n"
                    + "you can login by clicking link below. \n" + url + "\n\n"
                    + "Best Regards, \n"
                    + "OpenNEX Group";

            user.setLevel("normal");
            user.setIsActive("True");
            user.save();

            // Send individual mail.
            String email = json.path("email").asText();
            EmailUtils.sendIndividualEmail(config, email, "Your have been registered in CSHub", body);
            Logger.info("Send register email succeeded!");
        } catch (MessagingException e) {
            Logger.error("Send register email failed!");
            return internalServerError("UserController.autoRegisterUser exception: " + e.toString());
        } catch (Exception e) {
            Logger.debug("UserController.autoRegisterUser exception: " + e.toString());
            return internalServerError("UserController.autoRegisterUser exception: " + e.toString());
        }

        return ok();
    }


    //**************************** END BASIC REFACTORING *************************************************************//
    //**************************** END BASIC REFACTORING *************************************************************//
    //**************************** END BASIC REFACTORING *************************************************************//


    /******************************************************************************************************************/
    /******************************************************************************************************************/
    /******************************************************************************************************************/


    public Result updateLevel(Long id) {

        if (id < 0) {
            System.out.println("id is negative!");
            return badRequest("id is negative!");
        }
        JsonNode json = request().body().asJson();
        if (json == null) {
            System.out.println("IsActive Flag not updated, expecting Json data");
            return badRequest("IsActive Flag not updated, expecting Json data");
        }

        String isAdmin = json.findPath("isAdmin").asText();

        try {
//			BugReport bugReport = bugReportRepository.findOne(id);
            User user = User.find.query().where().eq("id", id).findOne();
            if (user == null) {
                return Common.badRequestWrapper("The user does not exist!");
            }
            if (isAdmin == "1") {
                user.setLevel("admin");
                System.out.println("admin");
            } else {
                user.setLevel("normal");
                System.out.println("normal");
            }
            user.save();
            System.out.println("IsAdmin Flag updated: "
                    + user.getId());
            return created("IsAdmin Flag updated: "
                    + user.getId());
        } catch (PersistenceException pe) {
            pe.printStackTrace();
            System.out.println("Isdmin Flag not updated: " + id);
            return badRequest("IsAdmin Flag not updated " + id);
        }
    }


    /**
     * Gets the list of all users including admins.
     * <p>
     * // Roles(multiple roles can be separated by semicolon)
     * // Admin: admin
     * // Superuser: superuser
     * // Normal: normal
     * // Guest: guest
     * // Tester: tester
     * // Other:other
     *
     * @return the list of all users including admins.
     */
    public Result getAllUsersInAllRoles() {

        List<User> users = User.find.query().where().eq("is_active", "True").findList();
        if (users == null) {
            Logger.debug("No User found");
        }

        return ok(userService.userList2JsonArray(users));
    }


    public static String MD5Hashing(String password) {

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        md.update(password.getBytes());
        byte byteData[] = md.digest();

        //convert the byte to hex format method
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            String hex = Integer.toHexString(0xff & byteData[i]);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString();
    }

    public static String AESEncode(String encodeRules, String content) {
        try {
//			SecretKey original_key=kgen.generateKey();
//			byte[] raw=original_key.getEncoded();
            SecretKey key = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] byte_encode = content.getBytes(StandardCharsets.UTF_8);
            byte[] byte_AES = cipher.doFinal(byte_encode);
            String AES_encode = new String(Base64.getEncoder().encode(byte_AES), StandardCharsets.UTF_8);
            return AES_encode;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String AESDncode(String encodeRules, String content) {
        try {
//			KeyGenerator keygen=KeyGenerator.getInstance("AES");
//			keygen.init(128, new SecureRandom(encodeRules.getBytes()));
//			SecretKey original_key=kgen.generateKey();
//			System.out.println("Secret key: " + original_key);
//			raw=original_key.getEncoded();
            SecretKey key = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] byte_content = Base64.getDecoder().decode(content);
            byte[] byte_decode = cipher.doFinal(byte_content);
            String AES_decode = new String(byte_decode, StandardCharsets.UTF_8);
            return AES_decode;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * This should return user by email
     *
     * @return user
     */
    public Result getUserByEmail() {
        JsonNode json = request().body().asJson();
        if (json == null) {
            return Common.badRequestWrapper("Cannot check email, expecting Json data");
        }
        String email = json.path("email").asText();
        String result = new String();
        try {
            List<User> users =
                    User.find.query().setMaxRows(1).where().eq("email", email).eq("is_active",
                            "True").findList();
            User user = users.get(0);
            JsonNode jsonNode = Json.toJson(user);
            result = jsonNode.toString();
        } catch (Exception e) {
            return Common.badRequestWrapper("User not found");
        }
        return ok(result);
    }

    /**
     * Search user by username
     *
     * @param display_name
     * @return the target user
     */
    public Result userSearch(String display_name) {
        if (display_name == null) {
            System.out.println("Display name is null or empty!");
            return Common.badRequestWrapper("Display name is null or empty!");
        }
        List<User> users = User.find.query().where().eq("is_active", "True").
                like("user_name", "%" + display_name +
                "%").findList();
        if (users == null || users.size() == 0) {
            return notFound("User not found with with display name: " + display_name);
        }
        String result = new String();
        JsonNode jsonNode = Json.toJson(users);
        result = jsonNode.toString();

        return ok(result);
    }


    public Result getActiveUsers(String format) {
        List<User> orderUsers = User.find.query().where().eq("is_active", "True").
                order("service_execution_counts " +
                "desc").findList();
        String result = new String();
        if (format == null) {
            return Common.badRequestWrapper("format is null.");
        }
        if (format.equals("json")) {
            result = Json.toJson(orderUsers).toString();
        }
        return ok(result);
    }


    public Result sendPasswordEmail() {
        JsonNode json = request().body().asJson();
        String email = json.get("email").asText();
        String randomPassword = randomPassword();
        String body = "Hi, \n\nYour temporary password is: " + randomPassword + "\n\n"
                + "Best Regards, \n"
                + "SMU-Lyle-Sci-Hub Group";
        User user = User.find.query().where().eq("is_active", "True").eq("email", email).
                findOne();
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("email", email);
        String error = null;
        if (null == user) error = "No user matched. Please check your email address.";
        else {
            user.setPassword(MD5Hashing(randomPassword));
            user.save();
            try {
                // Send individual mail.
                EmailUtils.sendIndividualEmail(config, email, "[SMU-Lyle-Sci-Hub]Your password has been reset", body);
            } catch (MessagingException e) {
                e.printStackTrace();
                error = "Password reset failed. Please try again later.";
            }
        }
        if (null != error)
            node.put("error", error);
        return ok(node);
    }

    /**
     * Send email to validate and finish register account
     *
     * @return
     */
    public Result sendRegisterEmail() throws UnsupportedEncodingException {
        Logger.info("sendRegisterEmail: Begin processing registration email.");
        JsonNode json = request().body().asJson();
        String email = json.get("email").asText();
        String id = json.get("id").asText();
        String url = json.get("url").asText();
        Logger.info("sendRegisterEmail: Received parameters - email: " + email + ", id: " + id + ", url: " + url);

        User thisUser = User.find.byId(Long.valueOf(id));

        String token = generateActivationToken(id);
        thisUser.setToken(token);
        thisUser.save();
        Logger.info("sendRegisterEmail: URL safe encoded token: " + token);
        /*
        try {
            URLCodec urlCodec = new URLCodec();
            hashcode = urlCodec.encode(hashcode, StandardCharsets.UTF_8.name());
            thisUser.setToken(hashcode);
            thisUser.save();
            Logger.info("sendRegisterEmail: URL encoded hashcode: " + hashcode);
        } catch (Exception e) {
            e.printStackTrace();
        }
         */

        String link = url + token;
        Logger.info("sendRegisterEmail: Activation link constructed: " + link);

        String body = "Hi, \n\nThank you for your registration!\n"
                + "Now the last step is to verify your email address. \n"
                + "Please click the link below to finish the registration process.\n"
                + link + "\n\n"
                + "Best Regards, \n"
                + "SMU-Lyle-Sci-Hub Group";
        Logger.info("sendRegisterEmail: Email body constructed: " + body);

        try {
            // Send individual mail.
            EmailUtils.sendIndividualEmail(config, email, "No-reply: Registration Verify", body);
            Logger.info("Send register email succeeded!");
        } catch (MessagingException e) {
            e.printStackTrace();
            Logger.error("Send register email failed!");
        }
        return ok();
    }

    public Result resendRegisterEmail() throws UnsupportedEncodingException {
        Logger.info("resendRegisterEmail: Begin processing registration email.");
        JsonNode json = request().body().asJson();
        String email = json.get("email").asText();
        String url = json.get("url").asText();
        Logger.info("resendRegisterEmail: Received parameters - email: " + email + ", url: " + url);

        User thisUser = User.find.query().where().eq("email", email).findOne();

        if (thisUser == null) {
            Logger.error("resendRegisterEmail: No user found with email: " + email);
            return badRequest("User not found");
        }

        String id = String.valueOf(thisUser.getId());

        String token = generateActivationToken(id);
        thisUser.setToken(token);
        thisUser.save();
        Logger.info("resendRegisterEmail: URL safe encoded token: " + token);

        String link = url + token;
        Logger.info("resendRegisterEmail: Activation link constructed: " + link);

        String body = "Hi, \n\nThank you for your registration!\n"
                + "Now the last step is to verify your email address. \n"
                + "Please click the link below to finish the registration process.\n"
                + link + "\n\n"
                + "Best Regards, \n"
                + "SMU-Lyle-Sci-Hub Group";
        Logger.info("resendRegisterEmail: Email body constructed: " + body);

        try {
            // Send individual mail.
            EmailUtils.sendIndividualEmail(config, email, "No-reply: Registration Verify", body);
            Logger.info("Resend register email succeeded!");
        } catch (MessagingException e) {
            e.printStackTrace();
            Logger.error("Resend register email failed!");
        }
        return ok();
    }

    private String generateActivationToken(String id) throws UnsupportedEncodingException {
        String dataToEncrypt = id + ":" + System.currentTimeMillis();
        // String hashcode = AESEncode("AES", id);
        String hashcode = AESEncode("AES", dataToEncrypt);
        Logger.info("AESEncode result: " + hashcode);
        byte[] hashBytes = Base64.getDecoder().decode(hashcode);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes);
        Logger.info("URL safe encoded token: " + token);
        return token;
    }

    public Result userActivation(String hashcode) {
        ObjectNode successResponse = Json.newObject();
        Logger.info("Received activation hashcode: " + hashcode);

        User user = User.find.query().where().eq("token", hashcode).findOne();

        if (user == null) {
            Logger.error("No user found with the provided hashcode: " + hashcode);
            successResponse.put("msg", "Invalid activation link!");
        } else {
            user.setIsActive("True");
            user.update();
            Logger.info("User " + user.getEmail() + " activated successfully.");
            successResponse.put("msg", "Activation successful!");

        }
        return ok(successResponse);
    }

    @With(OperationLoggingAction.class)
    public Result updatePassword() {
        JsonNode json = request().body().asJson();
        String email = json.get("email").asText();
        String password = json.get("password").asText();
        User user = User.find.query().where().eq("is_active", "True").eq("email", email).
                findOne();
        user.setPassword(MD5Hashing(password));
        user.save();
        return ok();
    }

    public static String randomPassword() {
        String stringDic = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(10);

        for (int i = 0; i < 10; i++) {
            sb.append(stringDic.charAt(rnd.nextInt(stringDic.length())));
        }
        return sb.toString();
    }


    public void sendResultEmail(Long userId, JsonNode contentJson, String actionType) {
        try {

            User thisUser = User.find.byId(Long.valueOf(userId));
            String email = thisUser.getEmail();

            String applicationLetterBody =
                    "Your users " + actionType + " action is done. This is your result:\n." +
                            toPrettyFormat(contentJson.toString());
            String subject = "Your batch action is done";
            EmailUtils.sendIndividualEmail(config, email, subject, applicationLetterBody);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendResultEmailToUser(JsonNode userNode, String content) {
        try {
            String applicationLetterBody = "";
            if (content != null && !content.equals("")) {
                applicationLetterBody = content;
            } else {
                applicationLetterBody =
                        "Dear " + userNode.get("firstName").asText() + " " + userNode.get("lastName").asText() + ",\n" +
                                "We have successfully added you to OpenNEX. The email is " + userNode.get("email").
                                asText() + ", the temporary password is " + userNode.get("password").asText() +
                                ". Please go to https://opennex.org and log in to change password, filling your " +
                                "profile into " +
                                "OpenNEX.";
            }
            String subject = "You have been added to OpenNEX";
            EmailUtils.sendIndividualEmail(config, userNode.get("email").asText(), subject, applicationLetterBody);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Convert a JSON string to pretty print version
     *
     * @param jsonString
     * @return
     */
    public String toPrettyFormat(String jsonString) {
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(jsonString).getAsJsonObject();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(json);

        return prettyJson;
    }


}
