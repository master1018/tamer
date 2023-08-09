public class ColConvCCMTest extends ColConvTest {
    final static int [] dataTypes = {
        DataBuffer.TYPE_BYTE,
        DataBuffer.TYPE_DOUBLE,
        DataBuffer.TYPE_FLOAT,
        DataBuffer.TYPE_INT,
        DataBuffer.TYPE_SHORT,
        DataBuffer.TYPE_USHORT
    };
    final static int [] cSpaces = {
        ColorSpace.CS_sRGB,
        ColorSpace.CS_LINEAR_RGB,
        ColorSpace.CS_GRAY,
        ColorSpace.CS_PYCC,
        ColorSpace.CS_CIEXYZ
    };
    final static double [] ACCURACY = {
        2.5,        
        6.5,        
        10.5,       
        45.5,       
        47.5        
    };
    final static String [] gldImgNames = {
        "SRGB.png", "LRGB.png", "GRAY.png", "PYCC.png",  "CIEXYZ.png"
    };
    static BufferedImage [] gldImages = null;
    static boolean testImage(int dataType, int rBits, int gBits, int bBits,
                              int cs, BufferedImage gldImage,
                              double accuracy)
     {
        BufferedImage src = ImageFactory.createCCMImage(cs, dataType);
        BufferedImage dst = ImageFactory.createDstImage(
            BufferedImage.TYPE_INT_RGB);
        ColorConvertOp op = new ColorConvertOp(null);
        op.filter(src, dst);
        ImageComparator cmp = new ImageComparator(accuracy, rBits, gBits,
                                                  bBits);
        boolean result = cmp.compare(gldImage, dst);
        if (!result) {
            System.err.println(cmp.getStat());
        }
        return result;
    }
     static boolean testSubImage(int x0, int y0, int dx, int dy,
                                 int dataType, int rBits, int gBits,
                                 int bBits, int cs, BufferedImage gldImage,
                                 double accuracy)
     {
        BufferedImage src = ImageFactory.createCCMImage(cs, dataType);
        BufferedImage subSrc = src.getSubimage(x0, y0, dx, dy);
        BufferedImage dst = ImageFactory.createDstImage(
            BufferedImage.TYPE_INT_RGB);
        BufferedImage subDst = dst.getSubimage(x0, y0, dx, dy);
        ColorConvertOp op = new ColorConvertOp(null);
        op.filter(subSrc, subDst);
        ImageComparator cmp = new ImageComparator(accuracy, rBits, gBits,
                                                  bBits);
        boolean result = cmp.compare(subDst, gldImage, x0, y0, dx, dy);
        if (!result) {
            System.err.println(cmp.getStat());
        }
        return result;
    }
     synchronized public static void initGoldenImages() {
        if (gldImages == null) {
            gldImages = new BufferedImage[gldImgNames.length];
            for (int i = 0; i < gldImgNames.length; i++) {
                try {
                    File gldFile = new File(System.getProperty("test.src", "."),
                                            gldImgNames[i]);
                    gldImages[i] = ImageIO.read(gldFile);
                } catch (IOException e) {
                    throw new RuntimeException("Cannot initialize golden " +
                                               "image: " + gldImgNames[i]);
                }
            }
        }
     }
     public void init() {
        initGoldenImages();
     }
     public void runTest() {
        for (int i = 0; i < cSpaces.length; i++) {
            BufferedImage gldImage = gldImages[i];
            for (int j = 0; j < dataTypes.length; j++) {
                if (!testImage(dataTypes[j], 8, 8, 8, cSpaces[i], gldImage,
                               ACCURACY[i]))
                {
                     throw new RuntimeException(
                        "Invalid result of the ColorConvertOp for " +
                        "ColorSpace:" + getCSName(cSpaces[i]) +
                        " Data type:" +
                        getDTName(dataTypes[j]) + ". Golden image:" +
                        gldImages[i]);
                 }
                 if (!testSubImage(SI_X, SI_Y, SI_W, SI_H, dataTypes[j],
                                   8, 8, 8, cSpaces[i], gldImage, ACCURACY[i]))
                 {
                    throw new RuntimeException(
                        "Invalid result of the ColorConvertOp for " +
                        "ColorSpace:" + getCSName(cSpaces[i]) +
                        " Data type:" +
                        getDTName(dataTypes[j]) + ". Golden image:" +
                        gldImages[i]);
                 }
            }
        }
     }
     public static void main(String [] args) throws Exception {
         ColConvCCMTest test = new ColConvCCMTest();
         test.init();
         test.run();
     }
}
