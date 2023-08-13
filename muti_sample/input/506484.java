public abstract class RecognitionService extends Service {
    @SdkConstant(SdkConstantType.SERVICE_ACTION)
    public static final String SERVICE_INTERFACE = "android.speech.RecognitionService";
    public static final String SERVICE_META_DATA = "android.speech";
    private static final String TAG = "RecognitionService";
    private static final boolean DBG = false;
    private RecognitionServiceBinder mBinder = new RecognitionServiceBinder(this);
    private Callback mCurrentCallback = null;
    private static final int MSG_START_LISTENING = 1;
    private static final int MSG_STOP_LISTENING = 2;
    private static final int MSG_CANCEL = 3;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_START_LISTENING:
                    StartListeningArgs args = (StartListeningArgs) msg.obj;
                    dispatchStartListening(args.mIntent, args.mListener);
                    break;
                case MSG_STOP_LISTENING:
                    dispatchStopListening((IRecognitionListener) msg.obj);
                    break;
                case MSG_CANCEL:
                    dispatchCancel((IRecognitionListener) msg.obj);
            }
        }
    };
    private void dispatchStartListening(Intent intent, IRecognitionListener listener) {
        if (mCurrentCallback == null) {
            if (DBG) Log.d(TAG, "created new mCurrentCallback, listener = " + listener.asBinder());
            mCurrentCallback = new Callback(listener);
            RecognitionService.this.onStartListening(intent, mCurrentCallback);
        } else {
            try {
                listener.onError(SpeechRecognizer.ERROR_RECOGNIZER_BUSY);
            } catch (RemoteException e) {
                Log.d(TAG, "onError call from startListening failed");
            }
            Log.i(TAG, "concurrent startListening received - ignoring this call");
        }
    }
    private void dispatchStopListening(IRecognitionListener listener) {
        try {
            if (mCurrentCallback == null) {
                listener.onError(SpeechRecognizer.ERROR_CLIENT);
                Log.w(TAG, "stopListening called with no preceding startListening - ignoring");
            } else if (mCurrentCallback.mListener.asBinder() != listener.asBinder()) {
                listener.onError(SpeechRecognizer.ERROR_RECOGNIZER_BUSY);
                Log.w(TAG, "stopListening called by other caller than startListening - ignoring");
            } else { 
                RecognitionService.this.onStopListening(mCurrentCallback);
            }
        } catch (RemoteException e) { 
            Log.d(TAG, "onError call from stopListening failed");
        }
    }
    private void dispatchCancel(IRecognitionListener listener) {
        if (mCurrentCallback == null) {
            if (DBG) Log.d(TAG, "cancel called with no preceding startListening - ignoring");
        } else if (mCurrentCallback.mListener.asBinder() != listener.asBinder()) {
            Log.w(TAG, "cancel called by client who did not call startListening - ignoring");
        } else { 
            RecognitionService.this.onCancel(mCurrentCallback);
            mCurrentCallback = null;
            if (DBG) Log.d(TAG, "canceling - setting mCurrentCallback to null");
        }
    }
    private class StartListeningArgs {
        public final Intent mIntent;
        public final IRecognitionListener mListener;
        public StartListeningArgs(Intent intent, IRecognitionListener listener) {
            this.mIntent = intent;
            this.mListener = listener;
        }
    }
    private boolean checkPermissions(IRecognitionListener listener) {
        if (DBG) Log.d(TAG, "checkPermissions");
        if (RecognitionService.this.checkCallingOrSelfPermission(android.Manifest.permission.
                RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        try {
            Log.e(TAG, "call for recognition service without RECORD_AUDIO permissions");
            listener.onError(SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS);
        } catch (RemoteException re) {
            Log.e(TAG, "sending ERROR_INSUFFICIENT_PERMISSIONS message failed", re);
        }
        return false;
    }
    protected abstract void onStartListening(Intent recognizerIntent, Callback listener);
    protected abstract void onCancel(Callback listener);
    protected abstract void onStopListening(Callback listener);
    @Override
    public final IBinder onBind(final Intent intent) {
        if (DBG) Log.d(TAG, "onBind, intent=" + intent);
        return mBinder;
    }
    @Override
    public void onDestroy() {
        if (DBG) Log.d(TAG, "onDestroy");
        mCurrentCallback = null;
        mBinder.clearReference();
        super.onDestroy();
    }
    public class Callback {
        private final IRecognitionListener mListener;
        private Callback(IRecognitionListener listener) {
            mListener = listener;
        }
        public void beginningOfSpeech() throws RemoteException {
            if (DBG) Log.d(TAG, "beginningOfSpeech");
            mListener.onBeginningOfSpeech();
        }
        public void bufferReceived(byte[] buffer) throws RemoteException {
            mListener.onBufferReceived(buffer);
        }
        public void endOfSpeech() throws RemoteException {
            mListener.onEndOfSpeech();
        }
        public void error(int error) throws RemoteException {
            mCurrentCallback = null;
            mListener.onError(error);
        }
        public void partialResults(Bundle partialResults) throws RemoteException {
            mListener.onPartialResults(partialResults);
        }
        public void readyForSpeech(Bundle params) throws RemoteException {
            mListener.onReadyForSpeech(params);
        }
        public void results(Bundle results) throws RemoteException {
            mCurrentCallback = null;
            mListener.onResults(results);
        }
        public void rmsChanged(float rmsdB) throws RemoteException {
            mListener.onRmsChanged(rmsdB);
        }
    }
    private static class RecognitionServiceBinder extends IRecognitionService.Stub {
        private RecognitionService mInternalService;
        public RecognitionServiceBinder(RecognitionService service) {
            mInternalService = service;
        }
        public void startListening(Intent recognizerIntent, IRecognitionListener listener) {
            if (DBG) Log.d(TAG, "startListening called by:" + listener.asBinder());
            if (mInternalService != null && mInternalService.checkPermissions(listener)) {
                mInternalService.mHandler.sendMessage(Message.obtain(mInternalService.mHandler,
                        MSG_START_LISTENING, mInternalService.new StartListeningArgs(
                                recognizerIntent, listener)));
            }
        }
        public void stopListening(IRecognitionListener listener) {
            if (DBG) Log.d(TAG, "stopListening called by:" + listener.asBinder());
            if (mInternalService != null && mInternalService.checkPermissions(listener)) {
                mInternalService.mHandler.sendMessage(Message.obtain(mInternalService.mHandler,
                        MSG_STOP_LISTENING, listener));
            }
        }
        public void cancel(IRecognitionListener listener) {
            if (DBG) Log.d(TAG, "cancel called by:" + listener.asBinder());
            if (mInternalService != null && mInternalService.checkPermissions(listener)) {
                mInternalService.mHandler.sendMessage(Message.obtain(mInternalService.mHandler,
                        MSG_CANCEL, listener));
            }
        }
        public void clearReference() {
            mInternalService = null;
        }
    }
}
