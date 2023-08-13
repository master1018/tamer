class D3DContext extends BufferedContext {
    private final D3DGraphicsDevice device;
    D3DContext(RenderQueue rq, D3DGraphicsDevice device) {
        super(rq);
        this.device = device;
    }
    static void invalidateCurrentContext() {
        if (currentContext != null) {
            currentContext.invalidateContext();
            currentContext = null;
        }
        D3DRenderQueue rq = D3DRenderQueue.getInstance();
        rq.ensureCapacity(4);
        rq.getBuffer().putInt(INVALIDATE_CONTEXT);
        rq.flushNow();
    }
    static void setScratchSurface(D3DContext d3dc) {
        if (d3dc != currentContext) {
            currentContext = null;
        }
        D3DRenderQueue rq = D3DRenderQueue.getInstance();
        RenderBuffer buf = rq.getBuffer();
        rq.ensureCapacity(8);
        buf.putInt(SET_SCRATCH_SURFACE);
        buf.putInt(d3dc.getDevice().getScreen());
    }
    public RenderQueue getRenderQueue() {
        return D3DRenderQueue.getInstance();
    }
    @Override
    public void saveState() {
        invalidateContext();
        invalidateCurrentContext();
        setScratchSurface(this);
        rq.ensureCapacity(4);
        buf.putInt(SAVE_STATE);
        rq.flushNow();
    }
    @Override
    public void restoreState() {
        invalidateContext();
        invalidateCurrentContext();
        setScratchSurface(this);
        rq.ensureCapacity(4);
        buf.putInt(RESTORE_STATE);
        rq.flushNow();
    }
    D3DGraphicsDevice getDevice() {
        return device;
    }
    static class D3DContextCaps extends ContextCapabilities {
        static final int CAPS_LCD_SHADER       = (FIRST_PRIVATE_CAP << 0);
        static final int CAPS_BIOP_SHADER      = (FIRST_PRIVATE_CAP << 1);
        static final int CAPS_DEVICE_OK        = (FIRST_PRIVATE_CAP << 2);
        static final int CAPS_AA_SHADER        = (FIRST_PRIVATE_CAP << 3);
        D3DContextCaps(int caps, String adapterId) {
            super(caps, adapterId);
        }
        @Override
        public String toString() {
            StringBuffer buf = new StringBuffer(super.toString());
            if ((caps & CAPS_LCD_SHADER) != 0) {
                buf.append("CAPS_LCD_SHADER|");
            }
            if ((caps & CAPS_BIOP_SHADER) != 0) {
                buf.append("CAPS_BIOP_SHADER|");
            }
            if ((caps & CAPS_AA_SHADER) != 0) {
                buf.append("CAPS_AA_SHADER|");
            }
            if ((caps & CAPS_DEVICE_OK) != 0) {
                buf.append("CAPS_DEVICE_OK|");
            }
            return buf.toString();
        }
    }
}
