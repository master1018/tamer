public abstract class FontMetrics implements Serializable {
    private static final long serialVersionUID = 1681126225205050147L;
    protected Font font;
    protected FontMetrics(Font fnt) {
        this.font = fnt;
    }
    @Override
    public String toString() {
        return this.getClass().getName() + "[font=" + this.getFont() + 
                "ascent=" + this.getAscent() + 
                ", descent=" + this.getDescent() + 
                ", height=" + this.getHeight() + "]"; 
    }
    public Font getFont() {
        return font;
    }
    public int getHeight() {
        return this.getAscent() + this.getDescent() + this.getLeading();
    }
    public int getAscent() {
        return 0;
    }
    public int getDescent() {
        return 0;
    }
    public int getLeading() {
        return 0;
    }
    public LineMetrics getLineMetrics(CharacterIterator ci, int beginIndex, int limit,
            Graphics context) {
        return font.getLineMetrics(ci, beginIndex, limit, this.getFRCFromGraphics(context));
    }
    public LineMetrics getLineMetrics(String str, Graphics context) {
        return font.getLineMetrics(str, this.getFRCFromGraphics(context));
    }
    public LineMetrics getLineMetrics(char[] chars, int beginIndex, int limit, Graphics context) {
        return font.getLineMetrics(chars, beginIndex, limit, this.getFRCFromGraphics(context));
    }
    public LineMetrics getLineMetrics(String str, int beginIndex, int limit, Graphics context) {
        return font.getLineMetrics(str, beginIndex, limit, this.getFRCFromGraphics(context));
    }
    public Rectangle2D getMaxCharBounds(Graphics context) {
        return this.font.getMaxCharBounds(this.getFRCFromGraphics(context));
    }
    public Rectangle2D getStringBounds(CharacterIterator ci, int beginIndex, int limit,
            Graphics context) {
        return font.getStringBounds(ci, beginIndex, limit, this.getFRCFromGraphics(context));
    }
    public Rectangle2D getStringBounds(String str, int beginIndex, int limit, Graphics context) {
        return font.getStringBounds(str, beginIndex, limit, this.getFRCFromGraphics(context));
    }
    public Rectangle2D getStringBounds(char[] chars, int beginIndex, int limit, Graphics context) {
        return font.getStringBounds(chars, beginIndex, limit, this.getFRCFromGraphics(context));
    }
    public Rectangle2D getStringBounds(String str, Graphics context) {
        return font.getStringBounds(str, this.getFRCFromGraphics(context));
    }
    public boolean hasUniformLineMetrics() {
        return this.font.hasUniformLineMetrics();
    }
    public int bytesWidth(byte[] data, int off, int len) {
        int width = 0;
        if ((off >= data.length) || (off < 0)) {
            throw new IllegalArgumentException(Messages.getString("awt.13B")); 
        }
        if ((off + len > data.length)) {
            throw new IllegalArgumentException(Messages.getString("awt.13C")); 
        }
        for (int i = off; i < off + len; i++) {
            width += charWidth(data[i]);
        }
        return width;
    }
    public int charsWidth(char[] data, int off, int len) {
        int width = 0;
        if ((off >= data.length) || (off < 0)) {
            throw new IllegalArgumentException(Messages.getString("awt.13B")); 
        }
        if ((off + len > data.length)) {
            throw new IllegalArgumentException(Messages.getString("awt.13C")); 
        }
        for (int i = off; i < off + len; i++) {
            width += charWidth(data[i]);
        }
        return width;
    }
    public int charWidth(int ch) {
        return 0;
    }
    public int charWidth(char ch) {
        return 0;
    }
    public int getMaxAdvance() {
        return 0;
    }
    public int getMaxAscent() {
        return 0;
    }
    @Deprecated
    public int getMaxDecent() {
        return 0;
    }
    public int getMaxDescent() {
        return 0;
    }
    public int[] getWidths() {
        return null;
    }
    public int stringWidth(String str) {
        return 0;
    }
    private FontRenderContext getFRCFromGraphics(Graphics context) {
        FontRenderContext frc;
        if (context instanceof Graphics2D) {
            frc = ((Graphics2D)context).getFontRenderContext();
        } else {
            frc = new FontRenderContext(null, false, false);
        }
        return frc;
    }
}
