public class WindowsTableHeaderUI extends BasicTableHeaderUI {
    private TableCellRenderer originalHeaderRenderer;
    public static ComponentUI createUI(JComponent h) {
        return new WindowsTableHeaderUI();
    }
    public void installUI(JComponent c) {
        super.installUI(c);
        if (XPStyle.getXP() != null) {
            originalHeaderRenderer = header.getDefaultRenderer();
            if (originalHeaderRenderer instanceof UIResource) {
                header.setDefaultRenderer(new XPDefaultRenderer());
            }
        }
    }
    public void uninstallUI(JComponent c) {
        if (header.getDefaultRenderer() instanceof XPDefaultRenderer) {
            header.setDefaultRenderer(originalHeaderRenderer);
        }
        super.uninstallUI(c);
    }
    @Override
    protected void rolloverColumnUpdated(int oldColumn, int newColumn) {
        if (XPStyle.getXP() != null) {
            header.repaint(header.getHeaderRect(oldColumn));
            header.repaint(header.getHeaderRect(newColumn));
        }
    }
    private class XPDefaultRenderer extends DefaultTableCellHeaderRenderer {
        Skin skin;
        boolean isSelected, hasFocus, hasRollover;
        int column;
        XPDefaultRenderer() {
            setHorizontalAlignment(LEADING);
        }
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected,
                                                hasFocus, row, column);
            this.isSelected = isSelected;
            this.hasFocus = hasFocus;
            this.column = column;
            this.hasRollover = (column == getRolloverColumn());
            if (skin == null) {
                skin = XPStyle.getXP().getSkin(header, Part.HP_HEADERITEM);
            }
            Insets margins = skin.getContentMargin();
            Border border = null;
            int contentTop = 0;
            int contentLeft = 0;
            int contentBottom = 0;
            int contentRight = 0;
            if (margins != null) {
                contentTop = margins.top;
                contentLeft = margins.left;
                contentBottom = margins.bottom;
                contentRight = margins.right;
            }
            contentLeft += 5;
            contentBottom += 4;
            contentRight += 5;
            Icon sortIcon;
            if (WindowsLookAndFeel.isOnVista()
                && ((sortIcon = getIcon()) instanceof javax.swing.plaf.UIResource
                    || sortIcon == null)) {
                contentTop += 1;
                setIcon(null);
                sortIcon = null;
                SortOrder sortOrder =
                    getColumnSortOrder(table, column);
                if (sortOrder != null) {
                    switch (sortOrder) {
                    case ASCENDING:
                        sortIcon =
                            UIManager.getIcon("Table.ascendingSortIcon");
                        break;
                    case DESCENDING:
                        sortIcon =
                            UIManager.getIcon("Table.descendingSortIcon");
                        break;
                    }
                }
                if (sortIcon != null) {
                    contentBottom = sortIcon.getIconHeight();
                    border = new IconBorder(sortIcon, contentTop, contentLeft,
                                            contentBottom, contentRight);
                } else {
                    sortIcon =
                        UIManager.getIcon("Table.ascendingSortIcon");
                    int sortIconHeight =
                        (sortIcon != null) ? sortIcon.getIconHeight() : 0;
                    if (sortIconHeight != 0) {
                        contentBottom = sortIconHeight;
                    }
                    border =
                        new EmptyBorder(
                            sortIconHeight + contentTop, contentLeft,
                            contentBottom, contentRight);
                }
            } else {
                contentTop += 3;
                border = new EmptyBorder(contentTop, contentLeft,
                                         contentBottom, contentRight);
            }
            setBorder(border);
            return this;
        }
        public void paint(Graphics g) {
            Dimension size = getSize();
            State state = State.NORMAL;
            TableColumn draggedColumn = header.getDraggedColumn();
            if (draggedColumn != null &&
                    column == SwingUtilities2.convertColumnIndexToView(
                            header.getColumnModel(), draggedColumn.getModelIndex())) {
                state = State.PRESSED;
            } else if (isSelected || hasFocus || hasRollover) {
                state = State.HOT;
            }
            if (WindowsLookAndFeel.isOnVista()) {
                SortOrder sortOrder = getColumnSortOrder(header.getTable(), column);
                if (sortOrder != null) {
                     switch(sortOrder) {
                     case ASCENDING:
                     case DESCENDING:
                         switch (state) {
                         case NORMAL:
                             state = State.SORTEDNORMAL;
                             break;
                         case PRESSED:
                             state = State.SORTEDPRESSED;
                             break;
                         case HOT:
                             state = State.SORTEDHOT;
                             break;
                         default:
                         }
                     default :
                     }
                }
            }
            skin.paintSkin(g, 0, 0, size.width-1, size.height-1, state);
            super.paint(g);
        }
    }
    private static class IconBorder implements Border, UIResource{
        private final Icon icon;
        private final int top;
        private final int left;
        private final int bottom;
        private final int right;
        public IconBorder(Icon icon, int top, int left,
                          int bottom, int right) {
            this.icon = icon;
            this.top = top;
            this.left = left;
            this.bottom = bottom;
            this.right = right;
        }
        public Insets getBorderInsets(Component c) {
            return new Insets(icon.getIconHeight() + top, left, bottom, right);
        }
        public boolean isBorderOpaque() {
            return false;
        }
        public void paintBorder(Component c, Graphics g, int x, int y,
                                int width, int height) {
            icon.paintIcon(c, g,
                x + left + (width - left - right - icon.getIconWidth()) / 2,
                y + top);
        }
    }
}
