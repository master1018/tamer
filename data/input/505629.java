class TelephonyRegistry extends ITelephonyRegistry.Stub {
    private static final String TAG = "TelephonyRegistry";
    private static class Record {
        String pkgForDebug;
        IBinder binder;
        IPhoneStateListener callback;
        int events;
    }
    private final Context mContext;
    private final ArrayList<Record> mRecords = new ArrayList();
    private final IBatteryStats mBatteryStats;
    private int mCallState = TelephonyManager.CALL_STATE_IDLE;
    private String mCallIncomingNumber = "";
    private ServiceState mServiceState = new ServiceState();
    private SignalStrength mSignalStrength = new SignalStrength();
    private boolean mMessageWaiting = false;
    private boolean mCallForwarding = false;
    private int mDataActivity = TelephonyManager.DATA_ACTIVITY_NONE;
    private int mDataConnectionState = TelephonyManager.DATA_CONNECTED;
    private boolean mDataConnectionPossible = false;
    private String mDataConnectionReason = "";
    private String mDataConnectionApn = "";
    private String[] mDataConnectionApnTypes = null;
    private String mDataConnectionInterfaceName = "";
    private Bundle mCellLocation = new Bundle();
    private int mDataConnectionNetworkType;
    static final int PHONE_STATE_PERMISSION_MASK =
                PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR |
                PhoneStateListener.LISTEN_CALL_STATE |
                PhoneStateListener.LISTEN_DATA_ACTIVITY |
                PhoneStateListener.LISTEN_DATA_CONNECTION_STATE |
                PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR;
    TelephonyRegistry(Context context) {
        CellLocation  location = CellLocation.getEmpty();
        if (location != null) {
            location.fillInNotifierBundle(mCellLocation);
        }
        mContext = context;
        mBatteryStats = BatteryStatsService.getService();
    }
    public void listen(String pkgForDebug, IPhoneStateListener callback, int events,
            boolean notifyNow) {
        if (events != 0) {
            checkListenerPermission(events);
            synchronized (mRecords) {
                Record r = null;
                find_and_add: {
                    IBinder b = callback.asBinder();
                    final int N = mRecords.size();
                    for (int i = 0; i < N; i++) {
                        r = mRecords.get(i);
                        if (b == r.binder) {
                            break find_and_add;
                        }
                    }
                    r = new Record();
                    r.binder = b;
                    r.callback = callback;
                    r.pkgForDebug = pkgForDebug;
                    mRecords.add(r);
                }
                int send = events & (events ^ r.events);
                r.events = events;
                if (notifyNow) {
                    if ((events & PhoneStateListener.LISTEN_SERVICE_STATE) != 0) {
                        sendServiceState(r, mServiceState);
                    }
                    if ((events & PhoneStateListener.LISTEN_SIGNAL_STRENGTH) != 0) {
                        try {
                            int gsmSignalStrength = mSignalStrength.getGsmSignalStrength();
                            r.callback.onSignalStrengthChanged((gsmSignalStrength == 99 ? -1
                                    : gsmSignalStrength));
                        } catch (RemoteException ex) {
                            remove(r.binder);
                        }
                    }
                    if ((events & PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR) != 0) {
                        try {
                            r.callback.onMessageWaitingIndicatorChanged(mMessageWaiting);
                        } catch (RemoteException ex) {
                            remove(r.binder);
                        }
                    }
                    if ((events & PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR) != 0) {
                        try {
                            r.callback.onCallForwardingIndicatorChanged(mCallForwarding);
                        } catch (RemoteException ex) {
                            remove(r.binder);
                        }
                    }
                    if ((events & PhoneStateListener.LISTEN_CELL_LOCATION) != 0) {
                        sendCellLocation(r, mCellLocation);
                    }
                    if ((events & PhoneStateListener.LISTEN_CALL_STATE) != 0) {
                        try {
                            r.callback.onCallStateChanged(mCallState, mCallIncomingNumber);
                        } catch (RemoteException ex) {
                            remove(r.binder);
                        }
                    }
                    if ((events & PhoneStateListener.LISTEN_DATA_CONNECTION_STATE) != 0) {
                        try {
                            r.callback.onDataConnectionStateChanged(mDataConnectionState,
                                mDataConnectionNetworkType);
                        } catch (RemoteException ex) {
                            remove(r.binder);
                        }
                    }
                    if ((events & PhoneStateListener.LISTEN_DATA_ACTIVITY) != 0) {
                        try {
                            r.callback.onDataActivity(mDataActivity);
                        } catch (RemoteException ex) {
                            remove(r.binder);
                        }
                    }
                    if ((events & PhoneStateListener.LISTEN_SIGNAL_STRENGTHS) != 0) {
                        try {
                            r.callback.onSignalStrengthsChanged(mSignalStrength);
                        } catch (RemoteException ex) {
                            remove(r.binder);
                        }
                    }
                }
            }
        } else {
            remove(callback.asBinder());
        }
    }
    private void remove(IBinder binder) {
        synchronized (mRecords) {
            final int recordCount = mRecords.size();
            for (int i = 0; i < recordCount; i++) {
                if (mRecords.get(i).binder == binder) {
                    mRecords.remove(i);
                    return;
                }
            }
        }
    }
    public void notifyCallState(int state, String incomingNumber) {
        if (!checkNotifyPermission("notifyCallState()")) {
            return;
        }
        synchronized (mRecords) {
            mCallState = state;
            mCallIncomingNumber = incomingNumber;
            for (int i = mRecords.size() - 1; i >= 0; i--) {
                Record r = mRecords.get(i);
                if ((r.events & PhoneStateListener.LISTEN_CALL_STATE) != 0) {
                    try {
                        r.callback.onCallStateChanged(state, incomingNumber);
                    } catch (RemoteException ex) {
                        remove(r.binder);
                    }
                }
            }
        }
        broadcastCallStateChanged(state, incomingNumber);
    }
    public void notifyServiceState(ServiceState state) {
        if (!checkNotifyPermission("notifyServiceState()")){
            return;
        }
        synchronized (mRecords) {
            mServiceState = state;
            for (int i = mRecords.size() - 1; i >= 0; i--) {
                Record r = mRecords.get(i);
                if ((r.events & PhoneStateListener.LISTEN_SERVICE_STATE) != 0) {
                    sendServiceState(r, state);
                }
            }
        }
        broadcastServiceStateChanged(state);
    }
    public void notifySignalStrength(SignalStrength signalStrength) {
        if (!checkNotifyPermission("notifySignalStrength()")) {
            return;
        }
        synchronized (mRecords) {
            mSignalStrength = signalStrength;
            for (int i = mRecords.size() - 1; i >= 0; i--) {
                Record r = mRecords.get(i);
                if ((r.events & PhoneStateListener.LISTEN_SIGNAL_STRENGTHS) != 0) {
                    sendSignalStrength(r, signalStrength);
                }
                if ((r.events & PhoneStateListener.LISTEN_SIGNAL_STRENGTH) != 0) {
                    try {
                        int gsmSignalStrength = signalStrength.getGsmSignalStrength();
                        r.callback.onSignalStrengthChanged((gsmSignalStrength == 99 ? -1
                                : gsmSignalStrength));
                    } catch (RemoteException ex) {
                        remove(r.binder);
                    }
                }
            }
        }
        broadcastSignalStrengthChanged(signalStrength);
    }
    public void notifyMessageWaitingChanged(boolean mwi) {
        if (!checkNotifyPermission("notifyMessageWaitingChanged()")) {
            return;
        }
        synchronized (mRecords) {
            mMessageWaiting = mwi;
            for (int i = mRecords.size() - 1; i >= 0; i--) {
                Record r = mRecords.get(i);
                if ((r.events & PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR) != 0) {
                    try {
                        r.callback.onMessageWaitingIndicatorChanged(mwi);
                    } catch (RemoteException ex) {
                        remove(r.binder);
                    }
                }
            }
        }
    }
    public void notifyCallForwardingChanged(boolean cfi) {
        if (!checkNotifyPermission("notifyCallForwardingChanged()")) {
            return;
        }
        synchronized (mRecords) {
            mCallForwarding = cfi;
            for (int i = mRecords.size() - 1; i >= 0; i--) {
                Record r = mRecords.get(i);
                if ((r.events & PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR) != 0) {
                    try {
                        r.callback.onCallForwardingIndicatorChanged(cfi);
                    } catch (RemoteException ex) {
                        remove(r.binder);
                    }
                }
            }
        }
    }
    public void notifyDataActivity(int state) {
        if (!checkNotifyPermission("notifyDataActivity()" )) {
            return;
        }
        synchronized (mRecords) {
            mDataActivity = state;
            for (int i = mRecords.size() - 1; i >= 0; i--) {
                Record r = mRecords.get(i);
                if ((r.events & PhoneStateListener.LISTEN_DATA_ACTIVITY) != 0) {
                    try {
                        r.callback.onDataActivity(state);
                    } catch (RemoteException ex) {
                        remove(r.binder);
                    }
                }
            }
        }
    }
    public void notifyDataConnection(int state, boolean isDataConnectivityPossible,
            String reason, String apn, String[] apnTypes, String interfaceName, int networkType) {
        if (!checkNotifyPermission("notifyDataConnection()" )) {
            return;
        }
        synchronized (mRecords) {
            mDataConnectionState = state;
            mDataConnectionPossible = isDataConnectivityPossible;
            mDataConnectionReason = reason;
            mDataConnectionApn = apn;
            mDataConnectionApnTypes = apnTypes;
            mDataConnectionInterfaceName = interfaceName;
            mDataConnectionNetworkType = networkType;
            for (int i = mRecords.size() - 1; i >= 0; i--) {
                Record r = mRecords.get(i);
                if ((r.events & PhoneStateListener.LISTEN_DATA_CONNECTION_STATE) != 0) {
                    try {
                        r.callback.onDataConnectionStateChanged(state, networkType);
                    } catch (RemoteException ex) {
                        remove(r.binder);
                    }
                }
            }
        }
        broadcastDataConnectionStateChanged(state, isDataConnectivityPossible, reason, apn,
                apnTypes, interfaceName);
    }
    public void notifyDataConnectionFailed(String reason) {
        if (!checkNotifyPermission("notifyDataConnectionFailed()")) {
            return;
        }
        broadcastDataConnectionFailed(reason);
    }
    public void notifyCellLocation(Bundle cellLocation) {
        if (!checkNotifyPermission("notifyCellLocation()")) {
            return;
        }
        synchronized (mRecords) {
            mCellLocation = cellLocation;
            for (int i = mRecords.size() - 1; i >= 0; i--) {
                Record r = mRecords.get(i);
                if ((r.events & PhoneStateListener.LISTEN_CELL_LOCATION) != 0) {
                    sendCellLocation(r, cellLocation);
                }
            }
        }
    }
    public void sendServiceState(Record r, ServiceState state) {
        try {
            r.callback.onServiceStateChanged(new ServiceState(state));
        } catch (RemoteException ex) {
            remove(r.binder);
        }
    }
    private void sendCellLocation(Record r, Bundle cellLocation) {
        try {
            r.callback.onCellLocationChanged(new Bundle(cellLocation));
        } catch (RemoteException ex) {
            remove(r.binder);
        }
    }
    private void sendSignalStrength(Record r, SignalStrength signalStrength) {
        try {
            r.callback.onSignalStrengthsChanged(new SignalStrength(signalStrength));
        } catch (RemoteException ex) {
            remove(r.binder);
        }
    }
    @Override
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (mContext.checkCallingOrSelfPermission(android.Manifest.permission.DUMP)
                != PackageManager.PERMISSION_GRANTED) {
            pw.println("Permission Denial: can't dump telephony.registry from from pid="
                    + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            return;
        }
        synchronized (mRecords) {
            final int recordCount = mRecords.size();
            pw.println("last known state:");
            pw.println("  mCallState=" + mCallState);
            pw.println("  mCallIncomingNumber=" + mCallIncomingNumber);
            pw.println("  mServiceState=" + mServiceState);
            pw.println("  mSignalStrength=" + mSignalStrength);
            pw.println("  mMessageWaiting=" + mMessageWaiting);
            pw.println("  mCallForwarding=" + mCallForwarding);
            pw.println("  mDataActivity=" + mDataActivity);
            pw.println("  mDataConnectionState=" + mDataConnectionState);
            pw.println("  mDataConnectionPossible=" + mDataConnectionPossible);
            pw.println("  mDataConnectionReason=" + mDataConnectionReason);
            pw.println("  mDataConnectionApn=" + mDataConnectionApn);
            pw.println("  mDataConnectionInterfaceName=" + mDataConnectionInterfaceName);
            pw.println("  mCellLocation=" + mCellLocation);
            pw.println("registrations: count=" + recordCount);
            for (int i = 0; i < recordCount; i++) {
                Record r = mRecords.get(i);
                pw.println("  " + r.pkgForDebug + " 0x" + Integer.toHexString(r.events));
            }
        }
    }
    private void broadcastServiceStateChanged(ServiceState state) {
        long ident = Binder.clearCallingIdentity();
        try {
            mBatteryStats.notePhoneState(state.getState());
        } catch (RemoteException re) {
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
        Intent intent = new Intent(TelephonyIntents.ACTION_SERVICE_STATE_CHANGED);
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        Bundle data = new Bundle();
        state.fillInNotifierBundle(data);
        intent.putExtras(data);
        mContext.sendStickyBroadcast(intent);
    }
    private void broadcastSignalStrengthChanged(SignalStrength signalStrength) {
        long ident = Binder.clearCallingIdentity();
        try {
            mBatteryStats.notePhoneSignalStrength(signalStrength);
        } catch (RemoteException e) {
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
        Intent intent = new Intent(TelephonyIntents.ACTION_SIGNAL_STRENGTH_CHANGED);
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        Bundle data = new Bundle();
        signalStrength.fillInNotifierBundle(data);
        intent.putExtras(data);
        mContext.sendStickyBroadcast(intent);
    }
    private void broadcastCallStateChanged(int state, String incomingNumber) {
        long ident = Binder.clearCallingIdentity();
        try {
            if (state == TelephonyManager.CALL_STATE_IDLE) {
                mBatteryStats.notePhoneOff();
            } else {
                mBatteryStats.notePhoneOn();
            }
        } catch (RemoteException e) {
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
        Intent intent = new Intent(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        intent.putExtra(Phone.STATE_KEY, DefaultPhoneNotifier.convertCallState(state).toString());
        if (!TextUtils.isEmpty(incomingNumber)) {
            intent.putExtra(TelephonyManager.EXTRA_INCOMING_NUMBER, incomingNumber);
        }
        mContext.sendBroadcast(intent, android.Manifest.permission.READ_PHONE_STATE);
    }
    private void broadcastDataConnectionStateChanged(int state,
            boolean isDataConnectivityPossible,
            String reason, String apn, String[] apnTypes, String interfaceName) {
        Intent intent = new Intent(TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED);
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        intent.putExtra(Phone.STATE_KEY, DefaultPhoneNotifier.convertDataState(state).toString());
        if (!isDataConnectivityPossible) {
            intent.putExtra(Phone.NETWORK_UNAVAILABLE_KEY, true);
        }
        if (reason != null) {
            intent.putExtra(Phone.STATE_CHANGE_REASON_KEY, reason);
        }
        intent.putExtra(Phone.DATA_APN_KEY, apn);
        String types = new String("");
        if (apnTypes.length > 0) {
            types = apnTypes[0];
            for (int i = 1; i < apnTypes.length; i++) {
                types = types+","+apnTypes[i];
            }
        }
        intent.putExtra(Phone.DATA_APN_TYPES_KEY, types);
        intent.putExtra(Phone.DATA_IFACE_NAME_KEY, interfaceName);
        mContext.sendStickyBroadcast(intent);
    }
    private void broadcastDataConnectionFailed(String reason) {
        Intent intent = new Intent(TelephonyIntents.ACTION_DATA_CONNECTION_FAILED);
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        intent.putExtra(Phone.FAILURE_REASON_KEY, reason);
        mContext.sendStickyBroadcast(intent);
    }
    private boolean checkNotifyPermission(String method) {
        if (mContext.checkCallingOrSelfPermission(android.Manifest.permission.MODIFY_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        String msg = "Modify Phone State Permission Denial: " + method + " from pid="
                + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid();
        Slog.w(TAG, msg);
        return false;
    }
    private void checkListenerPermission(int events) {
        if ((events & PhoneStateListener.LISTEN_CELL_LOCATION) != 0) {
            mContext.enforceCallingOrSelfPermission(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION, null);
        }
        if ((events & PHONE_STATE_PERMISSION_MASK) != 0) {
            mContext.enforceCallingOrSelfPermission(
                    android.Manifest.permission.READ_PHONE_STATE, null);
        }
    }
}
