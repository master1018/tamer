public class BluetoothOppTransfer implements BluetoothOppBatch.BluetoothOppBatchListener {
    private static final String TAG = "BtOppTransfer";
    private static final boolean D = Constants.DEBUG;
    private static final boolean V = Constants.VERBOSE;
    public static final int RFCOMM_ERROR = 10;
    public static final int RFCOMM_CONNECTED = 11;
    public static final int SDP_RESULT = 12;
    private static final int CONNECT_WAIT_TIMEOUT = 45000;
    private static final int CONNECT_RETRY_TIME = 100;
    private static final short OPUSH_UUID16 = 0x1105;
    private Context mContext;
    private BluetoothAdapter mAdapter;
    private BluetoothOppBatch mBatch;
    private BluetoothOppObexSession mSession;
    private BluetoothOppShareInfo mCurrentShare;
    private ObexTransport mTransport;
    private HandlerThread mHandlerThread;
    private EventHandler mSessionHandler;
    private PowerManager mPowerManager;
    private long mTimestamp;
    public BluetoothOppTransfer(Context context, PowerManager powerManager,
            BluetoothOppBatch batch, BluetoothOppObexSession session) {
        mContext = context;
        mPowerManager = powerManager;
        mBatch = batch;
        mSession = session;
        mBatch.registerListern(this);
        mAdapter = BluetoothAdapter.getDefaultAdapter();
    }
    public BluetoothOppTransfer(Context context, PowerManager powerManager, BluetoothOppBatch batch) {
        this(context, powerManager, batch, null);
    }
    public int getBatchId() {
        return mBatch.mId;
    }
    private class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDP_RESULT:
                    if (V) Log.v(TAG, "SDP request returned " + msg.arg1 + " (" +
                            (System.currentTimeMillis() - mTimestamp + " ms)"));
                    if (!((BluetoothDevice)msg.obj).equals(mBatch.mDestination)) {
                        return;
                    }
                    try {
                        mContext.unregisterReceiver(mReceiver);
                    } catch (IllegalArgumentException e) {
                    }
                    if (msg.arg1 > 0) {
                        mConnectThread = new SocketConnectThread(mBatch.mDestination, msg.arg1);
                        mConnectThread.start();
                    } else {
                        Log.e(TAG, "SDP query failed!");
                        markBatchFailed(BluetoothShare.STATUS_CONNECTION_ERROR);
                        mBatch.mStatus = Constants.BATCH_STATUS_FAILED;
                    }
                    break;
                case RFCOMM_ERROR:
                    if (V) Log.v(TAG, "receive RFCOMM_ERROR msg");
                    mConnectThread = null;
                    markBatchFailed(BluetoothShare.STATUS_CONNECTION_ERROR);
                    mBatch.mStatus = Constants.BATCH_STATUS_FAILED;
                    break;
                case RFCOMM_CONNECTED:
                    if (V) Log.v(TAG, "Transfer receive RFCOMM_CONNECTED msg");
                    mConnectThread = null;
                    mTransport = (ObexTransport)msg.obj;
                    startObexSession();
                    break;
                case BluetoothOppObexSession.MSG_SHARE_COMPLETE:
                    BluetoothOppShareInfo info = (BluetoothOppShareInfo)msg.obj;
                    if (V) Log.v(TAG, "receive MSG_SHARE_COMPLETE for info " + info.mId);
                    if (mBatch.mDirection == BluetoothShare.DIRECTION_OUTBOUND) {
                        mCurrentShare = mBatch.getPendingShare();
                        if (mCurrentShare != null) {
                            if (V) Log.v(TAG, "continue session for info " + mCurrentShare.mId +
                                    " from batch " + mBatch.mId);
                            processCurrentShare();
                        } else {
                            if (V) Log.v(TAG, "Batch " + mBatch.mId + " is done");
                            mSession.stop();
                        }
                    }
                    break;
                case BluetoothOppObexSession.MSG_SESSION_COMPLETE:
                    BluetoothOppShareInfo info1 = (BluetoothOppShareInfo)msg.obj;
                    if (V) Log.v(TAG, "receive MSG_SESSION_COMPLETE for batch " + mBatch.mId);
                    mBatch.mStatus = Constants.BATCH_STATUS_FINISHED;
                    tickShareStatus(info1);
                    break;
                case BluetoothOppObexSession.MSG_SESSION_ERROR:
                    if (V) Log.v(TAG, "receive MSG_SESSION_ERROR for batch " + mBatch.mId);
                    BluetoothOppShareInfo info2 = (BluetoothOppShareInfo)msg.obj;
                    mSession.stop();
                    mBatch.mStatus = Constants.BATCH_STATUS_FAILED;
                    markBatchFailed(info2.mStatus);
                    tickShareStatus(mCurrentShare);
                    break;
                case BluetoothOppObexSession.MSG_SHARE_INTERRUPTED:
                    if (V) Log.v(TAG, "receive MSG_SHARE_INTERRUPTED for batch " + mBatch.mId);
                    BluetoothOppShareInfo info3 = (BluetoothOppShareInfo)msg.obj;
                    if (mBatch.mDirection == BluetoothShare.DIRECTION_OUTBOUND) {
                        try {
                            if (mTransport == null) {
                                Log.v(TAG, "receive MSG_SHARE_INTERRUPTED but mTransport = null");
                            } else {
                                mTransport.close();
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "failed to close mTransport");
                        }
                        if (V) Log.v(TAG, "mTransport closed ");
                        mBatch.mStatus = Constants.BATCH_STATUS_FAILED;
                        if (info3 != null) {
                            markBatchFailed(info3.mStatus);
                        } else {
                            markBatchFailed();
                        }
                        tickShareStatus(mCurrentShare);
                    }
                    break;
                case BluetoothOppObexSession.MSG_CONNECT_TIMEOUT:
                    if (V) Log.v(TAG, "receive MSG_CONNECT_TIMEOUT for batch " + mBatch.mId);
                    if (mBatch.mDirection == BluetoothShare.DIRECTION_OUTBOUND) {
                        try {
                            if (mTransport == null) {
                                Log.v(TAG, "receive MSG_SHARE_INTERRUPTED but mTransport = null");
                            } else {
                                mTransport.close();
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "failed to close mTransport");
                        }
                        if (V) Log.v(TAG, "mTransport closed ");
                    } else {
                        NotificationManager nm = (NotificationManager)mContext
                                .getSystemService(Context.NOTIFICATION_SERVICE);
                        nm.cancel(mCurrentShare.mId);
                        Intent in = new Intent(BluetoothShare.USER_CONFIRMATION_TIMEOUT_ACTION);
                        mContext.sendBroadcast(in);
                        markShareTimeout(mCurrentShare);
                    }
                    break;
            }
        }
    }
    private void markShareTimeout(BluetoothOppShareInfo share) {
        Uri contentUri = Uri.parse(BluetoothShare.CONTENT_URI + "/" + share.mId);
        ContentValues updateValues = new ContentValues();
        updateValues
                .put(BluetoothShare.USER_CONFIRMATION, BluetoothShare.USER_CONFIRMATION_TIMEOUT);
        mContext.getContentResolver().update(contentUri, updateValues, null, null);
    }
    private void markBatchFailed(int failReason) {
        synchronized (this) {
            try {
                wait(1000);
            } catch (InterruptedException e) {
                if (V) Log.v(TAG, "Interrupted waiting for markBatchFailed");
            }
        }
        if (D) Log.d(TAG, "Mark all ShareInfo in the batch as failed");
        if (mCurrentShare != null) {
            if (V) Log.v(TAG, "Current share has status " + mCurrentShare.mStatus);
            if (BluetoothShare.isStatusError(mCurrentShare.mStatus)) {
                failReason = mCurrentShare.mStatus;
            }
            if (mCurrentShare.mDirection == BluetoothShare.DIRECTION_INBOUND
                    && mCurrentShare.mFilename != null) {
                new File(mCurrentShare.mFilename).delete();
            }
        }
        BluetoothOppShareInfo info = mBatch.getPendingShare();
        while (info != null) {
            if (info.mStatus < 200) {
                info.mStatus = failReason;
                Uri contentUri = Uri.parse(BluetoothShare.CONTENT_URI + "/" + info.mId);
                ContentValues updateValues = new ContentValues();
                updateValues.put(BluetoothShare.STATUS, info.mStatus);
                if (info.mDirection == BluetoothShare.DIRECTION_OUTBOUND) {
                    BluetoothOppSendFileInfo fileInfo = null;
                    fileInfo = BluetoothOppSendFileInfo.generateFileInfo(mContext, info.mUri,
                            info.mMimetype, info.mDestination);
                    if (fileInfo.mFileName != null) {
                        updateValues.put(BluetoothShare.FILENAME_HINT, fileInfo.mFileName);
                        updateValues.put(BluetoothShare.TOTAL_BYTES, fileInfo.mLength);
                        updateValues.put(BluetoothShare.MIMETYPE, fileInfo.mMimetype);
                    }
                } else {
                    if (info.mStatus < 200 && info.mFilename != null) {
                        new File(info.mFilename).delete();
                    }
                }
                mContext.getContentResolver().update(contentUri, updateValues, null, null);
                Constants.sendIntentIfCompleted(mContext, contentUri, info.mStatus);
            }
            info = mBatch.getPendingShare();
        }
    }
    private void markBatchFailed() {
        markBatchFailed(BluetoothShare.STATUS_UNKNOWN_ERROR);
    }
    public void start() {
        if (!mAdapter.isEnabled()) {
            Log.e(TAG, "Can't start transfer when Bluetooth is disabled for " + mBatch.mId);
            markBatchFailed();
            mBatch.mStatus = Constants.BATCH_STATUS_FAILED;
            return;
        }
        if (mHandlerThread == null) {
            if (V) Log.v(TAG, "Create handler thread for batch " + mBatch.mId);
            mHandlerThread = new HandlerThread("BtOpp Transfer Handler",
                    Process.THREAD_PRIORITY_BACKGROUND);
            mHandlerThread.start();
            mSessionHandler = new EventHandler(mHandlerThread.getLooper());
            if (mBatch.mDirection == BluetoothShare.DIRECTION_OUTBOUND) {
                startConnectSession();
            } else if (mBatch.mDirection == BluetoothShare.DIRECTION_INBOUND) {
                startObexSession();
            }
        }
    }
    public void stop() {
        if (V) Log.v(TAG, "stop");
        if (mConnectThread != null) {
            try {
                mConnectThread.interrupt();
                if (V) Log.v(TAG, "waiting for connect thread to terminate");
                mConnectThread.join();
            } catch (InterruptedException e) {
                if (V) Log.v(TAG, "Interrupted waiting for connect thread to join");
            }
            mConnectThread = null;
        }
        if (mSession != null) {
            if (V) Log.v(TAG, "Stop mSession");
            mSession.stop();
        }
        if (mHandlerThread != null) {
            mHandlerThread.getLooper().quit();
            mHandlerThread.interrupt();
            mHandlerThread = null;
        }
    }
    private void startObexSession() {
        mBatch.mStatus = Constants.BATCH_STATUS_RUNNING;
        mCurrentShare = mBatch.getPendingShare();
        if (mCurrentShare == null) {
            Log.e(TAG, "Unexpected error happened !");
            return;
        }
        if (V) Log.v(TAG, "Start session for info " + mCurrentShare.mId + " for batch " +
                mBatch.mId);
        if (mBatch.mDirection == BluetoothShare.DIRECTION_OUTBOUND) {
            if (V) Log.v(TAG, "Create Client session with transport " + mTransport.toString());
            mSession = new BluetoothOppObexClientSession(mContext, mTransport);
        } else if (mBatch.mDirection == BluetoothShare.DIRECTION_INBOUND) {
            if (mSession == null) {
                Log.e(TAG, "Unexpected error happened !");
                markBatchFailed();
                mBatch.mStatus = Constants.BATCH_STATUS_FAILED;
                return;
            }
            if (V) Log.v(TAG, "Transfer has Server session" + mSession.toString());
        }
        mSession.start(mSessionHandler);
        processCurrentShare();
    }
    private void processCurrentShare() {
        if (V) Log.v(TAG, "processCurrentShare" + mCurrentShare.mId);
        mSession.addShare(mCurrentShare);
    }
    public void setConfirmed() {
        final Thread notifyThread = new Thread("Server Unblock thread") {
            public void run() {
                synchronized (mSession) {
                    mSession.unblock();
                    mSession.notify();
                }
            }
        };
        if (V) Log.v(TAG, "setConfirmed to unblock mSession" + mSession.toString());
        notifyThread.start();
    }
    private void startConnectSession() {
        if (Constants.USE_TCP_DEBUG) {
            mConnectThread = new SocketConnectThread("localhost", Constants.TCP_DEBUG_PORT, 0);
            mConnectThread.start();
        } else {
            int channel = BluetoothOppPreference.getInstance(mContext).getChannel(
                    mBatch.mDestination, OPUSH_UUID16);
            if (channel != -1) {
                if (D) Log.d(TAG, "Get OPUSH channel " + channel + " from cache for " +
                        mBatch.mDestination);
                mTimestamp = System.currentTimeMillis();
                mSessionHandler.obtainMessage(SDP_RESULT, channel, -1, mBatch.mDestination)
                        .sendToTarget();
            } else {
                doOpushSdp();
            }
        }
    }
    private void doOpushSdp() {
        if (V) Log.v(TAG, "Do Opush SDP request for address " + mBatch.mDestination);
        mTimestamp = System.currentTimeMillis();
        int channel;
        channel = mBatch.mDestination.getServiceChannel(BluetoothUuid.ObexObjectPush);
        if (channel != -1) {
            if (D) Log.d(TAG, "Get OPUSH channel " + channel + " from SDP for "
                    + mBatch.mDestination);
            mSessionHandler.obtainMessage(SDP_RESULT, channel, -1, mBatch.mDestination)
                    .sendToTarget();
            return;
        } else {
            if (V) Log.v(TAG, "Remote Service channel not in cache");
            if (!mBatch.mDestination.fetchUuidsWithSdp()) {
                Log.e(TAG, "Start SDP query failed");
            } else {
                if (V) Log.v(TAG, "Start new SDP, wait for result");
                IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_UUID);
                mContext.registerReceiver(mReceiver, intentFilter);
                return;
            }
        }
        Message msg = mSessionHandler.obtainMessage(SDP_RESULT, channel, -1, mBatch.mDestination);
        mSessionHandler.sendMessageDelayed(msg, 2000);
    }
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothDevice.ACTION_UUID)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (V) Log.v(TAG, "ACTION_UUID for device " + device);
                if (device.equals(mBatch.mDestination)) {
                    int channel = -1;
                    Parcelable[] uuid = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
                    if (uuid != null) {
                        ParcelUuid[] uuids = new ParcelUuid[uuid.length];
                        for (int i = 0; i < uuid.length; i++) {
                            uuids[i] = (ParcelUuid)uuid[i];
                        }
                        if (BluetoothUuid.isUuidPresent(uuids, BluetoothUuid.ObexObjectPush)) {
                            if (V) Log.v(TAG, "SDP get OPP result for device " + device);
                            channel = mBatch.mDestination
                                    .getServiceChannel(BluetoothUuid.ObexObjectPush);
                        }
                    }
                    mSessionHandler.obtainMessage(SDP_RESULT, channel, -1, mBatch.mDestination)
                            .sendToTarget();
                }
            }
        }
    };
    private SocketConnectThread mConnectThread;
    private class SocketConnectThread extends Thread {
        private final String host;
        private final BluetoothDevice device;
        private final int channel;
        private boolean isConnected;
        private long timestamp;
        private BluetoothSocket btSocket = null;
        public SocketConnectThread(String host, int port, int dummy) {
            super("Socket Connect Thread");
            this.host = host;
            this.channel = port;
            this.device = null;
            isConnected = false;
        }
        public SocketConnectThread(BluetoothDevice device, int channel) {
            super("Socket Connect Thread");
            this.device = device;
            this.host = null;
            this.channel = channel;
            isConnected = false;
        }
        public void interrupt() {
            if (!Constants.USE_TCP_DEBUG) {
                if (btSocket != null) {
                    try {
                        btSocket.close();
                    } catch (IOException e) {
                        Log.v(TAG, "Error when close socket");
                    }
                }
            }
        }
        @Override
        public void run() {
            timestamp = System.currentTimeMillis();
            if (Constants.USE_TCP_DEBUG) {
                Socket s = new Socket();
                int result = 0;
                for (int i = 0; i < CONNECT_RETRY_TIME && result == 0; i++) {
                    try {
                        s.connect(new InetSocketAddress(host, channel), CONNECT_WAIT_TIMEOUT);
                    } catch (UnknownHostException e) {
                        Log.e(TAG, "TCP socket connect unknown host ");
                    } catch (IOException e) {
                        Log.e(TAG, "TCP socket connect failed ");
                    }
                    if (s.isConnected()) {
                        if (D) Log.d(TAG, "TCP socket connected ");
                        isConnected = true;
                        break;
                    }
                    if (isInterrupted()) {
                        Log.e(TAG, "TCP socket connect interrupted ");
                        markConnectionFailed(s);
                        return;
                    }
                }
                if (!isConnected) {
                    Log.e(TAG, "TCP socket connect failed after 20 seconds");
                    markConnectionFailed(s);
                    return;
                }
                if (V) Log.v(TAG, "TCP Socket connection attempt took " +
                        (System.currentTimeMillis() - timestamp) + " ms");
                TestTcpTransport transport;
                transport = new TestTcpTransport(s);
                if (isInterrupted()) {
                    isConnected = false;
                    markConnectionFailed(s);
                    transport = null;
                    return;
                }
                if (!isConnected) {
                    transport = null;
                    Log.e(TAG, "TCP connect session error: ");
                    markConnectionFailed(s);
                    return;
                } else {
                    if (D) Log.d(TAG, "Send transport message " + transport.toString());
                    mSessionHandler.obtainMessage(RFCOMM_CONNECTED, transport).sendToTarget();
                }
            } else {
                try {
                    btSocket = device.createInsecureRfcommSocket(channel);
                } catch (IOException e1) {
                    Log.e(TAG, "Rfcomm socket create error");
                    markConnectionFailed(btSocket);
                    return;
                }
                try {
                    btSocket.connect();
                    if (V) Log.v(TAG, "Rfcomm socket connection attempt took " +
                            (System.currentTimeMillis() - timestamp) + " ms");
                    BluetoothOppRfcommTransport transport;
                    transport = new BluetoothOppRfcommTransport(btSocket);
                    BluetoothOppPreference.getInstance(mContext).setChannel(device, OPUSH_UUID16,
                            channel);
                    BluetoothOppPreference.getInstance(mContext).setName(device, device.getName());
                    if (V) Log.v(TAG, "Send transport message " + transport.toString());
                    mSessionHandler.obtainMessage(RFCOMM_CONNECTED, transport).sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "Rfcomm socket connect exception ");
                    BluetoothOppPreference.getInstance(mContext)
                            .removeChannel(device, OPUSH_UUID16);
                    markConnectionFailed(btSocket);
                    return;
                }
            }
        }
        private void markConnectionFailed(Socket s) {
            try {
                s.close();
            } catch (IOException e) {
                Log.e(TAG, "TCP socket close error");
            }
            mSessionHandler.obtainMessage(RFCOMM_ERROR).sendToTarget();
        }
        private void markConnectionFailed(BluetoothSocket s) {
            try {
                s.close();
            } catch (IOException e) {
                if (V) Log.e(TAG, "Error when close socket");
            }
            mSessionHandler.obtainMessage(RFCOMM_ERROR).sendToTarget();
            return;
        }
    };
    private void tickShareStatus(BluetoothOppShareInfo share) {
        Uri contentUri = Uri.parse(BluetoothShare.CONTENT_URI + "/" + share.mId);
        ContentValues updateValues = new ContentValues();
        updateValues.put(BluetoothShare.DIRECTION, share.mDirection);
        mContext.getContentResolver().update(contentUri, updateValues, null, null);
    }
    public void onShareAdded(int id) {
        BluetoothOppShareInfo info = mBatch.getPendingShare();
        if (info.mDirection == BluetoothShare.DIRECTION_INBOUND) {
            mCurrentShare = mBatch.getPendingShare();
            if (mCurrentShare != null
                    && mCurrentShare.mConfirm == BluetoothShare.USER_CONFIRMATION_AUTO_CONFIRMED) {
                if (V) Log.v(TAG, "Transfer continue session for info " + mCurrentShare.mId +
                        " from batch " + mBatch.mId);
                processCurrentShare();
                setConfirmed();
            }
        }
    }
    public void onShareDeleted(int id) {
    }
    public void onBatchCanceled() {
        if (V) Log.v(TAG, "Transfer on Batch canceled");
        this.stop();
        mBatch.mStatus = Constants.BATCH_STATUS_FINISHED;
    }
}
