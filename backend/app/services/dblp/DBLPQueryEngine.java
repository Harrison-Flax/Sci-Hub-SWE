package services.dblp;

import lombok.NonNull;
import utils.RESTfulCalls;

public class DBLPQueryEngine {
    public String executeQuery(@NonNull final DBLPQuery query) {
        final String queryString = query.getAssembledQuery();
        return RESTfulCalls.getToString(queryString);
    }
}
