public abstract class TextHighlightingAnimation implements Runnable {
    private static final int MAX_ALPHA = 255;
    private static final int MIN_ALPHA = 50;
    private AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private DecelerateInterpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private final static DimmingSpan[] sEmptySpans = new DimmingSpan[0];
    private static final long FRAME_RATE = 50;
    private DimmingSpan mDimmingSpan;
    private Handler mHandler;
    private boolean mAnimating;
    private boolean mDimming;
    private long mTargetTime;
    private final int mDuration;
    public class TextWithHighlighting implements Spanned {
        private final DimmingSpan[] mSpans;
        private boolean mDimmingEnabled;
        private CharArrayBuffer mText;
        private int mDimmingSpanStart;
        private int mDimmingSpanEnd;
        private String mString;
        public TextWithHighlighting() {
            mSpans = new DimmingSpan[] { mDimmingSpan };
        }
        public void setText(CharArrayBuffer baseText, CharArrayBuffer highlightedText) {
            mText = baseText;
            mString = new String(mText.data, 0, mText.sizeCopied);
            int index = indexOf(baseText, highlightedText);
            if (index == 0 || index == -1) {
                mDimmingEnabled = false;
            } else {
                mDimmingEnabled = true;
                mDimmingSpanStart = 0;
                mDimmingSpanEnd = index;
            }
        }
        private int indexOf(CharArrayBuffer buffer1, CharArrayBuffer buffer2) {
            char[] string1 = buffer1.data;
            char[] string2 = buffer2.data;
            int count1 = buffer1.sizeCopied;
            int count2 = buffer2.sizeCopied;
            while (count1 > 0 && count2 > 0 && string1[count1 - 1] == string2[count2 - 1]) {
                count1--;
                count2--;
            }
            int size = count2;
            for (int i = 0; i < count1; i++) {
                if (i + size > count1) {
                    size = count1 - i;
                }
                int j;
                for (j = 0; j < size; j++) {
                    if (string1[i+j] != string2[j]) {
                        break;
                    }
                }
                if (j == size) {
                    return i;
                }
            }
            return -1;
        }
        @SuppressWarnings("unchecked")
        public <T> T[] getSpans(int start, int end, Class<T> type) {
            if (mDimmingEnabled) {
                return (T[])mSpans;
            } else {
                return (T[])sEmptySpans;
            }
        }
        public int getSpanStart(Object tag) {
            return mDimmingSpanStart;
        }
        public int getSpanEnd(Object tag) {
            return mDimmingSpanEnd;
        }
        public int getSpanFlags(Object tag) {
            return 0;
        }
        public int nextSpanTransition(int start, int limit, Class type) {
            return 0;
        }
        public char charAt(int index) {
            return mText.data[index];
        }
        public int length() {
            return mText.sizeCopied;
        }
        public CharSequence subSequence(int start, int end) {
            return new String(mText.data, start, end);
        }
        @Override
        public String toString() {
            return mString;
        }
    }
    private static class DimmingSpan extends CharacterStyle {
        private int mAlpha;
        public void setAlpha(int alpha) {
            mAlpha = alpha;
        }
        @Override
        public void updateDrawState(TextPaint ds) {
            int[] states = ds.drawableState;
            if (states != null) {
                int count = states.length;
                for (int i = 0; i < count; i++) {
                    switch (states[i]) {
                        case R.attr.state_pressed:
                        case R.attr.state_selected:
                        case R.attr.state_focused:
                            return;
                    }
                }
            }
            int color = ds.getColor();
            color = Color.argb(mAlpha, Color.red(color), Color.green(color), Color.blue(color));
            ds.setColor(color);
        }
    }
    public TextHighlightingAnimation(int duration) {
        mDuration = duration;
        mHandler = new Handler();
        mDimmingSpan = new DimmingSpan();
        mDimmingSpan.setAlpha(MAX_ALPHA);
    }
    public TextWithHighlighting createTextWithHighlighting() {
        return new TextWithHighlighting();
    }
    protected abstract void invalidate();
    public void startHighlighting() {
        startAnimation(true);
    }
    public void stopHighlighting() {
        startAnimation(false);
    }
    protected void onAnimationStarted() {
    }
    protected void onAnimationEnded() {
    }
    private void startAnimation(boolean dim) {
        if (mDimming != dim) {
            mDimming = dim;
            long now = System.currentTimeMillis();
            if (!mAnimating) {
                mAnimating = true;
                mTargetTime = now + mDuration;
                onAnimationStarted();
                mHandler.post(this);
            } else  {
                mTargetTime = (now + mDuration) - (mTargetTime - now);
            }
        }
    }
    public void run() {
        long now = System.currentTimeMillis();
        long timeLeft = mTargetTime - now;
        if (timeLeft < 0) {
            mDimmingSpan.setAlpha(mDimming ? MIN_ALPHA : MAX_ALPHA);
            mAnimating = false;
            onAnimationEnded();
            return;
        }
        float virtualTime = (float)timeLeft / mDuration;
        if (mDimming) {
            float interpolatedTime = DECELERATE_INTERPOLATOR.getInterpolation(virtualTime);
            mDimmingSpan.setAlpha((int)(MIN_ALPHA + (MAX_ALPHA-MIN_ALPHA) * interpolatedTime));
        } else {
            float interpolatedTime = ACCELERATE_INTERPOLATOR.getInterpolation(virtualTime);
            mDimmingSpan.setAlpha((int)(MIN_ALPHA + (MAX_ALPHA-MIN_ALPHA) * (1-interpolatedTime)));
        }
        invalidate();
        mHandler.postDelayed(this, FRAME_RATE);
    }
}
