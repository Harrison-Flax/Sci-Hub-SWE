package models.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import play.libs.Json;

@Getter
@Setter
public class RESTResponse {

    private ArrayNode items;
    private int limit = 0;
    private int offset = 0;
    private int total = 0;
    private String sort = "";


    // c: community-based; n: NASA-based
    private int communityTotal = 0;
    private int nasaTotal = 0;
    private int communityOffset = 0;
    private int nasaOffset = 0;
    private ArrayNode communityItems;
    private ArrayNode nasaItems;

    /*********************************************** Utility methods **************************************************/
    /**
     * Create a response json object with the items in a REST safe format.
     *
     * @return the response json.
     */
    public JsonNode response() {
        ObjectNode responseObj = Json.newObject();
        // Set the offset.
        responseObj.put("offset", this.offset);
        // Set the items.
        responseObj.set("items", this.items);
        // Set the count.
        int count = items==null ? 0 : this.items.size();
        responseObj.put("count", count);
        // Check if limit is set, else set it to count.
        if (this.limit != 0) {
            // Set the limit.
            responseObj.put("limit", this.limit);
        } else {
            // Set it to count.
            responseObj.put("limit", count);
        }
        // Total
        responseObj.put("total", total);
        // Sort order
        responseObj.put("sort", sort);
        // Return the result.

        // communityTotal
        responseObj.put("communityTotal", communityTotal);
        // nasaTotal
        responseObj.put("nasaTotal", nasaTotal);
        // Set the coffset.
        responseObj.put("communityOffset", this.communityOffset);
        // Set the noffset.
        responseObj.put("nasaOffset", this.nasaOffset);

        // Set the cCount.
        int cCount = this.communityItems ==null ? 0 : this.communityItems.size();
        responseObj.put("communityCount", cCount);
        // Set the nCount.
        int nCount = this.nasaItems ==null ? 0 : this.nasaItems.size();
        responseObj.put("nasaCount", nCount);

        responseObj.put("communityItems", communityItems);
        responseObj.put("nasaItems", nasaItems);
        return responseObj;
    }


}
