final class IconUtilities {
    private static final String TAG = "IconUtilities";
    private static final int sColors[] = { 0xffff0000, 0xff00ff00, 0xff0000ff };
    private int mIconWidth = -1;
    private int mIconHeight = -1;
    private int mIconTextureWidth = -1;
    private int mIconTextureHeight = -1;
    private final Paint mPaint = new Paint();
    private final Paint mBlurPaint = new Paint();
    private final Paint mGlowColorPressedPaint = new Paint();
    private final Paint mGlowColorFocusedPaint = new Paint();
    private final Rect mOldBounds = new Rect();
    private final Canvas mCanvas = new Canvas();
    private final DisplayMetrics mDisplayMetrics;
    private int mColorIndex = 0;
    public IconUtilities(Context context) {
        final Resources resources = context.getResources();
        DisplayMetrics metrics = mDisplayMetrics = resources.getDisplayMetrics();
        final float density = metrics.density;
        final float blurPx = 5 * density;
        mIconWidth = mIconHeight = (int) resources.getDimension(android.R.dimen.app_icon_size);
        mIconTextureWidth = mIconTextureHeight = mIconWidth + (int)(blurPx*2);
        mBlurPaint.setMaskFilter(new BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL));
        mGlowColorPressedPaint.setColor(0xffffc300);
        mGlowColorPressedPaint.setMaskFilter(TableMaskFilter.CreateClipTable(0, 30));
        mGlowColorFocusedPaint.setColor(0xffff8e00);
        mGlowColorFocusedPaint.setMaskFilter(TableMaskFilter.CreateClipTable(0, 30));
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0.2f);
        mCanvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
                Paint.FILTER_BITMAP_FLAG));
    }
    public Drawable createIconDrawable(Drawable src) {
        Bitmap scaled = createIconBitmap(src);
        StateListDrawable result = new StateListDrawable();
        result.addState(new int[] { android.R.attr.state_focused },
                new BitmapDrawable(createSelectedBitmap(scaled, false)));
        result.addState(new int[] { android.R.attr.state_pressed },
                new BitmapDrawable(createSelectedBitmap(scaled, true)));
        result.addState(new int[0], new BitmapDrawable(scaled));
        result.setBounds(0, 0, mIconTextureWidth, mIconTextureHeight);
        return result;
    }
    private Bitmap createIconBitmap(Drawable icon) {
        int width = mIconWidth;
        int height = mIconHeight;
        if (icon instanceof PaintDrawable) {
            PaintDrawable painter = (PaintDrawable) icon;
            painter.setIntrinsicWidth(width);
            painter.setIntrinsicHeight(height);
        } else if (icon instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (bitmap.getDensity() == Bitmap.DENSITY_NONE) {
                bitmapDrawable.setTargetDensity(mDisplayMetrics);
            }
        }
        int sourceWidth = icon.getIntrinsicWidth();
        int sourceHeight = icon.getIntrinsicHeight();
        if (sourceWidth > 0 && sourceWidth > 0) {
            if (width < sourceWidth || height < sourceHeight) {
                final float ratio = (float) sourceWidth / sourceHeight;
                if (sourceWidth > sourceHeight) {
                    height = (int) (width / ratio);
                } else if (sourceHeight > sourceWidth) {
                    width = (int) (height * ratio);
                }
            } else if (sourceWidth < width && sourceHeight < height) {
                width = sourceWidth;
                height = sourceHeight;
            }
        }
        int textureWidth = mIconTextureWidth;
        int textureHeight = mIconTextureHeight;
        final Bitmap bitmap = Bitmap.createBitmap(textureWidth, textureHeight,
                Bitmap.Config.ARGB_8888);
        final Canvas canvas = mCanvas;
        canvas.setBitmap(bitmap);
        final int left = (textureWidth-width) / 2;
        final int top = (textureHeight-height) / 2;
        if (false) {
            canvas.drawColor(sColors[mColorIndex]);
            if (++mColorIndex >= sColors.length) mColorIndex = 0;
            Paint debugPaint = new Paint();
            debugPaint.setColor(0xffcccc00);
            canvas.drawRect(left, top, left+width, top+height, debugPaint);
        }
        mOldBounds.set(icon.getBounds());
        icon.setBounds(left, top, left+width, top+height);
        icon.draw(canvas);
        icon.setBounds(mOldBounds);
        return bitmap;
    }
    private Bitmap createSelectedBitmap(Bitmap src, boolean pressed) {
        final Bitmap result = Bitmap.createBitmap(mIconTextureWidth, mIconTextureHeight,
                Bitmap.Config.ARGB_8888);
        final Canvas dest = new Canvas(result);
        dest.drawColor(0, PorterDuff.Mode.CLEAR);
        int[] xy = new int[2];
        Bitmap mask = src.extractAlpha(mBlurPaint, xy);
        dest.drawBitmap(mask, xy[0], xy[1],
                pressed ? mGlowColorPressedPaint : mGlowColorFocusedPaint);
        mask.recycle();
        dest.drawBitmap(src, 0, 0, mPaint);
        return result;
    }
}
