public class EasAuthenticatorService extends Service {
    public static final String OPTIONS_USERNAME = "username";
    public static final String OPTIONS_PASSWORD = "password";
    public static final String OPTIONS_CONTACTS_SYNC_ENABLED = "contacts";
    public static final String OPTIONS_CALENDAR_SYNC_ENABLED = "calendar";
    class EasAuthenticator extends AbstractAccountAuthenticator {
        public EasAuthenticator(Context context) {
            super(context);
        }
        @Override
        public Bundle addAccount(AccountAuthenticatorResponse response, String accountType,
                String authTokenType, String[] requiredFeatures, Bundle options)
                throws NetworkErrorException {
            if (options != null && options.containsKey(OPTIONS_PASSWORD)
                    && options.containsKey(OPTIONS_USERNAME)) {
                final Account account = new Account(options.getString(OPTIONS_USERNAME),
                        Email.EXCHANGE_ACCOUNT_MANAGER_TYPE);
                AccountManager.get(EasAuthenticatorService.this).addAccountExplicitly(
                            account, options.getString(OPTIONS_PASSWORD), null);
                boolean syncContacts = false;
                if (options.containsKey(OPTIONS_CONTACTS_SYNC_ENABLED) &&
                        options.getBoolean(OPTIONS_CONTACTS_SYNC_ENABLED)) {
                    syncContacts = true;
                }
                ContentResolver.setIsSyncable(account, ContactsContract.AUTHORITY, 1);
                ContentResolver.setSyncAutomatically(account, ContactsContract.AUTHORITY,
                        syncContacts);
                boolean syncCalendar = false;
                if (options.containsKey(OPTIONS_CALENDAR_SYNC_ENABLED) &&
                        options.getBoolean(OPTIONS_CALENDAR_SYNC_ENABLED)) {
                    syncCalendar = true;
                }
                ContentResolver.setIsSyncable(account, Calendar.AUTHORITY, 1);
                ContentResolver.setSyncAutomatically(account, Calendar.AUTHORITY, syncCalendar);
                Bundle b = new Bundle();
                b.putString(AccountManager.KEY_ACCOUNT_NAME, options.getString(OPTIONS_USERNAME));
                b.putString(AccountManager.KEY_ACCOUNT_TYPE, Email.EXCHANGE_ACCOUNT_MANAGER_TYPE);
                return b;
            } else {
                Bundle b = new Bundle();
                Intent intent =
                    AccountSetupBasics.actionSetupExchangeIntent(EasAuthenticatorService.this);
                intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
                b.putParcelable(AccountManager.KEY_INTENT, intent);
                return b;
            }
        }
        @Override
        public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account,
                Bundle options) {
            return null;
        }
        @Override
        public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
            return null;
        }
        @Override
        public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account,
                String authTokenType, Bundle loginOptions) throws NetworkErrorException {
            return null;
        }
        @Override
        public String getAuthTokenLabel(String authTokenType) {
            return null;
        }
        @Override
        public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account,
                String[] features) throws NetworkErrorException {
            return null;
        }
        @Override
        public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account,
                String authTokenType, Bundle loginOptions) {
            return null;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        String authenticatorIntent = "android.accounts.AccountAuthenticator";
        if (authenticatorIntent.equals(intent.getAction())) {
            return new EasAuthenticator(this).getIBinder();
        } else {
            return null;
        }
    }
}
