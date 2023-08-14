public class KeyStoreTest extends ActivityUnitTestCase<Activity> {
    private static final String TEST_PASSWD = "12345678";
    private static final String TEST_EMPTY_PASSWD = "";
    private static final String TEST_SHORT_PASSWD = "short";
    private static final String TEST_PASSWD2 = "87654321";
    private static final String TEST_KEYNAME = "testkey";
    private static final String TEST_KEYNAME1 = "testkey1";
    private static final String TEST_KEYNAME2 = "testkey2";
    private static final String TEST_KEYVALUE = "test value";
    private static final String TEST_I18N = "\u4F60\u597D, \u4E16\u754C";
    private KeyStore mKeyStore = null;
    public KeyStoreTest() {
        super(Activity.class);
    }
    @Override
    protected void setUp() throws Exception {
        mKeyStore = KeyStore.getInstance();
        if (mKeyStore.test() != KeyStore.UNINITIALIZED) mKeyStore.reset();
        assertEquals(KeyStore.UNINITIALIZED, mKeyStore.test());
        super.setUp();
    }
    @Override
    protected void tearDown() throws Exception {
        mKeyStore.reset();
        super.tearDown();
    }
    public void testTest() throws Exception {
        assertEquals(KeyStore.UNINITIALIZED, mKeyStore.test());
    }
    public void testPassword() throws Exception {
        assertTrue(mKeyStore.password(TEST_PASSWD));
        assertEquals(KeyStore.NO_ERROR, mKeyStore.test());
        assertFalse(mKeyStore.password(TEST_PASSWD2, TEST_PASSWD2));
        assertTrue(mKeyStore.password(TEST_PASSWD, TEST_PASSWD2));
    }
    public void testPut() throws Exception {
        assertFalse(mKeyStore.put(TEST_KEYNAME, TEST_KEYVALUE));
        assertFalse(mKeyStore.contains(TEST_KEYNAME));
        mKeyStore.password(TEST_PASSWD);
        assertTrue(mKeyStore.put(TEST_KEYNAME, TEST_KEYVALUE));
    }
    public void testI18n() throws Exception {
        assertFalse(mKeyStore.put(TEST_I18N, TEST_I18N));
        assertFalse(mKeyStore.contains(TEST_I18N));
        mKeyStore.password(TEST_I18N);
        assertTrue(mKeyStore.put(TEST_I18N, TEST_I18N));
        assertTrue(mKeyStore.contains(TEST_I18N));
    }
    public void testDelete() throws Exception {
        assertTrue(mKeyStore.delete(TEST_KEYNAME));
        mKeyStore.password(TEST_PASSWD);
        assertTrue(mKeyStore.delete(TEST_KEYNAME));
        mKeyStore.put(TEST_KEYNAME, TEST_KEYVALUE);
        assertTrue(mKeyStore.delete(TEST_KEYNAME));
    }
    public void testContains() throws Exception {
        assertFalse(mKeyStore.contains(TEST_KEYNAME));
        mKeyStore.password(TEST_PASSWD);
        assertFalse(mKeyStore.contains(TEST_KEYNAME));
        mKeyStore.put(TEST_KEYNAME, TEST_KEYVALUE);
        assertTrue(mKeyStore.contains(TEST_KEYNAME));
    }
    public void testSaw() throws Exception {
        String[] results = mKeyStore.saw(TEST_KEYNAME);
        assertEquals(0, results.length);
        mKeyStore.password(TEST_PASSWD);
        mKeyStore.put(TEST_KEYNAME1, TEST_KEYVALUE);
        mKeyStore.put(TEST_KEYNAME2, TEST_KEYVALUE);
        results = mKeyStore.saw(TEST_KEYNAME);
        assertEquals(2, results.length);
    }
    public void testLock() throws Exception {
        assertFalse(mKeyStore.lock());
        mKeyStore.password(TEST_PASSWD);
        assertEquals(KeyStore.NO_ERROR, mKeyStore.test());
        assertTrue(mKeyStore.lock());
        assertEquals(KeyStore.LOCKED, mKeyStore.test());
    }
    public void testUnlock() throws Exception {
        mKeyStore.password(TEST_PASSWD);
        assertEquals(KeyStore.NO_ERROR, mKeyStore.test());
        mKeyStore.lock();
        assertFalse(mKeyStore.unlock(TEST_PASSWD2));
        assertTrue(mKeyStore.unlock(TEST_PASSWD));
    }
}
