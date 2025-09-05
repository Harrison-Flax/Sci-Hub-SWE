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
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Job.class)
@ToString
public class TAJob extends Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String isActive;
    private String pdf;

    private String status;

    private int workTime;

    
    private int taJobSemesterTypes;

    
    private int taJobCourseSelections;

    
    private String taCoursesSelectionHidden = "";

    
    private String title = "";

    
    private String goals = "";

    
    private int minSalary;

    
    private int maxSalary;

    
    private int taTypes;

    
    private String shortDescription = "";

    
    private String longDescription = "";

    
    private String fields = "";

    
    private String publishDate = "";

    
    private String publishYear = "";
    
    private String publishMonth = "";

    
    private String imageURL = "";

    
    private String url = "";

    
    private String organization = "";

    
    private String location = "";

    
    private String requiredExpertise = "";

    
    private String preferredExpertise = "";

    
    private String numberOfPositions = "";

    
    private String expectedStartDate = "";

    
    private String expectedTimeDutation = "";

    @ManyToOne
    @JoinColumn(name = "tajob_publisher_id", referencedColumnName = "id")
    private User tajobPublisher;
    private int numberOfApplicants;


    /****************** Constructors **********************************************************************************/

    public TAJob() {
    }

    public TAJob(long tajobId) {
        this.id = tajobId;
    }

    /****************** End of Constructors ***************************************************************************/


    public static Finder<Long, TAJob> find = new Finder<Long, TAJob>(TAJob.class);

}

