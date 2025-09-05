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
public class RAJob extends Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String isActive;
    private String pdf;

    private String status;

    
    private String title = "";

    
    private String goals = "";

    
    private int minSalary;

    
    private int maxSalary;

    
    private int raTypes;

    
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

    
    private String expectedTimeDuration = "";

    @ManyToOne
    @JoinColumn(name = "rajob_publisher_id", referencedColumnName = "id")
    private User rajobPublisher;

    private int numberOfApplicants;

    private String createTime;

    private String updateTime;

    /****************** Constructors **********************************************************************************/

    public RAJob() {
    }

    public RAJob(long rajobId) {
        this.id = rajobId;
    }

    public int getNumberOfApplicants() {
        return numberOfApplicants;
    }

    public void setNumberOfApplicants(int numberOfApplicants) {
        this.numberOfApplicants = numberOfApplicants;
    }

    /****************** End of Constructors ***************************************************************************/


    public static Finder<Long, RAJob> find = new Finder<Long, RAJob>(RAJob.class);

}

