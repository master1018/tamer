class OGLMaskFill extends BufferedMaskFill {
    static void register() {
        GraphicsPrimitive[] primitives = {
            new OGLMaskFill(AnyColor,                  SrcOver),
            new OGLMaskFill(OpaqueColor,               SrcNoEa),
            new OGLMaskFill(GradientPaint,             SrcOver),
            new OGLMaskFill(OpaqueGradientPaint,       SrcNoEa),
            new OGLMaskFill(LinearGradientPaint,       SrcOver),
            new OGLMaskFill(OpaqueLinearGradientPaint, SrcNoEa),
            new OGLMaskFill(RadialGradientPaint,       SrcOver),
            new OGLMaskFill(OpaqueRadialGradientPaint, SrcNoEa),
            new OGLMaskFill(TexturePaint,              SrcOver),
            new OGLMaskFill(OpaqueTexturePaint,        SrcNoEa),
        };
        GraphicsPrimitiveMgr.register(primitives);
    }
    protected OGLMaskFill(SurfaceType srcType, CompositeType compType) {
        super(OGLRenderQueue.getInstance(),
              srcType, compType, OGLSurfaceData.OpenGLSurface);
    }
    @Override
    protected native void maskFill(int x, int y, int w, int h,
                                   int maskoff, int maskscan, int masklen,
                                   byte[] mask);
    @Override
    protected void validateContext(SunGraphics2D sg2d,
                                   Composite comp, int ctxflags)
    {
        OGLSurfaceData dstData = (OGLSurfaceData)sg2d.surfaceData;
        OGLContext.validateContext(dstData, dstData,
                                   sg2d.getCompClip(), comp,
                                   null, sg2d.paint, sg2d, ctxflags);
    }
}
