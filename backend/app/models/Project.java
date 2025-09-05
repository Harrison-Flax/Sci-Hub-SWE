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
import java.util.List;

@Entity
@Getter
@Setter
@ToString
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id",
        scope = Project.class)
@JsonIgnoreProperties({"apis", "notebooks", "datasetEntries", "dockers", "teamMembers"})

public class Project extends Model {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String isActive;
    private Long parentProjectId;
    private boolean isPopular;
    private long popularRanking;
    private String authentication;
    private long accessTimes;
    private Integer nextImageIndex;



    
    private String title;

    
    private String technology;

    
    private String pdf;

    
    private String imageUrl;

    
    private String goals;

    
    private String videoUrl;

    
    private String githubUrl;

    
    private String teamPageUrl;

    
    private String location;

    
    private String description;

    
    private String startDate;

    
    private String endDate;


    @ManyToOne
    @JoinColumn(name = "principal_investigator_id", referencedColumnName = "id")
    private User principalInvestigator;

    @ManyToOne
    @JoinColumn(name = "sponsor_contact_id", referencedColumnName = "id")
    private User sponsorContact;

    @ManyToOne
    @JoinColumn(name = "principal_investigator_organization_id", referencedColumnName = "id")
    private Organization principalInvestigatorOrganization;

    @ManyToOne
    @JoinColumn(name = "sponsor_organization_id", referencedColumnName = "id")
    private Organization sponsorOrganization;

    @ManyToMany(cascade = CascadeType.ALL, mappedBy = "participatedProjects")
    @JoinTable(name = "user_participation_project")
    private List<User> teamMembers;

    @ManyToMany(cascade = CascadeType.ALL, mappedBy = "participatedProjects")
    @JoinTable(name = "technology_usedin_project")
    private List<Technology> usedTechnologies;

    /******************************************** Constructors ********************************************************/

    public Project() {
    }

    public Project(long projectId) {
        this.id = projectId;
    }

    /************************************* End of Constructors ********************************************************/

    public static Finder<Long, Project> find =
            new Finder<Long, Project>(Project.class);

    /*************************************** Utility functions ********************************************************/


    /**
     * This method intends to deserialize JsonNode info and replace a given Project object.
     * @param node
     * @param user
     * @param project
     * @throws NullPointerException
     */


    /**
     * Turn project list into json array
     *
     * @param projectList list of projects
     * @return json array of serialized projects
     */
//    public static ArrayNode projectList2JsonArray(List<Project> projectList) {
//        ArrayNode projectsNode = Json.newArray();
//        for (Project project : projectList) {
//            ObjectNode projectNode = (ObjectNode) Json.toJson(project);
//            projectsNode.add(projectNode);
//        }
//        return projectsNode;
//    }

}

