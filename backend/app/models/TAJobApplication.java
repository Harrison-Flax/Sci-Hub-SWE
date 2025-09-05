package models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import io.ebean.Finder;
import io.ebean.Model;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Getter
@Setter
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = TAJobApplication.class)
@ToString

public class TAJobApplication extends Model {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id; // has to be unique

    @ManyToOne
    @JoinColumn(name = "tajob_id", referencedColumnName = "id")
    private TAJob appliedTAJob;

    @ManyToOne
    @JoinColumn(name = "applicant_id", referencedColumnName = "id")
    private User applicant;

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


    private String applyHeadline;


    private String applyCoverLetter;

    // 3 referees info
//    private String referee1Title;
//    private String referee1LastName;

//    private String referee1FirstName;
//    private String referee1Email;
//    private String referee1Phone;
//
//    private String referee2Title;
//    private String referee2LastName;
//    private String referee2FirstName;
//    private String referee2Email;
//    private String referee2Phone;
//
//    private String referee3Title;
//    private String referee3LastName;
//    private String referee3FirstName;
//    private String referee3Email;
//    private String referee3Phone;


    // Roles:(multiple roles can be sepatated by semicolon)
    // Admin: admin
    // Superuser: superuser
    // Normal: normal
    // Guest: guest
    // Tester: tester
    // Other: other

    private double tating;
    private long tatingCount;
    private double recommendRating;
    private long recommendRatingCount;
    private String homepage;
    private String avatar;

    private String createdTime;
    private String isActive;


    //TODO: TO remove

    /****************** Constructors **********************************************************************************/

    public TAJobApplication() {
    }

    public TAJobApplication(long Id) {
        this.id = Id;
    }

    public TAJobApplication(String applyHeadline) {
        this.applyHeadline = applyHeadline;
    }

    /****************** End of Constructors ***************************************************************************/


    public static Finder<Long, TAJobApplication> find =
            new Finder<Long, TAJobApplication>(TAJobApplication.class);


    /****************** Utility functions *****************************************************************************/
    /**
     * Combine first name + middle initial + last name to get full name for author.
     *
     * @return
     * @patam firstName
     * @patam middleInitial
     * @patam lastName
     */
    public static String createAuthorName(String firstName, String middleInitial, String lastName) {
        StringBuffer authorName = new StringBuffer();
        authorName.append(firstName);
        authorName.append(" ");
        if (!middleInitial.equals("")) {
            authorName.append(middleInitial);
            authorName.append(" ");
        }
        authorName.append(lastName);
        return authorName.toString();
    }
    /****************** End of Utility functions **********************************************************************/

}

