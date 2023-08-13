public class DrawLine extends GraphicsPrimitive
{
    public final static String methodSignature = "DrawLine(...)".toString();
    public final static int primTypeID = makePrimTypeID();
    public static DrawLine locate(SurfaceType srctype,
                                  CompositeType comptype,
                                  SurfaceType dsttype)
    {
        return (DrawLine) GraphicsPrimitiveMgr.locate(primTypeID,
                                                      srctype, comptype, dsttype);
    }
    protected DrawLine(SurfaceType srctype,
                       CompositeType comptype,
                       SurfaceType dsttype)
    {
        super(methodSignature, primTypeID, srctype, comptype, dsttype);
    }
    public DrawLine(long pNativePrim,
                    SurfaceType srctype,
                    CompositeType comptype,
                    SurfaceType dsttype)
    {
        super(pNativePrim, methodSignature, primTypeID, srctype, comptype, dsttype);
    }
    public native void DrawLine(SunGraphics2D sg2d, SurfaceData dest,
                                int x1, int y1, int x2, int y2);
    public GraphicsPrimitive makePrimitive(SurfaceType srctype,
                                           CompositeType comptype,
                                           SurfaceType dsttype)
    {
        throw new InternalError("DrawLine not implemented for "+
                                srctype+" with "+comptype);
    }
    public GraphicsPrimitive traceWrap() {
        return new TraceDrawLine(this);
    }
    private static class TraceDrawLine extends DrawLine {
        DrawLine target;
        public TraceDrawLine(DrawLine target) {
            super(target.getSourceType(),
                  target.getCompositeType(),
                  target.getDestType());
            this.target = target;
        }
        public GraphicsPrimitive traceWrap() {
            return this;
        }
        public void DrawLine(SunGraphics2D sg2d, SurfaceData dest,
                             int x1, int y1, int x2, int y2)
        {
            tracePrimitive(target);
            target.DrawLine(sg2d, dest, x1, y1, x2, y2);
        }
    }
}
