public class PresencePollingManager implements Runnable {
    private boolean mStopped;
    private boolean mFinished;
    private long mPollingInterval;
    private Object mLock = new Object();
    private ImpsAddress[] mPollingAddress;
    private ImpsAddress[] mContactLists;
    private ImpsContactListManager mManager;
    private Thread mPollingThread;
    public PresencePollingManager(ImpsContactListManager manager,
            long pollingIntervalMillis) {
        mManager = manager;
        mPollingInterval = pollingIntervalMillis;
        mStopped = true;
        mFinished = false;
    }
    public void resetPollingContacts() {
        synchronized (mLock) {
            mContactLists = null;
        }
    }
    public void startPolling() {
        synchronized (mLock) {
            mPollingAddress = null;
        }
        doStartPolling();
    }
    public void startPolling(ImpsUserAddress user){
        synchronized (mLock) {
            mPollingAddress = new ImpsAddress[] { user };
        }
        doStartPolling();
    }
    public void stopPolling() {
        mStopped = true;
    }
    public void shutdownPolling() {
        mFinished = true;
        synchronized (mLock) {
            mLock.notify();
        }
    }
    public void run() {
        while (!mFinished) {
            synchronized (mLock) {
                if (!mStopped) {
                    ImpsAddress[] pollingAddress = mPollingAddress;
                    if (pollingAddress == null) {
                        pollingAddress = getContactLists();
                    }
                    if (pollingAddress != null) {
                        mManager.fetchPresence(pollingAddress);
                    }
                }
                try {
                    mLock.wait(mPollingInterval);
                } catch (InterruptedException e) {
                }
            }
        }
    }
    private void doStartPolling() {
        mStopped = false;
        if (mPollingThread == null) {
            mPollingThread = new Thread(this, "PollingThread");
            mPollingThread.setDaemon(true);
            mPollingThread.start();
        } else {
            synchronized (mLock) {
                mLock.notify();
            }
        }
    }
    private ImpsAddress[] getContactLists() {
        if (mContactLists == null) {
            mContactLists = mManager.getAllListAddress();
        }
        return mContactLists;
    }
}
