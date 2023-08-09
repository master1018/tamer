public class SortHeaderMouseAdapter extends MouseAdapter {
    private SortableTableModel model;
    private JTable table;
    public SortHeaderMouseAdapter(JTable table, SortableTableModel model) {
        this.model = model;
        this.table = table;
    }
    public void mouseClicked(MouseEvent evt) {
        CommonUI.setWaitCursor(SwingUtilities.getRoot(table));
        TableColumnModel columnModel = table.getColumnModel();
        int viewColumn = columnModel.getColumnIndexAtX(evt.getX());
        int column = table.convertColumnIndexToModel(viewColumn);
        if (evt.getClickCount() == 1 && column != -1) {
            model.sortByColumn(column, !model.isAscending());
        }
        CommonUI.setDefaultCursor(SwingUtilities.getRoot(table));
    }
}
