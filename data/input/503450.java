public class SyncActivityTooManyDeletes extends Activity
        implements AdapterView.OnItemClickListener {
    private long mNumDeletes;
    private Account mAccount;
    private String mAuthority;
    private String mProvider;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            finish();
            return;
        }
        mNumDeletes = extras.getLong("numDeletes");
        mAccount = (Account) extras.getParcelable("account");
        mAuthority = extras.getString("authority");
        mProvider = extras.getString("provider");
        CharSequence[] options = new CharSequence[]{
                getResources().getText(R.string.sync_really_delete),
                getResources().getText(R.string.sync_undo_deletes),
                getResources().getText(R.string.sync_do_nothing)
        };
        ListAdapter adapter = new ArrayAdapter<CharSequence>(this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                options);
        ListView listView = new ListView(this);
        listView.setAdapter(adapter);
        listView.setItemsCanFocus(true);
        listView.setOnItemClickListener(this);
        TextView textView = new TextView(this);
        CharSequence tooManyDeletesDescFormat =
                getResources().getText(R.string.sync_too_many_deletes_desc);
        textView.setText(String.format(tooManyDeletesDescFormat.toString(),
                mNumDeletes, mProvider, mAccount.name));
        final LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0);
        ll.addView(textView, lp);
        ll.addView(listView, lp);
        setContentView(ll);
    }
    public void onItemClick(AdapterView parent, View view, int position, long id) {
        if (position == 0) startSyncReallyDelete();
        else if (position == 1) startSyncUndoDeletes();
        finish();
    }
    private void startSyncReallyDelete() {
        Bundle extras = new Bundle();
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_OVERRIDE_TOO_MANY_DELETIONS, true);
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_UPLOAD, true);
        ContentResolver.requestSync(mAccount, mAuthority, extras);
    }
    private void startSyncUndoDeletes() {
        Bundle extras = new Bundle();
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_DISCARD_LOCAL_DELETIONS, true);
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_UPLOAD, true);
        ContentResolver.requestSync(mAccount, mAuthority, extras);
    }
}
