public class DrawRect extends GraphicsPrimitive
{
    public final static String methodSignature = "DrawRect(...)".toString();
    public final static int primTypeID = makePrimTypeID();
    public static DrawRect locate(SurfaceType srctype,
                                  CompositeType comptype,
                                  SurfaceType dsttype)
    {
        return (DrawRect)
            GraphicsPrimitiveMgr.locate(primTypeID,
                                        srctype, comptype, dsttype);
    }
    protected DrawRect(SurfaceType srctype,
                       CompositeType comptype,
                       SurfaceType dsttype)
    {
        super(methodSignature, primTypeID, srctype, comptype, dsttype);
    }
    public DrawRect(long pNativePrim,
                    SurfaceType srctype,
                    CompositeType comptype,
                    SurfaceType dsttype)
    {
        super(pNativePrim, methodSignature, primTypeID, srctype, comptype, dsttype);
    }
    public native void DrawRect(SunGraphics2D sg2d, SurfaceData dest,
                                int x1, int y1, int w, int h);
    public GraphicsPrimitive makePrimitive(SurfaceType srctype,
                                           CompositeType comptype,
                                           SurfaceType dsttype)
    {
        throw new InternalError("DrawRect not implemented for "+
                                srctype+" with "+comptype);
    }
    public GraphicsPrimitive traceWrap() {
        return new TraceDrawRect(this);
    }
    private static class TraceDrawRect extends DrawRect {
        DrawRect target;
        public TraceDrawRect(DrawRect target) {
            super(target.getSourceType(),
                  target.getCompositeType(),
                  target.getDestType());
            this.target = target;
        }
        public GraphicsPrimitive traceWrap() {
            return this;
        }
        public void DrawRect(SunGraphics2D sg2d, SurfaceData dest,
                             int x1, int y1, int w, int h)
        {
            tracePrimitive(target);
            target.DrawRect(sg2d, dest, x1, y1, w, h);
        }
    }
}
