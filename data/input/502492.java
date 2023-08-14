public class CLIRListPreference extends ListPreference {
    private static final String LOG_TAG = "CLIRListPreference";
    private final boolean DBG = (PhoneApp.DBG_LEVEL >= 2);
    private MyHandler mHandler = new MyHandler();
    Phone phone;
    TimeConsumingPreferenceListener tcpListener;
    int clirArray[];
    public CLIRListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        phone = PhoneFactory.getDefaultPhone();
    }
    public CLIRListPreference(Context context) {
        this(context, null);
    }
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        phone.setOutgoingCallerIdDisplay(findIndexOfValue(getValue()),
                mHandler.obtainMessage(MyHandler.MESSAGE_SET_CLIR));
        if (tcpListener != null) {
            tcpListener.onStarted(this, false);
        }
    }
    void init(TimeConsumingPreferenceListener listener, boolean skipReading) {
        tcpListener = listener;
        if (!skipReading) {
            phone.getOutgoingCallerIdDisplay(mHandler.obtainMessage(MyHandler.MESSAGE_GET_CLIR,
                    MyHandler.MESSAGE_GET_CLIR, MyHandler.MESSAGE_GET_CLIR));
            if (tcpListener != null) {
                tcpListener.onStarted(this, true);
            }
        }
    }
    void handleGetCLIRResult(int tmpClirArray[]) {
        clirArray = tmpClirArray;
        final boolean enabled = tmpClirArray[1] == 1 || tmpClirArray[1] == 3 || tmpClirArray[1] == 4;
        setEnabled(enabled);
        int value = CommandsInterface.CLIR_DEFAULT;
        switch (tmpClirArray[1]) {
            case 1: 
            case 3: 
            case 4: 
                switch (tmpClirArray[0]) {
                    case 1: 
                        value = CommandsInterface.CLIR_INVOCATION;
                        break;
                    case 2: 
                        value = CommandsInterface.CLIR_SUPPRESSION;
                        break;
                    case 0: 
                    default:
                        value = CommandsInterface.CLIR_DEFAULT;
                        break;
                }
                break;
            case 0: 
            case 2: 
            default:
                value = CommandsInterface.CLIR_DEFAULT;
                break;
        }
        setValueIndex(value);
        int summary = R.string.sum_default_caller_id;
        switch (value) {
            case CommandsInterface.CLIR_SUPPRESSION:
                summary = R.string.sum_show_caller_id;
                break;
            case CommandsInterface.CLIR_INVOCATION:
                summary = R.string.sum_hide_caller_id;
                break;
            case CommandsInterface.CLIR_DEFAULT:
                summary = R.string.sum_default_caller_id;
                break;
        }
        setSummary(summary);
    }
    private class MyHandler extends Handler {
        private static final int MESSAGE_GET_CLIR = 0;
        private static final int MESSAGE_SET_CLIR = 1;
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_GET_CLIR:
                    handleGetCLIRResponse(msg);
                    break;
                case MESSAGE_SET_CLIR:
                    handleSetCLIRResponse(msg);
                    break;
            }
        }
        private void handleGetCLIRResponse(Message msg) {
            AsyncResult ar = (AsyncResult) msg.obj;
            if (msg.arg2 == MESSAGE_SET_CLIR) {
                tcpListener.onFinished(CLIRListPreference.this, false);
            } else {
                tcpListener.onFinished(CLIRListPreference.this, true);
            }
            clirArray = null;
            if (ar.exception != null) {
                if (DBG) Log.d(LOG_TAG, "handleGetCLIRResponse: ar.exception="+ar.exception);
                setEnabled(false);
                tcpListener.onError(CLIRListPreference.this, EXCEPTION_ERROR);
            } else if (ar.userObj instanceof Throwable) {
                tcpListener.onError(CLIRListPreference.this, RESPONSE_ERROR);
            } else {
                int clirArray[] = (int[]) ar.result;
                if (clirArray.length != 2) {
                    tcpListener.onError(CLIRListPreference.this, RESPONSE_ERROR);
                } else {
                    if (DBG) Log.d(LOG_TAG, "handleGetCLIRResponse: CLIR successfully queried, clirArray[0]="
                            + clirArray[0] + ", clirArray[1]=" + clirArray[1]);
                    handleGetCLIRResult(clirArray);
                }
            }
        }
        private void handleSetCLIRResponse(Message msg) {
            AsyncResult ar = (AsyncResult) msg.obj;
            if (ar.exception != null) {
                if (DBG) Log.d(LOG_TAG, "handleSetCallWaitingResponse: ar.exception="+ar.exception);
            }
            if (DBG) Log.d(LOG_TAG, "handleSetCallWaitingResponse: re get");
            phone.getOutgoingCallerIdDisplay(obtainMessage(MESSAGE_GET_CLIR,
                    MESSAGE_SET_CLIR, MESSAGE_SET_CLIR, ar.exception));
        }
    }
}