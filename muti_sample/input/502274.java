 public class EasSyncServiceTests extends AndroidTestCase {
    Context mMockContext;
    @Override
    public void setUp() throws Exception {
        super.setUp();
        mMockContext = getContext();
    }
    public void testCreateUniqueFile() throws IOException {
        EasSyncService svc = new EasSyncService();
        svc.mContext = mMockContext;
        try {
            String fileName = "A11achm3n1.doc";
            File uniqueFile = svc.createUniqueFileInternal(null, fileName);
            assertEquals(fileName, uniqueFile.getName());
            if (uniqueFile.createNewFile()) {
                uniqueFile = svc.createUniqueFileInternal(null, fileName);
                assertEquals("A11achm3n1-2.doc", uniqueFile.getName());
                if (uniqueFile.createNewFile()) {
                    uniqueFile = svc.createUniqueFileInternal(null, fileName);
                    assertEquals("A11achm3n1-3.doc", uniqueFile.getName());
                }
           }
            fileName = "A11achm3n1";
            uniqueFile = svc.createUniqueFileInternal(null, fileName);
            assertEquals(fileName, uniqueFile.getName());
            if (uniqueFile.createNewFile()) {
                uniqueFile = svc.createUniqueFileInternal(null, fileName);
                assertEquals("A11achm3n1-2", uniqueFile.getName());
            }
        } finally {
            File directory = getContext().getFilesDir();
            String[] fileNames = new String[] {"A11achm3n1.doc", "A11achm3n1-2.doc", "A11achm3n1"};
            int length = fileNames.length;
            for (int i = 0; i < length; i++) {
                File file = new File(directory, fileNames[i]);
                if (file.exists()) {
                    file.delete();
                }
            }
        }
    }
    public void testResetHeartbeats() {
        EasSyncService svc = new EasSyncService();
        svc.mPingMaxHeartbeat = 1000;
        svc.mPingMinHeartbeat = 200;
        svc.mPingHeartbeat = 300;
        svc.mPingForceHeartbeat = 100;
        svc.mPingHeartbeatDropped = true;
        svc.resetHeartbeats(400);
        assertEquals(400, svc.mPingMinHeartbeat);
        assertEquals(1000, svc.mPingMaxHeartbeat);
        assertEquals(400, svc.mPingHeartbeat);
        assertEquals(400, svc.mPingForceHeartbeat);
        assertFalse(svc.mPingHeartbeatDropped);
        svc.mPingMaxHeartbeat = 1000;
        svc.mPingMinHeartbeat = 200;
        svc.mPingHeartbeat = 100;
        svc.mPingForceHeartbeat = 100;
        svc.mPingHeartbeatDropped = true;
        svc.resetHeartbeats(150);
        assertEquals(200, svc.mPingMinHeartbeat);
        assertEquals(1000, svc.mPingMaxHeartbeat);
        assertEquals(150, svc.mPingHeartbeat);
        assertEquals(150, svc.mPingForceHeartbeat);
        assertFalse(svc.mPingHeartbeatDropped);
        svc.mPingMaxHeartbeat = 1000;
        svc.mPingMinHeartbeat = 200;
        svc.mPingHeartbeat = 800;
        svc.mPingForceHeartbeat = 100;
        svc.mPingHeartbeatDropped = true;
        svc.resetHeartbeats(600);
        assertEquals(200, svc.mPingMinHeartbeat);
        assertEquals(600, svc.mPingMaxHeartbeat);
        assertEquals(600, svc.mPingHeartbeat);
        assertEquals(100, svc.mPingForceHeartbeat);
        assertFalse(svc.mPingHeartbeatDropped);
    }
    public void testAddHeaders() {
        HttpRequestBase method = new HttpPost();
        EasSyncService svc = new EasSyncService();
        svc.mAuthString = "auth";
        svc.mProtocolVersion = "12.1";
        svc.mDeviceType = "android";
        svc.mAccount = null;
        svc.setHeaders(method, false);
        Header[] headers = method.getHeaders("X-MS-PolicyKey");
        assertEquals(0, headers.length);
        method.removeHeaders("X-MS-PolicyKey");
        svc.setHeaders(method, true);
        headers = method.getHeaders("X-MS-PolicyKey");
        assertEquals(1, headers.length);
        assertEquals("0", headers[0].getValue());
        Account account = new Account();
        account.mSecuritySyncKey = null;
        svc.mAccount = account;
        method.removeHeaders("X-MS-PolicyKey");
        svc.setHeaders(method, true);
        headers = method.getHeaders("X-MS-PolicyKey");
        assertEquals(1, headers.length);
        assertEquals("0", headers[0].getValue());
        account.mSecuritySyncKey = "key";
        svc.mAccount = account;
        method.removeHeaders("X-MS-PolicyKey");
        svc.setHeaders(method, true);
        headers = method.getHeaders("X-MS-PolicyKey");
        assertEquals(1, headers.length);
        assertEquals("key", headers[0].getValue());
    }
}
