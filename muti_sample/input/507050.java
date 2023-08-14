public class VisibilityTest extends ActivityInstrumentationTestCase<Visibility> {
    private TextView mRefUp;
    private TextView mRefDown;
    private TextView mVictim;
    private Button mVisible;
    private Button mInvisible;
    private Button mGone;
    public VisibilityTest() {
        super("com.android.frameworks.coretests", Visibility.class);
    }
    @Override
    public void setUp() throws Exception {
        super.setUp();
        final Visibility a = getActivity();
        mRefUp = (TextView) a.findViewById(R.id.refUp);
        mRefDown = (TextView) a.findViewById(R.id.refDown);
        mVictim = (TextView) a.findViewById(R.id.victim);
        mVisible = (Button) a.findViewById(R.id.vis);
        mInvisible = (Button) a.findViewById(R.id.invis);
        mGone = (Button) a.findViewById(R.id.gone);
    }
    @MediumTest
    public void testSetUpConditions() throws Exception {
        assertNotNull(mRefUp);
        assertNotNull(mRefDown);
        assertNotNull(mVictim);
        assertNotNull(mVisible);
        assertNotNull(mInvisible);
        assertNotNull(mGone);
        assertTrue(mVisible.hasFocus());
    }
    @MediumTest
    public void testVisibleToInvisible() throws Exception {
        sendKeys("DPAD_RIGHT");
        assertTrue(mInvisible.hasFocus());
        int oldTop = mVictim.getTop();
        sendKeys("DPAD_CENTER");
        assertEquals(View.INVISIBLE, mVictim.getVisibility());
        int newTop = mVictim.getTop();
        assertEquals(oldTop, newTop);
    }
    @MediumTest
    public void testVisibleToGone() throws Exception {
        sendRepeatedKeys(2, KEYCODE_DPAD_RIGHT);
        assertTrue(mGone.hasFocus());
        int oldTop = mVictim.getTop();
        sendKeys("DPAD_CENTER");
        assertEquals(View.GONE, mVictim.getVisibility());
        int refDownTop = mRefDown.getTop();
        assertEquals(oldTop, refDownTop);
    }
    @LargeTest
    public void testGoneToVisible() throws Exception {
        sendKeys("2*DPAD_RIGHT");
        assertTrue(mGone.hasFocus());
        int oldTop = mVictim.getTop();
        sendKeys("DPAD_CENTER");
        assertEquals(View.GONE, mVictim.getVisibility());
        int refDownTop = mRefDown.getTop();
        assertEquals(oldTop, refDownTop);
        sendKeys("2*DPAD_LEFT DPAD_CENTER");
        assertEquals(View.VISIBLE, mVictim.getVisibility());
        int newTop = mVictim.getTop();
        assertEquals(oldTop, newTop);
    }
    @MediumTest
    public void testGoneToInvisible() throws Exception {
        sendKeys("2*DPAD_RIGHT");
        assertTrue(mGone.hasFocus());
        int oldTop = mVictim.getTop();
        sendKeys("DPAD_CENTER");
        assertEquals(View.GONE, mVictim.getVisibility());
        int refDownTop = mRefDown.getTop();
        assertEquals(oldTop, refDownTop);
        sendKeys(KEYCODE_DPAD_LEFT, KEYCODE_DPAD_CENTER);
        assertEquals(View.INVISIBLE, mVictim.getVisibility());
        int newTop = mVictim.getTop();
        assertEquals(oldTop, newTop);
    }
    @MediumTest
    public void testInvisibleToVisible() throws Exception {
        sendKeys("DPAD_RIGHT");
        assertTrue(mInvisible.hasFocus());
        int oldTop = mVictim.getTop();
        sendKeys("DPAD_CENTER");
        assertEquals(View.INVISIBLE, mVictim.getVisibility());
        int newTop = mVictim.getTop();
        assertEquals(oldTop, newTop);
        sendKeys("DPAD_LEFT DPAD_CENTER");
        assertEquals(View.VISIBLE, mVictim.getVisibility());
        newTop = mVictim.getTop();
        assertEquals(oldTop, newTop);
    }
    @MediumTest
    public void testInvisibleToGone() throws Exception {
        sendKeys("DPAD_RIGHT");        
        assertTrue(mInvisible.hasFocus());
        int oldTop = mVictim.getTop();
        sendKeys("DPAD_CENTER");
        assertEquals(View.INVISIBLE, mVictim.getVisibility());
        int newTop = mVictim.getTop();
        assertEquals(oldTop, newTop);
        sendKeys("DPAD_RIGHT DPAD_CENTER");
        assertEquals(View.GONE, mVictim.getVisibility());
        int refDownTop = mRefDown.getTop();
        assertEquals(oldTop, refDownTop);
    }
}
