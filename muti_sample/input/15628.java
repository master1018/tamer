public abstract class RowSorter<M> {
    private EventListenerList listenerList = new EventListenerList();
    public RowSorter() {
    }
    public abstract M getModel();
    public abstract void toggleSortOrder(int column);
    public abstract int convertRowIndexToModel(int index);
    public abstract int convertRowIndexToView(int index);
    public abstract void setSortKeys(List<? extends SortKey> keys);
    public abstract List<? extends SortKey> getSortKeys();
    public abstract int getViewRowCount();
    public abstract int getModelRowCount();
    public abstract void modelStructureChanged();
    public abstract void allRowsChanged();
    public abstract void rowsInserted(int firstRow, int endRow);
    public abstract void rowsDeleted(int firstRow, int endRow);
    public abstract void rowsUpdated(int firstRow, int endRow);
    public abstract void rowsUpdated(int firstRow, int endRow, int column);
    public void addRowSorterListener(RowSorterListener l) {
        listenerList.add(RowSorterListener.class, l);
    }
    public void removeRowSorterListener(RowSorterListener l) {
        listenerList.remove(RowSorterListener.class, l);
    }
    protected void fireSortOrderChanged() {
        fireRowSorterChanged(new RowSorterEvent(this));
    }
    protected void fireRowSorterChanged(int[] lastRowIndexToModel) {
        fireRowSorterChanged(new RowSorterEvent(this,
                RowSorterEvent.Type.SORTED, lastRowIndexToModel));
    }
    void fireRowSorterChanged(RowSorterEvent event) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == RowSorterListener.class) {
                ((RowSorterListener)listeners[i + 1]).
                        sorterChanged(event);
            }
        }
    }
    public static class SortKey {
        private int column;
        private SortOrder sortOrder;
        public SortKey(int column, SortOrder sortOrder) {
            if (sortOrder == null) {
                throw new IllegalArgumentException(
                        "sort order must be non-null");
            }
            this.column = column;
            this.sortOrder = sortOrder;
        }
        public final int getColumn() {
            return column;
        }
        public final SortOrder getSortOrder() {
            return sortOrder;
        }
        public int hashCode() {
            int result = 17;
            result = 37 * result + column;
            result = 37 * result + sortOrder.hashCode();
            return result;
        }
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o instanceof SortKey) {
                return (((SortKey)o).column == column &&
                        ((SortKey)o).sortOrder == sortOrder);
            }
            return false;
        }
    }
}
