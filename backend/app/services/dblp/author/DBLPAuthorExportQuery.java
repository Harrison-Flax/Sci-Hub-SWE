package services.dblp.author;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import services.dblp.DBLPQuery;

@Builder
@Getter
public class DBLPAuthorExportQuery implements DBLPQuery {
    /**
     * Export format. Defaults to XML.
     */
    @NonNull
    @Builder.Default
    private final DBLPAuthorExportFormat format = DBLPAuthorExportFormat.XML;

    /**
     * PID of author. Should be in format of #/#; for example, "65/9612"
     */
    @NonNull
    private final String authorPID;

    @Override
    public String getAssembledQuery() {
        return "http://dblp.org/pid/" + authorPID + "." + format.getFormatName();
    }
}
