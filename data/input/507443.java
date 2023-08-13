public abstract class FileObserver {
    public static final int ACCESS = 0x00000001;
    public static final int MODIFY = 0x00000002;
    public static final int ATTRIB = 0x00000004;
    public static final int CLOSE_WRITE = 0x00000008;
    public static final int CLOSE_NOWRITE = 0x00000010;
    public static final int OPEN = 0x00000020;
    public static final int MOVED_FROM = 0x00000040;
    public static final int MOVED_TO = 0x00000080;
    public static final int CREATE = 0x00000100;
    public static final int DELETE = 0x00000200;
    public static final int DELETE_SELF = 0x00000400;
    public static final int MOVE_SELF = 0x00000800;
    public static final int ALL_EVENTS = ACCESS | MODIFY | ATTRIB | CLOSE_WRITE
            | CLOSE_NOWRITE | OPEN | MOVED_FROM | MOVED_TO | DELETE | CREATE
            | DELETE_SELF | MOVE_SELF;
    private static final String LOG_TAG = "FileObserver";
    private static class ObserverThread extends Thread {
        private HashMap<Integer, WeakReference> m_observers = new HashMap<Integer, WeakReference>();
        private int m_fd;
        public ObserverThread() {
            super("FileObserver");
            m_fd = init();
        }
        public void run() {
            observe(m_fd);
        }
        public int startWatching(String path, int mask, FileObserver observer) {
            int wfd = startWatching(m_fd, path, mask);
            Integer i = new Integer(wfd);
            if (wfd >= 0) {
                synchronized (m_observers) {
                    m_observers.put(i, new WeakReference(observer));
                }
            }
            return i;
        }
        public void stopWatching(int descriptor) {
            stopWatching(m_fd, descriptor);
        }
        public void onEvent(int wfd, int mask, String path) {
            FileObserver observer = null;
            synchronized (m_observers) {
                WeakReference weak = m_observers.get(wfd);
                if (weak != null) {  
                    observer = (FileObserver) weak.get();
                    if (observer == null) {
                        m_observers.remove(wfd);
                    }
                }
            }
            if (observer != null) {
                try {
                    observer.onEvent(mask, path);
                } catch (Throwable throwable) {
                    Log.wtf(LOG_TAG, "Unhandled exception in FileObserver " + observer, throwable);
                }
            }
        }
        private native int init();
        private native void observe(int fd);
        private native int startWatching(int fd, String path, int mask);
        private native void stopWatching(int fd, int wfd);
    }
    private static ObserverThread s_observerThread;
    static {
        s_observerThread = new ObserverThread();
        s_observerThread.start();
    }
    private String m_path;
    private Integer m_descriptor;
    private int m_mask;
    public FileObserver(String path) {
        this(path, ALL_EVENTS);
    }
    public FileObserver(String path, int mask) {
        m_path = path;
        m_mask = mask;
        m_descriptor = -1;
    }
    protected void finalize() {
        stopWatching();
    }
    public void startWatching() {
        if (m_descriptor < 0) {
            m_descriptor = s_observerThread.startWatching(m_path, m_mask, this);
        }
    }
    public void stopWatching() {
        if (m_descriptor >= 0) {
            s_observerThread.stopWatching(m_descriptor);
            m_descriptor = -1;
        }
    }
    public abstract void onEvent(int event, String path);
}
