public class DataList extends ListActivity
{
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        Intent intent = getIntent();
        mCursor = getContentResolver().query(intent.getData(), null, null, null, null);
        mDisplay = intent.getStringExtra("display");
        if (mDisplay == null) {
            mDisplay = "_id";
        }
        if (mCursor != null) {
            setListAdapter(new SimpleCursorAdapter(
                    this,
                    R.layout.url_list,
                    mCursor,
                    new String[] {mDisplay},
                    new int[] {android.R.id.text1}));
        }
    }
    public void onStop()
    {
        super.onStop();
        if (mCursor != null) {
            mCursor.deactivate();
        }
    }
    public void onResume()
    {
        super.onResume();
        if (mCursor != null) {
            mCursor.requery();
        }
        setTitle("Showing " + mDisplay);
    }
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        menu.add(0, 0, 0, "Requery").setOnMenuItemClickListener(mRequery);
        return true;
    }
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
        mCursor.moveToPosition(position);
        ArrayList<ColumnData> data = new ArrayList<ColumnData>();
        String[] columnNames = mCursor.getColumnNames();
        for (int i=0; i<columnNames.length; i++) {
            String str = mCursor.getString(i);
            ColumnData cd = new ColumnData(columnNames[i], str);
            data.add(cd);
        }
        Uri uri = null;
        int idCol = mCursor.getColumnIndex("_id");
        if (idCol >= 0) {
            uri = Uri.withAppendedPath(getIntent().getData(), mCursor.getString(idCol));
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setClass(this, Details.class);
        intent.putExtra("data", data);
        int displayColumn = mCursor.getColumnIndex(mDisplay);
        if (displayColumn >= 0) {
            intent.putExtra("title",
                                ((ColumnData)data.get(displayColumn)).value);
        }
        startActivity(intent);
    }
    MenuItem.OnMenuItemClickListener mRequery = new MenuItem.OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            mCursor.requery();
            if (mCursor != null) {
                setListAdapter(new SimpleCursorAdapter(
                        DataList.this,
                        R.layout.url_list,
                        mCursor,
                        new String[] {mDisplay},
                        new int[] {android.R.id.text1}));
            }
            return true;
        }
    };
    private String mDisplay;
    private Cursor mCursor;
}
