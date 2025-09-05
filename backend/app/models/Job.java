package models;

import java.util.List;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.ProjectController;
import io.ebean.*;
import io.ebean.annotation.JsonIgnore;

import java.util.List;
import java.util.Set;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import play.Logger;
import services.ProjectService;
import utils.Constants;

@Entity
@Getter
@Setter
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Job.class)
@ToString
public class Job extends Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String isActive;
    private String pdf;
    private String jobtxt;
    private String status;

    
    private String title = "";

    
    private String goals = "";

    
    private int minSalary;

    
    private int maxSalary;

    
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

    @ManyToOne
    @JoinColumn(name = "job_publisher_id", referencedColumnName = "id")
    private User jobPublisher;


    
    private String contactPersonName = "";
    
    private String contactPersonEmail = "";
    
    private String contactPersonPhone = "";

    // could be extracted out
    
    private String salaryLow = "";
    
    private String salaryHigh = "";
    
    private String minimumDegree = "";
    
    private String minimumDegreeInFields = "";

    // full time or part-time
    
    private String type = "";

    private int numberOfApplicants = 0;

    private String updateTime;


    /****************** Constructors **********************************************************************************/

    public Job() {
    }

    public Job(long jobId) {
        this.id = jobId;
    }

    /****************** End of Constructors ***************************************************************************/


    public static Finder<Long, Job> find = new Finder<Long, Job>(Job.class);
    public int getNumberOfApplicants() {
        return numberOfApplicants;
    }

    public void setNumberOfApplicants(int numOfApplicants) {
        this.numberOfApplicants = numOfApplicants;
    }
}

