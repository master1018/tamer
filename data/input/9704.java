public class bug6798062 extends JApplet {
    private final JSlider slider = new JSlider(0, 100);
    private final JTextField tfLink = new JTextField();
    private final JButton btnStart = new JButton("Start");
    private final JButton btnStop = new JButton("Stop");
    private final JButton btnGC = new JButton("Run System.gc()");
    private ShellFolder folder;
    private Thread thread;
    public static void main(String[] args) {
        JFrame frame = new JFrame("bug6798062");
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new bug6798062().initialize());
        frame.setVisible(true);
    }
    public void init() {
        add(initialize());
    }
    private JComponent initialize() {
        if (OSInfo.getOSType() != OSInfo.OSType.WINDOWS) {
            return new JLabel("The test is suitable only for Windows");
        }
        String tempDir = System.getProperty("java.io.tmpdir");
        if (tempDir.length() == 0) { 
            tempDir = System.getProperty("user.home");
        }
        System.out.println("Temp directory: " + tempDir);
        try {
            folder = ShellFolder.getShellFolder(new File(tempDir));
        } catch (FileNotFoundException e) {
            fail("Directory " + tempDir + " not found");
        }
        slider.setMajorTickSpacing(10);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setSnapToTicks(true);
        slider.setValue(10);
        btnStart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setEnabledState(false);
                thread = new MyThread(slider.getValue(), tfLink.getText());
                thread.start();
            }
        });
        btnStop.setEnabled(false);
        btnStop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                thread.interrupt();
                thread = null;
                setEnabledState(true);
            }
        });
        btnGC.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.gc();
            }
        });
        setEnabledState(true);
        JPanel pnButtons = new JPanel();
        pnButtons.setLayout(new BoxLayout(pnButtons, BoxLayout.X_AXIS));
        pnButtons.add(btnStart);
        pnButtons.add(btnStop);
        pnButtons.add(btnGC);
        tfLink.setMaximumSize(new Dimension(300, 20));
        JPanel pnContent = new JPanel();
        pnContent.setLayout(new BoxLayout(pnContent, BoxLayout.Y_AXIS));
        pnContent.add(new JLabel("Delay between listFiles() invocation (ms):"));
        pnContent.add(slider);
        pnContent.add(new JLabel("Provide link here:"));
        pnContent.add(tfLink);
        pnContent.add(pnButtons);
        return pnContent;
    }
    private void setEnabledState(boolean enabled) {
        slider.setEnabled(enabled);
        btnStart.setEnabled(enabled);
        btnStop.setEnabled(!enabled);
    }
    private static void fail(String msg) {
        throw new RuntimeException(msg);
    }
    private class MyThread extends Thread {
        private final int delay;
        private final ShellFolder link;
        private MyThread(int delay, String link) {
            this.delay = delay;
            ShellFolder linkFolder;
            try {
                linkFolder = ShellFolder.getShellFolder(new File(link));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                linkFolder = null;
            }
            this.link = linkFolder;
        }
        public void run() {
            while (!isInterrupted()) {
                folder.listFiles();
                if (link != null) {
                    try {
                        link.getLinkLocation();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                if (delay > 0) {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e1) {
                        return;
                    }
                }
            }
        }
    }
}
