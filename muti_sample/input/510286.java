public abstract class SMSDispatcher extends Handler {
    private static final String TAG = "SMS";
    private static final int DEFAULT_SMS_CHECK_PERIOD = 3600000;
    private static final int DEFAULT_SMS_MAX_COUNT = 100;
    private static final int DEFAULT_SMS_TIMOUEOUT = 6000;
    protected static final String[] RAW_PROJECTION = new String[] {
        "pdu",
        "sequence",
        "destination_port",
    };
    static final int MAIL_SEND_SMS = 1;
    static final protected int EVENT_NEW_SMS = 1;
    static final protected int EVENT_SEND_SMS_COMPLETE = 2;
    static final protected int EVENT_SEND_RETRY = 3;
    static final protected int EVENT_NEW_SMS_STATUS_REPORT = 5;
    static final protected int EVENT_ICC_FULL = 6;
    static final protected int EVENT_POST_ALERT = 7;
    static final protected int EVENT_SEND_CONFIRMED_SMS = 8;
    static final protected int EVENT_ALERT_TIMEOUT = 9;
    static final protected int EVENT_STOP_SENDING = 10;
    static final protected int EVENT_REPORT_MEMORY_STATUS_DONE = 11;
    static final protected int EVENT_RADIO_ON = 12;
    protected Phone mPhone;
    protected Context mContext;
    protected ContentResolver mResolver;
    protected CommandsInterface mCm;
    protected final WapPushOverSms mWapPush;
    protected final Uri mRawUri = Uri.withAppendedPath(Telephony.Sms.CONTENT_URI, "raw");
    private static final int MAX_SEND_RETRIES = 3;
    private static final int SEND_RETRY_DELAY = 2000;
    private static final int SINGLE_PART_SMS = 1;
    private static final int MO_MSG_QUEUE_LIMIT = 5;
    private static int sConcatenatedRef;
    private SmsCounter mCounter;
    private ArrayList mSTrackers = new ArrayList(MO_MSG_QUEUE_LIMIT);
    private PowerManager.WakeLock mWakeLock;
    private final int WAKE_LOCK_TIMEOUT = 5000;
    private static SmsMessage mSmsMessage;
    private static SmsMessageBase mSmsMessageBase;
    private SmsMessageBase.SubmitPduBase mSubmitPduBase;
    protected boolean mStorageAvailable = true;
    protected boolean mReportMemoryStatusPending = false;
    protected static int getNextConcatenatedRef() {
        sConcatenatedRef += 1;
        return sConcatenatedRef;
    }
    private class SmsCounter {
        private int mCheckPeriod;
        private int mMaxAllowed;
        private HashMap<String, ArrayList<Long>> mSmsStamp;
        SmsCounter(int mMax, int mPeriod) {
            mMaxAllowed = mMax;
            mCheckPeriod = mPeriod;
            mSmsStamp = new HashMap<String, ArrayList<Long>> ();
        }
        boolean check(String appName, int smsWaiting) {
            if (!mSmsStamp.containsKey(appName)) {
                mSmsStamp.put(appName, new ArrayList<Long>());
            }
            return isUnderLimit(mSmsStamp.get(appName), smsWaiting);
        }
        private boolean isUnderLimit(ArrayList<Long> sent, int smsWaiting) {
            Long ct =  System.currentTimeMillis();
            Log.d(TAG, "SMS send size=" + sent.size() + "time=" + ct);
            while (sent.size() > 0 && (ct - sent.get(0)) > mCheckPeriod ) {
                    sent.remove(0);
            }
            if ( (sent.size() + smsWaiting) <= mMaxAllowed) {
                for (int i = 0; i < smsWaiting; i++ ) {
                    sent.add(ct);
                }
                return true;
            }
            return false;
        }
    }
    protected SMSDispatcher(PhoneBase phone) {
        mPhone = phone;
        mWapPush = new WapPushOverSms(phone, this);
        mContext = phone.getContext();
        mResolver = mContext.getContentResolver();
        mCm = phone.mCM;
        createWakelock();
        int check_period = Settings.Secure.getInt(mResolver,
                Settings.Secure.SMS_OUTGOING_CHECK_INTERVAL_MS,
                DEFAULT_SMS_CHECK_PERIOD);
        int max_count = Settings.Secure.getInt(mResolver,
                Settings.Secure.SMS_OUTGOING_CHECK_MAX_COUNT,
                DEFAULT_SMS_MAX_COUNT);
        mCounter = new SmsCounter(max_count, check_period);
        mCm.setOnNewSMS(this, EVENT_NEW_SMS, null);
        mCm.setOnSmsStatus(this, EVENT_NEW_SMS_STATUS_REPORT, null);
        mCm.setOnIccSmsFull(this, EVENT_ICC_FULL, null);
        mCm.registerForOn(this, EVENT_RADIO_ON, null);
        sConcatenatedRef = new Random().nextInt(256);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_DEVICE_STORAGE_LOW);
        filter.addAction(Intent.ACTION_DEVICE_STORAGE_OK);
        mContext.registerReceiver(mResultReceiver, filter);
    }
    public void dispose() {
        mCm.unSetOnNewSMS(this);
        mCm.unSetOnSmsStatus(this);
        mCm.unSetOnIccSmsFull(this);
        mCm.unregisterForOn(this);
    }
    protected void finalize() {
        Log.d(TAG, "SMSDispatcher finalized");
    }
    protected final ArrayList<SmsTracker> deliveryPendingList = new ArrayList<SmsTracker>();
    @Override
    public void handleMessage(Message msg) {
        AsyncResult ar;
        switch (msg.what) {
        case EVENT_NEW_SMS:
            if (Config.LOGD) {
                Log.d(TAG, "New SMS Message Received");
            }
            SmsMessage sms;
            ar = (AsyncResult) msg.obj;
            if (ar.exception != null) {
                Log.e(TAG, "Exception processing incoming SMS. Exception:" + ar.exception);
                return;
            }
            sms = (SmsMessage) ar.result;
            try {
                int result = dispatchMessage(sms.mWrappedSmsMessage);
                if (result != Activity.RESULT_OK) {
                    boolean handled = (result == Intents.RESULT_SMS_HANDLED);
                    notifyAndAcknowledgeLastIncomingSms(handled, result, null);
                }
            } catch (RuntimeException ex) {
                Log.e(TAG, "Exception dispatching message", ex);
                notifyAndAcknowledgeLastIncomingSms(false, Intents.RESULT_SMS_GENERIC_ERROR, null);
            }
            break;
        case EVENT_SEND_SMS_COMPLETE:
            handleSendComplete((AsyncResult) msg.obj);
            break;
        case EVENT_SEND_RETRY:
            sendSms((SmsTracker) msg.obj);
            break;
        case EVENT_NEW_SMS_STATUS_REPORT:
            handleStatusReport((AsyncResult)msg.obj);
            break;
        case EVENT_ICC_FULL:
            handleIccFull();
            break;
        case EVENT_POST_ALERT:
            handleReachSentLimit((SmsTracker)(msg.obj));
            break;
        case EVENT_ALERT_TIMEOUT:
            ((AlertDialog)(msg.obj)).dismiss();
            msg.obj = null;
            if (mSTrackers.isEmpty() == false) {
                try {
                    SmsTracker sTracker = (SmsTracker)mSTrackers.remove(0);
                    sTracker.mSentIntent.send(RESULT_ERROR_LIMIT_EXCEEDED);
                } catch (CanceledException ex) {
                    Log.e(TAG, "failed to send back RESULT_ERROR_LIMIT_EXCEEDED");
                }
            }
            if (Config.LOGD) {
                Log.d(TAG, "EVENT_ALERT_TIMEOUT, message stop sending");
            }
            break;
        case EVENT_SEND_CONFIRMED_SMS:
            if (mSTrackers.isEmpty() == false) {
                SmsTracker sTracker = (SmsTracker)mSTrackers.remove(mSTrackers.size() - 1);
                if (isMultipartTracker(sTracker)) {
                    sendMultipartSms(sTracker);
                } else {
                    sendSms(sTracker);
                }
                removeMessages(EVENT_ALERT_TIMEOUT, msg.obj);
            }
            break;
        case EVENT_STOP_SENDING:
            if (mSTrackers.isEmpty() == false) {
                try {
                    SmsTracker sTracker = (SmsTracker)mSTrackers.remove(mSTrackers.size() - 1);
                    sTracker.mSentIntent.send(RESULT_ERROR_LIMIT_EXCEEDED);
                } catch (CanceledException ex) {
                    Log.e(TAG, "failed to send back RESULT_ERROR_LIMIT_EXCEEDED");
                }
                removeMessages(EVENT_ALERT_TIMEOUT, msg.obj);
            }
            break;
        case EVENT_REPORT_MEMORY_STATUS_DONE:
            ar = (AsyncResult)msg.obj;
            if (ar.exception != null) {
                mReportMemoryStatusPending = true;
                Log.v(TAG, "Memory status report to modem pending : mStorageAvailable = "
                        + mStorageAvailable);
            } else {
                mReportMemoryStatusPending = false;
            }
            break;
        case EVENT_RADIO_ON:
            if (mReportMemoryStatusPending) {
                Log.v(TAG, "Sending pending memory status report : mStorageAvailable = "
                        + mStorageAvailable);
                mCm.reportSmsMemoryStatus(mStorageAvailable,
                        obtainMessage(EVENT_REPORT_MEMORY_STATUS_DONE));
            }
            break;
        }
    }
    private void createWakelock() {
        PowerManager pm = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SMSDispatcher");
        mWakeLock.setReferenceCounted(true);
    }
    void dispatch(Intent intent, String permission) {
        mWakeLock.acquire(WAKE_LOCK_TIMEOUT);
        mContext.sendOrderedBroadcast(intent, permission, mResultReceiver,
                this, Activity.RESULT_OK, null, null);
    }
    private void handleIccFull(){
        Intent intent = new Intent(Intents.SIM_FULL_ACTION);
        mWakeLock.acquire(WAKE_LOCK_TIMEOUT);
        mContext.sendBroadcast(intent, "android.permission.RECEIVE_SMS");
    }
    protected abstract void handleStatusReport(AsyncResult ar);
    protected void handleSendComplete(AsyncResult ar) {
        SmsTracker tracker = (SmsTracker) ar.userObj;
        PendingIntent sentIntent = tracker.mSentIntent;
        if (ar.exception == null) {
            if (Config.LOGD) {
                Log.d(TAG, "SMS send complete. Broadcasting "
                        + "intent: " + sentIntent);
            }
            if (tracker.mDeliveryIntent != null) {
                int messageRef = ((SmsResponse)ar.result).messageRef;
                tracker.mMessageRef = messageRef;
                deliveryPendingList.add(tracker);
            }
            if (sentIntent != null) {
                try {
                    sentIntent.send(Activity.RESULT_OK);
                } catch (CanceledException ex) {}
            }
        } else {
            if (Config.LOGD) {
                Log.d(TAG, "SMS send failed");
            }
            int ss = mPhone.getServiceState().getState();
            if (ss != ServiceState.STATE_IN_SERVICE) {
                handleNotInService(ss, tracker);
            } else if ((((CommandException)(ar.exception)).getCommandError()
                    == CommandException.Error.SMS_FAIL_RETRY) &&
                   tracker.mRetryCount < MAX_SEND_RETRIES) {
                tracker.mRetryCount++;
                Message retryMsg = obtainMessage(EVENT_SEND_RETRY, tracker);
                sendMessageDelayed(retryMsg, SEND_RETRY_DELAY);
            } else if (tracker.mSentIntent != null) {
                try {
                    Intent fillIn = new Intent();
                    if (ar.result != null) {
                        fillIn.putExtra("errorCode", ((SmsResponse)ar.result).errorCode);
                    }
                    tracker.mSentIntent.send(mContext, RESULT_ERROR_GENERIC_FAILURE, fillIn);
                } catch (CanceledException ex) {}
            }
        }
    }
    protected void handleNotInService(int ss, SmsTracker tracker) {
        if (tracker.mSentIntent != null) {
            try {
                if (ss == ServiceState.STATE_POWER_OFF) {
                    tracker.mSentIntent.send(RESULT_ERROR_RADIO_OFF);
                } else {
                    tracker.mSentIntent.send(RESULT_ERROR_NO_SERVICE);
                }
            } catch (CanceledException ex) {}
        }
    }
    protected abstract int dispatchMessage(SmsMessageBase sms);
    protected int processMessagePart(SmsMessageBase sms,
            SmsHeader.ConcatRef concatRef, SmsHeader.PortAddrs portAddrs) {
        StringBuilder where = new StringBuilder("reference_number =");
        where.append(concatRef.refNumber);
        where.append(" AND address = ?");
        String[] whereArgs = new String[] {sms.getOriginatingAddress()};
        byte[][] pdus = null;
        Cursor cursor = null;
        try {
            cursor = mResolver.query(mRawUri, RAW_PROJECTION, where.toString(), whereArgs, null);
            int cursorCount = cursor.getCount();
            if (cursorCount != concatRef.msgCount - 1) {
                ContentValues values = new ContentValues();
                values.put("date", new Long(sms.getTimestampMillis()));
                values.put("pdu", HexDump.toHexString(sms.getPdu()));
                values.put("address", sms.getOriginatingAddress());
                values.put("reference_number", concatRef.refNumber);
                values.put("count", concatRef.msgCount);
                values.put("sequence", concatRef.seqNumber);
                if (portAddrs != null) {
                    values.put("destination_port", portAddrs.destPort);
                }
                mResolver.insert(mRawUri, values);
                return Intents.RESULT_SMS_HANDLED;
            }
            int pduColumn = cursor.getColumnIndex("pdu");
            int sequenceColumn = cursor.getColumnIndex("sequence");
            pdus = new byte[concatRef.msgCount][];
            for (int i = 0; i < cursorCount; i++) {
                cursor.moveToNext();
                int cursorSequence = (int)cursor.getLong(sequenceColumn);
                pdus[cursorSequence - 1] = HexDump.hexStringToByteArray(
                        cursor.getString(pduColumn));
            }
            pdus[concatRef.seqNumber - 1] = sms.getPdu();
            mResolver.delete(mRawUri, where.toString(), whereArgs);
        } catch (SQLException e) {
            Log.e(TAG, "Can't access multipart SMS database", e);
            return Intents.RESULT_SMS_GENERIC_ERROR;
        } finally {
            if (cursor != null) cursor.close();
        }
        if (portAddrs != null) {
            if (portAddrs.destPort == SmsHeader.PORT_WAP_PUSH) {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                for (int i = 0; i < concatRef.msgCount; i++) {
                    SmsMessage msg = SmsMessage.createFromPdu(pdus[i]);
                    byte[] data = msg.getUserData();
                    output.write(data, 0, data.length);
                }
                return mWapPush.dispatchWapPdu(output.toByteArray());
            } else {
                dispatchPortAddressedPdus(pdus, portAddrs.destPort);
            }
        } else {
            dispatchPdus(pdus);
        }
        return Activity.RESULT_OK;
    }
    protected void dispatchPdus(byte[][] pdus) {
        Intent intent = new Intent(Intents.SMS_RECEIVED_ACTION);
        intent.putExtra("pdus", pdus);
        dispatch(intent, "android.permission.RECEIVE_SMS");
    }
    protected void dispatchPortAddressedPdus(byte[][] pdus, int port) {
        Uri uri = Uri.parse("sms:
        Intent intent = new Intent(Intents.DATA_SMS_RECEIVED_ACTION, uri);
        intent.putExtra("pdus", pdus);
        dispatch(intent, "android.permission.RECEIVE_SMS");
    }
    protected abstract void sendData(String destAddr, String scAddr, int destPort,
            byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent);
    protected abstract void sendText(String destAddr, String scAddr,
            String text, PendingIntent sentIntent, PendingIntent deliveryIntent);
    protected abstract void sendMultipartText(String destAddr, String scAddr,
            ArrayList<String> parts, ArrayList<PendingIntent> sentIntents,
            ArrayList<PendingIntent> deliveryIntents);
    protected void sendRawPdu(byte[] smsc, byte[] pdu, PendingIntent sentIntent,
            PendingIntent deliveryIntent) {
        if (pdu == null) {
            if (sentIntent != null) {
                try {
                    sentIntent.send(RESULT_ERROR_NULL_PDU);
                } catch (CanceledException ex) {}
            }
            return;
        }
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("smsc", smsc);
        map.put("pdu", pdu);
        SmsTracker tracker = new SmsTracker(map, sentIntent,
                deliveryIntent);
        int ss = mPhone.getServiceState().getState();
        if (ss != ServiceState.STATE_IN_SERVICE) {
            handleNotInService(ss, tracker);
        } else {
            String appName = getAppNameByIntent(sentIntent);
            if (mCounter.check(appName, SINGLE_PART_SMS)) {
                sendSms(tracker);
            } else {
                sendMessage(obtainMessage(EVENT_POST_ALERT, tracker));
            }
        }
    }
    protected void handleReachSentLimit(SmsTracker tracker) {
        if (mSTrackers.size() >= MO_MSG_QUEUE_LIMIT) {
            try {
                tracker.mSentIntent.send(RESULT_ERROR_LIMIT_EXCEEDED);
            } catch (CanceledException ex) {
                Log.e(TAG, "failed to send back RESULT_ERROR_LIMIT_EXCEEDED");
            }
            return;
        }
        Resources r = Resources.getSystem();
        String appName = getAppNameByIntent(tracker.mSentIntent);
        AlertDialog d = new AlertDialog.Builder(mContext)
                .setTitle(r.getString(R.string.sms_control_title))
                .setMessage(appName + " " + r.getString(R.string.sms_control_message))
                .setPositiveButton(r.getString(R.string.sms_control_yes), mListener)
                .setNegativeButton(r.getString(R.string.sms_control_no), mListener)
                .create();
        d.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        d.show();
        mSTrackers.add(tracker);
        sendMessageDelayed ( obtainMessage(EVENT_ALERT_TIMEOUT, d),
                DEFAULT_SMS_TIMOUEOUT);
    }
    protected String getAppNameByIntent(PendingIntent intent) {
        Resources r = Resources.getSystem();
        return (intent != null) ? intent.getTargetPackage()
            : r.getString(R.string.sms_control_default_app_name);
    }
    protected abstract void sendSms(SmsTracker tracker);
    protected abstract void sendMultipartSms (SmsTracker tracker);
    protected abstract void activateCellBroadcastSms(int activate, Message response);
    protected abstract void getCellBroadcastSmsConfig(Message response);
    protected abstract void setCellBroadcastConfig(int[] configValuesArray, Message response);
    protected abstract void acknowledgeLastIncomingSms(boolean success,
            int result, Message response);
    private void notifyAndAcknowledgeLastIncomingSms(boolean success,
            int result, Message response) {
        if (!success) {
            Intent intent = new Intent(Intents.SMS_REJECTED_ACTION);
            intent.putExtra("result", result);
            mWakeLock.acquire(WAKE_LOCK_TIMEOUT);
            mContext.sendBroadcast(intent, "android.permission.RECEIVE_SMS");
        }
        acknowledgeLastIncomingSms(success, result, response);
    }
    private boolean isMultipartTracker (SmsTracker tracker) {
        HashMap map = tracker.mData;
        return ( map.get("parts") != null);
    }
    static protected class SmsTracker {
        public HashMap mData;
        public int mRetryCount;
        public int mMessageRef;
        public PendingIntent mSentIntent;
        public PendingIntent mDeliveryIntent;
        SmsTracker(HashMap data, PendingIntent sentIntent,
                PendingIntent deliveryIntent) {
            mData = data;
            mSentIntent = sentIntent;
            mDeliveryIntent = deliveryIntent;
            mRetryCount = 0;
        }
    }
    protected SmsTracker SmsTrackerFactory(HashMap data, PendingIntent sentIntent,
            PendingIntent deliveryIntent) {
        return new SmsTracker(data, sentIntent, deliveryIntent);
    }
    private DialogInterface.OnClickListener mListener =
        new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    Log.d(TAG, "click YES to send out sms");
                    sendMessage(obtainMessage(EVENT_SEND_CONFIRMED_SMS));
                } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                    Log.d(TAG, "click NO to stop sending");
                    sendMessage(obtainMessage(EVENT_STOP_SENDING));
                }
            }
        };
        private BroadcastReceiver mResultReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_DEVICE_STORAGE_LOW)) {
                    mStorageAvailable = false;
                    mCm.reportSmsMemoryStatus(false, obtainMessage(EVENT_REPORT_MEMORY_STATUS_DONE));
                } else if (intent.getAction().equals(Intent.ACTION_DEVICE_STORAGE_OK)) {
                    mStorageAvailable = true;
                    mCm.reportSmsMemoryStatus(true, obtainMessage(EVENT_REPORT_MEMORY_STATUS_DONE));
                } else {
                    int rc = getResultCode();
                    boolean success = (rc == Activity.RESULT_OK)
                                        || (rc == Intents.RESULT_SMS_HANDLED);
                    acknowledgeLastIncomingSms(success, rc, null);
                }
            }
        };
}
