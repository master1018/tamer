public class SpeechRecognizer {
    private final static boolean DBG = false;
    private static final String TAG = "SpeechRecognizer";
    public static final String RESULTS_RECOGNITION = "results_recognition";
    public static final int ERROR_NETWORK_TIMEOUT = 1;
    public static final int ERROR_NETWORK = 2;
    public static final int ERROR_AUDIO = 3;
    public static final int ERROR_SERVER = 4;
    public static final int ERROR_CLIENT = 5;
    public static final int ERROR_SPEECH_TIMEOUT = 6;
    public static final int ERROR_NO_MATCH = 7;
    public static final int ERROR_RECOGNIZER_BUSY = 8;
    public static final int ERROR_INSUFFICIENT_PERMISSIONS = 9;
    private final static int MSG_START = 1;
    private final static int MSG_STOP = 2;
    private final static int MSG_CANCEL = 3;
    private final static int MSG_CHANGE_LISTENER = 4;
    private IRecognitionService mService;
    private Connection mConnection;
    private final Context mContext;
    private final ComponentName mServiceComponent;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_START:
                    handleStartListening((Intent) msg.obj);
                    break;
                case MSG_STOP:
                    handleStopMessage();
                    break;
                case MSG_CANCEL:
                    handleCancelMessage();
                    break;
                case MSG_CHANGE_LISTENER:
                    handleChangeListener((RecognitionListener) msg.obj);
                    break;
            }
        }
    };
    private final Queue<Message> mPendingTasks = new LinkedList<Message>();
    private final InternalListener mListener = new InternalListener();
    private SpeechRecognizer(final Context context, final ComponentName serviceComponent) {
        mContext = context;
        mServiceComponent = serviceComponent;
    }
    private class Connection implements ServiceConnection {
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            mService = IRecognitionService.Stub.asInterface(service);
            if (DBG) Log.d(TAG, "onServiceConnected - Success");
            while (!mPendingTasks.isEmpty()) {
                mHandler.sendMessage(mPendingTasks.poll());
            }
        }
        public void onServiceDisconnected(final ComponentName name) {
            mService = null;
            mConnection = null;
            mPendingTasks.clear();
            if (DBG) Log.d(TAG, "onServiceDisconnected - Success");
        }
    }
    public static boolean isRecognitionAvailable(final Context context) {
        final List<ResolveInfo> list = context.getPackageManager().queryIntentServices(
                new Intent(RecognitionService.SERVICE_INTERFACE), 0);
        return list != null && list.size() != 0;
    }
    public static SpeechRecognizer createSpeechRecognizer(final Context context) {
        return createSpeechRecognizer(context, null);
    }
    public static SpeechRecognizer createSpeechRecognizer(final Context context,
            final ComponentName serviceComponent) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null)");
        }
        checkIsCalledFromMainThread();
        return new SpeechRecognizer(context, serviceComponent);
    }
    public void setRecognitionListener(RecognitionListener listener) {
        checkIsCalledFromMainThread();
        putMessage(Message.obtain(mHandler, MSG_CHANGE_LISTENER, listener));
    }
    public void startListening(final Intent recognizerIntent) {
        if (recognizerIntent == null) {
            throw new IllegalArgumentException("intent must not be null");
        }
        checkIsCalledFromMainThread();
        if (mConnection == null) { 
            mConnection = new Connection();
            Intent serviceIntent = new Intent(RecognitionService.SERVICE_INTERFACE);
            if (mServiceComponent == null) {
                String serviceComponent = Settings.Secure.getString(mContext.getContentResolver(),
                        Settings.Secure.VOICE_RECOGNITION_SERVICE);
                if (TextUtils.isEmpty(serviceComponent)) {
                    Log.e(TAG, "no selected voice recognition service");
                    mListener.onError(ERROR_CLIENT);
                    return;
                }
                serviceIntent.setComponent(ComponentName.unflattenFromString(serviceComponent));                
            } else {
                serviceIntent.setComponent(mServiceComponent);
            }
            if (!mContext.bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE)) {
                Log.e(TAG, "bind to recognition service failed");
                mConnection = null;
                mService = null;
                mListener.onError(ERROR_CLIENT);
                return;
            }
        }
        putMessage(Message.obtain(mHandler, MSG_START, recognizerIntent));
    }
    public void stopListening() {
        checkIsCalledFromMainThread();
        putMessage(Message.obtain(mHandler, MSG_STOP));
    }
    public void cancel() {
        checkIsCalledFromMainThread();
        putMessage(Message.obtain(mHandler, MSG_CANCEL));
    }
    private static void checkIsCalledFromMainThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new RuntimeException(
                    "SpeechRecognizer should be used only from the application's main thread");
        }
    }
    private void putMessage(Message msg) {
        if (mService == null) {
            mPendingTasks.offer(msg);
        } else {
            mHandler.sendMessage(msg);
        }
    }
    private void handleStartListening(Intent recognizerIntent) {
        if (!checkOpenConnection()) {
            return;
        }
        try {
            mService.startListening(recognizerIntent, mListener);
            if (DBG) Log.d(TAG, "service start listening command succeded");
        } catch (final RemoteException e) {
            Log.e(TAG, "startListening() failed", e);
            mListener.onError(ERROR_CLIENT);
        }
    }
    private void handleStopMessage() {
        if (!checkOpenConnection()) {
            return;
        }
        try {
            mService.stopListening(mListener);
            if (DBG) Log.d(TAG, "service stop listening command succeded");
        } catch (final RemoteException e) {
            Log.e(TAG, "stopListening() failed", e);
            mListener.onError(ERROR_CLIENT);
        }
    }
    private void handleCancelMessage() {
        if (!checkOpenConnection()) {
            return;
        }
        try {
            mService.cancel(mListener);
            if (DBG) Log.d(TAG, "service cancel command succeded");
        } catch (final RemoteException e) {
            Log.e(TAG, "cancel() failed", e);
            mListener.onError(ERROR_CLIENT);
        }
    }
    private boolean checkOpenConnection() {
        if (mService != null) {
            return true;
        }
        mListener.onError(ERROR_CLIENT);
        Log.e(TAG, "not connected to the recognition service");
        return false;
    }
    private void handleChangeListener(RecognitionListener listener) {
        if (DBG) Log.d(TAG, "handleChangeListener, listener=" + listener);
        mListener.mInternalListener = listener;
    }
    public void destroy() {
        if (mConnection != null) {
            mContext.unbindService(mConnection);
        }
        mPendingTasks.clear();
        mService = null;
        mConnection = null;
        mListener.mInternalListener = null;
    }
    private class InternalListener extends IRecognitionListener.Stub {
        private RecognitionListener mInternalListener;
        private final static int MSG_BEGINNING_OF_SPEECH = 1;
        private final static int MSG_BUFFER_RECEIVED = 2;
        private final static int MSG_END_OF_SPEECH = 3;
        private final static int MSG_ERROR = 4;
        private final static int MSG_READY_FOR_SPEECH = 5;
        private final static int MSG_RESULTS = 6;
        private final static int MSG_PARTIAL_RESULTS = 7;
        private final static int MSG_RMS_CHANGED = 8;
        private final static int MSG_ON_EVENT = 9;
        private final Handler mInternalHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (mInternalListener == null) {
                    return;
                }
                switch (msg.what) {
                    case MSG_BEGINNING_OF_SPEECH:
                        mInternalListener.onBeginningOfSpeech();
                        break;
                    case MSG_BUFFER_RECEIVED:
                        mInternalListener.onBufferReceived((byte[]) msg.obj);
                        break;
                    case MSG_END_OF_SPEECH:
                        mInternalListener.onEndOfSpeech();
                        break;
                    case MSG_ERROR:
                        mInternalListener.onError((Integer) msg.obj);
                        break;
                    case MSG_READY_FOR_SPEECH:
                        mInternalListener.onReadyForSpeech((Bundle) msg.obj);
                        break;
                    case MSG_RESULTS:
                        mInternalListener.onResults((Bundle) msg.obj);
                        break;
                    case MSG_PARTIAL_RESULTS:
                        mInternalListener.onPartialResults((Bundle) msg.obj);
                        break;
                    case MSG_RMS_CHANGED:
                        mInternalListener.onRmsChanged((Float) msg.obj);
                        break;
                    case MSG_ON_EVENT:
                        mInternalListener.onEvent(msg.arg1, (Bundle) msg.obj);
                        break;
                }
            }
        };
        public void onBeginningOfSpeech() {
            Message.obtain(mInternalHandler, MSG_BEGINNING_OF_SPEECH).sendToTarget();
        }
        public void onBufferReceived(final byte[] buffer) {
            Message.obtain(mInternalHandler, MSG_BUFFER_RECEIVED, buffer).sendToTarget();
        }
        public void onEndOfSpeech() {
            Message.obtain(mInternalHandler, MSG_END_OF_SPEECH).sendToTarget();
        }
        public void onError(final int error) {
            Message.obtain(mInternalHandler, MSG_ERROR, error).sendToTarget();
        }
        public void onReadyForSpeech(final Bundle noiseParams) {
            Message.obtain(mInternalHandler, MSG_READY_FOR_SPEECH, noiseParams).sendToTarget();
        }
        public void onResults(final Bundle results) {
            Message.obtain(mInternalHandler, MSG_RESULTS, results).sendToTarget();
        }
        public void onPartialResults(final Bundle results) {
            Message.obtain(mInternalHandler, MSG_PARTIAL_RESULTS, results).sendToTarget();
        }
        public void onRmsChanged(final float rmsdB) {
            Message.obtain(mInternalHandler, MSG_RMS_CHANGED, rmsdB).sendToTarget();
        }
        public void onEvent(final int eventType, final Bundle params) {
            Message.obtain(mInternalHandler, MSG_ON_EVENT, eventType, eventType, params)
                    .sendToTarget();
        }
    }
}
