public class UpdateGCTest
{
    public static void main(String[] args)
    {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gds = ge.getScreenDevices();
        if (gds.length < 2)
        {
            System.out.println("The test should be run in multi-screen configuration. Test PASSED/skipped");
            return;
        }
        boolean virtualConfig = false;
        for (GraphicsDevice gd : gds)
        {
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            if ((gc.getBounds().x != 0) || (gc.getBounds().y != 0))
            {
                virtualConfig = true;
                break;
            }
        }
        if (!virtualConfig)
        {
            System.out.println("The test should be run in virtual multi-screen mode. Test PASSED/skipped");
            return;
        }
        try
        {
            Robot robot = new Robot();
            Util.waitForIdle(robot);
            for (GraphicsDevice gdOrig : gds)
            {
                GraphicsConfiguration gcOrig = gdOrig.getDefaultConfiguration();
                Frame f = new Frame("F", gcOrig);
                f.setSize(200, 200);
                f.setLayout(new BorderLayout());
                f.add(new Canvas(gcOrig), BorderLayout.NORTH);
                Container c = new Container() {};
                c.setLayout(new BorderLayout());
                c.add(new Panel());
                c.add(new Canvas(gcOrig));
                f.add(c, BorderLayout.SOUTH);
                Panel p = new Panel();
                p.setLayout(new BorderLayout());
                p.add(new Canvas(gcOrig), BorderLayout.NORTH);
                p.add(new Component() {}, BorderLayout.SOUTH);
                p.add(new Panel(), BorderLayout.CENTER);
                f.add(p, BorderLayout.CENTER);
                f.setVisible(true);
                Util.waitForIdle(robot);
                for (GraphicsDevice gd : gds)
                {
                    GraphicsConfiguration gc = gd.getDefaultConfiguration();
                    f.setLocation(gc.getBounds().x + 100, gc.getBounds().y + 100);
                    Util.waitForIdle(robot);
                    checkGC(f, gc);
                }
            }
        }
        catch (Exception z)
        {
            System.err.println("Unknown exception caught");
            z.printStackTrace(System.err);
            throw new RuntimeException("Test FAILED: " + z.getMessage());
        }
        System.out.println("Test PASSED");
    }
    private static void checkGC(Component c, GraphicsConfiguration gc)
    {
        if (c.getGraphicsConfiguration() != gc)
        {
            System.err.println("GC for component (" + c + ") is not updated");
            System.err.println("Right GC: " + gc);
            System.err.println("Component GC: " + c.getGraphicsConfiguration());
            throw new RuntimeException("Test FAILED: component GC is not updated");
        }
        System.out.println("Checked GC for component (" + c + "): OK");
        if (c instanceof Container)
        {
            Container cc = (Container)c;
            Component[] children = cc.getComponents();
            for (Component child : children)
            {
                checkGC(child, gc);
            }
        }
    }
}
