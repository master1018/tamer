public class OutlineTextRenderer implements TextPipe {
    public static final int THRESHHOLD = 100;
    public void drawChars(SunGraphics2D g2d,
                          char data[], int offset, int length,
                          int x, int y) {
        String s = new String(data, offset, length);
        drawString(g2d, s, x, y);
    }
    public void drawString(SunGraphics2D g2d, String str, double x, double y) {
        if ("".equals(str)) {
            return; 
        }
        TextLayout tl = new TextLayout(str, g2d.getFont(),
                                       g2d.getFontRenderContext());
        Shape s = tl.getOutline(AffineTransform.getTranslateInstance(x, y));
        int textAAHint = g2d.getFontInfo().aaHint;
        int prevaaHint = - 1;
        if (textAAHint != SunHints.INTVAL_TEXT_ANTIALIAS_OFF &&
            g2d.antialiasHint != SunHints.INTVAL_ANTIALIAS_ON) {
            prevaaHint = g2d.antialiasHint;
            g2d.antialiasHint =  SunHints.INTVAL_ANTIALIAS_ON;
            g2d.validatePipe();
        } else if (textAAHint == SunHints.INTVAL_TEXT_ANTIALIAS_OFF
            && g2d.antialiasHint != SunHints.INTVAL_ANTIALIAS_OFF) {
            prevaaHint = g2d.antialiasHint;
            g2d.antialiasHint =  SunHints.INTVAL_ANTIALIAS_OFF;
            g2d.validatePipe();
        }
        g2d.fill(s);
        if (prevaaHint != -1) {
             g2d.antialiasHint = prevaaHint;
             g2d.validatePipe();
        }
    }
    public void drawGlyphVector(SunGraphics2D g2d, GlyphVector gv,
                                float x, float y) {
        Shape s = gv.getOutline(x, y);
        int prevaaHint = - 1;
        FontRenderContext frc = gv.getFontRenderContext();
        boolean aa = frc.isAntiAliased();
        if (aa) {
            if (g2d.getGVFontInfo(gv.getFont(), frc).aaHint ==
                SunHints.INTVAL_TEXT_ANTIALIAS_OFF) {
                aa = false;
            }
        }
        if (aa && g2d.antialiasHint != SunHints.INTVAL_ANTIALIAS_ON) {
            prevaaHint = g2d.antialiasHint;
            g2d.antialiasHint =  SunHints.INTVAL_ANTIALIAS_ON;
            g2d.validatePipe();
        } else if (!aa && g2d.antialiasHint != SunHints.INTVAL_ANTIALIAS_OFF) {
            prevaaHint = g2d.antialiasHint;
            g2d.antialiasHint =  SunHints.INTVAL_ANTIALIAS_OFF;
            g2d.validatePipe();
        }
        g2d.fill(s);
        if (prevaaHint != -1) {
             g2d.antialiasHint = prevaaHint;
             g2d.validatePipe();
        }
    }
}
