public abstract class UEventObserver {
    private static final String TAG = UEventObserver.class.getSimpleName();
    static public class UEvent {
        public HashMap<String,String> mMap = new HashMap<String,String>();
        public UEvent(String message) {
            int offset = 0;
            int length = message.length();
            while (offset < length) {
                int equals = message.indexOf('=', offset);
                int at = message.indexOf(0, offset);
                if (at < 0) break;
                if (equals > offset && equals < at) {
                    mMap.put(message.substring(offset, equals),
                            message.substring(equals + 1, at));
                }
                offset = at + 1;
            }
        }
        public String get(String key) {
            return mMap.get(key);
        }
        public String get(String key, String defaultValue) {
            String result = mMap.get(key);
            return (result == null ? defaultValue : result);
        }
        public String toString() {
            return mMap.toString();
        }
    }
    private static UEventThread sThread;
    private static boolean sThreadStarted = false;
    private static class UEventThread extends Thread {
        private ArrayList<Object> mObservers = new ArrayList<Object>();
        UEventThread() {
            super("UEventObserver");
        }
        public void run() {
            native_setup();
            byte[] buffer = new byte[1024];
            int len;
            while (true) {
                len = next_event(buffer);
                if (len > 0) {
                    String bufferStr = new String(buffer, 0, len);  
                    synchronized (mObservers) {
                        for (int i = 0; i < mObservers.size(); i += 2) {
                            if (bufferStr.indexOf((String)mObservers.get(i)) != -1) {
                                ((UEventObserver)mObservers.get(i+1))
                                        .onUEvent(new UEvent(bufferStr));
                            }
                        }
                    }
                }
            }
        }
        public void addObserver(String match, UEventObserver observer) {
            synchronized(mObservers) {
                mObservers.add(match);
                mObservers.add(observer);
            }
        }
        public void removeObserver(UEventObserver observer) {
            synchronized(mObservers) {
                boolean found = true;
                while (found) {
                    found = false;
                    for (int i = 0; i < mObservers.size(); i += 2) {
                        if (mObservers.get(i+1) == observer) {
                            mObservers.remove(i+1);
                            mObservers.remove(i);
                            found = true;
                            break;
                        }
                    }
                }
            }
        }
    }
    private static native void native_setup();
    private static native int next_event(byte[] buffer);
    private static final synchronized void ensureThreadStarted() {
        if (sThreadStarted == false) {
            sThread = new UEventThread();
            sThread.start();
            sThreadStarted = true;
        }
    }
    public final synchronized void startObserving(String match) {
        ensureThreadStarted();
        sThread.addObserver(match, this);
    }
    public final synchronized void stopObserving() {
        sThread.removeObserver(this);
    }
    public abstract void onUEvent(UEvent event);
    protected void finalize() throws Throwable {
        try {
            stopObserving();
        } finally {
            super.finalize();
        }
    }
}
