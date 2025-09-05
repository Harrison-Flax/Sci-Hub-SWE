package models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import play.Logger;
import play.libs.Json;

@Getter
@Setter
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Fellowship.class)
@JsonIgnoreProperties({"notebooks"})
public class Fellowship extends Project {
    /**
     * This method intends to deserialize a JsonNode to one Fellowship object.
     *
     * @param node: a JsonNode containing info about a Fellowship
     * @throws NullPointerException
     * @return: a Fellowship object
     */
    public static Fellowship deserialize(JsonNode node) throws Exception {
        try {
            if (node == null) {
                throw new NullPointerException("Project node should not be empty for Project.deserialize()");
            }
            if (node.get("id") == null) {
                return null;
            }
            Fellowship fellowship = Json.fromJson(node, Fellowship.class);
            //TODO: what is this???

            return fellowship;
        } catch (Exception e) {
            Logger.debug("Project.deserialize() exception: " + e.toString());
            throw new Exception("Project.deserialize() exception: " + e.toString());
        }
    }
}
