public class ContentQueryMapTest extends AndroidTestCase {
    private abstract class LooperThread extends Thread {
        public Throwable mError = null;
        public boolean mSuccess = false;
        abstract void go();
        public void run() {
            try {
                Looper.prepare();
                go();
                Looper.loop();
            } catch (Throwable e) {
                mError = e;
            }
        }
    }
    @MediumTest
    public void testContentQueryMap() throws Throwable {
        LooperThread thread = new LooperThread() {
            void go() {
                ContentResolver r = getContext().getContentResolver();
                Settings.System.putString(r, "test", "Value");
                Cursor cursor = r.query(
                        Settings.System.CONTENT_URI,
                        new String[] {
                            Settings.System.NAME,
                            Settings.System.VALUE,
                        }, null, null, null);
                final ContentQueryMap cqm = new ContentQueryMap(
                        cursor, Settings.System.NAME, true, null);
                cqm.getRows();
                Settings.System.putString(r, "test", "New Value");
                ContentValues v = cqm.getValues("test");
                String value = v.getAsString(Settings.System.VALUE);
                assertEquals("Value", value);
                cqm.addObserver(new Observer() {
                    public void update(Observable o, Object arg) {
                        ContentValues v = cqm.getValues("test");
                        String value = v.getAsString(Settings.System.VALUE);
                        assertEquals("New Value", value);
                        Looper.myLooper().quit();
                        cqm.close();
                        mSuccess = true;
                    }
                });
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        fail("Timed out");
                    }
                }, 5000);
            }
        };
        thread.start();
        thread.join();
        if (thread.mError != null) throw thread.mError;
        assertTrue(thread.mSuccess);
    }
}
