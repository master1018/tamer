public class BinderThreadPriorityService extends Service {
    private static final String TAG = "BinderThreadPriorityService";
    private final IBinderThreadPriorityService.Stub mBinder =
            new IBinderThreadPriorityService.Stub() {
        public int getThreadPriority() {
            return Process.getThreadPriority(Process.myTid());
        }
        public String getThreadSchedulerGroup() {
            return BinderThreadPriorityTest.getSchedulerGroup();
        }
        public void callBack(IBinderThreadPriorityService recurse) {
            try {
                recurse.callBack(this);
            } catch (RemoteException e) {
                Log.e(TAG, "Binder callback failed", e);
            }
        }
        public void setPriorityAndCallBack(int priority, IBinderThreadPriorityService recurse) {
            Process.setThreadPriority(priority);
            try {
                recurse.callBack(this);
            } catch (RemoteException e) {
                Log.e(TAG, "Binder callback failed", e);
            }
        }
    };
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
