public final class FloatAnim {
    private float mValue;
    private float mDelta;
    private float mDuration;
    private long mStartTime;
    public FloatAnim(float value) {
        mValue = value;
        mStartTime = 0;
    }
    public boolean isAnimating() {
        return mStartTime != 0;
    }
    public float getTimeRemaining(long currentTime) {
        float duration = (currentTime - mStartTime) * 0.001f;
        if (mDuration > duration) 
            return mDuration - duration;
        else
            return 0.0f;
    }
    public float getValue(long currentTime) {
        if (mStartTime == 0) {
            return mValue;
        } else {
            return getInterpolatedValue(currentTime);
        }
    }
    public void animateValue(float value, float duration, long currentTime) {
        mDelta = getValue(currentTime) - value;
        mValue = value;
        mDuration = duration;
        mStartTime = currentTime;
    }
    public void setValue(float value) {
        mValue = value;
        mStartTime = 0;
    }
    public void skip() {
        mStartTime = 0;
    }
    private float getInterpolatedValue(long currentTime) {
        float ratio = (float) (currentTime - mStartTime) * 0.001f / mDuration;
        if (ratio >= 1f) { 
            mStartTime = 0;
            return mValue;
        } else {
            ratio = 0.5f - 0.5f * FloatMath.cos(ratio * 3.14159265f); 
            return mValue + (1f - ratio) * mDelta;
        }
    }
}
