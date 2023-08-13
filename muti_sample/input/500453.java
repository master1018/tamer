public class Preferences {
    private static final String PREFERENCES_FILE = "AndroidMail.Main";
    private static final String ACCOUNT_UUIDS = "accountUuids";
    private static final String DEFAULT_ACCOUNT_UUID = "defaultAccountUuid";
    private static final String ENABLE_DEBUG_LOGGING = "enableDebugLogging";
    private static final String ENABLE_SENSITIVE_LOGGING = "enableSensitiveLogging";
    private static final String ENABLE_EXCHANGE_LOGGING = "enableExchangeLogging";
    private static final String ENABLE_EXCHANGE_FILE_LOGGING = "enableExchangeFileLogging";
    private static final String DEVICE_UID = "deviceUID";
    private static final String ONE_TIME_INITIALIZATION_PROGRESS = "oneTimeInitializationProgress";
    private static Preferences preferences;
    SharedPreferences mSharedPreferences;
    private Preferences(Context context) {
        mSharedPreferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
    }
    public static synchronized Preferences getPreferences(Context context) {
        if (preferences == null) {
            preferences = new Preferences(context);
        }
        return preferences;
    }
    public Account[] getAccounts() {
        String accountUuids = mSharedPreferences.getString(ACCOUNT_UUIDS, null);
        if (accountUuids == null || accountUuids.length() == 0) {
            return new Account[] {};
        }
        String[] uuids = accountUuids.split(",");
        Account[] accounts = new Account[uuids.length];
        for (int i = 0, length = uuids.length; i < length; i++) {
            accounts[i] = new Account(this, uuids[i]);
        }
        return accounts;
    }
    public Account getAccountByContentUri(Uri uri) {
        if (!"content".equals(uri.getScheme()) || !"accounts".equals(uri.getAuthority())) {
            return null;
        }
        String uuid = uri.getPath().substring(1);
        if (uuid == null) {
            return null;
        }
        String accountUuids = mSharedPreferences.getString(ACCOUNT_UUIDS, null);
        if (accountUuids == null || accountUuids.length() == 0) {
            return null;
        }
        String[] uuids = accountUuids.split(",");
        for (int i = 0, length = uuids.length; i < length; i++) {
            if (uuid.equals(uuids[i])) {
                return new Account(this, uuid);
            }
        }
        return null;
    }
    public Account getDefaultAccount() {
        String defaultAccountUuid = mSharedPreferences.getString(DEFAULT_ACCOUNT_UUID, null);
        Account defaultAccount = null;
        Account[] accounts = getAccounts();
        if (defaultAccountUuid != null) {
            for (Account account : accounts) {
                if (account.getUuid().equals(defaultAccountUuid)) {
                    defaultAccount = account;
                    break;
                }
            }
        }
        if (defaultAccount == null) {
            if (accounts.length > 0) {
                defaultAccount = accounts[0];
                setDefaultAccount(defaultAccount);
            }
        }
        return defaultAccount;
    }
    public void setDefaultAccount(Account account) {
        mSharedPreferences.edit().putString(DEFAULT_ACCOUNT_UUID, account.getUuid()).commit();
    }
    public void setEnableDebugLogging(boolean value) {
        mSharedPreferences.edit().putBoolean(ENABLE_DEBUG_LOGGING, value).commit();
    }
    public boolean getEnableDebugLogging() {
        return mSharedPreferences.getBoolean(ENABLE_DEBUG_LOGGING, false);
    }
    public void setEnableSensitiveLogging(boolean value) {
        mSharedPreferences.edit().putBoolean(ENABLE_SENSITIVE_LOGGING, value).commit();
    }
    public boolean getEnableSensitiveLogging() {
        return mSharedPreferences.getBoolean(ENABLE_SENSITIVE_LOGGING, false);
    }
    public void setEnableExchangeLogging(boolean value) {
        mSharedPreferences.edit().putBoolean(ENABLE_EXCHANGE_LOGGING, value).commit();
    }
    public boolean getEnableExchangeLogging() {
        return mSharedPreferences.getBoolean(ENABLE_EXCHANGE_LOGGING, false);
    }
    public void setEnableExchangeFileLogging(boolean value) {
        mSharedPreferences.edit().putBoolean(ENABLE_EXCHANGE_FILE_LOGGING, value).commit();
    }
    public boolean getEnableExchangeFileLogging() {
        return mSharedPreferences.getBoolean(ENABLE_EXCHANGE_FILE_LOGGING, false);
    }
    public synchronized String getDeviceUID() {
         String result = mSharedPreferences.getString(DEVICE_UID, null);
         if (result == null) {
             result = UUID.randomUUID().toString();
             mSharedPreferences.edit().putString(DEVICE_UID, result).commit();
         }
         return result;
    }
    public int getOneTimeInitializationProgress() {
        return mSharedPreferences.getInt(ONE_TIME_INITIALIZATION_PROGRESS, 0);
    }
    public void setOneTimeInitializationProgress(int progress) {
        mSharedPreferences.edit().putInt(ONE_TIME_INITIALIZATION_PROGRESS, progress).commit();
    }
    public void save() {
    }
    public void clear() {
        mSharedPreferences.edit().clear().commit();
    }
    public void dump() {
        if (Email.LOGD) {
            for (String key : mSharedPreferences.getAll().keySet()) {
                Log.v(Email.LOG_TAG, key + " = " + mSharedPreferences.getAll().get(key));
            }
        }
    }
}
