public class ScaledBlit extends GraphicsPrimitive
{
    public static final String methodSignature = "ScaledBlit(...)".toString();
    public static final int primTypeID = makePrimTypeID();
    private static RenderCache blitcache = new RenderCache(20);
    public static ScaledBlit locate(SurfaceType srctype,
                              CompositeType comptype,
                              SurfaceType dsttype)
    {
        return (ScaledBlit)
            GraphicsPrimitiveMgr.locate(primTypeID,
                                        srctype, comptype, dsttype);
    }
    public static ScaledBlit getFromCache(SurfaceType src,
                                          CompositeType comp,
                                          SurfaceType dst)
    {
        Object o = blitcache.get(src, comp, dst);
        if (o != null) {
            return (ScaledBlit) o;
        }
        ScaledBlit blit = locate(src, comp, dst);
        if (blit == null) {
        } else {
            blitcache.put(src, comp, dst, blit);
        }
        return blit;
    }
    protected ScaledBlit(SurfaceType srctype,
                   CompositeType comptype,
                   SurfaceType dsttype)
    {
        super(methodSignature, primTypeID, srctype, comptype, dsttype);
    }
    public ScaledBlit(long pNativePrim,
                      SurfaceType srctype,
                      CompositeType comptype,
                      SurfaceType dsttype)
    {
        super(pNativePrim, methodSignature, primTypeID,
              srctype, comptype, dsttype);
    }
    public native void Scale(SurfaceData src, SurfaceData dst,
                             Composite comp, Region clip,
                             int sx1, int sy1,
                             int sx2, int sy2,
                             double dx1, double dy1,
                             double dx2, double dy2);
    static {
        GraphicsPrimitiveMgr.registerGeneral(new ScaledBlit(null, null, null));
    }
    public GraphicsPrimitive makePrimitive(SurfaceType srctype,
                                           CompositeType comptype,
                                           SurfaceType dsttype)
    {
        return null;
    }
    public GraphicsPrimitive traceWrap() {
        return new TraceScaledBlit(this);
    }
    private static class TraceScaledBlit extends ScaledBlit {
        ScaledBlit target;
        public TraceScaledBlit(ScaledBlit target) {
            super(target.getSourceType(),
                  target.getCompositeType(),
                  target.getDestType());
            this.target = target;
        }
        public GraphicsPrimitive traceWrap() {
            return this;
        }
        public void Scale(SurfaceData src, SurfaceData dst,
                          Composite comp, Region clip,
                          int sx1, int sy1,
                          int sx2, int sy2,
                          double dx1, double dy1,
                          double dx2, double dy2)
        {
            tracePrimitive(target);
            target.Scale(src, dst, comp, clip,
                         sx1, sy1, sx2, sy2,
                         dx1, dy1, dx2, dy2);
        }
    }
}
