public abstract class SurfaceDataProxy
    implements DisplayChangedListener, SurfaceManager.FlushableCacheData
{
    private static boolean cachingAllowed;
    private static int defaultThreshold;
    static {
        cachingAllowed = true;
        String manimg = (String)AccessController.doPrivileged(
            new GetPropertyAction("sun.java2d.managedimages"));
        if (manimg != null && manimg.equals("false")) {
            cachingAllowed = false;
            System.out.println("Disabling managed images");
        }
        defaultThreshold = 1;
        String num = (String)AccessController.doPrivileged(
            new GetPropertyAction("sun.java2d.accthreshold"));
        if (num != null) {
            try {
                int parsed = Integer.parseInt(num);
                if (parsed >= 0) {
                    defaultThreshold = parsed;
                    System.out.println("New Default Acceleration Threshold: " +
                                       defaultThreshold);
                }
            } catch (NumberFormatException e) {
                System.err.println("Error setting new threshold:" + e);
            }
        }
    }
    public static boolean isCachingAllowed() {
        return cachingAllowed;
    }
    public abstract boolean isSupportedOperation(SurfaceData srcData,
                                                 int txtype,
                                                 CompositeType comp,
                                                 Color bgColor);
    public abstract SurfaceData validateSurfaceData(SurfaceData srcData,
                                                    SurfaceData cachedData,
                                                    int w, int h);
    public StateTracker getRetryTracker(SurfaceData srcData) {
        return new CountdownTracker(threshold);
    }
    public static class CountdownTracker implements StateTracker {
        private int countdown;
        public CountdownTracker(int threshold) {
            this.countdown = threshold;
        }
        public synchronized boolean isCurrent() {
            return (--countdown >= 0);
        }
    }
    public static SurfaceDataProxy UNCACHED = new SurfaceDataProxy(0) {
        @Override
        public boolean isAccelerated() {
            return false;
        }
        @Override
        public boolean isSupportedOperation(SurfaceData srcData,
                                            int txtype,
                                            CompositeType comp,
                                            Color bgColor)
        {
            return false;
        }
        @Override
        public SurfaceData validateSurfaceData(SurfaceData srcData,
                                               SurfaceData cachedData,
                                               int w, int h)
        {
            throw new InternalError("UNCACHED should never validate SDs");
        }
        @Override
        public SurfaceData replaceData(SurfaceData srcData,
                                       int txtype,
                                       CompositeType comp,
                                       Color bgColor)
        {
            return srcData;
        }
    };
    private int threshold;
    private StateTracker srcTracker;
    private int numtries;
    private SurfaceData cachedSD;
    private StateTracker cacheTracker;
    private boolean valid;
    public SurfaceDataProxy() {
        this(defaultThreshold);
    }
    public SurfaceDataProxy(int threshold) {
        this.threshold = threshold;
        this.srcTracker = StateTracker.NEVER_CURRENT;
        this.cacheTracker = StateTracker.NEVER_CURRENT;
        this.valid = true;
    }
    public boolean isValid() {
        return valid;
    }
    public void invalidate() {
        this.valid = false;
    }
    public boolean flush(boolean deaccelerated) {
        if (deaccelerated) {
            invalidate();
        }
        flush();
        return !isValid();
    }
    public synchronized void flush() {
        SurfaceData csd = this.cachedSD;
        this.cachedSD = null;
        this.cacheTracker = StateTracker.NEVER_CURRENT;
        if (csd != null) {
            csd.flush();
        }
    }
    public boolean isAccelerated() {
        return (isValid() &&
                srcTracker.isCurrent() &&
                cacheTracker.isCurrent());
    }
    protected void activateDisplayListener() {
        GraphicsEnvironment ge =
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        if (ge instanceof SunGraphicsEnvironment) {
            ((SunGraphicsEnvironment)ge).addDisplayChangedListener(this);
        }
    }
    public void displayChanged() {
        flush();
    }
    public void paletteChanged() {
        this.srcTracker = StateTracker.NEVER_CURRENT;
    }
    public SurfaceData replaceData(SurfaceData srcData,
                                   int txtype,
                                   CompositeType comp,
                                   Color bgColor)
    {
        if (isSupportedOperation(srcData, txtype, comp, bgColor)) {
            if (!srcTracker.isCurrent()) {
                synchronized (this) {
                    this.numtries = threshold;
                    this.srcTracker = srcData.getStateTracker();
                    this.cacheTracker = StateTracker.NEVER_CURRENT;
                }
                if (!srcTracker.isCurrent()) {
                    if (srcData.getState() == State.UNTRACKABLE) {
                        invalidate();
                        flush();
                    }
                    return srcData;
                }
            }
            SurfaceData csd = this.cachedSD;
            if (!cacheTracker.isCurrent()) {
                synchronized (this) {
                    if (numtries > 0) {
                        --numtries;
                        return srcData;
                    }
                }
                Rectangle r = srcData.getBounds();
                int w = r.width;
                int h = r.height;
                StateTracker curTracker = srcTracker;
                csd = validateSurfaceData(srcData, csd, w, h);
                if (csd == null) {
                    synchronized (this) {
                        if (curTracker == srcTracker) {
                            this.cacheTracker = getRetryTracker(srcData);
                            this.cachedSD = null;
                        }
                    }
                    return srcData;
                }
                updateSurfaceData(srcData, csd, w, h);
                if (!csd.isValid()) {
                    return srcData;
                }
                synchronized (this) {
                    if (curTracker == srcTracker && curTracker.isCurrent()) {
                        this.cacheTracker = csd.getStateTracker();
                        this.cachedSD = csd;
                    }
                }
            }
            if (csd != null) {
                return csd;
            }
        }
        return srcData;
    }
    public void updateSurfaceData(SurfaceData srcData,
                                  SurfaceData dstData,
                                  int w, int h)
    {
        SurfaceType srcType = srcData.getSurfaceType();
        SurfaceType dstType = dstData.getSurfaceType();
        Blit blit = Blit.getFromCache(srcType,
                                      CompositeType.SrcNoEa,
                                      dstType);
        blit.Blit(srcData, dstData,
                  AlphaComposite.Src, null,
                  0, 0, 0, 0, w, h);
        dstData.markDirty();
    }
    public void updateSurfaceDataBg(SurfaceData srcData,
                                    SurfaceData dstData,
                                    int w, int h, Color bgColor)
    {
        SurfaceType srcType = srcData.getSurfaceType();
        SurfaceType dstType = dstData.getSurfaceType();
        BlitBg blitbg = BlitBg.getFromCache(srcType,
                                            CompositeType.SrcNoEa,
                                            dstType);
        blitbg.BlitBg(srcData, dstData,
                      AlphaComposite.Src, null, bgColor.getRGB(),
                      0, 0, 0, 0, w, h);
        dstData.markDirty();
    }
}
