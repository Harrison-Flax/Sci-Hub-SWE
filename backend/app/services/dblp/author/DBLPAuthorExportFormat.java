package services.dblp.author;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DBLPAuthorExportFormat {
    XML("xml"),
    BIB("bib"),
    NT("nt"),
    RDF("rdf"),
    RSS("rss");

    private final String formatName;
}
