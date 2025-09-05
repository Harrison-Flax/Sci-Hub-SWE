package models;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import io.ebean.Finder;
import io.ebean.Model;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import javax.persistence.*;

/**
 * @author LUO, QIUYU
 * @version 1.0
 * This table records the weekly used hours of a TA in a certain course(decided by courseTAAssignment).
 */
@Entity
@Getter
@Setter
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = TAWeeklyHours.class)
@ToString

public class TAWeeklyHours extends Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    
    private int week;

    
    private int hours;

    private boolean approval;

    @ManyToOne
    @JoinColumn(name = "assignment_id", referencedColumnName = "id")
    private CourseTAAssignment courseTAAssignment;


    public TAWeeklyHours() {
    }


    /****************** End of Constructors ***************************************************************************/


    public static Finder<Long, TAWeeklyHours> find = new Finder<Long, TAWeeklyHours>(TAWeeklyHours.class);


}