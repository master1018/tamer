public class PhoneStateListener {
    public static final int LISTEN_NONE = 0;
    public static final int LISTEN_SERVICE_STATE                            = 0x00000001;
    @Deprecated
    public static final int LISTEN_SIGNAL_STRENGTH                          = 0x00000002;
    public static final int LISTEN_MESSAGE_WAITING_INDICATOR                = 0x00000004;
    public static final int LISTEN_CALL_FORWARDING_INDICATOR                = 0x00000008;
    public static final int LISTEN_CELL_LOCATION                            = 0x00000010;
    public static final int LISTEN_CALL_STATE                               = 0x00000020;
    public static final int LISTEN_DATA_CONNECTION_STATE                    = 0x00000040;
    public static final int LISTEN_DATA_ACTIVITY                            = 0x00000080;
    public static final int LISTEN_SIGNAL_STRENGTHS                         = 0x00000100;
    public PhoneStateListener() {
    }
    public void onServiceStateChanged(ServiceState serviceState) {
    }
    @Deprecated
    public void onSignalStrengthChanged(int asu) {
    }
    public void onMessageWaitingIndicatorChanged(boolean mwi) {
    }
    public void onCallForwardingIndicatorChanged(boolean cfi) {
    }
    public void onCellLocationChanged(CellLocation location) {
    }
    public void onCallStateChanged(int state, String incomingNumber) {
    }
    public void onDataConnectionStateChanged(int state) {
    }
    public void onDataConnectionStateChanged(int state, int networkType) {
    }
    public void onDataActivity(int direction) {
    }
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
    }
    IPhoneStateListener callback = new IPhoneStateListener.Stub() {
        public void onServiceStateChanged(ServiceState serviceState) {
            Message.obtain(mHandler, LISTEN_SERVICE_STATE, 0, 0, serviceState).sendToTarget();
        }
        public void onSignalStrengthChanged(int asu) {
            Message.obtain(mHandler, LISTEN_SIGNAL_STRENGTH, asu, 0, null).sendToTarget();
        }
        public void onMessageWaitingIndicatorChanged(boolean mwi) {
            Message.obtain(mHandler, LISTEN_MESSAGE_WAITING_INDICATOR, mwi ? 1 : 0, 0, null)
                    .sendToTarget();
        }
        public void onCallForwardingIndicatorChanged(boolean cfi) {
            Message.obtain(mHandler, LISTEN_CALL_FORWARDING_INDICATOR, cfi ? 1 : 0, 0, null)
                    .sendToTarget();
        }
        public void onCellLocationChanged(Bundle bundle) {
            CellLocation location = CellLocation.newFromBundle(bundle);
            Message.obtain(mHandler, LISTEN_CELL_LOCATION, 0, 0, location).sendToTarget();
        }
        public void onCallStateChanged(int state, String incomingNumber) {
            Message.obtain(mHandler, LISTEN_CALL_STATE, state, 0, incomingNumber).sendToTarget();
        }
        public void onDataConnectionStateChanged(int state, int networkType) {
            Message.obtain(mHandler, LISTEN_DATA_CONNECTION_STATE, state, networkType, null).
                    sendToTarget();
        }
        public void onDataActivity(int direction) {
            Message.obtain(mHandler, LISTEN_DATA_ACTIVITY, direction, 0, null).sendToTarget();
        }
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            Message.obtain(mHandler, LISTEN_SIGNAL_STRENGTHS, 0, 0, signalStrength).sendToTarget();
        }
    };
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LISTEN_SERVICE_STATE:
                    PhoneStateListener.this.onServiceStateChanged((ServiceState)msg.obj);
                    break;
                case LISTEN_SIGNAL_STRENGTH:
                    PhoneStateListener.this.onSignalStrengthChanged(msg.arg1);
                    break;
                case LISTEN_MESSAGE_WAITING_INDICATOR:
                    PhoneStateListener.this.onMessageWaitingIndicatorChanged(msg.arg1 != 0);
                    break;
                case LISTEN_CALL_FORWARDING_INDICATOR:
                    PhoneStateListener.this.onCallForwardingIndicatorChanged(msg.arg1 != 0);
                    break;
                case LISTEN_CELL_LOCATION:
                    PhoneStateListener.this.onCellLocationChanged((CellLocation)msg.obj);
                    break;
                case LISTEN_CALL_STATE:
                    PhoneStateListener.this.onCallStateChanged(msg.arg1, (String)msg.obj);
                    break;
                case LISTEN_DATA_CONNECTION_STATE:
                    PhoneStateListener.this.onDataConnectionStateChanged(msg.arg1, msg.arg2);
                    PhoneStateListener.this.onDataConnectionStateChanged(msg.arg1);
                    break;
                case LISTEN_DATA_ACTIVITY:
                    PhoneStateListener.this.onDataActivity(msg.arg1);
                    break;
                case LISTEN_SIGNAL_STRENGTHS:
                    PhoneStateListener.this.onSignalStrengthsChanged((SignalStrength)msg.obj);
                    break;
            }
        }
    };
}
