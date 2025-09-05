package controllers;

import actions.OperationLoggingAction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.typesafe.config.Config;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.forms.*;
import com.itextpdf.forms.fields.*;

import models.*;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import services.AccessTimesService;
import services.TAJobApplicationService;
import services.TAJobService;
import services.UserService;
import utils.Constants;
import utils.RESTfulCalls;
import views.html.*;


import javax.inject.Inject;
import java.awt.*;
import java.io.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

import static controllers.Application.checkLoginStatus;
import static utils.Common.beginIndexForPagination;
import static utils.Common.endIndexForPagination;

public class TAJobController extends Controller {

    @Inject
    Config config;

    private final TAJobService tajobService;
    private final TAJobApplicationService tajobApplicationService;

    private final UserService userService;
    private final AccessTimesService accessTimesService;
    private String [] courses = null;

    private Form<TAJob> tajobFormTemplate;
    private Form<TAJobApplication> tajobApplicationFormTemplate;
    private FormFactory myFactory;


    /******************************* Constructor **********************************************************************/
    @Inject
    public TAJobController(FormFactory factory,
                           TAJobService tajobService,
                           UserService userService,
                           TAJobApplicationService tajobApplicationService,
                           AccessTimesService accessTimesService) {
        tajobFormTemplate = factory.form(TAJob.class);
        myFactory = factory;
        tajobApplicationFormTemplate = factory.form(TAJobApplication.class);

        this.tajobApplicationService = tajobApplicationService;
        this.tajobService = tajobService;
        this.userService = userService;
        this.accessTimesService = accessTimesService;

        this.courses = new String[]{
                "CS 1340 Introduction to Computing Concepts Python",
                "CS 1341 Principles of Computer Science Java",
                "CS 1342 Programming Concepts C++",
                "CS 2353 Discrete Math",
                "CS 2240 Assembly Language ARM",
                "CS 2341 Data Structures",
                "CS 3330 Database Concepts",
                "CS 3339 Information Assurance and Security",
                "CS 3342 Programming Language",
                "CS 3345 Graphical User Interface Design and Implementation",
                "CS 3353 Fundamentals of Algorithms",
                "CS 4344 Computer Networks and Distributed Systems",
                "CS 4345 Software Engineering Principles",
                "CS 4351/4352 Senior Design",
                "CS 4381 Digital Computer Design",
                "CS 5/7314 Software Testing and Quality Assurance",
                "CS 5/7315 Software Project Planning and Management",
                "CS 5/7316 Software Requirements",
                "CS 5/7317 Leadership for Architecting Software Systems",
                "CS 5/7319 Software Architecture and Design",
                "CS 5/7320 Artificial Intelligence",
                "CS 5/7323 Mobile Applications for Sensing and Learning",
                "CS 5/7331 Data Mining",
                "CS 5/7339 Computer System Security",
                "CS 5/7343 Operating Systems and System Software",
                "CS 5/7345 Advanced Application Programming",
                "CS 5/7350 Algorithm Engineering",
                "CS 5/7383 Computer Graphics",
                "CS 8313 Object Oriented Analysis and Design"
        };
    }


    /************************************************** TA Job Registtation ********************************************/

    /**
     * This method intends to render the TA job registtation page.
     *
     * @return
     */
    @With(OperationLoggingAction.class)
    public Result tajobRegisterPage() {
        checkLoginStatus();
        return ok(tajobRegister.render());
    }

    /**
     * This method intends to gather TA job registtation information and create an TA job in database.
     *
     * @return
     */
    public Result tajobRegisterPOST() {
        checkLoginStatus();
        try {
            Form<TAJob> tajobForm = tajobFormTemplate.bindFromRequest();
            Http.MultipartFormData body = request().body().asMultipartFormData();
            JsonNode jsonData = tajobService.serializeFormToJson(tajobForm);
            JsonNode response = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.TAJOB_REGISTER_POST), jsonData);
            if (response == null || response.has("error")) {
                Logger.debug("TAJobController.tajobRegisterPOST: Cannot create the TA job in backend");
                return ok(registrationError.render("TAJob"));
            }

            long tajobId = response.asLong();
//            challengeService.savePDFToProject(body, projectId);

