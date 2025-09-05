package models;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class SearchBody {

    private String searchString;

    /*********************************************** Constuctors ******************************************************/

    /*********************************************** Utility methods **************************************************/

    /*********************************************** Getters & Setters ************************************************/
    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public SearchBody(JsonNode jsonNode) {
        searchString = jsonNode.toString();
    }

    public SearchBody(String searchString) {
        this.searchString = searchString;
    }

    public String toString() {
        return "Search string:" + getSearchString() + ":";
    }

}
