public class ExpandableList2 extends ExpandableListActivity {
    private int mGroupIdColumnIndex; 
    private String mPhoneNumberProjection[] = new String[] {
            People.Phones._ID, People.Phones.NUMBER
    };
    private ExpandableListAdapter mAdapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Cursor groupCursor = managedQuery(People.CONTENT_URI,
                new String[] {People._ID, People.NAME}, null, null, null);
        mGroupIdColumnIndex = groupCursor.getColumnIndexOrThrow(People._ID);
        mAdapter = new MyExpandableListAdapter(groupCursor,
                this,
                android.R.layout.simple_expandable_list_item_1,
                android.R.layout.simple_expandable_list_item_1,
                new String[] {People.NAME}, 
                new int[] {android.R.id.text1},
                new String[] {People.NUMBER}, 
                new int[] {android.R.id.text1});
        setListAdapter(mAdapter);
    }
    public class MyExpandableListAdapter extends SimpleCursorTreeAdapter {
        public MyExpandableListAdapter(Cursor cursor, Context context, int groupLayout,
                int childLayout, String[] groupFrom, int[] groupTo, String[] childrenFrom,
                int[] childrenTo) {
            super(context, cursor, groupLayout, groupFrom, groupTo, childLayout, childrenFrom,
                    childrenTo);
        }
        @Override
        protected Cursor getChildrenCursor(Cursor groupCursor) {
            Uri.Builder builder = People.CONTENT_URI.buildUpon();
            ContentUris.appendId(builder, groupCursor.getLong(mGroupIdColumnIndex));
            builder.appendEncodedPath(People.Phones.CONTENT_DIRECTORY);
            Uri phoneNumbersUri = builder.build();
            return managedQuery(phoneNumbersUri, mPhoneNumberProjection, null, null, null);
        }
    }
}
