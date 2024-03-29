public class SSLPerformanceTest extends AndroidTestCase {
    static final byte[] SESSION_DATA = new byte[6000];
    static {
        for (int i = 0; i < SESSION_DATA.length; i++) {
            SESSION_DATA[i] = (byte) i;
        }
    }
    static final File dataDir = new File("/data/data/android.core/");
    static final File filesDir = new File(dataDir, "files");
    static final File dbDir = new File(dataDir, "databases");
    static final String CACHE_DIR
            = SSLPerformanceTest.class.getName() + "/cache";
    static final int ITERATIONS = 10;
    public void testCreateNewEmptyDatabase() {
        deleteDatabase();
        Stopwatch stopwatch = new Stopwatch();
        DatabaseSessionCache cache = new DatabaseSessionCache(getContext());
        cache.getSessionData("crazybob.org", 443);
        stopwatch.stop();
    }
    public void testCreateNewEmptyDirectory() throws IOException {
        deleteDirectory();
        Stopwatch stopwatch = new Stopwatch();
        SSLClientSessionCache cache = FileClientSessionCache.usingDirectory(
                getCacheDirectory());
        cache.getSessionData("crazybob.org", 443);
        stopwatch.stop();
    }
    public void testOpenDatabaseWith10Sessions() {
        deleteDatabase();
        DatabaseSessionCache cache = new DatabaseSessionCache(getContext());
        putSessionsIn(cache);
        closeDatabase();
        System.err.println("Size of ssl_sessions.db w/ 10 sessions: "
                + new File(dbDir, "ssl_sessions.db").length());
        Stopwatch stopwatch = new Stopwatch();
        cache = new DatabaseSessionCache(getContext());
        cache.getSessionData("crazybob.org", 443);
        stopwatch.stop();
    }
    public void testOpenDirectoryWith10Sessions() throws IOException {
        deleteDirectory();
        SSLClientSessionCache cache = FileClientSessionCache.usingDirectory(
                getCacheDirectory());
        putSessionsIn(cache);
        closeDirectoryCache();
        Stopwatch stopwatch = new Stopwatch();
        cache = FileClientSessionCache.usingDirectory(
                getCacheDirectory());
        cache.getSessionData("crazybob.org", 443);
        stopwatch.stop();
    }
    public void testGetSessionFromDatabase() {
        deleteDatabase();
        DatabaseSessionCache cache = new DatabaseSessionCache(getContext());
        cache.putSessionData(new FakeSession("foo"), SESSION_DATA);
        closeDatabase();
        cache = new DatabaseSessionCache(getContext());
        cache.getSessionData("crazybob.org", 443);
        Stopwatch stopwatch = new Stopwatch();
        byte[] sessionData = cache.getSessionData("foo", 443);
        stopwatch.stop();
        assertTrue(Arrays.equals(SESSION_DATA, sessionData));
    }
    public void testGetSessionFromDirectory() throws IOException {
        deleteDirectory();
        SSLClientSessionCache cache = FileClientSessionCache.usingDirectory(
                getCacheDirectory());
        cache.putSessionData(new FakeSession("foo"), SESSION_DATA);
        closeDirectoryCache();
        cache = FileClientSessionCache.usingDirectory(
                getCacheDirectory());
        cache.getSessionData("crazybob.org", 443);
        Stopwatch stopwatch = new Stopwatch();
        byte[] sessionData = cache.getSessionData("foo", 443);
        stopwatch.stop();
        assertTrue(Arrays.equals(SESSION_DATA, sessionData));
    }
    public void testPutSessionIntoDatabase() {
        deleteDatabase();
        DatabaseSessionCache cache = new DatabaseSessionCache(getContext());
        cache.getSessionData("crazybob.org", 443);
        Stopwatch stopwatch = new Stopwatch();
        cache.putSessionData(new FakeSession("foo"), SESSION_DATA);
        stopwatch.stop();
    }
    public void testPutSessionIntoDirectory() throws IOException {
        deleteDirectory();
        SSLClientSessionCache cache = FileClientSessionCache.usingDirectory(
                getCacheDirectory());
        cache.getSessionData("crazybob.org", 443);
        Stopwatch stopwatch = new Stopwatch();
        cache.putSessionData(new FakeSession("foo"), SESSION_DATA);
        stopwatch.stop();
    }
    public void testEngineInit() throws IOException, KeyManagementException {
        Stopwatch stopwatch = new Stopwatch();
        new SSLContextImpl().engineInit(null, null, null);
        stopwatch.stop();
    }
    public void testWebRequestWithoutCache() throws IOException,
            KeyManagementException {
        SSLContextImpl sslContext = new SSLContextImpl();
        sslContext.engineInit(null, null, null);
        Stopwatch stopwatch = new Stopwatch();
        getVerisignDotCom(sslContext);
        stopwatch.stop();
    }
    public void testWebRequestWithFileCache() throws IOException,
            KeyManagementException {
        deleteDirectory();
        SSLContextImpl sslContext = new SSLContextImpl();
        sslContext.engineInit(null, null, null,
                FileClientSessionCache.usingDirectory(getCacheDirectory()),
                null);
        getVerisignDotCom(sslContext);
        sslContext.engineInit(null, null, null,
                FileClientSessionCache.usingDirectory(getCacheDirectory()),
                null);
        Stopwatch stopwatch = new Stopwatch();
        getVerisignDotCom(sslContext);
        stopwatch.stop();
    }
    public void testWebRequestWithInMemoryCache() throws IOException,
            KeyManagementException {
        deleteDirectory();
        SSLContextImpl sslContext = new SSLContextImpl();
        sslContext.engineInit(null, null, null);
        getVerisignDotCom(sslContext);
        Stopwatch stopwatch = new Stopwatch();
        getVerisignDotCom(sslContext);
        stopwatch.stop();
    }
    private void getVerisignDotCom(SSLContextImpl sslContext)
            throws IOException {
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("https",
                new SSLSocketFactory(sslContext.engineGetSocketFactory()),
                443));
        ClientConnectionManager manager =
                new SingleClientConnManager(null, schemeRegistry);
        new DefaultHttpClient(manager, null).execute(
                new HttpGet("https:
                new ResponseHandler<Object>() {
                    public Object handleResponse(HttpResponse response)
                            throws ClientProtocolException, IOException {
                        return null;
                    }
                });
    }
    private void putSessionsIn(SSLClientSessionCache cache) {
        for (int i = 0; i < 10; i++) {
            cache.putSessionData(new FakeSession("host" + i), SESSION_DATA);
        }
    }
    private void deleteDatabase() {
        closeDatabase();
        if (!new File(dbDir, "ssl_sessions.db").delete()) {
            System.err.println("Failed to delete database.");
        }
    }
    private void closeDatabase() {
        if (DatabaseSessionCache.sDefaultDatabaseHelper != null) {
            DatabaseSessionCache.sDefaultDatabaseHelper.close();
        }
        DatabaseSessionCache.sDefaultDatabaseHelper = null;
        DatabaseSessionCache.sHookInitializationDone = false;
        DatabaseSessionCache.mNeedsCacheLoad = true;
    }
    private void deleteDirectory() {
        closeDirectoryCache();
        File dir = getCacheDirectory();
        if (!dir.exists()) {
            return;
        }
        for (File file : dir.listFiles()) {
            file.delete();
        }
        if (!dir.delete()) {
            System.err.println("Failed to delete directory.");
        }
    }
    private void closeDirectoryCache() {
        try {
            Method reset = FileClientSessionCache.class
                    .getDeclaredMethod("reset");
            reset.setAccessible(true);
            reset.invoke(null);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    private File getCacheDirectory() {
        return new File(getContext().getFilesDir(), CACHE_DIR);
    }
    class Stopwatch {
        {
            Debug.startAllocCounting();
        }
        long start = System.nanoTime();
        void stop() {
            long elapsed = (System.nanoTime() - start) / 1000;
            Debug.stopAllocCounting();
            System.err.println(getName() + ": " + elapsed + "us, "
                + Debug.getThreadAllocCount() + " allocations, "
                + Debug.getThreadAllocSize() + " bytes");
        }
    }
}
class FakeSession implements SSLSession {
    final String host;
    FakeSession(String host) {
        this.host = host;
    }
    public int getApplicationBufferSize() {
        throw new UnsupportedOperationException();
    }
    public String getCipherSuite() {
        throw new UnsupportedOperationException();
    }
    public long getCreationTime() {
        throw new UnsupportedOperationException();
    }
    public byte[] getId() {
        return host.getBytes();
    }
    public long getLastAccessedTime() {
        throw new UnsupportedOperationException();
    }
    public Certificate[] getLocalCertificates() {
        throw new UnsupportedOperationException();
    }
    public Principal getLocalPrincipal() {
        throw new UnsupportedOperationException();
    }
    public int getPacketBufferSize() {
        throw new UnsupportedOperationException();
    }
    public javax.security.cert.X509Certificate[] getPeerCertificateChain() {
        throw new UnsupportedOperationException();
    }
    public Certificate[] getPeerCertificates() {
        throw new UnsupportedOperationException();
    }
    public String getPeerHost() {
        return host;
    }
    public int getPeerPort() {
        return 443;
    }
    public Principal getPeerPrincipal() {
        throw new UnsupportedOperationException();
    }
    public String getProtocol() {
        throw new UnsupportedOperationException();
    }
    public SSLSessionContext getSessionContext() {
        throw new UnsupportedOperationException();
    }
    public Object getValue(String name) {
        throw new UnsupportedOperationException();
    }
    public String[] getValueNames() {
        throw new UnsupportedOperationException();
    }
    public void invalidate() {
        throw new UnsupportedOperationException();
    }
    public boolean isValid() {
        throw new UnsupportedOperationException();
    }
    public void putValue(String name, Object value) {
        throw new UnsupportedOperationException();
    }
    public void removeValue(String name) {
        throw new UnsupportedOperationException();
    }
}
