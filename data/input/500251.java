public abstract class TextRenderer {
    public abstract void drawString(Graphics2D g, String str, float x, float y);
    public void drawString(Graphics2D g, String str, int x, int y){
        drawString(g, str, (float)x, (float)y);
    }
    public abstract void drawGlyphVector(Graphics2D g, GlyphVector glyphVector, float x, float y);
}
