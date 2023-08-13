public class TableTest {
    public static void main(String[] args) throws Exception {
        KeyboardFocusManager.getCurrentKeyboardFocusManager();
        System.setSecurityManager(new AppletSecurity());
        JTable table = new JTable();
        TableCellEditor de = table.getDefaultEditor(Double.class);
        if (de == null) {
            throw new RuntimeException("Table default editor is null");
        }
    }
}
