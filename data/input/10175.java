public class bug7032791 {
    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new SynthLookAndFeel());
        Object value = "Test value";
        JTable table = new JTable(1, 1);
        TableCellRenderer renderer = table.getDefaultRenderer(Object.class);
        renderer.getTableCellRendererComponent(null, value, true, true, 0, 0);
        System.out.println("OK");
    }
}
