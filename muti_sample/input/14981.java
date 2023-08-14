public class FillPPathTest {
    static final int IMG_WIDTH = 100;
    static final int IMG_HEIGHT = 100;
    private static BufferedImage createCustomImage() {
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        ComponentColorModel cm =
            new ComponentColorModel(cs, false, false, Transparency.OPAQUE,
                                    DataBuffer.TYPE_FLOAT);
        WritableRaster raster =
            cm.createCompatibleWritableRaster(IMG_WIDTH, IMG_HEIGHT);
        return new BufferedImage(cm, raster, false, null);
    }
    public static void main(String[] args) throws Exception {
        Path2D oddShape = new Path2D.Double();
        oddShape.moveTo(50,10);
        oddShape.curveTo(50,10,10,20,50,30);
        oddShape.moveTo(50,30);
        BufferedImage img = new BufferedImage(IMG_WIDTH, IMG_HEIGHT,
                                              BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0,0,IMG_WIDTH,IMG_HEIGHT);
        g2d.setColor(Color.WHITE);
        g2d.fill(oddShape);
        if (img.getRGB(60, 20) != Color.BLACK.getRGB()) {
            throw new RuntimeException("Error. Invalid pixel at (60,20)");
        }
        img = createCustomImage();
        g2d = img.createGraphics();
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0,0,IMG_WIDTH,IMG_HEIGHT);
        g2d.setColor(Color.WHITE);
        g2d.fill(oddShape);
        if (img.getRGB(60, 20) != Color.BLACK.getRGB()) {
            throw new RuntimeException("Error. Invalid pixel at (60,20)");
        }
    }
}
