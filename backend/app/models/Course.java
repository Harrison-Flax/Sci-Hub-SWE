
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

/**
 * @author LUO, QIUYU
 * @version 1.0
 */
@Entity
@Getter
@Setter
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Course.class)
@ToString

public class Course extends Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private boolean isActive = true;

    
    private String courseId;

    
    private String name;

    
    private String description;

    
    private String prerequisite;

    
    private String start_semester;

    
    private String start_year;

    
    private String end_semester;

    
    private String end_year;

    @OneToMany(mappedBy = "course")
    private List<CourseTAAssignment> assignments = new ArrayList<>();



    public Course() {
    }


    /****************** End of Constructors ***************************************************************************/


    public static Finder<Long, Course> find = new Finder<Long, Course>(Course.class);



    /****************** Tool Functions ***************************************************************************/

    public int computeTotalApprovedHours() {
        int approvedHours = 0;
        List<CourseTAAssignment> assignments = this.getAssignments();
        for (CourseTAAssignment assignment : assignments) {
            if (assignment.getCourse().isActive() && assignment.getSemester().equals("spring") && assignment.getYear().equals("2024")) {
                approvedHours += assignment.getApprovedHours();
            }
        }
        return approvedHours;
    }

    public int computeTotalUsedHoursByWeek(int week) {
        int totalUsedHours = 0;
        List<CourseTAAssignment> assignments = this.getAssignments();
        for (CourseTAAssignment assignment : assignments) {
            totalUsedHours += assignment.computeUsedHoursByWeek(week);
        }
        return totalUsedHours;
    }

}