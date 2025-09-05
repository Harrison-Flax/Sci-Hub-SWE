package models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.JsonNode;
import io.ebean.*;
import io.ebean.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;



import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.List;


@Entity
@Getter
@Setter
@AllArgsConstructor
@ToString
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id",
        scope = Organization.class)
public class Organization extends Model{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String organizationName;

    private String address;

    @Lob
    @Column(columnDefinition="LONGTEXT")
    private String focuses;

    private String URL;

    private String organizationLogo;

    private int numberOfEmployees;

    private String organizationHistory;

    
    private String streetAddress1;

    
    private String streetAddress2;

    private String city;

    private String state;

    
    private int zipCode;

    @Lob
    @Column(columnDefinition="LONGTEXT")
    private String shortDescription;

    @Lob
    @Column(columnDefinition="LONGTEXT")
    private String longDescription;

    @Lob
    @Column(columnDefinition="LONGTEXT")
    private String fields;

    private String contactPersonName;

    private String contactPersonEmail;

    private Long contactPersonPhone;
    private Long registrarId;


    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_organization")
    private List<User> userPool;

    @OneToMany(mappedBy = "principalInvestigatorOrganization", cascade = CascadeType.ALL)
    private List<Project> principalInvestigatedProjects;

    @OneToMany(mappedBy = "sponsorOrganization", cascade = CascadeType.ALL)
    private List<Project> sponsoredProjects;

    public static Finder<Long, Organization> find =
            new Finder<Long, Organization>(Organization.class);

    public Organization(String organizationName, String address, String focuses, String URL, Long registrarId) {
        this.organizationName = organizationName;
        this.address = address;
        this.focuses = focuses;
        this.URL = URL;
        this.registrarId = registrarId;
    }

    public Organization(String organizationName, String address, String focuses, String URL) {
        this.organizationName = organizationName;
        this.address = address;
        this.focuses = focuses;
        this.URL = URL;
    }

    public void deserializeFromJson(JsonNode json) throws Exception{
        if (json.path("organizationName") != null) this.setOrganizationName(json.path("organizationName").asText());
        if (json.path("address") != null) this.setAddress(json.path("address").asText());
        if (json.path("focuses") != null) this.setFocuses(json.path("focuses").asText());
        if (json.path("URL") != null) this.setURL(json.path("URL").asText());
        if (json.path("longDescription") != null) this.setLongDescription(json.path("longDescription").asText());
        if (json.path("zipCode") != null) this.setZipCode(Integer.parseInt(json.path("zipCode").asText()));
        if (json.path("city") != null) this.setCity(json.path("city").asText());
        if (json.path("streetAddress1") != null) this.setStreetAddress1(json.path("streetAddress1").asText());
        if (json.path("streetAddress2") != null) this.setStreetAddress2(json.path("streetAddress2").asText());
        if (json.path("shortDescription") != null) this.setShortDescription(json.path("shortDescription").asText());
        if (json.path("contactPersonName") != null) this.setContactPersonName(json.path("contactPersonName").asText());
        if (json.path("state") != null) this.setState(json.path("state").asText());
        if (json.path("organizationHistory") != null) this.setOrganizationHistory(json.path("organizationHistory").asText());
        if (json.path("focuses") != null) this.setFocuses(json.path("focuses").asText());
        if (json.path("fields") != null) this.setFields(json.path("fields").asText());
        if (json.path("contactPersonEmail") != null) this.setContactPersonEmail(json.path("contactPersonEmail").asText());

        if (!json.path("zipCode").asText().trim().isEmpty())
            this.setZipCode(json.path("zipCode").asInt());

        if (!json.path("contactPersonPhone").asText().trim().isEmpty())
            this.setContactPersonPhone(json.path("contactPersonPhone").asLong());

        if (!json.path("numberOfEmployees").asText().trim().isEmpty())
            this.setNumberOfEmployees(json.path("numberOfEmployees").asInt());
    }

}
