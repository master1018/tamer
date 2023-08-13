public abstract class GraphicAttribute {
    public static final int TOP_ALIGNMENT = -1;
    public static final int BOTTOM_ALIGNMENT = -2;
    public static final int ROMAN_BASELINE = 0;
    public static final int CENTER_BASELINE = 1;
    public static final int HANGING_BASELINE = 2;
    private int alignment;
    protected GraphicAttribute(int align) {
        if ((align < BOTTOM_ALIGNMENT) || (align > HANGING_BASELINE)) {
            throw new IllegalArgumentException(Messages.getString("awt.198")); 
        }
        this.alignment = align;
    }
    public abstract void draw(Graphics2D graphics, float x, float y);
    public abstract float getAdvance();
    public final int getAlignment() {
        return this.alignment;
    }
    public abstract float getAscent();
    public Rectangle2D getBounds() {
        float ascent = getAscent();
        float advance = getAdvance();
        float descent = getDescent();
        return new Rectangle2D.Float(0, -ascent, advance, ascent + descent);
    }
    public abstract float getDescent();
    public GlyphJustificationInfo getJustificationInfo() {
        float advance = getAdvance();
        return new GlyphJustificationInfo(advance, false,
                GlyphJustificationInfo.PRIORITY_INTERCHAR, advance / 3, advance / 3, false,
                GlyphJustificationInfo.PRIORITY_WHITESPACE, 0, 0);
    }
}
