public class LongCellRenderer extends DefaultTableCellRenderer {
    private JFormattedTextField textField;
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column)  {
        if (textField == null) {
            textField = new JFormattedTextField();
            textField.setFont(table.getFont());
            textField.setHorizontalAlignment(JTextField.RIGHT);
        }
        textField.setForeground(isSelected ? table.getSelectionForeground() :
                                table.getForeground());
        textField.setBackground(isSelected ? table.getSelectionBackground() :
                                table.getBackground());
        if (hasFocus) {
            textField.setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
        } else {
            textField.setBorder(noFocusBorder);
        }
        textField.setValue((Long)value);
        return textField;
    }
}
