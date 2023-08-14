public class RGBColorConvertTest {
    public static void main(String [] args) {
        BufferedImage src =
            new BufferedImage(256,3,BufferedImage.TYPE_INT_RGB);
        BufferedImage dst =
            new BufferedImage(256,3,BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < 256; i++) {
            src.setRGB(i,0,i);
            src.setRGB(i,1,i << 8);
            src.setRGB(i,2,i << 16);
        }
        ColorSpace srcColorSpace = src.getColorModel().getColorSpace();
        ColorConvertOp op = new ColorConvertOp(srcColorSpace, srcColorSpace,
                                               null);
        op.filter(src, dst);
        int errCount = 0;
        for (int i = 0; i < src.getWidth(); i++) {
            for (int j = 0; j < src.getHeight(); j++) {
                int scol = src.getRGB(i,j);
                int dcol = dst.getRGB(i,j);
                if (scol != dcol) {
                    System.err.println("(" + i + "," + j + ") : " +
                                       Integer.toHexString(scol) + "!=" +
                                       Integer.toHexString(dcol));
                    errCount++;
                }
            }
        }
        if (errCount > 0) {
            throw new RuntimeException(errCount + " pixels are changed by " +
                                       "transform between the same ICC color " +
                                       "spaces");
        }
    }
}
