public class TitleEditor extends Activity implements View.OnClickListener {
    public static final String EDIT_TITLE_ACTION = "com.android.notepad.action.EDIT_TITLE";
    private static final String[] PROJECTION = new String[] {
            NotePad.Notes._ID, 
            NotePad.Notes.TITLE, 
    };
    private static final int COLUMN_INDEX_TITLE = 1;
    private Cursor mCursor;
    private EditText mText;
    private Uri mUri;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.title_editor);
        mUri = getIntent().getData();
        mCursor = managedQuery(mUri, PROJECTION, null, null, null);
        mText = (EditText) this.findViewById(R.id.title);
        mText.setOnClickListener(this);
        Button b = (Button) findViewById(R.id.ok);
        b.setOnClickListener(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (mCursor != null) {
            mCursor.moveToFirst();
            mText.setText(mCursor.getString(COLUMN_INDEX_TITLE));
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (mCursor != null) {
            ContentValues values = new ContentValues();
            values.put(Notes.TITLE, mText.getText().toString());
            getContentResolver().update(mUri, values, null, null);
        }
    }
    public void onClick(View v) {
        finish();
    }
}
