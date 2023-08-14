public abstract class Drawable {
    private static final Rect ZERO_BOUNDS_RECT = new Rect();
    private int[] mStateSet = StateSet.WILD_CARD;
    private int mLevel = 0;
    private int mChangingConfigurations = 0;
    private Rect mBounds = ZERO_BOUNDS_RECT;  
     Callback mCallback = null;
    private boolean mVisible = true;
    public abstract void draw(Canvas canvas);
    public void setBounds(int left, int top, int right, int bottom) {
        Rect oldBounds = mBounds;
        if (oldBounds == ZERO_BOUNDS_RECT) {
            oldBounds = mBounds = new Rect();
        }
        if (oldBounds.left != left || oldBounds.top != top ||
                oldBounds.right != right || oldBounds.bottom != bottom) {
            mBounds.set(left, top, right, bottom);
            onBoundsChange(mBounds);
        }
    }
    public void setBounds(Rect bounds) {
        setBounds(bounds.left, bounds.top, bounds.right, bounds.bottom);
    }
    public final void copyBounds(Rect bounds) {
        bounds.set(mBounds);
    }
    public final Rect copyBounds() {
        return new Rect(mBounds);
    }
    public final Rect getBounds() {
        if (mBounds == ZERO_BOUNDS_RECT) {
            mBounds = new Rect();
        }
        return mBounds;
    }
    public void setChangingConfigurations(int configs) {
        mChangingConfigurations = configs;
    }
    public int getChangingConfigurations() {
        return mChangingConfigurations;
    }
    public void setDither(boolean dither) {}
    public void setFilterBitmap(boolean filter) {}
    public static interface Callback {
        public void invalidateDrawable(Drawable who);
        public void scheduleDrawable(Drawable who, Runnable what, long when);
        public void unscheduleDrawable(Drawable who, Runnable what);
    }
    public final void setCallback(Callback cb) {
        mCallback = cb;
    }
    public void invalidateSelf()
    {
        if (mCallback != null) {
            mCallback.invalidateDrawable(this);
        }
    }
    public void scheduleSelf(Runnable what, long when)
    {
        if (mCallback != null) {
            mCallback.scheduleDrawable(this, what, when);
        }
    }
    public void unscheduleSelf(Runnable what)
    {
        if (mCallback != null) {
            mCallback.unscheduleDrawable(this, what);
        }
    }
    public abstract void setAlpha(int alpha);
    public abstract void setColorFilter(ColorFilter cf);
    public void setColorFilter(int color, PorterDuff.Mode mode) {
        setColorFilter(new PorterDuffColorFilter(color, mode));
    }
    public void clearColorFilter() {
        setColorFilter(null);
    }
    public boolean isStateful() {
        return false;
    }
    public boolean setState(final int[] stateSet) {
        if (!Arrays.equals(mStateSet, stateSet)) {
            mStateSet = stateSet;
            return onStateChange(stateSet);
        }
        return false;
    }
    public int[] getState() {
        return mStateSet;
    }
    public Drawable getCurrent() {
        return this;
    }
    public final boolean setLevel(int level) {
        if (mLevel != level) {
            mLevel = level;
            return onLevelChange(level);
        }
        return false;
    }
    public final int getLevel() {
        return mLevel;
    }
    public boolean setVisible(boolean visible, boolean restart) {
        boolean changed = mVisible != visible;
        mVisible = visible;
        return changed;
    }
    public final boolean isVisible() {
        return mVisible;
    }
    public abstract int getOpacity();
    public static int resolveOpacity(int op1, int op2) {
        if (op1 == op2) {
            return op1;
        }
        if (op1 == PixelFormat.UNKNOWN || op2 == PixelFormat.UNKNOWN) {
            return PixelFormat.UNKNOWN;
        }
        if (op1 == PixelFormat.TRANSLUCENT || op2 == PixelFormat.TRANSLUCENT) {
            return PixelFormat.TRANSLUCENT;
        }
        if (op1 == PixelFormat.TRANSPARENT || op2 == PixelFormat.TRANSPARENT) {
            return PixelFormat.TRANSPARENT;
        }
        return PixelFormat.OPAQUE;
    }
    public Region getTransparentRegion() {
        return null;
    }
    protected boolean onStateChange(int[] state) { return false; }
    protected boolean onLevelChange(int level) { return false; }
    protected void onBoundsChange(Rect bounds) {}
    public int getIntrinsicWidth() {
        return -1;
    }
    public int getIntrinsicHeight() {
        return -1;
    }
    public int getMinimumWidth() {
        final int intrinsicWidth = getIntrinsicWidth();
        return intrinsicWidth > 0 ? intrinsicWidth : 0;
    }
    public int getMinimumHeight() {
        final int intrinsicHeight = getIntrinsicHeight();
        return intrinsicHeight > 0 ? intrinsicHeight : 0;
    }
    public boolean getPadding(Rect padding) {
        padding.set(0, 0, 0, 0);
        return false;
    }
    public Drawable mutate() {
        return this;
    }
    public static Drawable createFromStream(InputStream is, String srcName) {
        return createFromResourceStream(null, null, is, srcName, null);
    }
    public static Drawable createFromResourceStream(Resources res, TypedValue value,
            InputStream is, String srcName) {
        return createFromResourceStream(res, value, is, srcName, null);
    }
    public static Drawable createFromResourceStream(Resources res, TypedValue value,
            InputStream is, String srcName, BitmapFactory.Options opts) {
        if (is == null) {
            return null;
        }
        Rect pad = new Rect();
        if (opts == null) opts = new BitmapFactory.Options();
        opts.inScreenDensity = DisplayMetrics.DENSITY_DEVICE;
        Bitmap  bm = BitmapFactory.decodeResourceStream(res, value, is, pad, opts);
        if (bm != null) {
            byte[] np = bm.getNinePatchChunk();
            if (np == null || !NinePatch.isNinePatchChunk(np)) {
                np = null;
                pad = null;
            }
            return drawableFromBitmap(res, bm, np, pad, srcName);
        }
        return null;
    }
    public static Drawable createFromXml(Resources r, XmlPullParser parser)
            throws XmlPullParserException, IOException {
        AttributeSet attrs = Xml.asAttributeSet(parser);
        int type;
        while ((type=parser.next()) != XmlPullParser.START_TAG &&
                type != XmlPullParser.END_DOCUMENT) {
        }
        if (type != XmlPullParser.START_TAG) {
            throw new XmlPullParserException("No start tag found");
        }
        Drawable drawable = createFromXmlInner(r, parser, attrs);
        if (drawable == null) {
            throw new RuntimeException("Unknown initial tag: " + parser.getName());
        }
        return drawable;
    }
    public static Drawable createFromXmlInner(Resources r, XmlPullParser parser, AttributeSet attrs)
    throws XmlPullParserException, IOException {
        Drawable drawable;
        final String name = parser.getName();
        if (name.equals("selector")) {
            drawable = new StateListDrawable();
        } else if (name.equals("level-list")) {
            drawable = new LevelListDrawable();
        } else if (name.equals("layer-list")) {
            drawable = new LayerDrawable();
        } else if (name.equals("transition")) {
            drawable = new TransitionDrawable();
        } else if (name.equals("color")) {
            drawable = new ColorDrawable();
        } else if (name.equals("shape")) {
            drawable = new GradientDrawable();
        } else if (name.equals("scale")) {
            drawable = new ScaleDrawable();
        } else if (name.equals("clip")) {
            drawable = new ClipDrawable();
        } else if (name.equals("rotate")) {
            drawable = new RotateDrawable();
        } else if (name.equals("animated-rotate")) {
            drawable = new AnimatedRotateDrawable();            
        } else if (name.equals("animation-list")) {
            drawable = new AnimationDrawable();
        } else if (name.equals("inset")) {
            drawable = new InsetDrawable();
        } else if (name.equals("bitmap")) {
            drawable = new BitmapDrawable();
            if (r != null) {
               ((BitmapDrawable) drawable).setTargetDensity(r.getDisplayMetrics());
            }
        } else if (name.equals("nine-patch")) {
            drawable = new NinePatchDrawable();
            if (r != null) {
                ((NinePatchDrawable) drawable).setTargetDensity(r.getDisplayMetrics());
             }
        } else {
            throw new XmlPullParserException(parser.getPositionDescription() +
                    ": invalid drawable tag " + name);
        }
        drawable.inflate(r, parser, attrs);
        return drawable;
    }
    public static Drawable createFromPath(String pathName) {
        if (pathName == null) {
            return null;
        }
        Bitmap bm = BitmapFactory.decodeFile(pathName);
        if (bm != null) {
            return drawableFromBitmap(null, bm, null, null, pathName);
        }
        return null;
    }
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs)
            throws XmlPullParserException, IOException {
        TypedArray a = r.obtainAttributes(attrs, com.android.internal.R.styleable.Drawable);
        inflateWithAttributes(r, parser, a, com.android.internal.R.styleable.Drawable_visible);
        a.recycle();
    }
    void inflateWithAttributes(Resources r, XmlPullParser parser,
            TypedArray attrs, int visibleAttr)
            throws XmlPullParserException, IOException {
        mVisible = attrs.getBoolean(visibleAttr, mVisible);
    }
    public static abstract class ConstantState {
        public abstract Drawable newDrawable();
        public Drawable newDrawable(Resources res) {
            return newDrawable();
        }
        public abstract int getChangingConfigurations();
    }
    public ConstantState getConstantState() {
        return null;
    }
    private static Drawable drawableFromBitmap(Resources res, Bitmap bm, byte[] np,
            Rect pad, String srcName) {
        if (np != null) {
            return new NinePatchDrawable(res, bm, np, pad, srcName);
        }
        return new BitmapDrawable(res, bm);
    }
}
