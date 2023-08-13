public class NinePatchDrawable extends Drawable {
    private static final boolean DEFAULT_DITHER = true;
    private NinePatchState mNinePatchState;
    private NinePatch mNinePatch;
    private Rect mPadding;
    private Paint mPaint;
    private boolean mMutated;
    private int mTargetDensity = DisplayMetrics.DENSITY_DEFAULT;
    private int mBitmapWidth;
    private int mBitmapHeight;
    NinePatchDrawable() {
    }
    @Deprecated
    public NinePatchDrawable(Bitmap bitmap, byte[] chunk, Rect padding, String srcName) {
        this(new NinePatchState(new NinePatch(bitmap, chunk, srcName), padding), null);
    }
    public NinePatchDrawable(Resources res, Bitmap bitmap, byte[] chunk,
            Rect padding, String srcName) {
        this(new NinePatchState(new NinePatch(bitmap, chunk, srcName), padding), res);
        mNinePatchState.mTargetDensity = mTargetDensity;
    }
    @Deprecated
    public NinePatchDrawable(NinePatch patch) {
        this(new NinePatchState(patch, new Rect()), null);
    }
    public NinePatchDrawable(Resources res, NinePatch patch) {
        this(new NinePatchState(patch, new Rect()), res);
        mNinePatchState.mTargetDensity = mTargetDensity;
    }
    private void setNinePatchState(NinePatchState state, Resources res) {
        mNinePatchState = state;
        mNinePatch = state.mNinePatch;
        mPadding = state.mPadding;
        mTargetDensity = res != null ? res.getDisplayMetrics().densityDpi
                : state.mTargetDensity;
        if (DEFAULT_DITHER != state.mDither) {
            setDither(state.mDither);
        }
        if (mNinePatch != null) {
            computeBitmapSize();
        }
    }
    public void setTargetDensity(Canvas canvas) {
        setTargetDensity(canvas.getDensity());
    }
    public void setTargetDensity(DisplayMetrics metrics) {
        mTargetDensity = metrics.densityDpi;
        if (mNinePatch != null) {
            computeBitmapSize();
        }
    }
    public void setTargetDensity(int density) {
        mTargetDensity = density == 0 ? DisplayMetrics.DENSITY_DEFAULT : density;
        if (mNinePatch != null) {
            computeBitmapSize();
        }
    }
    private void computeBitmapSize() {
        final int sdensity = mNinePatch.getDensity();
        final int tdensity = mTargetDensity;
        if (sdensity == tdensity) {
            mBitmapWidth = mNinePatch.getWidth();
            mBitmapHeight = mNinePatch.getHeight();
        } else {
            mBitmapWidth = Bitmap.scaleFromDensity(mNinePatch.getWidth(),
                    sdensity, tdensity);
            mBitmapHeight = Bitmap.scaleFromDensity(mNinePatch.getHeight(),
                    sdensity, tdensity);
            Rect dest = mPadding;
            Rect src = mNinePatchState.mPadding;
            if (dest == src) {
                mPadding = dest = new Rect(src);
            }
            dest.left = Bitmap.scaleFromDensity(src.left, sdensity, tdensity);
            dest.top = Bitmap.scaleFromDensity(src.top, sdensity, tdensity);
            dest.right = Bitmap.scaleFromDensity(src.right, sdensity, tdensity);
            dest.bottom = Bitmap.scaleFromDensity(src.bottom, sdensity, tdensity);
        }
    }
    @Override
    public void draw(Canvas canvas) {
        if (false) {
            float[] pts = new float[2];
            canvas.getMatrix().mapPoints(pts);
            Log.v("9patch", "Drawing 9-patch @ " + pts[0] + "," + pts[1] + ": " + getBounds());
        }
        mNinePatch.draw(canvas, getBounds(), mPaint);
    }
    @Override
    public int getChangingConfigurations() {
        return super.getChangingConfigurations() | mNinePatchState.mChangingConfigurations;
    }
    @Override
    public boolean getPadding(Rect padding) {
        padding.set(mPadding);
        return true;
    }
    @Override
    public void setAlpha(int alpha) {
        getPaint().setAlpha(alpha);
    }
    @Override
    public void setColorFilter(ColorFilter cf) {
        getPaint().setColorFilter(cf);
    }
    @Override
    public void setDither(boolean dither) {
        getPaint().setDither(dither);
    }
    @Override
    public void setFilterBitmap(boolean filter) {
    }
    @Override
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs)
            throws XmlPullParserException, IOException {
        super.inflate(r, parser, attrs);
        TypedArray a = r.obtainAttributes(attrs, com.android.internal.R.styleable.NinePatchDrawable);
        final int id = a.getResourceId(com.android.internal.R.styleable.NinePatchDrawable_src, 0);
        if (id == 0) {
            throw new XmlPullParserException(parser.getPositionDescription() +
                    ": <nine-patch> requires a valid src attribute");
        }
        final boolean dither = a.getBoolean(
                com.android.internal.R.styleable.NinePatchDrawable_dither,
                DEFAULT_DITHER);
        final BitmapFactory.Options options = new BitmapFactory.Options();
        if (dither) {
            options.inDither = false;
        }
        options.inScreenDensity = DisplayMetrics.DENSITY_DEVICE;
        final Rect padding = new Rect();        
        Bitmap bitmap = null;
        try {
            final TypedValue value = new TypedValue();
            final InputStream is = r.openRawResource(id, value);
            bitmap = BitmapFactory.decodeResourceStream(r, value, is, padding, options);
            is.close();
        } catch (IOException e) {
        }
        if (bitmap == null) {
            throw new XmlPullParserException(parser.getPositionDescription() +
                    ": <nine-patch> requires a valid src attribute");
        } else if (bitmap.getNinePatchChunk() == null) {
            throw new XmlPullParserException(parser.getPositionDescription() +
                    ": <nine-patch> requires a valid 9-patch source image");
        }
        setNinePatchState(new NinePatchState(
                new NinePatch(bitmap, bitmap.getNinePatchChunk(), "XML 9-patch"),
                padding, dither), r);
        mNinePatchState.mTargetDensity = mTargetDensity;
        a.recycle();
    }
    public Paint getPaint() {
        if (mPaint == null) {
            mPaint = new Paint();
            mPaint.setDither(DEFAULT_DITHER);
        }
        return mPaint;
    }
    @Override
    public int getIntrinsicWidth() {
        return mBitmapWidth;
    }
    @Override
    public int getIntrinsicHeight() {
        return mBitmapHeight;
    }
    @Override
    public int getMinimumWidth() {
        return mBitmapWidth;
    }
    @Override
    public int getMinimumHeight() {
        return mBitmapHeight;
    }
    @Override
    public int getOpacity() {
        return mNinePatch.hasAlpha() || (mPaint != null && mPaint.getAlpha() < 255) ?
                PixelFormat.TRANSLUCENT : PixelFormat.OPAQUE;
    }
    @Override
    public Region getTransparentRegion() {
        return mNinePatch.getTransparentRegion(getBounds());
    }
    @Override
    public ConstantState getConstantState() {
        mNinePatchState.mChangingConfigurations = super.getChangingConfigurations();
        return mNinePatchState;
    }
    @Override
    public Drawable mutate() {
        if (!mMutated && super.mutate() == this) {
            mNinePatchState = new NinePatchState(mNinePatchState);
            mNinePatch = mNinePatchState.mNinePatch;
            mMutated = true;
        }
        return this;
    }
    final static class NinePatchState extends ConstantState {
        final NinePatch mNinePatch;
        final Rect mPadding;
        final boolean mDither;
        int mChangingConfigurations;
        int mTargetDensity = DisplayMetrics.DENSITY_DEFAULT;
        NinePatchState(NinePatch ninePatch, Rect padding) {
            this(ninePatch, padding, DEFAULT_DITHER);
        }
        NinePatchState(NinePatch ninePatch, Rect rect, boolean dither) {
            mNinePatch = ninePatch;
            mPadding = rect;
            mDither = dither;
        }
        NinePatchState(NinePatchState state) {
            mNinePatch = new NinePatch(state.mNinePatch);
            mPadding = state.mPadding;
            mDither = state.mDither;
            mChangingConfigurations = state.mChangingConfigurations;
            mTargetDensity = state.mTargetDensity;
        }
        @Override
        public Drawable newDrawable() {
            return new NinePatchDrawable(this, null);
        }
        @Override
        public Drawable newDrawable(Resources res) {
            return new NinePatchDrawable(this, res);
        }
        @Override
        public int getChangingConfigurations() {
            return mChangingConfigurations;
        }
    }
    private NinePatchDrawable(NinePatchState state, Resources res) {
        setNinePatchState(state, res);
    }
}
