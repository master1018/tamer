public class CalendarDebug extends ListActivity {
    private static final String[] CALENDARS_PROJECTION = new String[]{
            Calendar.Calendars._ID,
            Calendar.Calendars.DISPLAY_NAME,
    };
    private static final int INDEX_ID = 0;
    private static final int INDEX_DISPLAY_NAME = 1;
    private static final String[] EVENTS_PROJECTION = new String[]{
            Calendar.Events._ID,
    };
    private static final String KEY_TITLE = "title";
    private static final String KEY_TEXT = "text";
    private ContentResolver mContentResolver;
    private ListActivity mActivity;
    private class FetchInfoTask extends AsyncTask<Void, Void, List<Map<String, String>>> {
        @Override
        protected void onPreExecute() {
              setProgressBarIndeterminateVisibility(true);
        }
        protected List<Map<String, String>> doInBackground(Void... params) {
            Cursor cursor = null;
            List<Map<String, String>> items = new ArrayList<Map<String, String>>();
            try {
                cursor = mContentResolver.query(Calendar.Calendars.CONTENT_URI,
                        CALENDARS_PROJECTION,
                        null, null ,
                        Calendar.Calendars.DEFAULT_SORT_ORDER);
                if (cursor == null) {
                    addItem(items, mActivity.getString(R.string.calendar_info_error), "");
                } else {
                    while (cursor.moveToNext()) {
                        int id = cursor.getInt(INDEX_ID);
                        int eventCount = -1;
                        int dirtyCount = -1;
                        String displayName = cursor.getString(INDEX_DISPLAY_NAME);
                        String where = Calendar.EventsColumns.CALENDAR_ID + "=" + id;
                        Cursor eventCursor = Calendar.Events.query(mContentResolver,
                                EVENTS_PROJECTION, where, null);
                        try {
                            eventCount = eventCursor.getCount();
                        } finally {
                            eventCursor.close();
                        }
                        String dirtyWhere = Calendar.EventsColumns.CALENDAR_ID + "=" + id
                                + " AND " + Calendar.Events._SYNC_DIRTY + "=1";
                        Cursor dirtyCursor = Calendar.Events.query(mContentResolver,
                                EVENTS_PROJECTION, dirtyWhere, null);
                        try {
                            dirtyCount = dirtyCursor.getCount();
                        } finally {
                            dirtyCursor.close();
                        }
                        String text;
                        if (dirtyCount == 0) {
                            text = mActivity.getString(R.string.calendar_info_events,
                                    eventCount);
                        } else {
                            text = mActivity.getString(R.string.calendar_info_events_dirty,
                                    eventCount, dirtyCount);
                        }
                        addItem(items, displayName, text);
                    }
                }
            } catch (Exception e) {
                addItem(items, mActivity.getString(R.string.calendar_info_error), e.toString());
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            if (items.size() == 0) {
                addItem(items, mActivity.getString(R.string.calendar_info_no_calendars), "");
            }
            return items;
        }
        @Override
        protected void onPostExecute(List<Map<String, String>> items) {
            setProgressBarIndeterminateVisibility(false);
            ListAdapter adapter = new SimpleAdapter(mActivity, items,
                    android.R.layout.simple_list_item_2, new String[]{KEY_TITLE, KEY_TEXT},
                    new int[]{android.R.id.text1, android.R.id.text2});
            setListAdapter(adapter);
        }
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        mActivity = this;
        mContentResolver = getContentResolver();
        getListView(); 
        new FetchInfoTask().execute();
    }
    protected void addItem(List<Map<String, String>> items, String title, String text) {
        Map<String, String> itemMap = new HashMap<String, String>();
        itemMap.put(KEY_TITLE, title);
        itemMap.put(KEY_TEXT, text);
        items.add(itemMap);
    }
}
