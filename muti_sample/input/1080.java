public class DrawPath extends GraphicsPrimitive {
    public final static String methodSignature =
        "DrawPath(...)".toString();
    public final static int primTypeID = makePrimTypeID();
    public static DrawPath locate(SurfaceType srctype,
                                  CompositeType comptype,
                                  SurfaceType dsttype)
    {
        return (DrawPath)
            GraphicsPrimitiveMgr.locate(primTypeID,
                                        srctype, comptype, dsttype);
    }
    protected DrawPath(SurfaceType srctype,
                       CompositeType comptype,
                       SurfaceType dsttype)
    {
        super(methodSignature, primTypeID,
              srctype, comptype, dsttype);
    }
    public DrawPath(long pNativePrim,
                    SurfaceType srctype,
                    CompositeType comptype,
                    SurfaceType dsttype)
    {
        super(pNativePrim, methodSignature, primTypeID,
              srctype, comptype, dsttype);
    }
    public native void DrawPath(SunGraphics2D sg2d, SurfaceData sData,
                                int transX, int transY,
                                Path2D.Float p2df);
    public GraphicsPrimitive makePrimitive(SurfaceType srctype,
                                           CompositeType comptype,
                                           SurfaceType dsttype)
    {
        throw new InternalError("DrawPath not implemented for "+
                                srctype+" with "+comptype);
    }
    public GraphicsPrimitive traceWrap() {
        return new TraceDrawPath(this);
    }
    private static class TraceDrawPath extends DrawPath {
        DrawPath target;
        public TraceDrawPath(DrawPath target) {
            super(target.getSourceType(),
                  target.getCompositeType(),
                  target.getDestType());
            this.target = target;
        }
        public GraphicsPrimitive traceWrap() {
            return this;
        }
        public void DrawPath(SunGraphics2D sg2d, SurfaceData sData,
                             int transX, int transY,
                             Path2D.Float p2df)
        {
            tracePrimitive(target);
            target.DrawPath(sg2d, sData, transX, transY, p2df);
        }
    }
}
