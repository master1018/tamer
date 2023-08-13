@TestTargetClass(Proxy.class)
public class ProxyTest extends AndroidTestCase {
    private Context mContext;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getContext();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "Proxy",
        args = {}
    )
    public void testConstructor() {
        new Proxy();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDefaultPort",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDefaultHost",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getPort",
            args = {Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getHost",
            args = {Context.class}
        )
    })
    @BrokenTest("Cannot write secure settings table")
    public void testAccessProperties() {
        final int minValidPort = 0;
        final int maxValidPort = 65535;
        int defaultPort = Proxy.getDefaultPort();
        if(null == Proxy.getDefaultHost()) {
            assertEquals(-1, defaultPort);
        } else {
            assertTrue(defaultPort >= minValidPort && defaultPort <= maxValidPort);
        }
        final String host = "proxy.example.com";
        final int port = 2008;
        Secure.putString(mContext.getContentResolver(), Secure.HTTP_PROXY, host + ":" + port);
        assertEquals(host, Proxy.getHost(mContext));
        assertEquals(port, Proxy.getPort(mContext));
    }
}
