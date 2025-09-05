package models;
import com.fasterxml.jackson.annotation.*;
import io.ebean.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Challenge.class)
@ToString

public class Challenge extends Model{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String challengeTitle; // has to be unique
    private String shortDescription;
    private String longDescription;
    private String requiredExpertise;
    private String preferredExpertise;
    private String preferredTime;
    private String tech;
    private String time;
    private String budget;
    private String location;
    private String organizations;
    private String status;
    private String createTime;
    private String updateTime;
    private int minBudget;
    private int maxBudget;

    private String challengePdf;

    private String challengeImage;

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

    @ManyToOne
    @JoinColumn(name = "challenge_publisher_id", referencedColumnName = "id")
    private User challengePublisher;



    private int numberOfApplicants;
    //TODO: TO remove
    /****************** Constructors **********************************************************************************/

    public Challenge() {
    }

    public int getNumberOfApplicants() {
        return numberOfApplicants;
    }

    public void setNumberOfApplicants(int numberOfApplicants) {
        this.numberOfApplicants = numberOfApplicants;
    }
    public Challenge(long challengeId) {
        this.id = challengeId;
    }
    public Challenge(String challengeTitle) {
        this.challengeTitle = challengeTitle;
    }

    /****************** End of Constructors ***************************************************************************/


    public static Finder<Long, Challenge> find =
            new Finder<Long, Challenge>(Challenge.class);


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

