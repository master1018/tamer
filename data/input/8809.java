public class ReadingInterruptionTest implements IIOReadProgressListener {
    public static void main(String[] args) {
        createTestFile();
        System.out.println("Test abort()....");
        ReadingInterruptionTest t = new ReadingInterruptionTest(new AbortAction());
        t.doTest();
        System.out.println("Test reset()....");
        t = new ReadingInterruptionTest(new ResetAction());
        t.doTest();
        System.out.println("Test dispose()....");
        t = new ReadingInterruptionTest(new DisposeAction());
        t.doTest();
     }
    protected abstract static class Action implements Runnable {
        protected ImageReader target;
        public void setTarget(ImageReader target) {
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
    static File file = new File("IMGP1001.JPG");
    Action action;
    ImageInputStream iis;
    ImageReader reader;
    protected ReadingInterruptionTest(Action action) {
        this.action = action;
        reader = ImageIO.getImageReadersByFormatName("JPEG").next();
        this.action.setTarget(reader);
    }
    public void doTest() {
        try {
            reader.addIIOReadProgressListener(this);
            iis = ImageIO.createImageInputStream(file);
            reader.setInput(iis);
            ImageReadParam p = reader.getDefaultReadParam();
            Thread.sleep(70);
            BufferedImage res = reader.read(0, p);
            Thread.sleep(70);
        } catch (Exception e) {
            throw new RuntimeException("Test FAILED", e);
        } finally {
            try {
                reader.reset();
            } catch (IllegalStateException e) {
                System.out.println("Expected exception was caught: " + e);
            } catch(Exception e) {
                throw new RuntimeException("Test FAILED.", e);
            }
        }
        System.out.println("Test PASSED.");
    }
    public void imageStarted(ImageReader source,
                                  int imageIndex) {} ;
    public void imageProgress(ImageReader source,
                              float percentageDone)
    {
        if (20f < percentageDone && percentageDone < 80f) {
            Thread t = new Thread(action);
            t.start();
        }
    };
    public void imageComplete(ImageReader source) {};
    public void sequenceStarted(ImageReader source,
                                int minIndex) {};
    public void sequenceComplete(ImageReader source) {};
    public void thumbnailStarted(ImageReader source,
                                 int imageIndex,
                                 int thumbnailIndex) {};
    public void thumbnailProgress(ImageReader source,
                                  float percentageDone) {};
    public void thumbnailComplete(ImageReader source) {};
    public void readAborted(ImageReader source) {};
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
