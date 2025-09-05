package models;


import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import play.libs.Json;
import java.util.List;

@Getter
@Setter
@ToString
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Organization.class)
public class Organization {

    private long id;

    private String organizationName;

    private String address;

    private String organizationLogo;

    private int numberOfEmployees;

    private String organizationHistory;

    private String streetAddress1;
    private String streetAddress2;

    private String city;

    private String state;

    private int zipCode;

    private String shortDescription;

    private String longDescription;

    private String fields;

    private String contactPersonName;

    private String contactPersonEmail;

    private Long contactPersonPhone;
    private String focuses;

    private String URL;

    private Long registrarId;

    private List<User> UserPool;

    private List<Project> principalInvestigatedProjects;

    private List<Project> sponsoredProjects;


    /*********************************************** Constructors *****************************************************/
    public Organization() {
    }

    public Organization(long id) {
        this.id = id;
    }

    /*********************************************** Utility methods **************************************************/
    /**
     * Deserializes the json to a User.
     *
     * @param json the json to convert from.
     * @return the dataset object.
     * TODO: How to make sure all fields are checked???
     */
    public static Organization deserialize(JsonNode json) throws Exception {
        if (json == null) {
            throw new NullPointerException("User - Organization node should not be null to be serialized.");
        }
        Organization organization = Json.fromJson(json, Organization.class);
        return organization;
    }


    /*********************************************** Getters & Setters ************************************************/
    public Long getId(){ return id; }

    public void setId(long id) {
        this.id = id;
    }

    public String getOrganizationName() {
        return organizationName;
    }
    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getAddress(){ return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public void setFocuses(String focuses) {
        this.focuses = focuses;
    }

    public String getFocuses() {
        return focuses;
    }

    public String getURL(){
        return URL;
    }
    public void setURL(String URL) {
        this.URL = URL;
    }

    public Long getRegistrarId() {
        return registrarId;
    }

    public void setUserPool(List<User> userPool) {
        UserPool = userPool;
    }

    public String getOrganizationLogo() {
        return organizationLogo;
    }

    public void setOrganizationLogo(String organizationLogo) {
        this.organizationLogo = organizationLogo;
    }

    public int getNumberOfEmployees() {
        return numberOfEmployees;
    }

    public void setNumberOfEmployees(int numberOfEmployees) {
        this.numberOfEmployees = numberOfEmployees;
    }

    public String getOrganizationHistory() {
        return organizationHistory;
    }

    public void setOrganizationHistory(String organizationHistory) {
        this.organizationHistory = organizationHistory;
    }

    public String getStreetAddress1() {
        return streetAddress1;
    }

    public void setStreetAddress1(String streetAddress1) {
        this.streetAddress1 = streetAddress1;
    }

    public String getStreetAddress2() {
        return streetAddress2;
    }

    public void setStreetAddress2(String streetAddress2) {
        this.streetAddress2 = streetAddress2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getZipCode() {
        return zipCode;
    }

    public void setZipCode(int zipCode) {
        this.zipCode = zipCode;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public String getFields() {
        return fields;
    }

    public void setFields(String fields) {
        this.fields = fields;
    }

    public String getContactPersonName() {
        return contactPersonName;
    }

    public void setContactPersonName(String contactPersonName) {
        this.contactPersonName = contactPersonName;
    }

    public String getContactPersonEmail() {
        return contactPersonEmail;
    }

    public void setContactPersonEmail(String contactPersonEmail) {
        this.contactPersonEmail = contactPersonEmail;
    }

    public Long getContactPersonPhone() {
        return contactPersonPhone;
    }

    public void setContactPersonPhone(Long contactPersonPhone) {
        this.contactPersonPhone = contactPersonPhone;
    }
}
