package models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import play.libs.Json;

@Getter
@Setter
@ToString
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "orcid", scope = ResearcherInfo.class)
public class ResearcherInfo {
    private String highestDegree;

    private String orcid;

    private String researchFields;

    private String school;

    private String department;

    public static ResearcherInfo deserialize(JsonNode json) throws Exception {
        if (json == null) {
            throw new NullPointerException("User node should not be null to be serialized.");
        }
        ResearcherInfo researcher = Json.fromJson(json, ResearcherInfo.class);
        return researcher;
    }

    public String getHighestDegree(){
        return highestDegree;
    }

    public String getOrcid(){
        return orcid;
    }

    public String getResearchFields(){
        return researchFields;
    }

    public String getSchool(){
        return school;
    }

    public String getDepartment(){
        return department;
    }
}
