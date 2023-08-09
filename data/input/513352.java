public class QuickContactsDemo extends ListActivity {
    static final String[] CONTACTS_SUMMARY_PROJECTION = new String[] {
            Contacts._ID, 
            Contacts.DISPLAY_NAME, 
            Contacts.STARRED, 
            Contacts.TIMES_CONTACTED, 
            Contacts.CONTACT_PRESENCE, 
            Contacts.PHOTO_ID, 
            Contacts.LOOKUP_KEY, 
            Contacts.HAS_PHONE_NUMBER, 
    };
    static final int SUMMARY_ID_COLUMN_INDEX = 0;
    static final int SUMMARY_NAME_COLUMN_INDEX = 1;
    static final int SUMMARY_STARRED_COLUMN_INDEX = 2;
    static final int SUMMARY_TIMES_CONTACTED_COLUMN_INDEX = 3;
    static final int SUMMARY_PRESENCE_STATUS_COLUMN_INDEX = 4;
    static final int SUMMARY_PHOTO_ID_COLUMN_INDEX = 5;
    static final int SUMMARY_LOOKUP_KEY = 6;
    static final int SUMMARY_HAS_PHONE_COLUMN_INDEX = 7;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String select = "((" + Contacts.DISPLAY_NAME + " NOTNULL) AND ("
                + Contacts.HAS_PHONE_NUMBER + "=1) AND ("
                + Contacts.DISPLAY_NAME + " != '' ))";
        Cursor c =
                getContentResolver().query(Contacts.CONTENT_URI, CONTACTS_SUMMARY_PROJECTION, select,
                null, Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
        startManagingCursor(c);
        ContactListItemAdapter adapter = new ContactListItemAdapter(this, R.layout.quick_contacts, c);
        setListAdapter(adapter);
    }
    private final class ContactListItemAdapter extends ResourceCursorAdapter {
        public ContactListItemAdapter(Context context, int layout, Cursor c) {
            super(context, layout, c);
        }
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final ContactListItemCache cache = (ContactListItemCache) view.getTag();
            TextView nameView = cache.nameView;
            QuickContactBadge photoView = cache.photoView;
            cursor.copyStringToBuffer(SUMMARY_NAME_COLUMN_INDEX, cache.nameBuffer);
            int size = cache.nameBuffer.sizeCopied;
            cache.nameView.setText(cache.nameBuffer.data, 0, size);
            final long contactId = cursor.getLong(SUMMARY_ID_COLUMN_INDEX);
            final String lookupKey = cursor.getString(SUMMARY_LOOKUP_KEY);
            cache.photoView.assignContactUri(Contacts.getLookupUri(contactId, lookupKey));
        }
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = super.newView(context, cursor, parent);
            ContactListItemCache cache = new ContactListItemCache();
            cache.nameView = (TextView) view.findViewById(R.id.name);
            cache.photoView = (QuickContactBadge) view.findViewById(R.id.badge);
            view.setTag(cache);
            return view;
        }
    }
    final static class ContactListItemCache {
        public TextView nameView;
        public QuickContactBadge photoView;
        public CharArrayBuffer nameBuffer = new CharArrayBuffer(128);
    }
}
