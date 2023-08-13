class D3DMaskBlit extends BufferedMaskBlit {
    static void register() {
        GraphicsPrimitive[] primitives = {
            new D3DMaskBlit(IntArgb,    SrcOver),
            new D3DMaskBlit(IntArgbPre, SrcOver),
            new D3DMaskBlit(IntRgb,     SrcOver),
            new D3DMaskBlit(IntRgb,     SrcNoEa),
            new D3DMaskBlit(IntBgr,     SrcOver),
            new D3DMaskBlit(IntBgr,     SrcNoEa),
        };
        GraphicsPrimitiveMgr.register(primitives);
    }
    private D3DMaskBlit(SurfaceType srcType,
                        CompositeType compType)
    {
        super(D3DRenderQueue.getInstance(),
              srcType, compType, D3DSurfaceData.D3DSurface);
    }
    @Override
    protected void validateContext(SurfaceData dstData,
                                   Composite comp, Region clip)
    {
        D3DSurfaceData d3dDst = (D3DSurfaceData)dstData;
        D3DContext.validateContext(d3dDst, d3dDst,
                                   clip, comp, null, null, null,
                                   D3DContext.NO_CONTEXT_FLAGS);
    }
}
