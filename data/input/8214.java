public class CreatedFontTracker {
    public static final int MAX_FILE_SIZE = 32 * 1024 * 1024;
    public static final int MAX_TOTAL_BYTES = 10 * MAX_FILE_SIZE;
    static int numBytes;
    static CreatedFontTracker tracker;
    public static synchronized CreatedFontTracker getTracker() {
        if (tracker == null) {
            tracker = new CreatedFontTracker();
        }
        return tracker;
    }
    public synchronized int getNumBytes() {
        return numBytes;
    }
    public synchronized void addBytes(int sz) {
        numBytes += sz;
    }
    public synchronized void subBytes(int sz) {
        numBytes -= sz;
    }
}
