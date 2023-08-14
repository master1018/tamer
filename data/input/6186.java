public class TSFrame {
    static volatile boolean done = false;
    static final boolean useSwing = System.getProperty("useswing") != null;
    static final boolean useShape = System.getProperty("useshape") != null;
    static final boolean useTransl = System.getProperty("usetransl") != null;
    static final boolean useNonOpaque = System.getProperty("usenonop") != null;
    static final Random rnd = new Random();
    private static void render(Graphics g, int w, int h, boolean useNonOpaque) {
        if (useNonOpaque) {
            Graphics2D g2d = (Graphics2D)g;
            GradientPaint p =
                new GradientPaint(0.0f, 0.0f,
                                  new Color(rnd.nextInt(0xffffff)),
                                  w, h,
                                  new Color(rnd.nextInt(0xff),
                                            rnd.nextInt(0xff),
                                            rnd.nextInt(0xff), 0),
                                  true);
            g2d.setPaint(p);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                 RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.fillOval(0, 0, w, h);
        } else {
            g.setColor(new Color(rnd.nextInt(0xffffff)));
            g.fillRect(0, 0, w, h);
        }
    }
    private static class MyCanvas extends Canvas {
        @Override
        public void paint(Graphics g) {
            render(g, getWidth(), getHeight(), false);
        }
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(200, 100);
        }
    }
    private static class NonOpaqueJFrame extends JFrame {
        NonOpaqueJFrame() {
            super("NonOpaque Swing JFrame");
            JPanel p = new JPanel() {
                public void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    render(g, getWidth(), getHeight(), true);
                    g.setColor(Color.red);
                    g.drawString("Non-Opaque Swing JFrame", 10, 15);
                }
            };
            p.setDoubleBuffered(false);
            p.setOpaque(false);
            add(p);
            setUndecorated(true);
        }
    }
    private static class NonOpaqueJAppletFrame extends JFrame {
        JPanel p;
        NonOpaqueJAppletFrame() {
            super("NonOpaque Swing JAppletFrame");
            JApplet ja = new JApplet() {
                public void paint(Graphics g) {
                    super.paint(g);
                    System.err.println("JAppletFrame paint called");
                }
            };
            p = new JPanel() {
                public void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    render(g, getWidth(), getHeight(), true);
                    g.setColor(Color.red);
                    g.drawString("Non-Opaque Swing JFrame", 10, 15);
                }
            };
            p.setDoubleBuffered(false);
            p.setOpaque(false);
            ja.add(p);
            add(ja);
            setUndecorated(true);
        }
    }
    private static class NonOpaqueFrame extends Frame {
        NonOpaqueFrame() {
            super("NonOpaque AWT Frame");
        }
        @Override
        public void paint(Graphics g) {
            render(g, getWidth(), getHeight(), true);
            g.setColor(Color.red);
            g.drawString("Non-Opaque AWT Frame", 10, 15);
        }
    }
    private static class MyJPanel extends JPanel {
        @Override
        public void paintComponent(Graphics g) {
            render(g, getWidth(), getHeight(), false);
        }
    }
    public static Frame createGui(
                                  final boolean useSwing,
                                  final boolean useShape,
                                  final boolean useTransl,
                                  final boolean useNonOpaque,
                                  final float factor)
    {
        Frame frame;
        done = false;
        if (useNonOpaque) {
            if (useSwing) {
                frame = new NonOpaqueJFrame();
            } else {
                frame = new NonOpaqueFrame();
            }
            animateComponent(frame);
        } else if (useSwing) {
            frame = new JFrame("Swing Frame");
            JComponent p = new JButton("Swing!");
            p.setPreferredSize(new Dimension(200, 100));
            frame.add("North", p);
            p = new MyJPanel();
            animateComponent(p);
            frame.add("Center", p);
        } else {
            frame = new Frame("AWT Frame") {
                public void paint(Graphics g) {
                    g.setColor(Color.red);
                    g.fillRect(0, 0, 100, 100);
                }
            };
            frame.setLayout(new BorderLayout());
            Canvas c = new MyCanvas();
            frame.add("North", c);
            animateComponent(c);
            c = new MyCanvas();
            frame.add("Center", c);
            animateComponent(c);
            c = new MyCanvas();
            frame.add("South", c);
            animateComponent(c);
        }
        final Frame finalFrame = frame;
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                finalFrame.dispose();
                done = true;
            }
        });
        frame.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                finalFrame.dispose();
                done = true;
            }
        });
        frame.setPreferredSize(new Dimension(800, 600));
        if (useShape) {
            frame.setUndecorated(true);
        }
        frame.setLocation(450, 10);
        frame.pack();
        GraphicsDevice gd = frame.getGraphicsConfiguration().getDevice();
        if (useShape) {
            if (gd.isWindowTranslucencySupported(WindowTranslucency.PERPIXEL_TRANSPARENT)) {
                System.out.println("applying PERPIXEL_TRANSPARENT");
                frame.setShape(new Ellipse2D.Double(0, 0, frame.getWidth(),
                                                    frame.getHeight()/3));
                frame.setTitle("PERPIXEL_TRANSPARENT");
            } else {
                System.out.println("Passed: PERPIXEL_TRANSPARENT unsupported");
            }
        }
        if (useTransl) {
            if (gd.isWindowTranslucencySupported(WindowTranslucency.TRANSLUCENT)) {
                System.out.println("applying TRANSLUCENT");
                frame.setOpacity(factor);
                frame.setTitle("TRANSLUCENT");
            } else {
                System.out.println("Passed: TRANSLUCENT unsupported");
            }
        }
        if (useNonOpaque) {
            if (gd.isWindowTranslucencySupported(WindowTranslucency.PERPIXEL_TRANSLUCENT)) {
                System.out.println("applying PERPIXEL_TRANSLUCENT");
                frame.setBackground(new Color(0, 0, 0, 0));
                frame.setTitle("PERPIXEL_TRANSLUCENT");
            } else {
                System.out.println("Passed: PERPIXEL_TRANSLUCENT unsupported");
            }
        }
        frame.setVisible(true);
        return frame;
    }
    public static void stopThreads() {
        done = true;
    }
    private static void animateComponent(final Component comp) {
        Thread t = new Thread(new Runnable() {
            public void run() {
                do {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ex) {}
                    comp.repaint();
                } while (!done);
            }
        });
        t.start();
    }
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                TSFrame.createGui(useSwing,
                                  useShape,
                                  useTransl,
                                  useNonOpaque,
                                  0.7f);
            }
        });
    }
}
