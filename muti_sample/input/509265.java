@TestTargetClass(CompoundButton.class)
public class CompoundButtonTest extends AndroidTestCase {
    private Resources mResources;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mResources = mContext.getResources();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link CompoundButton}",
            method = "CompoundButton",
            args = {android.content.Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link CompoundButton}",
            method = "CompoundButton",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link CompoundButton}",
            method = "CompoundButton",
            args = {android.content.Context.class, android.util.AttributeSet.class, int.class}
        )
    })
    @ToBeFixed(bug="1417734", explanation="should add @throws clause into javadoc of " +
            "CompoundButton's constructors when the input AttributeSet or Context is null")
    public void testConstructor() {
        XmlPullParser parser = mContext.getResources().getXml(R.layout.togglebutton_layout);
        AttributeSet mAttrSet = Xml.asAttributeSet(parser);
        new MockCompoundButton(mContext, mAttrSet, 0);
        new MockCompoundButton(mContext, mAttrSet);
        new MockCompoundButton(mContext);
        try {
            new MockCompoundButton(null, null, -1);
            fail("Should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
        try {
            new MockCompoundButton(null, null);
            fail("Should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
        try {
            new MockCompoundButton(null);
            fail("Should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setChecked",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isChecked",
            args = {}
        )
    })
    public void testAccessChecked() {
        CompoundButton compoundButton = new MockCompoundButton(mContext);
        MockOnCheckedChangeListener listener = new MockOnCheckedChangeListener();
        compoundButton.setOnCheckedChangeListener(listener);
        assertFalse(compoundButton.isChecked());
        assertFalse(listener.hasCalledCheckedChange());
        compoundButton.setChecked(true);
        assertTrue(compoundButton.isChecked());
        assertTrue(listener.hasCalledCheckedChange());
        assertSame(compoundButton, listener.getInputCompoundButton());
        assertTrue(listener.getInputChecked());
        listener.reset();
        compoundButton.setChecked(true);
        assertTrue(compoundButton.isChecked());
        assertFalse(listener.hasCalledCheckedChange());
        compoundButton.setChecked(false);
        assertFalse(compoundButton.isChecked());
        assertTrue(listener.hasCalledCheckedChange());
        assertSame(compoundButton, listener.getInputCompoundButton());
        assertFalse(listener.getInputChecked());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link CompoundButton#setOnCheckedChangeListener(OnCheckedChangeListener)}",
        method = "setOnCheckedChangeListener",
        args = {android.widget.CompoundButton.OnCheckedChangeListener.class}
    )
    public void testSetOnCheckedChangeListener() {
        CompoundButton compoundButton = new MockCompoundButton(mContext);
        MockOnCheckedChangeListener listener = new MockOnCheckedChangeListener();
        compoundButton.setOnCheckedChangeListener(listener);
        assertFalse(compoundButton.isChecked());
        assertFalse(listener.hasCalledCheckedChange());
        compoundButton.setChecked(true);
        assertTrue(listener.hasCalledCheckedChange());
        compoundButton.setOnCheckedChangeListener(null);
        listener.reset();
        compoundButton.setChecked(false);
        assertFalse(listener.hasCalledCheckedChange());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link CompoundButton#toggle()}",
        method = "toggle",
        args = {}
    )
    public void testToggle() {
        CompoundButton compoundButton = new MockCompoundButton(mContext);
        assertFalse(compoundButton.isChecked());
        compoundButton.toggle();
        assertTrue(compoundButton.isChecked());
        compoundButton.toggle();
        assertFalse(compoundButton.isChecked());
        compoundButton.setChecked(true);
        compoundButton.toggle();
        assertFalse(compoundButton.isChecked());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link CompoundButton#performClick()}",
        method = "performClick",
        args = {}
    )
    public void testPerformClick() {
        CompoundButton compoundButton = new MockCompoundButton(mContext);
        assertFalse(compoundButton.isChecked());
        assertFalse(compoundButton.performClick());
        assertTrue(compoundButton.isChecked());
        assertFalse(compoundButton.performClick());
        assertFalse(compoundButton.isChecked());
        compoundButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            }
        });
        assertTrue(compoundButton.performClick());
        assertTrue(compoundButton.isChecked());
        assertTrue(compoundButton.performClick());
        assertFalse(compoundButton.isChecked());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link CompoundButton#drawableStateChanged()}",
        method = "drawableStateChanged",
        args = {}
    )
    public void testDrawableStateChanged() {
        MockCompoundButton compoundButton = new MockCompoundButton(mContext);
        assertFalse(compoundButton.isChecked());
        compoundButton.drawableStateChanged();
        Drawable drawable = mResources.getDrawable(R.drawable.scenery);
        compoundButton.setButtonDrawable(drawable);
        drawable.setState(null);
        assertNull(drawable.getState());
        compoundButton.drawableStateChanged();
        assertNotNull(drawable.getState());
        assertSame(compoundButton.getDrawableState(), drawable.getState());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link CompoundButton#setButtonDrawable(Drawable)}",
        method = "setButtonDrawable",
        args = {android.graphics.drawable.Drawable.class}
    )
    public void testSetButtonDrawableByDrawable() {
        CompoundButton compoundButton;
        compoundButton = new MockCompoundButton(mContext);
        compoundButton.setButtonDrawable(null);
        compoundButton = new MockCompoundButton(mContext);
        compoundButton.setVisibility(View.GONE);
        Drawable firstDrawable = mResources.getDrawable(R.drawable.scenery);
        firstDrawable.setVisible(true, false);
        assertEquals(StateSet.WILD_CARD, firstDrawable.getState());
        compoundButton.setButtonDrawable(firstDrawable);
        assertFalse(firstDrawable.isVisible());
        compoundButton.setVisibility(View.VISIBLE);
        Drawable secondDrawable = mResources.getDrawable(R.drawable.pass);
        secondDrawable.setVisible(true, false);
        assertEquals(StateSet.WILD_CARD, secondDrawable.getState());
        compoundButton.setButtonDrawable(secondDrawable);
        assertTrue(secondDrawable.isVisible());
        assertFalse(firstDrawable.isVisible());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link CompoundButton#setButtonDrawable(int)}",
        method = "setButtonDrawable",
        args = {int.class}
    )
    @ToBeFixed(bug = "1386429", explanation = "we can not check the drawable which is set" +
            " by id. we need getter method to complete this test case.")
    public void testSetButtonDrawableById() {
        CompoundButton compoundButton;
        compoundButton = new MockCompoundButton(mContext);
        compoundButton.setButtonDrawable(0);
        compoundButton = new MockCompoundButton(mContext);
        compoundButton.setButtonDrawable(R.drawable.scenery);
        compoundButton.setButtonDrawable(R.drawable.scenery);
        compoundButton.setButtonDrawable(R.drawable.pass);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link CompoundButton#onCreateDrawableState(int)}",
        method = "onCreateDrawableState",
        args = {int.class}
    )
    public void testOnCreateDrawableState() {
        MockCompoundButton compoundButton;
        compoundButton = new MockCompoundButton(mContext);
        int[] state = compoundButton.onCreateDrawableState(0);
        assertEquals(0, state[state.length - 1]);
        compoundButton.setChecked(true);
        int[] checkedState = compoundButton.onCreateDrawableState(0);
        assertEquals(2, checkedState.length);
        assertEquals(state[0], checkedState[0]);
        assertEquals(com.android.internal.R.attr.state_checked, checkedState[1]);
        compoundButton.setChecked(false);
        state = compoundButton.onCreateDrawableState(0);
        assertEquals(0, state[state.length - 1]);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link CompoundButton#onDraw(Canvas)}",
        method = "onDraw",
        args = {android.graphics.Canvas.class}
    )
    public void testOnDraw() {
        int viewHeight;
        int drawableWidth;
        int drawableHeight;
        Rect bounds;
        Drawable drawable;
        Canvas canvas = new Canvas();
        MockCompoundButton compoundButton;
        compoundButton = new MockCompoundButton(mContext);
        compoundButton.onDraw(canvas);
        compoundButton = new MockCompoundButton(mContext);
        drawable = mResources.getDrawable(R.drawable.scenery);
        compoundButton.setButtonDrawable(drawable);
        viewHeight = compoundButton.getHeight();
        drawableWidth = drawable.getIntrinsicWidth();
        drawableHeight = drawable.getIntrinsicHeight();
        compoundButton.onDraw(canvas);
        bounds = drawable.copyBounds();
        assertEquals(0, bounds.left);
        assertEquals(drawableWidth, bounds.right);
        assertEquals(0, bounds.top);
        assertEquals(drawableHeight, bounds.bottom);
        compoundButton.setGravity(Gravity.BOTTOM);
        compoundButton.onDraw(canvas);
        bounds = drawable.copyBounds();
        assertEquals(0, bounds.left);
        assertEquals(drawableWidth, bounds.right);
        assertEquals(viewHeight - drawableHeight, bounds.top);
        assertEquals(viewHeight, bounds.bottom);
        compoundButton.setGravity(Gravity.CENTER_VERTICAL);
        compoundButton.onDraw(canvas);
        bounds = drawable.copyBounds();
        assertEquals(0, bounds.left);
        assertEquals(drawableWidth, bounds.right);
        assertEquals( (viewHeight - drawableHeight) / 2, bounds.top);
        assertEquals( (viewHeight - drawableHeight) / 2 + drawableHeight, bounds.bottom);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onSaveInstanceState",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onRestoreInstanceState",
            args = {android.os.Parcelable.class}
        )
    })
    public void testAccessInstanceState() {
        CompoundButton compoundButton = new MockCompoundButton(mContext);
        Parcelable state;
        assertFalse(compoundButton.isChecked());
        assertFalse(compoundButton.getFreezesText());
        state = compoundButton.onSaveInstanceState();
        assertNotNull(state);
        assertTrue(compoundButton.getFreezesText());
        compoundButton.setChecked(true);
        compoundButton.onRestoreInstanceState(state);
        assertFalse(compoundButton.isChecked());
        assertTrue(compoundButton.isLayoutRequested());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "verifyDrawable",
        args = {android.graphics.drawable.Drawable.class}
    )
    public void testVerifyDrawable() {
        MockCompoundButton compoundButton = new MockCompoundButton(mContext);
        Drawable drawable = mContext.getResources().getDrawable(R.drawable.scenery);
        assertTrue(compoundButton.verifyDrawable(null));
        assertFalse(compoundButton.verifyDrawable(drawable));
        compoundButton.setButtonDrawable(drawable);
        assertTrue(compoundButton.verifyDrawable(null));
        assertTrue(compoundButton.verifyDrawable(drawable));
    }
    private final class MockCompoundButton extends CompoundButton {
        public MockCompoundButton(Context context) {
            super(context);
        }
        public MockCompoundButton(Context context, AttributeSet attrs) {
            super(context, attrs, 0);
        }
        public MockCompoundButton(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }
        @Override
        protected void drawableStateChanged() {
            super.drawableStateChanged();
        }
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
        }
        @Override
        protected int[] onCreateDrawableState(int extraSpace) {
            return super.onCreateDrawableState(extraSpace);
        }
        @Override
        protected boolean verifyDrawable(Drawable who) {
            return super.verifyDrawable(who);
        }
    }
    private final class MockOnCheckedChangeListener implements OnCheckedChangeListener {
        private boolean mHasCalledChecked;
        private CompoundButton mCompoundButton;
        private boolean mIsChecked;
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            mHasCalledChecked = true;
            mCompoundButton = buttonView;
            mIsChecked = isChecked;
        }
        public boolean getInputChecked() {
            return mIsChecked;
        }
        public CompoundButton getInputCompoundButton() {
            return mCompoundButton;
        }
        public boolean hasCalledCheckedChange() {
            return mHasCalledChecked;
        }
        public void reset() {
            mHasCalledChecked = false;
            mCompoundButton = null;
            mIsChecked = false;
        }
    }
}
