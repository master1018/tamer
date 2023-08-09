public class FillParallelogram extends GraphicsPrimitive
{
    public final static String methodSignature =
        "FillParallelogram(...)".toString();
    public final static int primTypeID = makePrimTypeID();
    public static FillParallelogram locate(SurfaceType srctype,
                                           CompositeType comptype,
                                           SurfaceType dsttype)
    {
        return (FillParallelogram)
            GraphicsPrimitiveMgr.locate(primTypeID,
                                        srctype, comptype, dsttype);
    }
    protected FillParallelogram(SurfaceType srctype,
                                CompositeType comptype,
                                SurfaceType dsttype)
    {
        super(methodSignature, primTypeID,
              srctype, comptype, dsttype);
    }
    public FillParallelogram(long pNativePrim,
                             SurfaceType srctype,
                             CompositeType comptype,
                             SurfaceType dsttype)
    {
        super(pNativePrim, methodSignature, primTypeID,
              srctype, comptype, dsttype);
    }
    public native void FillParallelogram(SunGraphics2D sg2d, SurfaceData dest,
                                         double x0, double y0,
                                         double dx1, double dy1,
                                         double dx2, double dy2);
    public GraphicsPrimitive makePrimitive(SurfaceType srctype,
                                           CompositeType comptype,
                                           SurfaceType dsttype)
    {
        throw new InternalError("FillParallelogram not implemented for "+
                                srctype+" with "+comptype);
    }
    public GraphicsPrimitive traceWrap() {
        return new TraceFillParallelogram(this);
    }
    private static class TraceFillParallelogram extends FillParallelogram {
        FillParallelogram target;
        public TraceFillParallelogram(FillParallelogram target) {
            super(target.getSourceType(),
                  target.getCompositeType(),
                  target.getDestType());
            this.target = target;
        }
        public GraphicsPrimitive traceWrap() {
            return this;
        }
        public void FillParallelogram(SunGraphics2D sg2d, SurfaceData dest,
                                      double x0, double y0,
                                      double dx1, double dy1,
                                      double dx2, double dy2)
        {
            tracePrimitive(target);
            target.FillParallelogram(sg2d, dest, x0, y0, dx1, dy1, dx2, dy2);
        }
    }
}
