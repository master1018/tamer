public class TableRowSorter<M extends TableModel> extends DefaultRowSorter<M, Integer> {
    private static final Comparator COMPARABLE_COMPARATOR =
            new ComparableComparator();
    private M tableModel;
    private TableStringConverter stringConverter;
    public TableRowSorter() {
        this(null);
    }
    public TableRowSorter(M model) {
        setModel(model);
    }
    public void setModel(M model) {
        tableModel = model;
        setModelWrapper(new TableRowSorterModelWrapper());
    }
    public void setStringConverter(TableStringConverter stringConverter) {
        this.stringConverter = stringConverter;
    }
    public TableStringConverter getStringConverter() {
        return stringConverter;
    }
    public Comparator<?> getComparator(int column) {
        Comparator comparator = super.getComparator(column);
        if (comparator != null) {
            return comparator;
        }
        Class columnClass = getModel().getColumnClass(column);
        if (columnClass == String.class) {
            return Collator.getInstance();
        }
        if (Comparable.class.isAssignableFrom(columnClass)) {
            return COMPARABLE_COMPARATOR;
        }
        return Collator.getInstance();
    }
    protected boolean useToString(int column) {
        Comparator comparator = super.getComparator(column);
        if (comparator != null) {
            return false;
        }
        Class columnClass = getModel().getColumnClass(column);
        if (columnClass == String.class) {
            return false;
        }
        if (Comparable.class.isAssignableFrom(columnClass)) {
            return false;
        }
        return true;
    }
    private class TableRowSorterModelWrapper extends ModelWrapper<M,Integer> {
        public M getModel() {
            return tableModel;
        }
        public int getColumnCount() {
            return (tableModel == null) ? 0 : tableModel.getColumnCount();
        }
        public int getRowCount() {
            return (tableModel == null) ? 0 : tableModel.getRowCount();
        }
        public Object getValueAt(int row, int column) {
            return tableModel.getValueAt(row, column);
        }
        public String getStringValueAt(int row, int column) {
            TableStringConverter converter = getStringConverter();
            if (converter != null) {
                String value = converter.toString(
                        tableModel, row, column);
                if (value != null) {
                    return value;
                }
                return "";
            }
            Object o = getValueAt(row, column);
            if (o == null) {
                return "";
            }
            String string = o.toString();
            if (string == null) {
                return "";
            }
            return string;
        }
        public Integer getIdentifier(int index) {
            return index;
        }
    }
    private static class ComparableComparator implements Comparator {
        @SuppressWarnings("unchecked")
        public int compare(Object o1, Object o2) {
            return ((Comparable)o1).compareTo(o2);
        }
    }
}
