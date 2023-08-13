public class X11TextRenderer extends GlyphListPipe {
    public void drawGlyphVector(SunGraphics2D sg2d, GlyphVector g,
                                float x, float y)
    {
        FontRenderContext frc = g.getFontRenderContext();
        FontInfo info = sg2d.getGVFontInfo(g.getFont(), frc);
        switch (info.aaHint) {
        case SunHints.INTVAL_TEXT_ANTIALIAS_OFF:
            super.drawGlyphVector(sg2d, g, x, y);
            return;
        case SunHints.INTVAL_TEXT_ANTIALIAS_ON:
             sg2d.surfaceData.aaTextRenderer.drawGlyphVector(sg2d, g, x, y);
            return;
        case SunHints.INTVAL_TEXT_ANTIALIAS_LCD_HRGB:
        case SunHints.INTVAL_TEXT_ANTIALIAS_LCD_VRGB:
             sg2d.surfaceData.lcdTextRenderer.drawGlyphVector(sg2d, g, x, y);
            return;
        default:
        }
    }
    native void doDrawGlyphList(long dstData, long xgc,
                                Region clip, GlyphList gl);
    protected void drawGlyphList(SunGraphics2D sg2d, GlyphList gl) {
        SunToolkit.awtLock();
        try {
            X11SurfaceData x11sd = (X11SurfaceData)sg2d.surfaceData;
            Region clip = sg2d.getCompClip();
            long xgc = x11sd.getRenderGC(clip, SunGraphics2D.COMP_ISCOPY,
                                         null, sg2d.pixel);
            doDrawGlyphList(x11sd.getNativeOps(), xgc, clip, gl);
        } finally {
            SunToolkit.awtUnlock();
        }
    }
    public X11TextRenderer traceWrap() {
        return new Tracer();
    }
    public static class Tracer extends X11TextRenderer {
        void doDrawGlyphList(long dstData, long xgc,
                             Region clip, GlyphList gl)
        {
            GraphicsPrimitive.tracePrimitive("X11DrawGlyphs");
            super.doDrawGlyphList(dstData, xgc, clip, gl);
        }
    }
}
