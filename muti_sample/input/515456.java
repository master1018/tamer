public abstract class Image {
    public static final Object UndefinedProperty = new Object(); 
    public static final int SCALE_DEFAULT = 1;
    public static final int SCALE_FAST = 2;
    public static final int SCALE_SMOOTH = 4;
    public static final int SCALE_REPLICATE = 8;
    public static final int SCALE_AREA_AVERAGING = 16;
    protected float accelerationPriority = 0.5f;
    private static final ImageCapabilities capabilities = new ImageCapabilities(false);
    public abstract Object getProperty(String name, ImageObserver observer);
    public abstract ImageProducer getSource();
    public abstract int getWidth(ImageObserver observer);
    public abstract int getHeight(ImageObserver observer);
    public Image getScaledInstance(int width, int height, int hints) {
        ImageFilter filter;
        if ((hints & (SCALE_SMOOTH | SCALE_AREA_AVERAGING)) != 0) {
            filter = new AreaAveragingScaleFilter(width, height);
        } else {
            filter = new ReplicateScaleFilter(width, height);
        }
        ImageProducer producer = new FilteredImageSource(getSource(), filter);
        return Toolkit.getDefaultToolkit().createImage(producer);
    }
    public abstract Graphics getGraphics();
    public abstract void flush();
    public float getAccelerationPriority() {
        return accelerationPriority;
    }
    public void setAccelerationPriority(float priority) {
        if (priority < 0 || priority > 1) {
            throw new IllegalArgumentException(Messages.getString("awt.10A")); 
        }
        accelerationPriority = priority;
    }
    public ImageCapabilities getCapabilities(GraphicsConfiguration gc) {
        return capabilities;
    }
}
