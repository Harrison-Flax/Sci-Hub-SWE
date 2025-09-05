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
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = JobApplication.class)
@ToString

public class JobApplication extends Model{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id; // has to be unique

    @ManyToOne
    @JoinColumn(name = "job_id", referencedColumnName = "id")
    private Job appliedJob;

    @ManyToOne
    @JoinColumn(name = "applicant_id", referencedColumnName = "id")
    private User applicant;

    
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

    private String createdTime;
    private String isActive;



    //TODO: TO remove
    /****************** Constructors **********************************************************************************/

    public JobApplication() {
    }

    public JobApplication(long Id) {
        this.id = Id;
    }
    public JobApplication(String applyCoverLetter) {
        this.applyCoverLetter = applyCoverLetter;
    }

    /****************** End of Constructors ***************************************************************************/


    public static Finder<Long, JobApplication> find =
            new Finder<Long, JobApplication>(JobApplication.class);


    /****************** Utility functions *****************************************************************************/
    /**
     * Combine first name + middle initial + last name to get full name for author.
     * @param firstName
     * @param middleInitial
     * @param lastName
     * @return
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

