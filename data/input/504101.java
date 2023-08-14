public final class GlyphMetrics {
    private float advanceX;
    private float advanceY;
    private boolean horizontal;
    private byte glyphType;
    private Rectangle2D.Float bounds;
    public static final byte STANDARD = 0;
    public static final byte LIGATURE = 1;
    public static final byte COMBINING = 2;
    public static final byte COMPONENT = 3;
    public static final byte WHITESPACE = 4;
    public GlyphMetrics(boolean horizontal, float advanceX, float advanceY, Rectangle2D bounds,
            byte glyphType) {
        this.horizontal = horizontal;
        this.advanceX = advanceX;
        this.advanceY = advanceY;
        this.bounds = new Rectangle2D.Float();
        this.bounds.setRect(bounds);
        this.glyphType = glyphType;
    }
    public GlyphMetrics(float advanceX, Rectangle2D bounds, byte glyphType) {
        this.advanceX = advanceX;
        this.advanceY = 0;
        this.horizontal = true;
        this.bounds = new Rectangle2D.Float();
        this.bounds.setRect(bounds);
        this.glyphType = glyphType;
    }
    public Rectangle2D getBounds2D() {
        return (Rectangle2D.Float)this.bounds.clone();
    }
    public boolean isWhitespace() {
        return ((this.glyphType & 4) == WHITESPACE);
    }
    public boolean isStandard() {
        return ((this.glyphType & 3) == STANDARD);
    }
    public boolean isLigature() {
        return ((this.glyphType & 3) == LIGATURE);
    }
    public boolean isComponent() {
        return ((this.glyphType & 3) == COMPONENT);
    }
    public boolean isCombining() {
        return ((this.glyphType & 3) == COMBINING);
    }
    public int getType() {
        return this.glyphType;
    }
    public float getRSB() {
        if (this.horizontal) {
            return this.advanceX - this.bounds.x - (float)this.bounds.getWidth();
        }
        return this.advanceY - this.bounds.y - (float)this.bounds.getHeight();
    }
    public float getLSB() {
        if (this.horizontal) {
            return this.bounds.x;
        }
        return this.bounds.y;
    }
    public float getAdvanceY() {
        return this.advanceY;
    }
    public float getAdvanceX() {
        return this.advanceX;
    }
    public float getAdvance() {
        if (this.horizontal) {
            return this.advanceX;
        }
        return this.advanceY;
    }
}
