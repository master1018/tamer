public class OGLContext extends BufferedContext {
    private final OGLGraphicsConfig config;
    OGLContext(RenderQueue rq, OGLGraphicsConfig config) {
        super(rq);
        this.config = config;
    }
    static void setScratchSurface(OGLGraphicsConfig gc) {
        setScratchSurface(gc.getNativeConfigInfo());
    }
    static void setScratchSurface(long pConfigInfo) {
        currentContext = null;
        OGLRenderQueue rq = OGLRenderQueue.getInstance();
        RenderBuffer buf = rq.getBuffer();
        rq.ensureCapacityAndAlignment(12, 4);
        buf.putInt(SET_SCRATCH_SURFACE);
        buf.putLong(pConfigInfo);
    }
    static void invalidateCurrentContext() {
        if (currentContext != null) {
            currentContext.invalidateContext();
            currentContext = null;
        }
        OGLRenderQueue rq = OGLRenderQueue.getInstance();
        rq.ensureCapacity(4);
        rq.getBuffer().putInt(INVALIDATE_CONTEXT);
        rq.flushNow();
    }
    public RenderQueue getRenderQueue() {
        return OGLRenderQueue.getInstance();
    }
    static final native String getOGLIdString();
    @Override
    public void saveState() {
        invalidateContext();
        invalidateCurrentContext();
        setScratchSurface(config);
        rq.ensureCapacity(4);
        buf.putInt(SAVE_STATE);
        rq.flushNow();
    }
    @Override
    public void restoreState() {
        invalidateContext();
        invalidateCurrentContext();
        setScratchSurface(config);
        rq.ensureCapacity(4);
        buf.putInt(RESTORE_STATE);
        rq.flushNow();
    }
    static class OGLContextCaps extends ContextCapabilities {
        static final int CAPS_EXT_FBOBJECT     =
                (CAPS_RT_TEXTURE_ALPHA | CAPS_RT_TEXTURE_OPAQUE);
        static final int CAPS_STORED_ALPHA     = CAPS_RT_PLAIN_ALPHA;
        static final int CAPS_DOUBLEBUFFERED   = (FIRST_PRIVATE_CAP << 0);
        static final int CAPS_EXT_LCD_SHADER   = (FIRST_PRIVATE_CAP << 1);
        static final int CAPS_EXT_BIOP_SHADER  = (FIRST_PRIVATE_CAP << 2);
        static final int CAPS_EXT_GRAD_SHADER  = (FIRST_PRIVATE_CAP << 3);
        static final int CAPS_EXT_TEXRECT      = (FIRST_PRIVATE_CAP << 4);
        OGLContextCaps(int caps, String adapterId) {
            super(caps, adapterId);
        }
        @Override
        public String toString() {
            StringBuffer buf = new StringBuffer(super.toString());
            if ((caps & CAPS_EXT_FBOBJECT) != 0) {
                buf.append("CAPS_EXT_FBOBJECT|");
            }
            if ((caps & CAPS_STORED_ALPHA) != 0) {
                buf.append("CAPS_STORED_ALPHA|");
            }
            if ((caps & CAPS_DOUBLEBUFFERED) != 0) {
                buf.append("CAPS_DOUBLEBUFFERED|");
            }
            if ((caps & CAPS_EXT_LCD_SHADER) != 0) {
                buf.append("CAPS_EXT_LCD_SHADER|");
            }
            if ((caps & CAPS_EXT_BIOP_SHADER) != 0) {
                buf.append("CAPS_BIOP_SHADER|");
            }
            if ((caps & CAPS_EXT_GRAD_SHADER) != 0) {
                buf.append("CAPS_EXT_GRAD_SHADER|");
            }
            if ((caps & CAPS_EXT_TEXRECT) != 0) {
                buf.append("CAPS_EXT_TEXRECT|");
            }
            return buf.toString();
        }
    }
}
