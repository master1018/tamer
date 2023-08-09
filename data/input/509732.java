public final class ShapeGraphicAttribute extends GraphicAttribute {
    private Shape fShape;
    private boolean fStroke;
    private Rectangle2D fBounds;
    private float fOriginX;
    private float fOriginY;
    private float fShapeWidth;
    private float fShapeHeight;
    public static final boolean STROKE = true;
    public static final boolean FILL = false;
    public ShapeGraphicAttribute(Shape shape, int alignment, boolean stroke) {
        super(alignment);
        this.fShape = shape;
        this.fStroke = stroke;
        this.fBounds = fShape.getBounds2D();
        this.fOriginX = (float)fBounds.getMinX();
        this.fOriginY = (float)fBounds.getMinY();
        this.fShapeWidth = (float)fBounds.getWidth();
        this.fShapeHeight = (float)fBounds.getHeight();
    }
    @Override
    public int hashCode() {
        HashCode hash = new HashCode();
        hash.append(fShape.hashCode());
        hash.append(getAlignment());
        return hash.hashCode();
    }
    public boolean equals(ShapeGraphicAttribute sga) {
        if (sga == null) {
            return false;
        }
        if (sga == this) {
            return true;
        }
        return (fStroke == sga.fStroke && getAlignment() == sga.getAlignment() && fShape
                .equals(sga.fShape));
    }
    @Override
    public boolean equals(Object obj) {
        try {
            return equals((ShapeGraphicAttribute)obj);
        } catch (ClassCastException e) {
            return false;
        }
    }
    @Override
    public void draw(Graphics2D g2, float x, float y) {
        AffineTransform at = AffineTransform.getTranslateInstance(x, y);
        if (fStroke == STROKE) {
            Stroke oldStroke = g2.getStroke();
            g2.setStroke(new BasicStroke());
            g2.draw(at.createTransformedShape(fShape));
            g2.setStroke(oldStroke);
        } else {
            g2.fill(at.createTransformedShape(fShape));
        }
    }
    @Override
    public float getAdvance() {
        return Math.max(0, fShapeWidth + fOriginX);
    }
    @Override
    public float getAscent() {
        return Math.max(0, -fOriginY);
    }
    @Override
    public Rectangle2D getBounds() {
        return (Rectangle2D)fBounds.clone();
    }
    @Override
    public float getDescent() {
        return Math.max(0, fShapeHeight + fOriginY);
    }
}
