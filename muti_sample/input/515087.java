@TestTargetClass(Interpolator.class)
public class InterpolatorTest extends TestCase {
    private static final int DEFAULT_KEYFRAME_COUNT = 2;
    private static final float TOLERANCE = 0.1f;
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Interpolator",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Interpolator",
            args = {int.class, int.class}
        )
    })
    public void testConstructor() {
        Interpolator i = new Interpolator(10);
        assertEquals(10, i.getValueCount());
        assertEquals(DEFAULT_KEYFRAME_COUNT, i.getKeyFrameCount());
        i = new Interpolator(15, 20);
        assertEquals(15, i.getValueCount());
        assertEquals(20, i.getKeyFrameCount());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "reset",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getValueCount",
            args = {}
        )
    })
    public void testReset1() {
        final int expected = 100;
        Interpolator interpolator = new Interpolator(10);
        assertEquals(DEFAULT_KEYFRAME_COUNT, interpolator.getKeyFrameCount());
        interpolator.reset(expected);
        assertEquals(expected, interpolator.getValueCount());
        assertEquals(DEFAULT_KEYFRAME_COUNT, interpolator.getKeyFrameCount());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "reset",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getKeyFrameCount",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getValueCount",
            args = {}
        )
    })
    public void testReset2() {
        int expected1 = 100;
        int expected2 = 200;
        Interpolator interpolator = new Interpolator(10);
        interpolator.reset(expected1, expected2);
        assertEquals(expected1, interpolator.getValueCount());
        assertEquals(expected2, interpolator.getKeyFrameCount());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "timeToValues",
            args = {float[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "reset",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setKeyFrame",
            args = {int.class, int.class, float[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getValueCount",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setKeyFrame",
            args = {int.class, int.class, float[].class, float[].class}
        )
    })
    public void testTimeToValues1() throws InterruptedException {
        Interpolator interpolator = new Interpolator(1);
        assertEquals(1, interpolator.getValueCount());
        long time = SystemClock.uptimeMillis();
        interpolator.setKeyFrame(0, (int)(time - 10000), new float[] {1.0f});
        interpolator.setKeyFrame(1, (int)(time + 10000), new float[] {2.0f});
        assertValue(1.5f, Result.NORMAL, interpolator);
        interpolator.reset(1);
        time = SystemClock.uptimeMillis();
        interpolator.setKeyFrame(0, (int)(time + 1000), new float[] {2.0f});
        interpolator.setKeyFrame(1, (int)(time + 2000), new float[] {3.0f});
        assertValue(2.0f, Result.FREEZE_START, interpolator);
        interpolator.reset(1);
        time = SystemClock.uptimeMillis();
        interpolator.setKeyFrame(0, (int)(time - 2000), new float[] {2.0f});
        interpolator.setKeyFrame(1, (int)(time - 1000), new float[] {3.0f});
        assertValue(3.0f, Result.FREEZE_END, interpolator);
        final int valueCount = 2;
        interpolator.reset(valueCount);
        assertEquals(valueCount, interpolator.getValueCount());
        try {
            interpolator.timeToValues(new float[valueCount - 1]);
            fail("should throw ArrayStoreException");
        } catch (ArrayStoreException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "timeToValues",
            args = {int.class, float[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "reset",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setKeyFrame",
            args = {int.class, int.class, float[].class}
        )
    })
    @ToBeFixed(explanation="For time < 0 results may be wrong due to signed/unsigned conversion")
    public void testTimeToValues2() {
        Interpolator interpolator = new Interpolator(1);
        interpolator.setKeyFrame(0, 2000, new float[] {1.0f});
        interpolator.setKeyFrame(1, 4000, new float[] {2.0f});
        assertValue(1000, 1.0f, Result.FREEZE_START, interpolator);
        assertValue(3000, 1.5f, Result.NORMAL, interpolator);
        assertValue(6000, 2.0f, Result.FREEZE_END, interpolator);
        assertValue(-1000, 2.0f, Result.FREEZE_END, interpolator);
        interpolator.reset(1, 3);
        interpolator.setKeyFrame(0, 2000, new float[] {1.0f});
        interpolator.setKeyFrame(1, 4000, new float[] {2.0f});
        interpolator.setKeyFrame(2, 6000, new float[] {4.0f});
        assertValue(0, 1.0f, Result.FREEZE_START, interpolator);
        assertValue(3000, 1.5f, Result.NORMAL, interpolator);
        assertValue(5000, 3.0f, Result.NORMAL, interpolator);
        assertValue(8000, 4.0f, Result.FREEZE_END, interpolator);
        final int valueCount = 2;
        final int validTime = 0;
        interpolator.reset(valueCount);
        assertEquals(valueCount, interpolator.getValueCount());
        try {
            interpolator.timeToValues(validTime, new float[valueCount - 1]);
            fail("should throw out ArrayStoreException");
        } catch (ArrayStoreException e) {
        }
        interpolator.reset(2, 2);
        interpolator.setKeyFrame(0, 4000, new float[] {1.0f, 1.0f});
        interpolator.setKeyFrame(1, 6000, new float[] {2.0f, 4.0f});
        assertValues(2000, new float[] {1.0f, 1.0f}, Result.FREEZE_START, interpolator);
        assertValues(5000, new float[] {1.5f, 2.5f}, Result.NORMAL, interpolator);
        assertValues(8000, new float[] {2.0f, 4.0f}, Result.FREEZE_END, interpolator);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setKeyFrame",
            args = {int.class, int.class, float[].class, float[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setKeyFrame",
            args = {int.class, int.class, float[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setRepeatMirror",
            args = {float.class, boolean.class}
        )
    })
    @ToBeFixed(explanation="For repeat > 1 and time < start key frame, FREEZE_END and the values" +
    		" for the end key frame are returned.")
    public void testSetRepeatMirror() {
        Interpolator interpolator = new Interpolator(1, 3);
        interpolator.setKeyFrame(0, 2000, new float[] {1.0f});
        interpolator.setKeyFrame(1, 4000, new float[] {2.0f});
        interpolator.setKeyFrame(2, 6000, new float[] {4.0f});
        assertValue(1000, 1.0f, Result.FREEZE_START, interpolator);
        assertValue(3000, 1.5f, Result.NORMAL, interpolator);
        assertValue(5000, 3.0f, Result.NORMAL, interpolator);
        assertValue(7000, 4.0f, Result.FREEZE_END, interpolator);
        interpolator.setRepeatMirror(2, false);
        assertValue( 1000, 4.0f, Result.FREEZE_END, interpolator); 
        assertValue( 3000, 1.5f, Result.NORMAL, interpolator);
        assertValue( 5000, 3.0f, Result.NORMAL, interpolator);
        assertValue( 7000, 1.5f, Result.NORMAL, interpolator);
        assertValue( 9000, 3.0f, Result.NORMAL, interpolator);
        assertValue(11000, 4.0f, Result.FREEZE_END, interpolator);
        interpolator.setRepeatMirror(2, true);
        assertValue( 1000, 4.0f, Result.FREEZE_END, interpolator); 
        assertValue( 3000, 1.5f, Result.NORMAL, interpolator);
        assertValue( 5000, 3.0f, Result.NORMAL, interpolator);
        assertValue( 7000, 3.0f, Result.NORMAL, interpolator);
        assertValue( 9000, 1.5f, Result.NORMAL, interpolator);
        assertValue(11000, 4.0f, Result.FREEZE_END, interpolator);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setKeyFrame",
            args = {int.class, int.class, float[].class, float[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setKeyFrame",
            args = {int.class, int.class, float[].class}
        )
    })
    public void testSetKeyFrame() {
        final float[] aZero = new float[] {0.0f};
        final float[] aOne = new float[] {1.0f};
        Interpolator interpolator = new Interpolator(1);
        interpolator.setKeyFrame(0, 2000, aZero);
        interpolator.setKeyFrame(1, 4000, aOne);
        assertValue(1000, 0.0f, Result.FREEZE_START, interpolator);
        assertValue(3000, 0.5f, Result.NORMAL, interpolator);
        assertValue(5000, 1.0f, Result.FREEZE_END, interpolator);
        final float[] linearBlend = new float[] {
                0.0f, 0.0f, 1.0f, 1.0f
        };
        final float[] accelerateBlend = new float[] {
                0.5f, 1.0f - 0.866f, 0.866f, 0.5f
        };
        final float[] decelerateBlend = new float[] {
                1.0f - 0.866f, 0.5f, 0.5f, 0.866f
        };
        interpolator.setKeyFrame(0, 2000, aZero, linearBlend);
        interpolator.setKeyFrame(1, 4000, aOne, linearBlend);
        assertValue(1000, 0.0f, Result.FREEZE_START, interpolator);
        assertValue(3000, 0.5f, Result.NORMAL, interpolator);
        assertValue(5000, 1.0f, Result.FREEZE_END, interpolator);
        interpolator.setKeyFrame(0, 2000, aZero);
        interpolator.setKeyFrame(1, 4000, aOne, accelerateBlend);
        assertValue(1000, 0.0f, Result.FREEZE_START, interpolator);
        assertValue(3000, 0.5f, Result.NORMAL, interpolator);
        assertValue(5000, 1.0f, Result.FREEZE_END, interpolator);
        final float[] result = new float[1];
        interpolator.setKeyFrame(0, 2000, aZero, accelerateBlend);
        interpolator.setKeyFrame(1, 4000, aOne);
        assertValue(1000, 0.0f, Result.FREEZE_START, interpolator);
        assertEquals(Result.NORMAL, interpolator.timeToValues(3000, result));
        assertTrue(result[0] < 0.5f); 
        assertValue(5000, 1.0f, Result.FREEZE_END, interpolator);
        interpolator.setKeyFrame(0, 2000, aZero, decelerateBlend);
        interpolator.setKeyFrame(1, 4000, aOne);
        assertValue(1000, 0.0f, Result.FREEZE_START, interpolator);
        assertEquals(Result.NORMAL, interpolator.timeToValues(3000, result));
        assertTrue(result[0] > 0.5f); 
        assertValue(5000, 1.0f, Result.FREEZE_END, interpolator);
        final int validTime = 0;
        final int valueCount = 2;
        interpolator.reset(valueCount);
        assertEquals(valueCount, interpolator.getValueCount());
        try {
            interpolator.setKeyFrame(0, validTime, new float[valueCount - 1]);
            fail("should throw ArrayStoreException");
        } catch (ArrayStoreException e) {
        }
        try {
            interpolator.setKeyFrame(-1, validTime, new float[valueCount]);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            interpolator.setKeyFrame(2, validTime, new float[valueCount]);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            interpolator.setKeyFrame(0, validTime, new float[valueCount], new float[3]);
            fail("should throw ArrayStoreException");
        } catch (ArrayStoreException e) {
        }
    }
    private void assertValue(int time, float expected, Result expectedResult,
            Interpolator interpolator) {
        float[] values = new float[1];
        assertEquals(expectedResult, interpolator.timeToValues(time, values));
        assertEquals(expected, values[0], TOLERANCE);
    }
    private void assertValues(int time, float[] expected, Result expectedResult,
            Interpolator interpolator) {
        float[] values = new float[expected.length];
        assertEquals(expectedResult, interpolator.timeToValues(time, values));
        assertFloatArray(expected, values);
    }
    private void assertValue(float expected, Result expectedResult, Interpolator interpolator) {
        float[] values = new float[1];
        assertEquals(expectedResult, interpolator.timeToValues(values));
        assertEquals(expected, values[0], TOLERANCE);
    }
    private void assertFloatArray(float[] expected, float[] actual) {
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], actual[i], TOLERANCE);
        }
    }
}
