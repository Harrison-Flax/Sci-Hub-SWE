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
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = ChallengeApplication.class)
@ToString

public class ChallengeApplication extends Model{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id; // has to be unique

    @ManyToOne
    @JoinColumn(name = "challenge_id", referencedColumnName = "id")
    private Challenge appliedChallenge;

    @ManyToOne
    @JoinColumn(name = "applicant_id", referencedColumnName = "id")
    private User applicant;

    // challenge application info
    private String applyHeadline;
    private String applyCoverLetter;
    private String applyDescription;

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
    private String status;
    private String isActive;



    //TODO: TO remove
    /****************** Constructors **********************************************************************************/

    public ChallengeApplication() {
    }

    public ChallengeApplication(long Id) {
        this.id = Id;
    }
    public ChallengeApplication(String applyDescription) {
        this.applyDescription = applyDescription;
    }

    /****************** End of Constructors ***************************************************************************/


    public static Finder<Long, ChallengeApplication> find =
            new Finder<Long, ChallengeApplication>(ChallengeApplication.class);


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

