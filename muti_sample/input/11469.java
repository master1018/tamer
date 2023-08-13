public abstract class VSyncedBSManager {
    private static VSyncedBSManager theInstance;
    private static final boolean vSyncLimit =
        Boolean.valueOf((String)java.security.AccessController.doPrivileged(
                new sun.security.action.GetPropertyAction(
                    "sun.java2d.vsynclimit", "true")));
    private static VSyncedBSManager getInstance(boolean create) {
        if (theInstance == null && create) {
            theInstance =
                vSyncLimit ? new SingleVSyncedBSMgr() : new NoLimitVSyncBSMgr();
        }
        return theInstance;
    }
    abstract boolean checkAllowed(BufferStrategy bs);
    abstract void relinquishVsync(BufferStrategy bs);
    public static boolean vsyncAllowed(BufferStrategy bs) {
        VSyncedBSManager bsm = getInstance(true);
        return bsm.checkAllowed(bs);
    }
    public static synchronized void releaseVsync(BufferStrategy bs) {
        VSyncedBSManager bsm = getInstance(false);
        if (bsm != null) {
            bsm.relinquishVsync(bs);
        }
    }
    private static final class NoLimitVSyncBSMgr extends VSyncedBSManager {
        @Override
        boolean checkAllowed(BufferStrategy bs) {
            return true;
        }
        @Override
        void relinquishVsync(BufferStrategy bs) {
        }
    }
    private static final class SingleVSyncedBSMgr extends VSyncedBSManager {
        private WeakReference<BufferStrategy> strategy;
        @Override
        public synchronized boolean checkAllowed(BufferStrategy bs) {
            if (strategy != null) {
                BufferStrategy current = strategy.get();
                if (current != null) {
                    return (current == bs);
                }
            }
            strategy = new WeakReference<BufferStrategy>(bs);
            return true;
        }
        @Override
        public synchronized void relinquishVsync(BufferStrategy bs) {
            if (strategy != null) {
                BufferStrategy b = strategy.get();
                if (b == bs) {
                    strategy.clear();
                    strategy = null;
                }
            }
        }
    }
}
