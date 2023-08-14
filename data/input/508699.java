public class SyncManagerTest extends AndroidTestCase {
    private static class MyContext extends ContextWrapper {
        public boolean isGetFileStreamPathCalled;
        public MyContext(Context base) {
            super(base);
        }
        @Override
        public File getFileStreamPath(String name) {
            isGetFileStreamPathCalled = true;
            return super.getFileStreamPath(name);
        }
    }
    public void testGetDeviceId() throws Exception {
        final MyContext context = new MyContext(getContext());
        final String id = SyncManager.getDeviceId(context);
        assertTrue(id.matches("^[a-zA-Z0-9]+$"));
        context.isGetFileStreamPathCalled = false;
        final String cachedId = SyncManager.getDeviceId(context);
        assertEquals(id, cachedId);
        assertFalse(context.isGetFileStreamPathCalled);
    }
}
