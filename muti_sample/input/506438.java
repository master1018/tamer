public class CalendarSyncEnabler {
    private final Context mContext;
    public CalendarSyncEnabler(Context context) {
        this.mContext = context;
    }
    public final void enableEasCalendarSync() {
        String emailAddresses = enableEasCalendarSyncInternal();
        if (emailAddresses.length() > 0) {
            showNotification(emailAddresses.toString());
        }
    }
     final String enableEasCalendarSyncInternal() {
        StringBuilder emailAddresses = new StringBuilder();
        Account[] exchangeAccounts = AccountManager.get(mContext)
                .getAccountsByType(Email.EXCHANGE_ACCOUNT_MANAGER_TYPE);
        for (Account account : exchangeAccounts) {
            final String emailAddress = account.name;
            Log.i(Email.LOG_TAG, "Enabling Exchange calendar sync for " + emailAddress);
            ContentResolver.setIsSyncable(account, Calendar.AUTHORITY, 1);
            ContentResolver.setSyncAutomatically(account, Calendar.AUTHORITY, true);
            if (emailAddresses.length() > 0) {
                emailAddresses.append(' ');
            }
            emailAddresses.append(emailAddress);
        }
        return emailAddresses.toString();
    }
     void showNotification(String emailAddresses) {
        PendingIntent launchCalendarPendingIntent = PendingIntent.getActivity(mContext, 0,
                createLaunchCalendarIntent(), 0);
        String tickerText = mContext.getString(R.string.notification_exchange_calendar_added);
        Notification n = new Notification(R.drawable.stat_notify_calendar,
                tickerText, System.currentTimeMillis());
        n.setLatestEventInfo(mContext, tickerText, emailAddresses, launchCalendarPendingIntent);
        n.flags = Notification.FLAG_AUTO_CANCEL;
        NotificationManager nm =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(MailService.NOTIFICATION_ID_EXCHANGE_CALENDAR_ADDED, n);
    }
    private Intent createLaunchCalendarIntent() {
        return new Intent(Intent.ACTION_VIEW, Uri.parse("content:
    }
}
