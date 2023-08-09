public class ApplicationsAdapter extends ResourceCursorAdapter {
    private static final boolean DBG = false;
    private static final String TAG = "ApplicationsAdapter";
    public ApplicationsAdapter(Context context, Cursor c) {
        super(context, R.layout.application_list_item, c);
    }
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView icon1 = (ImageView) view.findViewById(R.id.icon1);
        TextView text1 = (TextView) view.findViewById(R.id.text1);
        Uri iconUri = getColumnUri(cursor, Applications.ApplicationColumns.ICON);
        String name = getColumnString(cursor, Applications.ApplicationColumns.NAME);
        if (DBG) Log.d(TAG, "name=" + name + ",icon=" + iconUri);
        icon1.setImageURI(iconUri);
        text1.setText(name);
    }
    public static Uri getColumnUri(Cursor cursor, String columnName) {
        String uriString = getColumnString(cursor, columnName);
        if (TextUtils.isEmpty(uriString)) return null;
        return Uri.parse(uriString);
    }
    public static String getColumnString(Cursor cursor, String columnName) {
        int col = cursor.getColumnIndex(columnName);
        return getStringOrNull(cursor, col);
    }
    private static String getStringOrNull(Cursor cursor, int col) {
        if (col < 0) return null;
        try {
            return cursor.getString(col);
        } catch (RuntimeException e) {
            Log.e(TAG, "Failed to get column " + col + " from cursor", e);
            return null;
        }
    }
}
