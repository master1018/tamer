public class RemoteCallbackList<E extends IInterface> {
     HashMap<IBinder, Callback> mCallbacks
            = new HashMap<IBinder, Callback>();
    private Object[] mActiveBroadcast;
    private int mBroadcastCount = -1;
    private boolean mKilled = false;
    private final class Callback implements IBinder.DeathRecipient {
        final E mCallback;
        final Object mCookie;
        Callback(E callback, Object cookie) {
            mCallback = callback;
            mCookie = cookie;
        }
        public void binderDied() {
            synchronized (mCallbacks) {
                mCallbacks.remove(mCallback.asBinder());
            }
            onCallbackDied(mCallback, mCookie);
        }
    }
    public boolean register(E callback) {
        return register(callback, null);
    }
    public boolean register(E callback, Object cookie) {
        synchronized (mCallbacks) {
            if (mKilled) {
                return false;
            }
            IBinder binder = callback.asBinder();
            try {
                Callback cb = new Callback(callback, cookie);
                binder.linkToDeath(cb, 0);
                mCallbacks.put(binder, cb);
                return true;
            } catch (RemoteException e) {
                return false;
            }
        }
    }
    public boolean unregister(E callback) {
        synchronized (mCallbacks) {
            Callback cb = mCallbacks.remove(callback.asBinder());
            if (cb != null) {
                cb.mCallback.asBinder().unlinkToDeath(cb, 0);
                return true;
            }
            return false;
        }
    }
    public void kill() {
        synchronized (mCallbacks) {
            for (Callback cb : mCallbacks.values()) {
                cb.mCallback.asBinder().unlinkToDeath(cb, 0);
            }
            mCallbacks.clear();
            mKilled = true;
        }
    }
    public void onCallbackDied(E callback) {
    }
    public void onCallbackDied(E callback, Object cookie) {
        onCallbackDied(callback);
    }
    public int beginBroadcast() {
        synchronized (mCallbacks) {
            if (mBroadcastCount > 0) {
                throw new IllegalStateException(
                        "beginBroadcast() called while already in a broadcast");
            }
            final int N = mBroadcastCount = mCallbacks.size();
            if (N <= 0) {
                return 0;
            }
            Object[] active = mActiveBroadcast;
            if (active == null || active.length < N) {
                mActiveBroadcast = active = new Object[N];
            }
            int i=0;
            for (Callback cb : mCallbacks.values()) {
                active[i++] = cb;
            }
            return i;
        }
    }
    public E getBroadcastItem(int index) {
        return ((Callback)mActiveBroadcast[index]).mCallback;
    }
    public Object getBroadcastCookie(int index) {
        return ((Callback)mActiveBroadcast[index]).mCookie;
    }
    public void finishBroadcast() {
        if (mBroadcastCount < 0) {
            throw new IllegalStateException(
                    "finishBroadcast() called outside of a broadcast");
        }
        Object[] active = mActiveBroadcast;
        if (active != null) {
            final int N = mBroadcastCount;
            for (int i=0; i<N; i++) {
                active[i] = null;
            }
        }
        mBroadcastCount = -1;
    }
}
