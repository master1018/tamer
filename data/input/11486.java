public class DrawGlyphListLCD extends GraphicsPrimitive {
    public final static String
        methodSignature = "DrawGlyphListLCD(...)".toString();
    public final static int primTypeID = makePrimTypeID();
    public static DrawGlyphListLCD locate(SurfaceType srctype,
                                           CompositeType comptype,
                                           SurfaceType dsttype)
    {
        return (DrawGlyphListLCD)
            GraphicsPrimitiveMgr.locate(primTypeID,
                                        srctype, comptype, dsttype);
    }
    protected DrawGlyphListLCD(SurfaceType srctype,
                                CompositeType comptype,
                                SurfaceType dsttype)
    {
        super(methodSignature, primTypeID, srctype, comptype, dsttype);
    }
    public DrawGlyphListLCD(long pNativePrim,
                             SurfaceType srctype,
                             CompositeType comptype,
                             SurfaceType dsttype)
    {
        super(pNativePrim, methodSignature, primTypeID,
              srctype, comptype, dsttype);
    }
    public native void DrawGlyphListLCD(SunGraphics2D sg2d, SurfaceData dest,
                                         GlyphList srcData);
    static {
        GraphicsPrimitiveMgr.registerGeneral(
                                   new DrawGlyphListLCD(null, null, null));
    }
    public GraphicsPrimitive makePrimitive(SurfaceType srctype,
                                           CompositeType comptype,
                                           SurfaceType dsttype) {
        return null;
    }
    public GraphicsPrimitive traceWrap() {
        return new TraceDrawGlyphListLCD(this);
    }
    private static class TraceDrawGlyphListLCD extends DrawGlyphListLCD {
        DrawGlyphListLCD target;
        public TraceDrawGlyphListLCD(DrawGlyphListLCD target) {
            super(target.getSourceType(),
                  target.getCompositeType(),
                  target.getDestType());
            this.target = target;
        }
        public GraphicsPrimitive traceWrap() {
            return this;
        }
        public void DrawGlyphListLCD(SunGraphics2D sg2d, SurfaceData dest,
                                      GlyphList glyphs)
        {
            tracePrimitive(target);
            target.DrawGlyphListLCD(sg2d, dest, glyphs);
        }
    }
}
