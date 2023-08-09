class OGLTextRenderer extends BufferedTextPipe {
    OGLTextRenderer(RenderQueue rq) {
        super(rq);
    }
    @Override
    protected native void drawGlyphList(int numGlyphs, boolean usePositions,
                                        boolean subPixPos, boolean rgbOrder,
                                        int lcdContrast,
                                        float glOrigX, float glOrigY,
                                        long[] images, float[] positions);
    @Override
    protected void validateContext(SunGraphics2D sg2d, Composite comp) {
        OGLSurfaceData oglDst = (OGLSurfaceData)sg2d.surfaceData;
        OGLContext.validateContext(oglDst, oglDst,
                                   sg2d.getCompClip(), comp,
                                   null, sg2d.paint, sg2d,
                                   OGLContext.NO_CONTEXT_FLAGS);
    }
    OGLTextRenderer traceWrap() {
        return new Tracer(this);
    }
    private static class Tracer extends OGLTextRenderer {
        Tracer(OGLTextRenderer ogltr) {
            super(ogltr.rq);
        }
        protected void drawGlyphList(SunGraphics2D sg2d, GlyphList gl) {
            GraphicsPrimitive.tracePrimitive("OGLDrawGlyphs");
            super.drawGlyphList(sg2d, gl);
        }
    }
}
