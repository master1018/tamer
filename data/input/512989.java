public class TrackerDataHelper {
    private Context mContext;
    protected IFormatter mFormatter;
    public static final IFormatter CSV_FORMATTER = new CSVFormatter();
    public static final IFormatter KML_FORMATTER = new KMLFormatter();
    public static final IFormatter NO_FORMATTER = new IFormatter() {
        public String getFooter() {
            return "";
        }
        public String getHeader() {
            return "";
        }
        public String getOutput(TrackerEntry entry) {
            return "";
        }
    };
    public TrackerDataHelper(Context context, IFormatter formatter) {
        mContext = context;
        mFormatter = formatter;
    }
    public TrackerDataHelper(Context context) {
        this(context, NO_FORMATTER);
    }
    void writeEntry(TrackerEntry entry) {
        mContext.getContentResolver().insert(TrackerProvider.CONTENT_URI,
                entry.getAsContentValues());
    }
    public void writeEntry(Location loc, float distFromNetLoc) {
        writeEntry(TrackerEntry.createEntry(loc, distFromNetLoc));
    }
    public void writeEntry(String tag, String logMsg) {
        writeEntry(TrackerEntry.createEntry(tag, logMsg));
    }
    public void deleteAll() {
        mContext.getContentResolver().delete(TrackerProvider.CONTENT_URI, null,
                null);
    }
    public Cursor query(String tag, int limit) {
        String selection = (tag == null ? null : TrackerEntry.TAG + "=?");
        String[] selectionArgs = (tag == null ? null : new String[] {tag});
        Cursor cursor = mContext.getContentResolver().query(
                TrackerProvider.CONTENT_URI, TrackerEntry.ATTRIBUTES,
                selection, selectionArgs, null);
        if (cursor == null) {
            return cursor;
        }
        int pos = (cursor.getCount() < limit ? 0 : cursor.getCount() - limit);
        cursor.moveToPosition(pos);
        return cursor;
    }
    public Cursor query(int limit) {
        return query(null, limit);
    }
    public Cursor query(String tag) {
        return query(tag, Integer.MAX_VALUE);
    }
    public String getOutputHeader() {
        return mFormatter.getHeader();
    }
    public String getOutputFooter() {
        return mFormatter.getFooter();
    }
    public String getNextOutput(Cursor cursor) {
        if (cursor == null || cursor.isAfterLast()) {
            return null;
        }
        String output = mFormatter.getOutput(TrackerEntry.createEntry(cursor));
        cursor.moveToNext();
        return output;
    }
}
