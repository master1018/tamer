public class MockContextWrapperService extends Service {
    private static boolean mHadCalledOnBind = false;
    private static boolean mHadCalledOnUnbind = false;
    private static boolean mHadCalledOnStart = false;
    private static boolean mHadCalledOnDestory = false;
    private static final int TEST_MESSAGE_WHAT = 1;
    private final IBinder mBinder = new Binder();
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            mHandler.sendMessageDelayed(mHandler.obtainMessage(TEST_MESSAGE_WHAT), 1000);
        }
    };
    @Override
    public void onCreate() {
        mHandler.sendMessageDelayed(mHandler.obtainMessage(TEST_MESSAGE_WHAT), 1000);
    }
    @Override
    public void onDestroy() {
        mHadCalledOnDestory = true;
        mHandler.removeMessages(1);
    }
    @Override
    public boolean onUnbind(Intent intent) {
        mHadCalledOnUnbind = true;
        return true;
    }
    @Override
    public IBinder onBind(Intent intent) {
        mHadCalledOnBind = true;
        return mBinder;
    }
    @Override
    public void onStart(Intent intent, int startId) {
        mHadCalledOnStart = true;
    }
    public static void reset() {
        mHadCalledOnBind = false;
        mHadCalledOnUnbind = false;
        mHadCalledOnStart = false;
        mHadCalledOnDestory = false;
    }
    public static boolean hadCalledOnBind() {
        return mHadCalledOnBind;
    }
    public static boolean hadCalledOnUnbind() {
        return mHadCalledOnUnbind;
    }
    public static boolean hadCalledOnStart() {
        return mHadCalledOnStart;
    }
    public static boolean hadCalledOnDestory() {
        return mHadCalledOnDestory;
    }
}
