package models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import play.Logger;
import play.libs.Json;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Paper.class)
@JsonIgnoreProperties({"notebooks"})

public class Paper {
    private long id;
    private String title;
    private String bookTitle;
    private String year;
    private String date;
    private String pages;


    private List<Author> authors;
    private String editor;
    // Journal, Conference, Book, BookChapter, PhDThesis, URL
    private String publicationType;
    // if journal, <journal>; if conference paper, <booktitle> (need to extend to full conference proceedings name)
    private String publicationChannel;
    private String month;
    private String url;
    private String publisher;

    public String getAbstractText() {
        return abstractText;
    }

    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }

    private String abstractText;
    public String getEditor() {
        return editor;
    }

    public void setEditor(String editor) {
        this.editor = editor;
    }

    public String getPublicationType() {
        return publicationType;
    }

    public void setPublicationType(String publicationType) {
        this.publicationType = publicationType;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getChapter() {
        return chapter;
    }

    public void setChapter(String chapter) {
        this.chapter = chapter;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    private String address;
    private String isbn;
    private String series;
    private String school;
    private String chapter;
    private String volume;
    private String number;



    /*********************************************** Constuctors ******************************************************/
    public Paper() {
    }


    /*********************************************** Utility methods **************************************************/

    /**
     * This method intends to deserialize a JsonNode to one Project object.
     *
     * @param node: a JsonNode containing info about a Project
     * @throws NullPointerException
     * @return: a Project object
     */
    public static Paper deserialize(JsonNode node) throws Exception {
        try {
            if (node == null) {
                throw new NullPointerException("Paper node should not be empty for Paper.deserialize()");
            }
            if (node.get("id") == null) {
                return null;
            }
            Paper paper = Json.fromJson(node, Paper.class);

            return paper;
        } catch (Exception e) {
            Logger.debug("Paper.deserialize() exception: " + e.toString());
            throw new Exception("Paper.deserialize() exception: " + e.toString());
        }
    }

    /**
     * This method return the deserialized Paper list
     *
     * @param PaperJsonArray
     * @return the deserialized Paper list
     */
    public static ArrayList<Paper> deserializeJsonArrayToPaperList(JsonNode paperJsonArray) throws Exception {
        ArrayList<Paper> paperList = new ArrayList<>();
        for (int i = 0; i < paperJsonArray.size(); i++) {
            JsonNode json = paperJsonArray.path(i);
            Paper paper = Paper.deserialize(json);
            paperList.add(paper);
        }
        return paperList;
    }

    /**
     * This utility method intends to return a list of Papers from JsonNode based on starting and ending index.
     *
     * @param PapersJson
     * @param startIndex
     * @param endIndex
     * @return: a list of Papers
     */
    public static List<Paper> deserializeJsonToPaperList(
            JsonNode papersJson, int startIndex, int endIndex, String sortCriteria) throws Exception {
        List<Paper> returnPapersList = new ArrayList<Paper>();
        List<Paper> allPapers = new ArrayList<>();
        for (int i = 0; i < papersJson.size(); i++) {
            JsonNode json = papersJson.path(i);
            Paper paper = Paper.deserialize(json);
            allPapers.add(paper);
        }
        for (int i = startIndex; i <= endIndex; i++) {
            returnPapersList.add(allPapers.get(i));
        }

        return returnPapersList;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPublicationChannel() {
        return publicationChannel;
    }

    public void setPublicationChannel(String publicationChannel) {
        this.publicationChannel = publicationChannel;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPages() {
        return pages;
    }

    public void setPages(String pages) {
        this.pages = pages;
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }
}

