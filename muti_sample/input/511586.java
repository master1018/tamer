public class StubRemoteService extends Service{
    @Override
    public void onCreate() {
        super.onCreate();
        android.util.Log.d("Process test stub", "StubRemoteServiceProcessPid:" + Process.myPid());
    }
    private final ISecondary.Stub mSecondaryBinder = new ISecondary.Stub() {
        public int getPid() {
            return Process.myPid();
        }
        public long getElapsedCpuTime() {
            return Process.getElapsedCpuTime();
        }
        public String getTimeZoneID() {
            return java.util.TimeZone.getDefault().getID();
        }
    };
    @Override
    public IBinder onBind(Intent intent) {
        if (ISecondary.class.getName().equals(intent.getAction())) {
            return mSecondaryBinder;
        }
        return null;
    }
}
