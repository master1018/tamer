public class AccountBackupRestore {
    public static void backupAccounts(final Context context) {
        if (Email.DEBUG) {
            Log.v(Email.LOG_TAG, "backupAccounts");
        }
        new Thread() {
            @Override
            public void run() {
                doBackupAccounts(context, Preferences.getPreferences(context));
            }
        }.start();
    }
    public static void restoreAccountsIfNeeded(final Context context) {
        boolean restored = doRestoreAccounts(context, Preferences.getPreferences(context));
        if (restored) {
            Log.w(Email.LOG_TAG, "Register services after restoring accounts");
            SecurityPolicy.getInstance(context).updatePolicies(-1);
            Email.setServicesEnabled(context);
            ExchangeUtils.startExchangeService(context);
        }
    }
     synchronized static void doBackupAccounts(Context context,
            Preferences preferences) {
        Account[] oldBackups = preferences.getAccounts();
        for (Account backup : oldBackups) {
            backup.delete(preferences);
        }
        long defaultAccountId = EmailContent.Account.getDefaultAccountId(context);
        if (defaultAccountId == -1) {
            return;
        }
        Cursor c = context.getContentResolver().query(EmailContent.Account.CONTENT_URI,
                EmailContent.Account.CONTENT_PROJECTION, null, null, null);
        try {
            while (c.moveToNext()) {
                EmailContent.Account fromAccount =
                        EmailContent.getContent(c, EmailContent.Account.class);
                if (Email.DEBUG) {
                    Log.v(Email.LOG_TAG, "Backing up account:" + fromAccount.getDisplayName());
                }
                Account toAccount = LegacyConversions.makeLegacyAccount(context, fromAccount);
                if (fromAccount.mHostAuthRecv.mProtocol.equals("eas")) {
                    android.accounts.Account acct = new android.accounts.Account(
                            fromAccount.mEmailAddress, Email.EXCHANGE_ACCOUNT_MANAGER_TYPE);
                    boolean syncContacts = ContentResolver.getSyncAutomatically(acct,
                            ContactsContract.AUTHORITY);
                    if (syncContacts) {
                        toAccount.mBackupFlags |= Account.BACKUP_FLAGS_SYNC_CONTACTS;
                    }
                    boolean syncCalendar = ContentResolver.getSyncAutomatically(acct,
                            Calendar.AUTHORITY);
                    if (syncCalendar) {
                        toAccount.mBackupFlags |= Account.BACKUP_FLAGS_SYNC_CALENDAR;
                    }
                }
                if (fromAccount.mId == defaultAccountId) {
                    toAccount.mBackupFlags |= Account.BACKUP_FLAGS_IS_DEFAULT;
                }
                toAccount.mBackupFlags |= Account.BACKUP_FLAGS_IS_BACKUP;
                toAccount.save(preferences);
            }
        } finally {
            c.close();
        }
    }
     synchronized static boolean doRestoreAccounts(Context context,
            Preferences preferences) {
        boolean result = false;
        int numAccounts = EmailContent.count(context, EmailContent.Account.CONTENT_URI, null, null);
        if (numAccounts > 0) {
            return result;
        }
        Account[] backups = preferences.getAccounts();
        if (backups.length == 0) {
            return result;
        }
        Log.w(Email.LOG_TAG, "*** Restoring Email Accounts, found " + backups.length);
        for (Account backupAccount : backups) {
            if ((backupAccount.mBackupFlags & Account.BACKUP_FLAGS_IS_BACKUP) == 0) {
                continue;
            }
            Log.w(Email.LOG_TAG, "Restoring account:" + backupAccount.getDescription());
            EmailContent.Account toAccount =
                LegacyConversions.makeAccount(context, backupAccount);
            if (0 != (backupAccount.mBackupFlags & Account.BACKUP_FLAGS_IS_DEFAULT)) {
                toAccount.setDefaultAccount(true);
            }
            if (toAccount.mHostAuthRecv.mProtocol.equals("eas")) {
                boolean alsoSyncContacts =
                    (backupAccount.mBackupFlags & Account.BACKUP_FLAGS_SYNC_CONTACTS) != 0;
                boolean alsoSyncCalendar =
                    (backupAccount.mBackupFlags & Account.BACKUP_FLAGS_SYNC_CALENDAR) != 0;
                AccountManagerFuture<Bundle> addAccountResult =
                     ExchangeStore.addSystemAccount(context.getApplicationContext(), toAccount,
                             alsoSyncContacts, alsoSyncCalendar, null);
                    toAccount.save(context);
            } else {
                toAccount.save(context);
            }
            result = true;
        }
        return result;
    }
}
