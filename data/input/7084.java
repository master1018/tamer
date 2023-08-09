public class bug6884066 {
    private static JTableHeader header;
    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        SunToolkit toolkit = (SunToolkit) Toolkit.getDefaultToolkit();
        Robot robot = new Robot();
        robot.setAutoDelay(20);
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                JTable table = new JTable(10, 5);
                header = new JTableHeader(table.getColumnModel());
                checkColumn(0, "A");
                JFrame frame = new JFrame("standalone header");
                frame.add(header);
                frame.pack();
                frame.setVisible(true);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            }
        });
        toolkit.realSync();
        Point point = header.getLocationOnScreen();
        robot.mouseMove(point.x + 3, point.y + 3);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        for (int i = 0; i < header.getWidth() - 3; i++) {
            robot.mouseMove(point.x + i, point.y + 3);
        }
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                TableColumnModel model = header.getColumnModel();
                checkColumn(model.getColumnCount() - 1, "A");
            }
        });
    }
    private static void checkColumn(int index, String str) {
        TableColumnModel model = header.getColumnModel();
        Object value = model.getColumn(index).getHeaderValue();
        if (!str.equals(value)) {
            throw new RuntimeException("Unexpected header's value; " +
                    "index = " + index + " value = " + value);
        }
    }
}
