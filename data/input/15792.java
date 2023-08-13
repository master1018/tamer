public class SortHeaderCellRenderer extends DefaultTableCellRenderer {
    private Icon descendingIcon;
    private Icon ascendingIcon;
    private SortableTableModel model;
    public SortHeaderCellRenderer(JTableHeader header, SortableTableModel model) {
        this.model = model;
        descendingIcon = getIcon("navigation/Down16.gif");
        ascendingIcon = getIcon("navigation/Up16.gif");
        setForeground(header.getForeground());
        setBackground(header.getBackground());
        setFont(header.getFont());
        setBorder(UIManager.getBorder("TableHeader.cellBorder"));
        setHorizontalAlignment(JLabel.CENTER);
    }
    public ImageIcon getIcon(String name) {
        String imagePath = "/toolbarButtonGraphics/" + name;
        java.net.URL url = this.getClass().getResource(imagePath);
        if (url != null) {
            return new ImageIcon(url);
        }
        return null;
    }
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column)  {
        setText((value == null) ? "" : value.toString());
        Icon icon = null;
        if (column == model.getColumn()) {
            if (model.isAscending()) {
                icon = ascendingIcon;
            } else {
                icon = descendingIcon;
            }
        }
        setIcon(icon);
        return this;
    }
}
