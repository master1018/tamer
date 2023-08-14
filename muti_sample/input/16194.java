public class bug6607130 {
    private JFrame frame;
    private JComboBox cb;
    private Robot robot;
    public static void main(String[] args) throws Exception {
        final bug6607130 test = new bug6607130();
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
    public bug6607130() throws AWTException {
        robot = new Robot();
    }
    private void setupUI() {
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        DefaultTableModel model = new DefaultTableModel(1, 1);
        JTable table = new JTable(model);
        cb = new JComboBox(new String[]{"one", "two", "three"});
        table.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(cb));
        frame.add(table);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    private void test() throws Exception {
        realSync();
        test1();
        realSync();
        checkResult("First test");
        test2();
        realSync();
        checkResult("Second test");
    }
    private void test1() throws Exception {
        hitKey(KeyEvent.VK_TAB);
        realSync();
        hitKey(KeyEvent.VK_F2);
        realSync();
        hitKey(KeyEvent.VK_DOWN);
        realSync();
        hitKey(KeyEvent.VK_DOWN);
        realSync();
        hitKey(KeyEvent.VK_ENTER);
        realSync();
        hitKey(KeyEvent.VK_F2);
        realSync();
        hitKey(KeyEvent.VK_DOWN);
        realSync();
        hitKey(KeyEvent.VK_ENTER);
        realSync();
    }
    private void test2() throws Exception {
        hitKey(KeyEvent.VK_F2);
        realSync();
        hitKey(KeyEvent.VK_ENTER);
        realSync();
    }
    private void checkResult(String testName) {
        if (!cb.isShowing()) {
            System.out.println(testName + " passed");
        } else {
            System.out.println(testName + " failed");
            throw new RuntimeException("JComboBox is showing " +
                    "after item selection.");
        }
    }
    private static void realSync() {
        ((SunToolkit) (Toolkit.getDefaultToolkit())).realSync();
    }
    public void hitKey(int keycode) {
        robot.keyPress(keycode);
        robot.keyRelease(keycode);
        delay();
    }
    private void delay() {
        try {
            Thread.sleep(1000);
        } catch(InterruptedException ie) {
            ie.printStackTrace();
        }
    }
}
