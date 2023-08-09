public abstract class SortableTableModel extends AbstractTableModel {
    private TableModelComparator comparator;
    protected java.util.List elements;
    public void setComparator(TableModelComparator comparator) {
        this.comparator = comparator;
    }
    public void sortByColumn(int column, boolean ascending) {
        comparator.addColumn(column);
        comparator.setAscending(ascending);
        Collections.sort(elements, comparator);
        fireTableChanged(new TableModelEvent(this));
    }
    public boolean isAscending() {
        return comparator.isAscending();
    }
    public int getColumn() {
        return comparator.getColumn();
    }
}
