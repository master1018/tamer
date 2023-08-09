public abstract class CachedPainter {
    private static final Map<Object,ImageCache> cacheMap =
                   new HashMap<Object,ImageCache>();
    private static ImageCache getCache(Object key) {
        synchronized(CachedPainter.class) {
            ImageCache cache = cacheMap.get(key);
            if (cache == null) {
                cache = new ImageCache(1);
                cacheMap.put(key, cache);
            }
            return cache;
        }
    }
    public CachedPainter(int cacheCount) {
        getCache(getClass()).setMaxCount(cacheCount);
    }
    public void paint(Component c, Graphics g, int x,
                         int y, int w, int h, Object... args) {
        if (w <= 0 || h <= 0) {
            return;
        }
        if (c != null) {
            synchronized(c.getTreeLock()) {
                synchronized(CachedPainter.class) {
                    paint0(c, g, x, y, w, h, args);
                }
            }
        }
        else {
            synchronized(CachedPainter.class) {
                paint0(c, g, x, y, w, h, args);
            }
        }
    }
    private void paint0(Component c, Graphics g, int x,
                         int y, int w, int h, Object... args) {
        Object key = getClass();
        GraphicsConfiguration config = getGraphicsConfiguration(c);
        ImageCache cache = getCache(key);
        Image image = cache.getImage(key, config, w, h, args);
        int attempts = 0;
        do {
            boolean draw = false;
            if (image instanceof VolatileImage) {
                switch (((VolatileImage)image).validate(config)) {
                case VolatileImage.IMAGE_INCOMPATIBLE:
                    ((VolatileImage)image).flush();
                    image = null;
                    break;
                case VolatileImage.IMAGE_RESTORED:
                    draw = true;
                    break;
                }
            }
            if (image == null) {
                image = createImage(c, w, h, config, args);
                cache.setImage(key, config, w, h, args, image);
                draw = true;
            }
            if (draw) {
                Graphics g2 = image.getGraphics();
                paintToImage(c, image, g2, w, h, args);
                g2.dispose();
            }
            paintImage(c, g, x, y, w, h, image, args);
        } while ((image instanceof VolatileImage) &&
                 ((VolatileImage)image).contentsLost() && ++attempts < 3);
    }
    protected abstract void paintToImage(Component c, Image image, Graphics g,
                                         int w, int h, Object[] args);
    protected void paintImage(Component c, Graphics g,
                              int x, int y, int w, int h, Image image,
                              Object[] args) {
        g.drawImage(image, x, y, null);
    }
    protected Image createImage(Component c, int w, int h,
                                GraphicsConfiguration config, Object[] args) {
        if (config == null) {
            return new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        }
        return config.createCompatibleVolatileImage(w, h);
    }
    protected void flush() {
        synchronized(CachedPainter.class) {
            getCache(getClass()).flush();
        }
    }
    private GraphicsConfiguration getGraphicsConfiguration(Component c) {
        if (c == null) {
            return null;
        }
        return c.getGraphicsConfiguration();
    }
}
