@TestTargetClass(AnimationDrawable.class)
public class AnimationDrawableTest extends ActivityInstrumentationTestCase2<ImageViewStubActivity> {
    private static final int FRAMES_COUNT        = 3;
    private static final int FIRST_FRAME_INDEX   = 0;
    private static final int SECOND_FRAME_INDEX  = 1;
    private static final int THIRD_FRAME_INDEX   = 2;
    private static final long TOLERANCE = 500;
    private static final long FIRST_FRAME_DURATION   = 3000;
    private static final long SECOND_FRAME_DURATION  = 2000;
    private static final long THIRD_FRAME_DURATION   = 1000;
    private AnimationDrawable mAnimationDrawable;
    private Resources mResources;
    public AnimationDrawableTest() {
        super("com.android.cts.stub", ImageViewStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final Activity activity = getActivity();
        mResources = activity.getResources();
        try {
            runTestOnUiThread(new Runnable() {
                public void run() {
                    ImageView imageView = (ImageView) activity.findViewById(R.id.imageview);
                    imageView.setBackgroundResource(R.drawable.animationdrawable);
                    mAnimationDrawable = (AnimationDrawable) imageView.getBackground();
                }
            });
        } catch (Throwable t) {
            throw new Exception(t);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "AnimationDrawable",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete")
    public void testConstructor() {
        mAnimationDrawable = new AnimationDrawable();
        assertNotNull(mAnimationDrawable.getConstantState());
        assertFalse(mAnimationDrawable.isRunning());
        assertTrue(mAnimationDrawable.isOneShot());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setVisible",
        args = {boolean.class, boolean.class}
    )
    public void testSetVisible() throws Throwable {
        assertTrue(mAnimationDrawable.isVisible());
        runTestOnUiThread(new Runnable() {
            public void run() {
                mAnimationDrawable.start();
            }
        });
        assertTrue(mAnimationDrawable.isRunning());
        assertSame(mAnimationDrawable.getFrame(FIRST_FRAME_INDEX),
                mAnimationDrawable.getCurrent());
        delayedCheckDrawable(SECOND_FRAME_INDEX, FIRST_FRAME_DURATION);
        runTestOnUiThread(new Runnable() {
            public void run() {
                assertTrue(mAnimationDrawable.setVisible(false, false));
            }
        });
        assertFalse(mAnimationDrawable.isVisible());
        assertFalse(mAnimationDrawable.isRunning());
        assertStoppedAnimation(SECOND_FRAME_INDEX, SECOND_FRAME_DURATION);
        runTestOnUiThread(new Runnable() {
            public void run() {
                assertTrue(mAnimationDrawable.setVisible(true, true));
            }
        });
        assertTrue(mAnimationDrawable.isVisible());
        assertFalse(mAnimationDrawable.isRunning());
        assertStoppedAnimation(FIRST_FRAME_INDEX, FIRST_FRAME_DURATION);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "start",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "stop",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isRunning",
            args = {}
        )
    })
    public void testStart() throws Throwable {
        assertFalse(mAnimationDrawable.isOneShot());
        assertFalse(mAnimationDrawable.isRunning());
        runTestOnUiThread(new Runnable() {
            public void run() {
                mAnimationDrawable.start();
            }
        });
        assertTrue(mAnimationDrawable.isRunning());
        assertSame(mAnimationDrawable.getFrame(FIRST_FRAME_INDEX),
                mAnimationDrawable.getCurrent());
        delayedCheckDrawable(SECOND_FRAME_INDEX, FIRST_FRAME_DURATION);
        runTestOnUiThread(new Runnable() {
            public void run() {
                mAnimationDrawable.start();
            }
        });
        delayedCheckDrawable(THIRD_FRAME_INDEX, SECOND_FRAME_DURATION);
        runTestOnUiThread(new Runnable() {
            public void run() {
                mAnimationDrawable.stop();
            }
        });
        assertFalse(mAnimationDrawable.isRunning());
        assertStoppedAnimation(THIRD_FRAME_INDEX, THIRD_FRAME_DURATION);
        runTestOnUiThread(new Runnable() {
            public void run() {
                mAnimationDrawable.stop();
            }
        });
        assertFalse(mAnimationDrawable.isRunning());
        assertStoppedAnimation(THIRD_FRAME_INDEX, THIRD_FRAME_DURATION);
    }
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        method = "run",
        args = {}
    )
    public void testRun() {
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "unscheduleSelf",
        args = {java.lang.Runnable.class}
    )
    public void testUnscheduleSelf() throws Throwable {
        assertFalse(mAnimationDrawable.isRunning());
        runTestOnUiThread(new Runnable() {
            public void run() {
                mAnimationDrawable.start();
            }
        });
        assertTrue(mAnimationDrawable.isRunning());
        delayedCheckDrawable(SECOND_FRAME_INDEX, FIRST_FRAME_DURATION);
        runTestOnUiThread(new Runnable() {
            public void run() {
                mAnimationDrawable.unscheduleSelf(mAnimationDrawable);
            }
        });
        assertFalse(mAnimationDrawable.isRunning());
        assertStoppedAnimation(SECOND_FRAME_INDEX, SECOND_FRAME_DURATION);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getNumberOfFrames",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addFrame",
            args = {android.graphics.drawable.Drawable.class, int.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "should add @throws clause into javadoc of "
        + "AnimationDrawable#addFrame(Drawable, int) when param frame is null")
    public void testGetNumberOfFrames() {
        assertEquals(FRAMES_COUNT, mAnimationDrawable.getNumberOfFrames());
        Drawable frame = mResources.getDrawable(R.drawable.failed);
        mAnimationDrawable.addFrame(frame, 2000);
        assertEquals(FRAMES_COUNT + 1, mAnimationDrawable.getNumberOfFrames());
        mAnimationDrawable.addFrame(frame, 2000);
        assertEquals(FRAMES_COUNT + 2, mAnimationDrawable.getNumberOfFrames());
        try {
            mAnimationDrawable.addFrame(null, 1000);
            fail("Should throw NullPointerException if param frame is null.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getFrame",
        args = {int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "should add @throws clause into javadoc of "
            + "AnimationDrawable#getFrame(int) when param index is out of bounds")
    public void testGetFrame() {
        Drawable frame = mAnimationDrawable.getFrame(FIRST_FRAME_INDEX);
        Drawable drawable = mResources.getDrawable(R.drawable.testimage);
        assertEquals(drawable.getIntrinsicWidth(), frame.getIntrinsicWidth());
        assertEquals(drawable.getIntrinsicHeight(), frame.getIntrinsicHeight());
        frame = mAnimationDrawable.getFrame(SECOND_FRAME_INDEX);
        drawable = mResources.getDrawable(R.drawable.pass);
        assertEquals(drawable.getIntrinsicWidth(), frame.getIntrinsicWidth());
        assertEquals(drawable.getIntrinsicHeight(), frame.getIntrinsicHeight());
        frame = mAnimationDrawable.getFrame(THIRD_FRAME_INDEX);
        drawable = mResources.getDrawable(R.drawable.scenery);
        assertEquals(drawable.getIntrinsicWidth(), frame.getIntrinsicWidth());
        assertEquals(drawable.getIntrinsicHeight(), frame.getIntrinsicHeight());
        assertNull(mAnimationDrawable.getFrame(THIRD_FRAME_INDEX + 1));
        try {
            mAnimationDrawable.getFrame(-1);
            fail("Should throw ArrayIndexOutOfBoundsException.");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            mAnimationDrawable.getFrame(10);
            fail("Should throw ArrayIndexOutOfBoundsException.");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getDuration",
        args = {int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "should add @throws clause into javadoc of "
            + "AnimationDrawable#getDuration(int) when param index is out of bounds")
    public void testGetDuration() {
        assertEquals(FIRST_FRAME_DURATION, mAnimationDrawable.getDuration(FIRST_FRAME_INDEX));
        assertEquals(SECOND_FRAME_DURATION, mAnimationDrawable.getDuration(SECOND_FRAME_INDEX));
        assertEquals(THIRD_FRAME_DURATION, mAnimationDrawable.getDuration(THIRD_FRAME_INDEX));
        assertEquals(0, mAnimationDrawable.getDuration(THIRD_FRAME_INDEX + 1));
        try {
            mAnimationDrawable.getDuration(-1);
            fail("Should throw ArrayIndexOutOfBoundsException.");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            mAnimationDrawable.getDuration(10);
            fail("Should throw ArrayIndexOutOfBoundsException.");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isOneShot",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setOneShot",
            args = {boolean.class}
        )
    })
    public void testAccessOneShot() throws Throwable {
        assertFalse(mAnimationDrawable.isOneShot());
        runTestOnUiThread(new Runnable() {
            public void run() {
                mAnimationDrawable.start();
            }
        });
        delayedCheckDrawable(SECOND_FRAME_INDEX, FIRST_FRAME_DURATION);
        delayedCheckDrawable(THIRD_FRAME_INDEX, SECOND_FRAME_DURATION);
        delayedCheckDrawable(FIRST_FRAME_INDEX, THIRD_FRAME_DURATION);
        runTestOnUiThread(new Runnable() {
            public void run() {
                mAnimationDrawable.stop();
                mAnimationDrawable.setOneShot(true);
                assertTrue(mAnimationDrawable.isOneShot());
                mAnimationDrawable.start();
            }
        });
        delayedCheckDrawable(SECOND_FRAME_INDEX, FIRST_FRAME_DURATION);
        delayedCheckDrawable(THIRD_FRAME_INDEX, SECOND_FRAME_DURATION);
        assertStoppedAnimation(THIRD_FRAME_INDEX, THIRD_FRAME_DURATION);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "inflate",
        args = {android.content.res.Resources.class, org.xmlpull.v1.XmlPullParser.class,
                android.util.AttributeSet.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete")
    public void testInflate() throws XmlPullParserException, IOException {
        mAnimationDrawable = new AnimationDrawable();
        DrawableContainerState drawableContainerState =
            (DrawableContainerState) mAnimationDrawable.getConstantState();
        XmlResourceParser parser = getResourceParser(R.xml.anim_list_correct);
        mAnimationDrawable.inflate(mResources, parser, Xml.asAttributeSet(parser));
        assertFalse(mAnimationDrawable.isVisible());
        assertTrue(mAnimationDrawable.isOneShot());
        assertNull(drawableContainerState.getConstantPadding());
        assertEquals(2, mAnimationDrawable.getNumberOfFrames());
        assertEquals(2000, mAnimationDrawable.getDuration(0));
        assertEquals(1000, mAnimationDrawable.getDuration(1));
        assertSame(mAnimationDrawable.getFrame(0), mAnimationDrawable.getCurrent());
        parser = getResourceParser(R.xml.anim_list_missing_list_attrs);
        mAnimationDrawable.inflate(mResources, parser, Xml.asAttributeSet(parser));
        assertFalse(mAnimationDrawable.isVisible());
        assertFalse(mAnimationDrawable.isOneShot());
        assertEquals(3, mAnimationDrawable.getNumberOfFrames());
        assertEquals(2000, mAnimationDrawable.getDuration(0));
        assertEquals(1000, mAnimationDrawable.getDuration(1));
        assertEquals(2000, mAnimationDrawable.getDuration(2));
        assertSame(mAnimationDrawable.getFrame(0), mAnimationDrawable.getCurrent());
        parser = getResourceParser(R.xml.anim_list_missing_item_drawable);
        try {
            mAnimationDrawable.inflate(mResources, parser, Xml.asAttributeSet(parser));
            fail("Should throw XmlPullParserException if drawable of item is missing");
        } catch (XmlPullParserException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "inflate",
        args = {android.content.res.Resources.class, org.xmlpull.v1.XmlPullParser.class,
                android.util.AttributeSet.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "should add @throws clause into javadoc of "
            + "AnimationDrawable#inflate(Resources, XmlPullParser, AttributeSet) "
            + "when param r, parser or attrs is null")
    public void testInflateWithNullParameters() throws XmlPullParserException, IOException {
        XmlResourceParser parser = getResourceParser(R.drawable.animationdrawable);
        try {
            mAnimationDrawable.inflate(null, parser, Xml.asAttributeSet(parser));
            fail("Should throw NullPointerException if resource is null");
        } catch (NullPointerException e) {
        }
        try {
            mAnimationDrawable.inflate(mResources, null, Xml.asAttributeSet(parser));
            fail("Should throw NullPointerException if parser is null");
        } catch (NullPointerException e) {
        }
        try {
            mAnimationDrawable.inflate(mResources, parser, null);
            fail("Should throw NullPointerException if AttributeSet is null");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        method = "mutate",
        args = {}
    )
    @ToBeFixed(bug = "", explanation = "mutate always throws out NullPointerException")
    public void testMutate() {
        AnimationDrawable d1 = (AnimationDrawable) mResources
                .getDrawable(R.drawable.animationdrawable);
        d1.mutate();
    }
    private XmlResourceParser getResourceParser(int resId) throws XmlPullParserException,
            IOException {
        XmlResourceParser parser = mResources.getXml(resId);
        int type;
        while ((type = parser.next()) != XmlPullParser.START_TAG
                && type != XmlPullParser.END_DOCUMENT) {
        }
        return parser;
    }
    private void delayedCheckDrawable(final int index, long timeout) {
        new DelayedCheck(timeout + TOLERANCE) {
            Drawable expected = mAnimationDrawable.getFrame(index);
            @Override
            protected boolean check() {
                return mAnimationDrawable.getCurrent().equals(expected);
            }
        }.run();
    }
    private void assertStoppedAnimation(int index, long duration) throws InterruptedException {
        Thread.sleep(duration + TOLERANCE);
        assertSame(mAnimationDrawable.getFrame(index), mAnimationDrawable.getCurrent());
    }
}
