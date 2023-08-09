public abstract class TablePanel extends ClientDisplayPanel {
    private ITableFocusListener mGlobalListener;
    public final void setTableFocusListener(ITableFocusListener listener) {
        mGlobalListener = listener;
        setTableFocusListener();
    }
    protected void setTableFocusListener() {
    }
    protected final void addTableToFocusListener(final Table table,
            final int colStart, final int colEnd) {
        final IFocusedTableActivator activator = new IFocusedTableActivator() {
            public void copy(Clipboard clipboard) {
                int[] selection = table.getSelectionIndices();
                Arrays.sort(selection);
                StringBuilder sb = new StringBuilder();
                for (int i : selection) {
                    TableItem item = table.getItem(i);
                    for (int c = colStart ; c <= colEnd ; c++) {
                        sb.append(item.getText(c));
                        sb.append('\t');
                    }
                    sb.append('\n');
                }
                String data = sb.toString();
                if (data != null || data.length() > 0) {
                    clipboard.setContents(
                            new Object[] { data },
                            new Transfer[] { TextTransfer.getInstance() });
                }
            }
            public void selectAll() {
                table.selectAll();
            }
        };
        table.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                mGlobalListener.focusGained(activator);
            }
            public void focusLost(FocusEvent e) {
                mGlobalListener.focusLost(activator);
            }
        });
    }
    protected final void addTableToFocusListener(final Table table) {
        addTableToFocusListener(table, 0, table.getColumnCount()-1);
    }
}
