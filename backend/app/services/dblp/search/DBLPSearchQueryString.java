package services.dblp.search;

import lombok.*;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DBLPSearchQueryString {
    @NonNull
    private final String query;

    public static DBLPQueryStringBuilder builder() {
        return new DBLPQueryStringBuilder();
    }

    public static class DBLPQueryStringBuilder {
        private final StringBuilder queryBuilder = new StringBuilder();

        public DBLPQueryStringBuilder addPrefix(@NonNull final String prefix) {
            queryBuilder.append(prefix);
            return this;
        }

        public DBLPQueryStringBuilder addExactly(@NonNull final String exactly) {
            queryBuilder.append(exactly + "$");
            return this;
        }

        public DBLPQueryStringBuilder addAnd(@NonNull final DBLPSearchQueryString query) {
            addAnd(query.getQuery());
            return this;
        }

        public DBLPQueryStringBuilder addAnd(@NonNull final String... and) {
            for (@NonNull final String s : and) {
                queryBuilder.append(" " + s);
            }
            return this;
        }

        public DBLPQueryStringBuilder addOr(@NonNull final DBLPSearchQueryString query) {
            addOr(query.getQuery());
            return this;
        }

        public DBLPQueryStringBuilder addOr(@NonNull final String... or) {
            for (@NonNull final String s : or) {
                queryBuilder.append("|" + s);
            }
            return this;
        }

        public DBLPSearchQueryString build() {
            final String builtQuery = queryBuilder.toString();
            if (builtQuery.length() == 0) {
                throw new IllegalStateException("Empty query for DBLP");
            }
            return new DBLPSearchQueryString(builtQuery);
        }
    }
}
