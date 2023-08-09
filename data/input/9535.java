public abstract class BufferedTextPipe extends GlyphListPipe {
    private static final int BYTES_PER_GLYPH_IMAGE = 8;
    private static final int BYTES_PER_GLYPH_POSITION = 8;
    private static final int OFFSET_CONTRAST  = 8;
    private static final int OFFSET_RGBORDER  = 2;
    private static final int OFFSET_SUBPIXPOS = 1;
    private static final int OFFSET_POSITIONS = 0;
    private static int createPackedParams(SunGraphics2D sg2d, GlyphList gl) {
        return
            (((gl.usePositions() ? 1 : 0)   << OFFSET_POSITIONS) |
             ((gl.isSubPixPos()  ? 1 : 0)   << OFFSET_SUBPIXPOS) |
             ((gl.isRGBOrder()   ? 1 : 0)   << OFFSET_RGBORDER ) |
             ((sg2d.lcdTextContrast & 0xff) << OFFSET_CONTRAST ));
    }
    protected final RenderQueue rq;
    protected BufferedTextPipe(RenderQueue rq) {
        this.rq = rq;
    }
    @Override
    protected void drawGlyphList(SunGraphics2D sg2d, GlyphList gl) {
        Composite comp = sg2d.composite;
        if (comp == AlphaComposite.Src) {
            comp = AlphaComposite.SrcOver;
        }
        rq.lock();
        try {
            validateContext(sg2d, comp);
            enqueueGlyphList(sg2d, gl);
        } finally {
            rq.unlock();
        }
    }
    private void enqueueGlyphList(final SunGraphics2D sg2d,
                                  final GlyphList gl)
    {
        RenderBuffer buf = rq.getBuffer();
        final int totalGlyphs = gl.getNumGlyphs();
        int glyphBytesRequired = totalGlyphs * BYTES_PER_GLYPH_IMAGE;
        int posBytesRequired =
            gl.usePositions() ? totalGlyphs * BYTES_PER_GLYPH_POSITION : 0;
        int totalBytesRequired = 24 + glyphBytesRequired + posBytesRequired;
        final long[] images = gl.getImages();
        final float glyphListOrigX = gl.getX() + 0.5f;
        final float glyphListOrigY = gl.getY() + 0.5f;
        rq.addReference(gl.getStrike());
        if (totalBytesRequired <= buf.capacity()) {
            if (totalBytesRequired > buf.remaining()) {
                rq.flushNow();
            }
            rq.ensureAlignment(20);
            buf.putInt(DRAW_GLYPH_LIST);
            buf.putInt(totalGlyphs);
            buf.putInt(createPackedParams(sg2d, gl));
            buf.putFloat(glyphListOrigX);
            buf.putFloat(glyphListOrigY);
            buf.put(images, 0, totalGlyphs);
            if (gl.usePositions()) {
                float[] positions = gl.getPositions();
                buf.put(positions, 0, 2*totalGlyphs);
            }
        } else {
            rq.flushAndInvokeNow(new Runnable() {
                public void run() {
                    drawGlyphList(totalGlyphs, gl.usePositions(),
                                  gl.isSubPixPos(), gl.isRGBOrder(),
                                  sg2d.lcdTextContrast,
                                  glyphListOrigX, glyphListOrigY,
                                  images, gl.getPositions());
                }
            });
        }
    }
    protected abstract void drawGlyphList(int numGlyphs, boolean usePositions,
                                          boolean subPixPos, boolean rgbOrder,
                                          int lcdContrast,
                                          float glOrigX, float glOrigY,
                                          long[] images, float[] positions);
    protected abstract void validateContext(SunGraphics2D sg2d,
                                            Composite comp);
}
