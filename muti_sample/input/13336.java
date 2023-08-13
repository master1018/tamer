public class BufferedImageGraphicsConfig
    extends GraphicsConfiguration
{
    private static final int numconfigs = BufferedImage.TYPE_BYTE_BINARY;
    private static BufferedImageGraphicsConfig configs[] =
        new BufferedImageGraphicsConfig[numconfigs];
    public static BufferedImageGraphicsConfig getConfig(BufferedImage bImg) {
        BufferedImageGraphicsConfig ret;
        int type = bImg.getType();
        if (type > 0 && type < numconfigs) {
            ret = configs[type];
            if (ret != null) {
                return ret;
            }
        }
        ret = new BufferedImageGraphicsConfig(bImg, null);
        if (type > 0 && type < numconfigs) {
            configs[type] = ret;
        }
        return ret;
    }
    GraphicsDevice gd;
    ColorModel model;
    Raster raster;
    int width, height;
    public BufferedImageGraphicsConfig(BufferedImage bufImg, Component comp) {
        if (comp == null) {
            this.gd = new BufferedImageDevice(this);
        } else {
            Graphics2D g2d = (Graphics2D)comp.getGraphics();
            this.gd = g2d.getDeviceConfiguration().getDevice();
        }
        this.model = bufImg.getColorModel();
        this.raster = bufImg.getRaster().createCompatibleWritableRaster(1, 1);
        this.width = bufImg.getWidth();
        this.height = bufImg.getHeight();
    }
    public GraphicsDevice getDevice() {
        return gd;
    }
    public BufferedImage createCompatibleImage(int width, int height) {
        WritableRaster wr = raster.createCompatibleWritableRaster(width, height);
        return new BufferedImage(model, wr, model.isAlphaPremultiplied(), null);
    }
    public ColorModel getColorModel() {
        return model;
    }
    public ColorModel getColorModel(int transparency) {
        if (model.getTransparency() == transparency) {
            return model;
        }
        switch (transparency) {
        case Transparency.OPAQUE:
            return new DirectColorModel(24, 0xff0000, 0xff00, 0xff);
        case Transparency.BITMASK:
            return new DirectColorModel(25, 0xff0000, 0xff00, 0xff, 0x1000000);
        case Transparency.TRANSLUCENT:
            return ColorModel.getRGBdefault();
        default:
            return null;
        }
    }
    public AffineTransform getDefaultTransform() {
        return new AffineTransform();
    }
    public AffineTransform getNormalizingTransform() {
        return new AffineTransform();
    }
    public Rectangle getBounds() {
        return new Rectangle(0, 0, width, height);
    }
}
