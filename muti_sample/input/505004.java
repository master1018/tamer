public class ExchangeProvider extends ContentProvider {
    public static final String EXCHANGE_AUTHORITY = "com.android.exchange.provider";
    public static final Uri GAL_URI = Uri.parse("content:
    private static final int GAL_BASE = 0;
    private static final int GAL_FILTER = GAL_BASE;
    public static final String[] GAL_PROJECTION = new String[] {"_id", "displayName", "data"};
    public static final int GAL_COLUMN_ID = 0;
    public static final int GAL_COLUMN_DISPLAYNAME = 1;
    public static final int GAL_COLUMN_DATA = 2;
    public static final String EXTRAS_TOTAL_RESULTS = "com.android.exchange.provider.TOTAL_RESULTS";
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        UriMatcher matcher = sURIMatcher;
        matcher.addURI(EXCHANGE_AUTHORITY, "gal
    private static class MatrixCursorExtras extends MatrixCursor {
        private Bundle mExtras;
        public MatrixCursorExtras(String[] columnNames) {
            super(columnNames);
            mExtras = null;
        }
        public void setExtras(Bundle extras) {
            mExtras = extras;
        }
        @Override
        public Bundle getExtras() {
            return mExtras;
        }
    }
}
