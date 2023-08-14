public class ColorConvertTest {
    public static void main(String[] args) {
        ColorSpace rgbCS = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        ColorSpace grayCS = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        ColorConvertOp op = new ColorConvertOp(rgbCS, grayCS, null);
        BufferedImage src = new BufferedImage(100, 100,
                                              BufferedImage.TYPE_INT_RGB);
        BufferedImage dst = new BufferedImage(100, 100,
                                              BufferedImage.TYPE_BYTE_GRAY);
        try {
            op.filter(src, dst);
        } catch (ProfileDataException ex) {
            throw new RuntimeException("Test Failed", ex);
        }
    }
}
