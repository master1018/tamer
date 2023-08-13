public abstract class GraphicsConfiguration {
    protected GraphicsConfiguration() {
    }
    public abstract BufferedImage createCompatibleImage(int width, int height);
    public abstract BufferedImage createCompatibleImage(int width, int height, int transparency);
    public abstract VolatileImage createCompatibleVolatileImage(int width, int height);
    public abstract VolatileImage createCompatibleVolatileImage(int width, int height,
            int transparency);
    public abstract Rectangle getBounds();
    public abstract ColorModel getColorModel();
    public abstract ColorModel getColorModel(int transparency);
    public abstract AffineTransform getDefaultTransform();
    public abstract GraphicsDevice getDevice();
    public abstract AffineTransform getNormalizingTransform();
    public VolatileImage createCompatibleVolatileImage(int width, int height, ImageCapabilities caps)
            throws AWTException {
        VolatileImage res = createCompatibleVolatileImage(width, height);
        if (!res.getCapabilities().equals(caps)) {
            throw new AWTException(Messages.getString("awt.14A")); 
        }
        return res;
    }
    public VolatileImage createCompatibleVolatileImage(int width, int height,
            ImageCapabilities caps, int transparency) throws AWTException {
        VolatileImage res = createCompatibleVolatileImage(width, height, transparency);
        if (!res.getCapabilities().equals(caps)) {
            throw new AWTException(Messages.getString("awt.14A")); 
        }
        return res;
    }
    public BufferCapabilities getBufferCapabilities() {
        return new BufferCapabilities(new ImageCapabilities(false), new ImageCapabilities(false),
                BufferCapabilities.FlipContents.UNDEFINED);
    }
    public ImageCapabilities getImageCapabilities() {
        return new ImageCapabilities(false);
    }
}
