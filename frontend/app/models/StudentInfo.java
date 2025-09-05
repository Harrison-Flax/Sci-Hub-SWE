package models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import play.libs.Json;

import java.util.Date;

@Getter
@Setter
@ToString
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idNumber", scope = StudentInfo.class)
public class StudentInfo {
    private String idNumber;

    private String studentYear;

    private String studentType;

    private String major;

    private String firstEnrollDate;

    public static StudentInfo deserialize(JsonNode json) throws Exception {
        if (json == null) {
            throw new NullPointerException("User node should not be null to be serialized.");
        }
        StudentInfo studentInfo = Json.fromJson(json, StudentInfo.class);
        return studentInfo;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public String getMajor() {
        return major;
    }

    public String getStudentType() {
        return studentType;
    }

    public String getStudentYear() {
        return studentYear;
    }

    public String getFirstEnrollDate() {
        return firstEnrollDate;
    }
}
