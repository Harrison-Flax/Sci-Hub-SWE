package models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import play.data.validation.Constraints;
import utils.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
@Setter
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Suggestion.class)

public class Suggestion {

    private long id;

    @Constraints.Required
    private String title;

    @Constraints.Required
    private String description;

    private String longDescription;
    private int solved = 0;
    private Date createTime;
    private Date solveTime;

    private User suggestionReporter;
    private User suggestionImplementor;

    /*********************************************** Constuctors *******************************************************/

    public Suggestion() {

    }


    /*********************************************** Utility methods ***************************************************/
    public String getTruncDescription() {
        int maxChar = 80;
        return TextUtils.truncateString(maxChar, getDescription());
    }

    public String getCreateTime() {
        if (createTime != null) {
            return (new SimpleDateFormat("MM/dd/yyyy").format(createTime));
        } else {
            return null;
        }
    }

    public String getSolveTime() {
        if (solveTime != null) {
            return (new SimpleDateFormat("MM/dd/yyyy").format(solveTime));
        } else {
            return null;
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


    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }


    public void setSolveTime(Date solveTime) {
        this.solveTime = solveTime;
    }

    public User getSuggestionReporter() {
        return suggestionReporter;
    }

    public void setSuggestionReporter(User suggestionReporter) {
        this.suggestionReporter = suggestionReporter;
    }

    public User getSuggestionImplementor() {
        return suggestionImplementor;
    }

    public void setSuggestionImplementor(User suggestionImplementor) {
        this.suggestionImplementor = suggestionImplementor;
    }
}