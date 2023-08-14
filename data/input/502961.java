@TestTargetClass(LinearLayout.class)
public class LinearLayoutTest extends ActivityInstrumentationTestCase<LinearLayoutStubActivity> {
    private Context mContext;
    private Activity mActivity;
    public LinearLayoutTest() {
        super("com.android.cts.stub", LinearLayoutStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mContext = getInstrumentation().getTargetContext();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link LinearLayout}",
            method = "LinearLayout",
            args = {android.content.Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link LinearLayout}",
            method = "LinearLayout",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        )
    })
    @ToBeFixed(bug="1417734", explanation="LinearLayout#LinearLayout(Context, AttributeSet)" +
            " should check whether the input Context is null")
    public void testConstructor() {
        new LinearLayout(mContext);
        new LinearLayout(mContext, null);
        XmlPullParser parser = mContext.getResources().getXml(R.layout.linearlayout_layout);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        new LinearLayout(mContext, attrs);
        try {
            new LinearLayout(null, null);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isBaselineAligned",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setBaselineAligned",
            args = {boolean.class}
        )
    })
    public void testAccessBaselineAligned() {
        LinearLayout linearLayout = new LinearLayout(mContext);
        linearLayout.setBaselineAligned(true);
        assertTrue(linearLayout.isBaselineAligned());
        linearLayout.setBaselineAligned(false);
        assertFalse(linearLayout.isBaselineAligned());
        linearLayout = (LinearLayout) mActivity.findViewById(R.id.weightsum);
        assertFalse(linearLayout.isBaselineAligned());
        linearLayout = (LinearLayout) mActivity.findViewById(R.id.horizontal);
        assertTrue(linearLayout.isBaselineAligned());
        linearLayout = (LinearLayout) mActivity.findViewById(R.id.vertical);
        assertTrue(linearLayout.isBaselineAligned());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link LinearLayout#getBaseline()}",
        method = "getBaseline",
        args = {}
    )
    public void testGetBaseline() {
        LinearLayout linearLayout = new LinearLayout(mContext);
        ListView lv1 = new ListView(mContext);
        linearLayout.addView(lv1);
        assertEquals(-1, linearLayout.getBaseline());
        ListView lv2 = new ListView(mContext);
        linearLayout.addView(lv2);
        linearLayout.setBaselineAlignedChildIndex(1);
        try {
            linearLayout.getBaseline();
            fail("LinearLayout.getBaseline() should throw exception here.");
        } catch (RuntimeException e) {
        }
        MockListView lv3 = new MockListView(mContext);
        linearLayout.addView(lv3);
        linearLayout.setBaselineAlignedChildIndex(2);
        assertEquals(lv3.getBaseline(), linearLayout.getBaseline());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getBaselineAlignedChildIndex",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setBaselineAlignedChildIndex",
            args = {int.class}
        )
    })
    public void testAccessBaselineAlignedChildIndex() {
        LinearLayout linearLayout = new LinearLayout(mContext);
        ListView lv1 = new ListView(mContext);
        ListView lv2 = new ListView(mContext);
        ListView lv3 = new ListView(mContext);
        linearLayout.addView(lv1);
        linearLayout.addView(lv2);
        linearLayout.addView(lv3);
        linearLayout.setBaselineAlignedChildIndex(1);
        assertEquals(1, linearLayout.getBaselineAlignedChildIndex());
        linearLayout.setBaselineAlignedChildIndex(2);
        assertEquals(2, linearLayout.getBaselineAlignedChildIndex());
        try {
            linearLayout.setBaselineAlignedChildIndex(-1);
            fail("LinearLayout should throw IllegalArgumentException here.");
        } catch (IllegalArgumentException e) {
        }
        try {
            linearLayout.setBaselineAlignedChildIndex(3);
            fail("LinearLayout should throw IllegalArgumentException here.");
        } catch (IllegalArgumentException e) {
        }
        linearLayout = (LinearLayout) mActivity.findViewById(R.id.baseline_aligned_child_index);
        assertEquals(1, linearLayout.getBaselineAlignedChildIndex());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setWeightSum",
            args = {float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getWeightSum",
            args = {}
        )
    })
    public void testAccessWeightSum() {
        LinearLayout parent = (LinearLayout) mActivity.findViewById(R.id.weightsum);
        TextView weight02 = (TextView) mActivity.findViewById(R.id.weight_0_2);
        TextView weight05 = (TextView) mActivity.findViewById(R.id.weight_0_5);
        TextView weight03 = (TextView) mActivity.findViewById(R.id.weight_0_3);
        assertNotNull(parent);
        assertNotNull(weight02);
        assertNotNull(weight05);
        assertNotNull(weight03);
        assertEquals(mContext.getResources().getString(R.string.horizontal_text_1),
                weight02.getText().toString());
        assertEquals(mContext.getResources().getString(R.string.horizontal_text_2),
                weight05.getText().toString());
        assertEquals(mContext.getResources().getString(R.string.horizontal_text_3),
                weight03.getText().toString());
        assertEquals(LinearLayout.HORIZONTAL, parent.getOrientation());
        assertEquals(1.0f, parent.getWeightSum());
        int parentWidth = parent.getWidth();
        assertEquals(parentWidth * 0.2, (float) weight02.getWidth(), 1.0);
        assertEquals(parentWidth * 0.5, (float) weight05.getWidth(), 1.0);
        assertEquals(parentWidth * 0.3, (float) weight03.getWidth(), 1.0);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "generateLayoutParams",
            args = {android.util.AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "generateLayoutParams",
            args = {android.view.ViewGroup.LayoutParams.class}
        )
    })
    @ToBeFixed(bug="1417734", explanation="generateLayoutParams(AttributeSet)" +
            " will throw a RuntimeException:" +
            " Binary XML file line #-1: You must supply a layout_width attribute." +
            " But 'layout_width' attribute have been assigned to be 'match_parent'.")
    public void testGenerateLayoutParams() {
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(320, 240);
        MockLinearLayout mockLinearLayout = new MockLinearLayout(mContext);
        LayoutParams layoutParams1 = mockLinearLayout.generateLayoutParams(lp);
        assertEquals(320, layoutParams1.width);
        assertEquals(240, layoutParams1.height);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link LinearLayout#checkLayoutParams(ViewGroup.LayoutParams)}",
        method = "checkLayoutParams",
        args = {android.view.ViewGroup.LayoutParams.class}
    )
    public void testCheckLayoutParams() {
        MockLinearLayout mockLinearLayout = new MockLinearLayout(mContext);
        ViewGroup.LayoutParams params = new AbsoluteLayout.LayoutParams(240, 320, 0, 0);
        assertFalse(mockLinearLayout.checkLayoutParams(params));
        params = new LinearLayout.LayoutParams(240, 320);
        assertTrue(mockLinearLayout.checkLayoutParams(params));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link LinearLayout#generateDefaultLayoutParams()}",
        method = "generateDefaultLayoutParams",
        args = {}
    )
    public void testGenerateDefaultLayoutParams() {
        MockLinearLayout mockLinearLayout = new MockLinearLayout(mContext);
        mockLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        ViewGroup.LayoutParams param = mockLinearLayout.generateDefaultLayoutParams();
        assertNotNull(param);
        assertTrue(param instanceof LinearLayout.LayoutParams);
        assertEquals(ViewGroup.LayoutParams.WRAP_CONTENT, param.width);
        assertEquals(ViewGroup.LayoutParams.WRAP_CONTENT, param.height);
        mockLinearLayout.setOrientation(LinearLayout.VERTICAL);
        param = mockLinearLayout.generateDefaultLayoutParams();
        assertNotNull(param);
        assertTrue(param instanceof LinearLayout.LayoutParams);
        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, param.width);
        assertEquals(ViewGroup.LayoutParams.WRAP_CONTENT, param.height);
        mockLinearLayout.setOrientation(-1);
        assertNull(mockLinearLayout.generateDefaultLayoutParams());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test layout three horizontal children",
            method = "setGravity",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test layout three horizontal children",
            method = "setVerticalGravity",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test layout three horizontal children",
            method = "setOrientation",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test layout three horizontal children",
            method = "getOrientation",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test layout three horizontal children",
            method = "onMeasure",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test layout three horizontal children",
            method = "onLayout",
            args = {boolean.class, int.class, int.class, int.class, int.class}
        )
    })
    public void testLayoutHorizontal() {
        LinearLayout parent = (LinearLayout) mActivity.findViewById(R.id.horizontal);
        TextView topView = (TextView) mActivity.findViewById(R.id.gravity_top);
        TextView centerView = (TextView) mActivity.findViewById(R.id.gravity_center_vertical);
        TextView bottomView = (TextView) mActivity.findViewById(R.id.gravity_bottom);
        assertNotNull(parent);
        assertNotNull(topView);
        assertNotNull(centerView);
        assertNotNull(bottomView);
        assertEquals(mContext.getResources().getString(R.string.horizontal_text_1),
                topView.getText().toString());
        assertEquals(mContext.getResources().getString(R.string.horizontal_text_2),
                centerView.getText().toString());
        assertEquals(mContext.getResources().getString(R.string.horizontal_text_3),
                bottomView.getText().toString());
        assertEquals(LinearLayout.HORIZONTAL, parent.getOrientation());
        ViewAsserts.assertTopAligned(parent, topView);
        ViewAsserts.assertVerticalCenterAligned(parent, centerView);
        ViewAsserts.assertBottomAligned(parent, bottomView);
        assertEquals(0, topView.getTop());
        assertEquals(topView.getHeight(), topView.getBottom());
        assertEquals(0, topView.getLeft());
        assertEquals(centerView.getLeft(), topView.getRight());
        int offset = (parent.getHeight() - centerView.getHeight()) / 2;
        assertEquals(offset, centerView.getTop());
        assertEquals(offset + centerView.getHeight(), centerView.getBottom());
        assertEquals(topView.getRight(), centerView.getLeft());
        assertEquals(bottomView.getLeft(), centerView.getRight());
        assertEquals(parent.getHeight() - bottomView.getHeight(), bottomView.getTop());
        assertEquals(parent.getHeight(), bottomView.getBottom());
        assertEquals(centerView.getRight(), bottomView.getLeft());
        assertEquals(parent.getWidth(), bottomView.getRight());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test layout three vertical children",
            method = "setGravity",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test layout three vertical children",
            method = "setHorizontalGravity",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test layout three vertical children",
            method = "setOrientation",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test layout three vertical children",
            method = "getOrientation",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test layout three vertical children",
            method = "onMeasure",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test layout three vertical children",
            method = "onLayout",
            args = {boolean.class, int.class, int.class, int.class, int.class}
        )
    })
    public void testLayoutVertical() {
        LinearLayout parent = (LinearLayout) mActivity.findViewById(R.id.vertical);
        TextView leftView = (TextView) mActivity.findViewById(R.id.gravity_left);
        TextView centerView = (TextView) mActivity.findViewById(R.id.gravity_center_horizontal);
        TextView rightView = (TextView) mActivity.findViewById(R.id.gravity_right);
        assertNotNull(parent);
        assertNotNull(leftView);
        assertNotNull(centerView);
        assertNotNull(rightView);
        assertEquals(mContext.getResources().getString(R.string.vertical_text_1),
                leftView.getText().toString());
        assertEquals(mContext.getResources().getString(R.string.vertical_text_2),
                centerView.getText().toString());
        assertEquals(mContext.getResources().getString(R.string.vertical_text_3),
                rightView.getText().toString());
        assertEquals(LinearLayout.VERTICAL, parent.getOrientation());
        ViewAsserts.assertLeftAligned(parent, leftView);
        ViewAsserts.assertHorizontalCenterAligned(parent, centerView);
        ViewAsserts.assertRightAligned(parent, rightView);
        assertEquals(0, leftView.getTop());
        assertEquals(centerView.getTop(), leftView.getBottom());
        assertEquals(0, leftView.getLeft());
        assertEquals(leftView.getWidth(), leftView.getRight());
        int offset = (parent.getWidth() - centerView.getWidth()) / 2;
        assertEquals(leftView.getBottom(), centerView.getTop());
        assertEquals(rightView.getTop(), centerView.getBottom());
        assertEquals(offset, centerView.getLeft());
        assertEquals(offset + centerView.getWidth(), centerView.getRight());
        assertEquals(centerView.getBottom(), rightView.getTop());
        assertEquals(parent.getHeight(), rightView.getBottom());
        assertEquals(parent.getWidth() - rightView.getWidth(), rightView.getLeft());
        assertEquals(parent.getWidth(), rightView.getRight());
    }
    private class MockListView extends ListView {
        private final static int DEFAULT_CHILD_BASE_LINE = 1;
        public MockListView(Context context) {
            super(context);
        }
        public int getBaseline() {
            return DEFAULT_CHILD_BASE_LINE;
        }
    }
    private class MockLinearLayout extends LinearLayout {
        public MockLinearLayout(Context c) {
            super(c);
        }
        @Override
        protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
            return super.checkLayoutParams(p);
        }
        @Override
        protected LinearLayout.LayoutParams generateDefaultLayoutParams() {
            return super.generateDefaultLayoutParams();
        }
        @Override
        protected LinearLayout.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
            return super.generateLayoutParams(p);
        }
    }
}
