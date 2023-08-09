public class DisplayModeChanger {
    public static void main(String[] args)
        throws InterruptedException, InvocationTargetException
    {
        final GraphicsDevice gd =
            GraphicsEnvironment.getLocalGraphicsEnvironment().
                getDefaultScreenDevice();
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                Frame f = null;
                if (gd.isFullScreenSupported()) {
                    try {
                        f = new Frame("DisplayChanger Frame");
                        gd.setFullScreenWindow(f);
                        if (gd.isDisplayChangeSupported()) {
                            DisplayMode dm = findDisplayMode(gd);
                            if (gd != null) {
                                gd.setDisplayMode(dm);
                            }
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                        gd.setFullScreenWindow(null);
                    } finally {
                        if (f != null) {
                            f.dispose();
                        }
                    }
                }
            }
        });
    }
    private static DisplayMode findDisplayMode(GraphicsDevice gd) {
        DisplayMode dms[] = gd.getDisplayModes();
        DisplayMode currentDM = gd.getDisplayMode();
        for (DisplayMode dm : dms) {
            if (!dm.equals(currentDM) &&
                 dm.getRefreshRate() == currentDM.getRefreshRate())
            {
                return dm;
            }
        }
        return null;
    }
}
