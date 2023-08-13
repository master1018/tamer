@TestTargetClass(Gallery.class)
public class GalleryTest extends ActivityInstrumentationTestCase2<GalleryStubActivity>  {
    private Gallery mGallery;
    private Activity mActivity;
    private Instrumentation mInstrumentation;
    private Context mContext;
    private final static float DELTA = 0.01f;
    public GalleryTest() {
        super("com.android.cts.stub", GalleryStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mInstrumentation = getInstrumentation();
        mContext = mInstrumentation.getContext();
        mGallery = (Gallery) mActivity.findViewById(R.id.gallery_test);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Gallery",
            args = {android.content.Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Gallery",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Gallery",
            args = {android.content.Context.class, android.util.AttributeSet.class, int.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete")
    public void testConstructor() {
        new Gallery(mContext);
        new Gallery(mContext, null);
        new Gallery(mContext, null, 0);
        XmlPullParser parser = getActivity().getResources().getXml(R.layout.gallery_test);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        new Gallery(mContext, attrs);
        new Gallery(mContext, attrs, 0);
        try {
            new Gallery(null);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
        try {
            new Gallery(null, null);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
        try {
            new Gallery(null, null, 0);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setCallbackDuringFling",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onTouchEvent",
            args = {android.view.MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onFling",
            args = {MotionEvent.class, MotionEvent.class, float.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onDown",
            args = {android.view.MotionEvent.class}
        )
    })
    @BrokenTest("listener.isItemSelected() is false, need to investigate")
    public void testSetCallbackDuringFling() {
        MockOnItemSelectedListener listener = new MockOnItemSelectedListener();
        mGallery.setOnItemSelectedListener(listener);
        mGallery.setCallbackDuringFling(true);
        int[] xy = new int[2];
        getSelectedViewCenter(mGallery, xy);
        TouchUtils.drag(this, xy[0], 0, xy[1], xy[1], 1);
        listener.reset();
        TouchUtils.drag(this, xy[0], 0, xy[1], xy[1], 1);
        assertTrue(listener.isItemSelected());
        assertTrue(listener.getItemSelectedCalledCount() > 1);
        listener.reset();
        mGallery.setCallbackDuringFling(false);
        TouchUtils.drag(this, xy[0], 240, xy[1], xy[1], 1);
        assertTrue(listener.isItemSelected());
        assertTrue(listener.getItemSelectedCalledCount() == 1);
    }
    private void getSelectedViewCenter(Gallery gallery, int[] xy) {
        View v = gallery.getSelectedView();
        v.getLocationOnScreen(xy);
        final int viewWidth = v.getWidth();
        final int viewHeight = v.getHeight();
        xy[1] += viewHeight / 2;
        xy[0] += viewWidth / 2;
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        method = "setAnimationDuration",
        args = {int.class}
    )
    @ToBeFixed(bug = "1386429", explanation = "No getter and can't check indirectly. "
            + "It is hard to get transition animation to check if the duration is right.")
    public void testSetAnimationDuration() {
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setSpacing",
        args = {int.class}
    )
    public void testSetSpacing() throws Throwable {
        setSpacingAndCheck(0);
        setSpacingAndCheck(5);
        setSpacingAndCheck(-1);
    }
    private void setSpacingAndCheck(final int spacing) throws Throwable {
        runTestOnUiThread(new Runnable() {
            public void run() {
                mGallery.setSpacing(spacing);
                mGallery.requestLayout();
            }
        });
        mInstrumentation.waitForIdleSync();
        View v0 = mGallery.getChildAt(0);
        View v1 = mGallery.getChildAt(1);
        assertEquals(v0.getRight() + spacing, v1.getLeft());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setUnselectedAlpha",
            args = {float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getChildStaticTransformation",
            args = {android.view.View.class, android.view.animation.Transformation.class}
        )
    })
    public void testSetUnselectedAlpha() {
        final MyGallery gallery = (MyGallery) mActivity.findViewById(R.id.gallery_test);
        checkUnselectedAlpha(gallery, 0.0f);
        checkUnselectedAlpha(gallery, 0.5f);
    }
    private void checkUnselectedAlpha(MyGallery gallery, float alpha) {
        final float DEFAULT_ALPHA = 1.0f;
        View v0 = gallery.getChildAt(0);
        View v1 = gallery.getChildAt(1);
        gallery.setUnselectedAlpha(alpha);
        Transformation t = new Transformation();
        gallery.getChildStaticTransformation(v0, t);
        assertEquals(DEFAULT_ALPHA, t.getAlpha(), DELTA);
        gallery.getChildStaticTransformation(v1, t);
        assertEquals(alpha, t.getAlpha(), DELTA);
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
    public void testGenerateLayoutParams() throws XmlPullParserException, IOException {
        final int width = 320;
        final int height = 240;
        LayoutParams lp = new LayoutParams(width, height);
        MyGallery gallery = new MyGallery(mContext);
        LayoutParams layoutParams = gallery.generateLayoutParams(lp);
        assertEquals(width, layoutParams.width);
        assertEquals(height, layoutParams.height);
        XmlPullParser parser = getActivity().getResources().getXml(R.layout.gallery_test);
        WidgetTestUtils.beginDocument(parser, "LinearLayout");
        AttributeSet attrs = Xml.asAttributeSet(parser);
        mGallery = new Gallery(mContext, attrs);
        layoutParams = mGallery.generateLayoutParams(attrs);
        assertEquals(LayoutParams.MATCH_PARENT, layoutParams.width);
        assertEquals(LayoutParams.MATCH_PARENT, layoutParams.height);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onSingleTapUp",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onScroll",
            args = {MotionEvent.class, MotionEvent.class, float.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onLongPress",
            args = {android.view.MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onShowPress",
            args = {android.view.MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onKeyDown",
            args = {int.class, android.view.KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onKeyUp",
            args = {int.class, android.view.KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onFocusChanged",
            args = {boolean.class, int.class, android.graphics.Rect.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onLayout",
            args = {boolean.class, int.class, int.class, int.class, int.class}
        )
    })
    public void testFoo() {
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        method = "showContextMenuForChild",
        args = {android.view.View.class}
    )
    public void testShowContextMenuForChild() {
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        method = "showContextMenu",
        args = {}
    )
    public void testShowContextMenu() {
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "dispatchKeyEvent",
        args = {android.view.KeyEvent.class}
    )
    public void testDispatchKeyEvent() {
        mGallery = new Gallery(mContext);
        final KeyEvent validKeyEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER);
        assertTrue(mGallery.dispatchKeyEvent(validKeyEvent));
        final long time = SystemClock.uptimeMillis();
        final KeyEvent invalidKeyEvent
                = new KeyEvent(time, time, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_A, 5);
        assertFalse(mGallery.dispatchKeyEvent(invalidKeyEvent));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setGravity",
        args = {int.class}
    )
    public void testSetGravity() throws Throwable {
        setGalleryGravity(Gravity.CENTER_HORIZONTAL);
        View v0 = mGallery.getChildAt(0);
        ViewAsserts.assertHorizontalCenterAligned(mGallery, v0);
        setGalleryGravity(Gravity.TOP);
        v0 = mGallery.getChildAt(0);
        ViewAsserts.assertTopAligned(mGallery, v0, mGallery.getPaddingTop());
        setGalleryGravity(Gravity.BOTTOM);
        v0 = mGallery.getChildAt(0);
        ViewAsserts.assertBottomAligned(mGallery, v0, mGallery.getPaddingBottom());
    }
    private void setGalleryGravity(final int gravity) throws Throwable {
        runTestOnUiThread(new Runnable() {
            public void run() {
                mGallery.setGravity(gravity);
                mGallery.invalidate();
                mGallery.requestLayout();
            }
        });
        mInstrumentation.waitForIdleSync();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "checkLayoutParams",
        args = {android.view.ViewGroup.LayoutParams.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete")
    public void testCheckLayoutParams() {
        MyGallery gallery = new MyGallery(mContext);
        ViewGroup.LayoutParams p1 = new ViewGroup.LayoutParams(320, 480);
        assertFalse(gallery.checkLayoutParams(p1));
        Gallery.LayoutParams p2 = new Gallery.LayoutParams(320, 480);
        assertTrue(gallery.checkLayoutParams(p2));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "computeHorizontalScrollExtent",
        args = {}
    )
    public void testComputeHorizontalScrollExtent() {
        MyGallery gallery = new MyGallery(mContext);
        assertEquals(1, gallery.computeHorizontalScrollExtent());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "computeHorizontalScrollOffset",
        args = {}
    )
    public void testComputeHorizontalScrollOffset() {
        MyGallery gallery = new MyGallery(mContext);
        assertEquals(AdapterView.INVALID_POSITION, gallery.computeHorizontalScrollOffset());
        gallery.setAdapter(new ImageAdapter(mActivity));
        assertEquals(gallery.getSelectedItemPosition(), gallery.computeHorizontalScrollOffset());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "computeHorizontalScrollRange",
        args = {}
    )
    public void testComputeHorizontalScrollRange() {
        MyGallery gallery = new MyGallery(mContext);
        ImageAdapter adapter = new ImageAdapter(mActivity);
        gallery.setAdapter(adapter);
        assertEquals(adapter.getCount(), gallery.computeHorizontalScrollRange());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "dispatchSetPressed",
        args = {boolean.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are not right, "
            + "dispatchSetPressed did not dispatch setPressed to "
            + "all of this View's children, but only the selected view")
    @UiThreadTest
    public void testDispatchSetPressed() {
        final MyGallery gallery = (MyGallery) getActivity().findViewById(R.id.gallery_test);
        gallery.setSelection(0);
        gallery.dispatchSetPressed(true);
        assertTrue(gallery.getSelectedView().isPressed());
        assertFalse(gallery.getChildAt(1).isPressed());
        gallery.dispatchSetPressed(false);
        assertFalse(gallery.getSelectedView().isPressed());
        assertFalse(gallery.getChildAt(1).isPressed());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "generateDefaultLayoutParams",
        args = {}
    )
    public void testGenerateDefaultLayoutParams() {
        MyGallery gallery = new MyGallery(mContext);
        ViewGroup.LayoutParams p = gallery.generateDefaultLayoutParams();
        assertNotNull(p);
        assertTrue(p instanceof Gallery.LayoutParams);
        assertEquals(ViewGroup.LayoutParams.WRAP_CONTENT, p.width);
        assertEquals(ViewGroup.LayoutParams.WRAP_CONTENT, p.height);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getChildDrawingOrder",
        args = {int.class, int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete")
    public void testGetChildDrawingOrder() {
        final MyGallery gallery = (MyGallery) getActivity().findViewById(R.id.gallery_test);
        int childCount = 3;
        int index = 2;
        assertEquals(gallery.getSelectedItemPosition(),
                gallery.getChildDrawingOrder(childCount, index));
        childCount = 5;
        index = 2;
        assertEquals(index + 1, gallery.getChildDrawingOrder(childCount, index));
        childCount = 5;
        index = 3;
        assertEquals(index + 1, gallery.getChildDrawingOrder(childCount, index));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getContextMenuInfo",
        args = {}
    )
    public void testGetContextMenuInfo() {
        MockOnCreateContextMenuListener listener = new MockOnCreateContextMenuListener();
        MyGallery gallery = new MyGallery(mContext);
        gallery.setOnCreateContextMenuListener(listener);
        assertFalse(listener.hasCreatedContextMenu());
        gallery.createContextMenu(new ContextMenuBuilder(mContext));
        assertTrue(listener.hasCreatedContextMenu());
        assertSame(gallery.getContextMenuInfo(), listener.getContextMenuInfo());
    }
    private static class MockOnCreateContextMenuListener implements OnCreateContextMenuListener {
        private boolean hasCreatedContextMenu;
        private ContextMenuInfo mContextMenuInfo;
        public boolean hasCreatedContextMenu() {
            return hasCreatedContextMenu;
        }
        public ContextMenuInfo getContextMenuInfo() {
            return mContextMenuInfo;
        }
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
            hasCreatedContextMenu = true;
            mContextMenuInfo = menuInfo;
        }
    }
    private static class ImageAdapter extends BaseAdapter {
        public ImageAdapter(Context c) {
            mContext = c;
        }
        public int getCount() {
            return mImageIds.length;
        }
        public Object getItem(int position) {
            return position;
        }
        public long getItemId(int position) {
            return position;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView i = new ImageView(mContext);
            i.setImageResource(mImageIds[position]);
            i.setScaleType(ImageView.ScaleType.FIT_XY);
            i.setLayoutParams(new Gallery.LayoutParams(136, 88));
            return i;
        }
        private Context mContext;
        private Integer[] mImageIds = {
                R.drawable.faces,
                R.drawable.scenery,
                R.drawable.testimage,
                R.drawable.faces,
                R.drawable.scenery,
                R.drawable.testimage,
                R.drawable.faces,
                R.drawable.scenery,
                R.drawable.testimage,
        };
    }
    private static class MockOnItemSelectedListener implements OnItemSelectedListener {
        private boolean mIsItemSelected;
        private boolean mNothingSelected;
        private int mItemSelectedCalledCount;
        public boolean isItemSelected() {
            return mIsItemSelected;
        }
        public boolean hasNothingSelected() {
            return mNothingSelected;
        }
        public int getItemSelectedCalledCount() {
            return mItemSelectedCalledCount;
        }
        public void reset() {
            mIsItemSelected = false;
            mNothingSelected = true;
            mItemSelectedCalledCount = 0;
        }
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mIsItemSelected = true;
            mItemSelectedCalledCount++;
        }
        public void onNothingSelected(AdapterView<?> parent) {
            mNothingSelected = true;
        }
    }
}
