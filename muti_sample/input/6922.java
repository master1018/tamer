public abstract class SurfaceManager {
    public static abstract class ImageAccessor {
        public abstract SurfaceManager getSurfaceManager(Image img);
        public abstract void setSurfaceManager(Image img, SurfaceManager mgr);
    }
    private static ImageAccessor imgaccessor;
    public static void setImageAccessor(ImageAccessor ia) {
        if (imgaccessor != null) {
            throw new InternalError("Attempt to set ImageAccessor twice");
        }
        imgaccessor = ia;
    }
    public static SurfaceManager getManager(Image img) {
        SurfaceManager sMgr = imgaccessor.getSurfaceManager(img);
        if (sMgr == null) {
            try {
                BufferedImage bi = (BufferedImage) img;
                sMgr = new BufImgSurfaceManager(bi);
                setManager(bi, sMgr);
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("Invalid Image variant");
            }
        }
        return sMgr;
    }
    public static void setManager(Image img, SurfaceManager mgr) {
        imgaccessor.setSurfaceManager(img, mgr);
    }
    private ConcurrentHashMap cacheMap;
    public Object getCacheData(Object key) {
        return (cacheMap == null) ? null : cacheMap.get(key);
    }
    public void setCacheData(Object key, Object value) {
        if (cacheMap == null) {
            synchronized (this) {
                if (cacheMap == null) {
                    cacheMap = new ConcurrentHashMap(2);
                }
            }
        }
        cacheMap.put(key, value);
    }
    public abstract SurfaceData getPrimarySurfaceData();
    public abstract SurfaceData restoreContents();
    public void acceleratedSurfaceLost() {}
    public ImageCapabilities getCapabilities(GraphicsConfiguration gc) {
        return new ImageCapabilitiesGc(gc);
    }
    class ImageCapabilitiesGc extends ImageCapabilities {
        GraphicsConfiguration gc;
        public ImageCapabilitiesGc(GraphicsConfiguration gc) {
            super(false);
            this.gc = gc;
        }
        public boolean isAccelerated() {
            GraphicsConfiguration tmpGc = gc;
            if (tmpGc == null) {
                tmpGc = GraphicsEnvironment.getLocalGraphicsEnvironment().
                    getDefaultScreenDevice().getDefaultConfiguration();
            }
            if (tmpGc instanceof ProxiedGraphicsConfig) {
                Object proxyKey =
                    ((ProxiedGraphicsConfig) tmpGc).getProxyKey();
                if (proxyKey != null) {
                    SurfaceDataProxy sdp =
                        (SurfaceDataProxy) getCacheData(proxyKey);
                    return (sdp != null && sdp.isAccelerated());
                }
            }
            return false;
        }
    }
    public static interface ProxiedGraphicsConfig {
        public Object getProxyKey();
    }
    public synchronized void flush() {
        flush(false);
    }
    synchronized void flush(boolean deaccelerate) {
        if (cacheMap != null) {
            Iterator i = cacheMap.values().iterator();
            while (i.hasNext()) {
                Object o = i.next();
                if (o instanceof FlushableCacheData) {
                    if (((FlushableCacheData) o).flush(deaccelerate)) {
                        i.remove();
                    }
                }
            }
        }
    }
    public static interface FlushableCacheData {
        public boolean flush(boolean deaccelerated);
    }
    public void setAccelerationPriority(float priority) {
        if (priority == 0.0f) {
            flush(true);
        }
    }
}
