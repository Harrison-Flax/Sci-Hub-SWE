package models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import io.ebean.Finder;
import io.ebean.Model;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Mail.class)
@JsonIgnoreProperties({"sender", "receiver", "attachments"})

public class Mail extends Model {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String title;

    
    private String content;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy hh:mm:ss")
    private Date timestamp;

    @ManyToOne(cascade = CascadeType.ALL)
    private User sender;

    @ManyToOne(cascade = CascadeType.ALL)
    private User receiver;

    @ManyToMany(cascade = CascadeType.ALL)
    private List<File> attachments;



    @PrePersist
    void createdAt() {
        timestamp = new Date();
    }

    public static Finder<Long, Mail> find =
            new Finder<Long, Mail>(Mail.class);

}
