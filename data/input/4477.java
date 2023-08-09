public class FillRect extends GraphicsPrimitive
{
    public final static String methodSignature = "FillRect(...)".toString();
    public final static int primTypeID = makePrimTypeID();
    public static FillRect locate(SurfaceType srctype,
                                  CompositeType comptype,
                                  SurfaceType dsttype)
    {
        return (FillRect)
            GraphicsPrimitiveMgr.locate(primTypeID,
                                        srctype, comptype, dsttype);
    }
    protected FillRect(SurfaceType srctype,
                       CompositeType comptype,
                       SurfaceType dsttype)
    {
        super(methodSignature, primTypeID, srctype, comptype, dsttype);
    }
    public FillRect(long pNativePrim,
                    SurfaceType srctype,
                    CompositeType comptype,
                    SurfaceType dsttype)
    {
        super(pNativePrim, methodSignature, primTypeID, srctype, comptype, dsttype);
    }
    public native void FillRect(SunGraphics2D sg2d, SurfaceData dest,
                                int x, int y, int w, int h);
    static {
        GraphicsPrimitiveMgr.registerGeneral(new FillRect(null, null, null));
    }
    public GraphicsPrimitive makePrimitive(SurfaceType srctype,
                                           CompositeType comptype,
                                           SurfaceType dsttype)
    {
        return new General(srctype, comptype, dsttype);
    }
    public static class General extends FillRect {
        public MaskFill fillop;
        public General(SurfaceType srctype,
                       CompositeType comptype,
                       SurfaceType dsttype)
        {
            super(srctype, comptype, dsttype);
            fillop = MaskFill.locate(srctype, comptype, dsttype);
        }
        public void FillRect(SunGraphics2D sg2d, SurfaceData dest,
                             int x, int y, int w, int h)
        {
            fillop.MaskFill(sg2d, dest, sg2d.composite, x, y, w, h, null, 0, 0);
        }
    }
    public GraphicsPrimitive traceWrap() {
        return new TraceFillRect(this);
    }
    private static class TraceFillRect extends FillRect {
        FillRect target;
        public TraceFillRect(FillRect target) {
            super(target.getSourceType(),
                  target.getCompositeType(),
                  target.getDestType());
            this.target = target;
        }
        public GraphicsPrimitive traceWrap() {
            return this;
        }
        public void FillRect(SunGraphics2D sg2d, SurfaceData dest,
                             int x, int y, int w, int h)
        {
            tracePrimitive(target);
            target.FillRect(sg2d, dest, x, y, w, h);
        }
    }
}
