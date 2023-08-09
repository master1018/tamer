class ImageCache {
    private final LinkedHashMap<Integer, PixelCountSoftReference> map =
            new LinkedHashMap<Integer, PixelCountSoftReference>(16, 0.75f, true);
    private final int maxPixelCount;
    private final int maxSingleImagePixelSize;
    private int currentPixelCount = 0;
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private ReferenceQueue<Image> referenceQueue = new ReferenceQueue<Image>();
    private static final ImageCache instance = new ImageCache();
    static ImageCache getInstance() {
        return instance;
    }
    public ImageCache() {
        this.maxPixelCount = (8 * 1024 * 1024) / 4; 
        this.maxSingleImagePixelSize = 300 * 300;
    }
    public ImageCache(int maxPixelCount, int maxSingleImagePixelSize) {
        this.maxPixelCount = maxPixelCount;
        this.maxSingleImagePixelSize = maxSingleImagePixelSize;
    }
    public void flush() {
        lock.readLock().lock();
        try {
            map.clear();
        } finally {
            lock.readLock().unlock();
        }
    }
    public boolean isImageCachable(int w, int h) {
        return (w * h) < maxSingleImagePixelSize;
    }
    public Image getImage(GraphicsConfiguration config, int w, int h, Object... args) {
        lock.readLock().lock();
        try {
            PixelCountSoftReference ref = map.get(hash(config, w, h, args));
            if (ref != null && ref.equals(config,w, h, args)) {
                return ref.get();
            } else {
                return null;
            }
        } finally {
            lock.readLock().unlock();
        }
    }
    public boolean setImage(Image image, GraphicsConfiguration config, int w, int h, Object... args) {
        if (!isImageCachable(w, h)) return false;
        int hash = hash(config, w, h, args);
        lock.writeLock().lock();
        try {
            PixelCountSoftReference ref = map.get(hash);
            if (ref != null && ref.get() == image) {
                return true;
            }
            if (ref != null) {
                currentPixelCount -= ref.pixelCount;
                map.remove(hash);
            }
            int newPixelCount = image.getWidth(null) * image.getHeight(null);
            currentPixelCount += newPixelCount;
            if (currentPixelCount > maxPixelCount) {
                while ((ref = (PixelCountSoftReference)referenceQueue.poll()) != null){
                    map.remove(ref.hash);
                    currentPixelCount -= ref.pixelCount;
                }
            }
            if (currentPixelCount > maxPixelCount) {
                Iterator<Map.Entry<Integer, PixelCountSoftReference>> mapIter = map.entrySet().iterator();
                while ((currentPixelCount > maxPixelCount) && mapIter.hasNext()) {
                    Map.Entry<Integer, PixelCountSoftReference> entry = mapIter.next();
                    mapIter.remove();
                    Image img = entry.getValue().get();
                    if (img != null) img.flush();
                    currentPixelCount -= entry.getValue().pixelCount;
                }
            }
            map.put(hash, new PixelCountSoftReference(image, referenceQueue, newPixelCount,hash, config, w, h, args));
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }
    private int hash(GraphicsConfiguration config, int w, int h, Object ... args) {
        int hash;
        hash = (config != null ? config.hashCode() : 0);
        hash = 31 * hash + w;
        hash = 31 * hash + h;
        hash = 31 * hash + Arrays.deepHashCode(args);
        return hash;
    }
    private static class PixelCountSoftReference extends SoftReference<Image> {
        private final int pixelCount;
        private final int hash;
        private final GraphicsConfiguration config;
        private final int w;
        private final int h;
        private final Object[] args;
        public PixelCountSoftReference(Image referent, ReferenceQueue<? super Image> q, int pixelCount, int hash,
                                       GraphicsConfiguration config, int w, int h, Object[] args) {
            super(referent, q);
            this.pixelCount = pixelCount;
            this.hash = hash;
            this.config = config;
            this.w = w;
            this.h = h;
            this.args = args;
        }
        public boolean equals (GraphicsConfiguration config, int w, int h, Object[] args){
            return config == this.config &&
                            w == this.w &&
                            h == this.h &&
                            Arrays.equals(args, this.args);
        }
    }
}
