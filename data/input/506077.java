public class MailService extends Service {
    private static final boolean DEBUG_FORCE_QUICK_REFRESH = false;     
    private static final String LOG_TAG = "Email-MailService";
    public static int NOTIFICATION_ID_NEW_MESSAGES = 1;
    public static int NOTIFICATION_ID_SECURITY_NEEDED = 2;
    public static int NOTIFICATION_ID_EXCHANGE_CALENDAR_ADDED = 3;
    private static final String ACTION_CHECK_MAIL =
        "com.android.email.intent.action.MAIL_SERVICE_WAKEUP";
    private static final String ACTION_RESCHEDULE =
        "com.android.email.intent.action.MAIL_SERVICE_RESCHEDULE";
    private static final String ACTION_CANCEL =
        "com.android.email.intent.action.MAIL_SERVICE_CANCEL";
    private static final String ACTION_NOTIFY_MAIL =
        "com.android.email.intent.action.MAIL_SERVICE_NOTIFY";
    private static final String EXTRA_CHECK_ACCOUNT = "com.android.email.intent.extra.ACCOUNT";
    private static final String EXTRA_ACCOUNT_INFO = "com.android.email.intent.extra.ACCOUNT_INFO";
    private static final String EXTRA_DEBUG_WATCHDOG = "com.android.email.intent.extra.WATCHDOG";
    private static final int WATCHDOG_DELAY = 10 * 60 * 1000;   
    private static final String[] NEW_MESSAGE_COUNT_PROJECTION =
        new String[] {AccountColumns.NEW_MESSAGE_COUNT};
    private Controller.Result mControllerCallback = new ControllerResults();
    private int mStartId;
    private static HashMap<Long,AccountSyncReport> mSyncReports =
        new HashMap<Long,AccountSyncReport>();
    static ContentValues mClearNewMessages;
    static {
        mClearNewMessages = new ContentValues();
        mClearNewMessages.put(Account.NEW_MESSAGE_COUNT, 0);
    }
    public static void actionReschedule(Context context) {
        Intent i = new Intent();
        i.setClass(context, MailService.class);
        i.setAction(MailService.ACTION_RESCHEDULE);
        context.startService(i);
    }
    public static void actionCancel(Context context)  {
        Intent i = new Intent();
        i.setClass(context, MailService.class);
        i.setAction(MailService.ACTION_CANCEL);
        context.startService(i);
    }
    public static void resetNewMessageCount(Context context, long accountId) {
        synchronized (mSyncReports) {
            for (AccountSyncReport report : mSyncReports.values()) {
                if (accountId == -1 || accountId == report.accountId) {
                    report.numNewMessages = 0;
                }
            }
        }
        Uri uri;
        if (accountId == -1) {
            uri = Account.CONTENT_URI;
        } else {
            uri = ContentUris.withAppendedId(Account.CONTENT_URI, accountId);
        }
        context.getContentResolver().update(uri, mClearNewMessages, null, null);
    }
    public static void actionNotifyNewMessages(Context context, long accountId) {
        Intent i = new Intent(ACTION_NOTIFY_MAIL);
        i.setClass(context, MailService.class);
        i.putExtra(EXTRA_CHECK_ACCOUNT, accountId);
        context.startService(i);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        AccountBackupRestore.restoreAccountsIfNeeded(this);
        this.mStartId = startId;
        String action = intent.getAction();
        Controller controller = Controller.getInstance(getApplication());
        controller.addResultCallback(mControllerCallback);
        if (ACTION_CHECK_MAIL.equals(action)) {
            restoreSyncReports(intent);
            AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
            long checkAccountId = intent.getLongExtra(EXTRA_CHECK_ACCOUNT, -1);
            if (Config.LOGD && Email.DEBUG) {
                Log.d(LOG_TAG, "action: check mail for id=" + checkAccountId);
            }
            if (checkAccountId >= 0) {
                setWatchdog(checkAccountId, alarmManager);
            }
            if (checkAccountId == -1 || !syncOneAccount(controller, checkAccountId, startId)) {
                if (checkAccountId != -1) {
                    updateAccountReport(checkAccountId, 0);
                }
                reschedule(alarmManager);
                stopSelf(startId);
            }
        }
        else if (ACTION_CANCEL.equals(action)) {
            if (Config.LOGD && Email.DEBUG) {
                Log.d(LOG_TAG, "action: cancel");
            }
            cancel();
            stopSelf(startId);
        }
        else if (ACTION_RESCHEDULE.equals(action)) {
            if (Config.LOGD && Email.DEBUG) {
                Log.d(LOG_TAG, "action: reschedule");
            }
            NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(NOTIFICATION_ID_NEW_MESSAGES);
            refreshSyncReports();
            AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
            reschedule(alarmManager);
            stopSelf(startId);
        } else if (ACTION_NOTIFY_MAIL.equals(action)) {
            long accountId = intent.getLongExtra(EXTRA_CHECK_ACCOUNT, -1);
            Cursor c = getContentResolver().query(
                    ContentUris.withAppendedId(Account.CONTENT_URI, accountId),
                    NEW_MESSAGE_COUNT_PROJECTION, null, null, null);
            int newMessageCount = 0;
            try {
                if (c.moveToFirst()) {
                    newMessageCount = c.getInt(0);
                } else {
                    accountId = -1;
                }
            } finally {
                c.close();
            }
            if (Config.LOGD && Email.DEBUG) {
                Log.d(LOG_TAG, "notify accountId=" + Long.toString(accountId)
                        + " count=" + newMessageCount);
            }
            if (accountId != -1) {
                updateAccountReport(accountId, newMessageCount);
                notifyNewMessages(accountId);
            }
            stopSelf(startId);
        }
        return START_NOT_STICKY;
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Controller.getInstance(getApplication()).removeResultCallback(mControllerCallback);
    }
    private void cancel() {
        AlarmManager alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = createAlarmIntent(-1, null, false);
        alarmMgr.cancel(pi);
    }
    private void refreshSyncReports() {
        synchronized (mSyncReports) {
            HashMap<Long,AccountSyncReport> oldSyncReports =
                new HashMap<Long,AccountSyncReport>(mSyncReports);
            mSyncReports.clear();
            setupSyncReportsLocked(-1);
            for (AccountSyncReport newReport : mSyncReports.values()) {
                AccountSyncReport oldReport = oldSyncReports.get(newReport.accountId);
                if (oldReport != null) {
                    newReport.prevSyncTime = oldReport.prevSyncTime;
                    if (newReport.syncInterval > 0 && newReport.prevSyncTime != 0) {
                        newReport.nextSyncTime =
                            newReport.prevSyncTime + (newReport.syncInterval * 1000 * 60);
                    }
                }
            }
        }
    }
     void reschedule(AlarmManager alarmMgr) {
        setupSyncReports(-1);
        synchronized (mSyncReports) {
            int numAccounts = mSyncReports.size();
            long[] accountInfo = new long[numAccounts * 2];     
            int accountInfoIndex = 0;
            long nextCheckTime = Long.MAX_VALUE;
            AccountSyncReport nextAccount = null;
            long timeNow = SystemClock.elapsedRealtime();
            for (AccountSyncReport report : mSyncReports.values()) {
                if (report.syncInterval <= 0) {                         
                    continue;
                }
                long prevSyncTime = report.prevSyncTime;
                long nextSyncTime = report.nextSyncTime;
                if ((prevSyncTime == 0) || (nextSyncTime < timeNow)) {  
                    nextCheckTime = 0;
                    nextAccount = report;
                } else if (nextSyncTime < nextCheckTime) {              
                    nextCheckTime = nextSyncTime;
                    nextAccount = report;
                }
                accountInfo[accountInfoIndex++] = report.accountId;
                accountInfo[accountInfoIndex++] = report.prevSyncTime;
            }
            while (accountInfoIndex < accountInfo.length) {
                accountInfo[accountInfoIndex++] = -1;
            }
            long idToCheck = (nextAccount == null) ? -1 : nextAccount.accountId;
            PendingIntent pi = createAlarmIntent(idToCheck, accountInfo, false);
            if (nextAccount == null) {
                alarmMgr.cancel(pi);
                if (Config.LOGD && Email.DEBUG) {
                    Log.d(LOG_TAG, "reschedule: alarm cancel - no account to check");
                }
            } else {
                alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, nextCheckTime, pi);
                if (Config.LOGD && Email.DEBUG) {
                    Log.d(LOG_TAG, "reschedule: alarm set at " + nextCheckTime
                            + " for " + nextAccount);
                }
            }
        }
    }
    private void setWatchdog(long accountId, AlarmManager alarmMgr) {
        PendingIntent pi = createAlarmIntent(accountId, null, true);
        long timeNow = SystemClock.elapsedRealtime();
        long nextCheckTime = timeNow + WATCHDOG_DELAY;
        alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, nextCheckTime, pi);
    }
     PendingIntent createAlarmIntent(long checkId, long[] accountInfo,
            boolean isWatchdog) {
        Intent i = new Intent();
        i.setClass(this, MailService.class);
        i.setAction(ACTION_CHECK_MAIL);
        i.putExtra(EXTRA_CHECK_ACCOUNT, checkId);
        i.putExtra(EXTRA_ACCOUNT_INFO, accountInfo);
        if (isWatchdog) {
            i.putExtra(EXTRA_DEBUG_WATCHDOG, true);
        }
        PendingIntent pi = PendingIntent.getService(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        return pi;
    }
    private boolean syncOneAccount(Controller controller, long checkAccountId, int startId) {
        long inboxId = Mailbox.findMailboxOfType(this, checkAccountId, Mailbox.TYPE_INBOX);
        if (inboxId == Mailbox.NO_MAILBOX) {
            return false;
        } else {
            controller.serviceCheckMail(checkAccountId, inboxId, startId, mControllerCallback);
            return true;
        }
    }
    private static class AccountSyncReport {
        long accountId;
        long prevSyncTime;      
        long nextSyncTime;      
        int numNewMessages;
        int syncInterval;
        boolean notify;
        boolean vibrate;
        boolean vibrateWhenSilent;
        Uri ringtoneUri;
        String displayName;     
        @Override
        public String toString() {
            return displayName + ": id=" + accountId + " prevSync=" + prevSyncTime
                    + " nextSync=" + nextSyncTime + " numNew=" + numNewMessages;
        }
    }
     void setupSyncReports(long accountId) {
        synchronized (mSyncReports) {
            setupSyncReportsLocked(accountId);
        }
    }
    private void setupSyncReportsLocked(long accountId) {
        if (accountId == -1) {
            if (mSyncReports.size() > 0) {
                return;
            }
        } else {
            if (mSyncReports.containsKey(accountId)) {
                return;
            }
        }
        Uri uri;
        if (accountId == -1) {
            uri = Account.CONTENT_URI;
        } else {
            uri = ContentUris.withAppendedId(Account.CONTENT_URI, accountId);
        }
        Cursor c = getContentResolver().query(uri, Account.CONTENT_PROJECTION,
                null, null, null);
        try {
            while (c.moveToNext()) {
                AccountSyncReport report = new AccountSyncReport();
                int syncInterval = c.getInt(Account.CONTENT_SYNC_INTERVAL_COLUMN);
                int flags = c.getInt(Account.CONTENT_FLAGS_COLUMN);
                String ringtoneString = c.getString(Account.CONTENT_RINGTONE_URI_COLUMN);
                if (DEBUG_FORCE_QUICK_REFRESH && syncInterval >= 0) {
                    syncInterval = 1;
                }
                report.accountId = c.getLong(Account.CONTENT_ID_COLUMN);
                report.prevSyncTime = 0;
                report.nextSyncTime = (syncInterval > 0) ? 0 : -1;  
                report.numNewMessages = 0;
                report.syncInterval = syncInterval;
                report.notify = (flags & Account.FLAGS_NOTIFY_NEW_MAIL) != 0;
                report.vibrate = (flags & Account.FLAGS_VIBRATE_ALWAYS) != 0;
                report.vibrateWhenSilent = (flags & Account.FLAGS_VIBRATE_WHEN_SILENT) != 0;
                report.ringtoneUri = (ringtoneString == null) ? null
                        : Uri.parse(ringtoneString);
                report.displayName = c.getString(Account.CONTENT_DISPLAY_NAME_COLUMN);
                mSyncReports.put(report.accountId, report);
            }
        } finally {
            c.close();
        }
    }
     AccountSyncReport updateAccountReport(long accountId, int newCount) {
        setupSyncReports(accountId);
        synchronized (mSyncReports) {
            AccountSyncReport report = mSyncReports.get(accountId);
            if (report == null) {
                Log.d(LOG_TAG, "No account to update for id=" + Long.toString(accountId));
                return null;
            }
            report.prevSyncTime = SystemClock.elapsedRealtime();
            if (report.syncInterval > 0) {
                report.nextSyncTime = report.prevSyncTime + (report.syncInterval * 1000 * 60);
            }
            if (newCount != -1) {
                report.numNewMessages = newCount;
            }
            if (Config.LOGD && Email.DEBUG) {
                Log.d(LOG_TAG, "update account " + report.toString());
            }
            return report;
        }
    }
     void restoreSyncReports(Intent restoreIntent) {
        setupSyncReports(-1);
        synchronized (mSyncReports) {
            long[] accountInfo = restoreIntent.getLongArrayExtra(EXTRA_ACCOUNT_INFO);
            if (accountInfo == null) {
                Log.d(LOG_TAG, "no data in intent to restore");
                return;
            }
            int accountInfoIndex = 0;
            int accountInfoLimit = accountInfo.length;
            while (accountInfoIndex < accountInfoLimit) {
                long accountId = accountInfo[accountInfoIndex++];
                long prevSync = accountInfo[accountInfoIndex++];
                AccountSyncReport report = mSyncReports.get(accountId);
                if (report != null) {
                    if (report.prevSyncTime == 0) {
                        report.prevSyncTime = prevSync;
                        if (report.syncInterval > 0 && report.prevSyncTime != 0) {
                            report.nextSyncTime =
                                report.prevSyncTime + (report.syncInterval * 1000 * 60);
                        }
                    }
                }
            }
        }
    }
    class ControllerResults implements Controller.Result {
        public void loadMessageForViewCallback(MessagingException result, long messageId,
                int progress) {
        }
        public void loadAttachmentCallback(MessagingException result, long messageId,
                long attachmentId, int progress) {
        }
        public void updateMailboxCallback(MessagingException result, long accountId,
                long mailboxId, int progress, int numNewMessages) {
            if (result != null || progress == 100) {
                long inboxId = Mailbox.findMailboxOfType(MailService.this,
                        accountId, Mailbox.TYPE_INBOX);
                if (mailboxId == inboxId) {
                    if (progress == 100) {
                        updateAccountReport(accountId, numNewMessages);
                        if (numNewMessages > 0) {
                            notifyNewMessages(accountId);
                        }
                    } else {
                        updateAccountReport(accountId, -1);
                    }
                }
                Email.updateMailboxRefreshTime(mailboxId);
            }
        }
        public void updateMailboxListCallback(MessagingException result, long accountId,
                int progress) {
        }
        public void serviceCheckMailCallback(MessagingException result, long accountId,
                long mailboxId, int progress, long tag) {
            if (result != null || progress == 100) {
                if (result != null) {
                    updateAccountReport(accountId, -1);
                }
                AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                reschedule(alarmManager);
                int serviceId = MailService.this.mStartId;
                if (tag != 0) {
                    serviceId = (int) tag;
                }
                stopSelf(serviceId);
            }
        }
        public void sendMailCallback(MessagingException result, long accountId, long messageId,
                int progress) {
        }
    }
    private void notifyNewMessages(long accountId) {
        boolean notify = false;
        boolean vibrate = false;
        boolean vibrateWhenSilent = false;
        Uri ringtone = null;
        int accountsWithNewMessages = 0;
        int numNewMessages = 0;
        String reportName = null;
        synchronized (mSyncReports) {
            for (AccountSyncReport report : mSyncReports.values()) {
                if (report.numNewMessages == 0) {
                    continue;
                }
                numNewMessages += report.numNewMessages;
                accountsWithNewMessages += 1;
                if (report.accountId == accountId) {
                    notify = report.notify;
                    vibrate = report.vibrate;
                    vibrateWhenSilent = report.vibrateWhenSilent;
                    ringtone = report.ringtoneUri;
                    reportName = report.displayName;
                }
            }
        }
        if (!notify) {
            return;
        }
        Intent intent;
        String reportString;
        if (accountsWithNewMessages == 1) {
            reportString = getResources().getQuantityString(
                    R.plurals.notification_new_one_account_fmt, numNewMessages,
                    numNewMessages, reportName);
            intent = MessageList.createIntent(this, accountId, -1, Mailbox.TYPE_INBOX);
        } else {
            reportString = getResources().getQuantityString(
                    R.plurals.notification_new_multi_account_fmt, accountsWithNewMessages,
                    accountsWithNewMessages);
            intent = MessageList.createIntent(this, -1, Mailbox.QUERY_ALL_INBOXES, -1);
        }
        PendingIntent pending =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification(
                R.drawable.stat_notify_email_generic,
                getString(R.string.notification_new_title),
                System.currentTimeMillis());
        notification.setLatestEventInfo(this,
                getString(R.string.notification_new_title),
                reportString,
                pending);
        notification.sound = ringtone;
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        boolean nowSilent = audioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL;
        if (vibrate || (vibrateWhenSilent && nowSilent)) {
            notification.defaults |= Notification.DEFAULT_VIBRATE;
        }
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        notification.defaults |= Notification.DEFAULT_LIGHTS;
        NotificationManager notificationManager =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID_NEW_MESSAGES, notification);
    }
}
