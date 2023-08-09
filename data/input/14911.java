public class ScreenInsetsTest
{
    public static void main(String[] args)
    {
        if (!Toolkit.getDefaultToolkit().isFrameStateSupported(Frame.MAXIMIZED_BOTH))
        {
            return;
        }
        boolean passed = true;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gds = ge.getScreenDevices();
        for (GraphicsDevice gd : gds)
        {
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            Rectangle gcBounds = gc.getBounds();
            Insets gcInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
            Frame f = new Frame("Test", gc);
            f.setUndecorated(true);
            f.setBounds(gcBounds.x + 100, gcBounds.y + 100, 320, 240);
            f.setVisible(true);
            Util.waitForIdle(null);
            f.setExtendedState(Frame.MAXIMIZED_BOTH);
            Util.waitForIdle(null);
            Rectangle fBounds = f.getBounds();
            if (fBounds.x < gcBounds.x)
            {
                fBounds.width -= (gcBounds.x - fBounds.x) * 2; 
                fBounds.x = gcBounds.x;
            }
            if (fBounds.y < gcBounds.y)
            {
                fBounds.height -= (gcBounds.y - fBounds.y) * 2; 
                fBounds.y = gcBounds.y;
            }
            Insets expected = new Insets(fBounds.y - gcBounds.y,
                                         fBounds.x - gcBounds.x,
                                         gcBounds.y + gcBounds.height - fBounds.y - fBounds.height,
                                         gcBounds.x + gcBounds.width - fBounds.x - fBounds.width);
            if (!expected.equals(gcInsets))
            {
                passed = false;
                System.err.println("Wrong insets for GraphicsConfig: " + gc);
                System.err.println("\tExpected: " + expected);
                System.err.println("\tActual: " + gcInsets);
            }
            f.dispose();
        }
        if (!passed)
        {
            throw new RuntimeException("TEST FAILED: Toolkit.getScreenInsets() returns wrong value for some screens");
        }
    }
}
