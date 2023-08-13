public class ConcurrentReadingTest extends Thread {
    static ImageReader r = null;
    static File file = new File("IMGP1001.JPG");
    private static final int MAX_THREADS = 50;
    static int completeCount = 0;;
    static Object lock = new Object();
    public static void main(String[] args) throws Exception {
        createTestFile();
        ImageInputStream iis = ImageIO.createImageInputStream(file);
        r = ImageIO.getImageReaders(iis).next();
        iis.close();
        for (int i = 0; i < MAX_THREADS; i++) {
            (new ConcurrentReadingTest()).start();
        }
        boolean needWait = true;
        while (needWait) {
            Thread.sleep(100);
            synchronized(lock) {
                needWait = completeCount < MAX_THREADS;
            }
        }
        System.out.println("Test PASSED.");
    }
    public void run() {
        try {
            ImageInputStream iis = ImageIO.createImageInputStream(file);
            r.setInput(iis);
            ImageReadParam p = r.getDefaultReadParam();
            Thread.sleep(70);
            BufferedImage res = r.read(0, p);
            Thread.sleep(70);
            r.reset();
        } catch (IllegalStateException e) {
            System.out.println(e);
        } catch (IOException e) {
            System.out.println(e);
        } catch (Throwable e) {
            throw new RuntimeException("Test failed.", e);
        } finally {
            synchronized(lock) {
                completeCount ++;
            }
        }
    }
    private static void createTestFile() {
        int w = 1280;
        int h = 1024;
        BufferedImage img = new
            BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        Color[] colors = { Color.red, Color.green, Color.blue };
        float[] dist = {0.0f, 0.5f, 1.0f };
        Point2D center = new Point2D.Float(0.5f * w, 0.5f * h);
        RadialGradientPaint p =
            new RadialGradientPaint(center, 0.5f * w, dist, colors);
        g.setPaint(p);
        g.fillRect(0, 0, w, h);
        g.dispose();
        try {
            System.out.println("Create test image " + file.getAbsolutePath());
            boolean b = ImageIO.write(img, "JPEG", file);
            if (!b) {
                throw new RuntimeException("Failed to create test image.");
            }
        } catch (IOException e) {
            throw new RuntimeException("Test failed", e);
        }
    }
}
