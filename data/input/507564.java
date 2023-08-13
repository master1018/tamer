public class RoundRectShape extends RectShape {
    private float[] mOuterRadii;
    private RectF   mInset;
    private float[] mInnerRadii;
    private RectF mInnerRect;
    private Path  mPath;    
    public RoundRectShape(float[] outerRadii, RectF inset,
                          float[] innerRadii) {
        if (outerRadii.length < 8) {
            throw new ArrayIndexOutOfBoundsException(
                                        "outer radii must have >= 8 values");
        }
        if (innerRadii != null && innerRadii.length < 8) {
            throw new ArrayIndexOutOfBoundsException(
                                        "inner radii must have >= 8 values");
        }
        mOuterRadii = outerRadii;
        mInset = inset;
        mInnerRadii = innerRadii;
        if (inset != null) {
            mInnerRect = new RectF();
        }
        mPath = new Path();
    }
    @Override
    public void draw(Canvas canvas, Paint paint) {
        canvas.drawPath(mPath, paint);
    }
    @Override
    protected void onResize(float w, float h) {
        super.onResize(w, h);
        RectF r = rect();
        mPath.reset();
        if (mOuterRadii != null) {
            mPath.addRoundRect(r, mOuterRadii, Path.Direction.CW);
        } else {
            mPath.addRect(r, Path.Direction.CW);
        }
        if (mInnerRect != null) {
            mInnerRect.set(r.left + mInset.left, r.top + mInset.top,
                           r.right - mInset.right, r.bottom - mInset.bottom);
            if (mInnerRect.width() < w && mInnerRect.height() < h) {
                if (mInnerRadii != null) {
                    mPath.addRoundRect(mInnerRect, mInnerRadii,
                                       Path.Direction.CCW);
                } else {
                    mPath.addRect(mInnerRect, Path.Direction.CCW);
                }
            }
        }
    }
    @Override
    public RoundRectShape clone() throws CloneNotSupportedException {
        RoundRectShape shape = (RoundRectShape) super.clone();
        shape.mOuterRadii = mOuterRadii.clone();
        shape.mInnerRadii = mInnerRadii.clone();
        shape.mInset = new RectF(mInset);
        shape.mInnerRect = new RectF(mInnerRect);
        shape.mPath = new Path(mPath);
        return shape;
    }
}
