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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LUO, QIUYU
 * @version 1.0
 */
@Entity
@Getter
@Setter
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = CourseTAAssignment.class)
@ToString
public class CourseTAAssignment extends Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "course_id", referencedColumnName = "id")
    private Course course;

    @ManyToOne
    @JoinColumn(name = "ta_id", referencedColumnName = "id")
    private TACandidate taCandidate;

    
    private String semester = "spring";

    
    private String year = "2024";

    @OneToMany(mappedBy = "courseTAAssignment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TAWeeklyHours> usedHours = new ArrayList<>();

    
    private int approvedHours;

    
    private String f1Approved;


    public CourseTAAssignment() {
    }


    /****************** End of Constructors ***************************************************************************/


    public static Finder<Long, CourseTAAssignment> find = new Finder<Long, CourseTAAssignment>(CourseTAAssignment.class);


    /****************** Tool Functions ***************************************************************************/

    public int computeUsedHoursByWeek(int week) {
        int usedHours = 0;
        List<TAWeeklyHours> weeklyHours = this.getUsedHours();
        for (TAWeeklyHours weeklyHour: weeklyHours) {
            if (weeklyHour.getWeek() == week) {
                usedHours += weeklyHour.getHours();
            }
        }
        return usedHours;
    }

    public boolean isApproved(int week) {
        List<TAWeeklyHours> weeklyHours = this.getUsedHours();
        for (TAWeeklyHours weeklyHour: weeklyHours) {
            if (weeklyHour.getWeek() == week) {
                if (weeklyHour.isApproval()) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    public void approveWeek(int week) {
        List<TAWeeklyHours> weeklyHours = this.getUsedHours();
        for (TAWeeklyHours weeklyHour: weeklyHours) {
            if (weeklyHour.getWeek() == week) {
                weeklyHour.setApproval(true);
            }
        }
    }


}