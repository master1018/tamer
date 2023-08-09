public class SmsReceiverService extends Service {
    private static final String TAG = "SmsReceiverService";
    private ServiceHandler mServiceHandler;
    private Looper mServiceLooper;
    private boolean mSending;
    public static final String MESSAGE_SENT_ACTION =
        "com.android.mms.transaction.MESSAGE_SENT";
    public static final String EXTRA_MESSAGE_SENT_SEND_NEXT ="SendNextMsg";
    public static final String ACTION_SEND_MESSAGE =
        "com.android.mms.transaction.SEND_MESSAGE";
    private static final String[] SEND_PROJECTION = new String[] {
        Sms._ID,        
        Sms.THREAD_ID,  
        Sms.ADDRESS,    
        Sms.BODY,       
        Sms.STATUS,     
    };
    public Handler mToastHandler = new Handler();
    private static final int SEND_COLUMN_ID         = 0;
    private static final int SEND_COLUMN_THREAD_ID  = 1;
    private static final int SEND_COLUMN_ADDRESS    = 2;
    private static final int SEND_COLUMN_BODY       = 3;
    private static final int SEND_COLUMN_STATUS     = 4;
    private int mResultCode;
    @Override
    public void onCreate() {
        HandlerThread thread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mResultCode = intent != null ? intent.getIntExtra("result", 0) : 0;
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);
        return Service.START_NOT_STICKY;
    }
    @Override
    public void onDestroy() {
        mServiceLooper.quit();
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            int serviceId = msg.arg1;
            Intent intent = (Intent)msg.obj;
            if (intent != null) {
                String action = intent.getAction();
                int error = intent.getIntExtra("errorCode", 0);
                if (MESSAGE_SENT_ACTION.equals(intent.getAction())) {
                    handleSmsSent(intent, error);
                } else if (SMS_RECEIVED_ACTION.equals(action)) {
                    handleSmsReceived(intent, error);
                } else if (ACTION_BOOT_COMPLETED.equals(action)) {
                    handleBootCompleted();
                } else if (TelephonyIntents.ACTION_SERVICE_STATE_CHANGED.equals(action)) {
                    handleServiceStateChanged(intent);
                } else if (ACTION_SEND_MESSAGE.endsWith(action)) {
                    handleSendMessage();
                }
            }
            SmsReceiver.finishStartingService(SmsReceiverService.this, serviceId);
        }
    }
    private void handleServiceStateChanged(Intent intent) {
        ServiceState serviceState = ServiceState.newFromBundle(intent.getExtras());
        if (serviceState.getState() == ServiceState.STATE_IN_SERVICE) {
            sendFirstQueuedMessage();
        }
    }
    private void handleSendMessage() {
        if (!mSending) {
            sendFirstQueuedMessage();
        }
    }
    public synchronized void sendFirstQueuedMessage() {
        boolean success = true;
        final Uri uri = Uri.parse("content:
        ContentResolver resolver = getContentResolver();
        Cursor c = SqliteWrapper.query(this, resolver, uri,
                        SEND_PROJECTION, null, null, "date ASC");   
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    String msgText = c.getString(SEND_COLUMN_BODY);
                    String address = c.getString(SEND_COLUMN_ADDRESS);
                    int threadId = c.getInt(SEND_COLUMN_THREAD_ID);
                    int status = c.getInt(SEND_COLUMN_STATUS);
                    int msgId = c.getInt(SEND_COLUMN_ID);
                    Uri msgUri = ContentUris.withAppendedId(Sms.CONTENT_URI, msgId);
                    SmsMessageSender sender = new SmsSingleRecipientSender(this,
                            address, msgText, threadId, status == Sms.STATUS_PENDING,
                            msgUri);
                    if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                        Log.v(TAG, "sendFirstQueuedMessage " + msgUri +
                                ", address: " + address +
                                ", threadId: " + threadId +
                                ", body: " + msgText);
                    }
                    try {
                        sender.sendMessage(SendingProgressTokenManager.NO_TOKEN);;
                        mSending = true;
                    } catch (MmsException e) {
                        Log.e(TAG, "sendFirstQueuedMessage: failed to send message " + msgUri
                                + ", caught ", e);
                        mSending = false;
                        messageFailedToSend(msgUri, SmsManager.RESULT_ERROR_GENERIC_FAILURE);
                        success = false;
                    }
                }
            } finally {
                c.close();
            }
        }
        if (success) {
            unRegisterForServiceStateChanges();
        }
    }
    private void handleSmsSent(Intent intent, int error) {
        Uri uri = intent.getData();
        mSending = false;
        boolean sendNextMsg = intent.getBooleanExtra(EXTRA_MESSAGE_SENT_SEND_NEXT, false);
        if (mResultCode == Activity.RESULT_OK) {
            if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                Log.v(TAG, "handleSmsSent sending uri: " + uri);
            }
            if (!Sms.moveMessageToFolder(this, uri, Sms.MESSAGE_TYPE_SENT, error)) {
                Log.e(TAG, "handleSmsSent: failed to move message " + uri + " to sent folder");
            }
            if (sendNextMsg) {
                sendFirstQueuedMessage();
            }
            MessagingNotification.updateSendFailedNotification(this);
        } else if ((mResultCode == SmsManager.RESULT_ERROR_RADIO_OFF) ||
                (mResultCode == SmsManager.RESULT_ERROR_NO_SERVICE)) {
            if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                Log.v(TAG, "handleSmsSent: no service, queuing message w/ uri: " + uri);
            }
            registerForServiceStateChanges();
            Sms.moveMessageToFolder(this, uri, Sms.MESSAGE_TYPE_QUEUED, error);
            mToastHandler.post(new Runnable() {
                public void run() {
                    Toast.makeText(SmsReceiverService.this, getString(R.string.message_queued),
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            messageFailedToSend(uri, error);
            if (sendNextMsg) {
                sendFirstQueuedMessage();
            }
        }
    }
    private void messageFailedToSend(Uri uri, int error) {
        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
            Log.v(TAG, "messageFailedToSend msg failed uri: " + uri);
        }
        Sms.moveMessageToFolder(this, uri, Sms.MESSAGE_TYPE_FAILED, error);
        MessagingNotification.notifySendFailed(getApplicationContext(), true);
    }
    private void handleSmsReceived(Intent intent, int error) {
        SmsMessage[] msgs = Intents.getMessagesFromIntent(intent);
        Uri messageUri = insertMessage(this, msgs, error);
        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
            SmsMessage sms = msgs[0];
            Log.v(TAG, "handleSmsReceived" + (sms.isReplace() ? "(replace)" : "") +
                    " messageUri: " + messageUri +
                    ", address: " + sms.getOriginatingAddress() +
                    ", body: " + sms.getMessageBody());
        }
        if (messageUri != null) {
            MessagingNotification.blockingUpdateNewMessageIndicator(this, true, false);
        }
    }
    private void handleBootCompleted() {
        moveOutboxMessagesToQueuedBox();
        sendFirstQueuedMessage();
        MessagingNotification.blockingUpdateNewMessageIndicator(this, true, false);
    }
    private void moveOutboxMessagesToQueuedBox() {
        ContentValues values = new ContentValues(1);
        values.put(Sms.TYPE, Sms.MESSAGE_TYPE_QUEUED);
        SqliteWrapper.update(
                getApplicationContext(), getContentResolver(), Outbox.CONTENT_URI,
                values, "type = " + Sms.MESSAGE_TYPE_OUTBOX, null);
    }
    public static final String CLASS_ZERO_BODY_KEY = "CLASS_ZERO_BODY";
    private final static String[] REPLACE_PROJECTION = new String[] {
        Sms._ID,
        Sms.ADDRESS,
        Sms.PROTOCOL
    };
    private static final int REPLACE_COLUMN_ID = 0;
    private Uri insertMessage(Context context, SmsMessage[] msgs, int error) {
        SmsMessage sms = msgs[0];
        if (sms.getMessageClass() == SmsMessage.MessageClass.CLASS_0) {
            displayClassZeroMessage(context, sms);
            return null;
        } else if (sms.isReplace()) {
            return replaceMessage(context, msgs, error);
        } else {
            return storeMessage(context, msgs, error);
        }
    }
    private Uri replaceMessage(Context context, SmsMessage[] msgs, int error) {
        SmsMessage sms = msgs[0];
        ContentValues values = extractContentValues(sms);
        values.put(Inbox.BODY, sms.getMessageBody());
        values.put(Sms.ERROR_CODE, error);
        ContentResolver resolver = context.getContentResolver();
        String originatingAddress = sms.getOriginatingAddress();
        int protocolIdentifier = sms.getProtocolIdentifier();
        String selection =
                Sms.ADDRESS + " = ? AND " +
                Sms.PROTOCOL + " = ?";
        String[] selectionArgs = new String[] {
            originatingAddress, Integer.toString(protocolIdentifier)
        };
        Cursor cursor = SqliteWrapper.query(context, resolver, Inbox.CONTENT_URI,
                            REPLACE_PROJECTION, selection, selectionArgs, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    long messageId = cursor.getLong(REPLACE_COLUMN_ID);
                    Uri messageUri = ContentUris.withAppendedId(
                            Sms.CONTENT_URI, messageId);
                    SqliteWrapper.update(context, resolver, messageUri,
                                        values, null, null);
                    return messageUri;
                }
            } finally {
                cursor.close();
            }
        }
        return storeMessage(context, msgs, error);
    }
    private Uri storeMessage(Context context, SmsMessage[] msgs, int error) {
        SmsMessage sms = msgs[0];
        ContentValues values = extractContentValues(sms);
        values.put(Sms.ERROR_CODE, error);
        int pduCount = msgs.length;
        if (pduCount == 1) {
            values.put(Inbox.BODY, sms.getDisplayMessageBody());
        } else {
            StringBuilder body = new StringBuilder();
            for (int i = 0; i < pduCount; i++) {
                sms = msgs[i];
                body.append(sms.getDisplayMessageBody());
            }
            values.put(Inbox.BODY, body.toString());
        }
        Long threadId = values.getAsLong(Sms.THREAD_ID);
        String address = values.getAsString(Sms.ADDRESS);
        Contact cacheContact = Contact.get(address,true);
        if (cacheContact != null) {
            address = cacheContact.getNumber();
        }
        if (((threadId == null) || (threadId == 0)) && (address != null)) {
            threadId = Threads.getOrCreateThreadId(context, address);
            values.put(Sms.THREAD_ID, threadId);
        }
        ContentResolver resolver = context.getContentResolver();
        Uri insertedUri = SqliteWrapper.insert(context, resolver, Inbox.CONTENT_URI, values);
        Recycler.getSmsRecycler().deleteOldMessagesByThreadId(getApplicationContext(), threadId);
        return insertedUri;
    }
    private ContentValues extractContentValues(SmsMessage sms) {
        ContentValues values = new ContentValues();
        values.put(Inbox.ADDRESS, sms.getDisplayOriginatingAddress());
        values.put(Inbox.DATE, new Long(System.currentTimeMillis()));
        values.put(Inbox.PROTOCOL, sms.getProtocolIdentifier());
        values.put(Inbox.READ, 0);
        values.put(Inbox.SEEN, 0);
        if (sms.getPseudoSubject().length() > 0) {
            values.put(Inbox.SUBJECT, sms.getPseudoSubject());
        }
        values.put(Inbox.REPLY_PATH_PRESENT, sms.isReplyPathPresent() ? 1 : 0);
        values.put(Inbox.SERVICE_CENTER, sms.getServiceCenterAddress());
        return values;
    }
    private void displayClassZeroMessage(Context context, SmsMessage sms) {
        Intent smsDialogIntent = new Intent(context, ClassZeroActivity.class)
                .putExtra("pdu", sms.getPdu())
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                          | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        context.startActivity(smsDialogIntent);
    }
    private void registerForServiceStateChanges() {
        Context context = getApplicationContext();
        unRegisterForServiceStateChanges();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TelephonyIntents.ACTION_SERVICE_STATE_CHANGED);
        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
            Log.v(TAG, "registerForServiceStateChanges");
        }
        context.registerReceiver(SmsReceiver.getInstance(), intentFilter);
    }
    private void unRegisterForServiceStateChanges() {
        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
            Log.v(TAG, "unRegisterForServiceStateChanges");
        }
        try {
            Context context = getApplicationContext();
            context.unregisterReceiver(SmsReceiver.getInstance());
        } catch (IllegalArgumentException e) {
        }
    }
}
