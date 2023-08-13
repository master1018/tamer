public class List2 extends ListActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Cursor c = getContentResolver().query(People.CONTENT_URI, null, null, null, null);
        startManagingCursor(c);
        ListAdapter adapter = new SimpleCursorAdapter(this, 
                android.R.layout.simple_list_item_1, 
                c, 
                new String[] {People.NAME} ,
                new int[] {android.R.id.text1}); 
        setListAdapter(adapter);
    }
}
