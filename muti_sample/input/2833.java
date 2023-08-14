public class LocationRelativeToTest
{
    private static int FRAME_WIDTH = 400;
    private static int FRAME_HEIGHT = 300;
    public static void main(String[] args)
    {
        Robot r = Util.createRobot();
        GraphicsEnvironment ge =
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        System.out.println("Center point: " + ge.getCenterPoint());
        GraphicsDevice[] gds = ge.getScreenDevices();
        GraphicsDevice gdDef = ge.getDefaultScreenDevice();
        GraphicsConfiguration gcDef =
            gdDef.getDefaultConfiguration();
        Frame f = new Frame("F", gcDef);
        f.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        f.setVisible(true);
        Util.waitForIdle(r);
        f.setLocationRelativeTo(null);
        Util.waitForIdle(r);
        checkLocation(f, ge.getCenterPoint());
        for (GraphicsDevice gd : gds)
        {
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            Rectangle gcBounds = gc.getBounds();
            Frame f2 = new Frame("F2", gc);
            f2.setBounds(gcBounds.x + 100, gcBounds.y + 100,
                         FRAME_WIDTH, FRAME_HEIGHT);
            f.setLocationRelativeTo(f2);
            Util.waitForIdle(r);
            checkLocation(f, new Point(gcBounds.x + gcBounds.width / 2,
                                       gcBounds.y + gcBounds.height / 2));
            f2.setVisible(true);
            Util.waitForIdle(r);
            Point f2Loc = f2.getLocationOnScreen();
            f.setLocationRelativeTo(f2);
            Util.waitForIdle(r);
            checkLocation(f, new Point(f2Loc.x + f2.getWidth() / 2,
                                       f2Loc.y + f2.getHeight() / 2));
        }
    }
    private static void checkLocation(Frame f, Point rightLoc)
    {
        Point actualLoc = new Point(f.getBounds().x + f.getWidth() / 2,
                                    f.getBounds().y + f.getHeight() / 2);
        if (!rightLoc.equals(actualLoc))
        {
            System.err.println("Right location for the window center: " + rightLoc);
            System.err.println("Actual location for the window center: " + actualLoc);
            throw new RuntimeException("Test FAILED: setLocationRelativeTo() operates incorrectly");
        }
    }
}
