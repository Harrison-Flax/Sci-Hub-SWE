package models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.ebean.Finder;
import io.ebean.Model;
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
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = BugReport.class)

public class BugReport extends Model {

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
    private User reporter;

    @ManyToOne
    @JoinColumn(name = "fixer_id", referencedColumnName = "id")
    private User fixer;


    /************************************************** Constructor ****************************************************/
    public BugReport() {
    }

    /************************************************** End of Constructor *********************************************/


    /*************************************************** Finder ********************************************************/
    public static Finder<Long, BugReport> find =  new Finder<Long, BugReport>(BugReport.class);
    /*************************************************** End of Finder *************************************************/


    /*************************************************** Deserializer **************************************************/
    /**
     * This method could be used for bug report registration, when user information is not passed.
     *
     * @param jsonNode
     * @param user
     * @return
     * @throws Exception
     */
//    public static BugReport deserialize(JsonNode jsonNode, User user) throws Exception {
//        BugReport bugReport = Json.fromJson(jsonNode, BugReport.class);
//
//        bugReport.setEmail(user.getEmail());
//        bugReport.setName(user.getUserName());
//        bugReport.setOrganization(user.getorganization());
//
//        return bugReport;
//    }

    /**
     * Turn bug report report list into json array
     *
     * @param bugReportList list of bug reports
     * @return json array of serialized bug reports
     */
//    public static ArrayNode bugReportList2JsonArray(List<BugReport> bugReportList) throws Exception {
//        ArrayNode bugReportsNode = Json.newArray();
//        for (BugReport bugReport : bugReportList) {
//            ObjectNode projectNode = (ObjectNode) Json.toJson(bugReport);
//            bugReportsNode.add(projectNode);
//        }
//        return bugReportsNode;
//    }

    /*************************************************** End of Deserializer *******************************************/

}
