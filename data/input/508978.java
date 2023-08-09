public class AndroidSmsService implements SmsService {
    private static final String TAG = RemoteImService.TAG;
    private static final String SMS_STATUS_RECEIVED_ACTION =
        "com.android.im.SmsService.SMS_STATUS_RECEIVED";
    private static final int sMaxSmsLength =
        SmsMessage.MAX_USER_DATA_BYTES - 7;
    private Context mContext;
    private SmsReceiver mSmsReceiver;
    private IntentFilter mIntentFilter;
    private boolean mStarted;
    HashMap<Integer, ListenerList> mListeners;
    HashMap<Long, SmsSendFailureCallback> mFailureCallbacks;
    public AndroidSmsService(Context context) {
        mContext = context;
        mSmsReceiver = new SmsReceiver();
        mIntentFilter = new IntentFilter(
                Telephony.Sms.Intents.DATA_SMS_RECEIVED_ACTION);
        mIntentFilter.addDataScheme("sms");
        mListeners = new HashMap<Integer, ListenerList>();
        mFailureCallbacks = new HashMap<Long, SmsSendFailureCallback>();
    }
    public int getMaxSmsLength() {
        return sMaxSmsLength;
    }
    public void sendSms(String dest, int port, byte[] data) {
        sendSms(dest, port, data, null);
    }
    public void sendSms(String dest, int port, byte[] data,
            SmsSendFailureCallback callback) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            try {
                log(dest + ":" + port + " >>> " + new String(data, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
            }
        }
        if (data.length > sMaxSmsLength) {
            Log.e(TAG, "SMS data message can only contain " + sMaxSmsLength
                    + " bytes");
            return;
        }
        SmsManager smsManager = SmsManager.getDefault();
        PendingIntent sentIntent;
        if (callback == null) {
            sentIntent = null;
        } else {
            long msgId = genMsgId();
            mFailureCallbacks.put(msgId, callback);
            sentIntent = PendingIntent.getBroadcast(mContext, 0,
                new Intent(
                        SMS_STATUS_RECEIVED_ACTION,
                        Uri.parse("content:
                        mContext, SmsReceiver.class),
                0);
        }
        smsManager.sendDataMessage(dest, null,
                (short) port, data,
                sentIntent,
                null);
    }
    public void addSmsListener(String from, int port, SmsListener listener) {
        ListenerList l = mListeners.get(port);
        if (l == null) {
            l = new ListenerList(port);
            mListeners.put(port, l);
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                log("Register SMS receiver on port " + port);
            }
            mIntentFilter.addDataAuthority("*", String.valueOf(port));
            mContext.registerReceiver(mSmsReceiver, mIntentFilter);
            synchronized (this) {
                mStarted = true;
            }
        }
        l.addListener(from, listener);
    }
    public void removeSmsListener(SmsListener listener) {
        Iterator<ListenerList> iter = mListeners.values().iterator();
        while (iter.hasNext()) {
            ListenerList l = iter.next();
            l.removeListener(listener);
            if (l.isEmpty()) {
                iter.remove();
            }
        }
    }
    public synchronized void stop() {
        if (mStarted) {
            mContext.unregisterReceiver(mSmsReceiver);
            mStarted = false;
        }
    }
    private static long sNextMsgId = 0;
    private static synchronized long genMsgId() {
        return sNextMsgId++;
    }
    private static void log(String msg) {
        Log.d(TAG, "[SmsService]" + msg);
    }
    private final class SmsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (SMS_STATUS_RECEIVED_ACTION.equals(intent.getAction())) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    log("send status received");
                }
                long id = ContentUris.parseId(intent.getData());
                SmsSendFailureCallback callback = mFailureCallbacks.get(id);
                if (callback == null) {
                    return;
                }
                int resultCode = getResultCode();
                if (resultCode == SmsManager.RESULT_ERROR_GENERIC_FAILURE) {
                    callback.onFailure(SmsSendFailureCallback.ERROR_GENERIC_FAILURE);
                } else if (resultCode == SmsManager.RESULT_ERROR_RADIO_OFF) {
                    callback.onFailure(SmsSendFailureCallback.ERROR_RADIO_OFF);
                }
                mFailureCallbacks.remove(id);
            } else if (DATA_SMS_RECEIVED_ACTION.equals(intent.getAction())){
                Uri uri = intent.getData();
                int port = uri.getPort();
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    log("Received sms on port:" + port);
                }
                ListenerList listeners = mListeners.get(port);
                if (listeners == null) {
                    if (Log.isLoggable(TAG, Log.DEBUG)) {
                        log("No listener on port " + port + ", ignore");
                    }
                    return;
                }
                SmsMessage[] receivedSms
                    = Telephony.Sms.Intents.getMessagesFromIntent(intent);
                for (SmsMessage msg : receivedSms) {
                    String from = msg.getOriginatingAddress();
                    byte[] data = msg.getUserData();
                    if (Log.isLoggable(TAG, Log.DEBUG)) {
                        try {
                            log(from + ":" + port + " <<< " + new String(data, "UTF-8"));
                        } catch (UnsupportedEncodingException e) {
                        }
                    }
                    listeners.notifySms(from, data);
                }
            }
        }
    }
    private final static class ListenerList {
        private int mPort;
        private ArrayList<String> mAddrList;
        private ArrayList<SmsListener> mListenerList;
        public ListenerList(int port) {
            mPort = port;
            mAddrList = new ArrayList<String>();
            mListenerList = new ArrayList<SmsListener>();
        }
        public synchronized void addListener(String addr, SmsListener listener) {
            mAddrList.add(addr);
            mListenerList.add(listener);
        }
        public synchronized void removeListener(SmsListener listener) {
            int index = -1;
            while ((index = mListenerList.indexOf(listener)) != -1) {
                mAddrList.remove(index);
                mListenerList.remove(index);
            }
        }
        public void notifySms(String addr, byte[] data) {
            int N = mListenerList.size();
            for (int i = 0; i < N; i++) {
                String listenAddr = mAddrList.get(i);
                if (ANY_ADDRESS.equals(listenAddr)
                        || addr.equals(listenAddr)
                        || PhoneNumberUtils.compare(addr, listenAddr)) {
                    mListenerList.get(i).onIncomingSms(data);
                }
            }
        }
        public boolean isEmpty() {
            return mListenerList.isEmpty();
        }
        public int getPort() {
            return mPort;
        }
    }
}