            return ok(registerConfirmation.render(new Long(tajobId), "Tajob"));
        } catch (Exception e) {
            Logger.debug("TAJobController job registtation exception: " + e.toString());
            return ok(registrationError.render("TAJob"));
        }
    }

    /************************************************** End of TAJob Registtation **************************************/


    /************************************************** TAJob Edit *****************************************************/

    /**
     * This method intends to prepare to edit an TA job.
     *
     * @patam tajobId: TA job id
     * @return
     */
    @With(OperationLoggingAction.class)
    public Result tajobEditPage(Long tajobId) {
        try {
            TAJob tajob = tajobService.getTAJobById(tajobId);
            if (tajob == null) {
                Logger.debug("TAJob.tajobEditPage exception: cannot get TA job by id");
                Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
                return ok(generalError.render());
            }

            return ok(tajobEdit.render(tajob));
        } catch (Exception e) {
            Logger.debug("TAJobController.tajobEditPage() exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }

    /**
     * This method intends to submit the edit in the TA job edit page.
     *
     * @patam tajobId TA job id
     * @return
     */
    public Result tajobEditPOST(Long tajobId) {
        checkLoginStatus();

        try {
            Form<TAJob> tajobForm = tajobFormTemplate.bindFromRequest();
            Http.MultipartFormData body = request().body().asMultipartFormData();
            ObjectMapper mapper = new ObjectMapper();

            JsonNode jsonData = tajobService.serializeFormToJson(tajobForm);
            JsonNode response = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.TAJOB_EDIT_POST + tajobId), jsonData);
            if (response == null || response.has("error")) {
                Logger.debug("Cannot update the TA job");
                return redirect(routes.TAJobController.tajobEditPage(tajobId));
            }

            String record = tajobForm.field("record").value();

            String pdfRecord = tajobForm.field("pdfRecord").value();
//            if (pdfRecord.equals("delete")) {
//                JsonNode imgResponse = RESTfulCalls.deleteAPI(RESTfulCalls.getBackendAPIUrl(config,
//                        Constants.DELETE_PROJECT_PDF + projectId));
//            }
//            challengeService.savePDFToProject(body, projectId);
//
//            challengeService.addTeamMembersToProject(projectForm, body, projectId);
//            challengeService.deleteTeamMembersToProject(projectForm);
            return ok(editConfirmation.render(tajobId, Long.parseLong("0"), "TAJob"));//TODO: 0 is entryId
        } catch (Exception e) {
            Logger.debug("JobController TA job edit POST exception: " + e.toString());
            return ok(editError.render("TAJob"));
        }

    }

    /************************************************** End of TAJob Edit **********************************************/

    /************************************************** TAJob List *****************************************************/

    /**
     * This method intends to prepare data for all TA jobs.
     *
     * @patam pageNum
     * @patam sortCriteria: sortCriteria on some fields. Could be empty if not specified at the first time.
     * @return: data for jobList.scala.html
     */
    @With(OperationLoggingAction.class)
    public Result tajobList(Integer pageNum, String sortCriteria) {
        checkLoginStatus();

        // If session contains the current projectId, set it.

        // Set the offset and pageLimit.
        int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
        try {
            JsonNode tajobListJsonNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.TAJOB_LIST + session("id") + "?pageNum=" +
                            pageNum + "&pageLimit=" + pageLimit + "&sortCriteria=" + sortCriteria));
            return tajobService.renderTAJobListPage(tajobListJsonNode,
                    pageLimit, null, "all", session("username"),
                    Long.parseLong(session("id")));
        } catch (Exception e) {
            Logger.debug("TAJobController.tajobList() exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }


    /**
     * This method intends to prepare data for all TA jobs.
     *
     * @patam pageNum
     * @patam sortCriteria: sortCriteria on some fields. Could be empty if not specified at the first time.
     * @return: data for jobList.scala.html
     */
    @With(OperationLoggingAction.class)
    public Result tajobListPostedByUser(Integer pageNum) {
        String userId = session("id");
        try{
            JsonNode tajobsNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config, Constants.TAJOB_POSTED_BY_USER + userId));
            List<TAJob> tajobs = new ArrayList<TAJob>();
//            if (tajobsNode.isNull() || tajobsNode.has("error") || !tajobsNode.isArray()) {
//
//                return ok(jobList.render(tajobs, (int) pageNum,
//                        0, tajobsNode.size(), 0, "search", 20,
//                        Long.parseLong(session("id")), 0, 0));
//            }
            int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
            int startIndex = ((int) pageNum - 1) * pageLimit;
            int endIndex = ((int) pageNum - 1) * pageLimit + pageLimit - 1;
            // Handle the last page, where there might be less than 50 item
            if (pageNum == (tajobsNode.size() - 1) / pageLimit + 1) {
                // minus 1 for endIndex for preventing ArrayOutOfBoundException
                endIndex = tajobsNode.size() - 1;
            }
            int count = endIndex - startIndex + 1;
            tajobs = TAJob.deserializeJsonToTAJobList(tajobsNode, startIndex, endIndex);
            int beginIndexPagination = beginIndexForPagination(pageLimit, tajobsNode.size(), (int) pageNum);
            int endIndexPagination = endIndexForPagination(pageLimit, tajobsNode.size(), (int) pageNum);
            return ok(tajobListPostedByUser.render(tajobs,
                    (int) pageNum,
                    startIndex,
                    tajobsNode.size(),
                    count,
                    pageLimit, Long.parseLong(session("id")), beginIndexPagination, endIndexPagination));
        }catch(Exception e){
            Logger.debug("JobController.jobPostedByUser() exception: " + e.toString());
            e.printStackTrace();
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }
    /************************************************** End of TAJob List **********************************************/

    /************************************************** TAJob Detail ***************************************************/
    /**
     * Ths method intends to return details of an TA job. If an TA job is not found, return to the all job page (page 1?).
     *
     * @patam tajobId: TA job id
     * @return: TAJob, a list of TA jobs to jobDetail.scala.html
     */
    @With(OperationLoggingAction.class)
    public Result tajobDetail(Long tajobId) {
        try {
            TAJob tajob = tajobService.getTAJobById(tajobId);


            if (tajob == null) {
                Logger.debug("TAJobController.tajobDetail() get null from backend");
                Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
                return ok(generalError.render());
            }
            accessTimesService.AddOneTime("tajob", tajobId);
            return ok(tajobDetail.render(tajob));
        } catch (Exception e) {
            Logger.debug("TAJobController.tajobDetail() exception: " + e.toString());
            e.printStackTrace();
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }

    /**
     * Ths method intends to return details of an RA job application. If an RA job application is not found, return to the all job application page (page 1?).
     *
     * @param tajobApplicationId: RA job application id
     * @return: RAJobApplication, a list of RA jobs application to rajobApplicationDetail.scala.html
     */
    @With(OperationLoggingAction.class)
    public Result tajobApplicationDetail(Long tajobApplicationId) {
        try {
            TAJobApplication tajobApplication = tajobApplicationService.getTAJobApplicationById(tajobApplicationId);

            if (tajobApplication == null) {
                Logger.debug("TAJobController.tajobApplicationDetail() get null from backend");
                Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
                return ok(generalError.render());
            }
            accessTimesService.AddOneTime("tajobApplication", tajobApplicationId);
            return ok(tajobApplicationDetail.render(tajobApplication));
        } catch (Exception e) {
            Logger.debug("TAJobController.rajobApplicationDetail() exception: " + e.toString());
            e.printStackTrace();
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }

    @With(OperationLoggingAction.class)
    public Result tajobApplicationExportPDF(Long tajobApplicationId) {
        try {
            TAJobApplication tajobApplication = tajobApplicationService.getTAJobApplicationById(tajobApplicationId);

            if (tajobApplication == null) {
                Logger.debug("TAJobController.tajobApplicationDetail() get null from backend");
                Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
                return ok(generalError.render());
            }

            return ok(tajobApplicationDetail.render(tajobApplication));
        } catch (Exception e) {
            Logger.debug("TAJobController.rajobApplicationDetail() exception: " + e.toString());
            e.printStackTrace();
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }
    /************************************************** End of TAJob Detail ********************************************/

    /************************************************** TA Job Apply **************************************************/
    /**
     * This method intends to prepare to edit a job.
     *
     * @patam tajobId: job id
     * @return
     */
    @With(OperationLoggingAction.class)
    public Result tajobApplyPage(Long tajobId) {
        try {
            TAJob tajob = tajobService.getTAJobById(tajobId);
            if (tajob == null) {
                Logger.debug("TAJobController.tajobApplyPage exception: cannot get tajob by id");
                Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
                return ok(generalError.render());
            }
            System.out.println("Apply page job info: "+ tajob);
//            JsonNode teamMembersNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
//                    Constants.GET_TEAM_MEMBERS_BY_PROJECT_ID + jobId));
//            job.setTeamMembers(Common.deserializeJsonToList(teamMembersNode, User.class));


            return ok(tajobSMUApplication.render(tajob));
        } catch (Exception e) {
            Logger.debug("TAJobController.tajobApplyPage() exception: " + e.toString());
            Application.flashMsg(RESTfulCalls.createUserResponse(RESTfulCalls.UserResponseType.GENERALERROR));
            return ok(generalError.render());
        }
    }

    /**
     * This method intends to save TA job as a PDF file.
     *
     * @patam tajobId: job id
     * @return
     */
    public Result saveTAJobPdf(Long tajobApplicationId) {
        checkLoginStatus();

        // input file path
        String src = "./SMU_CS_NoCourseSelect_TA_application.pdf";
        // output file path
        String dest = "./new_test_pdf.pdf";

        // declare pdf variables
        PdfReader reader;
        PdfDocument pdf;
        PdfAcroForm form;
        PdfPage page;

        System.out.println("current_project_ path: " + System.getProperty("user.dir"));
        try {
            TAJobApplication app = tajobApplicationService.getTAJobApplicationById(tajobApplicationId);
            TAJob job = app.getAppliedTAJob();
            User user = app.getApplicant();

            // initialize pdf variables
            reader = new PdfReader(src);
            pdf = new PdfDocument(reader, new PdfWriter(dest));
            form = PdfAcroForm.getAcroForm(pdf, true);
            page = pdf.getFirstPage();

            Map<String, PdfFormField> fields = form.getFormFields();

            // BaseFont bf = BaseFont.createFont("Font/SIMYOU.TTF", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
            // form.addSubstitutionFont(bf);
            // 6 get info================================================

            Map<String, Object> data = new HashMap<>();

            // page 1
            page = pdf.getPage(1);

            // Metadata: Date, Last Name, First Name, SMU ID, Email, Cell Number, Semester(Fall, Spring, Summer), Student Type(New, Continuing)
            data.put("Todays date", app.getCreatedTime());
            data.put("Last Family Name", user.getLastName());
            data.put("First Name", user.getFirstName());
            data.put("SMU ID", app.getSmuID());
            data.put("Email", user.getEmail());
            data.put("Cell Phone Number", user.getPhoneNumber());
            data.put("SemesterApplied", "Choice" + job.getTaJobSemesterTypes());
            data.put("Student Type", (app.getTaStudentTypes() == 1) ? "New Student" : "Continuing Student");

            // Schooling Details: application for admission, V, Q, A, GRE Date, Undergraduate GPA, School, Graduate GPA, School_2, Class Rank, Score Percentage, Grade, Others
            data.put("If new student have you submitted a completed application for admission to the Lyle School of Engineering", (app.getTaStudentAdmissionTypes() == 1) ? "Yes" : "No");
            data.put("V", app.getGreV());
            data.put("Q", app.getGreQ());
            data.put("A", app.getGreA());
            data.put("GRE Date", app.getGreDate());
            data.put("Undergraduate GPA", app.getUndergraduateGPA());
            data.put("School", app.getUndergraduateSchool());
            data.put("Graduate GPA", app.getGraduateGPA());
            data.put("School_2", app.getGraduateSchool());
            data.put("What was your class rank Example 2 out of 50", app.getClassRankNoGPA());
            data.put("What was your score percentage in your year of graduation Example 90", app.getScorePercentageNoGPA());
            data.put("What was your grade in the year of graduation Example Distinction with Honor", app.getGradeNoGPA());
            data.put("Others", app.getOtherInfoNoGPA());

            // page 2
            page = pdf.getPage(2);

            // Degree Info: Enrolled Degree, I plan to continue in PhD CS program, undefined_4, 1, 2, 3, 4
            int enrolledDegree = app.getEnrolledDegree();
            String degree = "";
            switch (enrolledDegree) {
                case 1: degree = "MS CS";       break;
                case 2: degree = "MS Cyber";    break;
                case 3: degree = "MS SE";       break;
                case 4: degree = "DE SE";       break;
                case 5: degree = "PhD CS";      break;
                default: degree = "Other";
            }
            data.put("Degree Program", degree);
            data.put("I plan to continue in PhD CS program", (app.getEnrolledPhdDegree() == 1) ? "Yes_2" : "No_2");
            data.put("undefined_4", "");
            data.put("1", app.getAreasResearchInterest1());
            data.put("2", app.getAreasResearchInterest2());
            data.put("3", app.getAreasResearchInterest3());
            data.put("4", app.getAreasResearchInterest4());

            // RA: Research Assistant, If yes when, Faculty Research Advisor, Email Address
            data.put("Have you ever been a Research Assistant at SMU", (app.getRaSMU() == 1) ? "Yes_3" : "No_3");
            data.put("If yes when", app.getRaSMUTime());
            data.put("Name of Faculty Research Advisor", app.getRaSMUAdvisorName());
            data.put("Advisors Email address", app.getRaSMUAdvisorEmail());

            // TA: Teacher's Assistant, If yes when, TA Supervisor, Email Address
            data.put("Have you ever been a Teaching Assistant at SMU", (app.getTaSMU() == 1) ? "Yes_4" : "No_4");
            data.put("If yes when_2", app.getTaSMUTime());
            data.put("Name of TA Supervisor", app.getTaSMUAdvisorName());
            data.put("Supervisors Email address", app.getTaSMUAdvisorEmail());

            // Qualifications: Assembler Type, Computer Systems, Cpp, Java, Python, R, SQL, Javascript, Verilog, Assembler
            data.put("C++", "Choice" + app.getProgrammingLanguageCpp());
            data.put("Java", "Choice" + app.getProgrammingLanguageJava());
            data.put("Python", "Choice" + app.getProgrammingLanguagePython());
            data.put("R", "Choice" + app.getProgrammingLanguageR());
            data.put("SQL", "Choice" + app.getProgrammingLanguageSQL());
            data.put("Javascript", "Choice" + app.getProgrammingLanguageJavascript());
            data.put("Verilog", "Choice" + app.getProgrammingLanguageVerilog());
            data.put("Assembler", "Choice" + app.getProgrammingLanguageAssembler());
            data.put("Assembler List Type ARM MIPS etc", app.getProgrammingLanguageAssemblerType());
            data.put("Computer Systems List Type Linux MAC OS Windows etc", app.getComputerSystemsType());

            // Courses: Course 1, Course 2, Course 3
            data.put("Course Title", app.getPreviousTeachingExp1Title());
            data.put("Where", app.getPreviousTeachingExp1Where());
            data.put("Date", app.getPreviousTeachingExp1Date());
            data.put("Course Title_2", app.getPreviousTeachingExp2Title());
            data.put("Where_2", app.getPreviousTeachingExp2Where());
            data.put("Date_2", app.getPreviousTeachingExp2Date());
            data.put("Course Title_3", app.getPreviousTeachingExp3Title());
            data.put("Where_3", app.getPreviousTeachingExp3Where());
            data.put("Date_3", app.getPreviousTeachingExp3Date());

            // Working Policies: US Citizen, English Speaker, English Proficiency, Duolingo, Score, Date_4
            data.put("US citizen", (app.getTaUSCitizen() == 1) ? "Yes_5" : "No_5");
            data.put("Speak English", (app.getTaNativeLanguage() == 1) ? "Yes_6" : "No_6");
            data.put("Have you taken an English proficiency test", (app.getTaEnglishProficiencyTest() == 1) ? "Yes_7" : "No_7");
            data.put("If yes what test did you take eg TOEFL IELTS Duolingo", app.getTaEnglishProficiencyTestName());
            data.put("Score", app.getTaEnglishProficiencyTestScore());
            data.put("Date_4", app.getTaEnglishProficiencyTestDate());

            // page 3
            page = pdf.getPage(3);

            // initialize list of possible courses
            String[] getCourses = job.getTaCoursesSelectionHidden().split(";");
            System.out.println(job.getTaCoursesSelectionHidden());

            SortedMap<String, Object> allCourses = new TreeMap<>();
            for (int i = 0; i < getCourses.length; i++) {
                allCourses.put(getCourses[i].trim(), "");
            }

            // clean up preferred courses
            String[] prefCourses = app.getTaCoursesPreferenceHidden().split(";");
            for (int i = 0 ; i < prefCourses.length; i++) {
                prefCourses[i] = prefCourses[i].trim();
                allCourses.replace(prefCourses[i], "On");
            }

            // clean up not preferred courses
            String[] noPrefCourses = app.getTaCoursesNotPreferenceHidden().split(";");
            for (int i = 0 ; i < noPrefCourses.length; i++) {
                noPrefCourses[i] = noPrefCourses[i].trim();
                allCourses.replace(noPrefCourses[i], "Off");
            }

            float x = 40;
            float y = page.getPageSize().getHeight()/2;

            int fontSize = 11;
            String font = StandardFonts.TIMES_ROMAN;
            String fontBold = StandardFonts.TIMES_BOLD;

            // pref title
            paint(page, x, y, fontBold, fontSize, "Courses listed below are all CS courses that need Teaching Assistants.");
            y -= 30;
            paint(page, x, y, fontBold, fontSize, "Which courses would you like to TA?");
            y -= 15;
            for (String key : allCourses.keySet()) {
                PdfButtonFormField check = PdfFormField.createCheckBox(
                        pdf,
                        new Rectangle(x + 40, y,10, 10),
                        key,
                        (allCourses.get(key) == "On") ? "On" : "Off",
                        PdfFormField.TYPE_CHECK
                );
                check.setBorderColor(ColorConstants.BLACK);
                check.setBorderWidth(1);
                form.addField(check, page);
                paint(page, x + 60, y, font, fontSize, key);
                y -= 15;
                if (y < 40) {
                    pdf.addNewPage(4, new PageSize(page.getPageSize()));
                    page = pdf.getPage(4);
                    y = page.getPageSize().getHeight() - 40;
                }
            }
            paint(page, x, y, fontBold, fontSize, "Which courses would you not mind TA-ing?");
            y -= 15;
            for (String key : allCourses.keySet()) {
                PdfButtonFormField check = PdfFormField.createCheckBox(
                        pdf,
                        new Rectangle(x + 40, y,10, 10),
                        key + "_2",
                        (allCourses.get(key) == "Off") ? "On" : "Off",
                        PdfFormField.TYPE_CHECK
                );
                check.setBorderColor(ColorConstants.BLACK);
                check.setBorderWidth(1);
                form.addField(check, page);
                paint(page, x + 60, y, font, fontSize, key);
                y -= 15;
                if (y < 40) {
                    pdf.addNewPage(5, new PageSize(page.getPageSize()));
                    page = pdf.getPage(5);
                    y = page.getPageSize().getHeight() - 40;
                }
            }

            // page 4

            // resume
            data.put("You may attach a resume to this application", app.getApplyCoverLetter());

            // 7 iterate form info and fill the pdf form
            for (String key : data.keySet()) {
                if (form.getField(key) == null)
                    System.out.println("error: " + key);
                else
                    form.getField(key).setValue(data.get(key).toString());
            }

            pdf.close();

            System.out.println("===============PDF export successfully=============");

            tajobApplicationService.savePDFToJob(new File(dest), tajobApplicationId);

            return ok(new FileInputStream(dest)).as("application/pdf");
        } catch (Exception e) {
            Logger.debug("tajobController TA job export PDF exception: " + e.toString());
            return ok(editError.render("TAJob"));
        } finally {
            try {
                //reader.close();
            } catch (Exception e) {
                e.printStackTrace();
                Logger.debug("jobController job APPLY POST exception: " + e.toString());
                return ok(editError.render("Job"));
            }
        }
    }

    public void paint(PdfPage page, double x, double y, String font, int fontSize, String text) throws IOException {
        PdfCanvas canvas = new PdfCanvas(page);
        canvas.beginText().setFontAndSize(
                PdfFontFactory.createFont(font), fontSize)
                .moveText(x, y)
                .showText(text)
                .endText();
    }

    /**
     * This method intends to submit the edit in the job edit page.
     *
     * @patam tajobId job id
     * @return
     */
    public Result tajobApplyPOST(Long tajobId) {
        checkLoginStatus();

        try {

            Form<TAJobApplication> tajobApplicationForm = tajobApplicationFormTemplate.bindFromRequest();

            Http.MultipartFormData body = request().body().asMultipartFormData();
            ObjectMapper mapper = new ObjectMapper();

            ObjectNode jsonData = tajobApplicationService.serializeFormToJson(tajobApplicationForm, tajobId);



            System.out.println("send apply post jsonData +-+: " + jsonData.toString());
            System.out.println("send apply post requ +-+: " + Constants.TAJOB_APPLY_POST + tajobId);
            JsonNode response = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.TAJOB_APPLY_POST + tajobId), jsonData);

            System.out.println("send edit post request: " + response.toString());
            if (response == null || response.has("error")) {
                Logger.debug("Cannot Apply the job");
                return redirect(routes.TAJobController.tajobDetail(tajobId));
            }

            return ok(editConfirmation.render(tajobId, Long.parseLong("0"), "Tajob"));//TODO: 0 is entryId
        } catch (Exception e) {
            Logger.debug("tajobController tajob APPLY POST exception: " + e.toString());
            return ok(editError.render("TAJob"));
        }

    }

    /**
     * This method intends to change ta job status.
     *
     * @param tajobId job id
     * @param tajobStatus: open, pending, close
     * @return
     */
    public Result tajobStatueChange(Long tajobId, String tajobStatus){
        checkLoginStatus();
        try {
            System.out.println(tajobStatus);
            ObjectNode jsonData = JsonNodeFactory.instance.objectNode();
            jsonData.put("status", tajobStatus);
            System.out.println("ta Job id:"+ tajobId + " jsonData: " + jsonData.toString());
            JsonNode response = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.TAJOB_STATUS_UPDATE + tajobId), jsonData);

            if (response == null || response.has("error")) {
                Logger.debug("Cannot change status of this ta job");
                return redirect(routes.TAJobController.tajobList(1, ""));
            }

            return ok(editConfirmation.render(tajobId, Long.parseLong("0"), "TajobOffer"));//TODO: 0 is entryId
        } catch (Exception e) {
            Logger.debug("tajobController ta job status update exception: " + e.toString());
            return ok(editError.render("TAJob"));
        }
    }

    /************************************************** End of TA Job Apply *******************************************/

    /**
     * This method intends to render an empty search.scala.html page
     *
     * @return
     */
    public Result searchPage() {
        checkLoginStatus();

        return ok(jobSearch.render());
    }

    /**
     * This method intends to prepare data for rending TA job research result page
     *
     * @patam pageNum
     * @return: data prepared for tajobList.scala.html (same as show all job list page)
     */
    public Result searchPOST(Integer pageNum, String sortCriteria) {
        checkLoginStatus();

        try {
            Form<TAJob> tmpForm = tajobFormTemplate.bindFromRequest();
            Map<String, String> tmpMap = tmpForm.data();

            JsonNode searchJson = Json.toJson(tmpMap);
            String searchString = "";

            // if not coming from the search input page, then fetch searchJson from the form from key "searchString"
            if (tmpMap.get("searchString") != null) {
                searchString = tmpMap.get("searchString");
                searchJson = Json.parse(searchString);
            } else {
                searchString = Json.stringify(searchJson);
            }

            List<TAJob> tajobs = new ArrayList<TAJob>();
            JsonNode tajobsNode = null;

            tajobsNode = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.GET_TAJOBS_BY_CONDITION), searchJson);
            if (tajobsNode.isNull() || tajobsNode.has("error") || !tajobsNode.isArray()) {

                return ok(tajobList.render(tajobs, (int) pageNum, sortCriteria,
                        0, tajobsNode.size(), 0, "search", 20, searchString,
                        Long.parseLong(session("id")), 0, 0));
            }
            int pageLimit = Integer.parseInt(Constants.PAGINATION_NUMBER_ITEM_TWENTY);
            int startIndex = ((int) pageNum - 1) * pageLimit;
            int endIndex = ((int) pageNum - 1) * pageLimit + pageLimit - 1;
            // Handle the last page, where there might be less than 50 item
            if (pageNum == (tajobsNode.size() - 1) / pageLimit + 1) {
                // minus 1 for endIndex for preventing ArtayOutOfBoundException
                endIndex = tajobsNode.size() - 1;
            }
            int count = endIndex - startIndex + 1;

            tajobs = TAJob.deserializeJsonToTAJobList(tajobsNode, startIndex, endIndex);
            int beginIndexPagination = beginIndexForPagination(pageLimit, tajobsNode.size(), (int) pageNum);
            int endIndexPagination = endIndexForPagination(pageLimit, tajobsNode.size(), (int) pageNum);

            return ok(tajobList.render(tajobs,
                    (int) pageNum,
                    sortCriteria,
                    startIndex,
                    tajobsNode.size(),
                    count,
                    "search",
                    pageLimit,
                    searchString,
                    Long.parseLong(session("id")),
                    beginIndexPagination,
                    endIndexPagination));
        } catch (Exception e) {
            Logger.debug("TAJobController.searchPOST() exception: " + e.toString());
            return redirect(routes.Application.home());
        }
    }


