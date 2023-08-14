public class ConcurrentWritingTest extends Thread {
    static ImageWriter w = null;
    static File pwd = new File(".");
    static BufferedImage img;
    private static int MAX_THREADS = 50;
    private static int completeCount = 0;
    private static Object lock = new Object();
    public static void main(String[] args) throws Exception {
        img = createTestImage();
        w = ImageIO.getImageWritersByFormatName("JPEG").next();
        for (int i = 0; i < MAX_THREADS; i++) {
            (new ConcurrentWritingTest()).start();
        }
        boolean needWait = true;
        while(needWait) {
            synchronized(lock) {
                needWait = completeCount < MAX_THREADS;
            }
        }
        System.out.println("Test PASSED");
    }
    public void run() {
        try {
            File f = File.createTempFile("writer_", ".jpg", pwd);
            ImageOutputStream ios = ImageIO.createImageOutputStream(f);
            w.setOutput(ios);
            Thread.sleep(70);
            w.write(img);
            Thread.sleep(70);
            w.reset();
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
    private static BufferedImage createTestImage() {
        int w = 1024;
        int h = 768;
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
        return img;
    }
}
