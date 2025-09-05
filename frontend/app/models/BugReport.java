package models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import play.Logger;
import play.libs.Json;
import play.data.validation.Constraints;
import utils.TextUtils;

import java.util.Date;

@Getter
@Setter
@ToString
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = BugReport.class)

public class BugReport {

    private long id;

    @Constraints.Required
    private String title;

    @Constraints.Required
    private String description;

    private String longDescription;
    private int solved = 0;
    private Date createTime;
    private Date solveTime;

    private User reporter;
    private User fixer;

    /*********************************************** Constuctors *******************************************************/
    public BugReport() {
    }

    /*********************************************** Utility methods ***************************************************/
    public String getTruncDescription() {
        int maxChar = 80;
        return TextUtils.truncateString(maxChar, getDescription());
    }

    public static BugReport deserialize(JsonNode json) throws Exception {
        try {
            BugReport bugReport = Json.fromJson(json, BugReport.class);

            return bugReport;
        } catch (Exception e) {
            Logger.debug("BugReport.deserialize() exception: " + e.toString());
            throw e;
        }
    }


    /*********************************************** Getters & Setters *************************************************/

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public int getSolved() {
        return solved;
    }

    public void setSolved(int solved) {
        this.solved = solved;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getSolveTime() {
        return solveTime;
    }

    public void setSolveTime(Date solveTime) {
        this.solveTime = solveTime;
    }

    public User getReporter() {
        return reporter;
    }

    public void setReporter(User reporter) {
        this.reporter = reporter;
    }

    public User getFixer() {
        return fixer;
    }

    public void setFixer(User fixer) {
        this.fixer = fixer;
    }
}