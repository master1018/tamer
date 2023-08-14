@TestTargetClass(android.graphics.drawable.RotateDrawable.class)
public class RotateDrawableTest extends AndroidTestCase {
    private RotateDrawable mRotateDrawable;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Resources resources = mContext.getResources();
        mRotateDrawable = (RotateDrawable) resources.getDrawable(R.drawable.rotatedrawable);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "RotateDrawable",
        args = {}
    )
    public void testConstructor() {
        new RotateDrawable();
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        method = "draw",
        args = {android.graphics.Canvas.class}
    )
    public void testDraw() {
        Canvas canvas = new Canvas();
        mRotateDrawable.draw(canvas);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getChangingConfigurations",
        args = {}
    )
    public void testGetChangingConfigurations() {
        assertEquals(0, mRotateDrawable.getChangingConfigurations());
        mRotateDrawable.setChangingConfigurations(Configuration.KEYBOARD_NOKEYS);
        assertEquals(Configuration.KEYBOARD_NOKEYS, mRotateDrawable.getChangingConfigurations());
        mRotateDrawable.setChangingConfigurations(Configuration.KEYBOARD_12KEY);
        assertEquals(Configuration.KEYBOARD_12KEY, mRotateDrawable.getChangingConfigurations());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setAlpha",
        args = {int.class}
    )
    public void testSetAlpha() {
        mRotateDrawable.setAlpha(100);
        assertEquals(100, ((BitmapDrawable) mRotateDrawable.getDrawable()).getPaint().getAlpha());
        mRotateDrawable.setAlpha(255);
        assertEquals(255, ((BitmapDrawable) mRotateDrawable.getDrawable()).getPaint().getAlpha());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setColorFilter",
        args = {android.graphics.ColorFilter.class}
    )
    public void testSetColorFilter() {
        ColorFilter filter = new ColorFilter();
        mRotateDrawable.setColorFilter(filter);
        assertSame(filter,
                ((BitmapDrawable) mRotateDrawable.getDrawable()).getPaint().getColorFilter());
        mRotateDrawable.setColorFilter(null);
        assertNull(((BitmapDrawable) mRotateDrawable.getDrawable()).getPaint().getColorFilter());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getOpacity",
        args = {}
    )
    public void testGetOpacity() {
        assertEquals(PixelFormat.OPAQUE, mRotateDrawable.getOpacity());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "invalidateDrawable",
        args = {android.graphics.drawable.Drawable.class}
    )
    public void testInvalidateDrawable() {
        Drawable drawable = mContext.getResources().getDrawable(R.drawable.pass);
        MockCallback callback = new MockCallback();
        mRotateDrawable.setCallback(callback);
        mRotateDrawable.invalidateDrawable(null);
        assertTrue(callback.hasCalledInvalidate());
        callback.reset();
        mRotateDrawable.invalidateDrawable(drawable);
        assertTrue(callback.hasCalledInvalidate());
        callback.reset();
        mRotateDrawable.setCallback(null);
        mRotateDrawable.invalidateDrawable(drawable);
        assertFalse(callback.hasCalledInvalidate());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "scheduleDrawable",
        args = {android.graphics.drawable.Drawable.class, java.lang.Runnable.class, long.class}
    )
    public void testScheduleDrawable() {
        MockCallback callback = new MockCallback();
        mRotateDrawable.setCallback(callback);
        mRotateDrawable.scheduleDrawable(null, null, 0);
        assertTrue(callback.hasCalledSchedule());
        callback.reset();
        mRotateDrawable.scheduleDrawable(new BitmapDrawable(), new Runnable() {
            public void run() {
            }
        }, 1000L);
        assertTrue(callback.hasCalledSchedule());
        callback.reset();
        mRotateDrawable.setCallback(null);
        mRotateDrawable.scheduleDrawable(null, null, 0);
        assertFalse(callback.hasCalledSchedule());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "unscheduleDrawable",
        args = {android.graphics.drawable.Drawable.class, java.lang.Runnable.class}
    )
    public void testUnscheduleDrawable() {
        MockCallback callback = new MockCallback();
        mRotateDrawable.setCallback(callback);
        mRotateDrawable.unscheduleDrawable(null, null);
        assertTrue(callback.hasCalledUnschedule());
        callback.reset();
        mRotateDrawable.unscheduleDrawable(new BitmapDrawable(), new Runnable() {
            public void run() {
            }
        });
        assertTrue(callback.hasCalledUnschedule());
        callback.reset();
        mRotateDrawable.setCallback(null);
        mRotateDrawable.unscheduleDrawable(null, null);
        assertFalse(callback.hasCalledUnschedule());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getPadding",
        args = {android.graphics.Rect.class}
    )
    public void testGetPadding() {
        Rect rect = new Rect();
        assertFalse(mRotateDrawable.getPadding(rect));
        assertEquals(0, rect.left);
        assertEquals(0, rect.top);
        assertEquals(0, rect.right);
        assertEquals(0, rect.bottom);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setVisible",
        args = {boolean.class, boolean.class}
    )
    public void testSetVisible() {
        assertTrue(mRotateDrawable.isVisible());
        assertTrue(mRotateDrawable.setVisible(false, false));
        assertFalse(mRotateDrawable.isVisible());
        assertFalse(mRotateDrawable.setVisible(false, true));
        assertFalse(mRotateDrawable.isVisible());
        assertTrue(mRotateDrawable.setVisible(true, false));
        assertTrue(mRotateDrawable.isVisible());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "isStateful",
        args = {}
    )
    public void testIsStateful() {
        assertFalse(mRotateDrawable.isStateful());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "onStateChange",
            args = {int[].class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "onLevelChange",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "onBoundsChange",
            args = {android.graphics.Rect.class}
        )
    })
    public void testMethods() {
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getIntrinsicWidth",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getIntrinsicHeight",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "inflate",
            args = {android.content.res.Resources.class, org.xmlpull.v1.XmlPullParser.class,
                    android.util.AttributeSet.class}
        )
    })
    public void testGetIntrinsicWidthAndHeight() throws XmlPullParserException, IOException {
        Drawable drawable = mContext.getResources().getDrawable(R.drawable.testimage);
        assertEquals(drawable.getIntrinsicWidth(), mRotateDrawable.getIntrinsicWidth());
        assertEquals(drawable.getIntrinsicHeight(), mRotateDrawable.getIntrinsicHeight());
        RotateDrawable rotateDrawable = new RotateDrawable();
        Resources r = mContext.getResources();
        XmlPullParser parser = r.getXml(R.drawable.rotatedrawable);
        while (parser.next() != XmlPullParser.START_TAG) {
        }
        AttributeSet attrs = Xml.asAttributeSet(parser);
        rotateDrawable.inflate(r, parser, attrs);
        assertEquals(drawable.getIntrinsicWidth(), rotateDrawable.getIntrinsicWidth());
        assertEquals(drawable.getIntrinsicHeight(), rotateDrawable.getIntrinsicHeight());
        try {
            mRotateDrawable.inflate(null, null, null);
            fail("did not throw NullPointerException when parameters are null.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getConstantState",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete")
    public void testGetConstantState() {
        ConstantState state = mRotateDrawable.getConstantState();
        assertNotNull(state);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "mutate",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDrawable",
            args = {}
        )
    })
    public void testMutate() {
        Resources resources = mContext.getResources();
        RotateDrawable d1 = (RotateDrawable) resources.getDrawable(R.drawable.rotatedrawable);
        RotateDrawable d2 = (RotateDrawable) resources.getDrawable(R.drawable.rotatedrawable);
        RotateDrawable d3 = (RotateDrawable) resources.getDrawable(R.drawable.rotatedrawable);
        d1.setAlpha(100);
        assertEquals(100, ((BitmapDrawable) d1.getDrawable()).getPaint().getAlpha());
        assertEquals(100, ((BitmapDrawable) d2.getDrawable()).getPaint().getAlpha());
        assertEquals(100, ((BitmapDrawable) d3.getDrawable()).getPaint().getAlpha());
        d1.mutate();
        d1.setAlpha(200);
        assertEquals(200, ((BitmapDrawable) d1.getDrawable()).getPaint().getAlpha());
        assertEquals(100, ((BitmapDrawable) d2.getDrawable()).getPaint().getAlpha());
        assertEquals(100, ((BitmapDrawable) d3.getDrawable()).getPaint().getAlpha());
        d2.setAlpha(50);
        assertEquals(200, ((BitmapDrawable) d1.getDrawable()).getPaint().getAlpha());
        assertEquals(50, ((BitmapDrawable) d2.getDrawable()).getPaint().getAlpha());
        assertEquals(50, ((BitmapDrawable) d3.getDrawable()).getPaint().getAlpha());
    }
    private static class MockCallback implements Callback {
        private boolean mCalledInvalidate;
        private boolean mCalledSchedule;
        private boolean mCalledUnschedule;
        public void invalidateDrawable(Drawable who) {
            mCalledInvalidate = true;
        }
        public void scheduleDrawable(Drawable who, Runnable what, long when) {
            mCalledSchedule = true;
        }
        public void unscheduleDrawable(Drawable who, Runnable what) {
            mCalledUnschedule = true;
        }
        public boolean hasCalledInvalidate() {
            return mCalledInvalidate;
        }
        public boolean hasCalledSchedule() {
            return mCalledSchedule;
        }
        public boolean hasCalledUnschedule() {
            return mCalledUnschedule;
        }
        public void reset() {
            mCalledInvalidate = false;
            mCalledSchedule = false;
            mCalledUnschedule = false;
        }
    }
}
