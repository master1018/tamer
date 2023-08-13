public class bug6711682 {
    private static JCheckBox editorCb;
    private static JCheckBox rendererCb;
    private static JTable table;
    public static void main(String[] args) throws Exception {
        Robot robot = new Robot();
        robot.setAutoDelay(50);
        SunToolkit toolkit = (SunToolkit) Toolkit.getDefaultToolkit();
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
        toolkit.realSync();
        Point l = table.getLocationOnScreen();
        int h = table.getRowHeight();
        for (int i = 0; i < 3; i++) {
            robot.mouseMove(l.x + 5, l.y + 5 + i * h);
            robot.mousePress(InputEvent.BUTTON1_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_MASK);
        }
        robot.keyPress(KeyEvent.VK_F2);
        robot.keyRelease(KeyEvent.VK_F2);
        for (int i = 0; i < 3; i++) {
            if (!Boolean.TRUE.equals(table.getValueAt(i, 0))) {
                throw new RuntimeException("Row #" + i + " checkbox is not selected");
            }
        }
        for (int i = 2; i >= 0; i--) {
            robot.mouseMove(l.x + 5, l.y + 5 + i * h);
            robot.mousePress(InputEvent.BUTTON1_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_MASK);
        }
        robot.keyPress(KeyEvent.VK_F2);
        robot.keyRelease(KeyEvent.VK_F2);
        for (int i = 0; i < 3; i++) {
            if (Boolean.TRUE.equals(table.getValueAt(i, 0))) {
                throw new RuntimeException("Row #" + i + " checkbox is selected");
            }
        }
    }
    private static void createAndShowGUI() {
        editorCb = new JCheckBox();
        rendererCb = new JCheckBox();
        JFrame f = new JFrame("Table with CheckBox");
        Container p = f.getContentPane();
        p.setLayout(new BorderLayout());
        table = new JTable(new Object[][]{{false}, {false}, {false}}, new Object[]{"CheckBox"});
        TableCellEditor editor = new TableCellEditor() {
            int editedRow;
            public Component getTableCellEditorComponent(JTable table,
                                                         Object value, boolean isSelected, int row, int column) {
                this.editedRow = row;
                editorCb.setSelected(Boolean.TRUE.equals(value));
                editorCb.setBackground(UIManager.getColor("Table.selectionBackground"));
                return editorCb;
            }
            public void addCellEditorListener(CellEditorListener l) {
            }
            public void cancelCellEditing() {
            }
            public Object getCellEditorValue() {
                return editorCb.isSelected();
            }
            public boolean isCellEditable(EventObject anEvent) {
                return true;
            }
            public void removeCellEditorListener(CellEditorListener l) {
            }
            public boolean shouldSelectCell(EventObject anEvent) {
                return true;
            }
            public boolean stopCellEditing() {
                table.getModel().setValueAt(editorCb.isSelected(), editedRow, 0);
                return true;
            }
        };
        table.getColumnModel().getColumn(0).setCellEditor(editor);
        TableCellRenderer renderer = new TableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table,
                                                           Object value, boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                rendererCb.setSelected(Boolean.TRUE.equals(value));
                return rendererCb;
            }
        };
        table.getColumnModel().getColumn(0).setCellRenderer(renderer);
        p.add(table, BorderLayout.CENTER);
        f.pack();
        f.setVisible(true);
    }
}
