public class Paint {
     int     mNativePaint;
    private ColorFilter mColorFilter;
    private MaskFilter  mMaskFilter;
    private PathEffect  mPathEffect;
    private Rasterizer  mRasterizer;
    private Shader      mShader;
    private Typeface    mTypeface;
    private Xfermode    mXfermode;
    private boolean     mHasCompatScaling;
    private float       mCompatScaling;
    private float       mInvCompatScaling;
    private static final Style[] sStyleArray = {
        Style.FILL, Style.STROKE, Style.FILL_AND_STROKE
    };
    private static final Cap[] sCapArray = {
        Cap.BUTT, Cap.ROUND, Cap.SQUARE
    };
    private static final Join[] sJoinArray = {
        Join.MITER, Join.ROUND, Join.BEVEL
    };
    private static final Align[] sAlignArray = {
        Align.LEFT, Align.CENTER, Align.RIGHT
    };
    public static final int ANTI_ALIAS_FLAG     = 0x01;
    public static final int FILTER_BITMAP_FLAG  = 0x02;
    public static final int DITHER_FLAG         = 0x04;
    public static final int UNDERLINE_TEXT_FLAG = 0x08;
    public static final int STRIKE_THRU_TEXT_FLAG = 0x10;
    public static final int FAKE_BOLD_TEXT_FLAG = 0x20;
    public static final int LINEAR_TEXT_FLAG    = 0x40;
    public static final int SUBPIXEL_TEXT_FLAG  = 0x80;
    public static final int DEV_KERN_TEXT_FLAG  = 0x100;
    private static final int DEFAULT_PAINT_FLAGS = DEV_KERN_TEXT_FLAG;
    public enum Style {
        FILL            (0),
        STROKE          (1),
        FILL_AND_STROKE (2);
        Style(int nativeInt) {
            this.nativeInt = nativeInt;
        }
        final int nativeInt;
    }
    public enum Cap {
        BUTT    (0),
        ROUND   (1),
        SQUARE  (2);
        private Cap(int nativeInt) {
            this.nativeInt = nativeInt;
        }
        final int nativeInt;
    }
    public enum Join {
        MITER   (0),
        ROUND   (1),
        BEVEL   (2);
        private Join(int nativeInt) {
            this.nativeInt = nativeInt;
        }
        final int nativeInt;
    }
    public enum Align {
        LEFT    (0),
        CENTER  (1),
        RIGHT   (2);
        private Align(int nativeInt) {
            this.nativeInt = nativeInt;
        }
        final int nativeInt;
    }
    public Paint() {
        this(0);
    }
    public Paint(int flags) {
        mNativePaint = native_init();
        setFlags(flags | DEFAULT_PAINT_FLAGS);
        mCompatScaling = mInvCompatScaling = 1;
    }
    public Paint(Paint paint) {
        mNativePaint = native_initWithPaint(paint.mNativePaint);
        mHasCompatScaling = paint.mHasCompatScaling;
        mCompatScaling = paint.mCompatScaling;
        mInvCompatScaling = paint.mInvCompatScaling;
    }
    public void reset() {
        native_reset(mNativePaint);
        setFlags(DEFAULT_PAINT_FLAGS);
        mHasCompatScaling = false;
        mCompatScaling = mInvCompatScaling = 1;
    }
    public void set(Paint src) {
        if (this != src) {
            native_set(mNativePaint, src.mNativePaint);
            mColorFilter    = src.mColorFilter;
            mMaskFilter     = src.mMaskFilter;
            mPathEffect     = src.mPathEffect;
            mRasterizer     = src.mRasterizer;
            mShader         = src.mShader;
            mTypeface       = src.mTypeface;
            mXfermode       = src.mXfermode;
            mHasCompatScaling = src.mHasCompatScaling;
            mCompatScaling    = src.mCompatScaling;
            mInvCompatScaling = src.mInvCompatScaling;
        }
    }
    public void setCompatibilityScaling(float factor) {
        if (factor == 1.0) {
            mHasCompatScaling = false;
            mCompatScaling = mInvCompatScaling = 1.0f;
        } else {
            mHasCompatScaling = true;
            mCompatScaling = factor;
            mInvCompatScaling = 1.0f/factor;
        }
    }
    public native int getFlags();
    public native void setFlags(int flags);
    public final boolean isAntiAlias() {
        return (getFlags() & ANTI_ALIAS_FLAG) != 0;
    }
    public native void setAntiAlias(boolean aa);
    public final boolean isDither() {
        return (getFlags() & DITHER_FLAG) != 0;
    }
    public native void setDither(boolean dither);
    public final boolean isLinearText() {
        return (getFlags() & LINEAR_TEXT_FLAG) != 0;
    }
    public native void setLinearText(boolean linearText);
    public final boolean isSubpixelText() {
        return (getFlags() & SUBPIXEL_TEXT_FLAG) != 0;
    }
    public native void setSubpixelText(boolean subpixelText);
    public final boolean isUnderlineText() {
        return (getFlags() & UNDERLINE_TEXT_FLAG) != 0;
    }
    public native void setUnderlineText(boolean underlineText);
    public final boolean isStrikeThruText() {
        return (getFlags() & STRIKE_THRU_TEXT_FLAG) != 0;
    }
    public native void setStrikeThruText(boolean strikeThruText);
    public final boolean isFakeBoldText() {
        return (getFlags() & FAKE_BOLD_TEXT_FLAG) != 0;
    }
    public native void setFakeBoldText(boolean fakeBoldText);
    public final boolean isFilterBitmap() {
        return (getFlags() & FILTER_BITMAP_FLAG) != 0;
    }
    public native void setFilterBitmap(boolean filter);
    public Style getStyle() {
        return sStyleArray[native_getStyle(mNativePaint)];
    }
    public void setStyle(Style style) {
        native_setStyle(mNativePaint, style.nativeInt);
    }
    public native int getColor();
    public native void setColor(int color);
    public native int getAlpha();
    public native void setAlpha(int a);
    public void setARGB(int a, int r, int g, int b) {
        setColor((a << 24) | (r << 16) | (g << 8) | b);
    }
    public native float getStrokeWidth();
    public native void setStrokeWidth(float width);
    public native float getStrokeMiter();
    public native void setStrokeMiter(float miter);
    public Cap getStrokeCap() {
        return sCapArray[native_getStrokeCap(mNativePaint)];
    }
    public void setStrokeCap(Cap cap) {
        native_setStrokeCap(mNativePaint, cap.nativeInt);
    }
    public Join getStrokeJoin() {
        return sJoinArray[native_getStrokeJoin(mNativePaint)];
    }
    public void setStrokeJoin(Join join) {
        native_setStrokeJoin(mNativePaint, join.nativeInt);
    }
    public boolean getFillPath(Path src, Path dst) {
        return native_getFillPath(mNativePaint, src.ni(), dst.ni());
    }
    public Shader getShader() {
        return mShader;
    }
    public Shader setShader(Shader shader) {
        int shaderNative = 0;
        if (shader != null)
            shaderNative = shader.native_instance;
        native_setShader(mNativePaint, shaderNative);
        mShader = shader;
        return shader;
    }
    public ColorFilter getColorFilter() {
        return mColorFilter;
    }
    public ColorFilter setColorFilter(ColorFilter filter) {
        int filterNative = 0;
        if (filter != null)
            filterNative = filter.native_instance;
        native_setColorFilter(mNativePaint, filterNative);
        mColorFilter = filter;
        return filter;
    }
    public Xfermode getXfermode() {
        return mXfermode;
    }
    public Xfermode setXfermode(Xfermode xfermode) {
        int xfermodeNative = 0;
        if (xfermode != null)
            xfermodeNative = xfermode.native_instance;
        native_setXfermode(mNativePaint, xfermodeNative);
        mXfermode = xfermode;
        return xfermode;
    }
    public PathEffect getPathEffect() {
        return mPathEffect;
    }
    public PathEffect setPathEffect(PathEffect effect) {
        int effectNative = 0;
        if (effect != null) {
            effectNative = effect.native_instance;
        }
        native_setPathEffect(mNativePaint, effectNative);
        mPathEffect = effect;
        return effect;
    }
    public MaskFilter getMaskFilter() {
        return mMaskFilter;
    }
    public MaskFilter setMaskFilter(MaskFilter maskfilter) {
        int maskfilterNative = 0;
        if (maskfilter != null) {
            maskfilterNative = maskfilter.native_instance;
        }
        native_setMaskFilter(mNativePaint, maskfilterNative);
        mMaskFilter = maskfilter;
        return maskfilter;
    }
    public Typeface getTypeface() {
        return mTypeface;
    }
    public Typeface setTypeface(Typeface typeface) {
        int typefaceNative = 0;
        if (typeface != null) {
            typefaceNative = typeface.native_instance;
        }
        native_setTypeface(mNativePaint, typefaceNative);
        mTypeface = typeface;
        return typeface;
    }
    public Rasterizer getRasterizer() {
        return mRasterizer;
    }
    public Rasterizer setRasterizer(Rasterizer rasterizer) {
        int rasterizerNative = 0;
        if (rasterizer != null) {
            rasterizerNative = rasterizer.native_instance;
        }
        native_setRasterizer(mNativePaint, rasterizerNative);
        mRasterizer = rasterizer;
        return rasterizer;
    }
    public native void setShadowLayer(float radius, float dx, float dy,
                                      int color);
    public void clearShadowLayer() {
        setShadowLayer(0, 0, 0, 0);
    }
    public Align getTextAlign() {
        return sAlignArray[native_getTextAlign(mNativePaint)];
    }
    public void setTextAlign(Align align) {
        native_setTextAlign(mNativePaint, align.nativeInt);
    }
    public native float getTextSize();
    public native void setTextSize(float textSize);
    public native float getTextScaleX();
    public native void setTextScaleX(float scaleX);
    public native float getTextSkewX();
    public native void setTextSkewX(float skewX);
    public native float ascent();
    public native float descent();
    public static class FontMetrics {
        public float   top;
        public float   ascent;
        public float   descent;
        public float   bottom;
        public float   leading;
    }
    public native float getFontMetrics(FontMetrics metrics);
    public FontMetrics getFontMetrics() {
        FontMetrics fm = new FontMetrics();
        getFontMetrics(fm);
        return fm;
    }
    public static class FontMetricsInt {
        public int   top;
        public int   ascent;
        public int   descent;
        public int   bottom;
        public int   leading;
        @Override public String toString() {
            return "FontMetricsInt: top=" + top + " ascent=" + ascent +
                    " descent=" + descent + " bottom=" + bottom +
                    " leading=" + leading;
        }
    }
    public native int getFontMetricsInt(FontMetricsInt fmi);
    public FontMetricsInt getFontMetricsInt() {
        FontMetricsInt fm = new FontMetricsInt();
        getFontMetricsInt(fm);
        return fm;
    }
    public float getFontSpacing() {
        return getFontMetrics(null);
    }
    public float measureText(char[] text, int index, int count) {
        if (!mHasCompatScaling) return native_measureText(text, index, count);
        final float oldSize = getTextSize();
        setTextSize(oldSize*mCompatScaling);
        float w = native_measureText(text, index, count);
        setTextSize(oldSize);
        return w*mInvCompatScaling;
    }
    private native float native_measureText(char[] text, int index, int count);
    public float measureText(String text, int start, int end) {
        if (!mHasCompatScaling) return native_measureText(text, start, end);
        final float oldSize = getTextSize();
        setTextSize(oldSize*mCompatScaling);
        float w = native_measureText(text, start, end);
        setTextSize(oldSize);
        return w*mInvCompatScaling;
    }
    private native float native_measureText(String text, int start, int end);
    public float measureText(String text) {
        if (!mHasCompatScaling) return native_measureText(text);
        final float oldSize = getTextSize();
        setTextSize(oldSize*mCompatScaling);
        float w = native_measureText(text);
        setTextSize(oldSize);
        return w*mInvCompatScaling;
    }
    private native float native_measureText(String text);
    public float measureText(CharSequence text, int start, int end) {
        if (text instanceof String) {
            return measureText((String)text, start, end);
        }
        if (text instanceof SpannedString ||
            text instanceof SpannableString) {
            return measureText(text.toString(), start, end);
        }
        if (text instanceof GraphicsOperations) {
            return ((GraphicsOperations)text).measureText(start, end, this);
        }
        char[] buf = TemporaryBuffer.obtain(end - start);
        TextUtils.getChars(text, start, end, buf, 0);
        float result = measureText(buf, 0, end - start);
        TemporaryBuffer.recycle(buf);
        return result;
    }
    public int breakText(char[] text, int index, int count,
                                float maxWidth, float[] measuredWidth) {
        if (!mHasCompatScaling) {
            return native_breakText(text, index, count, maxWidth, measuredWidth);
        }
        final float oldSize = getTextSize();
        setTextSize(oldSize*mCompatScaling);
        int res = native_breakText(text, index, count, maxWidth*mCompatScaling,
                measuredWidth);
        setTextSize(oldSize);
        if (measuredWidth != null) measuredWidth[0] *= mInvCompatScaling;
        return res;
    }
    private native int native_breakText(char[] text, int index, int count,
                                        float maxWidth, float[] measuredWidth);
    public int breakText(CharSequence text, int start, int end,
                         boolean measureForwards,
                         float maxWidth, float[] measuredWidth) {
        if (start == 0 && text instanceof String && end == text.length()) {
            return breakText((String) text, measureForwards, maxWidth,
                             measuredWidth);
        }
        char[] buf = TemporaryBuffer.obtain(end - start);
        int result;
        TextUtils.getChars(text, start, end, buf, 0);
        if (measureForwards) {
            result = breakText(buf, 0, end - start, maxWidth, measuredWidth);
        } else {
            result = breakText(buf, 0, -(end - start), maxWidth, measuredWidth);
        }
        TemporaryBuffer.recycle(buf);
        return result;
    }
    public int breakText(String text, boolean measureForwards,
                                float maxWidth, float[] measuredWidth) {
        if (!mHasCompatScaling) {
            return native_breakText(text, measureForwards, maxWidth, measuredWidth);
        }
        final float oldSize = getTextSize();
        setTextSize(oldSize*mCompatScaling);
        int res = native_breakText(text, measureForwards, maxWidth*mCompatScaling,
                measuredWidth);
        setTextSize(oldSize);
        if (measuredWidth != null) measuredWidth[0] *= mInvCompatScaling;
        return res;
    }
    private native int native_breakText(String text, boolean measureForwards,
                                        float maxWidth, float[] measuredWidth);
    public int getTextWidths(char[] text, int index, int count,
                             float[] widths) {
        if ((index | count) < 0 || index + count > text.length
                || count > widths.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        if (!mHasCompatScaling) {
            return native_getTextWidths(mNativePaint, text, index, count, widths);
        }
        final float oldSize = getTextSize();
        setTextSize(oldSize*mCompatScaling);
        int res = native_getTextWidths(mNativePaint, text, index, count, widths);
        setTextSize(oldSize);
        for (int i=0; i<res; i++) {
            widths[i] *= mInvCompatScaling;
        }
        return res;
    }
    public int getTextWidths(CharSequence text, int start, int end,
                             float[] widths) {
        if (text instanceof String) {
            return getTextWidths((String) text, start, end, widths);
        }
        if (text instanceof SpannedString ||
            text instanceof SpannableString) {
            return getTextWidths(text.toString(), start, end, widths);
        }
        if (text instanceof GraphicsOperations) {
            return ((GraphicsOperations) text).getTextWidths(start, end,
                                                                 widths, this);
        }
        char[] buf = TemporaryBuffer.obtain(end - start);
    	TextUtils.getChars(text, start, end, buf, 0);
    	int result = getTextWidths(buf, 0, end - start, widths);
        TemporaryBuffer.recycle(buf);
    	return result;
    }
    public int getTextWidths(String text, int start, int end, float[] widths) {
        if ((start | end | (end - start) | (text.length() - end)) < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (end - start > widths.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        if (!mHasCompatScaling) {
            return native_getTextWidths(mNativePaint, text, start, end, widths);
        }
        final float oldSize = getTextSize();
        setTextSize(oldSize*mCompatScaling);
        int res = native_getTextWidths(mNativePaint, text, start, end, widths);
        setTextSize(oldSize);
        for (int i=0; i<res; i++) {
            widths[i] *= mInvCompatScaling;
        }
        return res;
    }
    public int getTextWidths(String text, float[] widths) {
        return getTextWidths(text, 0, text.length(), widths);
    }
    public void getTextPath(char[] text, int index, int count,
                            float x, float y, Path path) {
        if ((index | count) < 0 || index + count > text.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        native_getTextPath(mNativePaint, text, index, count, x, y, path.ni());
    }
    public void getTextPath(String text, int start, int end,
                            float x, float y, Path path) {
        if ((start | end | (end - start) | (text.length() - end)) < 0) {
            throw new IndexOutOfBoundsException();
        }
        native_getTextPath(mNativePaint, text, start, end, x, y, path.ni());
    }
    public void getTextBounds(String text, int start, int end, Rect bounds) {
        if ((start | end | (end - start) | (text.length() - end)) < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (bounds == null) {
            throw new NullPointerException("need bounds Rect");
        }
        nativeGetStringBounds(mNativePaint, text, start, end, bounds);
    }
    public void getTextBounds(char[] text, int index, int count, Rect bounds) {
        if ((index | count) < 0 || index + count > text.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        if (bounds == null) {
            throw new NullPointerException("need bounds Rect");
        }
        nativeGetCharArrayBounds(mNativePaint, text, index, count, bounds);
    }
    protected void finalize() throws Throwable {
        finalizer(mNativePaint);
    }
    private static native int native_init();
    private static native int native_initWithPaint(int paint);
    private static native void native_reset(int native_object);
    private static native void native_set(int native_dst, int native_src);
    private static native int native_getStyle(int native_object);
    private static native void native_setStyle(int native_object, int style);
    private static native int native_getStrokeCap(int native_object);
    private static native void native_setStrokeCap(int native_object, int cap);
    private static native int native_getStrokeJoin(int native_object);
    private static native void native_setStrokeJoin(int native_object,
                                                    int join);
    private static native boolean native_getFillPath(int native_object,
                                                     int src, int dst);
    private static native int native_setShader(int native_object, int shader);
    private static native int native_setColorFilter(int native_object,
                                                    int filter);
    private static native int native_setXfermode(int native_object,
                                                 int xfermode);
    private static native int native_setPathEffect(int native_object,
                                                   int effect);
    private static native int native_setMaskFilter(int native_object,
                                                   int maskfilter);
    private static native int native_setTypeface(int native_object,
                                                 int typeface);
    private static native int native_setRasterizer(int native_object,
                                                   int rasterizer);
    private static native int native_getTextAlign(int native_object);
    private static native void native_setTextAlign(int native_object,
                                                   int align);
    private static native float native_getFontMetrics(int native_paint,
                                                      FontMetrics metrics);
    private static native int native_getTextWidths(int native_object,
                            char[] text, int index, int count, float[] widths);
    private static native int native_getTextWidths(int native_object,
                            String text, int start, int end, float[] widths);
    private static native void native_getTextPath(int native_object,
                char[] text, int index, int count, float x, float y, int path);
    private static native void native_getTextPath(int native_object,
                String text, int start, int end, float x, float y, int path);
    private static native void nativeGetStringBounds(int nativePaint,
                                String text, int start, int end, Rect bounds);
    private static native void nativeGetCharArrayBounds(int nativePaint,
                                char[] text, int index, int count, Rect bounds);
    private static native void finalizer(int nativePaint);
}
