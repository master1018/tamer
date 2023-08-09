public class MaskBlit extends GraphicsPrimitive
{
    public static final String methodSignature = "MaskBlit(...)".toString();
    public static final int primTypeID = makePrimTypeID();
    private static RenderCache blitcache = new RenderCache(20);
    public static MaskBlit locate(SurfaceType srctype,
                                  CompositeType comptype,
                                  SurfaceType dsttype)
    {
        return (MaskBlit)
            GraphicsPrimitiveMgr.locate(primTypeID,
                                        srctype, comptype, dsttype);
    }
    public static MaskBlit getFromCache(SurfaceType src,
                                        CompositeType comp,
                                        SurfaceType dst)
    {
        Object o = blitcache.get(src, comp, dst);
        if (o != null) {
            return (MaskBlit) o;
        }
        MaskBlit blit = locate(src, comp, dst);
        if (blit == null) {
            System.out.println("mask blit loop not found for:");
            System.out.println("src:  "+src);
            System.out.println("comp: "+comp);
            System.out.println("dst:  "+dst);
        } else {
            blitcache.put(src, comp, dst, blit);
        }
        return blit;
    }
    protected MaskBlit(SurfaceType srctype,
                       CompositeType comptype,
                       SurfaceType dsttype)
    {
        super(methodSignature, primTypeID, srctype, comptype, dsttype);
    }
    public MaskBlit(long pNativePrim,
                    SurfaceType srctype,
                    CompositeType comptype,
                    SurfaceType dsttype)
    {
        super(pNativePrim, methodSignature, primTypeID, srctype, comptype, dsttype);
    }
    public native void MaskBlit(SurfaceData src, SurfaceData dst,
                                Composite comp, Region clip,
                                int srcx, int srcy,
                                int dstx, int dsty,
                                int width, int height,
                                byte[] mask, int maskoff, int maskscan);
    static {
        GraphicsPrimitiveMgr.registerGeneral(new MaskBlit(null, null, null));
    }
    public GraphicsPrimitive makePrimitive(SurfaceType srctype,
                                           CompositeType comptype,
                                           SurfaceType dsttype)
    {
        if (CompositeType.Xor.equals(comptype)) {
            throw new InternalError("Cannot construct MaskBlit for " +
                                    "XOR mode");
        }
        General ob = new General(srctype, comptype, dsttype);
        setupGeneralBinaryOp(ob);
        return ob;
    }
    private static class General
        extends MaskBlit
        implements GeneralBinaryOp
    {
        Blit convertsrc;
        Blit convertdst;
        MaskBlit performop;
        Blit convertresult;
        WeakReference srcTmp;
        WeakReference dstTmp;
        public General(SurfaceType srctype,
                       CompositeType comptype,
                       SurfaceType dsttype)
        {
            super(srctype, comptype, dsttype);
        }
        public void setPrimitives(Blit srcconverter,
                                  Blit dstconverter,
                                  GraphicsPrimitive genericop,
                                  Blit resconverter)
        {
            this.convertsrc = srcconverter;
            this.convertdst = dstconverter;
            this.performop = (MaskBlit) genericop;
            this.convertresult = resconverter;
        }
        public synchronized void MaskBlit(SurfaceData srcData,
                                          SurfaceData dstData,
                                          Composite comp,
                                          Region clip,
                                          int srcx, int srcy,
                                          int dstx, int dsty,
                                          int width, int height,
                                          byte mask[], int offset, int scan)
        {
            SurfaceData src, dst;
            Region opclip;
            int sx, sy, dx, dy;
            if (convertsrc == null) {
                src = srcData;
                sx = srcx;
                sy = srcy;
            } else {
                SurfaceData cachedSrc = null;
                if (srcTmp != null) {
                    cachedSrc = (SurfaceData) srcTmp.get();
                }
                src = convertFrom(convertsrc, srcData, srcx, srcy,
                                  width, height, cachedSrc);
                sx = 0;
                sy = 0;
                if (src != cachedSrc) {
                    srcTmp = new WeakReference(src);
                }
            }
            if (convertdst == null) {
                dst = dstData;
                dx = dstx;
                dy = dsty;
                opclip = clip;
            } else {
                SurfaceData cachedDst = null;
                if (dstTmp != null) {
                    cachedDst = (SurfaceData) dstTmp.get();
                }
                dst = convertFrom(convertdst, dstData, dstx, dsty,
                                  width, height, cachedDst);
                dx = 0;
                dy = 0;
                opclip = null;
                if (dst != cachedDst) {
                    dstTmp = new WeakReference(dst);
                }
            }
            performop.MaskBlit(src, dst, comp, opclip,
                               sx, sy, dx, dy, width, height,
                               mask, offset, scan);
            if (convertresult != null) {
                convertTo(convertresult, dst, dstData, clip,
                          dstx, dsty, width, height);
            }
        }
    }
    public GraphicsPrimitive traceWrap() {
        return new TraceMaskBlit(this);
    }
    private static class TraceMaskBlit extends MaskBlit {
        MaskBlit target;
        public TraceMaskBlit(MaskBlit target) {
            super(target.getNativePrim(),
                  target.getSourceType(),
                  target.getCompositeType(),
                  target.getDestType());
            this.target = target;
        }
        public GraphicsPrimitive traceWrap() {
            return this;
        }
        public void MaskBlit(SurfaceData src, SurfaceData dst,
                             Composite comp, Region clip,
                             int srcx, int srcy, int dstx, int dsty,
                             int width, int height,
                             byte[] mask, int maskoff, int maskscan)
        {
            tracePrimitive(target);
            target.MaskBlit(src, dst, comp, clip,
                            srcx, srcy, dstx, dsty, width, height,
                            mask, maskoff, maskscan);
        }
    }
}
