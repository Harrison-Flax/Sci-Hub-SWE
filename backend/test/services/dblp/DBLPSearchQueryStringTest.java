package services.dblp;

import org.junit.Test;
import services.dblp.search.DBLPSearchQueryString;

import static org.junit.Assert.assertEquals;

public class DBLPSearchQueryStringTest {
    @Test
    public void testQueryPrefix() {
        final DBLPSearchQueryString query = DBLPSearchQueryString.builder()
                .addPrefix("myPrefix")
                .build();
        assertEquals("myPrefix", query.getQuery());
    }

    @Test
    public void testQueryExactly() {
        final DBLPSearchQueryString query = DBLPSearchQueryString.builder()
                .addExactly("myExact")
                .build();
        assertEquals("myExact$", query.getQuery());
    }

    @Test
    public void testCompoundAnd() {
        final DBLPSearchQueryString query = DBLPSearchQueryString.builder()
                .addPrefix("PartA")
                .addAnd("PartB", "PartC")
                .build();
        assertEquals("PartA PartB PartC", query.getQuery());
    }

    @Test
    public void testCompoundOr() {
        final DBLPSearchQueryString query = DBLPSearchQueryString.builder()
                .addPrefix("PartA")
                .addOr("PartB", "PartC")
                .build();
        assertEquals("PartA|PartB|PartC", query.getQuery());
    }

    @Test
    public void testNestedCompoundAnd() {
        final DBLPSearchQueryString firstQuery = DBLPSearchQueryString.builder()
                .addPrefix("PartA")
                .addOr("PartB", "PartC")
                .build();
        final DBLPSearchQueryString compoundQuery = DBLPSearchQueryString.builder()
                .addPrefix("Prefix")
                .addAnd(firstQuery)
                .build();
        assertEquals("Prefix PartA|PartB|PartC", compoundQuery.getQuery());
    }

    @Test
    public void testNestedCompoundOr() {
        final DBLPSearchQueryString firstQuery = DBLPSearchQueryString.builder()
                .addPrefix("PartA")
                .addAnd("PartB", "PartC")
                .build();
        final DBLPSearchQueryString compoundQuery = DBLPSearchQueryString.builder()
                .addPrefix("Prefix")
                .addOr(firstQuery)
                .build();
        assertEquals("Prefix|PartA PartB PartC", compoundQuery.getQuery());
    }
}
