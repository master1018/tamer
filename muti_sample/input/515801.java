public final class NotePad {
    public static final class Notes implements BaseColumns {
        public static final Uri CONTENT_URI
                = Uri.parse("content:
        public static final String DEFAULT_SORT_ORDER = "modified DESC";
        public static final String TITLE = "title";
        public static final String NOTE = "note";
        public static final String CREATED_DATE = "created";
        public static final String MODIFIED_DATE = "modified";
    }
}
