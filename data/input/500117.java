public class NoteEditor extends Activity {
    private static final String TAG = "Notes";
    private static final String[] PROJECTION = new String[] {
            Notes._ID, 
            Notes.NOTE, 
    };
    private static final int COLUMN_INDEX_NOTE = 1;
    private static final String ORIGINAL_CONTENT = "origContent";
    private static final int REVERT_ID = Menu.FIRST;
    private static final int DISCARD_ID = Menu.FIRST + 1;
    private static final int DELETE_ID = Menu.FIRST + 2;
    private static final int STATE_EDIT = 0;
    private static final int STATE_INSERT = 1;
    private int mState;
    private boolean mNoteOnly = false;
    private Uri mUri;
    private Cursor mCursor;
    private EditText mText;
    private String mOriginalContent;
    public static class LinedEditText extends EditText {
        private Rect mRect;
        private Paint mPaint;
        public LinedEditText(Context context, AttributeSet attrs) {
            super(context, attrs);
            mRect = new Rect();
            mPaint = new Paint();
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(0x800000FF);
        }
        @Override
        protected void onDraw(Canvas canvas) {
            int count = getLineCount();
            Rect r = mRect;
            Paint paint = mPaint;
            for (int i = 0; i < count; i++) {
                int baseline = getLineBounds(i, r);
                canvas.drawLine(r.left, baseline + 1, r.right, baseline + 1, paint);
            }
            super.onDraw(canvas);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();
        final String action = intent.getAction();
        if (Intent.ACTION_EDIT.equals(action)) {
            mState = STATE_EDIT;
            mUri = intent.getData();
        } else if (Intent.ACTION_INSERT.equals(action)) {
            mState = STATE_INSERT;
            mUri = getContentResolver().insert(intent.getData(), null);
            if (mUri == null) {
                Log.e(TAG, "Failed to insert new note into " + getIntent().getData());
                finish();
                return;
            }
            setResult(RESULT_OK, (new Intent()).setAction(mUri.toString()));
        } else {
            Log.e(TAG, "Unknown action, exiting");
            finish();
            return;
        }
        setContentView(R.layout.note_editor);
        mText = (EditText) findViewById(R.id.note);
        mCursor = managedQuery(mUri, PROJECTION, null, null, null);
        if (savedInstanceState != null) {
            mOriginalContent = savedInstanceState.getString(ORIGINAL_CONTENT);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (mCursor != null) {
            mCursor.moveToFirst();
            if (mState == STATE_EDIT) {
                setTitle(getText(R.string.title_edit));
            } else if (mState == STATE_INSERT) {
                setTitle(getText(R.string.title_create));
            }
            String note = mCursor.getString(COLUMN_INDEX_NOTE);
            mText.setTextKeepState(note);
            if (mOriginalContent == null) {
                mOriginalContent = note;
            }
        } else {
            setTitle(getText(R.string.error_title));
            mText.setText(getText(R.string.error_message));
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(ORIGINAL_CONTENT, mOriginalContent);
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (mCursor != null) {
            String text = mText.getText().toString();
            int length = text.length();
            if (isFinishing() && (length == 0) && !mNoteOnly) {
                setResult(RESULT_CANCELED);
                deleteNote();
            } else {
                ContentValues values = new ContentValues();
                if (!mNoteOnly) {
                    values.put(Notes.MODIFIED_DATE, System.currentTimeMillis());
                    if (mState == STATE_INSERT) {
                        String title = text.substring(0, Math.min(30, length));
                        if (length > 30) {
                            int lastSpace = title.lastIndexOf(' ');
                            if (lastSpace > 0) {
                                title = title.substring(0, lastSpace);
                            }
                        }
                        values.put(Notes.TITLE, title);
                    }
                }
                values.put(Notes.NOTE, text);
                getContentResolver().update(mUri, values, null, null);
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if (mState == STATE_EDIT) {
            menu.add(0, REVERT_ID, 0, R.string.menu_revert)
                    .setShortcut('0', 'r')
                    .setIcon(android.R.drawable.ic_menu_revert);
            if (!mNoteOnly) {
                menu.add(0, DELETE_ID, 0, R.string.menu_delete)
                        .setShortcut('1', 'd')
                        .setIcon(android.R.drawable.ic_menu_delete);
            }
        } else {
            menu.add(0, DISCARD_ID, 0, R.string.menu_discard)
                    .setShortcut('0', 'd')
                    .setIcon(android.R.drawable.ic_menu_delete);
        }
        if (!mNoteOnly) {
            Intent intent = new Intent(null, getIntent().getData());
            intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
            menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                    new ComponentName(this, NoteEditor.class), null, intent, 0, null);
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case DELETE_ID:
            deleteNote();
            finish();
            break;
        case DISCARD_ID:
            cancelNote();
            break;
        case REVERT_ID:
            cancelNote();
            break;
        }
        return super.onOptionsItemSelected(item);
    }
    private final void cancelNote() {
        if (mCursor != null) {
            if (mState == STATE_EDIT) {
                mCursor.close();
                mCursor = null;
                ContentValues values = new ContentValues();
                values.put(Notes.NOTE, mOriginalContent);
                getContentResolver().update(mUri, values, null, null);
            } else if (mState == STATE_INSERT) {
                deleteNote();
            }
        }
        setResult(RESULT_CANCELED);
        finish();
    }
    private final void deleteNote() {
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
            getContentResolver().delete(mUri, null, null);
            mText.setText("");
        }
    }
}
