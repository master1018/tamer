public class CallLog {
    public static final String AUTHORITY = "call_log";
    public static final Uri CONTENT_URI =
        Uri.parse("content:
    public static class Calls implements BaseColumns {
        public static final Uri CONTENT_URI =
                Uri.parse("content:
        public static final Uri CONTENT_FILTER_URI =
                Uri.parse("content:
        public static final String DEFAULT_SORT_ORDER = "date DESC";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/calls";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/calls";
        public static final String TYPE = "type";
        public static final int INCOMING_TYPE = 1;
        public static final int OUTGOING_TYPE = 2;
        public static final int MISSED_TYPE = 3;
        public static final String NUMBER = "number";
        public static final String DATE = "date";
        public static final String DURATION = "duration";
        public static final String NEW = "new";
        public static final String CACHED_NAME = "name";
        public static final String CACHED_NUMBER_TYPE = "numbertype";
        public static final String CACHED_NUMBER_LABEL = "numberlabel";
        public static Uri addCall(CallerInfo ci, Context context, String number,
                int presentation, int callType, long start, int duration) {
            final ContentResolver resolver = context.getContentResolver();
            if (presentation == Connection.PRESENTATION_RESTRICTED) {
                number = CallerInfo.PRIVATE_NUMBER;
                if (ci != null) ci.name = "";
            } else if (presentation == Connection.PRESENTATION_PAYPHONE) {
                number = CallerInfo.PAYPHONE_NUMBER;
                if (ci != null) ci.name = "";
            } else if (TextUtils.isEmpty(number)
                    || presentation == Connection.PRESENTATION_UNKNOWN) {
                number = CallerInfo.UNKNOWN_NUMBER;
                if (ci != null) ci.name = "";
            }
            ContentValues values = new ContentValues(5);
            values.put(NUMBER, number);
            values.put(TYPE, Integer.valueOf(callType));
            values.put(DATE, Long.valueOf(start));
            values.put(DURATION, Long.valueOf(duration));
            values.put(NEW, Integer.valueOf(1));
            if (ci != null) {
                values.put(CACHED_NAME, ci.name);
                values.put(CACHED_NUMBER_TYPE, ci.numberType);
                values.put(CACHED_NUMBER_LABEL, ci.numberLabel);
            }
            if ((ci != null) && (ci.person_id > 0)) {
                ContactsContract.Contacts.markAsContacted(resolver, ci.person_id);
            }
            Uri result = resolver.insert(CONTENT_URI, values);
            removeExpiredEntries(context);
            return result;
        }
        public static String getLastOutgoingCall(Context context) {
            final ContentResolver resolver = context.getContentResolver();
            Cursor c = null;
            try {
                c = resolver.query(
                    CONTENT_URI,
                    new String[] {NUMBER},
                    TYPE + " = " + OUTGOING_TYPE,
                    null,
                    DEFAULT_SORT_ORDER + " LIMIT 1");
                if (c == null || !c.moveToFirst()) {
                    return "";
                }
                return c.getString(0);
            } finally {
                if (c != null) c.close();
            }
        }
        private static void removeExpiredEntries(Context context) {
            final ContentResolver resolver = context.getContentResolver();
            resolver.delete(CONTENT_URI, "_id IN " +
                    "(SELECT _id FROM calls ORDER BY " + DEFAULT_SORT_ORDER
                    + " LIMIT -1 OFFSET 500)", null);
        }
    }
}
