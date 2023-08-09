class D3DMaskFill extends BufferedMaskFill {
    static void register() {
        GraphicsPrimitive[] primitives = {
            new D3DMaskFill(AnyColor,                  SrcOver),
            new D3DMaskFill(OpaqueColor,               SrcNoEa),
            new D3DMaskFill(GradientPaint,             SrcOver),
            new D3DMaskFill(OpaqueGradientPaint,       SrcNoEa),
            new D3DMaskFill(LinearGradientPaint,       SrcOver),
            new D3DMaskFill(OpaqueLinearGradientPaint, SrcNoEa),
            new D3DMaskFill(RadialGradientPaint,       SrcOver),
            new D3DMaskFill(OpaqueRadialGradientPaint, SrcNoEa),
            new D3DMaskFill(TexturePaint,              SrcOver),
            new D3DMaskFill(OpaqueTexturePaint,        SrcNoEa),
        };
        GraphicsPrimitiveMgr.register(primitives);
    }
    protected D3DMaskFill(SurfaceType srcType, CompositeType compType) {
        super(D3DRenderQueue.getInstance(),
              srcType, compType, D3DSurfaceData.D3DSurface);
    }
    @Override
    protected native void maskFill(int x, int y, int w, int h,
                                   int maskoff, int maskscan, int masklen,
                                   byte[] mask);
    @Override
    protected void validateContext(SunGraphics2D sg2d,
                                   Composite comp, int ctxflags)
    {
        D3DSurfaceData dstData = (D3DSurfaceData)sg2d.surfaceData;
        D3DContext.validateContext(dstData, dstData,
                                   sg2d.getCompClip(), comp,
                                   null, sg2d.paint, sg2d, ctxflags);
    }
}
