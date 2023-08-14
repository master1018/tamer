public class TrackerListHelper extends TrackerDataHelper {
    private ListActivity mActivity;
    private static final String SORT_ORDER = TrackerEntry.ID_COL + " DESC";
    public TrackerListHelper(ListActivity activity) {
        super(activity, TrackerDataHelper.CSV_FORMATTER);
        mActivity = activity;
    }
    public void bindListUI(int layout) {
        Cursor cursor = mActivity.managedQuery(TrackerProvider.CONTENT_URI,
                TrackerEntry.ATTRIBUTES, null, null, SORT_ORDER);
        TrackerAdapter adapter = new TrackerAdapter(mActivity, layout, cursor);
        mActivity.setListAdapter(adapter);
        cursor.setNotificationUri(mActivity.getContentResolver(),
                TrackerProvider.CONTENT_URI);
    }
    private class TrackerAdapter extends ResourceCursorAdapter {
        public TrackerAdapter(Context context, int layout, Cursor c) {
            super(context, layout, c);
        }
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final TextView v = (TextView) view
                    .findViewById(R.id.entrylist_item);
            String rowText = mFormatter.getOutput(TrackerEntry
                    .createEntry(cursor));
            v.setText(rowText);
        }
    }
}
