public class MultimonFullscreenTest extends Frame implements ActionListener {
    GraphicsDevice  defDev = GraphicsEnvironment.getLocalGraphicsEnvironment().
            getDefaultScreenDevice();
    GraphicsDevice  gd[] = GraphicsEnvironment.getLocalGraphicsEnvironment().
            getScreenDevices();
    HashMap<Button, GraphicsDevice> deviceMap;
    private static boolean dmChange = false;
    static boolean setNullOnDispose = false;
    static boolean useFSFrame = true;
    static boolean useFSWindow = false;
    static boolean useFSDialog = false;
    static boolean useBS = false;
    static boolean runRenderLoop = false;
    static boolean addHWChildren = false;
    static volatile boolean done = true;
    public MultimonFullscreenTest(String title) {
        super(title);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        Panel p = new Panel();
        deviceMap = new HashMap<Button, GraphicsDevice>(gd.length);
        int num = 0;
        for (GraphicsDevice dev : gd) {
            Button b;
            if (dev == defDev) {
                b = new Button("Primary screen: " + num);
                System.out.println("Primary Dev : " + dev + " Bounds: " +
                        dev.getDefaultConfiguration().getBounds());
            } else {
                b = new Button("Secondary screen " + num);
                System.out.println("Secondary Dev : " + dev + " Bounds: " +
                        dev.getDefaultConfiguration().getBounds());
            }
            b.addActionListener(this);
            p.add(b);
            deviceMap.put(b, dev);
            num++;
        }
        add("South", p);
        Panel p1 = new Panel();
        p1.setLayout(new GridLayout(2,0));
        Checkbox cb = new Checkbox("Change DM on entering FS");
        cb.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                dmChange = ((Checkbox)e.getSource()).getState();
            }
        });
        p1.add(cb);
        CheckboxGroup cbg = new CheckboxGroup();
        cb = new Checkbox("Use Frame to enter FS", cbg, true);
        cb.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                useFSFrame = true;
                useFSWindow = false;
                useFSDialog = false;
            }
        });
        p1.add(cb);
        cb = new Checkbox("Use Window to enter FS", cbg, false);
        cb.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                useFSFrame = false;
                useFSWindow = true;
                useFSDialog = false;
            }
        });
        p1.add(cb);
        cb = new Checkbox("Use Dialog to enter FS", cbg, false);
        cb.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                useFSFrame = false;
                useFSWindow = false;
                useFSDialog = true;
            }
        });
        p1.add(cb);
        cb = new Checkbox("Run render loop");
        cb.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                runRenderLoop = ((Checkbox)e.getSource()).getState();
            }
        });
        p1.add(cb);
        cb = new Checkbox("Use BufferStrategy in render loop");
        cb.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                useBS = ((Checkbox)e.getSource()).getState();
            }
        });
        p1.add(cb);
        cb = new Checkbox("Add Children to FS window");
        cb.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                addHWChildren = ((Checkbox)e.getSource()).getState();
            }
        });
        p1.add(cb);
        add("North", p1);
        pack();
        setVisible(true);
    }
    Font f = new Font("Dialog", Font.BOLD, 24);
    Random rnd = new Random();
    public void renderDimensions(Graphics g, Rectangle rectWndBounds,
                                 GraphicsConfiguration gc) {
        g.setColor(new Color(rnd.nextInt(0xffffff)));
        g.fillRect(0, 0, rectWndBounds.width, rectWndBounds.height);
        g.setColor(new Color(rnd.nextInt(0xffffff)));
        Rectangle rectStrBounds;
        g.setFont(f);
        rectStrBounds = g.getFontMetrics().
                getStringBounds(rectWndBounds.toString(), g).getBounds();
        rectStrBounds.height += 30;
        g.drawString(rectWndBounds.toString(), 50, rectStrBounds.height);
        int oldHeight = rectStrBounds.height;
        String isFSupported = "Exclusive Fullscreen mode supported: " +
                              gc.getDevice().isFullScreenSupported();
        rectStrBounds = g.getFontMetrics().
                getStringBounds(isFSupported, g).getBounds();
        rectStrBounds.height += (10 + oldHeight);
        g.drawString(isFSupported, 50, rectStrBounds.height);
        oldHeight = rectStrBounds.height;
        String isDMChangeSupported = "Display Mode Change supported: " +
                              gc.getDevice().isDisplayChangeSupported();
        rectStrBounds = g.getFontMetrics().
                getStringBounds(isDMChangeSupported, g).getBounds();
        rectStrBounds.height += (10 + oldHeight);
        g.drawString(isDMChangeSupported, 50, rectStrBounds.height);
        oldHeight = rectStrBounds.height;
        String usingBS = "Using BufferStrategy: " + useBS;
        rectStrBounds = g.getFontMetrics().
                getStringBounds(usingBS, g).getBounds();
        rectStrBounds.height += (10 + oldHeight);
        g.drawString(usingBS, 50, rectStrBounds.height);
        final String m_strQuitMsg = "Double-click to dispose FullScreen Window";
        rectStrBounds = g.getFontMetrics().
                getStringBounds(m_strQuitMsg, g).getBounds();
        g.drawString(m_strQuitMsg,
                (rectWndBounds.width - rectStrBounds.width) / 2,
                (rectWndBounds.height - rectStrBounds.height) / 2);
    }
    public void actionPerformed(ActionEvent ae) {
        GraphicsDevice dev = deviceMap.get(ae.getSource());
        System.err.println("Setting FS on device:"+dev);
        final Window fsWindow;
        if (useFSWindow) {
            fsWindow = new Window(this, dev.getDefaultConfiguration()) {
                public void paint(Graphics g) {
                    renderDimensions(g, getBounds(),
                                     this.getGraphicsConfiguration());
                }
            };
        } else if (useFSDialog) {
            fsWindow = new Dialog((Frame)null, "FS Dialog on device "+dev, false,
                                 dev.getDefaultConfiguration());
            fsWindow.add(new Component() {
                public void paint(Graphics g) {
                    renderDimensions(g, getBounds(),
                                     this.getGraphicsConfiguration());
                }
            });
        } else {
            fsWindow = new Frame("FS Frame on device "+dev,
                                 dev.getDefaultConfiguration())
            {
                public void paint(Graphics g) {
                    renderDimensions(g, getBounds(),
                                     this.getGraphicsConfiguration());
                }
            };
            if (addHWChildren) {
                fsWindow.add("South", new Panel() {
                    public void paint(Graphics g) {
                        g.setColor(Color.red);
                        g.fillRect(0, 0, getWidth(), getHeight());
                    }
                });
                fsWindow.add("North", new Button("Button, sucka!"));
            }
        }
        fsWindow.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    done = true;
                    fsWindow.dispose();
                }
            }
        });
        fsWindow.addWindowListener(new WindowHandler());
        dev.setFullScreenWindow(fsWindow);
        if (dmChange && dev.isDisplayChangeSupported()) {
            DisplayMode dms[] = dev.getDisplayModes();
            DisplayMode myDM = null;
            for (DisplayMode dm : dms) {
                if (dm.getWidth() == 800 && dm.getHeight() == 600 &&
                    (dm.getBitDepth() >= 16 ||
                     dm.getBitDepth() == DisplayMode.BIT_DEPTH_MULTI) &&
                     (dm.getRefreshRate() >= 60 ||
                      dm.getRefreshRate() == DisplayMode.REFRESH_RATE_UNKNOWN))
                {
                    myDM = dm;
                    break;
                }
            }
            if (myDM != null) {
                System.err.println("Setting Display Mode: "+
                        myDM.getWidth() + "x" + myDM.getHeight() + "x" +
                        myDM.getBitDepth() + "@" + myDM.getRefreshRate() +
                        "Hz on device" + dev);
                dev.setDisplayMode(myDM);
            } else {
                System.err.println("Can't find suitable display mode.");
            }
        }
        done = false;
        if (runRenderLoop) {
            Thread updateThread = new Thread(new Runnable() {
                public void run() {
                    BufferStrategy bs = null;
                    if (useBS) {
                        fsWindow.createBufferStrategy(2);
                        bs = fsWindow.getBufferStrategy();
                    }
                    while (!done) {
                        if (useBS) {
                            Graphics g = bs.getDrawGraphics();
                            renderDimensions(g, fsWindow.getBounds(),
                                           fsWindow.getGraphicsConfiguration());
                            bs.show();
                        } else {
                            fsWindow.repaint();
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {}
                    }
                    if (useBS) {
                        bs.dispose();
                    }
                }
            });
            updateThread.start();
        }
    }
    public static void main(String args[]) {
        for (String s : args) {
            if (s.equalsIgnoreCase("-dm")) {
                System.err.println("Do Display Change after entering FS mode");
                dmChange = true;
            } else if (s.equalsIgnoreCase("-usewindow")) {
                System.err.println("Using Window to enter FS mode");
                useFSWindow = true;
            } else if (s.equalsIgnoreCase("-setnull")) {
                System.err.println("Setting null FS window on dispose");
                setNullOnDispose = true;
            } else {
                System.err.println("Usage: MultimonFullscreenTest " +
                        "[-dm][-usewindow][-setnull]");
            }
        }
        MultimonFullscreenTest fs =
                new MultimonFullscreenTest("Test Full Screen");
    }
    class WindowHandler extends WindowAdapter {
        public void windowClosing(WindowEvent we) {
            done = true;
            Window w = (Window)we.getSource();
            if (setNullOnDispose) {
                w.getGraphicsConfiguration().getDevice().setFullScreenWindow(null);
            }
            w.dispose();
        }
    }
}
