public class SharedMemoryPixmapsTest {
    static final int IMAGE_SIZE = 100;
    static boolean show = false;
    final Frame testFrame;
    public SharedMemoryPixmapsTest() {
        testFrame = new Frame("SharedMemoryPixmapsTest");
        testFrame.add(new TestComponent());
        testFrame.pack();
        testFrame.setVisible(true);
    }
    public static void main(String[] args) {
        for (String s : args) {
            if ("-show".equals(s)) {
                show = true;
            } else {
                System.err.println("Usage: SharedMemoryPixmapsTest [-show]");
            }
        }
        new SharedMemoryPixmapsTest();
    }
    private class TestComponent extends Component {
        VolatileImage vi = null;
        boolean tested = false;
        void initVI() {
            int res;
            if (vi == null) {
                res = VolatileImage.IMAGE_INCOMPATIBLE;
            } else {
                res = vi.validate(getGraphicsConfiguration());
            }
            if (res == VolatileImage.IMAGE_INCOMPATIBLE) {
                if (vi != null) vi.flush();
                vi = createVolatileImage(IMAGE_SIZE, IMAGE_SIZE);
                vi.validate(getGraphicsConfiguration());
                res = VolatileImage.IMAGE_RESTORED;
            }
            if (res == VolatileImage.IMAGE_RESTORED) {
                Graphics vig = vi.getGraphics();
                vig.setColor(Color.red);
                vig.fillRect(0, 0, vi.getWidth(), vi.getHeight());
                vig.dispose();
            }
        }
        @Override
        public synchronized void paint(Graphics g) {
            do {
                g.setColor(Color.green);
                g.fillRect(0, 0, getWidth(), getHeight());
                initVI();
                g.drawImage(vi, 0, 0, null);
            } while (vi.contentsLost());
            Toolkit.getDefaultToolkit().sync();
            if (!tested) {
                if (testRendering()) {
                    System.err.println("Test Passed");
                } else {
                    System.err.println("Test Failed");
                }
                tested = true;
            }
            if (!show) {
                testFrame.setVisible(false);
                testFrame.dispose();
            }
        }
        private boolean testRendering() throws RuntimeException {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {}
            Robot r = null;
            try {
                r = new Robot();
            } catch (AWTException ex) {
                ex.printStackTrace();
                throw new RuntimeException("Can't create Robot");
            }
            Point p = getLocationOnScreen();
            BufferedImage b =
                r.createScreenCapture(new Rectangle(p, getPreferredSize()));
            for (int y = 0; y < b.getHeight(); y++) {
                for (int x = 0; x < b.getWidth(); x++) {
                    if (b.getRGB(x, y) != Color.red.getRGB()) {
                        System.err.println("Incorrect pixel" + " at "
                            + x + "x" + y + " : " +
                            Integer.toHexString(b.getRGB(x, y)));
                        if (show) {
                            return false;
                        }
                        System.err.println("Test Failed");
                        System.exit(1);
                    }
                }
            }
            return true;
        }
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(IMAGE_SIZE, IMAGE_SIZE);
        }
    }
}
