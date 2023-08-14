public class TranslucentJAppletTest {
    private static JFrame frame;
    private static volatile boolean paintComponentCalled = false;
    private static void initAndShowGUI() {
        frame = new JFrame();
        JApplet applet = new JApplet();
        applet.setBackground(new Color(0, 0, 0, 0));
        JPanel panel = new JPanel() {
            protected void paintComponent(Graphics g) {
                paintComponentCalled = true;
                g.setColor(Color.RED);
                g.fillOval(0, 0, getWidth(), getHeight());
            }
        };
        panel.setDoubleBuffered(false);
        panel.setOpaque(false);
        applet.add(panel);
        frame.add(applet);
        frame.setBounds(100, 100, 200, 200);
        frame.setUndecorated(true);
        frame.setBackground(new Color(0, 0, 0, 0));
        frame.setVisible(true);
    }
    public static void main(String[] args)
        throws Exception
    {
        sun.awt.SunToolkit tk = (sun.awt.SunToolkit)Toolkit.getDefaultToolkit();
        Robot r = new Robot();
        Color color1 = r.getPixelColor(100, 100); 
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                initAndShowGUI();
            }
        });
        tk.realSync();
        if (!paintComponentCalled) {
            throw new RuntimeException("Test FAILED: panel's paintComponent() method is not called");
        }
        Color newColor1 = r.getPixelColor(100, 100);
        BufferedImage bim = r.createScreenCapture(new Rectangle(200, 200, 1, 1));
        Color newColor2 = new Color(bim.getRGB(0, 0));
        if (!color1.equals(newColor1)) {
            System.err.println("color1 = " + color1);
            System.err.println("newColor1 = " + newColor1);
            throw new RuntimeException("Test FAILED: frame pixel at (0, 0) is not transparent");
        }
        if (!newColor2.equals(Color.RED)) {
            System.err.println("newColor2 = " + newColor2);
            throw new RuntimeException("Test FAILED: frame pixel at (100, 100) is not red (transparent?)");
        }
        System.out.println("Test PASSED");
    }
}
