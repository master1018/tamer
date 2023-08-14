public class AndroidGraphicsConfiguration extends GraphicsConfiguration {
    @Override
    public BufferedImage createCompatibleImage(int width, int height) {
        return null;
    }
    @Override
    public BufferedImage createCompatibleImage(int width, int height,
            int transparency) {
        return null;
    }
    @Override
    public VolatileImage createCompatibleVolatileImage(int width, int height) {
        return null;
    }
    @Override
    public VolatileImage createCompatibleVolatileImage(int width, int height,
            int transparency) {
        return null;
    }
    @Override
    public Rectangle getBounds() {
        Canvas c = AndroidGraphics2D.getAndroidCanvas();
        if(c != null)
            return new Rectangle(0, 0, c.getWidth(), c.getHeight());
        return null;
    }
    @Override
    public ColorModel getColorModel() {
        return null;
    }
    @Override
    public ColorModel getColorModel(int transparency) {
        return null;
    }
    @Override
    public AffineTransform getDefaultTransform() {
        return new AffineTransform();
    }
    @Override
    public GraphicsDevice getDevice() {
        return null;
    }
    @Override
    public AffineTransform getNormalizingTransform() {
        return null;
    }
}
