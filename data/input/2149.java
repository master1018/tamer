public class TableSorter extends DefaultTableModel implements MouseListener {
    private boolean ascending = true;
    private TableColumnModel columnModel;
    private JTable tableView;
    private Vector<TableModelListener> evtListenerList;
    private int sortColumn = 0;
    private int[] invertedIndex;
    public TableSorter() {
        super();
        evtListenerList = new Vector<TableModelListener>();
    }
    public TableSorter(Object[] columnNames, int numRows) {
        super(columnNames,numRows);
        evtListenerList = new Vector<TableModelListener>();
    }
    @Override
    public void newDataAvailable(TableModelEvent e) {
        super.newDataAvailable(e);
        invertedIndex = new int[getRowCount()];
        for (int i = 0; i < invertedIndex.length; i++) {
            invertedIndex[i] = i;
        }
        sort(this.sortColumn, this.ascending);
    }
    @Override
    public void addTableModelListener(TableModelListener l) {
        evtListenerList.add(l);
        super.addTableModelListener(l);
    }
    @Override
    public void removeTableModelListener(TableModelListener l) {
        evtListenerList.remove(l);
        super.removeTableModelListener(l);
    }
    private void removeListeners() {
        for(TableModelListener tnl : evtListenerList)
            super.removeTableModelListener(tnl);
    }
    private void restoreListeners() {
        for(TableModelListener tnl : evtListenerList)
            super.addTableModelListener(tnl);
    }
    @SuppressWarnings("unchecked")
    private int compare(Object o1, Object o2) {
        if (o1 == o2)
            return 0;
        if (o1==null)
            return 1;
        if (o2==null)
            return -1;
        else if ((o1.getClass().equals(o2.getClass())) &&
                 (o1 instanceof Comparable)) {
            return (((Comparable) o1).compareTo(o2));
        }
        else {
            return o1.toString().compareTo(o2.toString());
        }
    }
    private void sort(int column, boolean isAscending) {
        final XMBeanAttributes attrs =
                (tableView instanceof XMBeanAttributes)
                ?(XMBeanAttributes) tableView
                :null;
        if (attrs != null && attrs.isEditing())
            attrs.cancelCellEditing();
        removeListeners();
        if (JConsole.isDebug()) {
            System.err.println("sorting table against column="+column
                    +" ascending="+isAscending);
        }
        quickSort(0,getRowCount()-1,column,isAscending);
        restoreListeners();
        if (attrs != null) {
            for (int i = 0; i < getRowCount(); i++) {
                Vector data = (Vector) dataVector.elementAt(i);
                attrs.updateRowHeight(data.elementAt(1), i);
            }
        }
    }
    private boolean compareS(Object s1, Object s2, boolean isAscending) {
        if (isAscending)
            return (compare(s1,s2) > 0);
        else
            return (compare(s1,s2) < 0);
    }
    private boolean compareG(Object s1, Object s2, boolean isAscending) {
        if (isAscending)
            return (compare(s1,s2) < 0);
        else
            return (compare(s1,s2) > 0);
    }
    private void quickSort(int lo0,int hi0, int key, boolean isAscending) {
        int lo = lo0;
        int hi = hi0;
        Object mid;
        if ( hi0 > lo0)
            {
                mid = getValueAt( ( lo0 + hi0 ) / 2 , key);
                while( lo <= hi )
                    {
                        while( ( lo < hi0 ) &&
                               ( compareS(mid,getValueAt(lo,key), isAscending) ))
                            ++lo;
                        while( ( hi > lo0 ) &&
                               ( compareG(mid,getValueAt(hi,key), isAscending) ))
                            --hi;
                        if( lo <= hi )
                            {
                                swap(lo, hi, key);
                                ++lo;
                                --hi;
                            }
                    }
                if( lo0 < hi )
                    quickSort(lo0, hi , key, isAscending);
                if( lo <= hi0 )
                    quickSort(lo, hi0 , key, isAscending);
            }
    }
    private Vector getRow(int row) {
        return (Vector) dataVector.elementAt(row);
    }
    @SuppressWarnings("unchecked")
    private void setRow(Vector data, int row) {
        dataVector.setElementAt(data,row);
    }
    private void swap(int i, int j, int column) {
        Vector data = getRow(i);
        setRow(getRow(j),i);
        setRow(data,j);
        int a = invertedIndex[i];
        invertedIndex[i] = invertedIndex[j];
        invertedIndex[j] = a;
    }
    public void sortByColumn(int column) {
        sortByColumn(column, !ascending);
    }
    public void sortByColumn(int column, boolean ascending) {
        this.ascending = ascending;
        this.sortColumn = column;
        sort(column,ascending);
    }
    public int getIndexOfRow(int row) {
        return invertedIndex[row];
    }
    public void addMouseListenerToHeaderInTable(JTable table) {
        tableView = table;
        columnModel = tableView.getColumnModel();
        JTableHeader th = tableView.getTableHeader();
        th.addMouseListener(this);
    }
    public void mouseClicked(MouseEvent e) {
        int viewColumn = columnModel.getColumnIndexAtX(e.getX());
        int column = tableView.convertColumnIndexToModel(viewColumn);
        if (e.getClickCount() == 1 && column != -1) {
            if (tableView instanceof XTable) {
                XTable attrs = (XTable) tableView;
                attrs.sortRequested(column);
            }
            tableView.invalidate();
            sortByColumn(column);
            tableView.validate();
            tableView.repaint();
        }
    }
    public void mousePressed(MouseEvent e) {
    }
    public void mouseEntered(MouseEvent e) {
    }
    public void mouseExited(MouseEvent e) {
    }
    public void mouseReleased(MouseEvent e) {
    }
}
