public class DeviceIdentificationTest {
    public static void main(String args[]) {
        final Frame f = new Frame("DeviceIdentificationTest");
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                f.dispose();
            }
        });
        f.addComponentListener(new ComponentAdapter() {
            public void componentMoved(ComponentEvent e) {
                f.setTitle("Currently on: "+
                           f.getGraphicsConfiguration().getDevice());
            }
        });
        Panel p = new Panel();
        Button b = new Button("Print Current Devices");
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                GraphicsDevice gds[] =
                    GraphicsEnvironment.getLocalGraphicsEnvironment().
                        getScreenDevices();
                int i = 0;
                System.err.println("--- Devices: ---");
                for (GraphicsDevice gd : gds) {
                    System.err.println("Device["+i+"]= "+ gd);
                    System.err.println("  bounds = "+
                        gd.getDefaultConfiguration().getBounds());
                    i++;
                }
                System.err.println("-------------------");
            }
        });
        p.add(b);
        b = new Button("Print My Device");
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                GraphicsConfiguration gc = f.getGraphicsConfiguration();
                GraphicsDevice gd = gc.getDevice();
                System.err.println("--- My Device ---");
                System.err.println("Device  = "+ gd);
                System.err.println(" bounds = "+
                        gd.getDefaultConfiguration().getBounds());
            }
        });
        p.add(b);
        b = new Button("Create FS Frame on my Device");
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                GraphicsConfiguration gc = f.getGraphicsConfiguration();
                final GraphicsDevice gd = gc.getDevice();
                System.err.println("--- Creating FS Frame on Device ---");
                System.err.println("Device  = "+ gd);
                System.err.println(" bounds = "+
                        gd.getDefaultConfiguration().getBounds());
                final Frame fsf = new Frame("Full-screen Frame on dev"+gd, gc) {
                    public void paint(Graphics g) {
                        g.setColor(Color.green);
                        g.fillRect(0, 0, getWidth(), getHeight());
                        g.setColor(Color.red);
                        g.drawString("FS on device: "+gd, 200, 200);
                        g.drawString("Click to exit Full-screen.", 200, 250);
                    }
                };
                fsf.setUndecorated(true);
                fsf.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        gd.setFullScreenWindow(null);
                        fsf.dispose();
                    }
                });
                gd.setFullScreenWindow(fsf);
            }
        });
        p.add(b);
        f.add("North", p);
        p = new Panel();
        b = new Button("Test Passed");
        b.setBackground(Color.green);
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Test Passed");
                f.dispose();
            }
        });
        p.add(b);
        b = new Button("Test Failed");
        b.setBackground(Color.red);
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Test FAILED");
                f.dispose();
                throw new RuntimeException("Test FAILED");
            }
        });
        p.add(b);
        f.add("South", p);
        f.pack();
        f.setVisible(true);
    }
}
