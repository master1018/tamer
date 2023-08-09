public class ImageView extends View {
    private Uri mUri;
    private int mResource = 0;
    private Matrix mMatrix;
    private ScaleType mScaleType;
    private boolean mHaveFrame = false;
    private boolean mAdjustViewBounds = false;
    private int mMaxWidth = Integer.MAX_VALUE;
    private int mMaxHeight = Integer.MAX_VALUE;
    private ColorFilter mColorFilter;
    private int mAlpha = 255;
    private int mViewAlphaScale = 256;
    private boolean mColorMod = false;
    private Drawable mDrawable = null;
    private int[] mState = null;
    private boolean mMergeState = false;
    private int mLevel = 0;
    private int mDrawableWidth;
    private int mDrawableHeight;
    private Matrix mDrawMatrix = null;
    private RectF mTempSrc = new RectF();
    private RectF mTempDst = new RectF();
    private boolean mCropToPadding;
    private boolean mBaselineAligned = false;
    private static final ScaleType[] sScaleTypeArray = {
        ScaleType.MATRIX,
        ScaleType.FIT_XY,
        ScaleType.FIT_START,
        ScaleType.FIT_CENTER,
        ScaleType.FIT_END,
        ScaleType.CENTER,
        ScaleType.CENTER_CROP,
        ScaleType.CENTER_INSIDE
    };
    public ImageView(Context context) {
        super(context);
        initImageView();
    }
    public ImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public ImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initImageView();
        TypedArray a = context.obtainStyledAttributes(attrs,
                com.android.internal.R.styleable.ImageView, defStyle, 0);
        Drawable d = a.getDrawable(com.android.internal.R.styleable.ImageView_src);
        if (d != null) {
            setImageDrawable(d);
        }
        mBaselineAligned = a.getBoolean(
                com.android.internal.R.styleable.ImageView_baselineAlignBottom, false);
        setAdjustViewBounds(
            a.getBoolean(com.android.internal.R.styleable.ImageView_adjustViewBounds,
            false));
        setMaxWidth(a.getDimensionPixelSize(
                com.android.internal.R.styleable.ImageView_maxWidth, Integer.MAX_VALUE));
        setMaxHeight(a.getDimensionPixelSize(
                com.android.internal.R.styleable.ImageView_maxHeight, Integer.MAX_VALUE));
        int index = a.getInt(com.android.internal.R.styleable.ImageView_scaleType, -1);
        if (index >= 0) {
            setScaleType(sScaleTypeArray[index]);
        }
        int tint = a.getInt(com.android.internal.R.styleable.ImageView_tint, 0);
        if (tint != 0) {
            setColorFilter(tint);
        }
        mCropToPadding = a.getBoolean(
                com.android.internal.R.styleable.ImageView_cropToPadding, false);
        a.recycle();
    }
    private void initImageView() {
        mMatrix     = new Matrix();
        mScaleType  = ScaleType.FIT_CENTER;
    }
    @Override
    protected boolean verifyDrawable(Drawable dr) {
        return mDrawable == dr || super.verifyDrawable(dr);
    }
    @Override
    public void invalidateDrawable(Drawable dr) {
        if (dr == mDrawable) {
            invalidate();
        } else {
            super.invalidateDrawable(dr);
        }
    }
    @Override
    protected boolean onSetAlpha(int alpha) {
        if (getBackground() == null) {
            int scale = alpha + (alpha >> 7);
            if (mViewAlphaScale != scale) {
                mViewAlphaScale = scale;
                mColorMod = true;
                applyColorMod();
            }
            return true;
        }
        return false;
    }
    @android.view.RemotableViewMethod
    public void setAdjustViewBounds(boolean adjustViewBounds) {
        mAdjustViewBounds = adjustViewBounds;
        if (adjustViewBounds) {
            setScaleType(ScaleType.FIT_CENTER);
        }
    }
    @android.view.RemotableViewMethod
    public void setMaxWidth(int maxWidth) {
        mMaxWidth = maxWidth;
    }
    @android.view.RemotableViewMethod
    public void setMaxHeight(int maxHeight) {
        mMaxHeight = maxHeight;
    }
    public Drawable getDrawable() {
        return mDrawable;
    }
    @android.view.RemotableViewMethod
    public void setImageResource(int resId) {
        if (mUri != null || mResource != resId) {
            updateDrawable(null);
            mResource = resId;
            mUri = null;
            resolveUri();
            requestLayout();
            invalidate();
        }
    }
    @android.view.RemotableViewMethod
    public void setImageURI(Uri uri) {
        if (mResource != 0 ||
                (mUri != uri &&
                 (uri == null || mUri == null || !uri.equals(mUri)))) {
            updateDrawable(null);
            mResource = 0;
            mUri = uri;
            resolveUri();
            requestLayout();
            invalidate();
        }
    }
    public void setImageDrawable(Drawable drawable) {
        if (mDrawable != drawable) {
            mResource = 0;
            mUri = null;
            updateDrawable(drawable);
            requestLayout();
            invalidate();
        }
    }
    @android.view.RemotableViewMethod
    public void setImageBitmap(Bitmap bm) {
        setImageDrawable(new BitmapDrawable(mContext.getResources(), bm));
    }
    public void setImageState(int[] state, boolean merge) {
        mState = state;
        mMergeState = merge;
        if (mDrawable != null) {
            refreshDrawableState();
            resizeFromDrawable();
        }
    }
    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        resizeFromDrawable();
    }
    @android.view.RemotableViewMethod
    public void setImageLevel(int level) {
        mLevel = level;
        if (mDrawable != null) {
            mDrawable.setLevel(level);
            resizeFromDrawable();
        }
    }
    public enum ScaleType {
        MATRIX      (0),
        FIT_XY      (1),
        FIT_START   (2),
        FIT_CENTER  (3),
        FIT_END     (4),
        CENTER      (5),
        CENTER_CROP (6),
        CENTER_INSIDE (7);
        ScaleType(int ni) {
            nativeInt = ni;
        }
        final int nativeInt;
    }
    public void setScaleType(ScaleType scaleType) {
        if (scaleType == null) {
            throw new NullPointerException();
        }
        if (mScaleType != scaleType) {
            mScaleType = scaleType;
            setWillNotCacheDrawing(mScaleType == ScaleType.CENTER);            
            requestLayout();
            invalidate();
        }
    }
    public ScaleType getScaleType() {
        return mScaleType;
    }
    public Matrix getImageMatrix() {
        return mMatrix;
    }
    public void setImageMatrix(Matrix matrix) {
        if (matrix != null && matrix.isIdentity()) {
            matrix = null;
        }
        if (matrix == null && !mMatrix.isIdentity() ||
                matrix != null && !mMatrix.equals(matrix)) {
            mMatrix.set(matrix);
            configureBounds();
            invalidate();
        }
    }
    private void resolveUri() {
        if (mDrawable != null) {
            return;
        }
        Resources rsrc = getResources();
        if (rsrc == null) {
            return;
        }
        Drawable d = null;
        if (mResource != 0) {
            try {
                d = rsrc.getDrawable(mResource);
            } catch (Exception e) {
                Log.w("ImageView", "Unable to find resource: " + mResource, e);
                mUri = null;
            }
        } else if (mUri != null) {
            String scheme = mUri.getScheme();
            if (ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme)) {
                try {
                    ContentResolver.OpenResourceIdResult r =
                            mContext.getContentResolver().getResourceId(mUri);
                    d = r.r.getDrawable(r.id);
                } catch (Exception e) {
                    Log.w("ImageView", "Unable to open content: " + mUri, e);
                }
            } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)
                    || ContentResolver.SCHEME_FILE.equals(scheme)) {
                try {
                    d = Drawable.createFromStream(
                        mContext.getContentResolver().openInputStream(mUri),
                        null);
                } catch (Exception e) {
                    Log.w("ImageView", "Unable to open content: " + mUri, e);
                }
            } else {
                d = Drawable.createFromPath(mUri.toString());
            }
            if (d == null) {
                System.out.println("resolveUri failed on bad bitmap uri: "
                                   + mUri);
                mUri = null;
            }
        } else {
            return;
        }
        updateDrawable(d);
    }
    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        if (mState == null) {
            return super.onCreateDrawableState(extraSpace);
        } else if (!mMergeState) {
            return mState;
        } else {
            return mergeDrawableStates(
                    super.onCreateDrawableState(extraSpace + mState.length), mState);
        }
    }
    private void updateDrawable(Drawable d) {
        if (mDrawable != null) {
            mDrawable.setCallback(null);
            unscheduleDrawable(mDrawable);
        }
        mDrawable = d;
        if (d != null) {
            d.setCallback(this);
            if (d.isStateful()) {
                d.setState(getDrawableState());
            }
            d.setLevel(mLevel);
            mDrawableWidth = d.getIntrinsicWidth();
            mDrawableHeight = d.getIntrinsicHeight();
            applyColorMod();
            configureBounds();
        }
    }
    private void resizeFromDrawable() {
        Drawable d = mDrawable;
        if (d != null) {
            int w = d.getIntrinsicWidth();
            if (w < 0) w = mDrawableWidth;
            int h = d.getIntrinsicHeight();
            if (h < 0) h = mDrawableHeight;
            if (w != mDrawableWidth || h != mDrawableHeight) {
                mDrawableWidth = w;
                mDrawableHeight = h;
                requestLayout();
            }
        }
    }
    private static final Matrix.ScaleToFit[] sS2FArray = {
        Matrix.ScaleToFit.FILL,
        Matrix.ScaleToFit.START,
        Matrix.ScaleToFit.CENTER,
        Matrix.ScaleToFit.END
    };
    private static Matrix.ScaleToFit scaleTypeToScaleToFit(ScaleType st)  {
        return sS2FArray[st.nativeInt - 1];
    }    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        resolveUri();
        int w;
        int h;
        float desiredAspect = 0.0f;
        boolean resizeWidth = false;
        boolean resizeHeight = false;
        if (mDrawable == null) {
            mDrawableWidth = -1;
            mDrawableHeight = -1;
            w = h = 0;
        } else {
            w = mDrawableWidth;
            h = mDrawableHeight;
            if (w <= 0) w = 1;
            if (h <= 0) h = 1;
            if (mAdjustViewBounds) {
                int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
                int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
                resizeWidth = widthSpecMode != MeasureSpec.EXACTLY;
                resizeHeight = heightSpecMode != MeasureSpec.EXACTLY;
                desiredAspect = (float)w/(float)h;
            }
        }
        int pleft = mPaddingLeft;
        int pright = mPaddingRight;
        int ptop = mPaddingTop;
        int pbottom = mPaddingBottom;
        int widthSize;
        int heightSize;
        if (resizeWidth || resizeHeight) {
            widthSize = resolveAdjustedSize(w + pleft + pright,
                                                 mMaxWidth, widthMeasureSpec);
            heightSize = resolveAdjustedSize(h + ptop + pbottom,
                                                mMaxHeight, heightMeasureSpec);
            if (desiredAspect != 0.0f) {
                float actualAspect = (float)(widthSize - pleft - pright) /
                                        (heightSize - ptop - pbottom);
                if (Math.abs(actualAspect - desiredAspect) > 0.0000001) {
                    boolean done = false;
                    if (resizeWidth) {
                        int newWidth = (int)(desiredAspect *
                                            (heightSize - ptop - pbottom))
                                            + pleft + pright;
                        if (newWidth <= widthSize) {
                            widthSize = newWidth;
                            done = true;
                        } 
                    }
                    if (!done && resizeHeight) {
                        int newHeight = (int)((widthSize - pleft - pright)
                                            / desiredAspect) + ptop + pbottom;
                        if (newHeight <= heightSize) {
                            heightSize = newHeight;
                        } 
                    }
                }
            }
        } else {
            w += pleft + pright;
            h += ptop + pbottom;
            w = Math.max(w, getSuggestedMinimumWidth());
            h = Math.max(h, getSuggestedMinimumHeight());
            widthSize = resolveSize(w, widthMeasureSpec);
            heightSize = resolveSize(h, heightMeasureSpec);
        }
        setMeasuredDimension(widthSize, heightSize);
    }
    private int resolveAdjustedSize(int desiredSize, int maxSize,
                                   int measureSpec) {
        int result = desiredSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize =  MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                result = Math.min(desiredSize, maxSize);
                break;
            case MeasureSpec.AT_MOST:
                result = Math.min(Math.min(desiredSize, specSize), maxSize);
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
        }
        return result;
    }
    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        boolean changed = super.setFrame(l, t, r, b);
        mHaveFrame = true;
        configureBounds();
        return changed;
    }
    private void configureBounds() {
        if (mDrawable == null || !mHaveFrame) {
            return;
        }
        int dwidth = mDrawableWidth;
        int dheight = mDrawableHeight;
        int vwidth = getWidth() - mPaddingLeft - mPaddingRight;
        int vheight = getHeight() - mPaddingTop - mPaddingBottom;
        boolean fits = (dwidth < 0 || vwidth == dwidth) &&
                       (dheight < 0 || vheight == dheight);
        if (dwidth <= 0 || dheight <= 0 || ScaleType.FIT_XY == mScaleType) {
            mDrawable.setBounds(0, 0, vwidth, vheight);
            mDrawMatrix = null;
        } else {
            mDrawable.setBounds(0, 0, dwidth, dheight);
            if (ScaleType.MATRIX == mScaleType) {
                if (mMatrix.isIdentity()) {
                    mDrawMatrix = null;
                } else {
                    mDrawMatrix = mMatrix;
                }
            } else if (fits) {
                mDrawMatrix = null;
            } else if (ScaleType.CENTER == mScaleType) {
                mDrawMatrix = mMatrix;
                mDrawMatrix.setTranslate((int) ((vwidth - dwidth) * 0.5f + 0.5f),
                                         (int) ((vheight - dheight) * 0.5f + 0.5f));
            } else if (ScaleType.CENTER_CROP == mScaleType) {
                mDrawMatrix = mMatrix;
                float scale;
                float dx = 0, dy = 0;
                if (dwidth * vheight > vwidth * dheight) {
                    scale = (float) vheight / (float) dheight; 
                    dx = (vwidth - dwidth * scale) * 0.5f;
                } else {
                    scale = (float) vwidth / (float) dwidth;
                    dy = (vheight - dheight * scale) * 0.5f;
                }
                mDrawMatrix.setScale(scale, scale);
                mDrawMatrix.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));
            } else if (ScaleType.CENTER_INSIDE == mScaleType) {
                mDrawMatrix = mMatrix;
                float scale;
                float dx;
                float dy;
                if (dwidth <= vwidth && dheight <= vheight) {
                    scale = 1.0f;
                } else {
                    scale = Math.min((float) vwidth / (float) dwidth, 
                            (float) vheight / (float) dheight);
                }
                dx = (int) ((vwidth - dwidth * scale) * 0.5f + 0.5f);
                dy = (int) ((vheight - dheight * scale) * 0.5f + 0.5f);
                mDrawMatrix.setScale(scale, scale);
                mDrawMatrix.postTranslate(dx, dy);
            } else {
                mTempSrc.set(0, 0, dwidth, dheight);
                mTempDst.set(0, 0, vwidth, vheight);
                mDrawMatrix = mMatrix;
                mDrawMatrix.setRectToRect(mTempSrc, mTempDst,
                                          scaleTypeToScaleToFit(mScaleType));
            }
        }
    }
    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        Drawable d = mDrawable;
        if (d != null && d.isStateful()) {
            d.setState(getDrawableState());
        }
    }
    @Override 
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDrawable == null) {
            return; 
        }
        if (mDrawableWidth == 0 || mDrawableHeight == 0) {
            return;     
        }
        if (mDrawMatrix == null && mPaddingTop == 0 && mPaddingLeft == 0) {
            mDrawable.draw(canvas);
        } else {
            int saveCount = canvas.getSaveCount();
            canvas.save();
            if (mCropToPadding) {
                final int scrollX = mScrollX;
                final int scrollY = mScrollY;
                canvas.clipRect(scrollX + mPaddingLeft, scrollY + mPaddingTop,
                        scrollX + mRight - mLeft - mPaddingRight,
                        scrollY + mBottom - mTop - mPaddingBottom);
            }
            canvas.translate(mPaddingLeft, mPaddingTop);
            if (mDrawMatrix != null) {
                canvas.concat(mDrawMatrix);
            }
            mDrawable.draw(canvas);
            canvas.restoreToCount(saveCount);
        }
    }
    @Override
    public int getBaseline() {
        return mBaselineAligned ? getMeasuredHeight() : -1;
    }
    public final void setColorFilter(int color, PorterDuff.Mode mode) {
        setColorFilter(new PorterDuffColorFilter(color, mode));
    }
    @RemotableViewMethod
    public final void setColorFilter(int color) {
        setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }
    public final void clearColorFilter() {
        setColorFilter(null);
    }
    public void setColorFilter(ColorFilter cf) {
        if (mColorFilter != cf) {
            mColorFilter = cf;
            mColorMod = true;
            applyColorMod();
            invalidate();
        }
    }
    @RemotableViewMethod
    public void setAlpha(int alpha) {
        alpha &= 0xFF;          
        if (mAlpha != alpha) {
            mAlpha = alpha;
            mColorMod = true;
            applyColorMod();
            invalidate();
        }
    }
    private void applyColorMod() {
        if (mDrawable != null && mColorMod) {
            mDrawable = mDrawable.mutate();
            mDrawable.setColorFilter(mColorFilter);
            mDrawable.setAlpha(mAlpha * mViewAlphaScale >> 8);
        }
    }
}
