public class GrayPngTest {
    public static void main(String[] args) throws IOException {
        int bpp = 4;
        int trans_type = Transparency.BITMASK;
        int trans_pixel = 3;
        try {
            bpp = Integer.parseInt(args[0]);
            trans_type = Integer.parseInt(args[1]);
            trans_pixel = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            System.out.println("Ignore ncorrect bpp value: " + args[0]);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Default test argumens.");
        }
        new GrayPngTest(bpp).doTest(trans_type, trans_pixel);
    }
    private BufferedImage getTestImage(int trans_type, int trans_pixel) {
        IndexColorModel icm = null;
        switch(trans_type) {
            case Transparency.OPAQUE:
                icm = new IndexColorModel(bpp, numColors, r, g, b);
                break;
            case Transparency.BITMASK:
                icm = new IndexColorModel(bpp, numColors, r, g, b, trans_pixel);
                break;
            case Transparency.TRANSLUCENT:
                a = Arrays.copyOf(r, r.length);
                icm = new IndexColorModel(bpp, numColors, r, g, b, a);
                break;
            default:
                throw new RuntimeException("Invalid transparency: " + trans_type);
        }
        int w = 256 * 2;
        int h = 200;
        dx = w / (numColors);
        WritableRaster wr = icm.createCompatibleWritableRaster(w, h);
        for (int i = 0; i < numColors; i ++) {
            int rx = i * dx;
            int[] samples = new int[h * dx];
            Arrays.fill(samples, i);
            wr.setPixels(rx, 0, dx, h, samples);
        }
        int[] samples = new int[w * 10];
        Arrays.fill(samples, trans_pixel);
        wr.setPixels(0, h / 2 - 5, w, 10, samples);
        return new BufferedImage(icm, wr, false, null);
    }
    static File pwd = new File(".");
    private BufferedImage src;
    private BufferedImage dst;
    private int bpp;
    private int numColors;
    private int dx;
    private byte[] r;
    private byte[] g;
    private byte[] b;
    private byte[] a;
    protected GrayPngTest(int bpp) {
        if (0 > bpp || bpp > 8) {
            throw new RuntimeException("Invalid bpp: " + bpp);
        }
        this.bpp = bpp;
        numColors = (1 << bpp);
        System.out.println("Num colors: " + numColors);
        r = new byte[numColors];
        g = new byte[numColors];
        b = new byte[numColors];
        int dc = 0xff / (numColors - 1);
        System.out.println("dc = " + dc);
        for (int i = 0; i < numColors; i ++) {
            byte l = (byte)(i * dc);
            r[i] = l; g[i] = l; b[i] = l;
        }
    }
    public void doTest() throws IOException {
        for (int i = 0; i < numColors; i++) {
            doTest(Transparency.BITMASK, i);
        }
    }
    public void doTest(int trans_type, int trans_index) throws IOException {
        src = getTestImage(trans_type, trans_index);
        System.out.println("src: " + src);
        File f = File.createTempFile("gray_png_" + bpp + "bpp_" +
              trans_type + "tt_" +
                    trans_index + "tp_", ".png", pwd);
        System.out.println("File: " + f.getAbsolutePath());
        if (!ImageIO.write(src, "png", f)) {
            throw new RuntimeException("Writing failed!");
        };
        try {
            dst = ImageIO.read(f);
            System.out.println("dst: " + dst);
        } catch (Exception e) {
            throw new RuntimeException("Test FAILED.", e);
        }
        checkImages();
    }
    private void checkImages() {
        for (int i = 0; i < numColors; i++) {
            int src_rgb = src.getRGB(i * dx, 5);
            int dst_rgb = dst.getRGB(i * dx, 5);
            if ((0xff000000 & src_rgb) != (0xff000000 & dst_rgb)) {
                throw new RuntimeException("Test FAILED. Color difference detected: " +
                        Integer.toHexString(dst_rgb) + " instead of " +
                        Integer.toHexString(src_rgb) + " for index " + i);
            }
        }
    }
}
