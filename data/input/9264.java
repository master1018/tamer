public final class StrikeCache {
    static final Unsafe unsafe = Unsafe.getUnsafe();
    static ReferenceQueue refQueue = Disposer.getQueue();
    static ArrayList<GlyphDisposedListener> disposeListeners = new ArrayList<GlyphDisposedListener>(1);
    static int MINSTRIKES = 8; 
    static int recentStrikeIndex = 0;
    static FontStrike[] recentStrikes;
    static boolean cacheRefTypeWeak;
    static int nativeAddressSize;
    static int glyphInfoSize;
    static int xAdvanceOffset;
    static int yAdvanceOffset;
    static int boundsOffset;
    static int widthOffset;
    static int heightOffset;
    static int rowBytesOffset;
    static int topLeftXOffset;
    static int topLeftYOffset;
    static int pixelDataOffset;
    static int cacheCellOffset;
    static int managedOffset;
    static long invisibleGlyphPtr;
    static native void getGlyphCacheDescription(long[] infoArray);
    static {
        long[] nativeInfo = new long[13];
        getGlyphCacheDescription(nativeInfo);
        nativeAddressSize = (int)nativeInfo[0];
        glyphInfoSize     = (int)nativeInfo[1];
        xAdvanceOffset    = (int)nativeInfo[2];
        yAdvanceOffset    = (int)nativeInfo[3];
        widthOffset       = (int)nativeInfo[4];
        heightOffset      = (int)nativeInfo[5];
        rowBytesOffset    = (int)nativeInfo[6];
        topLeftXOffset    = (int)nativeInfo[7];
        topLeftYOffset    = (int)nativeInfo[8];
        pixelDataOffset   = (int)nativeInfo[9];
        invisibleGlyphPtr = nativeInfo[10];
        cacheCellOffset = (int) nativeInfo[11];
        managedOffset = (int) nativeInfo[12];
        if (nativeAddressSize < 4) {
            throw new InternalError("Unexpected address size for font data: " +
                                    nativeAddressSize);
        }
        java.security.AccessController.doPrivileged(
                                    new java.security.PrivilegedAction() {
            public Object run() {
               String refType =
                   System.getProperty("sun.java2d.font.reftype", "soft");
               cacheRefTypeWeak = refType.equals("weak");
                String minStrikesStr =
                    System.getProperty("sun.java2d.font.minstrikes");
                if (minStrikesStr != null) {
                    try {
                        MINSTRIKES = Integer.parseInt(minStrikesStr);
                        if (MINSTRIKES <= 0) {
                            MINSTRIKES = 1;
                        }
                    } catch (NumberFormatException e) {
                    }
                }
                recentStrikes = new FontStrike[MINSTRIKES];
                return null;
            }
        });
    }
    static void refStrike(FontStrike strike) {
        int index = recentStrikeIndex;
        recentStrikes[index] = strike;
        index++;
        if (index == MINSTRIKES) {
            index = 0;
        }
        recentStrikeIndex = index;
    }
    private static final void doDispose(FontStrikeDisposer disposer) {
        if (disposer.intGlyphImages != null) {
            freeCachedIntMemory(disposer.intGlyphImages,
                    disposer.pScalerContext);
        } else if (disposer.longGlyphImages != null) {
            freeCachedLongMemory(disposer.longGlyphImages,
                    disposer.pScalerContext);
        } else if (disposer.segIntGlyphImages != null) {
            for (int i=0; i<disposer.segIntGlyphImages.length; i++) {
                if (disposer.segIntGlyphImages[i] != null) {
                    freeCachedIntMemory(disposer.segIntGlyphImages[i],
                            disposer.pScalerContext);
                    disposer.pScalerContext = 0L;
                    disposer.segIntGlyphImages[i] = null;
                }
            }
            if (disposer.pScalerContext != 0L) {
                freeCachedIntMemory(new int[0], disposer.pScalerContext);
            }
        } else if (disposer.segLongGlyphImages != null) {
            for (int i=0; i<disposer.segLongGlyphImages.length; i++) {
                if (disposer.segLongGlyphImages[i] != null) {
                    freeCachedLongMemory(disposer.segLongGlyphImages[i],
                            disposer.pScalerContext);
                    disposer.pScalerContext = 0L;
                    disposer.segLongGlyphImages[i] = null;
                }
            }
            if (disposer.pScalerContext != 0L) {
                freeCachedLongMemory(new long[0], disposer.pScalerContext);
            }
        } else if (disposer.pScalerContext != 0L) {
            if (longAddresses()) {
                freeCachedLongMemory(new long[0], disposer.pScalerContext);
            } else {
                freeCachedIntMemory(new int[0], disposer.pScalerContext);
            }
        }
    }
    private static boolean longAddresses() {
        return nativeAddressSize == 8;
    }
    static void disposeStrike(final FontStrikeDisposer disposer) {
        if (Disposer.pollingQueue) {
            doDispose(disposer);
            return;
        }
        RenderQueue rq = null;
        GraphicsEnvironment ge =
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        if (!ge.isHeadless()) {
            GraphicsConfiguration gc =
                ge.getDefaultScreenDevice().getDefaultConfiguration();
            if (gc instanceof AccelGraphicsConfig) {
                AccelGraphicsConfig agc = (AccelGraphicsConfig)gc;
                BufferedContext bc = agc.getContext();
                if (bc != null) {
                    rq = bc.getRenderQueue();
                }
            }
        }
        if (rq != null) {
            rq.lock();
            try {
                rq.flushAndInvokeNow(new Runnable() {
                    public void run() {
                        doDispose(disposer);
                        Disposer.pollRemove();
                    }
                });
            } finally {
                rq.unlock();
            }
        } else {
            doDispose(disposer);
        }
    }
    static native void freeIntPointer(int ptr);
    static native void freeLongPointer(long ptr);
    private static native void freeIntMemory(int[] glyphPtrs, long pContext);
    private static native void freeLongMemory(long[] glyphPtrs, long pContext);
    private static void freeCachedIntMemory(int[] glyphPtrs, long pContext) {
        synchronized(disposeListeners) {
            if (disposeListeners.size() > 0) {
                ArrayList<Long> gids = null;
                for (int i = 0; i < glyphPtrs.length; i++) {
                    if (glyphPtrs[i] != 0 && unsafe.getByte(glyphPtrs[i] + managedOffset) == 0) {
                        if (gids == null) {
                            gids = new ArrayList<Long>();
                        }
                        gids.add((long) glyphPtrs[i]);
                    }
                }
                if (gids != null) {
                    notifyDisposeListeners(gids);
                }
            }
        }
        freeIntMemory(glyphPtrs, pContext);
    }
    private static void  freeCachedLongMemory(long[] glyphPtrs, long pContext) {
        synchronized(disposeListeners) {
        if (disposeListeners.size() > 0)  {
                ArrayList<Long> gids = null;
                for (int i=0; i < glyphPtrs.length; i++) {
                    if (glyphPtrs[i] != 0
                            && unsafe.getByte(glyphPtrs[i] + managedOffset) == 0) {
                        if (gids == null) {
                            gids = new ArrayList<Long>();
                        }
                        gids.add((long) glyphPtrs[i]);
                    }
                }
                if (gids != null) {
                    notifyDisposeListeners(gids);
                }
        }
        }
        freeLongMemory(glyphPtrs, pContext);
    }
    public static void addGlyphDisposedListener(GlyphDisposedListener listener) {
        synchronized(disposeListeners) {
            disposeListeners.add(listener);
        }
    }
    private static void notifyDisposeListeners(ArrayList<Long> glyphs) {
        for (GlyphDisposedListener listener : disposeListeners) {
            listener.glyphDisposed(glyphs);
        }
    }
    public static Reference getStrikeRef(FontStrike strike) {
        return getStrikeRef(strike, cacheRefTypeWeak);
    }
    public static Reference getStrikeRef(FontStrike strike, boolean weak) {
        if (strike.disposer == null) {
            if (weak) {
                return new WeakReference(strike);
            } else {
                return new SoftReference(strike);
            }
        }
        if (weak) {
            return new WeakDisposerRef(strike);
        } else {
            return new SoftDisposerRef(strike);
        }
    }
    static interface DisposableStrike {
        FontStrikeDisposer getDisposer();
    }
    static class SoftDisposerRef
        extends SoftReference implements DisposableStrike {
        private FontStrikeDisposer disposer;
        public FontStrikeDisposer getDisposer() {
            return disposer;
        }
        SoftDisposerRef(FontStrike strike) {
            super(strike, StrikeCache.refQueue);
            disposer = strike.disposer;
            Disposer.addReference(this, disposer);
        }
    }
    static class WeakDisposerRef
        extends WeakReference implements DisposableStrike {
        private FontStrikeDisposer disposer;
        public FontStrikeDisposer getDisposer() {
            return disposer;
        }
        WeakDisposerRef(FontStrike strike) {
            super(strike, StrikeCache.refQueue);
            disposer = strike.disposer;
            Disposer.addReference(this, disposer);
        }
    }
}
