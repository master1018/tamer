public abstract class BufferedMaskBlit extends MaskBlit {
    private static final int ST_INT_ARGB     = 0;
    private static final int ST_INT_ARGB_PRE = 1;
    private static final int ST_INT_RGB      = 2;
    private static final int ST_INT_BGR      = 3;
    private final RenderQueue rq;
    private final int srcTypeVal;
    private Blit blitop;
    protected BufferedMaskBlit(RenderQueue rq,
                               SurfaceType srcType,
                               CompositeType compType,
                               SurfaceType dstType)
    {
        super(srcType, compType, dstType);
        this.rq = rq;
        if (srcType == SurfaceType.IntArgb) {
            this.srcTypeVal = ST_INT_ARGB;
        } else if (srcType == SurfaceType.IntArgbPre) {
            this.srcTypeVal = ST_INT_ARGB_PRE;
        } else if (srcType == SurfaceType.IntRgb) {
            this.srcTypeVal = ST_INT_RGB;
        } else if (srcType == SurfaceType.IntBgr) {
            this.srcTypeVal = ST_INT_BGR;
        } else {
            throw new InternalError("unrecognized source surface type");
        }
    }
    @Override
    public void MaskBlit(SurfaceData src, SurfaceData dst,
                         Composite comp, Region clip,
                         int srcx, int srcy,
                         int dstx, int dsty,
                         int width, int height,
                         byte[] mask, int maskoff, int maskscan)
    {
        if (width <= 0 || height <= 0) {
            return;
        }
        if (mask == null) {
            if (blitop == null) {
                blitop = Blit.getFromCache(src.getSurfaceType(),
                                           CompositeType.AnyAlpha,
                                           this.getDestType());
            }
            blitop.Blit(src, dst,
                        comp, clip,
                        srcx, srcy, dstx, dsty,
                        width, height);
            return;
        }
        AlphaComposite acomp = (AlphaComposite)comp;
        if (acomp.getRule() != AlphaComposite.SRC_OVER) {
            comp = AlphaComposite.SrcOver;
        }
        rq.lock();
        try {
            validateContext(dst, comp, clip);
            RenderBuffer buf = rq.getBuffer();
            int totalBytesRequired = 20 + (width * height * 4);
            rq.ensureCapacity(totalBytesRequired);
            int newpos = enqueueTile(buf.getAddress(), buf.position(),
                                     src, src.getNativeOps(), srcTypeVal,
                                     mask, mask.length, maskoff, maskscan,
                                     srcx, srcy, dstx, dsty,
                                     width, height);
            buf.position(newpos);
        } finally {
            rq.unlock();
        }
    }
    private native int enqueueTile(long buf, int bpos,
                                   SurfaceData srcData,
                                   long pSrcOps, int srcType,
                                   byte[] mask, int masklen,
                                   int maskoff, int maskscan,
                                   int srcx, int srcy, int dstx, int dsty,
                                   int width, int height);
    protected abstract void validateContext(SurfaceData dstData,
                                            Composite comp, Region clip);
}
