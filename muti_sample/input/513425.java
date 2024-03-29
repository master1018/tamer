public class VpnTest extends AndroidTestCase {
    private static final String NAME = "a name";
    private static final String SERVER_NAME = "a server name";
    private static final String ID = "some id";
    private static final String SUFFICES = "some suffices";
    private static final String ROUTES = "some routes";
    private static final String SAVED_NAME = "some name";
    @Override
    public void setUp() {
    }
    @Override
    public void tearDown() {
    }
    @SmallTest
    public void testVpnType() {
        testVpnType(VpnType.L2TP);
        testVpnType(VpnType.L2TP_IPSEC);
        testVpnType(VpnType.L2TP_IPSEC_PSK);
        testVpnType(VpnType.PPTP);
    }
    @SmallTest
    public void testVpnProfile() {
        VpnState state = VpnState.CONNECTING;
        testVpnProfile(createTestProfile(state), state);
    }
    @SmallTest
    public void testGetType() {
        assertEquals(VpnType.L2TP, new L2tpProfile().getType());
        assertEquals(VpnType.L2TP_IPSEC, new L2tpIpsecProfile().getType());
        assertEquals(VpnType.L2TP_IPSEC_PSK, 
                new L2tpIpsecPskProfile().getType());
        assertEquals(VpnType.PPTP, new PptpProfile().getType());
    }
    @SmallTest
    public void testVpnTypes() {
        assertTrue(VpnManager.getSupportedVpnTypes().length > 0);
    }
    @SmallTest
    public void testGetTypeFromManager() {
        VpnManager m = new VpnManager(getContext());
        VpnType[] types = VpnManager.getSupportedVpnTypes();
        for (VpnType t : types) {
            assertEquals(t, m.createVpnProfile(t).getType());
        }
    }
    @SmallTest
    public void testParcelable() {
        VpnProfile p = createTestProfile(VpnState.CONNECTED);
        Parcel parcel = Parcel.obtain();
        p.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        testVpnProfile(VpnProfile.CREATOR.createFromParcel(parcel), null);
    }
    @SmallTest
    public void testReceiver() {
        final String profileName = "whatever";
        final VpnState state = VpnState.DISCONNECTING;
        final ConditionVariable cv = new ConditionVariable();
        cv.close();
        BroadcastReceiver r = new BroadcastReceiver() {
            public void onReceive(Context c, Intent i) {
                assertEquals(profileName,
                        i.getStringExtra(VpnManager.BROADCAST_PROFILE_NAME));
                assertEquals(state, i.getSerializableExtra(
                        VpnManager.BROADCAST_CONNECTION_STATE));
                cv.open();
            }
        };
        VpnManager m = new VpnManager(getContext());
        m.registerConnectivityReceiver(r);
        m.broadcastConnectivity(profileName, state);
        assertTrue(cv.block(5000));
    }
    private void testVpnType(VpnType type) {
        assertFalse(TextUtils.isEmpty(type.getDisplayName()));
        assertNotNull(type.getProfileClass());
    }
    private VpnProfile createTestProfile(VpnState state) {
        VpnProfile p = new L2tpProfile();
        p.setName(NAME);
        p.setServerName(SERVER_NAME);
        p.setId(ID);
        p.setDomainSuffices(SUFFICES);
        p.setRouteList(ROUTES);
        p.setSavedUsername(SAVED_NAME);
        p.setState(state);
        return p;
    }
    private void testVpnProfile(VpnProfile p, VpnState state) {
        assertEquals(NAME, p.getName());
        assertEquals(SERVER_NAME, p.getServerName());
        assertEquals(ID, p.getId());
        assertEquals(SUFFICES, p.getDomainSuffices());
        assertEquals(ROUTES, p.getRouteList());
        assertEquals(SAVED_NAME, p.getSavedUsername());
        if (state != null) assertEquals(state, p.getState());
    }
}
