public class BufferStrategyExceptionTest {
    private static final int TEST_REPS = 20;
    public static void main(String[] args) {
        GraphicsDevice gd =
            GraphicsEnvironment.getLocalGraphicsEnvironment().
                getDefaultScreenDevice();
        for (int i = 0; i < TEST_REPS; i++) {
            TestFrame f = new TestFrame();
            f.pack();
            f.setSize(400, 400);
            f.setVisible(true);
            if (i % 2 == 0) {
                gd.setFullScreenWindow(f);
            }
            Dimension d = f.getSize();
            d.width -= 5; d.height -= 5;
            f.setSize(d);
            f.render();
            gd.setFullScreenWindow(null);
            sleep(100);
            f.dispose();
        }
        System.out.println("Test passed.");
    }
    private static void sleep(long msecs) {
        try {
            Thread.sleep(msecs);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    private static final BufferedImage bi =
        new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
    static class TestFrame extends Frame {
        TestFrame() {
            setUndecorated(true);
            setIgnoreRepaint(true);
            setSize(400, 400);
        }
        public void render() {
            ImageCapabilities imgBackBufCap = new ImageCapabilities(true);
            ImageCapabilities imgFrontBufCap = new ImageCapabilities(true);
            BufferCapabilities bufCap =
                new BufferCapabilities(imgFrontBufCap,
                    imgBackBufCap, BufferCapabilities.FlipContents.COPIED);
            try {
                createBufferStrategy(2, bufCap);
            } catch (AWTException ex) {
                createBufferStrategy(2);
            }
            BufferStrategy bs = getBufferStrategy();
            do {
                Graphics g =  bs.getDrawGraphics();
                g.setColor(Color.green);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(Color.red);
                g.drawString("Rendering test", 20, 20);
                g.drawImage(bi, 50, 50, null);
                g.dispose();
                bs.show();
            } while (bs.contentsLost()||bs.contentsRestored());
        }
    }
}
