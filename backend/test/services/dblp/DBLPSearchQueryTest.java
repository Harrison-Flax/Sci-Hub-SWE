package services.dblp;

import org.junit.Test;
import services.dblp.search.DBLPSearchEndpoint;
import services.dblp.search.DBLPSearchFormat;
import services.dblp.search.DBLPSearchQuery;
import services.dblp.search.DBLPSearchQueryString;

import static org.junit.Assert.assertEquals;

public class DBLPSearchQueryTest {
    @Test
    public void testQuery() {
        final DBLPSearchQuery query = DBLPSearchQuery
                .builder()
                .queryString(DBLPSearchQueryString.builder()
                        .addPrefix("mySearch")
                        .build())
                .endpoint(DBLPSearchEndpoint.AUTHOR)
                .format(DBLPSearchFormat.JSON)
                .maxCompletionTerms(15)
                .maxResults(20)
                .minRecordNumber(25)
                .build();
        assertEquals("http://dblp.org/search/author/api?format=json&h=20&f=25&c=15&q=mySearch",
                query.getAssembledQuery());
    }
}
