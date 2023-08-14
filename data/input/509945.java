public class ColorDrawable extends Drawable {
    private ColorState mState;
    public ColorDrawable() {
        this(null);
    }
    public ColorDrawable(int color) {
        this(null);
        mState.mBaseColor = mState.mUseColor = color;
    }
    private ColorDrawable(ColorState state) {
        mState = new ColorState(state);
    }
    @Override
    public int getChangingConfigurations() {
        return super.getChangingConfigurations() | mState.mChangingConfigurations;
    }
    @Override
    public void draw(Canvas canvas) {
        canvas.drawColor(mState.mUseColor);
    }
    public int getAlpha() {
        return mState.mUseColor >>> 24;
    }
    public void setAlpha(int alpha) {
        alpha += alpha >> 7;   
        int baseAlpha = mState.mBaseColor >>> 24;
        int useAlpha = baseAlpha * alpha >> 8;
        mState.mUseColor = (mState.mBaseColor << 8 >>> 8) | (useAlpha << 24);
    }
    public void setColorFilter(ColorFilter colorFilter) {
    }
    public int getOpacity() {
        switch (mState.mUseColor >>> 24) {
            case 255:
                return PixelFormat.OPAQUE;
            case 0:
                return PixelFormat.TRANSPARENT;
        }
        return PixelFormat.TRANSLUCENT;
    }
    @Override
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs)
            throws XmlPullParserException, IOException {
        super.inflate(r, parser, attrs);
        TypedArray a = r.obtainAttributes(attrs, com.android.internal.R.styleable.ColorDrawable);
        int color = mState.mBaseColor;
        color = a.getColor(com.android.internal.R.styleable.ColorDrawable_color, color);
        mState.mBaseColor = mState.mUseColor = color;
        a.recycle();
    }
    @Override
    public ConstantState getConstantState() {
        mState.mChangingConfigurations = super.getChangingConfigurations();
        return mState;
    }
    final static class ColorState extends ConstantState {
        int mBaseColor; 
        int mUseColor;  
        int mChangingConfigurations;
        ColorState(ColorState state) {
            if (state != null) {
                mBaseColor = state.mBaseColor;
                mUseColor = state.mUseColor;
            }
        }
        @Override
        public Drawable newDrawable() {
            return new ColorDrawable(this);
        }
        @Override
        public Drawable newDrawable(Resources res) {
            return new ColorDrawable(this);
        }
        @Override
        public int getChangingConfigurations() {
            return mChangingConfigurations;
        }
    }
}
