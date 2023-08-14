public class bug6647340 {
    private JFrame frame;
    private Point location;
    private JInternalFrame jif;
    public static void main(String[] args) throws Exception {
        final bug6647340 test = new bug6647340();
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    test.setupUI();
                }
            });
            test.test();
        } finally {
            if (test.frame != null) {
                test.frame.dispose();
            }
        }
    }
    private void setupUI() {
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JDesktopPane desktop = new JDesktopPane();
        frame.add(desktop);
        jif = new JInternalFrame("Internal Frame", true, true, true, true);
        jif.setBounds(20, 20, 200, 100);
        desktop.add(jif);
        jif.setVisible(true);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setBounds((screen.width - 400) / 2, (screen.height - 400) / 2, 400, 400);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    private void test() throws Exception {
        realSync();
        test1();
        realSync();
        check1();
        realSync();
        test2();
        realSync();
        check2();
    }
    private void test1() throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                setIcon(true);
                location = jif.getDesktopIcon().getLocation();
                Dimension size = frame.getSize();
                frame.setSize(size.width + 100, size.height + 100);
            }
        });
    }
    private void test2() throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                setIcon(false);
            }
        });
        realSync();
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                Dimension size = frame.getSize();
                frame.setSize(size.width - 100, size.height - 100);
            }
        });
        realSync();
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                setIcon(true);
            }
        });
    }
    private void check1() {
        if (!jif.getDesktopIcon().getLocation().equals(location)) {
            System.out.println("First test passed");
        } else {
            throw new RuntimeException("Icon isn't shifted with the frame bounds");
        }
    }
    private void check2() {
        if (jif.getDesktopIcon().getLocation().equals(location)) {
            System.out.println("Second test passed");
        } else {
            throw new RuntimeException("Icon isn't located near the frame bottom");
        }
    }
    private static void realSync() {
        ((SunToolkit) (Toolkit.getDefaultToolkit())).realSync();
    }
    private void setIcon(boolean b) {
        try {
            jif.setIcon(b);
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }
    }
}
