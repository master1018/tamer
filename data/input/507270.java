public class LinearLayoutGridTest extends SingleLaunchActivityTestCase<LinearLayoutGrid> {
    private ViewGroup mRootView;
    public LinearLayoutGridTest() {
        super("com.android.frameworks.coretests", LinearLayoutGrid.class);
    }
    protected void setUp() throws Exception {
        super.setUp();
        mRootView = getActivity().getRootView();
    }
    @MediumTest
    public void testGoDownFromMiddle() {
        assertEquals(getActivity().getButtonAt(2, 1),
                FocusFinder.getInstance().findNextFocus(
                        mRootView,
                        getActivity().getButtonAt(1, 1),
                        View.FOCUS_DOWN));
    }
    @MediumTest
    public void testGoUpFromMiddle() {
        assertEquals(getActivity().getButtonAt(0, 1),
                FocusFinder.getInstance().findNextFocus(
                        mRootView,
                        getActivity().getButtonAt(1, 1),
                        View.FOCUS_UP));
    }
    @MediumTest
    public void testGoRightFromMiddle() {
        assertEquals(getActivity().getButtonAt(1, 2),
                FocusFinder.getInstance().findNextFocus(
                        mRootView,
                        getActivity().getButtonAt(1, 1),
                        View.FOCUS_RIGHT));
    }
    @MediumTest
    public void testGoLeftFromMiddle() {
        assertEquals(getActivity().getButtonAt(1, 0),
                FocusFinder.getInstance().findNextFocus(
                        mRootView,
                        getActivity().getButtonAt(1, 1),
                        View.FOCUS_LEFT));
    }
}
