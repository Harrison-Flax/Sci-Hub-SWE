package models;
import com.fasterxml.jackson.annotation.*;
import io.ebean.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Author.class)
@ToString

public class Technology extends Model{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String technologyTitle; // has to be unique
    private String goals;
    private String shortDescription;
    private String longDescription;
    private String keywords;
    private String pIName;
    private String teamMembers;
    private String fields;
    private String organizations;
    private User   contactPerson;
    private String pdf;
    private String representativePaperURL;

    private double rating;
    private long ratingCount;
    private double recommendRating;
    private long recommendRatingCount;
    private String homepage;

    private String registeredTime;
    private String isActive;

    @ManyToOne
    @JoinColumn(name = "technology_publisher_id", referencedColumnName = "id")
    private User technologyPublisher;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "technology_usedin_project")
    private List<Project> participatedProjects;

    /****************** Constructors **********************************************************************************/

    public Technology() {
    }

    public Technology(long technologyId) {
        this.id = technologyId;
    }
    public Technology(String technologyTitle) {
        this.technologyTitle = technologyTitle;
    }

    /****************** End of Constructors ***************************************************************************/


    public static Finder<Long, Technology> find =
            new Finder<Long, Technology>(Technology.class);


    /****************** Utility functions *****************************************************************************/

    /****************** End of Utility functions **********************************************************************/

}