/*************************************** Private Methods **************************************************************/

    /**
     * This method intends to inactivate the TA job by calling the backend
     *
     * @patam tajobId
     * @return redirect to the job list page
     */
    public Result deleteTAJob(long tajobId) {
        checkLoginStatus();


        try {
            JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.DELETE_TAJOB_BY_ID + tajobId));
            //Todo We have to decide what to do if for some reason the TA job could not get deactivated???
            return redirect(routes.TAJobController.tajobList(1, ""));
        } catch (Exception e) {
            Logger.debug("TAJobController TA job delete exception: " + e.toString());
            return redirect(routes.TAJobController.tajobList(1, ""));
        }
    }

    public Result isTAJobNameExisted() {
        JsonNode json = request().body().asJson();
        String title = json.path("title").asText();

        ObjectNode jsonData = Json.newObject();
        JsonNode response = null;

        try {
            jsonData.put("title", title);
            response = RESTfulCalls.postAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.CHECK_TAJOB_NAME), jsonData);
            Application.flashMsg(response);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            Application.flashMsg(RESTfulCalls
                    .createResponse(RESTfulCalls.ResponseType.CONVERSIONERROR));
        } catch (Exception e) {
            e.printStackTrace();
            Application.flashMsg(RESTfulCalls.createResponse(RESTfulCalls.ResponseType.UNKNOWN));
        }
        return ok(response);
    }


    /*********************************** END Basic refactoring ********************************************************/

