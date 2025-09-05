package services.dblp.search;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DBLPSearchEndpoint {
    PUBLICATION("/search/publ/api"),
    AUTHOR("/search/author/api"),
    VENUE("/search/venue/api");

    private final String extension;
}
