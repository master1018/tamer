public class SecurityPolicy {
    private static SecurityPolicy sInstance = null;
    private Context mContext;
    private DevicePolicyManager mDPM;
    private ComponentName mAdminName;
    private PolicySet mAggregatePolicy;
     static final PolicySet NO_POLICY_SET =
            new PolicySet(0, PolicySet.PASSWORD_MODE_NONE, 0, 0, false);
    private static final String[] ACCOUNT_SECURITY_PROJECTION = new String[] {
        AccountColumns.ID, AccountColumns.SECURITY_FLAGS
    };
    private static final int ACCOUNT_SECURITY_COLUMN_FLAGS = 1;
    private static final String WHERE_ACCOUNT_SECURITY_NONZERO =
        Account.SECURITY_FLAGS + " IS NOT NULL AND " + Account.SECURITY_FLAGS + "!=0";
    private static final String[] ACCOUNT_FLAGS_PROJECTION = new String[] {
        AccountColumns.ID, AccountColumns.FLAGS, AccountColumns.SECURITY_FLAGS
    };
    private static final int ACCOUNT_FLAGS_COLUMN_ID = 0;
    private static final int ACCOUNT_FLAGS_COLUMN_FLAGS = 1;
    public synchronized static SecurityPolicy getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SecurityPolicy(context);
        }
        return sInstance;
    }
    private SecurityPolicy(Context context) {
        mContext = context;
        mDPM = null;
        mAdminName = new ComponentName(context, PolicyAdmin.class);
        mAggregatePolicy = null;
    }
     void setContext(Context context) {
        mContext = context;
    }
     PolicySet computeAggregatePolicy() {
        boolean policiesFound = false;
        int minPasswordLength = Integer.MIN_VALUE;
        int passwordMode = Integer.MIN_VALUE;
        int maxPasswordFails = Integer.MAX_VALUE;
        int maxScreenLockTime = Integer.MAX_VALUE;
        boolean requireRemoteWipe = false;
        Cursor c = mContext.getContentResolver().query(Account.CONTENT_URI,
                ACCOUNT_SECURITY_PROJECTION, WHERE_ACCOUNT_SECURITY_NONZERO, null, null);
        try {
            while (c.moveToNext()) {
                int flags = c.getInt(ACCOUNT_SECURITY_COLUMN_FLAGS);
                if (flags != 0) {
                    PolicySet p = new PolicySet(flags);
                    minPasswordLength = Math.max(p.mMinPasswordLength, minPasswordLength);
                    passwordMode  = Math.max(p.mPasswordMode, passwordMode);
                    if (p.mMaxPasswordFails > 0) {
                        maxPasswordFails = Math.min(p.mMaxPasswordFails, maxPasswordFails);
                    }
                    if (p.mMaxScreenLockTime > 0) {
                        maxScreenLockTime = Math.min(p.mMaxScreenLockTime, maxScreenLockTime);
                    }
                    requireRemoteWipe |= p.mRequireRemoteWipe;
                    policiesFound = true;
                }
            }
        } finally {
            c.close();
        }
        if (policiesFound) {
            if (minPasswordLength == Integer.MIN_VALUE) minPasswordLength = 0;
            if (passwordMode == Integer.MIN_VALUE) passwordMode = 0;
            if (maxPasswordFails == Integer.MAX_VALUE) maxPasswordFails = 0;
            if (maxScreenLockTime == Integer.MAX_VALUE) maxScreenLockTime = 0;
            return new PolicySet(minPasswordLength, passwordMode, maxPasswordFails,
                    maxScreenLockTime, requireRemoteWipe);
        } else {
            return NO_POLICY_SET;
        }
    }
    public synchronized PolicySet getAggregatePolicy() {
        if (mAggregatePolicy == null) {
            mAggregatePolicy = computeAggregatePolicy();
        }
        return mAggregatePolicy;
    }
    private synchronized DevicePolicyManager getDPM() {
        if (mDPM == null) {
            mDPM = (DevicePolicyManager) mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
        }
        return mDPM;
    }
    public synchronized void updatePolicies(long accountId) {
        mAggregatePolicy = null;
    }
    public void reducePolicies() {
        updatePolicies(-1);
        setActivePolicies();
    }
    public boolean isActive(PolicySet policies) {
        if (policies == null) {
            policies = getAggregatePolicy();
        }
        if (policies == NO_POLICY_SET) {
            return true;
        }
        DevicePolicyManager dpm = getDPM();
        if (dpm.isAdminActive(mAdminName)) {
            if (policies.mMinPasswordLength > 0) {
                if (dpm.getPasswordMinimumLength(mAdminName) < policies.mMinPasswordLength) {
                    return false;
                }
            }
            if (policies.mPasswordMode > 0) {
                if (dpm.getPasswordQuality(mAdminName) < policies.getDPManagerPasswordQuality()) {
                    return false;
                }
                if (!dpm.isActivePasswordSufficient()) {
                    return false;
                }
            }
            if (policies.mMaxScreenLockTime > 0) {
                if (dpm.getMaximumTimeToLock(mAdminName) > policies.mMaxScreenLockTime * 1000) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    public void setActivePolicies() {
        DevicePolicyManager dpm = getDPM();
        PolicySet policies = getAggregatePolicy();
        if (policies == NO_POLICY_SET) {
            dpm.removeActiveAdmin(mAdminName);
        } else if (dpm.isAdminActive(mAdminName)) {
            dpm.setPasswordQuality(mAdminName, policies.getDPManagerPasswordQuality());
            dpm.setPasswordMinimumLength(mAdminName, policies.mMinPasswordLength);
            dpm.setMaximumTimeToLock(mAdminName, policies.mMaxScreenLockTime * 1000);
            dpm.setMaximumFailedPasswordsForWipe(mAdminName, policies.mMaxPasswordFails);
        }
    }
    public void setAccountHoldFlag(Account account, boolean newState) {
        if (newState) {
            account.mFlags |= Account.FLAGS_SECURITY_HOLD;
        } else {
            account.mFlags &= ~Account.FLAGS_SECURITY_HOLD;
        }
        ContentValues cv = new ContentValues();
        cv.put(AccountColumns.FLAGS, account.mFlags);
        account.update(mContext, cv);
    }
    public void clearAccountHoldFlags() {
        ContentResolver resolver = mContext.getContentResolver();
        Cursor c = resolver.query(Account.CONTENT_URI, ACCOUNT_FLAGS_PROJECTION,
                WHERE_ACCOUNT_SECURITY_NONZERO, null, null);
        try {
            while (c.moveToNext()) {
                int flags = c.getInt(ACCOUNT_FLAGS_COLUMN_FLAGS);
                if (0 != (flags & Account.FLAGS_SECURITY_HOLD)) {
                    ContentValues cv = new ContentValues();
                    cv.put(AccountColumns.FLAGS, flags & ~Account.FLAGS_SECURITY_HOLD);
                    long accountId = c.getLong(ACCOUNT_FLAGS_COLUMN_ID);
                    Uri uri = ContentUris.withAppendedId(Account.CONTENT_URI, accountId);
                    resolver.update(uri, cv, null, null);
                }
            }
        } finally {
            c.close();
        }
    }
    public void policiesRequired(long accountId) {
        Account account = EmailContent.Account.restoreAccountWithId(mContext, accountId);
        setAccountHoldFlag(account, true);
        String tickerText = mContext.getString(R.string.security_notification_ticker_fmt,
                account.getDisplayName());
        String contentTitle = mContext.getString(R.string.security_notification_content_title);
        String contentText = account.getDisplayName();
        String ringtoneString = account.getRingtone();
        Uri ringTone = (ringtoneString == null) ? null : Uri.parse(ringtoneString);
        boolean vibrate = 0 != (account.mFlags & Account.FLAGS_VIBRATE_ALWAYS);
        boolean vibrateWhenSilent = 0 != (account.mFlags & Account.FLAGS_VIBRATE_WHEN_SILENT);
        Intent intent = AccountSecurity.actionUpdateSecurityIntent(mContext, accountId);
        PendingIntent pending =
            PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification(R.drawable.stat_notify_email_generic,
                tickerText, System.currentTimeMillis());
        notification.setLatestEventInfo(mContext, contentTitle, contentText, pending);
        AudioManager audioManager =
            (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        boolean nowSilent =
            audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE;
        notification.sound = ringTone;
        if (vibrate || (vibrateWhenSilent && nowSilent)) {
            notification.defaults |= Notification.DEFAULT_VIBRATE;
        }
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        notification.defaults |= Notification.DEFAULT_LIGHTS;
        NotificationManager notificationManager =
            (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(MailService.NOTIFICATION_ID_SECURITY_NEEDED, notification);
    }
    public void clearNotification(long accountId) {
        NotificationManager notificationManager =
            (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(MailService.NOTIFICATION_ID_SECURITY_NEEDED);
    }
    public void remoteWipe() {
        DevicePolicyManager dpm = getDPM();
        if (dpm.isAdminActive(mAdminName)) {
            dpm.wipeData(0);
        } else {
            Log.d(Email.LOG_TAG, "Could not remote wipe because not device admin.");
        }
    }
    public static class PolicySet {
        private static final int PASSWORD_LENGTH_MASK = 31;
        private static final int PASSWORD_LENGTH_SHIFT = 0;
        public static final int PASSWORD_LENGTH_MAX = 30;
        private static final int PASSWORD_MODE_SHIFT = 5;
        private static final int PASSWORD_MODE_MASK = 15 << PASSWORD_MODE_SHIFT;
        public static final int PASSWORD_MODE_NONE = 0 << PASSWORD_MODE_SHIFT;
        public static final int PASSWORD_MODE_SIMPLE = 1 << PASSWORD_MODE_SHIFT;
        public static final int PASSWORD_MODE_STRONG = 2 << PASSWORD_MODE_SHIFT;
        private static final int PASSWORD_MAX_FAILS_SHIFT = 9;
        private static final int PASSWORD_MAX_FAILS_MASK = 31 << PASSWORD_MAX_FAILS_SHIFT;
        public static final int PASSWORD_MAX_FAILS_MAX = 31;
        private static final int SCREEN_LOCK_TIME_SHIFT = 14;
        private static final int SCREEN_LOCK_TIME_MASK = 2047 << SCREEN_LOCK_TIME_SHIFT;
        public static final int SCREEN_LOCK_TIME_MAX = 2047;
        private static final int REQUIRE_REMOTE_WIPE = 1 << 25;
         final int mMinPasswordLength;
         final int mPasswordMode;
         final int mMaxPasswordFails;
         final int mMaxScreenLockTime;
         final boolean mRequireRemoteWipe;
        public int getMinPasswordLength() {
            return mMinPasswordLength;
        }
        public int getPasswordMode() {
            return mPasswordMode;
        }
        public int getMaxPasswordFails() {
            return mMaxPasswordFails;
        }
        public int getMaxScreenLockTime() {
            return mMaxScreenLockTime;
        }
        public boolean isRequireRemoteWipe() {
            return mRequireRemoteWipe;
        }
        public PolicySet(int minPasswordLength, int passwordMode, int maxPasswordFails,
                int maxScreenLockTime, boolean requireRemoteWipe) throws IllegalArgumentException {
            if (passwordMode == PASSWORD_MODE_NONE) {
                maxPasswordFails = 0;
                maxScreenLockTime = 0;
                minPasswordLength = 0;
            } else {
                if ((passwordMode != PASSWORD_MODE_SIMPLE) &&
                        (passwordMode != PASSWORD_MODE_STRONG)) {
                    throw new IllegalArgumentException("password mode");
                }
                if (minPasswordLength > PASSWORD_LENGTH_MAX) {
                    throw new IllegalArgumentException("password length");
                }
                if (maxPasswordFails > PASSWORD_MAX_FAILS_MAX) {
                    maxPasswordFails = PASSWORD_MAX_FAILS_MAX;
                }
                if (maxScreenLockTime > SCREEN_LOCK_TIME_MAX) {
                    maxScreenLockTime = SCREEN_LOCK_TIME_MAX;
                }
            }
            mMinPasswordLength = minPasswordLength;
            mPasswordMode = passwordMode;
            mMaxPasswordFails = maxPasswordFails;
            mMaxScreenLockTime = maxScreenLockTime;
            mRequireRemoteWipe = requireRemoteWipe;
        }
        public PolicySet(Account account) {
            this(account.mSecurityFlags);
        }
        public PolicySet(int flags) {
            mMinPasswordLength =
                (flags & PASSWORD_LENGTH_MASK) >> PASSWORD_LENGTH_SHIFT;
            mPasswordMode =
                (flags & PASSWORD_MODE_MASK);
            mMaxPasswordFails =
                (flags & PASSWORD_MAX_FAILS_MASK) >> PASSWORD_MAX_FAILS_SHIFT;
            mMaxScreenLockTime =
                (flags & SCREEN_LOCK_TIME_MASK) >> SCREEN_LOCK_TIME_SHIFT;
            mRequireRemoteWipe = 0 != (flags & REQUIRE_REMOTE_WIPE);
        }
        public int getDPManagerPasswordQuality() {
            switch (mPasswordMode) {
                case PASSWORD_MODE_SIMPLE:
                    return DevicePolicyManager.PASSWORD_QUALITY_NUMERIC;
                case PASSWORD_MODE_STRONG:
                    return DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC;
                default:
                    return DevicePolicyManager .PASSWORD_QUALITY_UNSPECIFIED;
            }
        }
        public boolean writeAccount(Account account, String syncKey, boolean update,
                Context context) {
            int newFlags = hashCode();
            boolean dirty = (newFlags != account.mSecurityFlags);
            account.mSecurityFlags = newFlags;
            account.mSecuritySyncKey = syncKey;
            if (update) {
                if (account.isSaved()) {
                    ContentValues cv = new ContentValues();
                    cv.put(AccountColumns.SECURITY_FLAGS, account.mSecurityFlags);
                    cv.put(AccountColumns.SECURITY_SYNC_KEY, account.mSecuritySyncKey);
                    account.update(context, cv);
                } else {
                    account.save(context);
                }
            }
            return dirty;
        }
        @Override
        public boolean equals(Object o) {
            if (o instanceof PolicySet) {
                PolicySet other = (PolicySet)o;
                return (this.mMinPasswordLength == other.mMinPasswordLength)
                        && (this.mPasswordMode == other.mPasswordMode)
                        && (this.mMaxPasswordFails == other.mMaxPasswordFails)
                        && (this.mMaxScreenLockTime == other.mMaxScreenLockTime)
                        && (this.mRequireRemoteWipe == other.mRequireRemoteWipe);
            }
            return false;
        }
        @Override
        public int hashCode() {
            int flags = 0;
            flags = mMinPasswordLength << PASSWORD_LENGTH_SHIFT;
            flags |= mPasswordMode;
            flags |= mMaxPasswordFails << PASSWORD_MAX_FAILS_SHIFT;
            flags |= mMaxScreenLockTime << SCREEN_LOCK_TIME_SHIFT;
            if (mRequireRemoteWipe) {
                flags |= REQUIRE_REMOTE_WIPE;
            }
            return flags;
        }
        @Override
        public String toString() {
            return "{ " + "pw-len-min=" + mMinPasswordLength + " pw-mode=" + mPasswordMode
                    + " pw-fails-max=" + mMaxPasswordFails + " screenlock-max="
                    + mMaxScreenLockTime + " remote-wipe-req=" + mRequireRemoteWipe + "}";
        }
    }
    public boolean isActiveAdmin() {
        DevicePolicyManager dpm = getDPM();
        return dpm.isAdminActive(mAdminName);
    }
    public ComponentName getAdminComponent() {
        return mAdminName;
    }
     void onAdminEnabled(boolean isEnabled) {
        if (!isEnabled) {
            ContentValues cv = new ContentValues();
            cv.put(AccountColumns.SECURITY_FLAGS, 0);
            cv.putNull(AccountColumns.SECURITY_SYNC_KEY);
            mContext.getContentResolver().update(Account.CONTENT_URI, cv, null, null);
            updatePolicies(-1);
        }
    }
    public static class PolicyAdmin extends DeviceAdminReceiver {
        @Override
        public void onEnabled(Context context, Intent intent) {
            SecurityPolicy.getInstance(context).onAdminEnabled(true);
        }
        @Override
        public void onDisabled(Context context, Intent intent) {
            SecurityPolicy.getInstance(context).onAdminEnabled(false);
        }
        @Override
        public void onPasswordChanged(Context context, Intent intent) {
            SecurityPolicy.getInstance(context).clearAccountHoldFlags();
        }
    }
}
