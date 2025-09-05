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
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id",
        scope = File.class)
@JsonIgnoreProperties({"mails"})

public class File extends Model {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String fileName;
    private String path;
    private String tableName;
    private String fileType;
    private String tableRecorderId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
    private Date timestamp;



    public static Finder<Long, File> find =
            new Finder<Long, File>(File.class);

    @PrePersist
    void createdAt() {
        timestamp = new Date();
    }
}
