public abstract class TokenWatcher
{
    public TokenWatcher(Handler h, String tag)
    {
        mHandler = h;
        mTag = tag != null ? tag : "TokenWatcher";
    }
    public abstract void acquired();
    public abstract void released();
    public void acquire(IBinder token, String tag)
    {
        synchronized (mTokens) {
            int oldSize = mTokens.size();
            Death d = new Death(token, tag);
            try {
                token.linkToDeath(d, 0);
            } catch (RemoteException e) {
                return;
            }
            mTokens.put(token, d);
            if (oldSize == 0 && !mAcquired) {
                sendNotificationLocked(true);
                mAcquired = true;
            }
        }
    }
    public void cleanup(IBinder token, boolean unlink)
    {
        synchronized (mTokens) {
            Death d = mTokens.remove(token);
            if (unlink && d != null) {
                d.token.unlinkToDeath(d, 0);
                d.token = null;
            }
            if (mTokens.size() == 0 && mAcquired) {
                sendNotificationLocked(false);
                mAcquired = false;
            }
        }
    }
    public void release(IBinder token)
    {
        cleanup(token, true);
    }
    public boolean isAcquired()
    {
        synchronized (mTokens) {
            return mAcquired;
        }
    }
    public void dump()
    {
        synchronized (mTokens) {
            Set<IBinder> keys = mTokens.keySet();
            Log.i(mTag, "Token count: " + mTokens.size());
            int i = 0;
            for (IBinder b: keys) {
                Log.i(mTag, "[" + i + "] " + mTokens.get(b).tag + " - " + b);
                i++;
            }
        }
    }
    private Runnable mNotificationTask = new Runnable() {
        public void run()
        {
            int value;
            synchronized (mTokens) {
                value = mNotificationQueue;
                mNotificationQueue = -1;
            }
            if (value == 1) {
                acquired();
            }
            else if (value == 0) {
                released();
            }
        }
    };
    private void sendNotificationLocked(boolean on)
    {
        int value = on ? 1 : 0;
        if (mNotificationQueue == -1) {
            mNotificationQueue = value;
            mHandler.post(mNotificationTask);
        }
        else if (mNotificationQueue != value) {
            mNotificationQueue = -1;
            mHandler.removeCallbacks(mNotificationTask);
        }
    }
    private class Death implements IBinder.DeathRecipient
    {
        IBinder token;
        String tag;
        Death(IBinder token, String tag)
        {
            this.token = token;
            this.tag = tag;
        }
        public void binderDied()
        {
            cleanup(token, false);
        }
        protected void finalize() throws Throwable
        {
            try {
                if (token != null) {
                    Log.w(mTag, "cleaning up leaked reference: " + tag);
                    release(token);
                }
            }
            finally {
                super.finalize();
            }
        }
    }
    private WeakHashMap<IBinder,Death> mTokens = new WeakHashMap<IBinder,Death>();
    private Handler mHandler;
    private String mTag;
    private int mNotificationQueue = -1;
    private volatile boolean mAcquired = false;
}
