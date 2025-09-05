package models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import io.ebean.Finder;
import io.ebean.Model;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = TACandidate.class)
@ToString
/**
 * @author LUO, QIUYU
 * @version 1.0
 */


public class TACandidate extends Model {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private boolean isActive;

    private int isResumeSent;

    
    private String smuId = "";

    
    private String semester = "spring";

    
    private String year = "2024";

    
    private String status = "";

    
    private int hours;


    
    private String courses = "";

    @ManyToOne
    @JoinColumn(name = "ta_applicant_id", referencedColumnName = "id")
    private User applicant;


    
    private String preference = "";

    
    private String unwanted = "";

    @OneToMany(mappedBy = "taCandidate")
    private List<CourseTAAssignment> assignments = new ArrayList<>();

    
    private String comment = "";



    /****************** Constructors **********************************************************************************/

    public TACandidate() {
    }

    public TACandidate(long Id) {
        this.id = Id;
    }

    /****************** End of Constructors ***************************************************************************/


    public static Finder<Long, TACandidate> find = new Finder<Long, TACandidate>(TACandidate.class);


    public void setIsActive(boolean b) {
        this.isActive = b;
    }

    public boolean getIsActive() {
        return isActive;
    }

    /****************** Tool Functions ***************************************************************************/


}

