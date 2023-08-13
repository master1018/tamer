public class AccountShortcutPicker extends ListActivity implements OnItemClickListener {
    private final static String[] sFromColumns = new String[] { 
            EmailContent.AccountColumns.DISPLAY_NAME,
            EmailContent.AccountColumns.EMAIL_ADDRESS,
            EmailContent.RECORD_ID
    };
    private final int[] sToIds = new int[] {
            R.id.description,
            R.id.email,
            R.id.new_message_count
    };
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (!Intent.ACTION_CREATE_SHORTCUT.equals(getIntent().getAction())) {
            finish();
            return;
        }
        Cursor c = this.managedQuery(
                EmailContent.Account.CONTENT_URI, 
                EmailContent.Account.CONTENT_PROJECTION,
                null, null, null);
        if (c.getCount() == 0) {
            finish();
            return;
        }
        setContentView(R.layout.accounts);
        ListView listView = getListView();
        listView.setOnItemClickListener(this);
        listView.setItemsCanFocus(false);
        listView.setEmptyView(findViewById(R.id.empty));
        AccountsAdapter a = new AccountsAdapter(this, 
                R.layout.accounts_item, c, sFromColumns, sToIds);
        listView.setAdapter(a);
    }
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = (Cursor)parent.getItemAtPosition(position);
        Account account = new Account().restore(cursor);
        setupShortcut(account);
        finish();
    }
    private static class AccountsAdapter extends SimpleCursorAdapter {
        public AccountsAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
            super(context, layout, c, from, to);
            setViewBinder(new MyViewBinder());
        }
        private static class MyViewBinder implements SimpleCursorAdapter.ViewBinder {
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (view.getId() == R.id.new_message_count) {
                    int unreadMessageCount = 0;     
                    if (unreadMessageCount <= 0) {
                        view.setVisibility(View.GONE);
                    } else {
                        ((TextView)view).setText(String.valueOf(unreadMessageCount));
                    }
                    return true;
                }
                return false;
            }
        }
    }
    private void setupShortcut(Account account) {
        Intent shortcutIntent = MessageList.createAccountIntentForShortcut(
                this, account, Mailbox.TYPE_INBOX);
        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, account.getDisplayName());
        Parcelable iconResource = Intent.ShortcutIconResource.fromContext(this, R.drawable.icon);
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
        setResult(RESULT_OK, intent);
    }
}
