package models;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.ProjectController;
import io.ebean.*;
import io.ebean.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import play.Logger;
import services.ProjectService;
import utils.Constants;

@Entity
@Getter
@Setter
@JsonIgnoreProperties({"project", "followers", "friendRequestSender", "friends", "createdProjects", "organizations"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = User.class)
@ToString
public class User extends Model {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private String userName;

	@JsonIgnore
	private String password;

	private String firstName;
	private String lastName;
	private String middleInitial;
	private String organization;
	private String email;
	private String mailingAddress;
	private String phoneNumber;
//	private String researchFields;
//	private String highestDegree;
	private String level; // user level, e.g., admin, normal
	private double rating;
	private long ratingCount;
	private double recommendRating;
	private long recommendRatingCount;
	private String homepage;
	private String avatar;

	// as a service provider. if a user participates in a wish, she will be considered a service provider, and can
	// be recommended in the future for wish creator etc.
	private boolean serviceProvider;
	private String expertises;
	private String categories;
	private String detail;
	private Integer userType;
	private long service_execution_counts;

	// as a service user (wish initiator)
	private boolean serviceUser;

	private String createTime;

	private String isActive;

	private String token;

	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private ResearcherInfo researcherInfo;

	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private StudentInfo studentInfo;


	@ManyToOne
	@JoinColumn(name = "project_zone_id", referencedColumnName = "id")
	private Project projectZone;

	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "user_participation_project")
	private List<Project> participatedProjects;

	@ManyToMany(cascade = CascadeType.ALL, mappedBy = "userPool")
	@JoinTable(name = "user_organization")
	private List<Organization> organizations;


	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "followers", joinColumns = { @JoinColumn(name = "userid", referencedColumnName = "id") },
			inverseJoinColumns = { @JoinColumn(name = "followerid", referencedColumnName = "id") })
	protected Set<User> followers;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "friendrequests", joinColumns = { @JoinColumn(name = "userid", referencedColumnName = "id") },
			inverseJoinColumns = { @JoinColumn(name = "senderid", referencedColumnName = "id") })
	protected Set<User> friendRequestSender;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "friendship", joinColumns = { @JoinColumn(name = "useraid", referencedColumnName = "id") },
			inverseJoinColumns = { @JoinColumn(name = "userbid", referencedColumnName = "id") })
	protected Set<User> friends;

	@OneToMany(mappedBy = "jobPublisher", cascade = CascadeType.ALL)
	private List<Job> publishedJobs;

	@OneToMany(mappedBy = "reporter", cascade = CascadeType.ALL)
	private List<BugReport> reportedBugReports;

	@OneToMany(mappedBy = "fixer", cascade = CascadeType.ALL)
	private List<BugReport> fixedBugReports;

	@OneToMany(mappedBy = "suggestionReporter", cascade = CascadeType.ALL)
	private List<Suggestion> reportedSuggestions;

	@OneToMany(mappedBy = "suggestionImplementor", cascade = CascadeType.ALL)
	private List<Suggestion> implementedSuggestions;

	@OneToMany(mappedBy = "sender", cascade = CascadeType.ALL)
	private List<Mail> sentMail;

	@OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL)
	private List<Mail> receivedMail;

	@OneToMany(mappedBy = "principalInvestigator", cascade = CascadeType.ALL)
	private List<Project> principalInvestigatedProjects;

	@OneToMany(mappedBy = "sponsorContact", cascade = CascadeType.ALL)
	private List<Project> sponsoredProjects;

	@OneToMany(mappedBy = "challengePublisher", cascade = CascadeType.ALL)
	private List<Challenge> createdChallenges;

	//TODO: TO remove
	private boolean unreadMention;

	/******************************************** Constructors ********************************************************/

	public User() {
	}

	public User(long id) {
		this.id = id;
	}

	public User(String userName, String email) {
		this.userName = userName;
		this.email = email;

		String firstName = userName.split(" ")[0];
		String lastName = "";
		if (userName.trim().length() > firstName.trim().length())
			lastName = userName.split(" ")[1];
		else
			lastName = firstName;
		this.firstName = firstName;
		this.lastName = lastName;

		this.password = "opennex";
		this.level = "normal";
		this.isActive = "True";
	}

	public User(String userName, String email, String password) {
		this.userName = userName;
		this.email = email;
		this.password = password;
	}

	public User(String userName, String password, String email,
				String phoneNumber) {
		super();
		this.userName = userName;
		this.password = password;
		this.email = email;
		this.phoneNumber = phoneNumber;
	}

	public User(String userName, String password, String firstName,
				String lastName, String middleInitial, String organization,
				String email, String mailingAddress, String phoneNumber, String level) {
		super();
		this.userName = userName;
		this.password = password;
		this.firstName = firstName;
		this.lastName = lastName;
		this.middleInitial = middleInitial;
		this.organization = organization;
		this.email = email;
		this.mailingAddress = mailingAddress;
		this.phoneNumber = phoneNumber;
		this.level = level;
	}

	/************************************* End of Constructors ********************************************************/

	public static Finder<Long, User> find =
			new Finder<Long, User>(User.class);


	/*************************************** Utility functions ********************************************************/


	public boolean isResearcher() {
		return this.userType != null && (this.userType & Constants.USER_TYPE.RESEARCHER.value()) == Constants.USER_TYPE.RESEARCHER.value();
	}

	public boolean isSponsor() {
		return this.userType != null && (this.userType & Constants.USER_TYPE.SPONSOR.value()) == Constants.USER_TYPE.SPONSOR.value();
	}

	public boolean isStudent() {
		return this.userType != null && (this.userType & Constants.USER_TYPE.STUDENT.value()) == Constants.USER_TYPE.STUDENT.value();
	}

	public void setResearcherFlag(boolean set) {
		if (set && !this.isResearcher()) {
			if (this.userType == null)
				this.userType = 0;
			this.userType += Constants.USER_TYPE.RESEARCHER.value();
		} else if (!set && this.isResearcher()) {
			this.userType -= Constants.USER_TYPE.RESEARCHER.value();
		}
	}

	public void setStudentFlag(boolean set) {
		if (set && !this.isStudent()) {
			if (this.userType == null)
				this.userType = 0;
			this.userType += Constants.USER_TYPE.STUDENT.value();
		} else if (!set && this.isStudent()) {
			this.userType -= Constants.USER_TYPE.STUDENT.value();
		}
	}

	public String getResearchFields() {
		if (this.researcherInfo != null) return this.researcherInfo.getResearchFields();
		return "";
	}

	public void setResearchFields(String researchFields) {
		if (this.researcherInfo != null) this.researcherInfo.setResearchFields(researchFields);
	}

	public String getHighestDegree() {
		if (this.researcherInfo != null) return this.researcherInfo.getHighestDegree();
		return "";
	}

	public void setHighestDegree(String highestDegree) {
		if (this.researcherInfo != null) this.researcherInfo.setHighestDegree(highestDegree);
	}

	public String getHomepage() {
		return homepage;
	}

	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}

	public User updateOrganization(String organization, String hiddenOrganizationName) {
		Organization organizationObj = null;

		if ("-1".equals(organization)) {
			organizationObj = Organization.find.query().where().eq(
					"organization_name", hiddenOrganizationName).findOne();
			if (null == organizationObj) {
				organizationObj = new Organization(hiddenOrganizationName, "", "", "");
				organizationObj.setOrganizationName(hiddenOrganizationName);
				organizationObj.save();
			}
		} else {
			organizationObj = Organization.find.byId(Long.valueOf(organization));
		}

//		List<Organization> organizations = this.getOrganizations();
		List<Organization> organizations = new ArrayList<>();
		organizations.add(organizationObj);
		this.setOrganizations(organizations);
		if (organizationObj != null) {
			this.setOrganization(organizationObj.getOrganizationName());
		}

		return this;
	}

	/**
	 * This method receives a json and updates user's information based on the given json, and then call eBean save
	 * function to store into database.
	 * @param json given json of updated user's information
	 */
	public void updateFromJson(JsonNode json, User user) throws Exception {
		try {
			this.deserializeFromJson(json);
			String projectIdStr = null;
			JsonNode projectIdNode = json.path("projectId");
			if (projectIdNode != null && !projectIdNode.asText().trim().equals("") && !projectIdNode.asText().trim().equals("null")) {
				projectIdStr = projectIdNode.asText();

				Long projectId = Long.parseLong(projectIdStr);
				ProjectController projectController = new ProjectController(new ProjectService());
			}
			this.save();
		} catch (Exception e) {
			Logger.debug("User.updateFromJson exception: " + e.toString());
			throw e;
		}
	}

	/**
	 * This method intends to deserialize a json and prepare a User object.
	 * @param json given json
	 */
	public void deserializeFromJson(JsonNode json) throws Exception {
		if (json.path("userName") != null) this.setUserName(json.path("userName").asText());
		if (json.path("firstName") != null) this.setFirstName(json.path("firstName").asText());
		if (json.path("middleInitial") != null) this.setMiddleInitial(json.path("middleInitial").asText());
		if (json.path("lastName") != null) this.setLastName(json.path("lastName").asText());
		if (json.path("organization") != null) this.updateOrganization(json.path("organization").asText(), json.path("hiddenOrganization").asText());
		if (json.path("email") != null) this.setEmail(json.path("email").asText());
		if (json.path("mailingAddress") != null) this.setMailingAddress(json.path("mailingAddress").asText());
		if (json.path("phoneNumber") != null) this.setPhoneNumber(json.path("phoneNumber").asText());
		if (json.path("researchFields") != null) this.setResearchFields(json.findPath("researchFields").asText());
		if (json.path("highestDegree") != null) this.setHighestDegree(json.path("highestDegree").asText());
		if (json.path("service_execution_counts") != null && json.path("service_execution_counts").asText() != "") {
			this.setService_execution_counts(Long.parseLong(json.path("service_execution_counts").asText()));
		}
		if (json.path("ratingCount") != null && json.path("ratingCount").asText() != "")
			this.setRatingCount(Long.parseLong(json.path("ratingCount").asText()));
		if (json.path("recommendRatingCount") != null && json.path("recommendRatingCount").asText() != "")
			this.setRecommendRatingCount(Long.parseLong(json.path("recommendRatingCount").asText()));
		if (json.path("rating") != null && json.path("rating").asText() != "")
			this.setRating(Double.parseDouble(json.path("rating").asText()));
		if (json.path("recommendRating") != null && json.path("recommendRating").asText() != "")
			this.setRecommendRating(Double.parseDouble(json.path("recommendRating").asText()));
		this.isActive = "True";

		try {
			JsonNode projectIdNode = json.path("projectId");
			if (projectIdNode != null && !projectIdNode.asText().trim().equals("") && !projectIdNode.asText().trim().equals("null")) {
				Long projectId = Long.parseLong(projectIdNode.asText());
				Project project = Project.find.byId(projectId);
				this.setProjectZone(project);
			}
		} catch (Exception e) {
			Logger.debug("User.deserializeFromJson exception: " + e.toString());
			throw e;
		}
	}


}
