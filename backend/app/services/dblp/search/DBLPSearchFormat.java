package services.dblp.search;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DBLPSearchFormat {
    XML("xml"),
    JSON("json"),
    JSONP("jsonp");

    private final String formatText;
}
