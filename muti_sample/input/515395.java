public class ListManagedCursor extends ListActivity implements OnItemClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Cursor c = getContentResolver().query(Settings.System.CONTENT_URI, null, null, null, null);
        startManagingCursor(c);
        ListAdapter adapter = new SimpleCursorAdapter(this, 
                android.R.layout.simple_list_item_1, 
                c, 
                new String[] {People.NAME} ,
                new int[] {android.R.id.text1}); 
        setListAdapter(adapter);
        getListView().setOnItemClickListener(this);
    }
    public void onItemClick(AdapterView parent, View view, int position, long id) {
        Intent dummyIntent = new Intent(this, ListSimple.class);
        startActivity(dummyIntent);
    }
}
