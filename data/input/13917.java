public abstract class BufferedMaskFill extends MaskFill {
    protected final RenderQueue rq;
    protected BufferedMaskFill(RenderQueue rq,
                               SurfaceType srcType,
                               CompositeType compType,
                               SurfaceType dstType)
    {
        super(srcType, compType, dstType);
        this.rq = rq;
    }
    @Override
    public void MaskFill(SunGraphics2D sg2d, SurfaceData sData,
                         Composite comp,
                         final int x, final int y, final int w, final int h,
                         final byte[] mask,
                         final int maskoff, final int maskscan)
    {
        AlphaComposite acomp = (AlphaComposite)comp;
        if (acomp.getRule() != AlphaComposite.SRC_OVER) {
            comp = AlphaComposite.SrcOver;
        }
        rq.lock();
        try {
            validateContext(sg2d, comp, BufferedContext.USE_MASK);
            int maskBytesRequired;
            if (mask != null) {
                maskBytesRequired = (mask.length + 3) & (~3);
            } else {
                maskBytesRequired = 0;
            }
            int totalBytesRequired = 32 + maskBytesRequired;
            RenderBuffer buf = rq.getBuffer();
            if (totalBytesRequired <= buf.capacity()) {
                if (totalBytesRequired > buf.remaining()) {
                    rq.flushNow();
                }
                buf.putInt(MASK_FILL);
                buf.putInt(x).putInt(y).putInt(w).putInt(h);
                buf.putInt(maskoff);
                buf.putInt(maskscan);
                buf.putInt(maskBytesRequired);
                if (mask != null) {
                    int padding = maskBytesRequired - mask.length;
                    buf.put(mask);
                    if (padding != 0) {
                        buf.position(buf.position() + padding);
                    }
                }
            } else {
                rq.flushAndInvokeNow(new Runnable() {
                    public void run() {
                        maskFill(x, y, w, h,
                                 maskoff, maskscan, mask.length, mask);
                    }
                });
            }
        } finally {
            rq.unlock();
        }
    }
    protected abstract void maskFill(int x, int y, int w, int h,
                                     int maskoff, int maskscan, int masklen,
                                     byte[] mask);
    protected abstract void validateContext(SunGraphics2D sg2d,
                                            Composite comp, int ctxflags);
}
