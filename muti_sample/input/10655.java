public class LUTCompareTest implements ImageObserver {
    public static void main(String[] args) throws IOException {
        Image img = createTestImage();
        Toolkit tk = Toolkit.getDefaultToolkit();
        LUTCompareTest o = new LUTCompareTest(img);
        tk.prepareImage(img, -1, -1, o);
        while(!o.isImageReady()) {
            synchronized(lock) {
                try {
                    lock.wait(200);
                } catch (InterruptedException e) {
                }
            }
        }
        checkResults(img);
    }
    private static Object lock = new Object();
    Image image;
    boolean isReady = false;
    public LUTCompareTest(Image img) {
        this.image = img;
    }
    public boolean imageUpdate(Image image, int info,
                               int x, int y, int w, int h) {
        if (image == this.image) {
            System.out.println("Image status: " + dump(info));
            synchronized(this) {
                isReady = (info & ImageObserver.ALLBITS) != 0;
                    if (isReady) {
                        synchronized(lock) {
                            lock.notifyAll();
                        }
                    }
            }
            return !isReady;
        } else {
            return true;
        }
    }
    public synchronized boolean isImageReady() {
        return isReady;
    }
    private static void checkResults(Image image) {
        BufferedImage buf = new BufferedImage(w, h,
                                              BufferedImage.TYPE_INT_RGB);
        Graphics2D g = buf.createGraphics();
        g.setColor(Color.pink);
        g.fillRect(0, 0, w, h);
        g.drawImage(image, 0, 0, null);
        g.dispose();
        int rgb = buf.getRGB(w/2, h/2);
        System.out.printf("Result color: %x\n", rgb);
        if (rgb != 0xff0000ff) {
            throw new RuntimeException("Test FAILED!");
        }
        System.out.println("Test PASSED.");
    }
    private static int w = 100;
    private static int h = 100;
    private static Image createTestImage() throws IOException  {
        BufferedImage frame1 = createFrame(new int[] { 0xffff0000, 0xffff0000 });
        BufferedImage frame2 = createFrame(new int[] { 0xff0000ff, 0xffff0000 });
        ImageWriter writer = ImageIO.getImageWritersByFormatName("GIF").next();
        ImageOutputStream ios = ImageIO.createImageOutputStream(new File("lut_test.gif"));
        ImageWriteParam param = writer.getDefaultWriteParam();
        writer.setOutput(ios);
        writer.prepareWriteSequence(null);
        writer.writeToSequence(new IIOImage(frame1, null, null), param);
        writer.writeToSequence(new IIOImage(frame2, null, null), param);
        writer.endWriteSequence();
        writer.reset();
        writer.dispose();
        ios.flush();
        ios.close();
        return Toolkit.getDefaultToolkit().createImage("lut_test.gif");
    }
    private static BufferedImage createFrame(int[] palette) {
        IndexColorModel icm = new IndexColorModel(getNumBits(palette.length),
            palette.length, palette, 0, false, -1, DataBuffer.TYPE_BYTE);
        WritableRaster wr = icm.createCompatibleWritableRaster(w, h);
        int[] samples = new int[w * h];
        Arrays.fill(samples, 0);
        wr.setSamples(0, 0, w, h, 0, samples);
        BufferedImage img = new BufferedImage(icm, wr, false, null);
        return img;
    }
    private static int getNumBits(int size) {
        if (size < 0) {
            throw new RuntimeException("Invalid palette size: " + size);
        } else if (size < 3) {
            return 1;
        } else if (size < 5) {
            return 2;
        } else {
            throw new RuntimeException("Palette size is not supported: " + size);
        }
    }
     private static String[] name = new String[] {
        "WIDTH", "HEIGHT", "PROPERTIES", "SOMEBITS",
        "FRAMEBITS", "ALLBITS", "ERROR", "ABORT"
    };
    private static String dump(int info) {
        String res = "";
        int count = 0;
        while (info != 0) {
            if ((info & 0x1) == 1) {
                res += name[count];
                if ((info >> 1) != 0) {
                    res += " ";
                }
            }
            count ++;
            info = (info >> 1);
        }
        return res;
    }
}
