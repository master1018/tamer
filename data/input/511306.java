public abstract class CanvasTexture {
    private int mWidth;
    private int mHeight;
    private int mTextureId;
    private int mTextureWidth;
    private int mTextureHeight;
    private float mNormalizedWidth;
    private float mNormalizedHeight;
    private final Canvas mCanvas = new Canvas();
    private final Bitmap.Config mBitmapConfig;
    private Bitmap mBitmap = null;
    private boolean mNeedsDraw = false;
    private boolean mNeedsResize = false;
    private GL11 mCachedGL = null;
    public CanvasTexture(Bitmap.Config bitmapConfig) {
        mBitmapConfig = bitmapConfig;
    }
    public final void setNeedsDraw() {
        mNeedsDraw = true;
    }
    public final int getWidth() {
        return mWidth;
    }
    public final int getHeight() {
        return mHeight;
    }
    public final float getNormalizedWidth() {
        return mNormalizedWidth;
    }
    public final float getNormalizedHeight() {
        return mNormalizedHeight;
    }
    public final void setSize(int width, int height) {
        mWidth = width;
        mHeight = height;
        mNeedsResize = true;
        mTextureWidth = -1;
        mTextureHeight = -1;
        onSizeChanged();
    }
    public void resetTexture() {
        mTextureId = 0;
        mNeedsResize = true;
    }
    public boolean bind(GL11 gl) {
        if (mCachedGL != gl) {
            mCachedGL = gl;
            resetTexture();
        }
        int width = (int) mWidth;
        int height = (int) mHeight;
        int textureId = mTextureId;
        int textureWidth = mTextureWidth;
        int textureHeight = mTextureHeight;
        Canvas canvas = mCanvas;
        Bitmap bitmap = mBitmap;
        if (mNeedsResize || mTextureId == 0) {
            mNeedsDraw = true;
            int newTextureWidth = Shared.nextPowerOf2(width);
            int newTextureHeight = Shared.nextPowerOf2(height);
            if (textureWidth != newTextureWidth || textureHeight != newTextureHeight || mTextureId == 0) {
                if (textureId == 0) {
                    int[] textureIdOut = new int[1];
                    gl.glGenTextures(1, textureIdOut, 0);
                    textureId = textureIdOut[0];
                    mNeedsResize = false;
                    mTextureId = textureId;
                    gl.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
                    gl.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP_TO_EDGE);
                    gl.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP_TO_EDGE);
                    gl.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                    gl.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
                }
                textureWidth = newTextureWidth;
                textureHeight = newTextureHeight;
                mTextureWidth = newTextureWidth;
                mTextureHeight = newTextureHeight;
                mNormalizedWidth = (float) width / textureWidth;
                mNormalizedHeight = (float) height / textureHeight;
                if (bitmap != null)
                    bitmap.recycle();
                if (textureWidth > 0 && textureHeight > 0) {
                    bitmap = Bitmap.createBitmap(textureWidth, textureHeight, mBitmapConfig);
                    canvas.setBitmap(bitmap);
                    mBitmap = bitmap;
                }
            }
        }
        if (textureId == 0) {
            return false;
        }
        gl.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        if (mNeedsDraw) {
            mNeedsDraw = false;
            renderCanvas(canvas, bitmap, width, height);
            int[] cropRect = { 0, height, width, -height };
            gl.glTexParameteriv(GL11.GL_TEXTURE_2D, GL11Ext.GL_TEXTURE_CROP_RECT_OES, cropRect, 0);
            GLUtils.texImage2D(GL11.GL_TEXTURE_2D, 0, bitmap, 0);
        }
        return true;
    }
    public void draw(RenderView view, GL11 gl, int x, int y) {
        if (bind(gl)) {
            view.draw2D(x, y, 0, mWidth, mHeight);
        }
    }
    public void drawWithEffect(RenderView view, GL11 gl, float x, float y, float anchorX, float anchorY, float alpha, float scale) {
        if (bind(gl)) {
            float width = mWidth;
            float height = mHeight;
            if (scale != 1) { 
                float originX = x + anchorX * width;
                float originY = y + anchorY * height;
                width *= scale;
                height *= scale;
                x = originX - anchorX * width;
                y = originY - anchorY * height;
            }
            if (alpha != 1f) { 
                view.setAlpha(alpha);
            }
            view.draw2D(x, y, 0, width, height);
            if (alpha != 1f) {
                view.resetColor();
            }
        }
    }
    protected abstract void onSizeChanged();
    protected abstract void renderCanvas(Canvas canvas, Bitmap backing, int width, int height);
} 
