package models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties({""})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "user", scope = StudentInfo.class)
@ToString
public class StudentInfo extends Model {
    private String idNumber;

    private String studentYear;

    private String studentType;

    private String major;

    private String firstEnrollDate;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    public StudentInfo(User user, String idNumber, String studentYear, String studentType, String major, String firstEnrollDate) {
        this.user = user;
        this.idNumber = idNumber;
        this.studentYear = studentYear;
        this.studentType = studentType;
        this.major = major;
        this.firstEnrollDate = firstEnrollDate;
    }

    public static Finder<Long, StudentInfo> find = new Finder<Long, StudentInfo>(StudentInfo.class);
}
