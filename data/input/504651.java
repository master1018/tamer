public class Focus2AndroidTest extends AndroidTestCase {
    private FocusFinder mFocusFinder;
    private ViewGroup mRoot;
    private Button mLeftButton;
    private Button mCenterButton;
    private Button mRightButton;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mFocusFinder = FocusFinder.getInstance();
        final Context context = getContext();
        final LayoutInflater inflater = LayoutInflater.from(context);
        mRoot = (ViewGroup) inflater.inflate(R.layout.focus_2, null);
        mRoot.measure(500, 500);
        mRoot.layout(0, 0, 500, 500);
        mLeftButton = (Button) mRoot.findViewById(R.id.leftButton);
        mCenterButton = (Button) mRoot.findViewById(R.id.centerButton);
        mRightButton = (Button) mRoot.findViewById(R.id.rightButton);
    }
    @SmallTest
    public void testPreconditions() {
        assertNotNull(mLeftButton);
        assertTrue("center button should be right of left button",
                mLeftButton.getRight() < mCenterButton.getLeft());
        assertTrue("right button should be right of center button",
                mCenterButton.getRight() < mRightButton.getLeft());
    }
    @SmallTest
    public void testGoingRightFromLeftButtonJumpsOverCenterToRight() {
        assertEquals("right should be next focus from left",
                mRightButton,
                mFocusFinder.findNextFocus(mRoot, mLeftButton, View.FOCUS_RIGHT));
    }
    @SmallTest
    public void testGoingLeftFromRightButtonGoesToCenter() {
        assertEquals("center should be next focus from right",
                mCenterButton,
                mFocusFinder.findNextFocus(mRoot, mRightButton, View.FOCUS_LEFT));
    }
}