//    /**
//     *
//     * @return
//     */
//    public Result getJobLists() {
//        checkLoginStatus();
//        ArtayNode jobList = Json.newArtay();
//        JsonNode jobsNode = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
//                Constants.GET_ALL_ACTIVE_JOB));
//        // if no value is returned or error or is not json artay
//
//        ObjectMapper mapper = new ObjectMapper();
//        // parse the json string into object
//        for (int i = 0; i < jobsNode.size(); i++) {
//            JsonNode json = jobsNode.path(i);
//            ObjectNode jsonData = mapper.createObjectNode();
//            jsonData.put("id", json.findPath("id").asLong());
//            jsonData.put("text", json.findPath("title").asText());
//            jobList.add(jsonData);
//        }
//
//        return ok(jobList);
//    }

    /**
     * @patam id
     * @return
     */
    public Result isTAJobExist(String id) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        if (pattern.matcher(id).matches()) {
            JsonNode response = RESTfulCalls.getAPI(RESTfulCalls.getBackendAPIUrl(config,
                    Constants.GET_TAJOB_BY_ID + Long.parseLong(id)));
            if (response == null || response.has("error")) {
                return badRequest("cannot find TA job");
            } else return ok(response.findPath("id"));
        } else return badRequest("cannot find TA job");
    }




}