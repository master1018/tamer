public abstract class GlyphListLoopPipe extends GlyphListPipe
    implements LoopBasedPipe
{
    protected void drawGlyphList(SunGraphics2D sg2d, GlyphList gl,
                                 int aaHint) {
        switch (aaHint) {
         case SunHints.INTVAL_TEXT_ANTIALIAS_OFF:
             sg2d.loops.drawGlyphListLoop.
                 DrawGlyphList(sg2d, sg2d.surfaceData, gl);
             return;
         case SunHints.INTVAL_TEXT_ANTIALIAS_ON:
             sg2d.loops.drawGlyphListAALoop.
                 DrawGlyphListAA(sg2d, sg2d.surfaceData, gl);
             return;
        case SunHints.INTVAL_TEXT_ANTIALIAS_LCD_HRGB:
        case SunHints.INTVAL_TEXT_ANTIALIAS_LCD_VRGB:
            sg2d.loops.drawGlyphListLCDLoop.
                DrawGlyphListLCD(sg2d,sg2d.surfaceData, gl);
            return;
        }
    }
}
