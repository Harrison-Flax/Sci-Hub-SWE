package services.dblp;

import org.junit.Test;
import services.dblp.author.DBLPAuthorExportFormat;
import services.dblp.author.DBLPAuthorExportQuery;

import static org.junit.Assert.assertEquals;

public class DBLPAuthorQueryTest {
    @Test
    public void testAuthorQuery() {
        final DBLPAuthorExportQuery query = DBLPAuthorExportQuery.builder()
                .authorPID("65/9612")
                .format(DBLPAuthorExportFormat.RDF)
                .build();

        assertEquals("http://dblp.org/pid/65/9612.rdf", query.getAssembledQuery());
    }
}
