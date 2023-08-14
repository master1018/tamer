public class StrikeDisposalCrashTest {
    public static void main(String[] args) {
        System.setProperty("sun.java2d.font.reftype", "weak");
        GraphicsDevice gd[] =
            GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        Frame frames[] = new Frame[gd.length];
        for (int i = 0; i < frames.length; i++) {
            GraphicsConfiguration gc = gd[i].getDefaultConfiguration();
            Frame f = new Frame("Frame on "+gc, gc);
            f.setSize(100, 100);
            f.setLocation(gc.getBounds().x, gc.getBounds().y);
            f.pack();
            frames[i] = f;
        }
        Font f1 = new Font("Dialog", Font.PLAIN, 10);
        Font f2 = new Font("Dialog", Font.ITALIC, 12);
        for (int i = 0; i < frames.length/2; i++) {
            renderText(frames[i], f1);
            renderText(frames[frames.length -1 - i], f1);
            renderText(frames[frames.length -1 - i], f2);
            renderText(frames[i], f2);
        }
        System.gc();
        System.runFinalization();
        System.gc();
        System.runFinalization();
        for (Frame f : frames) {
            f.dispose();
        }
        System.err.println("Exiting. If the test crashed after this it FAILED");
    }
    private static final String text =
        "The quick brown fox jumps over the lazy dog 1234567890";
    private static void renderText(Frame frame, Font f1) {
        VolatileImage vi = frame.createVolatileImage(256, 32);
        vi.validate(frame.getGraphicsConfiguration());
        Graphics2D g = vi.createGraphics();
        g.setFont(f1);
        g.drawString(text, 0, vi.getHeight()/2);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                           RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.drawString(text, 0, vi.getHeight()/2);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                           RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g.drawString(text, 0, vi.getHeight()/2);
        Toolkit.getDefaultToolkit().sync();
    }
}
