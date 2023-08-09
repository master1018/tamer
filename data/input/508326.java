public class UserDictionary {
    public static final String AUTHORITY = "user_dictionary";
    public static final Uri CONTENT_URI =
        Uri.parse("content:
    public static class Words implements BaseColumns {
        public static final Uri CONTENT_URI =
                Uri.parse("content:
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.google.userword";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.google.userword";
        public static final String _ID = BaseColumns._ID;
        public static final String WORD = "word";
        public static final String FREQUENCY = "frequency";
        public static final String LOCALE = "locale";
        public static final String APP_ID = "appid";
        public static final int LOCALE_TYPE_ALL = 0;
        public static final int LOCALE_TYPE_CURRENT = 1;
        public static final String DEFAULT_SORT_ORDER = FREQUENCY + " DESC";
        public static void addWord(Context context, String word, 
                int frequency, int localeType) {
            final ContentResolver resolver = context.getContentResolver();
            if (TextUtils.isEmpty(word) || localeType < 0 || localeType > 1) {
                return;
            }
            if (frequency < 0) frequency = 0;
            if (frequency > 255) frequency = 255;
            String locale = null;
            if (localeType == LOCALE_TYPE_CURRENT) {
                locale = Locale.getDefault().toString();
            }
            ContentValues values = new ContentValues(4);
            values.put(WORD, word);
            values.put(FREQUENCY, frequency);
            values.put(LOCALE, locale);
            values.put(APP_ID, 0); 
            Uri result = resolver.insert(CONTENT_URI, values);
        }
    }
}
