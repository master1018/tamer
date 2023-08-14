public abstract class VolatileImage extends Image
        implements Transparency {
    public static final int IMAGE_INCOMPATIBLE = 2;
    public static final int IMAGE_OK = 0;
    public static final int IMAGE_RESTORED = 1;
    protected int transparency = OPAQUE;
    public VolatileImage() {
        super();
    }
    public abstract boolean contentsLost();
    public abstract Graphics2D createGraphics();
    public abstract ImageCapabilities getCapabilities();
    public abstract int getHeight();
    public abstract BufferedImage getSnapshot();
    public abstract int getWidth();
    public abstract int validate(GraphicsConfiguration gc);
    @Override
    public void flush() {
    }
    @Override
    public Graphics getGraphics() {
        return createGraphics();
    }
    @Override
    public ImageProducer getSource() {
        return getSnapshot().getSource();
    }
    public int getTransparency() {
        return transparency;
    }
}
