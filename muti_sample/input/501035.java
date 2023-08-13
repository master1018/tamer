public class Interpolator {
    public Interpolator(int valueCount) {
        mValueCount = valueCount;
        mFrameCount = 2;
        native_instance = nativeConstructor(valueCount, 2);
    }
    public Interpolator(int valueCount, int frameCount) {
        mValueCount = valueCount;
        mFrameCount = frameCount;
        native_instance = nativeConstructor(valueCount, frameCount);
    }
    public void reset(int valueCount) {
        reset(valueCount, 2);
    }
    public void reset(int valueCount, int frameCount) {
        mValueCount = valueCount;
        mFrameCount = frameCount;
        nativeReset(native_instance, valueCount, frameCount);
    }
    public final int getKeyFrameCount() {
        return mFrameCount;
    }
    public final int getValueCount() {
        return mValueCount;
    }
    public void setKeyFrame(int index, int msec, float[] values) {
        setKeyFrame(index, msec, values, null);
    }
    public void setKeyFrame(int index, int msec, float[] values, float[] blend) {
        if (index < 0 || index >= mFrameCount) {
            throw new IndexOutOfBoundsException();
        }
        if (values.length < mValueCount) {
            throw new ArrayStoreException();
        }
        if (blend != null && blend.length < 4) {
            throw new ArrayStoreException();
        }
        nativeSetKeyFrame(native_instance, index, msec, values, blend);
    }
    public void setRepeatMirror(float repeatCount, boolean mirror) {
        if (repeatCount >= 0) {
            nativeSetRepeatMirror(native_instance, repeatCount, mirror);
        }
    }
    public enum Result {
        NORMAL,
        FREEZE_START,
        FREEZE_END
    }
    public Result timeToValues(float[] values) {
        return timeToValues((int)SystemClock.uptimeMillis(), values);
    }
    public Result timeToValues(int msec, float[] values) {
        if (values != null && values.length < mValueCount) {
            throw new ArrayStoreException();
        }
        switch (nativeTimeToValues(native_instance, msec, values)) {
            case 0: return Result.NORMAL;
            case 1: return Result.FREEZE_START;
            default: return Result.FREEZE_END;
        }
    }
    @Override
    protected void finalize() throws Throwable {
        nativeDestructor(native_instance);
    }
    private int mValueCount;
    private int mFrameCount;
    private final int native_instance;
    private static native int  nativeConstructor(int valueCount, int frameCount);
    private static native void nativeDestructor(int native_instance);
    private static native void nativeReset(int native_instance, int valueCount, int frameCount);
    private static native void nativeSetKeyFrame(int native_instance, int index, int msec, float[] values, float[] blend);
    private static native void nativeSetRepeatMirror(int native_instance, float repeatCount, boolean mirror);
    private static native int  nativeTimeToValues(int native_instance, int msec, float[] values);
}
