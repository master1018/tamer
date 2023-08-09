public class FolderMessageList extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                openInbox();
                return null;
            }
            @Override
            protected void onPostExecute(Void result) {
                finish();
            }
        }.execute();
    }
    private void openInbox() {
        if (UpgradeAccounts.doBulkUpgradeIfNecessary(this)) {
            return;
        }
        Uri uri = getIntent().getData();
        if (uri == null || !"content".equals(uri.getScheme())
                || !"accounts".equals(uri.getAuthority())) {
            return;
        }
        String uuid = uri.getPath();
        if (uuid.length() > 0) {
            uuid = uuid.substring(1); 
        }
        if (TextUtils.isEmpty(uuid)) {
            return;
        }
        MessageList.actionOpenAccountInboxUuid(this, uuid);
    }
}
