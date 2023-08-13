public class FileObserverTest extends AndroidTestCase {
    private Observer mObserver;
    private File mTestFile;
    private static class Observer extends FileObserver {
        public List<Map> events = Lists.newArrayList();
        public int totalEvents = 0;
        public Observer(String path) {
            super(path);
        }
        public void onEvent(int event, String path) {
            synchronized (this) {
                totalEvents++;
                Map<String, Object> map = Maps.newHashMap();
                map.put("event", event);
                map.put("path", path);
                events.add(map);
                this.notifyAll();
            }
        }
    }
    @Override
    protected void setUp() throws Exception {
        mTestFile = File.createTempFile(".file_observer_test", ".txt");
    }
    @Override
    protected void tearDown() throws Exception {
        if (mTestFile != null && mTestFile.exists()) {
            mTestFile.delete();
        }
    }
    @LargeTest
    public void testRun() throws Exception {
        assertTrue(mTestFile.exists());
        assertNotNull(mTestFile.getParent());
        mObserver = new Observer(mTestFile.getParent());
        mObserver.startWatching();
        FileOutputStream out = new FileOutputStream(mTestFile);
        try {
            out.write(0x20);
            waitForEvent(); 
            waitForEvent(); 
            mTestFile.delete();
            waitForEvent(); 
            waitForEvent(); 
            mObserver.stopWatching();
            assertTrue(mObserver.totalEvents > 3);
        } finally {
            out.close();
        }
    }
    private void waitForEvent() {
        synchronized (mObserver) {
            boolean done = false;
            while (!done) {
                try {
                    mObserver.wait(2000);
                    done = true;
                } catch (InterruptedException e) {
                }
            }
            Iterator<Map> it = mObserver.events.iterator();
            while (it.hasNext()) {
                Map map = it.next();
                Log.i("FileObserverTest", "event: " + getEventString((Integer)map.get("event")) + " path: " + map.get("path"));
            }
            mObserver.events.clear();
        }
    }
    private String getEventString(int event) {
        switch (event) {
            case  FileObserver.ACCESS:
                return "ACCESS";
            case FileObserver.MODIFY:
                return "MODIFY";
            case FileObserver.ATTRIB:
                return "ATTRIB";
            case FileObserver.CLOSE_WRITE:
                return "CLOSE_WRITE";
            case FileObserver.CLOSE_NOWRITE:
                return "CLOSE_NOWRITE";
            case FileObserver.OPEN:
                return "OPEN";
            case FileObserver.MOVED_FROM:
                return "MOVED_FROM";
            case FileObserver.MOVED_TO:
                return "MOVED_TO";
            case FileObserver.CREATE:
                return "CREATE";
            case FileObserver.DELETE:
                return "DELETE";
            case FileObserver.DELETE_SELF:
                return "DELETE_SELF";
            case FileObserver.MOVE_SELF:
                return "MOVE_SELF";
            default:
                return "UNKNOWN";
        }
    }
}
