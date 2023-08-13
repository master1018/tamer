public class DrawParallelogram extends GraphicsPrimitive
{
    public final static String methodSignature =
        "DrawParallelogram(...)".toString();
    public final static int primTypeID = makePrimTypeID();
    public static DrawParallelogram locate(SurfaceType srctype,
                                           CompositeType comptype,
                                           SurfaceType dsttype)
    {
        return (DrawParallelogram)
            GraphicsPrimitiveMgr.locate(primTypeID,
                                        srctype, comptype, dsttype);
    }
    protected DrawParallelogram(SurfaceType srctype,
                                CompositeType comptype,
                                SurfaceType dsttype)
    {
        super(methodSignature, primTypeID,
              srctype, comptype, dsttype);
    }
    public DrawParallelogram(long pNativePrim,
                             SurfaceType srctype,
                             CompositeType comptype,
                             SurfaceType dsttype)
    {
        super(pNativePrim, methodSignature, primTypeID,
              srctype, comptype, dsttype);
    }
    public native void DrawParallelogram(SunGraphics2D sg, SurfaceData dest,
                                         double x, double y,
                                         double dx1, double dy1,
                                         double dx2, double dy2,
                                         double lw1, double lw2);
    public GraphicsPrimitive makePrimitive(SurfaceType srctype,
                                           CompositeType comptype,
                                           SurfaceType dsttype)
    {
        throw new InternalError("DrawParallelogram not implemented for "+
                                srctype+" with "+comptype);
    }
    public GraphicsPrimitive traceWrap() {
        return new TraceDrawParallelogram(this);
    }
    private static class TraceDrawParallelogram extends DrawParallelogram {
        DrawParallelogram target;
        public TraceDrawParallelogram(DrawParallelogram target) {
            super(target.getSourceType(),
                  target.getCompositeType(),
                  target.getDestType());
            this.target = target;
        }
        public GraphicsPrimitive traceWrap() {
            return this;
        }
        public void DrawParallelogram(SunGraphics2D sg2d, SurfaceData dest,
                                      double x, double y,
                                      double dx1, double dy1,
                                      double dx2, double dy2,
                                      double lw1, double lw2)
        {
            tracePrimitive(target);
            target.DrawParallelogram(sg2d, dest,
                                     x, y, dx1, dy1, dx2, dy2, lw1, lw2);
        }
    }
}
