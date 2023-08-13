public class Gallery2 extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_2);
        Cursor c = getContentResolver().query(People.CONTENT_URI, null, null, null, null);
        startManagingCursor(c);
        SpinnerAdapter adapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_gallery_item,
                c,
                new String[] {People.NAME},
                new int[] { android.R.id.text1 });
        Gallery g = (Gallery) findViewById(R.id.gallery);
        g.setAdapter(adapter);
    }
}
