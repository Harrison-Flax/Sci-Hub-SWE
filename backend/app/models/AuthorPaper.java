package models;
import com.fasterxml.jackson.annotation.*;
import io.ebean.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
@Entity
@Getter
@Setter
public class AuthorPaper extends Model implements Serializable {

    private Long authorId;
    private Long paperId;



    public static Finder<Long, AuthorPaper> find =
            new Finder<Long, AuthorPaper>(AuthorPaper.class);
}
