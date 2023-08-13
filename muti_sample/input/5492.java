public class WritingInterruptionTest implements IIOWriteProgressListener {
    static File pwd = new File(".");
    static BufferedImage img;
    public static void main(String[] args) {
        img = createTestImage();
        System.out.println("Test abort()....");
        WritingInterruptionTest t = new WritingInterruptionTest(new AbortAction());
        t.doTest();
        System.out.println("Test reset()....");
        t = new WritingInterruptionTest(new ResetAction());
        t.doTest();
        System.out.println("Test dispose()....");
        t = new WritingInterruptionTest(new DisposeAction());
        t.doTest();
     }
    protected abstract static class Action implements Runnable {
        protected ImageWriter target;
        public void setTarget(ImageWriter target) {
            this.target = target;
        }
        public abstract void run();
    }
    protected static class DisposeAction extends Action {
        public void run() {
            try {
                target.dispose();
            } catch (IllegalStateException e) {
                System.out.println("Test PASSED: IllegalStateException was thrown.");
            } catch (Throwable e) {
                throw new RuntimeException("Test FAILED.", e);
            }
        }
    }
    protected static class AbortAction extends Action {
        public void run() {
            try {
                target.abort();
            } catch (IllegalStateException e) {
                System.out.println("Test PASSED: IllegalStateException was thrown.");
            } catch (Throwable e) {
                throw new RuntimeException("Test FAILED.", e);
            }
        }
    }
    protected static class ResetAction extends Action {
        public void run() {
            try {
                target.reset();
            } catch (IllegalStateException e) {
                System.out.println("Test PASSED: IllegalStateException was thrown.");
            } catch (Throwable e) {
                throw new RuntimeException("Test FAILED.", e);
            }
        }
    }
    Action action;
    ImageOutputStream ios;
    ImageWriter w;
    protected WritingInterruptionTest(Action action) {
        this.action = action;
        w = ImageIO.getImageWritersByFormatName("JPEG").next();
        this.action.setTarget(w);
    }
    public void doTest() {
        try {
            w.addIIOWriteProgressListener(this);
            File f = File.createTempFile("writer_", ".jpg", pwd);
            ImageOutputStream ios = ImageIO.createImageOutputStream(f);
            w.setOutput(ios);
            Thread.sleep(70);
            w.write(img);
            Thread.sleep(70);
        } catch (Exception e) {
            throw new RuntimeException("Test FAILED", e);
        } finally {
            try {
                w.reset();
            } catch (IllegalStateException e) {
                System.out.println("Expected exception was caught: " + e);
            } catch(Exception e) {
                throw new RuntimeException("Test FAILED.", e);
            }
        }
        System.out.println("Test PASSED.");
    }
    public void imageStarted(ImageWriter source,
                                  int imageIndex) {} ;
    public void imageProgress(ImageWriter source,
                              float percentageDone)
    {
        if (20f < percentageDone && percentageDone < 80f ) {
            Thread t = new Thread(action);
            t.start();
        }
    };
    public void imageComplete(ImageWriter source) {};
    public void thumbnailStarted(ImageWriter source,
                                 int imageIndex,
                                 int thumbnailIndex) {};
    public void thumbnailProgress(ImageWriter source,
                                  float percentageDone) {};
    public void thumbnailComplete(ImageWriter source) {};
    public void writeAborted(ImageWriter source) {};
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
