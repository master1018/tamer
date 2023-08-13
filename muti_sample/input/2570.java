public class Test6505027 {
    private static final boolean INTERNAL = true;
    private static final boolean TERMINATE = true;
    private static final int WIDTH = 450;
    private static final int HEIGHT = 200;
    private static final int OFFSET = 10;
    private static final String[] COLUMNS = { "Size", "Shape" }; 
    private static final String[] ITEMS = { "a", "b", "c", "d" }; 
    private static final String KEY = "terminateEditOnFocusLost"; 
    public static void main(String[] args) throws Throwable {
        SwingTest.start(Test6505027.class);
    }
    private final JTable table = new JTable(new DefaultTableModel(COLUMNS, 2));
    public Test6505027(JFrame main) {
        Container container = main;
        if (INTERNAL) {
            JInternalFrame frame = new JInternalFrame();
            frame.setBounds(OFFSET, OFFSET, WIDTH, HEIGHT);
            frame.setVisible(true);
            JDesktopPane desktop = new JDesktopPane();
            desktop.add(frame, new Integer(1));
            container.add(desktop);
            container = frame;
        }
        if (TERMINATE) {
            this.table.putClientProperty(KEY, Boolean.TRUE);
        }
        TableColumn column = this.table.getColumn(COLUMNS[1]);
        column.setCellEditor(new DefaultCellEditor(new JComboBox(ITEMS)));
        container.add(BorderLayout.NORTH, new JTextField());
        container.add(BorderLayout.CENTER, new JScrollPane(this.table));
    }
    public void press() throws AWTException {
        Point point = this.table.getCellRect(1, 1, false).getLocation();
        SwingUtilities.convertPointToScreen(point, this.table);
        Robot robot = new Robot();
        robot.mouseMove(point.x + 1, point.y + 1);
        robot.mousePress(InputEvent.BUTTON1_MASK);
    }
    public static void validate() {
        Component component = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (!component.getClass().equals(JComboBox.class)) {
            throw new Error("unexpected focus owner: " + component);
        }
    }
}
