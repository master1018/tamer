public class ContactListFilterView extends LinearLayout {
    private ListView mContactListView;
    private Filter mFilter;
    private ContactAdapter mContactAdapter;
    private Uri mUri;
    public ContactListFilterView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    protected void onFinishInflate() {
        mContactListView = (ListView) findViewById(R.id.filteredList);
        mContactListView.setTextFilterEnabled(true);
        mContactListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView parent, View view, int position,
                    long id) {
                if (mContext instanceof ContactListActivity) {
                    ContactListActivity list = (ContactListActivity) mContext;
                    mContactListView.setSelection(position);
                    Cursor c = (Cursor) mContactListView.getSelectedItem();
                    list.mContactListView.startChat(c);
                    list.showContactListView();
                }
            }
        });
    }
    public ListView getListView() {
        return mContactListView;
    }
    public Cursor getContactAtPosition(int position) {
        return (Cursor) mContactAdapter.getItem(position);
    }
    public void doFilter(Uri uri, String filterString) {
        if (!uri.equals(mUri)) {
            mUri = uri;
            Cursor contactCursor = runQuery(filterString);
            if (mContactAdapter == null) {
                mContactAdapter = new ContactAdapter(mContext, contactCursor);
                mFilter = mContactAdapter.getFilter();
                mContactListView.setAdapter(mContactAdapter);
            } else {
                mContactAdapter.changeCursor(contactCursor);
            }
        } else {
            mFilter.filter(filterString);
        }
    }
    Cursor runQuery(CharSequence constraint) {
        StringBuilder buf = new StringBuilder();
        buf.append(Imps.Chats.LAST_MESSAGE_DATE);
        buf.append(" IS NULL");
        if (constraint != null) {
            buf.append(" AND ");
            buf.append(Imps.Contacts.NICKNAME);
            buf.append(" LIKE ");
            DatabaseUtils.appendValueToSql(buf, "%" + constraint + "%");
        }
        return mContext.getContentResolver().query(mUri, ContactView.CONTACT_PROJECTION,
                buf == null ? null : buf.toString(), null, Imps.Contacts.DEFAULT_SORT_ORDER);
    }
    private class ContactAdapter extends ResourceCursorAdapter {
        private String mSearchString;
        public ContactAdapter(Context context, Cursor cursor) {
            super(context, R.layout.contact_view, cursor);
        }
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ContactView v = (ContactView) view;
            v.setPadding(0, 0, 0, 0);
            v.bind(cursor, mSearchString, false);
        }
        @Override
        public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
            if (constraint != null) {
                mSearchString = constraint.toString();
            }
            return ContactListFilterView.this.runQuery(constraint);
        }
    }
}
