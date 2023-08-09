public class ColCvtIntARGB {
    public static void main(String args[]) {
        BufferedImage src
            = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Color pelColor = new Color(100, 100, 100, 128);
        src.setRGB(0, 0, pelColor.getRGB());
        ColorConvertOp op = new ColorConvertOp(null);
        BufferedImage dst
            = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB_PRE);
        op.filter(src, dst);
        if (((dst.getRGB(0, 0) >> 24) & 0xff) != 128) {
            throw new RuntimeException("Incorrect destination alpha value.");
        }
        dst = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        op.filter(src, dst);
        if (((dst.getRGB(0, 0) >> 24) & 0xff) != 128) {
            throw new RuntimeException("Incorrect destination alpha value.");
        }
    }
}
