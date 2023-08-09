public class FSFrame extends Frame implements Runnable {
    boolean visible = false;
    Robot robot = null;
    static volatile boolean done = false;
    public void paint(Graphics g) {
        if (!visible && getWidth() != 0 && getHeight() != 0) {
            visible = true;
            try {
                GraphicsDevice gd = getGraphicsConfiguration().getDevice();
                robot = new Robot(gd);
            } catch (Exception e) {
                System.out.println("Problem creating robot: cannot verify FS " +
                                   "window display");
            }
        }
        g.setColor(Color.green);
        g.fillRect(0, 0, getWidth(), getHeight());
    }
    @Override
    public void update(Graphics g) {
        paint(g);
    }
    boolean checkColor(int x, int y, BufferedImage bImg) {
        int pixelColor;
        int correctColor = Color.green.getRGB();
        pixelColor = bImg.getRGB(x, y);
        if (pixelColor != correctColor) {
            System.out.println("FAILURE: pixelColor " +
                               Integer.toHexString(pixelColor) +
                               " != correctColor " +
                               Integer.toHexString(correctColor) +
                               " at coordinates (" + x + ", " + y + ")");
            return false;
        }
        return true;
    }
    void checkFSDisplay(boolean fsSupported) {
        GraphicsConfiguration gc = getGraphicsConfiguration();
        GraphicsDevice gd = gc.getDevice();
        Rectangle r = gc.getBounds();
        Insets in = null;
        if (!fsSupported) {
            in = Toolkit.getDefaultToolkit().getScreenInsets(gc);
            r = new Rectangle(in.left, in.top,
                              r.width -  (in.left + in.right),
                              r.height - (in.top  + in.bottom));
        }
        BufferedImage bImg = robot.createScreenCapture(r);
        if (robot == null) {
            return;
        }
        boolean colorCorrect = true;
        colorCorrect &= checkColor(0, 0, bImg);
        colorCorrect &= checkColor(0, bImg.getHeight() - 1, bImg);
        colorCorrect &= checkColor(bImg.getWidth() - 1, 0, bImg);
        colorCorrect &= checkColor(bImg.getWidth() - 1, bImg.getHeight() - 1, bImg);
        colorCorrect &= checkColor(bImg.getWidth() / 2, bImg.getHeight() / 2, bImg);
        if (!colorCorrect) {
            System.err.println("Test failed for mode: fsSupported="+fsSupported);
            if (in != null) {
                System.err.println("screen insets   : " + in);
            }
            System.err.println("screen shot rect: " + r);
            String name = "FSFrame_fs_"+
                    (fsSupported?"supported":"not_supported")+".png";
            try {
                ImageIO.write(bImg, "png", new File(name));
                System.out.println("Dumped screen shot to "+name);
            } catch (IOException ex) {}
            throw new Error("Some pixel colors not correct; FS window may not" +
                            " have been displayed correctly");
        }
    }
    void checkFSFunctionality(boolean withSecurity) {
        GraphicsDevice gd = getGraphicsConfiguration().getDevice();
        if (withSecurity) {
            SecurityManager sm = new SecurityManager();
            System.setSecurityManager(sm);
        }
        try {
            final boolean fs = gd.isFullScreenSupported();
            System.out.println("FullscreenSupported: " + (fs ? "yes" : "no"));
            gd.setFullScreenWindow(this);
            try {
                Thread.sleep(2000);
            } catch (Exception e) {}
            if (!withSecurity) {
                try {
                    EventQueue.invokeAndWait(new Runnable() {
                        public void run() {
                            repaint();
                            checkFSDisplay(fs);
                        }
                    });
                } catch (InvocationTargetException ex) {
                    ex.printStackTrace();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            gd.setFullScreenWindow(null);
            try {
                Thread.sleep(2000);
            } catch (Exception e) {}
        } catch (SecurityException e) {
            e.printStackTrace();
            throw new Error("Failure: should not get an exception when " +
                            "calling isFSSupported or setFSWindow");
        }
    }
    public void run() {
        boolean firstTime = true;
        while (!done) {
            if (visible) {
                checkFSFunctionality(false);
                checkFSFunctionality(true);
                done = true;
            } else {
                try {
                    Thread.sleep(100);
                } catch (Exception e) {}
            }
        }
        System.out.println("PASS");
    }
    public static void main(String args[]) {
        FSFrame frame = new FSFrame();
        frame.setUndecorated(true);
        Thread t = new Thread(frame);
        frame.setSize(500, 500);
        frame.setVisible(true);
        t.start();
        while (!done) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {}
        }
        frame.dispose();
    }
}
