package models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = ListPagination.class)

public class ListPagination {
    private int page;
    private int count;
    private int total;
    private int offset;
    private int beginIndex;
    private int endIndex;

    /*********************************************** Constuctors ******************************************************/
    public ListPagination(int page, int count, int total, int offset, int beginIndex, int endIndex) {
        this.page = page;
        this.count = count;
        this.total = total;
        this.offset = offset;
        this.beginIndex = beginIndex;
        this.endIndex = endIndex;
    }
    /*********************************************** Utility methods **************************************************/



    /*********************************************** Getters & Setters ************************************************/
    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getBeginIndex() {
        return beginIndex;
    }

    public void setBeginIndex(int beginIndex) {
        this.beginIndex = beginIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }
}
