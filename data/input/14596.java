public class LCDTextRenderer extends GlyphListLoopPipe {
    protected void drawGlyphList(SunGraphics2D sg2d, GlyphList gl) {
        sg2d.loops.drawGlyphListLCDLoop.
            DrawGlyphListLCD(sg2d, sg2d.surfaceData, gl);
    }
}
