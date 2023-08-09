public class AATextRenderer extends GlyphListLoopPipe
    implements LoopBasedPipe
{
   protected void drawGlyphList(SunGraphics2D sg2d, GlyphList gl) {
       sg2d.loops.drawGlyphListAALoop.DrawGlyphListAA(sg2d, sg2d.surfaceData,
                                                      gl);
   }
}
