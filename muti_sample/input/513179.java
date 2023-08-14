public class Email extends Application {
    public static final String LOG_TAG = "Email";
    public static File tempDirectory;
    public static boolean DEBUG = false;
    public static boolean DEBUG_SENSITIVE = false;
    public static final boolean LOGD = false;
    public static final String[] ACCEPTABLE_ATTACHMENT_SEND_INTENT_TYPES = new String[] {
        "*
    public static final String[] ACCEPTABLE_ATTACHMENT_SEND_UI_TYPES = new String[] {
        "image
    public static final String[] ACCEPTABLE_ATTACHMENT_VIEW_TYPES = new String[] {
        "*
    public static final String[] UNACCEPTABLE_ATTACHMENT_VIEW_TYPES = new String[] {
    };
    public static final String[] ACCEPTABLE_ATTACHMENT_DOWNLOAD_TYPES = new String[] {
        "image
    public static final String[] UNACCEPTABLE_ATTACHMENT_DOWNLOAD_TYPES = new String[] {
    };
    public static final int VISIBLE_LIMIT_DEFAULT = 25;
    public static final int VISIBLE_LIMIT_INCREMENT = 25;
    public static final int MAX_ATTACHMENT_DOWNLOAD_SIZE = (5 * 1024 * 1024);
    public static final int MAX_ATTACHMENT_UPLOAD_SIZE = (5 * 1024 * 1024);
    private static HashMap<Long, Long> sMailboxSyncTimes = new HashMap<Long, Long>();
    private static final long UPDATE_INTERVAL = 5 * DateUtils.MINUTE_IN_MILLIS;
    private static boolean sAccountsChangedNotification = false;
    public static final String EXCHANGE_ACCOUNT_MANAGER_TYPE = "com.android.exchange";
    private static final int[] ACCOUNT_COLOR_CHIP_RES_IDS = new int[] {
        R.drawable.appointment_indicator_leftside_1,
        R.drawable.appointment_indicator_leftside_2,
        R.drawable.appointment_indicator_leftside_3,
        R.drawable.appointment_indicator_leftside_4,
        R.drawable.appointment_indicator_leftside_5,
        R.drawable.appointment_indicator_leftside_6,
        R.drawable.appointment_indicator_leftside_7,
        R.drawable.appointment_indicator_leftside_8,
        R.drawable.appointment_indicator_leftside_9,
    };
    private static final int[] ACCOUNT_COLOR_CHIP_RGBS = new int[] {
        0x71aea7,
        0x621919,
        0x18462f,
        0xbf8e52,
        0x001f79,
        0xa8afc2,
        0x6b64c4,
        0x738359,
        0x9d50a4,
    };
     static int getColorIndexFromAccountId(long accountId) {
        return Math.abs((int) (accountId - 1) % ACCOUNT_COLOR_CHIP_RES_IDS.length);
    }
    public static int getAccountColorResourceId(long accountId) {
        return ACCOUNT_COLOR_CHIP_RES_IDS[getColorIndexFromAccountId(accountId)];
    }
    public static int getAccountColor(long accountId) {
        return ACCOUNT_COLOR_CHIP_RGBS[getColorIndexFromAccountId(accountId)];
    }
    public static boolean setServicesEnabled(Context context) {
        Cursor c = null;
        try {
            c = context.getContentResolver().query(
                    EmailContent.Account.CONTENT_URI,
                    EmailContent.Account.ID_PROJECTION,
                    null, null, null);
            boolean enable = c.getCount() > 0;
            setServicesEnabled(context, enable);
            return enable;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }
    public static void setServicesEnabled(Context context, boolean enabled) {
        PackageManager pm = context.getPackageManager();
        if (!enabled && pm.getComponentEnabledSetting(new ComponentName(context, MailService.class)) ==
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            MailService.actionReschedule(context);
        }
        pm.setComponentEnabledSetting(
                new ComponentName(context, MessageCompose.class),
                enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(
                new ComponentName(context, AccountShortcutPicker.class),
                enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(
                new ComponentName(context, BootReceiver.class),
                enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(
                new ComponentName(context, MailService.class),
                enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        if (enabled && pm.getComponentEnabledSetting(new ComponentName(context, MailService.class)) ==
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            MailService.actionReschedule(context);
        }
    }
    @Override
    public void onCreate() {
        super.onCreate();
        Preferences prefs = Preferences.getPreferences(this);
        DEBUG = prefs.getEnableDebugLogging();
        DEBUG_SENSITIVE = prefs.getEnableSensitiveLogging();
        Controller.getInstance(this).resetVisibleLimits();
        BinaryTempFileBody.setTempDirectory(getCacheDir());
        Debug.updateLoggingFlags(this);
    }
    public static void log(String message) {
        Log.d(LOG_TAG, message);
    }
    public static void updateMailboxRefreshTime(long mailboxId) {
        synchronized (sMailboxSyncTimes) {
            sMailboxSyncTimes.put(mailboxId, System.currentTimeMillis());
        }
    }
    public static boolean mailboxRequiresRefresh(long mailboxId) {
        synchronized (sMailboxSyncTimes) {
            return
                !sMailboxSyncTimes.containsKey(mailboxId)
                || (System.currentTimeMillis() - sMailboxSyncTimes.get(mailboxId)
                        > UPDATE_INTERVAL);
        }
    }
    public static synchronized void setNotifyUiAccountsChanged(boolean setFlag) {
        sAccountsChangedNotification = setFlag;
    }
    public static synchronized boolean getNotifyUiAccountsChanged() {
        return sAccountsChangedNotification;
    }
}
