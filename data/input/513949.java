public class SocialContract {
    public static final String AUTHORITY = "com.android.social";
        public static final String RES_PACKAGE = "res_package";
        public static final String MIMETYPE = "mimetype";
        public static final String RAW_ID = "raw_id";
        public static final String IN_REPLY_TO = "in_reply_to";
        public static final String AUTHOR_CONTACT_ID = "author_contact_id";
        public static final String TARGET_CONTACT_ID = "target_contact_id";
        public static final String PUBLISHED = "published";
        public static final String THREAD_PUBLISHED = "thread_published";
        public static final String TITLE = "title";
        public static final String SUMMARY = "summary";
        public static final String LINK = "link";
        public static final String THUMBNAIL = "thumbnail";
    }
    public static final class Activities implements BaseColumns, ActivitiesColumns {
        private Activities() {
        }
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "activities");
        public static final Uri CONTENT_AUTHORED_BY_URI =
            Uri.withAppendedPath(CONTENT_URI, "authored_by");
        public static final Uri CONTENT_CONTACT_STATUS_URI =
            Uri.withAppendedPath(AUTHORITY_URI, "contact_status");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/activity";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/activity";
    }
}
