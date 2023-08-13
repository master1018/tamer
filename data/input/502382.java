@TestTargetClass(RelativeLayout.class)
public class RelativeLayoutTest extends
        ActivityInstrumentationTestCase2<RelativeLayoutStubActivity> {
    private Activity mActivity;
    public RelativeLayoutTest() {
        super("com.android.cts.stub", RelativeLayoutStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "RelativeLayout",
            args = {android.content.Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "RelativeLayout",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "RelativeLayout",
            args = {android.content.Context.class, android.util.AttributeSet.class, int.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for constructors does not exist.")
    public void testConstructor() {
        new RelativeLayout(mActivity);
        new RelativeLayout(mActivity, null);
        new RelativeLayout(mActivity, null, 0);
        XmlPullParser parser = mActivity.getResources().getXml(R.layout.relative_layout);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        new RelativeLayout(mActivity, attrs);
        try {
            new RelativeLayout(null, null);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setIgnoreGravity",
        args = {int.class}
    )
    public void testSetIgnoreGravity() {
        final RelativeLayout relativeLayout = (RelativeLayout) mActivity.findViewById(
                R.id.relative_sublayout_ignore_gravity);
        View view12 = mActivity.findViewById(R.id.relative_view12);
        View view13 = mActivity.findViewById(R.id.relative_view13);
        ViewAsserts.assertLeftAligned(relativeLayout, view12);
        ViewAsserts.assertRightAligned(relativeLayout, view13);
        relativeLayout.setIgnoreGravity(R.id.relative_view13);
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                relativeLayout.requestLayout();
            }
        });
        getInstrumentation().waitForIdleSync();
        ViewAsserts.assertRightAligned(relativeLayout, view12);
        ViewAsserts.assertLeftAligned(relativeLayout, view13);
        relativeLayout.setIgnoreGravity(0);
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                relativeLayout.requestLayout();
            }
        });
        getInstrumentation().waitForIdleSync();
        ViewAsserts.assertRightAligned(relativeLayout, view12);
        ViewAsserts.assertRightAligned(relativeLayout, view13);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setGravity",
        args = {int.class}
    )
    public void testSetGravity() {
        final RelativeLayout relativeLayout = (RelativeLayout) mActivity.findViewById(
                R.id.relative_sublayout_gravity);
        View view10 = mActivity.findViewById(R.id.relative_view10);
        View view11 = mActivity.findViewById(R.id.relative_view11);
        ViewAsserts.assertLeftAligned(relativeLayout, view10);
        ViewAsserts.assertTopAligned(relativeLayout, view10);
        ViewAsserts.assertLeftAligned(relativeLayout, view11);
        assertEquals(view11.getTop(), view10.getBottom());
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                relativeLayout.setGravity(Gravity.BOTTOM | Gravity.RIGHT);
            }
        });
        getInstrumentation().waitForIdleSync();
        ViewAsserts.assertRightAligned(relativeLayout, view10);
        assertEquals(view11.getTop(), view10.getBottom());
        ViewAsserts.assertRightAligned(relativeLayout, view11);
        ViewAsserts.assertBottomAligned(relativeLayout, view11);
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                relativeLayout.setGravity(Gravity.BOTTOM);
            }
        });
        getInstrumentation().waitForIdleSync();
        ViewAsserts.assertLeftAligned(relativeLayout, view10);
        assertEquals(view11.getTop(), view10.getBottom());
        ViewAsserts.assertLeftAligned(relativeLayout, view11);
        ViewAsserts.assertBottomAligned(relativeLayout, view11);
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                relativeLayout.setGravity(Gravity.CENTER_HORIZONTAL);
            }
        });
        getInstrumentation().waitForIdleSync();
        ViewAsserts.assertHorizontalCenterAligned(relativeLayout, view10);
        ViewAsserts.assertTopAligned(relativeLayout, view10);
        ViewAsserts.assertHorizontalCenterAligned(relativeLayout, view11);
        assertEquals(view11.getTop(), view10.getBottom());
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                relativeLayout.setGravity(Gravity.CENTER_VERTICAL);
            }
        });
        getInstrumentation().waitForIdleSync();
        ViewAsserts.assertLeftAligned(relativeLayout, view10);
        int topSpace = view10.getTop();
        int bottomSpace = relativeLayout.getHeight() - view11.getBottom();
        assertTrue(Math.abs(bottomSpace - topSpace) <= 1);
        ViewAsserts.assertLeftAligned(relativeLayout, view11);
        assertEquals(view11.getTop(), view10.getBottom());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setHorizontalGravity",
        args = {int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for setHorizontalGravity()" +
            " does not exist.")
    public void testSetHorizontalGravity() {
        final RelativeLayout relativeLayout = (RelativeLayout) mActivity.findViewById(
                R.id.relative_sublayout_gravity);
        View view10 = mActivity.findViewById(R.id.relative_view10);
        View view11 = mActivity.findViewById(R.id.relative_view11);
        ViewAsserts.assertLeftAligned(relativeLayout, view10);
        ViewAsserts.assertTopAligned(relativeLayout, view10);
        ViewAsserts.assertLeftAligned(relativeLayout, view11);
        assertEquals(view11.getTop(), view10.getBottom());
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                relativeLayout.setHorizontalGravity(Gravity.RIGHT);
            }
        });
        getInstrumentation().waitForIdleSync();
        ViewAsserts.assertRightAligned(relativeLayout, view10);
        ViewAsserts.assertTopAligned(relativeLayout, view10);
        ViewAsserts.assertRightAligned(relativeLayout, view11);
        assertEquals(view11.getTop(), view10.getBottom());
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                relativeLayout.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
            }
        });
        getInstrumentation().waitForIdleSync();
        ViewAsserts.assertHorizontalCenterAligned(relativeLayout, view10);
        ViewAsserts.assertTopAligned(relativeLayout, view10);
        ViewAsserts.assertHorizontalCenterAligned(relativeLayout, view11);
        assertEquals(view11.getTop(), view10.getBottom());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setVerticalGravity",
        args = {int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for setVerticalGravity()" +
            " does not exist.")
    public void testSetVerticalGravity() {
        final RelativeLayout relativeLayout = (RelativeLayout) mActivity.findViewById(
                R.id.relative_sublayout_gravity);
        View view10 = mActivity.findViewById(R.id.relative_view10);
        View view11 = mActivity.findViewById(R.id.relative_view11);
        ViewAsserts.assertLeftAligned(relativeLayout, view10);
        ViewAsserts.assertTopAligned(relativeLayout, view10);
        ViewAsserts.assertLeftAligned(relativeLayout, view11);
        assertEquals(view11.getTop(), view10.getBottom());
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                relativeLayout.setVerticalGravity(Gravity.BOTTOM);
            }
        });
        getInstrumentation().waitForIdleSync();
        ViewAsserts.assertLeftAligned(relativeLayout, view10);
        assertEquals(view11.getTop(), view10.getBottom());
        ViewAsserts.assertLeftAligned(relativeLayout, view11);
        ViewAsserts.assertBottomAligned(relativeLayout, view11);
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                relativeLayout.setVerticalGravity(Gravity.CENTER_VERTICAL);
            }
        });
        getInstrumentation().waitForIdleSync();
        ViewAsserts.assertLeftAligned(relativeLayout, view10);
        int topSpace = view10.getTop();
        int bottomSpace = relativeLayout.getHeight() - view11.getBottom();
        assertTrue(Math.abs(bottomSpace - topSpace) <= 1);
        ViewAsserts.assertLeftAligned(relativeLayout, view11);
        assertEquals(view11.getTop(), view10.getBottom());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getBaseline",
        args = {}
    )
    public void testGetBaseline() {
        RelativeLayout relativeLayout = new RelativeLayout(mActivity);
        assertEquals(-1, relativeLayout.getBaseline());
        relativeLayout = (RelativeLayout) mActivity.findViewById(R.id.relative_sublayout_attrs);
        View view = mActivity.findViewById(R.id.relative_view1);
        assertEquals(view.getBaseline(), relativeLayout.getBaseline());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "generateLayoutParams",
        args = {android.util.AttributeSet.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for generateLayoutParams() is" +
            " incomplete, not clear what is supposed to happen when attrs is null.")
    public void testGenerateLayoutParams1() throws XmlPullParserException, IOException {
        RelativeLayout relativeLayout = new RelativeLayout(mActivity);
        XmlResourceParser parser = mActivity.getResources().getLayout(R.layout.relative_layout);
        XmlUtils.beginDocument(parser, "RelativeLayout");
        LayoutParams layoutParams = relativeLayout.generateLayoutParams(parser);
        assertEquals(LayoutParams.MATCH_PARENT, layoutParams.width);
        assertEquals(LayoutParams.MATCH_PARENT, layoutParams.height);
        try {
            relativeLayout.generateLayoutParams((AttributeSet) null);
            fail("Should throw RuntimeException");
        } catch (RuntimeException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "generateLayoutParams",
        args = {android.view.ViewGroup.LayoutParams.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for generateLayoutParams() is" +
            " incomplete, not clear what is supposed to happen when the LayoutParam is null.")
    public void testGenerateLayoutParams2() {
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(200, 300);
        MyRelativeLayout myRelativeLayout = new MyRelativeLayout(mActivity);
         RelativeLayout.LayoutParams layoutParams =
                 (RelativeLayout.LayoutParams) myRelativeLayout.generateLayoutParams(p);
         assertEquals(200, layoutParams.width);
         assertEquals(300, layoutParams.height);
         try {
             myRelativeLayout.generateLayoutParams((ViewGroup.LayoutParams) null);
             fail("Should throw RuntimeException");
         } catch (RuntimeException e) {
         }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "generateDefaultLayoutParams",
        args = {}
    )
    public void testGenerateDefaultLayoutParams() {
        MyRelativeLayout myRelativeLayout = new MyRelativeLayout(mActivity);
        ViewGroup.LayoutParams layoutParams = myRelativeLayout.generateDefaultLayoutParams();
        assertTrue(layoutParams instanceof RelativeLayout.LayoutParams);
        assertEquals(ViewGroup.LayoutParams.WRAP_CONTENT, layoutParams.width);
        assertEquals(ViewGroup.LayoutParams.WRAP_CONTENT, layoutParams.height);
    }
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        method = "onMeasure",
        args = {int.class, int.class}
    )
    public void testOnMeasure() {
    }
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        method = "onLayout",
        args = {boolean.class, int.class, int.class, int.class, int.class}
    )
    public void testOnLayout() {
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "checkLayoutParams",
        args = {android.view.ViewGroup.LayoutParams.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for checkLayoutParams() does not exist.")
    public void testCheckLayoutParams() {
        MyRelativeLayout myRelativeLayout = new MyRelativeLayout(mActivity);
        ViewGroup.LayoutParams p1 = new ViewGroup.LayoutParams(200, 300);
        assertFalse(myRelativeLayout.checkLayoutParams(p1));
        RelativeLayout.LayoutParams p2 = new RelativeLayout.LayoutParams(200, 300);
        assertTrue(myRelativeLayout.checkLayoutParams(p2));
        AbsListView.LayoutParams p3 = new AbsListView.LayoutParams(200, 300);
        assertFalse(myRelativeLayout.checkLayoutParams(p3));
    }
    private class MyRelativeLayout extends RelativeLayout {
        public MyRelativeLayout(Context context) {
            super(context);
        }
        @Override
        protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);
        }
        @Override
        protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
            return super.checkLayoutParams(p);
        }
        @Override
        protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
            return super.generateDefaultLayoutParams();
        }
        @Override
        protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
            return super.generateLayoutParams(p);
        }
    }
}
