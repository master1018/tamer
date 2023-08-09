class OGLMaskBlit extends BufferedMaskBlit {
    static void register() {
        GraphicsPrimitive[] primitives = {
            new OGLMaskBlit(IntArgb,    SrcOver),
            new OGLMaskBlit(IntArgbPre, SrcOver),
            new OGLMaskBlit(IntRgb,     SrcOver),
            new OGLMaskBlit(IntRgb,     SrcNoEa),
            new OGLMaskBlit(IntBgr,     SrcOver),
            new OGLMaskBlit(IntBgr,     SrcNoEa),
        };
        GraphicsPrimitiveMgr.register(primitives);
    }
    private OGLMaskBlit(SurfaceType srcType,
                        CompositeType compType)
    {
        super(OGLRenderQueue.getInstance(),
              srcType, compType, OGLSurfaceData.OpenGLSurface);
    }
    @Override
    protected void validateContext(SurfaceData dstData,
                                   Composite comp, Region clip)
    {
        OGLSurfaceData oglDst = (OGLSurfaceData)dstData;
        OGLContext.validateContext(oglDst, oglDst,
                                   clip, comp, null, null, null,
                                   OGLContext.NO_CONTEXT_FLAGS);
    }
}
