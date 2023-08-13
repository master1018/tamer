public class EncodeSubImageTest {
    private static String format = "gif";
    private static ImageWriter writer;
    private static String file_suffix;
    private static final int subSampleX = 2;
    private static final int subSampleY = 2;
    public static void main(String[] args) throws IOException {
        if (args.length > 0) {
            format = args[0];
        }
        writer = ImageIO.getImageWritersByFormatName(format).next();
        file_suffix =writer.getOriginatingProvider().getFileSuffixes()[0];
        BufferedImage src = createTestImage();
        EncodeSubImageTest m1 = new EncodeSubImageTest(src);
        m1.doTest("test_src");
        BufferedImage sub = src.getSubimage(subImageOffset, subImageOffset,
                src.getWidth() - 2 * subImageOffset,
                src.getHeight() - 2 * subImageOffset);
        EncodeSubImageTest m2 = new EncodeSubImageTest(sub);
        m2.doTest("test_sub");
    }
    BufferedImage img;
    public EncodeSubImageTest(BufferedImage img) {
        this.img = img;
    }
    public void doTest(String prefix) throws IOException {
        System.out.println(prefix);
        File f = new File(prefix + file_suffix);
        write(f, false);
        verify(f, false);
        System.out.println(prefix + "_subsampled");
        f = new File(prefix + "_subsampled");
        write(f, true);
        verify(f, true);
        System.out.println(prefix + ": Test PASSED.");
    }
    private static final int subImageOffset = 10;
    private void verify(File f, boolean isSubsampled) {
        BufferedImage dst = null;
        try {
            dst = ImageIO.read(f);
        } catch (IOException e) {
            throw new RuntimeException("Test FAILED: can't readin test image " +
                f.getAbsolutePath(), e);
        }
        if (dst == null) {
            throw new RuntimeException("Test FAILED: no dst image available.");
        }
        checkPixel(dst, 0, 0, isSubsampled);
        checkPixel(dst, img.getWidth() / 2, img.getHeight() / 2, isSubsampled);
    }
    private void checkPixel(BufferedImage dst, int x, int y,
                            boolean isSubsampled)
    {
        int dx = isSubsampled ? x / subSampleX : x;
        int dy = isSubsampled ? y / subSampleY : y;
        int src_rgb = img.getRGB(x, y);
        System.out.printf("src_rgb: %x\n", src_rgb);
        int dst_rgb = dst.getRGB(dx, dy);
        System.out.printf("dst_rgb: %x\n", dst_rgb);
        if (src_rgb != dst_rgb) {
            throw new RuntimeException("Test FAILED: invalid color in dst");
        }
    }
    private static BufferedImage createTestImage() {
        int w = 100;
        int h = 100;
        BufferedImage src = new BufferedImage(w, h,
                BufferedImage.TYPE_BYTE_INDEXED);
        Graphics g = src.createGraphics();
        g.setColor(Color.red);
        g.fillRect(0, 0, w, h);
        g.setColor(Color.green);
        g.fillRect(subImageOffset, subImageOffset,
                w - 2 * subImageOffset, h - 2* subImageOffset);
        g.setColor(Color.blue);
        g.fillRect(2 * subImageOffset, 2 * subImageOffset,
                w - 4 * subImageOffset, h - 4 * subImageOffset);
        g.dispose();
        return src;
    }
    private void write(File f, boolean subsample) throws IOException {
        ImageOutputStream ios = ImageIO.createImageOutputStream(f);
        writer.setOutput(ios);
        ImageWriteParam p = writer.getDefaultWriteParam();
        if (subsample) {
            p.setSourceSubsampling(subSampleX, subSampleY, 0, 0);
        }
        writer.write(null, new IIOImage(img, null, null), p);
        ios.close();
        writer.reset();
    }
}
