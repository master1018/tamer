public class BoundaryPatch {
    private Paint   mPaint;
    private Bitmap  mTexture;
    private int     mRows;
    private int     mCols;
    private float[] mCubicPoints;
    private boolean mDirty;
    private float[] mVerts;
    private short[] mIndices;
    public BoundaryPatch() {
        mRows = mCols = 2;  
        mCubicPoints = new float[24];
        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setFilterBitmap(true);
        mDirty = true;
    }
    public void setCubicBoundary(float[] pts, int offset, int rows, int cols) {
        if (rows < 2 || cols < 2) {
            throw new RuntimeException("rows and cols must be >= 2");
        }
        System.arraycopy(pts, offset, mCubicPoints, 0, 24);
        if (mRows != rows || mCols != cols) {
            mRows = rows;
            mCols = cols;
        }
        mDirty = true;
    }
    public void setTexture(Bitmap texture) {
        if (mTexture != texture) {
            if (mTexture == null ||
                    mTexture.getWidth() != texture.getWidth() ||
                    mTexture.getHeight() != texture.getHeight()) {
                mDirty = true;
            }
            mTexture = texture;
            mPaint.setShader(new BitmapShader(texture,
                                              Shader.TileMode.CLAMP,
                                              Shader.TileMode.CLAMP));
        }
    }
    public int getPaintFlags() {
        return mPaint.getFlags();
    }
    public void setPaintFlags(int flags) {
        mPaint.setFlags(flags);
    }
    public void setXfermode(Xfermode mode) {
        mPaint.setXfermode(mode);
    }
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }
    public void draw(Canvas canvas) {
        if (mDirty) {
            buildCache();
            mDirty = false;
        }
        int vertCount = mVerts.length >> 1;
        canvas.drawVertices(Canvas.VertexMode.TRIANGLES, vertCount,
                            mVerts, 0, mVerts, vertCount, null, 0,
                            mIndices, 0, mIndices.length,
                            mPaint);
    }
    private void buildCache() {
        int vertCount = mRows * mCols * 4;
        if (mVerts == null || mVerts.length != vertCount) {
            mVerts = new float[vertCount];
        }
        int indexCount = (mRows - 1) * (mCols - 1) * 6;
        if (mIndices == null || mIndices.length != indexCount) {
            mIndices = new short[indexCount];
        }
        nativeComputeCubicPatch(mCubicPoints,
                                mTexture.getWidth(), mTexture.getHeight(),
                                mRows, mCols, mVerts, mIndices);
    }
    private static native
    void nativeComputeCubicPatch(float[] cubicPoints,
                                 int texW, int texH, int rows, int cols,
                                 float[] verts, short[] indices);
}
