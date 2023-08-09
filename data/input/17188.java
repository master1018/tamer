public class WindowGCChangeTest extends Applet
{
    public void init()
    {
    }
    public void start()
    {
        Robot robot = null;
        try
        {
            robot = new Robot();
        }
        catch (Exception z)
        {
            z.printStackTrace(System.err);
            throw new RuntimeException("Test FAILED: couldn't create Robot instance", z);
        }
        setSize(200, 200);
        setVisible(true);
        validate();
        Util.waitForIdle(robot);
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gds = ge.getScreenDevices();
        if (gds.length != 2)
        {
            return;
        }
        int defGDNo = 0;
        int nondefGDNo = 0;
        boolean isVirtualScreen = false;
        GraphicsDevice defgd = ge.getDefaultScreenDevice();
        for (int i = 0; i < gds.length; i++)
        {
            Rectangle r = gds[i].getDefaultConfiguration().getBounds();
            if ((r.x != 0) || (r.y != 0))
            {
                isVirtualScreen = true;
            }
            if (gds[i] == defgd)
            {
                defGDNo = i;
            }
            else
            {
                nondefGDNo = i;
            }
        }
        if (!isVirtualScreen)
        {
            return;
        }
        GraphicsDevice defGD = gds[defGDNo];
        GraphicsDevice nondefGD = gds[nondefGDNo];
        final GraphicsConfiguration defGC = defGD.getDefaultConfiguration();
        final GraphicsConfiguration nondefGC = nondefGD.getDefaultConfiguration();
        final Frame f = new Frame(defGC);
        f.setBounds(nondefGC.getBounds().x + 100, nondefGC.getBounds().y + 100, 100, 100);
        f.addWindowListener(new WindowAdapter()
        {
            public void windowActivated(WindowEvent ev)
            {
                GraphicsConfiguration gcf = f.getGraphicsConfiguration();
                if (gcf != nondefGC)
                {
                    throw new RuntimeException("Test FAILED: graphics config is not updated");
                }
                f.dispose();
            }
        });
        f.setVisible(true);
        Util.waitForIdle(robot);
        final Frame g = new Frame(nondefGC);
        g.setBounds(defGC.getBounds().x + 100, defGC.getBounds().y + 100, 100, 100);
        g.addWindowListener(new WindowAdapter()
        {
            public void windowActivated(WindowEvent ev)
            {
                GraphicsConfiguration gcg = g.getGraphicsConfiguration();
                if (gcg != defGC)
                {
                    throw new RuntimeException("Test FAILED: graphics config is not updated");
                }
                g.dispose();
            }
        });
        g.setVisible(true);
        Util.waitForIdle(robot);
        final Frame h = new Frame(defGC);
        h.setBounds(defGC.getBounds().x + 100, defGC.getBounds().y + 100, 100, 100);
        h.addWindowListener(new WindowAdapter()
        {
            public void windowActivated(WindowEvent ev)
            {
                GraphicsConfiguration gch = h.getGraphicsConfiguration();
                if (gch != nondefGC)
                {
                    throw new RuntimeException("Test FAILED: graphics config is not updated");
                }
                h.dispose();
            }
        });
        h.setUndecorated(true);
        nondefGD.setFullScreenWindow(h);
        Util.waitForIdle(robot);
    }
}
