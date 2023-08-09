public class ColorSpaceCvtCrashTest {
    public static void main(String argv[]) {
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        ColorConvertOp theOp = new ColorConvertOp(cs, null);
        BufferedImage srcImg =
            new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImage dstImg =
            new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        theOp.filter(srcImg, dstImg);
        System.err.println("Test passed");
    }
}
