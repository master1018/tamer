public class StateListDrawableTest extends TestCase {
    private StateListDrawable slDrawable;
    private MockDrawable mockFocusedDrawable;
    private MockDrawable mockCheckedDrawable;
    private MockView mockView;
    private MockDrawable mockDefaultDrawable;
    public void broken_testFocusScenarioSetStringWildcardFirst() throws Exception {
        int focusedStateSet[] = {R.attr.state_focused};
        int checkedStateSet[] = {R.attr.state_checked};
        slDrawable.addState(StateSet.WILD_CARD,
                               mockDefaultDrawable);
        slDrawable.addState(checkedStateSet, mockCheckedDrawable);
        slDrawable.addState(focusedStateSet, mockFocusedDrawable);
        mockView.requestFocus();
        mockView.getBackground().draw(null);
        assertTrue(mockDefaultDrawable.wasDrawn);
    }
    public void broken_testFocusScenarioStateSetWildcardLast() throws Exception {
        int focusedStateSet[] = {R.attr.state_focused};
        int checkedStateSet[] = {R.attr.state_checked};
        slDrawable.addState(checkedStateSet, mockCheckedDrawable);
        slDrawable.addState(focusedStateSet, mockFocusedDrawable);
        slDrawable.addState(StateSet.WILD_CARD,
                               mockDefaultDrawable);
        mockView.requestFocus();
        mockView.getBackground().draw(null);
        assertTrue(mockFocusedDrawable.wasDrawn);
    }
    protected void setUp() throws Exception {
        super.setUp();
        slDrawable = new StateListDrawable();
        mockFocusedDrawable = new MockDrawable();
        mockCheckedDrawable = new MockDrawable();
        mockDefaultDrawable = new MockDrawable();
        mockView = new MockView();
        mockView.setBackgroundDrawable(slDrawable);
    }
    static class MockDrawable extends Drawable {
        public boolean wasDrawn = false;
        public void draw(Canvas canvas) {
            wasDrawn = true;
        }
        public void setAlpha(int alpha) {
        }
        public void setColorFilter(ColorFilter cf) {
        }
        public int getOpacity() {
            return android.graphics.PixelFormat.UNKNOWN;
        }
    }
}
