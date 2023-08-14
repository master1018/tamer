public class GSMPhoneTest extends AndroidTestCase implements PerformanceTestCase {
    private SimulatedRadioControl mRadioControl;
    private GSMPhone mGSMPhone;
    private GSMTestHandler mGSMTestHandler;
    private Handler mHandler;
    private static final int EVENT_PHONE_STATE_CHANGED = 1;
    private static final int EVENT_DISCONNECT = 2;
    private static final int EVENT_RINGING = 3;
    private static final int EVENT_CHANNEL_OPENED = 4;
    private static final int EVENT_POST_DIAL = 5;
    private static final int EVENT_DONE = 6;
    private static final int EVENT_SSN = 7;
    private static final int EVENT_MMI_INITIATE = 8;
    private static final int EVENT_MMI_COMPLETE = 9;
    private static final int EVENT_IN_SERVICE = 10;
    private static final int SUPP_SERVICE_FAILED = 11;
    private static final int SERVICE_STATE_CHANGED = 12;
    private static final int EVENT_OEM_RIL_MESSAGE = 13;
    public static final int ANY_MESSAGE = -1;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mGSMTestHandler = new GSMTestHandler(mContext);
        mGSMTestHandler.start();
        synchronized (mGSMTestHandler) {
            do {
                mGSMTestHandler.wait();
            } while (mGSMTestHandler.getGSMPhone() == null);
        }
        mGSMPhone = mGSMTestHandler.getGSMPhone();
        mRadioControl = mGSMTestHandler.getSimulatedCommands();
        mHandler = mGSMTestHandler.getHandler();
        mGSMPhone.registerForPreciseCallStateChanged(mHandler, EVENT_PHONE_STATE_CHANGED, null);
        mGSMPhone.registerForNewRingingConnection(mHandler, EVENT_RINGING, null);
        mGSMPhone.registerForDisconnect(mHandler, EVENT_DISCONNECT, null);
        mGSMPhone.setOnPostDialCharacter(mHandler, EVENT_POST_DIAL, null);
        mGSMPhone.registerForSuppServiceNotification(mHandler, EVENT_SSN, null);
        mGSMPhone.registerForMmiInitiate(mHandler, EVENT_MMI_INITIATE, null);
        mGSMPhone.registerForMmiComplete(mHandler, EVENT_MMI_COMPLETE, null);
        mGSMPhone.registerForSuppServiceFailed(mHandler, SUPP_SERVICE_FAILED, null);
        mGSMPhone.registerForServiceStateChanged(mHandler, SERVICE_STATE_CHANGED, null);
        Message msg;
        ServiceState state;
        do {
            msg = mGSMTestHandler.waitForMessage(SERVICE_STATE_CHANGED);
            assertNotNull("Message Time Out", msg);
            state = (ServiceState) ((AsyncResult) msg.obj).result;
        } while (state.getState() != ServiceState.STATE_IN_SERVICE);
    }
    @Override
    protected void tearDown() throws Exception {
        mRadioControl.shutdown();
        mGSMPhone.unregisterForPreciseCallStateChanged(mHandler);
        mGSMPhone.unregisterForNewRingingConnection(mHandler);
        mGSMPhone.unregisterForDisconnect(mHandler);
        mGSMPhone.setOnPostDialCharacter(mHandler, 0, null);
        mGSMPhone.unregisterForSuppServiceNotification(mHandler);
        mGSMPhone.unregisterForMmiInitiate(mHandler);
        mGSMPhone.unregisterForMmiComplete(mHandler);
        mGSMPhone = null;
        mRadioControl = null;
        mHandler = null;
        mGSMTestHandler.cleanup();
        super.tearDown();
    }
    public int startPerformance(Intermediates intermediates) {
        return 1;
    }
    public boolean isPerformanceOnly() {
        return false;
    }
    public void brokenTestGeneral() throws Exception {
        Connection cn;
        Message msg;
        AsyncResult ar;
        assertEquals(Phone.State.IDLE, mGSMPhone.getState());
        assertEquals(0, mGSMPhone.getRingingCall().getConnections().size());
        assertEquals(0, mGSMPhone.getForegroundCall().getConnections().size());
        assertEquals(0, mGSMPhone.getBackgroundCall().getConnections().size());
        assertEquals(Call.State.IDLE, mGSMPhone.getRingingCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getBackgroundCall().getState());
        assertEquals(0, mGSMPhone.getForegroundCall().getEarliestCreateTime());
        assertEquals(0, mGSMPhone.getForegroundCall().getEarliestConnectTime());
        assertFalse(mGSMPhone.canConference());
        mRadioControl.setAutoProgressConnectingCall(false);
        mGSMPhone.dial("+13125551212");
        assertEquals(Phone.State.OFFHOOK, mGSMPhone.getState());
        msg = mGSMTestHandler.waitForMessage(EVENT_PHONE_STATE_CHANGED);
        assertNotNull("Message Time Out", msg);
        assertEquals(Phone.State.OFFHOOK, mGSMPhone.getState());
        assertEquals(Call.State.DIALING, mGSMPhone.getForegroundCall().getState());
        assertTrue(mGSMPhone.getForegroundCall().isDialingOrAlerting());
        assertEquals(0, mGSMPhone.getRingingCall().getConnections().size());
        assertEquals(1, mGSMPhone.getForegroundCall().getConnections().size());
        assertEquals(0, mGSMPhone.getBackgroundCall().getConnections().size());
        assertEquals(Call.State.IDLE, mGSMPhone.getRingingCall().getState());
        assertEquals(Call.State.DIALING,
                mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getBackgroundCall().getState());
        assertTrue(mGSMPhone.getForegroundCall().getEarliestCreateTime() > 0);
        assertEquals(0, mGSMPhone.getForegroundCall().getEarliestConnectTime());
        cn = mGSMPhone.getForegroundCall().getConnections().get(0);
        assertTrue(!cn.isIncoming());
        assertEquals(Connection.PostDialState.NOT_STARTED, cn.getPostDialState());
        assertEquals(Connection.DisconnectCause.NOT_DISCONNECTED, cn.getDisconnectCause());
        assertFalse(mGSMPhone.canConference());
        mRadioControl.progressConnectingCallState();
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        }
        while (mGSMPhone.getForegroundCall().getState() != Call.State.ALERTING);
        assertEquals(Phone.State.OFFHOOK, mGSMPhone.getState());
        assertTrue(mGSMPhone.getForegroundCall().isDialingOrAlerting());
        assertEquals(0, mGSMPhone.getRingingCall().getConnections().size());
        assertEquals(1, mGSMPhone.getForegroundCall().getConnections().size());
        assertEquals(0, mGSMPhone.getBackgroundCall().getConnections().size());
        assertEquals(Call.State.IDLE, mGSMPhone.getRingingCall().getState());
        assertEquals(Call.State.ALERTING, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getBackgroundCall().getState());
        assertTrue(mGSMPhone.getForegroundCall().getEarliestCreateTime() > 0);
        assertEquals(0, mGSMPhone.getForegroundCall().getEarliestConnectTime());
        cn = mGSMPhone.getForegroundCall().getConnections().get(0);
        assertTrue(!cn.isIncoming());
        assertEquals(Connection.PostDialState.NOT_STARTED, cn.getPostDialState());
        assertFalse(mGSMPhone.canConference());
        mRadioControl.progressConnectingCallState();
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getForegroundCall().getState() != Call.State.ACTIVE);
        assertEquals(Phone.State.OFFHOOK, mGSMPhone.getState());
        assertFalse(mGSMPhone.getForegroundCall().isDialingOrAlerting());
        assertEquals(0, mGSMPhone.getRingingCall().getConnections().size());
        assertEquals(1, mGSMPhone.getForegroundCall().getConnections().size());
        assertEquals(0, mGSMPhone.getBackgroundCall().getConnections().size());
        assertEquals(Call.State.IDLE, mGSMPhone.getRingingCall().getState());
        assertEquals(Call.State.ACTIVE, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getBackgroundCall().getState());
        assertTrue(mGSMPhone.getForegroundCall().getEarliestCreateTime() > 0);
        assertTrue(mGSMPhone.getForegroundCall().getEarliestConnectTime() > 0);
        cn = mGSMPhone.getForegroundCall().getConnections().get(0);
        assertTrue(!cn.isIncoming());
        assertEquals(Connection.PostDialState.COMPLETE, cn.getPostDialState());
        assertFalse(mGSMPhone.canConference());
        mGSMPhone.getForegroundCall().hangup();
        msg = mGSMTestHandler.waitForMessage(EVENT_DISCONNECT);
        assertNotNull("Message Time Out", msg);
        assertEquals(Phone.State.IDLE, mGSMPhone.getState());
        assertFalse(mGSMPhone.getForegroundCall().isDialingOrAlerting());
        assertEquals(0, mGSMPhone.getRingingCall().getConnections().size());
        assertEquals(1, mGSMPhone.getForegroundCall().getConnections().size());
        assertEquals(0, mGSMPhone.getBackgroundCall().getConnections().size());
        assertEquals(Call.State.IDLE, mGSMPhone.getRingingCall().getState());
        assertEquals(Call.State.DISCONNECTED, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getBackgroundCall().getState());
        assertTrue(mGSMPhone.getForegroundCall().getEarliestCreateTime() > 0);
        assertTrue(mGSMPhone.getForegroundCall().getEarliestConnectTime() > 0);
        assertFalse(mGSMPhone.canConference());
        cn = mGSMPhone.getForegroundCall().getEarliestConnection();
        assertEquals(Call.State.DISCONNECTED, cn.getState());
        mGSMPhone.clearDisconnected();
        assertEquals(Phone.State.IDLE, mGSMPhone.getState());
        assertFalse(mGSMPhone.getForegroundCall().isDialingOrAlerting());
        assertEquals(0, mGSMPhone.getRingingCall().getConnections().size());
        assertEquals(0, mGSMPhone.getForegroundCall().getConnections().size());
        assertEquals(0, mGSMPhone.getBackgroundCall().getConnections().size());
        assertEquals(Call.State.IDLE, mGSMPhone.getRingingCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getBackgroundCall().getState());
        assertEquals(0, mGSMPhone.getForegroundCall().getEarliestCreateTime());
        assertEquals(0, mGSMPhone.getForegroundCall().getEarliestConnectTime());
        assertFalse(mGSMPhone.canConference());
        assertEquals(Call.State.DISCONNECTED, cn.getState());
        mRadioControl.triggerRing("18005551212");
        msg = mGSMTestHandler.waitForMessage(EVENT_RINGING);
        assertNotNull("Message Time Out", msg);
        assertEquals(Phone.State.RINGING, mGSMPhone.getState());
        assertTrue(mGSMPhone.getRingingCall().isRinging());
        ar = (AsyncResult) msg.obj;
        cn = (Connection) ar.result;
        assertTrue(cn.isRinging());
        assertEquals(mGSMPhone.getRingingCall(), cn.getCall());
        assertEquals(1, mGSMPhone.getRingingCall().getConnections().size());
        assertEquals(0, mGSMPhone.getForegroundCall().getConnections().size());
        assertEquals(0, mGSMPhone.getBackgroundCall().getConnections().size());
        assertEquals(Call.State.INCOMING, mGSMPhone.getRingingCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getBackgroundCall().getState());
        assertTrue(mGSMPhone.getRingingCall().getEarliestCreateTime() > 0);
        assertEquals(0, mGSMPhone.getRingingCall().getEarliestConnectTime());
        assertEquals(0, mGSMPhone.getForegroundCall().getEarliestCreateTime());
        assertEquals(0, mGSMPhone.getForegroundCall().getEarliestConnectTime());
        cn = mGSMPhone.getRingingCall().getConnections().get(0);
        assertTrue(cn.isIncoming());
        assertEquals(Connection.PostDialState.NOT_STARTED, cn.getPostDialState());
        assertFalse(mGSMPhone.canConference());
        mGSMPhone.acceptCall();
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getRingingCall().getConnections().size() == 1);
        assertEquals(Phone.State.OFFHOOK, mGSMPhone.getState());
        assertFalse(mGSMPhone.getRingingCall().isRinging());
        assertEquals(0, mGSMPhone.getRingingCall().getConnections().size());
        assertEquals(1, mGSMPhone.getForegroundCall().getConnections().size());
        assertEquals(0, mGSMPhone.getBackgroundCall().getConnections().size());
        assertEquals(Call.State.IDLE, mGSMPhone.getRingingCall().getState());
        assertEquals(Call.State.ACTIVE,
                mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getBackgroundCall().getState());
        assertTrue(mGSMPhone.getForegroundCall().getEarliestCreateTime() > 0);
        assertTrue(mGSMPhone.getForegroundCall().getEarliestConnectTime() > 0);
        cn = mGSMPhone.getForegroundCall().getConnections().get(0);
        assertTrue(cn.isIncoming());
        assertEquals(Connection.PostDialState.NOT_STARTED, cn.getPostDialState());
        assertFalse(mGSMPhone.canConference());
        try {
            Connection conn;
            conn = mGSMPhone.getForegroundCall().getConnections().get(0);
            conn.hangup();
        } catch (CallStateException ex) {
            ex.printStackTrace();
            fail("unexpected ex");
        }
        msg = mGSMTestHandler.waitForMessage(EVENT_DISCONNECT);
        assertNotNull("Message Time Out", msg);
        assertEquals(Phone.State.IDLE, mGSMPhone.getState());
        assertFalse(mGSMPhone.getForegroundCall().isDialingOrAlerting());
        assertFalse(mGSMPhone.getRingingCall().isRinging());
        assertEquals(0, mGSMPhone.getRingingCall().getConnections().size());
        assertEquals(1, mGSMPhone.getForegroundCall().getConnections().size());
        assertEquals(0, mGSMPhone.getBackgroundCall().getConnections().size());
        assertEquals(Call.State.IDLE, mGSMPhone.getRingingCall().getState());
        assertEquals(Call.State.DISCONNECTED,
                mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getBackgroundCall().getState());
        assertTrue(mGSMPhone.getForegroundCall().getEarliestCreateTime() > 0);
        assertTrue(mGSMPhone.getForegroundCall().getEarliestConnectTime() > 0);
        cn = mGSMPhone.getForegroundCall().getEarliestConnection();
        assertEquals(Call.State.DISCONNECTED, cn.getState());
        assertEquals(Connection.DisconnectCause.LOCAL, cn.getDisconnectCause());
        assertFalse(mGSMPhone.canConference());
        mGSMPhone.clearDisconnected();
        assertFalse(mGSMPhone.getForegroundCall().isDialingOrAlerting());
        assertFalse(mGSMPhone.getRingingCall().isRinging());
        assertEquals(Connection.DisconnectCause.LOCAL, cn.getDisconnectCause());
        assertEquals(0, mGSMPhone.getForegroundCall().getConnections().size());
        assertEquals(Phone.State.IDLE, mGSMPhone.getState());
        assertEquals(0, mGSMPhone.getRingingCall().getConnections().size());
        assertEquals(0, mGSMPhone.getForegroundCall().getConnections().size());
        assertEquals(0, mGSMPhone.getBackgroundCall().getConnections().size());
        assertEquals(Call.State.IDLE, mGSMPhone.getRingingCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getBackgroundCall().getState());
        assertEquals(0, mGSMPhone.getForegroundCall().getEarliestCreateTime());
        assertEquals(0, mGSMPhone.getForegroundCall().getEarliestConnectTime());
        assertFalse(mGSMPhone.canConference());
        assertEquals(Call.State.DISCONNECTED, cn.getState());
        mRadioControl.triggerRing("18005551212");
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getRingingCall().getConnections().isEmpty());
        assertEquals(Phone.State.RINGING, mGSMPhone.getState());
        assertFalse(mGSMPhone.getForegroundCall().isDialingOrAlerting());
        assertTrue(mGSMPhone.getRingingCall().isRinging());
        assertEquals(1, mGSMPhone.getRingingCall().getConnections().size());
        assertEquals(0, mGSMPhone.getForegroundCall().getConnections().size());
        assertEquals(0, mGSMPhone.getBackgroundCall().getConnections().size());
        assertEquals(Call.State.INCOMING, mGSMPhone.getRingingCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getBackgroundCall().getState());
        assertTrue(mGSMPhone.getRingingCall().getEarliestCreateTime() > 0);
        assertEquals(0, mGSMPhone.getRingingCall().getEarliestConnectTime());
        assertEquals(0, mGSMPhone.getForegroundCall().getEarliestCreateTime());
        assertEquals(0, mGSMPhone.getForegroundCall().getEarliestConnectTime());
        assertFalse(mGSMPhone.canConference());
        mGSMPhone.rejectCall();
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getState() != Phone.State.IDLE);
        assertFalse(mGSMPhone.getForegroundCall().isDialingOrAlerting());
        assertFalse(mGSMPhone.getRingingCall().isRinging());
        assertEquals(1, mGSMPhone.getRingingCall().getConnections().size());
        assertEquals(0, mGSMPhone.getForegroundCall().getConnections().size());
        assertEquals(0, mGSMPhone.getBackgroundCall().getConnections().size());
        assertEquals(Call.State.DISCONNECTED, mGSMPhone.getRingingCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getBackgroundCall().getState());
        assertTrue(mGSMPhone.getRingingCall().getEarliestCreateTime() > 0);
        assertEquals(0, mGSMPhone.getRingingCall().getEarliestConnectTime());
        assertEquals(0, mGSMPhone.getForegroundCall().getEarliestCreateTime());
        assertEquals(0, mGSMPhone.getForegroundCall().getEarliestConnectTime());
        cn = mGSMPhone.getRingingCall().getEarliestConnection();
        assertEquals(Call.State.DISCONNECTED, cn.getState());
        assertEquals(Connection.DisconnectCause.INCOMING_MISSED, cn.getDisconnectCause());
        assertFalse(mGSMPhone.canConference());
        mGSMPhone.clearDisconnected();
        assertEquals(Connection.DisconnectCause.INCOMING_MISSED, cn.getDisconnectCause());
        assertEquals(0, mGSMPhone.getForegroundCall().getConnections().size());
        assertEquals(Phone.State.IDLE, mGSMPhone.getState());
        assertEquals(0, mGSMPhone.getRingingCall().getConnections().size());
        assertEquals(0, mGSMPhone.getForegroundCall().getConnections().size());
        assertEquals(0, mGSMPhone.getBackgroundCall().getConnections().size());
        assertEquals(Call.State.IDLE, mGSMPhone.getRingingCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getBackgroundCall().getState());
        assertEquals(0, mGSMPhone.getForegroundCall().getEarliestCreateTime());
        assertEquals(0, mGSMPhone.getForegroundCall().getEarliestConnectTime());
        assertFalse(mGSMPhone.canConference());
        assertEquals(Call.State.DISCONNECTED, cn.getState());
        mRadioControl.triggerRing("18005551212");
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getRingingCall().getConnections().isEmpty());
        assertEquals(Phone.State.RINGING, mGSMPhone.getState());
        assertFalse(mGSMPhone.getForegroundCall().isDialingOrAlerting());
        assertTrue(mGSMPhone.getRingingCall().isRinging());
        cn = mGSMPhone.getRingingCall().getEarliestConnection();
        mRadioControl.triggerHangupForeground();
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getState() != Phone.State.IDLE);
        assertEquals(Connection.DisconnectCause.INCOMING_MISSED, cn.getDisconnectCause());
        mRadioControl.triggerRing("18005551212");
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getState() != Phone.State.RINGING);
        cn = mGSMPhone.getRingingCall().getEarliestConnection();
        mGSMPhone.acceptCall();
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getState() != Phone.State.OFFHOOK);
        assertEquals(Call.State.ACTIVE, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getBackgroundCall().getState());
        mGSMPhone.switchHoldingAndActive();
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getBackgroundCall().getState() == Call.State.IDLE);
        assertEquals(Call.State.IDLE, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.HOLDING, mGSMPhone.getBackgroundCall().getState());
        mGSMPhone.switchHoldingAndActive();
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        }
        while (mGSMPhone.getBackgroundCall().getState() == Call.State.HOLDING);
        assertEquals(Call.State.ACTIVE, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getBackgroundCall().getState());
        mRadioControl.triggerHangupAll();
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getState() != Phone.State.IDLE);
        assertEquals(Call.State.DISCONNECTED, mGSMPhone.getForegroundCall().getState());
        assertEquals(Connection.DisconnectCause.NORMAL, cn.getDisconnectCause());
        mRadioControl.triggerRing("18005551212");
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getState() != Phone.State.RINGING);
        mGSMPhone.rejectCall();
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (msg.what != EVENT_DISCONNECT);
        ar = (AsyncResult) msg.obj;
        cn = (Connection) ar.result;
        assertEquals(Connection.DisconnectCause.INCOMING_MISSED, cn.getDisconnectCause());
        assertEquals(Phone.State.IDLE, mGSMPhone.getState());
        assertEquals(Call.State.DISCONNECTED, mGSMPhone.getRingingCall().getState());
        mRadioControl.triggerRing("18005551212");
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getState() != Phone.State.RINGING);
        cn = mGSMPhone.getRingingCall().getEarliestConnection();
        mGSMPhone.acceptCall();
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getState() != Phone.State.OFFHOOK);
        assertEquals(Connection.DisconnectCause.NOT_DISCONNECTED, cn.getDisconnectCause());
        assertEquals(Call.State.IDLE, mGSMPhone.getRingingCall().getState());
        assertEquals(Call.State.ACTIVE, mGSMPhone.getForegroundCall().getState());
        try {
            mGSMPhone.getForegroundCall().hangup();
        } catch (CallStateException ex) {
            ex.printStackTrace();
            fail("unexpected ex");
        }
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getForegroundCall().getState()
                != Call.State.DISCONNECTED);
        assertEquals(Connection.DisconnectCause.LOCAL, cn.getDisconnectCause());
        mGSMPhone.dial("+13125551212");
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getState() != Phone.State.OFFHOOK);
        assertTrue(mGSMPhone.getForegroundCall().isDialingOrAlerting());
        mRadioControl.progressConnectingCallState();
        mRadioControl.progressConnectingCallState();
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getForegroundCall().getState() != Call.State.ACTIVE);
        assertFalse(mGSMPhone.getForegroundCall().isDialingOrAlerting());
        mRadioControl.triggerRing("18005551212");
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getState() != Phone.State.RINGING);
        assertFalse(mGSMPhone.getForegroundCall().isDialingOrAlerting());
        assertTrue(mGSMPhone.getRingingCall().isRinging());
        mGSMPhone.acceptCall();
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getState() != Phone.State.OFFHOOK);
        assertFalse(mGSMPhone.getForegroundCall().isDialingOrAlerting());
        assertEquals(Call.State.IDLE, mGSMPhone.getRingingCall().getState());
        assertEquals(Call.State.ACTIVE, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.HOLDING, mGSMPhone.getBackgroundCall().getState());
        assertTrue(mGSMPhone.canConference());
        mGSMPhone.conference();
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getBackgroundCall().getState() != Call.State.IDLE);
        assertFalse(mGSMPhone.getForegroundCall().isDialingOrAlerting());
        assertEquals(Call.State.IDLE, mGSMPhone.getRingingCall().getState());
        assertEquals(Call.State.ACTIVE, mGSMPhone.getForegroundCall().getState());
        assertTrue(mGSMPhone.getForegroundCall().isMultiparty());
        assertFalse(mGSMPhone.canConference());
        mGSMPhone.switchHoldingAndActive();
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        }
        while (mGSMPhone.getBackgroundCall().getState() != Call.State.HOLDING);
        assertEquals(Call.State.IDLE, mGSMPhone.getRingingCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getForegroundCall().getState());
        assertTrue(mGSMPhone.getBackgroundCall().isMultiparty());
        assertFalse(mGSMPhone.canConference());
        mRadioControl.triggerRing("18005558355");
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getState() != Phone.State.RINGING);
        assertFalse(mGSMPhone.getForegroundCall().isDialingOrAlerting());
        assertTrue(mGSMPhone.getRingingCall().isRinging());
        assertEquals(Call.State.IDLE, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.HOLDING, mGSMPhone.getBackgroundCall().getState());
        assertTrue(mGSMPhone.getBackgroundCall().isMultiparty());
        assertEquals(Call.State.WAITING, mGSMPhone.getRingingCall().getState());
        assertFalse(mGSMPhone.canConference());
        mGSMPhone.getBackgroundCall().hangup();
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getBackgroundCall().getState() != Call.State.DISCONNECTED);
        assertEquals(Phone.State.RINGING, mGSMPhone.getState());
        assertEquals(Call.State.DISCONNECTED, mGSMPhone.getBackgroundCall().getState());
        assertFalse(mGSMPhone.getForegroundCall().isDialingOrAlerting());
        assertTrue(mGSMPhone.getRingingCall().isRinging());
        mGSMPhone.rejectCall();
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getState() != Phone.State.IDLE);
        assertFalse(mGSMPhone.getForegroundCall().isDialingOrAlerting());
        assertFalse(mGSMPhone.getRingingCall().isRinging());
    }
    public void testOutgoingCallFailImmediately() throws Exception {
        Message msg;
        mRadioControl.setNextDialFailImmediately(true);
        Connection cn = mGSMPhone.dial("+13125551212");
        msg = mGSMTestHandler.waitForMessage(EVENT_DISCONNECT);
        assertNotNull("Message Time Out", msg);
        assertEquals(Phone.State.IDLE, mGSMPhone.getState());
        assertEquals(Connection.DisconnectCause.NORMAL, cn.getDisconnectCause());
        assertEquals(0, mGSMPhone.getRingingCall().getConnections().size());
        assertEquals(1, mGSMPhone.getForegroundCall().getConnections().size());
        assertEquals(0, mGSMPhone.getBackgroundCall().getConnections().size());
        assertEquals(Call.State.IDLE, mGSMPhone.getRingingCall().getState());
        assertEquals(Call.State.DISCONNECTED, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getBackgroundCall().getState());
        assertTrue(mGSMPhone.getForegroundCall().getEarliestCreateTime() > 0);
        assertEquals(0, mGSMPhone.getForegroundCall().getEarliestConnectTime());
    }
    public void testHangupOnOutgoing() throws Exception {
        Connection cn;
        Message msg;
        mRadioControl.setAutoProgressConnectingCall(false);
        mGSMPhone.dial("+13125551212");
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        }
        while (mGSMPhone.getForegroundCall().getState() != Call.State.DIALING);
        cn = mGSMPhone.getForegroundCall().getEarliestConnection();
        mGSMPhone.getForegroundCall().hangup();
        msg = mGSMTestHandler.waitForMessage(EVENT_DISCONNECT);
        assertNotNull("Message Time Out", msg);
        assertEquals(Phone.State.IDLE, mGSMPhone.getState());
        assertEquals(Call.State.DISCONNECTED, mGSMPhone.getForegroundCall().getState());
        assertEquals(Connection.DisconnectCause.LOCAL, cn.getDisconnectCause());
        mGSMPhone.dial("+13125551212");
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getState() != Phone.State.OFFHOOK);
        mRadioControl.progressConnectingCallState();
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        }
        while (mGSMPhone.getForegroundCall().getState() != Call.State.ALERTING);
        cn = mGSMPhone.getForegroundCall().getEarliestConnection();
        mGSMPhone.getForegroundCall().hangup();
        msg = mGSMTestHandler.waitForMessage(EVENT_DISCONNECT);
        assertNotNull("Message Time Out", msg);
        assertEquals(Phone.State.IDLE, mGSMPhone.getState());
        assertEquals(Call.State.DISCONNECTED, mGSMPhone.getForegroundCall().getState());
        assertEquals(Connection.DisconnectCause.LOCAL, cn.getDisconnectCause());
        mRadioControl.pauseResponses();
        cn = mGSMPhone.dial("+13125551212");
        cn.hangup();
        mRadioControl.resumeResponses();
        msg = mGSMTestHandler.waitForMessage(EVENT_DISCONNECT);
        assertNotNull("Message Time Out", msg);
        assertEquals(Phone.State.IDLE, mGSMPhone.getState());
        assertEquals(Call.State.DISCONNECTED, mGSMPhone.getForegroundCall().getState());
        assertEquals(Connection.DisconnectCause.LOCAL,
                mGSMPhone.getForegroundCall().getEarliestConnection().getDisconnectCause());
    }
    public void testHangupOnChannelClose() throws Exception {
        mGSMPhone.dial("+13125551212");
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getForegroundCall().getConnections().isEmpty());
        mRadioControl.shutdown();
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
            mGSMPhone.clearDisconnected();
        } while (!mGSMPhone.getForegroundCall().getConnections().isEmpty());
    }
    public void testIncallMmiCallDeflection() throws Exception {
        Message msg;
        mGSMPhone.dial("+13125551212");
        do {
            mRadioControl.progressConnectingCallState();
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getForegroundCall().getState() != Call.State.ACTIVE);
        assertEquals(Call.State.ACTIVE, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getBackgroundCall().getState());
        mRadioControl.triggerRing("18005551212");
        msg = mGSMTestHandler.waitForMessage(EVENT_RINGING);
        assertNotNull("Message Time Out", msg);
        assertEquals(Phone.State.RINGING, mGSMPhone.getState());
        assertTrue(mGSMPhone.getRingingCall().isRinging());
        assertEquals(Call.State.WAITING, mGSMPhone.getRingingCall().getState());
        assertEquals(Call.State.ACTIVE, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getBackgroundCall().getState());
        mGSMPhone.handleInCallMmiCommands("0");
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getRingingCall().getState() == Call.State.WAITING);
        assertEquals(Phone.State.OFFHOOK, mGSMPhone.getState());
        assertFalse(mGSMPhone.getRingingCall().isRinging());
        assertEquals(Call.State.DISCONNECTED, mGSMPhone.getRingingCall().getState());
        assertEquals(Call.State.ACTIVE, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getBackgroundCall().getState());
        mGSMPhone.switchHoldingAndActive();
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getBackgroundCall().getState() == Call.State.IDLE);
        assertEquals(Call.State.IDLE, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.HOLDING, mGSMPhone.getBackgroundCall().getState());
        mGSMPhone.handleInCallMmiCommands("0");
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getBackgroundCall().getState() == Call.State.HOLDING);
        assertEquals(Phone.State.IDLE, mGSMPhone.getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.DISCONNECTED, mGSMPhone.getBackgroundCall().getState());
    }
    public void testIncallMmiCallWaiting() throws Exception {
        Message msg;
        mGSMPhone.dial("+13125551212");
        do {
            mRadioControl.progressConnectingCallState();
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getForegroundCall().getState() != Call.State.ACTIVE);
        assertEquals(Call.State.ACTIVE, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getBackgroundCall().getState());
        mRadioControl.triggerRing("18005551212");
        do {
            msg = mGSMTestHandler.waitForMessage(ANY_MESSAGE);
            assertNotNull("Message Time Out", msg);
        } while (msg.what != EVENT_RINGING);
        assertEquals(Phone.State.RINGING, mGSMPhone.getState());
        assertTrue(mGSMPhone.getRingingCall().isRinging());
        assertEquals(Call.State.WAITING, mGSMPhone.getRingingCall().getState());
        assertEquals(Call.State.ACTIVE, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getBackgroundCall().getState());
        mGSMPhone.handleInCallMmiCommands("1");
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getRingingCall().getState() == Call.State.WAITING);
        assertEquals(Phone.State.OFFHOOK, mGSMPhone.getState());
        assertFalse(mGSMPhone.getRingingCall().isRinging());
        assertEquals(Call.State.IDLE, mGSMPhone.getRingingCall().getState());
        assertEquals(Call.State.ACTIVE, mGSMPhone.getForegroundCall().getState());
        assertEquals("18005551212",
                mGSMPhone.getForegroundCall().getConnections().get(0).getAddress());
        mGSMPhone.switchHoldingAndActive();
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getBackgroundCall().getState() == Call.State.IDLE);
        assertEquals(Call.State.IDLE, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.HOLDING, mGSMPhone.getBackgroundCall().getState());
        mGSMPhone.handleInCallMmiCommands("1");
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getBackgroundCall().getState() != Call.State.IDLE);
        assertEquals(Phone.State.OFFHOOK, mGSMPhone.getState());
        assertEquals(Call.State.ACTIVE, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getBackgroundCall().getState());
        assertEquals("18005551212",
                mGSMPhone.getForegroundCall().getConnections().get(0).getAddress());
        mRadioControl.triggerRing("16505550100");
        msg = mGSMTestHandler.waitForMessage(EVENT_RINGING);
        assertNotNull("Message Time Out", msg);
        assertEquals(Phone.State.RINGING, mGSMPhone.getState());
        assertTrue(mGSMPhone.getRingingCall().isRinging());
        assertEquals(Call.State.WAITING, mGSMPhone.getRingingCall().getState());
        assertEquals(Call.State.ACTIVE, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getBackgroundCall().getState());
        mGSMPhone.handleInCallMmiCommands("12");
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getForegroundCall().getState() == Call.State.ACTIVE);
        assertEquals(Phone.State.RINGING, mGSMPhone.getState());
        assertTrue(mGSMPhone.getRingingCall().isRinging());
        assertEquals(Call.State.WAITING, mGSMPhone.getRingingCall().getState());
        assertEquals(Call.State.DISCONNECTED, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getBackgroundCall().getState());
        mGSMPhone.acceptCall();
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getState() != Phone.State.OFFHOOK);
        assertEquals(Phone.State.OFFHOOK, mGSMPhone.getState());
        assertFalse(mGSMPhone.getRingingCall().isRinging());
        assertEquals(Call.State.IDLE, mGSMPhone.getRingingCall().getState());
        assertEquals(Call.State.ACTIVE, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getBackgroundCall().getState());
        mGSMPhone.dial("+13125551212");
        do {
            mRadioControl.progressConnectingCallState();
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getForegroundCall().getState() != Call.State.ACTIVE ||
                mGSMPhone.getBackgroundCall().getState() != Call.State.HOLDING);
        assertEquals(Call.State.ACTIVE, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.HOLDING, mGSMPhone.getBackgroundCall().getState());
        mGSMPhone.handleInCallMmiCommands("11");
        msg = mGSMTestHandler.waitForMessage(SUPP_SERVICE_FAILED);
        assertNotNull("Message Time Out", msg);
        assertFalse("IncallMmiCallWaiting: command should not work on holding call", msg == null);
        mGSMPhone.handleInCallMmiCommands("12");
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getForegroundCall().getState() == Call.State.ACTIVE);
        assertEquals(Call.State.DISCONNECTED, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.HOLDING, mGSMPhone.getBackgroundCall().getState());
        mGSMPhone.handleInCallMmiCommands("1");
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getBackgroundCall().getState() != Call.State.IDLE);
        assertEquals(Phone.State.OFFHOOK, mGSMPhone.getState());
        assertEquals(Call.State.ACTIVE, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getBackgroundCall().getState());
        assertEquals("16505550100",
                mGSMPhone.getForegroundCall().getConnections().get(0).getAddress());
        mGSMPhone.handleInCallMmiCommands("11");
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getForegroundCall().getState() == Call.State.ACTIVE);
        assertEquals(Call.State.DISCONNECTED, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getBackgroundCall().getState());
    }
    public void testIncallMmiCallHold() throws Exception {
        Message msg;
        mGSMPhone.dial("13125551212");
        do {
            mRadioControl.progressConnectingCallState();
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getForegroundCall().getState() != Call.State.ACTIVE);
        assertEquals(Call.State.ACTIVE, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getBackgroundCall().getState());
        mRadioControl.triggerRing("18005551212");
        msg = mGSMTestHandler.waitForMessage(EVENT_RINGING);
        assertNotNull("Message Time Out", msg);
        assertEquals(Phone.State.RINGING, mGSMPhone.getState());
        assertTrue(mGSMPhone.getRingingCall().isRinging());
        assertEquals(Call.State.WAITING, mGSMPhone.getRingingCall().getState());
        assertEquals(Call.State.ACTIVE, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getBackgroundCall().getState());
        mGSMPhone.handleInCallMmiCommands("2");
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getRingingCall().getState() == Call.State.WAITING);
        assertFalse(mGSMPhone.getRingingCall().isRinging());
        assertEquals(Phone.State.OFFHOOK, mGSMPhone.getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getRingingCall().getState());
        assertEquals(Call.State.ACTIVE,
                mGSMPhone.getForegroundCall().getState());
        assertEquals("18005551212",
                mGSMPhone.getForegroundCall().getConnections().get(0).getAddress());
        assertEquals(Call.State.HOLDING, mGSMPhone.getBackgroundCall().getState());
        assertEquals("13125551212",
                mGSMPhone.getBackgroundCall().getConnections().get(0).getAddress());
        mGSMPhone.handleInCallMmiCommands("2");
        msg = mGSMTestHandler.waitForMessage(EVENT_PHONE_STATE_CHANGED);
        assertNotNull("Message Time Out", msg);
        assertEquals(Call.State.ACTIVE, mGSMPhone.getForegroundCall().getState());
        assertEquals("13125551212",
                mGSMPhone.getForegroundCall().getConnections().get(0).getAddress());
        assertEquals(Call.State.HOLDING, mGSMPhone.getBackgroundCall().getState());
        assertEquals("18005551212",
                mGSMPhone.getBackgroundCall().getConnections().get(0).getAddress());
        mGSMPhone.conference();
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getBackgroundCall().getState() != Call.State.IDLE);
        assertEquals(Call.State.ACTIVE, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getBackgroundCall().getState());
        assertEquals(2, mGSMPhone.getForegroundCall().getConnections().size());
        mGSMPhone.handleInCallMmiCommands("23");
        msg = mGSMTestHandler.waitForMessage(SUPP_SERVICE_FAILED);
        assertNotNull("Message Time Out", msg);
        assertFalse("IncallMmiCallHold: separate should have failed!", msg == null);
        mGSMPhone.handleInCallMmiCommands("21");
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getBackgroundCall().getState() == Call.State.IDLE);
        assertEquals(Call.State.ACTIVE, mGSMPhone.getForegroundCall().getState());
        assertEquals("13125551212",
                mGSMPhone.getForegroundCall().getConnections().get(0).getAddress());
        assertEquals(Call.State.HOLDING, mGSMPhone.getBackgroundCall().getState());
        assertEquals("18005551212",
                mGSMPhone.getBackgroundCall().getConnections().get(0).getAddress());
    }
    public void testIncallMmiMultipartyServices() throws Exception {
        mGSMPhone.dial("13125551212");
        do {
            mRadioControl.progressConnectingCallState();
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getForegroundCall().getState() != Call.State.ACTIVE);
        assertEquals(Call.State.ACTIVE, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getBackgroundCall().getState());
        mGSMPhone.dial("18005551212");
        do {
            mRadioControl.progressConnectingCallState();
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getForegroundCall().getState() != Call.State.ACTIVE);
        assertEquals(Call.State.ACTIVE, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.HOLDING, mGSMPhone.getBackgroundCall().getState());
        mGSMPhone.handleInCallMmiCommands("3");
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getBackgroundCall().getState() != Call.State.IDLE);
        assertEquals(Phone.State.OFFHOOK, mGSMPhone.getState());
        assertEquals(Call.State.ACTIVE, mGSMPhone.getForegroundCall().getState());
        assertEquals("18005551212",
                mGSMPhone.getForegroundCall().getConnections().get(0).getAddress());
        assertEquals("13125551212",
                mGSMPhone.getForegroundCall().getConnections().get(1).getAddress());
        assertEquals(Call.State.IDLE, mGSMPhone.getBackgroundCall().getState());
    }
    public void testCallIndex() throws Exception {
        Message msg;
        mGSMPhone.dial("16505550100");
        do {
            mRadioControl.progressConnectingCallState();
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getForegroundCall().getState() != Call.State.ACTIVE);
        assertEquals(Call.State.ACTIVE, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getBackgroundCall().getState());
        String baseNumber = "1650555010";
        for (int i = 1; i < 6; i++) {
            String number = baseNumber + i;
            mGSMPhone.dial(number);
            do {
                mRadioControl.progressConnectingCallState();
                assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
            } while (mGSMPhone.getForegroundCall().getState() != Call.State.ACTIVE);
            assertEquals(Call.State.ACTIVE, mGSMPhone.getForegroundCall().getState());
            assertEquals(Call.State.HOLDING, mGSMPhone.getBackgroundCall().getState());
            if (mGSMPhone.getBackgroundCall().getConnections().size() >= 5) {
                break;
            }
            mGSMPhone.conference();
            do {
                assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
            } while (mGSMPhone.getBackgroundCall().getState() != Call.State.IDLE);
            assertEquals(Call.State.ACTIVE, mGSMPhone.getForegroundCall().getState());
            assertEquals(Call.State.IDLE, mGSMPhone.getBackgroundCall().getState());
        }
        assertEquals(Call.State.ACTIVE, mGSMPhone.getForegroundCall().getState());
        assertEquals("16505550105",
                mGSMPhone.getForegroundCall().getConnections().get(0).getAddress());
        assertEquals(Call.State.HOLDING, mGSMPhone.getBackgroundCall().getState());
        mRadioControl.triggerRing("18005551212");
        msg = mGSMTestHandler.waitForMessage(EVENT_RINGING);
        assertNotNull("Message Time Out", msg);
        assertEquals(Phone.State.RINGING, mGSMPhone.getState());
        assertTrue(mGSMPhone.getRingingCall().isRinging());
        assertEquals(Call.State.WAITING, mGSMPhone.getRingingCall().getState());
        assertEquals(Call.State.ACTIVE, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.HOLDING, mGSMPhone.getBackgroundCall().getState());
        mGSMPhone.getBackgroundCall().hangup();
        mGSMPhone.acceptCall();
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getRingingCall().getState() != Call.State.IDLE);
        assertEquals(Call.State.ACTIVE, mGSMPhone.getForegroundCall().getState());
        assertEquals("18005551212",
                mGSMPhone.getForegroundCall().getConnections().get(0).getAddress());
        assertEquals(Call.State.HOLDING, mGSMPhone.getBackgroundCall().getState());
        assertEquals("16505550105",
                mGSMPhone.getBackgroundCall().getConnections().get(0).getAddress());
        mGSMPhone.handleInCallMmiCommands("17");
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getForegroundCall().getState() == Call.State.ACTIVE);
        assertEquals(Call.State.DISCONNECTED, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.HOLDING, mGSMPhone.getBackgroundCall().getState());
        assertEquals("16505550105",
                mGSMPhone.getBackgroundCall().getConnections().get(0).
                        getAddress());
        mGSMPhone.handleInCallMmiCommands("1");
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getForegroundCall().getState() != Call.State.ACTIVE);
        assertEquals(Call.State.ACTIVE, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getBackgroundCall().getState());
        mGSMPhone.handleInCallMmiCommands("16");
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (mGSMPhone.getForegroundCall().getState() == Call.State.ACTIVE);
        assertEquals(Call.State.DISCONNECTED, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getBackgroundCall().getState());
    }
    public void testPostDialSequences() throws Exception {
        Message msg;
        AsyncResult ar;
        Connection cn;
        mGSMPhone.dial("+13125551212,1234;5N8xx");
        msg = mGSMTestHandler.waitForMessage(EVENT_POST_DIAL);
        assertNotNull("Message Time Out", msg);
        ar = (AsyncResult) (msg.obj);
        cn = (Connection) (ar.result);
        assertEquals(',', msg.arg1);
        assertEquals("1234;5N8", cn.getRemainingPostDialString());
        msg = mGSMTestHandler.waitForMessage(EVENT_POST_DIAL);
        assertNotNull("Message Time Out", msg);
        assertEquals('1', msg.arg1);
        ar = (AsyncResult) (msg.obj);
        assertEquals(Connection.PostDialState.STARTED, ar.userObj);
        msg = mGSMTestHandler.waitForMessage(EVENT_POST_DIAL);
        assertNotNull("Message Time Out", msg);
        assertEquals('2', msg.arg1);
        ar = (AsyncResult) (msg.obj);
        assertEquals(Connection.PostDialState.STARTED, ar.userObj);
        msg = mGSMTestHandler.waitForMessage(EVENT_POST_DIAL);
        assertNotNull("Message Time Out", msg);
        assertEquals('3', msg.arg1);
        ar = (AsyncResult) (msg.obj);
        assertEquals(Connection.PostDialState.STARTED, ar.userObj);
        msg = mGSMTestHandler.waitForMessage(EVENT_POST_DIAL);
        assertNotNull("Message Time Out", msg);
        assertEquals('4', msg.arg1);
        ar = (AsyncResult) (msg.obj);
        assertEquals(Connection.PostDialState.STARTED, ar.userObj);
        msg = mGSMTestHandler.waitForMessage(EVENT_POST_DIAL);
        assertNotNull("Message Time Out", msg);
        assertEquals(';', msg.arg1);
        ar = (AsyncResult) (msg.obj);
        cn = (Connection) (ar.result);
        assertEquals(Connection.PostDialState.WAIT, cn.getPostDialState());
        assertEquals(Connection.PostDialState.WAIT, ar.userObj);
        cn.proceedAfterWaitChar();
        msg = mGSMTestHandler.waitForMessage(EVENT_POST_DIAL);
        assertNotNull("Message Time Out", msg);
        assertEquals('5', msg.arg1);
        ar = (AsyncResult) (msg.obj);
        assertEquals(Connection.PostDialState.STARTED, ar.userObj);
        msg = mGSMTestHandler.waitForMessage(EVENT_POST_DIAL);
        assertEquals('N', msg.arg1);
        ar = (AsyncResult) (msg.obj);
        cn = (Connection) (ar.result);
        assertEquals(Connection.PostDialState.WILD, cn.getPostDialState());
        assertEquals(Connection.PostDialState.WILD, ar.userObj);
        cn.proceedAfterWildChar(",6;7");
        msg = mGSMTestHandler.waitForMessage(EVENT_POST_DIAL);
        assertNotNull("Message Time Out", msg);
        ar = (AsyncResult) (msg.obj);
        cn = (Connection) (ar.result);
        assertEquals(',', msg.arg1);
        assertEquals("6;78", cn.getRemainingPostDialString());
        msg = mGSMTestHandler.waitForMessage(EVENT_POST_DIAL);
        assertNotNull("Message Time Out", msg);
        assertEquals('6', msg.arg1);
        ar = (AsyncResult) (msg.obj);
        assertEquals(Connection.PostDialState.STARTED, ar.userObj);
        msg = mGSMTestHandler.waitForMessage(EVENT_POST_DIAL);
        assertNotNull("Message Time Out", msg);
        assertEquals(';', msg.arg1);
        ar = (AsyncResult) (msg.obj);
        cn = (Connection) (ar.result);
        assertEquals(Connection.PostDialState.WAIT, cn.getPostDialState());
        assertEquals(Connection.PostDialState.WAIT, ar.userObj);
        cn.proceedAfterWaitChar();
        msg = mGSMTestHandler.waitForMessage(EVENT_POST_DIAL);
        assertNotNull("Message Time Out", msg);
        assertEquals('7', msg.arg1);
        ar = (AsyncResult) (msg.obj);
        assertEquals(Connection.PostDialState.STARTED, ar.userObj);
        msg = mGSMTestHandler.waitForMessage(EVENT_POST_DIAL);
        assertNotNull("Message Time Out", msg);
        assertEquals('8', msg.arg1);
        ar = (AsyncResult) (msg.obj);
        assertEquals(Connection.PostDialState.STARTED, ar.userObj);
        msg = mGSMTestHandler.waitForMessage(EVENT_POST_DIAL);
        assertNotNull("Message Time Out", msg);
        assertEquals(0, msg.arg1);
        ar = (AsyncResult) (msg.obj);
        cn = (Connection) (ar.result);
        assertEquals(Connection.PostDialState.COMPLETE,
                cn.getPostDialState());
        assertEquals(Connection.PostDialState.COMPLETE, ar.userObj);
    }
    public void testPostDialCancel() throws Exception {
        Message msg;
        AsyncResult ar;
        Connection cn;
        mGSMPhone.dial("+13125551212,N");
        mRadioControl.progressConnectingToActive();
        mRadioControl.progressConnectingToActive();
        msg = mGSMTestHandler.waitForMessage(EVENT_POST_DIAL);
        assertNotNull("Message Time Out", msg);
        assertEquals(',', msg.arg1);
        msg = mGSMTestHandler.waitForMessage(EVENT_POST_DIAL);
        assertEquals('N', msg.arg1);
        ar = (AsyncResult) (msg.obj);
        cn = (Connection) (ar.result);
        assertEquals(Connection.PostDialState.WILD, cn.getPostDialState());
        cn.cancelPostDial();
        assertEquals(Connection.PostDialState.CANCELLED, cn.getPostDialState());
    }
    public void testOutgoingCallFail() throws Exception {
        Message msg;
        mRadioControl.setNextCallFailCause(CallFailCause.NORMAL_CLEARING);
        mRadioControl.setAutoProgressConnectingCall(false);
        Connection cn = mGSMPhone.dial("+13125551212");
        mRadioControl.progressConnectingCallState();
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (cn.getState() == Call.State.DIALING);
        mRadioControl.triggerHangupAll();
        msg = mGSMTestHandler.waitForMessage(EVENT_DISCONNECT);
        assertNotNull("Message Time Out", msg);
        assertEquals(Phone.State.IDLE, mGSMPhone.getState());
        assertEquals(Connection.DisconnectCause.NORMAL, cn.getDisconnectCause());
        assertEquals(0, mGSMPhone.getRingingCall().getConnections().size());
        assertEquals(1, mGSMPhone.getForegroundCall().getConnections().size());
        assertEquals(0, mGSMPhone.getBackgroundCall().getConnections().size());
        assertEquals(Call.State.IDLE, mGSMPhone.getRingingCall().getState());
        assertEquals(Call.State.DISCONNECTED, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getBackgroundCall().getState());
        assertTrue(mGSMPhone.getForegroundCall().getEarliestCreateTime() > 0);
        assertEquals(0, mGSMPhone.getForegroundCall().getEarliestConnectTime());
        mRadioControl.setNextCallFailCause(CallFailCause.USER_BUSY);
        mRadioControl.setAutoProgressConnectingCall(false);
        cn = mGSMPhone.dial("+13125551212");
        mRadioControl.progressConnectingCallState();
        do {
            assertNotNull("Message Time Out", mGSMTestHandler.waitForMessage(ANY_MESSAGE));
        } while (cn.getState() == Call.State.DIALING);
        mRadioControl.triggerHangupAll();
        msg = mGSMTestHandler.waitForMessage(EVENT_DISCONNECT);
        assertNotNull("Message Time Out", msg);
        assertEquals(Phone.State.IDLE, mGSMPhone.getState());
        assertEquals(Connection.DisconnectCause.BUSY, cn.getDisconnectCause());
        assertEquals(0, mGSMPhone.getRingingCall().getConnections().size());
        assertEquals(1, mGSMPhone.getForegroundCall().getConnections().size());
        assertEquals(0, mGSMPhone.getBackgroundCall().getConnections().size());
        assertEquals(Call.State.IDLE, mGSMPhone.getRingingCall().getState());
        assertEquals(Call.State.DISCONNECTED,
                mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getBackgroundCall().getState());
        assertTrue(mGSMPhone.getForegroundCall().getEarliestCreateTime() > 0);
        assertEquals(0, mGSMPhone.getForegroundCall().getEarliestConnectTime());
        mRadioControl.setNextCallFailCause(CallFailCause.NO_CIRCUIT_AVAIL);
        mRadioControl.setAutoProgressConnectingCall(false);
        cn = mGSMPhone.dial("+13125551212");
        mRadioControl.progressConnectingCallState();
        do {
            msg = mGSMTestHandler.waitForMessage(ANY_MESSAGE);
            assertNotNull("Message Time Out", msg);
        } while (cn.getState() == Call.State.DIALING);
        mRadioControl.triggerHangupAll();
        do {
            msg = mGSMTestHandler.waitForMessage(ANY_MESSAGE);
            assertNotNull("Message Time Out", msg);
        } while (!(msg.what == EVENT_PHONE_STATE_CHANGED
                && mGSMPhone.getState() == Phone.State.IDLE));
        assertEquals(Phone.State.IDLE, mGSMPhone.getState());
        assertEquals(Connection.DisconnectCause.CONGESTION, cn.getDisconnectCause());
        assertEquals(0, mGSMPhone.getRingingCall().getConnections().size());
        assertEquals(1, mGSMPhone.getForegroundCall().getConnections().size());
        assertEquals(0, mGSMPhone.getBackgroundCall().getConnections().size());
        assertEquals(Call.State.IDLE, mGSMPhone.getRingingCall().getState());
        assertEquals(Call.State.DISCONNECTED, mGSMPhone.getForegroundCall().getState());
        assertEquals(Call.State.IDLE, mGSMPhone.getBackgroundCall().getState());
        assertTrue(mGSMPhone.getForegroundCall().getEarliestCreateTime() > 0);
        assertEquals(0, mGSMPhone.getForegroundCall().getEarliestConnectTime());
    }
    public void testSSNotification() throws Exception {
        runTest(0, SuppServiceNotification.MO_CODE_UNCONDITIONAL_CF_ACTIVE);
        runTest(0, SuppServiceNotification.MO_CODE_CALL_IS_WAITING);
        runTest(0, SuppServiceNotification.MO_CODE_CALL_DEFLECTED);
        runTest(1, SuppServiceNotification.MT_CODE_FORWARDED_CALL);
        runTest(1, SuppServiceNotification.MT_CODE_CALL_CONNECTED_ECT);
        runTest(1, SuppServiceNotification.MT_CODE_ADDITIONAL_CALL_FORWARDED);
    }
    private void runTest(int type, int code) {
        Message msg;
        mRadioControl.triggerSsn(type, code);
        msg = mGSMTestHandler.waitForMessage(EVENT_SSN);
        assertNotNull("Message Time Out", msg);
        AsyncResult ar = (AsyncResult) msg.obj;
        assertNull(ar.exception);
        SuppServiceNotification notification =
                (SuppServiceNotification) ar.result;
        assertEquals(type, notification.notificationType);
        assertEquals(code, notification.code);
    }
    public void testUssd() throws Exception {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
        }
        verifyNormal();
        verifyCancel();
        varifyNetworkInitiated();
    }
    private void varifyNetworkInitiated() {
        Message msg;
        AsyncResult ar;
        MmiCode mmi;
        mRadioControl.triggerIncomingUssd("0", "NOTIFY message");
        msg = mGSMTestHandler.waitForMessage(EVENT_MMI_COMPLETE);
        assertNotNull("Message Time Out", msg);
        ar = (AsyncResult) msg.obj;
        mmi = (MmiCode) ar.result;
        assertFalse(mmi.isUssdRequest());
        mRadioControl.triggerIncomingUssd("1", "REQUEST Message");
        msg = mGSMTestHandler.waitForMessage(EVENT_MMI_COMPLETE);
        assertNotNull("Message Time Out", msg);
        ar = (AsyncResult) msg.obj;
        mmi = (MmiCode) ar.result;
        assertTrue(mmi.isUssdRequest());
        mGSMPhone.sendUssdResponse("## TEST: TEST_GSMPhone responding...");
        msg = mGSMTestHandler.waitForMessage(EVENT_MMI_INITIATE);
        assertNotNull("Message Time Out", msg);
        ar = (AsyncResult) msg.obj;
        mmi = (MmiCode) ar.result;
        GsmMmiCode gsmMmi = (GsmMmiCode) mmi;
        assertTrue(gsmMmi.isPendingUSSD());
        msg = mGSMTestHandler.waitForMessage(EVENT_MMI_COMPLETE);
        assertNotNull("Message Time Out", msg);
        ar = (AsyncResult) msg.obj;
        mmi = (MmiCode) ar.result;
        assertNull(ar.exception);
        assertFalse(mmi.isUssdRequest());
        mRadioControl.triggerIncomingUssd("1", "REQUEST Message");
        msg = mGSMTestHandler.waitForMessage(EVENT_MMI_COMPLETE);
        assertNotNull("Message Time Out", msg);
        ar = (AsyncResult) msg.obj;
        mmi = (MmiCode) ar.result;
        assertTrue(mmi.isUssdRequest());
        mmi.cancel();
        msg = mGSMTestHandler.waitForMessage(EVENT_MMI_COMPLETE);
        assertNotNull("Message Time Out", msg);
        ar = (AsyncResult) msg.obj;
        mmi = (MmiCode) ar.result;
        assertNull(ar.exception);
        assertEquals(MmiCode.State.CANCELLED, mmi.getState());
        List mmiList = mGSMPhone.getPendingMmiCodes();
        assertEquals(0, mmiList.size());
    }
    private void verifyNormal() throws CallStateException {
        Message msg;
        AsyncResult ar;
        MmiCode mmi;
        mGSMPhone.dial("#646#");
        msg = mGSMTestHandler.waitForMessage(EVENT_MMI_INITIATE);
        assertNotNull("Message Time Out", msg);
        msg = mGSMTestHandler.waitForMessage(EVENT_MMI_COMPLETE);
        assertNotNull("Message Time Out", msg);
        ar = (AsyncResult) msg.obj;
        mmi = (MmiCode) ar.result;
        assertEquals(MmiCode.State.COMPLETE, mmi.getState());
    }
    private void verifyCancel() throws CallStateException {
        Message msg;
        mGSMPhone.dial("#646#");
        List<? extends MmiCode> pendingMmis = mGSMPhone.getPendingMmiCodes();
        assertEquals(1, pendingMmis.size());
        MmiCode mmi = pendingMmis.get(0);
        assertTrue(mmi.isCancelable());
        mmi.cancel();
        msg = mGSMTestHandler.waitForMessage(EVENT_MMI_INITIATE);
        assertNotNull("Message Time Out", msg);
        msg = mGSMTestHandler.waitForMessage(EVENT_MMI_COMPLETE);
        assertNotNull("Message Time Out", msg);
        AsyncResult ar = (AsyncResult) msg.obj;
        mmi = (MmiCode) ar.result;
        assertEquals(MmiCode.State.CANCELLED, mmi.getState());
    }
    public void testRilHooks() throws Exception {
        Message msg;
        AsyncResult ar;
        mGSMPhone.invokeOemRilRequestRaw(null, mHandler.obtainMessage(EVENT_OEM_RIL_MESSAGE));
        msg = mGSMTestHandler.waitForMessage(EVENT_OEM_RIL_MESSAGE);
        assertNotNull("Message Time Out", msg);
        ar = ((AsyncResult) msg.obj);
        assertNull(ar.result);
        assertNull(ar.exception);
        mGSMPhone.invokeOemRilRequestRaw(new byte[0], mHandler.obtainMessage(EVENT_OEM_RIL_MESSAGE));
        msg = mGSMTestHandler.waitForMessage(EVENT_OEM_RIL_MESSAGE);
        assertNotNull("Message Time Out", msg);
        ar = ((AsyncResult) msg.obj);
        assertEquals(0, ((byte[]) (ar.result)).length);
        assertNull(ar.exception);
        mGSMPhone.invokeOemRilRequestRaw("Hello".getBytes("utf-8"),
                mHandler.obtainMessage(EVENT_OEM_RIL_MESSAGE));
        msg = mGSMTestHandler.waitForMessage(EVENT_OEM_RIL_MESSAGE);
        assertNotNull("Message Time Out", msg);
        ar = ((AsyncResult) msg.obj);
        assertEquals("Hello", new String(((byte[]) (ar.result)), "utf-8"));
        assertNull(ar.exception);
        mGSMPhone.invokeOemRilRequestStrings(null, mHandler.obtainMessage(EVENT_OEM_RIL_MESSAGE));
        msg = mGSMTestHandler.waitForMessage(EVENT_OEM_RIL_MESSAGE);
        assertNotNull("Message Time Out", msg);
        ar = ((AsyncResult) msg.obj);
        assertNull(ar.result);
        assertNull(ar.exception);
        mGSMPhone.invokeOemRilRequestStrings(new String[0],
                mHandler.obtainMessage(EVENT_OEM_RIL_MESSAGE));
        msg = mGSMTestHandler.waitForMessage(EVENT_OEM_RIL_MESSAGE);
        assertNotNull("Message Time Out", msg);
        ar = ((AsyncResult) msg.obj);
        assertEquals(0, ((String[]) (ar.result)).length);
        assertNull(ar.exception);
        String s[] = new String[1];
        s[0] = "Hello";
        mGSMPhone.invokeOemRilRequestStrings(s, mHandler.obtainMessage(EVENT_OEM_RIL_MESSAGE));
        msg = mGSMTestHandler.waitForMessage(EVENT_OEM_RIL_MESSAGE);
        assertNotNull("Message Time Out", msg);
        ar = ((AsyncResult) msg.obj);
        assertEquals("Hello", ((String[]) (ar.result))[0]);
        assertEquals(1, ((String[]) (ar.result)).length);
        assertNull(ar.exception);
    }
    public void testMmi() throws Exception {
        mRadioControl.setAutoProgressConnectingCall(false);
        runValidMmi("*#67#", false);
        runValidMmi("##43*11#", false);
        runValidMmi("#33*1234*11#", false);
        runValidMmi("*21*6505551234**5#", false);
        runValidMmi("**03**1234*4321*4321#", false);
        runValidMmi("5308234092307540923#", true);
        runValidMmi("22", true);
        runValidMmiWithConnect("*31#6505551234");
        runNotMmi("6505551234");
        runNotMmi("1234#*12#34566654");
        runNotMmi("*#*#12#*");
    }
    private void runValidMmi(String dialString, boolean cancelable) throws CallStateException {
        Connection c = mGSMPhone.dial(dialString);
        assertNull(c);
        Message msg = mGSMTestHandler.waitForMessage(EVENT_MMI_INITIATE);
        assertNotNull("Message Time Out", msg);
        AsyncResult ar = (AsyncResult) msg.obj;
        MmiCode mmi = (MmiCode) ar.result;
        assertEquals(cancelable, mmi.isCancelable());
        msg = mGSMTestHandler.waitForMessage(EVENT_MMI_COMPLETE);
        assertNotNull("Message Time Out", msg);
    }
    private void runValidMmiWithConnect(String dialString) throws CallStateException {
        mRadioControl.pauseResponses();
        Connection c = mGSMPhone.dial(dialString);
        assertNotNull(c);
        hangup(c);
    }
    private void hangup(Connection cn) throws CallStateException {
        cn.hangup();
        mRadioControl.resumeResponses();
        assertNotNull(mGSMTestHandler.waitForMessage(EVENT_DISCONNECT));
    }
    private void runNotMmi(String dialString) throws CallStateException {
        mRadioControl.pauseResponses();
        Connection c = mGSMPhone.dial(dialString);
        assertNotNull(c);
        hangup(c);
    }
}
