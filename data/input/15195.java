public abstract class TableModelComparator implements Comparator {
    private boolean ascending;
    protected TableModel model;
    private int[] columns;
    public TableModelComparator(TableModel model) {
        this.model = model;
        columns = new int[model.getColumnCount()];
        columns[0] = -1;
    }
    public void addColumn(int column) {
        int[] tempArray = new int[model.getColumnCount()];
        System.arraycopy(columns, 1, tempArray, 0, columns.length - 1);
        columns = tempArray;
        columns[0] = column;
    }
    public int getColumn() {
        return columns[0];
    }
    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }
    public boolean isAscending() {
        return ascending;
    }
    public int compare(Object row1, Object row2) {
        for (int i = 0; i < columns.length; i++) {
            Object o1 = getValueForColumn(row1, columns[i]);
            Object o2 = getValueForColumn(row2, columns[i]);
            if (o1 == null && o2 == null) {
                return 0;
            } else if (o1 == null) { 
                return -1;
            } else if (o2 == null) {
                return 1;
            }
            int result = 0;
            if (o1 instanceof Comparable) {
                Comparable c1 = (Comparable)o1;
                Comparable c2 = (Comparable)o2;
                result = c1.compareTo(c2);
            }
            if (result != 0) {
                return ascending ? result : -result;
            }
        }
        return 0;
    }
    public abstract Object getValueForColumn(Object obj, int column);
}
