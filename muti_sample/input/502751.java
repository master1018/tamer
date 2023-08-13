public class NotificationMgr implements CallerInfoAsyncQuery.OnQueryCompleteListener{
    private static final String LOG_TAG = "NotificationMgr";
    private static final boolean DBG = (PhoneApp.DBG_LEVEL >= 2);
    private static final String[] CALL_LOG_PROJECTION = new String[] {
        Calls._ID,
        Calls.NUMBER,
        Calls.DATE,
        Calls.DURATION,
        Calls.TYPE,
    };
    static final int MISSED_CALL_NOTIFICATION = 1;
    static final int IN_CALL_NOTIFICATION = 2;
    static final int MMI_NOTIFICATION = 3;
    static final int NETWORK_SELECTION_NOTIFICATION = 4;
    static final int VOICEMAIL_NOTIFICATION = 5;
    static final int CALL_FORWARD_NOTIFICATION = 6;
    static final int DATA_DISCONNECTED_ROAMING_NOTIFICATION = 7;
    static final int SELECTED_OPERATOR_FAIL_NOTIFICATION = 8;
    private static NotificationMgr sMe = null;
    private Phone mPhone;
    private Context mContext;
    private NotificationManager mNotificationMgr;
    private StatusBarManager mStatusBar;
    private StatusBarMgr mStatusBarMgr;
    private Toast mToast;
    private IBinder mSpeakerphoneIcon;
    private IBinder mMuteIcon;
    private int mNumberMissedCalls = 0;
    private int mInCallResId;
    private boolean mSelectedUnavailableNotify = false;
    private static final int MAX_VM_NUMBER_RETRIES = 5;
    private static final int VM_NUMBER_RETRY_DELAY_MILLIS = 10000;
    private int mVmNumberRetriesRemaining = MAX_VM_NUMBER_RETRIES;
    private QueryHandler mQueryHandler = null;
    private static final int CALL_LOG_TOKEN = -1;
    private static final int CONTACT_TOKEN = -2;
    NotificationMgr(Context context) {
        mContext = context;
        mNotificationMgr = (NotificationManager)
            context.getSystemService(Context.NOTIFICATION_SERVICE);
        mStatusBar = (StatusBarManager) context.getSystemService(Context.STATUS_BAR_SERVICE);
        PhoneApp app = PhoneApp.getInstance();
        mPhone = app.phone;
    }
    static void init(Context context) {
        sMe = new NotificationMgr(context);
        sMe.updateNotificationsAtStartup();
    }
    static NotificationMgr getDefault() {
        return sMe;
    }
    StatusBarMgr getStatusBarMgr() {
        if (mStatusBarMgr == null) {
            mStatusBarMgr = new StatusBarMgr();
        }
        return mStatusBarMgr;
    }
    class StatusBarMgr {
        private boolean mIsNotificationEnabled = true;
        private boolean mIsExpandedViewEnabled = true;
        private StatusBarMgr () {
        }
        void enableNotificationAlerts(boolean enable) {
            if (mIsNotificationEnabled != enable) {
                mIsNotificationEnabled = enable;
                updateStatusBar();
            }
        }
        void enableExpandedView(boolean enable) {
            if (mIsExpandedViewEnabled != enable) {
                mIsExpandedViewEnabled = enable;
                updateStatusBar();
            }
        }
        void updateStatusBar() {
            int state = StatusBarManager.DISABLE_NONE;
            if (!mIsExpandedViewEnabled) {
                state |= StatusBarManager.DISABLE_EXPAND;
            }
            if (!mIsNotificationEnabled) {
                state |= StatusBarManager.DISABLE_NOTIFICATION_ALERTS;
            }
            if (DBG) log("updating status bar state: " + state);
            mStatusBar.disable(state);
        }
    }
    private void updateNotificationsAtStartup() {
        if (DBG) log("updateNotificationsAtStartup()...");
        mQueryHandler = new QueryHandler(mContext.getContentResolver());
        StringBuilder where = new StringBuilder("type=");
        where.append(Calls.MISSED_TYPE);
        where.append(" AND new=1");
        mQueryHandler.startQuery(CALL_LOG_TOKEN, null, Calls.CONTENT_URI,  CALL_LOG_PROJECTION,
                where.toString(), null, Calls.DEFAULT_SORT_ORDER);
        if (mPhone.getState() != Phone.State.OFFHOOK) {
            if (DBG) log("Phone is idle, canceling notification.");
            cancelInCall();
        } else {
            if (DBG) log("Phone is offhook, updating notification.");
            updateInCallNotification();
        }
    }
    static final String[] PHONES_PROJECTION = new String[] {
        PhoneLookup.NUMBER,
        PhoneLookup.DISPLAY_NAME
    };
    private class QueryHandler extends AsyncQueryHandler {
        private class NotificationInfo {
            public String name;
            public String number;
            public String label;
            public long date;
        }
        public QueryHandler(ContentResolver cr) {
            super(cr);
        }
        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            switch (token) {
                case CALL_LOG_TOKEN:
                    if (DBG) log("call log query complete.");
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            NotificationInfo n = getNotificationInfo (cursor);
                            if (DBG) log("query contacts for number: " + n.number);
                            mQueryHandler.startQuery(CONTACT_TOKEN, n,
                                    Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, n.number),
                                    PHONES_PROJECTION, null, null, PhoneLookup.NUMBER);
                        }
                        if (DBG) log("closing call log cursor.");
                        cursor.close();
                    }
                    break;
                case CONTACT_TOKEN:
                    if (DBG) log("contact query complete.");
                    if ((cursor != null) && (cookie != null)){
                        NotificationInfo n = (NotificationInfo) cookie;
                        if (cursor.moveToFirst()) {
                            if (DBG) log("contact :" + n.name + " found for phone: " + n.number);
                            n.name = cursor.getString(
                                    cursor.getColumnIndexOrThrow(PhoneLookup.DISPLAY_NAME));
                        }
                        if (DBG) log("sending notification.");
                        notifyMissedCall(n.name, n.number, n.label, n.date);
                        if (DBG) log("closing contact cursor.");
                        cursor.close();
                    }
                    break;
                default:
            }
        }
        private final NotificationInfo getNotificationInfo(Cursor cursor) {
            NotificationInfo n = new NotificationInfo();
            n.name = null;
            n.number = cursor.getString(cursor.getColumnIndexOrThrow(Calls.NUMBER));
            n.label = cursor.getString(cursor.getColumnIndexOrThrow(Calls.TYPE));
            n.date = cursor.getLong(cursor.getColumnIndexOrThrow(Calls.DATE));
            if ( (n.number.equals(CallerInfo.UNKNOWN_NUMBER)) ||
                 (n.number.equals(CallerInfo.PRIVATE_NUMBER)) ||
                 (n.number.equals(CallerInfo.PAYPHONE_NUMBER)) ) {
                n.number = null;
            }
            if (DBG) log("NotificationInfo constructed for number: " + n.number);
            return n;
        }
    }
    private static void configureLedNotification(Notification note) {
        note.flags |= Notification.FLAG_SHOW_LIGHTS;
        note.defaults |= Notification.DEFAULT_LIGHTS;
    }
    void notifyMissedCall(String name, String number, String label, long date) {
        int titleResId;
        String expandedText, callName;
        mNumberMissedCalls++;
        if (name != null && TextUtils.isGraphic(name)) {
            callName = name;
        } else if (!TextUtils.isEmpty(number)){
            callName = number;
        } else {
            callName = mContext.getString(R.string.unknown);
        }
        if (mNumberMissedCalls == 1) {
            titleResId = R.string.notification_missedCallTitle;
            expandedText = callName;
        } else {
            titleResId = R.string.notification_missedCallsTitle;
            expandedText = mContext.getString(R.string.notification_missedCallsMsg,
                    mNumberMissedCalls);
        }
        final Intent intent = PhoneApp.createCallLogIntent();
        Notification note = new Notification(mContext, 
                android.R.drawable.stat_notify_missed_call, 
                mContext.getString(R.string.notification_missedCallTicker, callName), 
                date, 
                mContext.getText(titleResId), 
                expandedText, 
                intent 
                );
        configureLedNotification(note);
        mNotificationMgr.notify(MISSED_CALL_NOTIFICATION, note);
    }
    void cancelMissedCallNotification() {
        mNumberMissedCalls = 0;
        mNotificationMgr.cancel(MISSED_CALL_NOTIFICATION);
    }
    void notifySpeakerphone() {
        if (mSpeakerphoneIcon == null) {
            mSpeakerphoneIcon = mStatusBar.addIcon("speakerphone",
                    android.R.drawable.stat_sys_speakerphone, 0);
        }
    }
    void cancelSpeakerphone() {
        if (mSpeakerphoneIcon != null) {
            mStatusBar.removeIcon(mSpeakerphoneIcon);
            mSpeakerphoneIcon = null;
        }
    }
    void updateSpeakerNotification() {
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        if ((mPhone.getState() == Phone.State.OFFHOOK) && audioManager.isSpeakerphoneOn()) {
            if (DBG) log("updateSpeakerNotification: speaker ON");
            notifySpeakerphone();
        } else {
            if (DBG) log("updateSpeakerNotification: speaker OFF (or not offhook)");
            cancelSpeakerphone();
        }
    }
    void notifyMute() {
        if (mMuteIcon == null) {
            mMuteIcon = mStatusBar.addIcon("mute", android.R.drawable.stat_notify_call_mute, 0);
        }
    }
    void cancelMute() {
        if (mMuteIcon != null) {
            mStatusBar.removeIcon(mMuteIcon);
            mMuteIcon = null;
        }
    }
    void updateMuteNotification() {
        if ((mPhone.getState() == Phone.State.OFFHOOK) && mPhone.getMute()) {
            if (DBG) log("updateMuteNotification: MUTED");
            notifyMute();
        } else {
            if (DBG) log("updateMuteNotification: not muted (or not offhook)");
            cancelMute();
        }
    }
    void updateInCallNotification() {
        int resId;
        if (DBG) log("updateInCallNotification()...");
        if (mPhone.getState() != Phone.State.OFFHOOK) {
            return;
        }
        final boolean hasActiveCall = !mPhone.getForegroundCall().isIdle();
        final boolean hasHoldingCall = !mPhone.getBackgroundCall().isIdle();
        boolean enhancedVoicePrivacy = PhoneApp.getInstance().notifier.getCdmaVoicePrivacyState();
        if (DBG) log("updateInCallNotification: enhancedVoicePrivacy = " + enhancedVoicePrivacy);
        if (!hasActiveCall && hasHoldingCall) {
            if (enhancedVoicePrivacy) {
                resId = android.R.drawable.stat_sys_vp_phone_call_on_hold;
            } else {
                resId = android.R.drawable.stat_sys_phone_call_on_hold;
            }
        } else if (PhoneApp.getInstance().showBluetoothIndication()) {
            if (enhancedVoicePrivacy) {
                resId = com.android.internal.R.drawable.stat_sys_vp_phone_call_bluetooth;
            } else {
                resId = com.android.internal.R.drawable.stat_sys_phone_call_bluetooth;
            }
        } else {
            if (enhancedVoicePrivacy) {
                resId = android.R.drawable.stat_sys_vp_phone_call;
            } else {
                resId = android.R.drawable.stat_sys_phone_call;
            }
        }
        if (DBG) log("- Updating status bar icon: " + resId);
        mInCallResId = resId;
        int expandedViewIcon = mInCallResId;
        Call currentCall = hasActiveCall ? mPhone.getForegroundCall()
                : mPhone.getBackgroundCall();
        Connection currentConn = currentCall.getEarliestConnection();
        Notification notification = new Notification();
        notification.icon = mInCallResId;
        notification.contentIntent = PendingIntent.getActivity(mContext, 0,
                PhoneApp.createInCallIntent(), 0);
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        RemoteViews contentView = new RemoteViews(mContext.getPackageName(),
                                                   R.layout.ongoing_call_notification);
        contentView.setImageViewResource(R.id.icon, expandedViewIcon);
        if (currentConn != null) {
            long callDurationMsec = currentConn.getDurationMillis();
            long chronometerBaseTime = SystemClock.elapsedRealtime() - callDurationMsec;
            String expandedViewLine1;
            if (hasHoldingCall && !hasActiveCall) {
                expandedViewLine1 = mContext.getString(R.string.notification_on_hold);
            } else {
                expandedViewLine1 = mContext.getString(R.string.notification_ongoing_call_format);
            }
            if (DBG) log("- Updating expanded view: line 1 '" + expandedViewLine1 + "'");
            contentView.setChronometer(R.id.text1,
                                       chronometerBaseTime,
                                       expandedViewLine1,
                                       true);
        } else if (DBG) {
            log("updateInCallNotification: connection is null, call status not updated.");
        }
        String expandedViewLine2 = "";
        if (PhoneUtils.isConferenceCall(currentCall)) {
            expandedViewLine2 = mContext.getString(R.string.card_title_conf_call);
        } else {
            PhoneUtils.CallerInfoToken cit =
                PhoneUtils.startGetCallerInfo (mContext, currentCall, this, contentView);
            expandedViewLine2 = PhoneUtils.getCompactNameFromCallerInfo(cit.currentInfo, mContext);
        }
        if (DBG) log("- Updating expanded view: line 2 '" + expandedViewLine2 + "'");
        contentView.setTextViewText(R.id.text2, expandedViewLine2);
        notification.contentView = contentView;
        if (DBG) log("Notifying IN_CALL_NOTIFICATION: " + notification);
        mNotificationMgr.notify(IN_CALL_NOTIFICATION,
                                notification);
        updateSpeakerNotification();
        updateMuteNotification();
    }
    public void onQueryComplete(int token, Object cookie, CallerInfo ci){
        if (DBG) log("callerinfo query complete, updating ui.");
        ((RemoteViews) cookie).setTextViewText(R.id.text2,
                PhoneUtils.getCompactNameFromCallerInfo(ci, mContext));
    }
    private void cancelInCall() {
        if (DBG) log("cancelInCall()...");
        cancelMute();
        cancelSpeakerphone();
        mNotificationMgr.cancel(IN_CALL_NOTIFICATION);
        mInCallResId = 0;
    }
    void cancelCallInProgressNotification() {
        if (DBG) log("cancelCallInProgressNotification()...");
        if (mInCallResId == 0) {
            return;
        }
        if (DBG) log("cancelCallInProgressNotification: " + mInCallResId);
        cancelInCall();
    }
     void updateMwi(boolean visible) {
        if (DBG) log("updateMwi(): " + visible);
        if (visible) {
            int resId = android.R.drawable.stat_notify_voicemail;
            String notificationTitle = mContext.getString(R.string.notification_voicemail_title);
            String vmNumber = mPhone.getVoiceMailNumber();
            if (DBG) log("- got vm number: '" + vmNumber + "'");
            if ((vmNumber == null) && !mPhone.getIccRecordsLoaded()) {
                if (DBG) log("- Null vm number: SIM records not loaded (yet)...");
                if (mVmNumberRetriesRemaining-- > 0) {
                    if (DBG) log("  - Retrying in " + VM_NUMBER_RETRY_DELAY_MILLIS + " msec...");
                    PhoneApp.getInstance().notifier.sendMwiChangedDelayed(
                            VM_NUMBER_RETRY_DELAY_MILLIS);
                    return;
                } else {
                    Log.w(LOG_TAG, "NotificationMgr.updateMwi: getVoiceMailNumber() failed after "
                          + MAX_VM_NUMBER_RETRIES + " retries; giving up.");
                }
            }
            if (mPhone.getPhoneType() == Phone.PHONE_TYPE_CDMA) {
                int vmCount = mPhone.getVoiceMessageCount();
                String titleFormat = mContext.getString(R.string.notification_voicemail_title_count);
                notificationTitle = String.format(titleFormat, vmCount);
            }
            String notificationText;
            if (TextUtils.isEmpty(vmNumber)) {
                notificationText = mContext.getString(
                        R.string.notification_voicemail_no_vm_number);
            } else {
                notificationText = String.format(
                        mContext.getString(R.string.notification_voicemail_text_format),
                        PhoneNumberUtils.formatNumber(vmNumber));
            }
            Intent intent = new Intent(Intent.ACTION_CALL,
                    Uri.fromParts("voicemail", "", null));
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
            Notification notification = new Notification(
                    resId,  
                    null, 
                    System.currentTimeMillis()  
                    );
            notification.setLatestEventInfo(
                    mContext,  
                    notificationTitle,  
                    notificationText,  
                    pendingIntent  
                    );
            notification.defaults |= Notification.DEFAULT_SOUND;
            notification.flags |= Notification.FLAG_NO_CLEAR;
            configureLedNotification(notification);
            mNotificationMgr.notify(VOICEMAIL_NOTIFICATION, notification);
        } else {
            mNotificationMgr.cancel(VOICEMAIL_NOTIFICATION);
        }
    }
     void updateCfi(boolean visible) {
        if (DBG) log("updateCfi(): " + visible);
        if (visible) {
            Notification notification;
            final boolean showExpandedNotification = true;
            if (showExpandedNotification) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClassName("com.android.phone",
                        "com.android.phone.CallFeaturesSetting");
                notification = new Notification(
                        mContext,  
                        android.R.drawable.stat_sys_phone_call_forward,  
                        null, 
                        0,  
                        mContext.getString(R.string.labelCF), 
                        mContext.getString(R.string.sum_cfu_enabled_indicator),  
                        intent 
                        );
            } else {
                notification = new Notification(
                        android.R.drawable.stat_sys_phone_call_forward,  
                        null,  
                        System.currentTimeMillis()  
                        );
            }
            notification.flags |= Notification.FLAG_ONGOING_EVENT;  
            mNotificationMgr.notify(
                    CALL_FORWARD_NOTIFICATION,
                    notification);
        } else {
            mNotificationMgr.cancel(CALL_FORWARD_NOTIFICATION);
        }
    }
     void showDataDisconnectedRoaming() {
        if (DBG) log("showDataDisconnectedRoaming()...");
        Intent intent = new Intent(mContext,
                                   Settings.class);  
        Notification notification = new Notification(
                mContext,  
                android.R.drawable.stat_sys_warning,  
                null, 
                System.currentTimeMillis(),
                mContext.getString(R.string.roaming), 
                mContext.getString(R.string.roaming_reenable_message),  
                intent 
                );
        mNotificationMgr.notify(
                DATA_DISCONNECTED_ROAMING_NOTIFICATION,
                notification);
    }
     void hideDataDisconnectedRoaming() {
        if (DBG) log("hideDataDisconnectedRoaming()...");
        mNotificationMgr.cancel(DATA_DISCONNECTED_ROAMING_NOTIFICATION);
    }
    private void showNetworkSelection(String operator) {
        if (DBG) log("showNetworkSelection(" + operator + ")...");
        String titleText = mContext.getString(
                R.string.notification_network_selection_title);
        String expandedText = mContext.getString(
                R.string.notification_network_selection_text, operator);
        Notification notification = new Notification();
        notification.icon = com.android.internal.R.drawable.stat_sys_warning;
        notification.when = 0;
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notification.tickerText = null;
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        intent.setComponent(new ComponentName("com.android.phone",
                "com.android.phone.NetworkSetting"));
        PendingIntent pi = PendingIntent.getActivity(mContext, 0, intent, 0);
        notification.setLatestEventInfo(mContext, titleText, expandedText, pi);
        mNotificationMgr.notify(SELECTED_OPERATOR_FAIL_NOTIFICATION, notification);
    }
    private void cancelNetworkSelection() {
        if (DBG) log("cancelNetworkSelection()...");
        mNotificationMgr.cancel(SELECTED_OPERATOR_FAIL_NOTIFICATION);
    }
    void updateNetworkSelection(int serviceState) {
        if (mPhone.getPhoneType() == Phone.PHONE_TYPE_GSM) {
            SharedPreferences sp =
                    PreferenceManager.getDefaultSharedPreferences(mContext);
            String networkSelection =
                    sp.getString(PhoneBase.NETWORK_SELECTION_NAME_KEY, "");
            if (TextUtils.isEmpty(networkSelection)) {
                networkSelection =
                        sp.getString(PhoneBase.NETWORK_SELECTION_KEY, "");
            }
            if (DBG) log("updateNetworkSelection()..." + "state = " +
                    serviceState + " new network " + networkSelection);
            if (serviceState == ServiceState.STATE_OUT_OF_SERVICE
                    && !TextUtils.isEmpty(networkSelection)) {
                if (!mSelectedUnavailableNotify) {
                    showNetworkSelection(networkSelection);
                    mSelectedUnavailableNotify = true;
                }
            } else {
                if (mSelectedUnavailableNotify) {
                    cancelNetworkSelection();
                    mSelectedUnavailableNotify = false;
                }
            }
        }
    }
     void postTransientNotification(int notifyId, CharSequence msg) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
        mToast.show();
    }
    private void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
}
