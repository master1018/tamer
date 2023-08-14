public final class PhoneStateIntentReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "PHONE";
    private static final boolean DBG = false;
    private static final int NOTIF_PHONE    = 1 << 0;
    private static final int NOTIF_SERVICE  = 1 << 1;
    private static final int NOTIF_SIGNAL   = 1 << 2;
    private static final int NOTIF_MAX      = 1 << 5;
    Phone.State mPhoneState = Phone.State.IDLE;
    ServiceState mServiceState = new ServiceState();
    SignalStrength mSignalStrength = new SignalStrength();
    private Context mContext;
    private Handler mTarget;
    private IntentFilter mFilter;
    private int mWants;
    private int mPhoneStateEventWhat;
    private int mServiceStateEventWhat;
    private int mLocationEventWhat;
    private int mAsuEventWhat;
    public PhoneStateIntentReceiver() {
        super();
        mFilter = new IntentFilter();
    }
    public PhoneStateIntentReceiver(Context context, Handler target) {
        this();
        setContext(context);
        setTarget(target);
    }
    public void setContext(Context c) {
        mContext = c;
    }
    public void setTarget(Handler h) {
        mTarget = h;
    }
    public Phone.State getPhoneState() {
        if ((mWants & NOTIF_PHONE) == 0) {
            throw new RuntimeException
                ("client must call notifyPhoneCallState(int)");
        }
        return mPhoneState;
    }
    public ServiceState getServiceState() {
        if ((mWants & NOTIF_SERVICE) == 0) {
            throw new RuntimeException
                ("client must call notifyServiceState(int)");
        }
        return mServiceState;
    }
    public int getSignalStrength() {
        if ((mWants & NOTIF_SIGNAL) == 0) {
            throw new RuntimeException
                ("client must call notifySignalStrength(int)");
        }
        int gsmSignalStrength = mSignalStrength.getGsmSignalStrength();
        return (gsmSignalStrength == 99 ? -1 : gsmSignalStrength);
    }
    public int getSignalStrengthDbm() {
        if ((mWants & NOTIF_SIGNAL) == 0) {
            throw new RuntimeException
                ("client must call notifySignalStrength(int)");
        }
        int dBm = -1;
        if(!mSignalStrength.isGsm()) {
            dBm = mSignalStrength.getCdmaDbm();
        } else {
            int gsmSignalStrength = mSignalStrength.getGsmSignalStrength();
            int asu = (gsmSignalStrength == 99 ? -1 : gsmSignalStrength);
            if (asu != -1) {
                dBm = -113 + 2*asu;
            }
        }
        return dBm;
    }
    public void notifyPhoneCallState(int eventWhat) {
        mWants |= NOTIF_PHONE;
        mPhoneStateEventWhat = eventWhat;
        mFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
    }
    public boolean getNotifyPhoneCallState() {
        return ((mWants & NOTIF_PHONE) != 0);
    }
    public void notifyServiceState(int eventWhat) {
        mWants |= NOTIF_SERVICE;
        mServiceStateEventWhat = eventWhat;
        mFilter.addAction(TelephonyIntents.ACTION_SERVICE_STATE_CHANGED);
    }
    public boolean getNotifyServiceState() {
        return ((mWants & NOTIF_SERVICE) != 0);
    }
    public void notifySignalStrength (int eventWhat) {
        mWants |= NOTIF_SIGNAL;
        mAsuEventWhat = eventWhat;
        mFilter.addAction(TelephonyIntents.ACTION_SIGNAL_STRENGTH_CHANGED);
    }
    public boolean getNotifySignalStrength() {
        return ((mWants & NOTIF_SIGNAL) != 0);
    }
    public void registerIntent() {
        mContext.registerReceiver(this, mFilter);
    }
    public void unregisterIntent() {
        mContext.unregisterReceiver(this);
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        try {
            if (TelephonyIntents.ACTION_SIGNAL_STRENGTH_CHANGED.equals(action)) {
                mSignalStrength = SignalStrength.newFromBundle(intent.getExtras());
                if (mTarget != null && getNotifySignalStrength()) {
                    Message message = Message.obtain(mTarget, mAsuEventWhat);
                    mTarget.sendMessage(message);
                }
            } else if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)) {
                if (DBG) Log.d(LOG_TAG, "onReceiveIntent: ACTION_PHONE_STATE_CHANGED, state="
                               + intent.getStringExtra(Phone.STATE_KEY));
                String phoneState = intent.getStringExtra(Phone.STATE_KEY);
                mPhoneState = (Phone.State) Enum.valueOf(
                        Phone.State.class, phoneState);
                if (mTarget != null && getNotifyPhoneCallState()) {
                    Message message = Message.obtain(mTarget,
                            mPhoneStateEventWhat);
                    mTarget.sendMessage(message);
                }
            } else if (TelephonyIntents.ACTION_SERVICE_STATE_CHANGED.equals(action)) {
                mServiceState = ServiceState.newFromBundle(intent.getExtras());
                if (mTarget != null && getNotifyServiceState()) {
                    Message message = Message.obtain(mTarget,
                            mServiceStateEventWhat);
                    mTarget.sendMessage(message);
                }
            }
        } catch (Exception ex) {
            Log.e(LOG_TAG, "[PhoneStateIntentRecv] caught " + ex);
            ex.printStackTrace();
        }
    }
}
