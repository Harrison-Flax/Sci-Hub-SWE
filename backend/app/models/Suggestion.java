package models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.ebean.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import play.libs.Json;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Setter
@Getter
@ToString

public class Suggestion extends Model {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String title;
    private String description;
    private int solved = 0;
    private Date createTime;
    private Date solveTime;

    
    private String longDescription;

    @ManyToOne
    @JoinColumn(name = "reporter_id", referencedColumnName = "id")
    private User suggestionReporter;

    @ManyToOne
    @JoinColumn(name = "implementor_id", referencedColumnName = "id")
    private User suggestionImplementor;


    public Suggestion() {
    }

    public static Finder<Long, Suggestion> find = new Finder<Long, Suggestion>(Suggestion.class);

}
