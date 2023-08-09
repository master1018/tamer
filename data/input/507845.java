public abstract class ContentObserver {
    private Transport mTransport;
    private Object lock = new Object();
     Handler mHandler;
    private final class NotificationRunnable implements Runnable {
        private boolean mSelf;
        public NotificationRunnable(boolean self) {
            mSelf = self;
        }
        public void run() {
            ContentObserver.this.onChange(mSelf);
        }
    }
    private static final class Transport extends IContentObserver.Stub {
        ContentObserver mContentObserver;
        public Transport(ContentObserver contentObserver) {
            mContentObserver = contentObserver;
        }
        public boolean deliverSelfNotifications() {
            ContentObserver contentObserver = mContentObserver;
            if (contentObserver != null) {
                return contentObserver.deliverSelfNotifications();
            }
            return false;
        }
        public void onChange(boolean selfChange) {
            ContentObserver contentObserver = mContentObserver;
            if (contentObserver != null) {
                contentObserver.dispatchChange(selfChange);
            }
        }
        public void releaseContentObserver() {
            mContentObserver = null;
        }
    }
    public ContentObserver(Handler handler) {
        mHandler = handler;
    }
    public IContentObserver getContentObserver() {
        synchronized(lock) {
            if (mTransport == null) {
                mTransport = new Transport(this);
            }
            return mTransport;
        }
    }
    public IContentObserver releaseContentObserver() {
        synchronized(lock) {
            Transport oldTransport = mTransport;
            if (oldTransport != null) {
                oldTransport.releaseContentObserver();
                mTransport = null;
            }
            return oldTransport;
        }
    }
    public boolean deliverSelfNotifications() {
        return false;
    }
    public void onChange(boolean selfChange) {}
    public final void dispatchChange(boolean selfChange) {
        if (mHandler == null) {
            onChange(selfChange);
        } else {
            mHandler.post(new NotificationRunnable(selfChange));
        }
    }
}
