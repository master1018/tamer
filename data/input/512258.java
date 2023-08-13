public class ScaleDrawable extends Drawable implements Drawable.Callback {
    private ScaleState mScaleState;
    private boolean mMutated;
    private final Rect mTmpRect = new Rect();
    ScaleDrawable() {
        this(null, null);
    }
    public ScaleDrawable(Drawable drawable, int gravity, float scaleWidth, float scaleHeight) {
        this(null, null);
        mScaleState.mDrawable = drawable;
        mScaleState.mGravity = gravity;
        mScaleState.mScaleWidth = scaleWidth;
        mScaleState.mScaleHeight = scaleHeight;
        if (drawable != null) {
            drawable.setCallback(this);
        }
    }
    public Drawable getDrawable() {
        return mScaleState.mDrawable;
    }
    private static float getPercent(TypedArray a, int name) {
        String s = a.getString(name);
        if (s != null) {
            if (s.endsWith("%")) {
                String f = s.substring(0, s.length() - 1);
                return Float.parseFloat(f) / 100.0f;
            }
        }
        return -1;
    }
    @Override
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs)
            throws XmlPullParserException, IOException {
        super.inflate(r, parser, attrs);
        int type;
        TypedArray a = r.obtainAttributes(attrs, com.android.internal.R.styleable.ScaleDrawable);
        float sw = getPercent(a, com.android.internal.R.styleable.ScaleDrawable_scaleWidth);
        float sh = getPercent(a, com.android.internal.R.styleable.ScaleDrawable_scaleHeight);
        int g = a.getInt(com.android.internal.R.styleable.ScaleDrawable_scaleGravity, Gravity.LEFT);
        Drawable dr = a.getDrawable(com.android.internal.R.styleable.ScaleDrawable_drawable);
        a.recycle();
        final int outerDepth = parser.getDepth();
        while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {
            if (type != XmlPullParser.START_TAG) {
                continue;
            }
            dr = Drawable.createFromXmlInner(r, parser, attrs);
        }
        if (dr == null) {
            throw new IllegalArgumentException("No drawable specified for <scale>");
        }
        mScaleState.mDrawable = dr;
        mScaleState.mScaleWidth = sw;
        mScaleState.mScaleHeight = sh;
        mScaleState.mGravity = g;
        if (dr != null) {
            dr.setCallback(this);
        }
    }
    public void invalidateDrawable(Drawable who) {
        if (mCallback != null) {
            mCallback.invalidateDrawable(this);
        }
    }
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        if (mCallback != null) {
            mCallback.scheduleDrawable(this, what, when);
        }
    }
    public void unscheduleDrawable(Drawable who, Runnable what) {
        if (mCallback != null) {
            mCallback.unscheduleDrawable(this, what);
        }
    }
    @Override
    public void draw(Canvas canvas) {
        if (mScaleState.mDrawable.getLevel() != 0)
            mScaleState.mDrawable.draw(canvas);
    }
    @Override
    public int getChangingConfigurations() {
        return super.getChangingConfigurations()
                | mScaleState.mChangingConfigurations
                | mScaleState.mDrawable.getChangingConfigurations();
    }
    @Override
    public boolean getPadding(Rect padding) {
        return mScaleState.mDrawable.getPadding(padding);
    }
    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        mScaleState.mDrawable.setVisible(visible, restart);
        return super.setVisible(visible, restart);
    }
    @Override
    public void setAlpha(int alpha) {
        mScaleState.mDrawable.setAlpha(alpha);
    }
    @Override
    public void setColorFilter(ColorFilter cf) {
        mScaleState.mDrawable.setColorFilter(cf);
    }
    @Override
    public int getOpacity() {
        return mScaleState.mDrawable.getOpacity();
    }
    @Override
    public boolean isStateful() {
        return mScaleState.mDrawable.isStateful();
    }
    @Override
    protected boolean onStateChange(int[] state) {
        boolean changed = mScaleState.mDrawable.setState(state);
        onBoundsChange(getBounds());
        return changed;
    }
    @Override
    protected boolean onLevelChange(int level) {
        mScaleState.mDrawable.setLevel(level);
        onBoundsChange(getBounds());
        invalidateSelf();
        return true;
    }
    @Override
    protected void onBoundsChange(Rect bounds) {
        final Rect r = mTmpRect;
        int level = getLevel();
        int w = bounds.width();
        final int iw = 0; 
        if (mScaleState.mScaleWidth > 0) {
            w -= (int) ((w - iw) * (10000 - level) * mScaleState.mScaleWidth / 10000);
        }
        int h = bounds.height();
        final int ih = 0; 
        if (mScaleState.mScaleHeight > 0) {
            h -= (int) ((h - ih) * (10000 - level) * mScaleState.mScaleHeight / 10000);
        }
        Gravity.apply(mScaleState.mGravity, w, h, bounds, r);
        if (w > 0 && h > 0) {
            mScaleState.mDrawable.setBounds(r.left, r.top, r.right, r.bottom);
        }
    }
    @Override
    public int getIntrinsicWidth() {
        return mScaleState.mDrawable.getIntrinsicWidth();
    }
    @Override
    public int getIntrinsicHeight() {
        return mScaleState.mDrawable.getIntrinsicHeight();
    }
    @Override
    public ConstantState getConstantState() {
        if (mScaleState.canConstantState()) {
            mScaleState.mChangingConfigurations = super.getChangingConfigurations();
            return mScaleState;
        }
        return null;
    }
    @Override
    public Drawable mutate() {
        if (!mMutated && super.mutate() == this) {
            mScaleState.mDrawable.mutate();
            mMutated = true;
        }
        return this;
    }
    final static class ScaleState extends ConstantState {
        Drawable mDrawable;
        int mChangingConfigurations;
        float mScaleWidth;
        float mScaleHeight;
        int mGravity;
        private boolean mCheckedConstantState;
        private boolean mCanConstantState;
        ScaleState(ScaleState orig, ScaleDrawable owner, Resources res) {
            if (orig != null) {
                if (res != null) {
                    mDrawable = orig.mDrawable.getConstantState().newDrawable(res);
                } else {
                    mDrawable = orig.mDrawable.getConstantState().newDrawable();
                }
                mDrawable.setCallback(owner);
                mScaleWidth = orig.mScaleWidth;
                mScaleHeight = orig.mScaleHeight;
                mGravity = orig.mGravity;
                mCheckedConstantState = mCanConstantState = true;
            }
        }
        @Override
        public Drawable newDrawable() {
            return new ScaleDrawable(this, null);
        }
        @Override
        public Drawable newDrawable(Resources res) {
            return new ScaleDrawable(this, res);
        }
        @Override
        public int getChangingConfigurations() {
            return mChangingConfigurations;
        }
        boolean canConstantState() {
            if (!mCheckedConstantState) {
                mCanConstantState = mDrawable.getConstantState() != null;
                mCheckedConstantState = true;
            }
            return mCanConstantState;
        }
    }
    private ScaleDrawable(ScaleState state, Resources res) {
        mScaleState = new ScaleState(state, this, res);
    }
}
