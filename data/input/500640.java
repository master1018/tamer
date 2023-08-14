public class Welcome extends Activity {
    public static void actionStart(Activity fromActivity) {
        Intent i = new Intent(fromActivity, Welcome.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        fromActivity.startActivity(i);
    }
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Email.setNotifyUiAccountsChanged(false);
        if (UpgradeAccounts.doBulkUpgradeIfNecessary(this)) {
            finish();
            return;
        }
        AccountBackupRestore.restoreAccountsIfNeeded(this);
        ExchangeUtils.startExchangeService(this);
        Cursor c = null;
        try {
            c = getContentResolver().query(
                    EmailContent.Account.CONTENT_URI,
                    EmailContent.Account.ID_PROJECTION,
                    null, null, null);
            switch (c.getCount()) {
                case 0:
                    AccountSetupBasics.actionNewAccount(this);
                    break;
                case 1:
                    c.moveToFirst();
                    long accountId = c.getLong(EmailContent.Account.CONTENT_ID_COLUMN);
                    MessageList.actionHandleAccount(this, accountId, Mailbox.TYPE_INBOX);
                    break;
                default:
                    AccountFolderList.actionShowAccounts(this);
                    break;
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        finish();
    }
}
