package models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import play.Logger;
import play.libs.Json;
import utils.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Getter
@Setter
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Project.class)
@JsonIgnoreProperties({"notebooks"})

public class Project extends Challenge{
    private long id;
    private Long parentProjectId;

    private String isActive;
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

    private User principalInvestigator;
    private Organization principalInvestigatorOrganization;

    private Organization sponsorOrganization;
    private User sponsorContact;

    private User creator;


    private List<Challenge> projectChallenges;

    private List<User> teamMembers;


    /*********************************************** Constuctors ******************************************************/
    public Project() {
    }


    /*********************************************** Utility methods **************************************************/
    public String getTruncGoals() {
        int maxChar = 58;
        return TextUtils.truncateString(maxChar, getGoals());
    }

    public boolean getIsPopular() {
        return this.isPopular;
    }

    /**
     * This method intends to deserialize a JsonNode to one Project object.
     *
     * @param node: a JsonNode containing info about a Project
     * @throws NullPointerException
     * @return: a Project object
     */
    public static Project deserialize(JsonNode node) throws Exception {
        try {
            if (node == null) {
                throw new NullPointerException("Project node should not be empty for Project.deserialize()");
            }
            if (node.get("id") == null) {
                return null;
            }
            Project project = Json.fromJson(node, Project.class);
            if (project.getAuthentication() == null) {
                project.setAuthentication("public");
            }
            //TODO: what is this???
            project.setNextImageIndex(1);

            return project;
        } catch (Exception e) {
            Logger.debug("Project.deserialize() exception: " + e.toString());
            throw new Exception("Project.deserialize() exception: " + e.toString());
        }
    }

    /**
     * This method return the deserialized project list
     *
     * @param projectJsonArray
     * @return the deserialized project list
     */
    public static ArrayList<Project> deserializeJsonArrayToProjectList(JsonNode projectJsonArray) throws Exception {
        ArrayList<Project> projectList = new ArrayList<>();
        for (int i = 0; i < projectJsonArray.size(); i++) {
            JsonNode json = projectJsonArray.path(i);
            Project project = Project.deserialize(json);
            projectList.add(project);
        }
        return projectList;
    }

    /**
     * This utility method intends to return a list of projects from JsonNode based on starting and ending index.
     *
     * @param projectsJson
     * @param startIndex
     * @param endIndex
     * @return: a list of projects
     */
    public static List<Project> deserializeJsonToProjectList(
            JsonNode projectsJson, int startIndex, int endIndex, String sortCriteria) throws Exception {
        List<Project> returnProjectsList = new ArrayList<Project>();
        List<Project> allProjects = new ArrayList<>();
        for (int i = 0; i < projectsJson.size(); i++) {
            JsonNode json = projectsJson.path(i);
            Project project = Project.deserialize(json);
            allProjects.add(project);
        }
        sortProjects(allProjects, sortCriteria);
        for (int i = startIndex; i <= endIndex; i++) {
            returnProjectsList.add(allProjects.get(i));
        }

        return returnProjectsList;
    }


    //****************************************************************************************************************//
    //************************************* Private Methods **********************************************************//
    //****************************************************************************************************************//

    /**
     * This private method intends to sort a list of projects based on sortCriteria (some field).
     *
     * @param projects
     * @param sortCriteria
     */
    private static void sortProjects(List<Project> projects, String sortCriteria) {
        if (sortCriteria.equals("id")) {
            Comparator<Project> com = new Comparator<Project>() {
                @Override
                public int compare(Project o1, Project o2) {
                    long id1 = o1.getId();
                    long id2 = o2.getId();
                    return (int) (id1 - id2);
                }
            };
            Collections.sort(projects, com);
        } else if (sortCriteria.equals("title")) {
            Comparator<Project> com = new Comparator<Project>() {
                @Override
                public int compare(Project o1, Project o2) {
                    String title1 = o1.getTitle();
                    String title2 = o2.getTitle();
                    return title1.toLowerCase().compareTo(title2.toLowerCase());
                }
            };
            Collections.sort(projects, com);
        } else if (sortCriteria.equals("location")) {
            Comparator<Project> com = new Comparator<Project>() {
                @Override
                public int compare(Project o1, Project o2) {
                    String location1 = o1.getLocation();
                    String location2 = o2.getLocation();
                    return location1.toLowerCase().compareTo(location2.toLowerCase());
                }
            };
            Collections.sort(projects, com);
        }
    }


    /*********************************************** Getters & Setters *************************************************/

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getParentProjectId() {
        return parentProjectId;
    }

    public void setParentProjectId(Long parentProjectId) {
        this.parentProjectId = parentProjectId;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getEndDate() {
        return endDate;
    }
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStartDate() {
        return startDate;
    }
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
    public void setIsPopular(boolean popular) {
        this.isPopular = popular;
    }

    public long getPopularRanking() {
        return popularRanking;
    }

    public void setPopularRanking(long popularRanking) {
        this.popularRanking = popularRanking;
    }

    public String getAuthentication() {
        return authentication;
    }

    public void setAuthentication(String authentication) {
        this.authentication = authentication;
    }

    public long getAccessTimes() {
        return accessTimes;
    }

    public void setAccessTimes(long accessTimes) {
        this.accessTimes = accessTimes;
    }

    public Integer getNextImageIndex() {
        return nextImageIndex;
    }

    public void setNextImageIndex(Integer nextImageIndex) {
        this.nextImageIndex = nextImageIndex;
    }

    public String getTechnology() {
        return technology;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }

    public String getPdf() {
        return pdf;
    }

    public void setPdf(String pdf) {
        this.pdf = pdf;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getGoals() {
        return goals;
    }

    public void setGoals(String goals) {
        this.goals = goals;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getGithubUrl() {
        return githubUrl;
    }

    public void setGithubUrl(String githubUrl) {
        this.githubUrl = githubUrl;
    }

    public String getTeamPageUrl() {
        return teamPageUrl;
    }

    public void setTeamPageUrl(String teamPageUrl) {
        this.teamPageUrl = teamPageUrl;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public List<User> getTeamMembers() {
        return teamMembers;
    }

    public void setTeamMembers(List<User> teamMembers) {
        this.teamMembers = teamMembers;
    }


    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }

    public User getPrincipalInvestigator() {
        return principalInvestigator;
    }

    public void setPrincipalInvestigator(User principalInvestigator) {
        this.principalInvestigator = principalInvestigator;
    }

    public Organization getPrincipalInvestigatorOrganization() {
        return principalInvestigatorOrganization;
    }

    public void setPrincipalInvestigatorOrganization(Organization principalInvestigatorOrganization) {
        this.principalInvestigatorOrganization = principalInvestigatorOrganization;
    }

    public Organization getSponsorOrganization() {
        return sponsorOrganization;
    }

    public void setSponsorOrganization(Organization sponsorOrganization) {
        this.sponsorOrganization = sponsorOrganization;
    }

    public User getSponsorContact() {
        return sponsorContact;
    }

    public void setSponsorContact(User sponsorContact) {
        this.sponsorContact = sponsorContact;
    }
}

