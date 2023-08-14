public class PaintDrawable extends ShapeDrawable {
    public PaintDrawable() {
    }
    public PaintDrawable(int color) {
        getPaint().setColor(color);
    }
    public void setCornerRadius(float radius) {
        float[] radii = null;
        if (radius > 0) {
            radii = new float[8];
            for (int i = 0; i < 8; i++) {
                radii[i] = radius;
            }
        }
        setCornerRadii(radii);
    }
    public void setCornerRadii(float[] radii) {
        if (radii == null) {
            if (getShape() != null) {
                setShape(null);
            }
        } else {
            setShape(new RoundRectShape(radii, null, null));
        }
    }
    @Override
    protected boolean inflateTag(String name, Resources r, XmlPullParser parser,
                                 AttributeSet attrs) {
        if (name.equals("corners")) {
            TypedArray a = r.obtainAttributes(attrs,
                                        com.android.internal.R.styleable.DrawableCorners);
            int radius = a.getDimensionPixelSize(
                                com.android.internal.R.styleable.DrawableCorners_radius, 0);
            setCornerRadius(radius);
            int topLeftRadius = a.getDimensionPixelSize(
                    com.android.internal.R.styleable.DrawableCorners_topLeftRadius, radius);
            int topRightRadius = a.getDimensionPixelSize(
                    com.android.internal.R.styleable.DrawableCorners_topRightRadius, radius);
            int bottomLeftRadius = a.getDimensionPixelSize(
                com.android.internal.R.styleable.DrawableCorners_bottomLeftRadius, radius);
            int bottomRightRadius = a.getDimensionPixelSize(
                com.android.internal.R.styleable.DrawableCorners_bottomRightRadius, radius);
            if (topLeftRadius != radius || topRightRadius != radius ||
                    bottomLeftRadius != radius || bottomRightRadius != radius) {
                setCornerRadii(new float[] {
                               topLeftRadius, topLeftRadius,
                               topRightRadius, topRightRadius,
                               bottomLeftRadius, bottomLeftRadius,
                               bottomRightRadius, bottomRightRadius
                               });
            }
            a.recycle();
            return true;
        }
        return super.inflateTag(name, r, parser, attrs);
    }
}
