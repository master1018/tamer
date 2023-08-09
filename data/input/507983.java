public class AndroidSystemService extends SystemService {
    private static AndroidSystemService sInstance;
    private AndroidSystemService() {
    }
    public static AndroidSystemService getInstance() {
        if (sInstance == null) {
            sInstance = new AndroidSystemService();
        }
        return sInstance;
    }
    private Context mContext;
    private AndroidHeartBeatService mHeartbeatServcie;
    private AndroidSmsService mSmsService;
    public void initialize(Context context) {
        mContext = context;
    }
    public void shutdown() {
        if (mHeartbeatServcie != null) {
            mHeartbeatServcie.stopAll();
        }
        if (mSmsService != null) {
            mSmsService.stop();
        }
    }
    @Override
    public HeartbeatService getHeartbeatService() {
        if(mContext == null) {
            throw new IllegalStateException("Hasn't been initialized yet");
        }
        if (mHeartbeatServcie == null) {
            mHeartbeatServcie = new AndroidHeartBeatService(mContext);
        }
        return mHeartbeatServcie;
    }
    @Override
    public SmsService getSmsService() {
        if(mContext == null) {
            throw new IllegalStateException("Hasn't been initialized yet");
        }
        if (mSmsService == null) {
            mSmsService = new AndroidSmsService(mContext);
        }
        return mSmsService;
    }
}
