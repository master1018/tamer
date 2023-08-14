public class DrawPolygons extends GraphicsPrimitive
{
    public final static String methodSignature = "DrawPolygons(...)".toString();
    public final static int primTypeID = makePrimTypeID();
    public static DrawPolygons locate(SurfaceType srctype,
                                      CompositeType comptype,
                                      SurfaceType dsttype)
    {
        return (DrawPolygons)
            GraphicsPrimitiveMgr.locate(primTypeID,
                                        srctype, comptype, dsttype);
    }
    protected DrawPolygons(SurfaceType srctype,
                           CompositeType comptype,
                           SurfaceType dsttype)
    {
        super(methodSignature, primTypeID, srctype, comptype, dsttype);
    }
    public DrawPolygons(long pNativePrim,
                        SurfaceType srctype,
                        CompositeType comptype,
                        SurfaceType dsttype)
    {
        super(pNativePrim, methodSignature, primTypeID, srctype, comptype, dsttype);
    }
    public native void DrawPolygons(SunGraphics2D sg2d, SurfaceData sData,
                                    int xPoints[], int yPoints[],
                                    int nPoints[], int numPolys,
                                    int transX, int transY,
                                    boolean close);
    public GraphicsPrimitive makePrimitive(SurfaceType srctype,
                                           CompositeType comptype,
                                           SurfaceType dsttype)
    {
        throw new InternalError("DrawPolygons not implemented for "+
                                srctype+" with "+comptype);
    }
    public GraphicsPrimitive traceWrap() {
        return new TraceDrawPolygons(this);
    }
    private static class TraceDrawPolygons extends DrawPolygons {
        DrawPolygons target;
        public TraceDrawPolygons(DrawPolygons target) {
            super(target.getSourceType(),
                  target.getCompositeType(),
                  target.getDestType());
            this.target = target;
        }
        public GraphicsPrimitive traceWrap() {
            return this;
        }
        public void DrawPolygons(SunGraphics2D sg2d, SurfaceData sData,
                                 int xPoints[], int yPoints[],
                                 int nPoints[], int numPolys,
                                 int transX, int transY,
                                 boolean close)
        {
            tracePrimitive(target);
            target.DrawPolygons(sg2d, sData,
                                xPoints, yPoints, nPoints, numPolys,
                                transX, transY, close);
        }
    }
}
