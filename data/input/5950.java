public class PrinterGraphicsConfig extends GraphicsConfiguration {
    static ColorModel theModel;
    GraphicsDevice gd;
    int pageWidth, pageHeight;
    AffineTransform deviceTransform;
    public PrinterGraphicsConfig(String printerID, AffineTransform deviceTx,
                                 int pageWid, int pageHgt) {
        this.pageWidth = pageWid;
        this.pageHeight = pageHgt;
        this.deviceTransform = deviceTx;
        this.gd = new PrinterGraphicsDevice(this, printerID);
    }
    public GraphicsDevice getDevice() {
        return gd;
    }
    public ColorModel getColorModel() {
        if (theModel == null) {
            BufferedImage bufImg =
                new BufferedImage(1,1, BufferedImage.TYPE_3BYTE_BGR);
            theModel = bufImg.getColorModel();
        }
        return theModel;
    }
    public ColorModel getColorModel(int transparency) {
        switch (transparency) {
        case Transparency.OPAQUE:
            return getColorModel();
        case Transparency.BITMASK:
            return new DirectColorModel(25, 0xff0000, 0xff00, 0xff, 0x1000000);
        case Transparency.TRANSLUCENT:
            return ColorModel.getRGBdefault();
        default:
            return null;
        }
    }
    public AffineTransform getDefaultTransform() {
        return new AffineTransform(deviceTransform);
    }
    public AffineTransform getNormalizingTransform() {
        return new AffineTransform();
    }
    public Rectangle getBounds() {
        return new Rectangle(0, 0, pageWidth, pageHeight);
    }
}
