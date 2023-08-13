public class GSMTestHandler extends HandlerThread implements Handler.Callback {
    private Handler mHandler;
    private Message mCurrentMessage;
    private Boolean mMsgConsumed;
    private SimulatedCommands sc;
    private GSMPhone mGSMPhone;
    private Context mContext;
    private static final int FAIL_TIMEOUT_MILLIS = 5 * 1000;
    public GSMTestHandler(Context context) {
        super("GSMPhoneTest");
        mMsgConsumed = false;
        mContext = context;
   }
    @Override
    protected void onLooperPrepared() {
        sc = new SimulatedCommands();
        mGSMPhone = new GSMPhone(mContext, sc, new TestPhoneNotifier(), true);
        mHandler = new Handler(getLooper(), this);
        synchronized (this) {
            notifyAll();
        }
    }
    public boolean handleMessage(Message msg) {
        synchronized (this) {
            mCurrentMessage = msg;
            this.notifyAll();
            while(!mMsgConsumed) {
                try {
                    this.wait();
                } catch (InterruptedException e) {}
            }
            mMsgConsumed = false;
        }
        return true;
    }
    public void cleanup() {
        Looper looper = getLooper();
        if (looper != null) looper.quit();
        mHandler = null;
    }
    public Handler getHandler() {
        return mHandler;
    }
    public SimulatedCommands getSimulatedCommands() {
        return sc;
    }
    public GSMPhone getGSMPhone() {
        return mGSMPhone;
    }
    public Message waitForMessage(int code) {
        Message msg;
        while(true) {
            msg = null;
            synchronized (this) {
                try {
                    this.wait(FAIL_TIMEOUT_MILLIS);
                } catch (InterruptedException e) {
                }
                if (mCurrentMessage != null) {
                    msg = Message.obtain();
                    msg.copyFrom(mCurrentMessage);
                    mCurrentMessage = null;
                    mMsgConsumed = true;
                    this.notifyAll();
                }
            }
            if (msg == null || code == GSMPhoneTest.ANY_MESSAGE || msg.what == code) return msg;
       }
    }
}
