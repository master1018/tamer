public class SyncContext {
    private ISyncContext mSyncContext;
    private long mLastHeartbeatSendTime;
    private static final long HEARTBEAT_SEND_INTERVAL_IN_MS = 1000;
    public SyncContext(ISyncContext syncContextInterface) {
        mSyncContext = syncContextInterface;
        mLastHeartbeatSendTime = 0;
    }
    public void setStatusText(String message) {
        updateHeartbeat();
    }
    private void updateHeartbeat() {
        final long now = SystemClock.elapsedRealtime();
        if (now < mLastHeartbeatSendTime + HEARTBEAT_SEND_INTERVAL_IN_MS) return;
        try {
            mLastHeartbeatSendTime = now;
            if (mSyncContext != null) {
                mSyncContext.sendHeartbeat();
            }
        } catch (RemoteException e) {
        }
    }
    public void onFinished(SyncResult result) {
        try {
            if (mSyncContext != null) {
                mSyncContext.onFinished(result);
            }
        } catch (RemoteException e) {
        }
    }
    public IBinder getSyncContextBinder() {
        return (mSyncContext == null) ? null : mSyncContext.asBinder();
    }
}
