public class List7 extends ListActivity implements OnItemSelectedListener {
    private static final String[] PROJECTION = new String[] {
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.HAS_PHONE_NUMBER,
            ContactsContract.Contacts.LOOKUP_KEY
    };
    private int mIdColumnIndex;
    private int mHasPhoneColumnIndex;
    private TextView mPhone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_7);
        mPhone = (TextView) findViewById(R.id.phone);
        getListView().setOnItemSelectedListener(this);
        Cursor c = managedQuery(ContactsContract.Contacts.CONTENT_URI,
                PROJECTION, null, null, null);
        mIdColumnIndex = c.getColumnIndex(ContactsContract.Contacts._ID);
        mHasPhoneColumnIndex = c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
        ListAdapter adapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_1, 
                c, 
                new String[] { ContactsContract.Contacts.DISPLAY_NAME }, 
                new int[] { android.R.id.text1 }); 
        setListAdapter(adapter);
    }
    public void onItemSelected(AdapterView parent, View v, int position, long id) {
        if (position >= 0) {
            final Cursor c = (Cursor) parent.getItemAtPosition(position);
            if (c.getInt(mHasPhoneColumnIndex) > 0) {
                final long contactId = c.getLong(mIdColumnIndex);
                final Cursor phones = getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER },
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId, null,
                        ContactsContract.CommonDataKinds.Phone.IS_SUPER_PRIMARY + " DESC");
                try {
                    phones.moveToFirst();
                    mPhone.setText(phones.getString(0));
                } finally {
                    phones.close();
                }
            } else {
                mPhone.setText(R.string.list_7_nothing);                
            }
        }
    }
    public void onNothingSelected(AdapterView parent) {
        mPhone.setText(R.string.list_7_nothing);
    }
}
