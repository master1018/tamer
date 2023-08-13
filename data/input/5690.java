public class MTSafetyTest {
    static boolean failed = false;
    static int[] colorSpaceType = {
        ColorSpace.CS_CIEXYZ,
        ColorSpace.CS_GRAY,
        ColorSpace.CS_LINEAR_RGB,
        ColorSpace.CS_PYCC,
        ColorSpace.CS_sRGB
    };
    static private final int[] imageTypes = new int[] {
        BufferedImage.TYPE_INT_RGB,
        BufferedImage.TYPE_INT_ARGB,
        BufferedImage.TYPE_INT_ARGB_PRE,
        BufferedImage.TYPE_INT_BGR,
        BufferedImage.TYPE_3BYTE_BGR,
        BufferedImage.TYPE_4BYTE_ABGR,
        BufferedImage.TYPE_4BYTE_ABGR_PRE,
        BufferedImage.TYPE_USHORT_565_RGB,
        BufferedImage.TYPE_USHORT_555_RGB,
        BufferedImage.TYPE_BYTE_GRAY,
        BufferedImage.TYPE_USHORT_GRAY,
        BufferedImage.TYPE_BYTE_BINARY,
        BufferedImage.TYPE_BYTE_INDEXED
    };
    public static void main(String[] args) {
        int nImgTypes = imageTypes.length;
        int nCSTypes = colorSpaceType.length;
        Vector<Thread> threads =
            new Vector<Thread>(nImgTypes*nCSTypes*nCSTypes);
        for (int i = 0; i < nImgTypes; i++) {
            BufferedImage origImage =
                new BufferedImage(300, 300, imageTypes[i]);
            for (int j = 0; j < nCSTypes; j++) {
                for (int k = 0; k < nCSTypes; k++) {
                    Graphics2D g2 = (Graphics2D) origImage.getGraphics();
                    g2.fillRect(0, 0, 300, 150);
                    ColorConvertOp colorOp = getColorConvertOp(j,k);
                    ColorConvert cc = new ColorConvert(origImage, colorOp);
                    Thread colorThread = new Thread(cc);
                    threads.add(colorThread);
                    colorThread.start();
                }
            }
        }
        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Unexpected exception" + e);
        }
        if (failed) {
            throw new RuntimeException("Unexpected exception");
        }
    }
    private static ColorConvertOp getColorConvertOp(int srcIndex,
                                                    int destIndex)
    {
        ColorSpace srcColorSpace = ColorSpace.getInstance(
            colorSpaceType[srcIndex]);
        ColorSpace destColorSpace = ColorSpace.getInstance(
            colorSpaceType[destIndex]);
        return new ColorConvertOp(srcColorSpace, destColorSpace, null);
    }
    static class ColorConvert implements Runnable {
        BufferedImage original = null;
        ColorConvertOp colorOp = null;
        public ColorConvert(BufferedImage orig, ColorConvertOp ccOp) {
            original = orig;
            colorOp = ccOp;
        }
        public void run() {
            try {
                colorOp.filter(original, null);
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
                failed = true;
            }
        }
    }
}
