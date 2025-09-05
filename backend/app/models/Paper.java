package models;

import java.util.List;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.ProjectController;
import io.ebean.*;
import io.ebean.annotation.JsonIgnore;

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
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Paper.class)
@ToString
public class Paper extends Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    
    private String title="";
    
    private String bookTitle="";

    
    private String editor="";

    
    private String abstractText="";
    // Journal, Conference, Book, BookChapter, PhDThesis, URL
    private String publicationType="";

    // if journal, <journal>; if conference paper, <booktitle> (need to extend to full conference proceedings name)
    
    private String publicationChannel="";

    
    private String date="";

    
    private String year="";
    
    private String month="";

    
    private String url="";

    
    private String publisher="";

    
    private String address="";

    
    private String isbn="";
    
    private String series="";
    
    private String school="";
    
    private String chapter="";

    
    private String volume="";
    
    private String number="";

    
    private String pages="";

    
    private String allAuthorsString="";


    @ManyToMany(cascade = CascadeType.ALL,mappedBy="papersByAuthor")
    @JoinTable(name = "author_paper")
    private List<Author> authors;

    /****************** Constructors **********************************************************************************/
    public Paper() {
    }

    public Paper(long paperId) {
        this.id = paperId;
    }
    /****************** End of Constructors ***************************************************************************/

    public static Finder<Long, Paper> find =
            new Finder<Long, Paper>(Paper.class);


}
