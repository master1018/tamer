public class WslList extends JList {
    public static final int DEFAULT_WIDTH = 100;
    private JScrollPane _sp;
    private Dimension _dimension = null;
    public WslList() {
        super(new DefaultListModel());
        _sp = new JScrollPane(this);
    }
    public WslList(int height) {
        super(new DefaultListModel());
        _sp = new JScrollPane(this);
        _dimension = new Dimension(DEFAULT_WIDTH, height);
        updateSize();
    }
    public WslList(Dimension dimension) {
        super(new DefaultListModel());
        _sp = new JScrollPane(this);
        _dimension = dimension;
        updateSize();
    }
    private void updateSize() {
        setSize(getPreferredSize());
        setMinimumSize(getPreferredSize());
        setMaximumSize(getPreferredSize());
    }
    public WslList(String entityName) {
        _sp = new JScrollPane(this);
        DataSource ds = DataManager.getDataSource(entityName);
        Util.argCheckNull(ds);
        try {
            RecordSet rs = ds.select(new Query(entityName));
            if (rs != null) buildFromRecordSet(rs);
        } catch (DataSourceException e) {
            throw new RuntimeException(e.toString());
        }
    }
    public WslList(RecordSet rs) {
        _sp = new JScrollPane(this);
        buildFromRecordSet(rs);
    }
    public WslList(Vector v) {
        _sp = new JScrollPane(this);
        buildFromVector(v);
    }
    public DefaultListModel getDefaultModel() {
        return (DefaultListModel) getModel();
    }
    public JScrollPane getScrollPane() {
        return _sp;
    }
    public void buildFromRecordSet(RecordSet rs) {
        buildFromVector(rs.getRows());
    }
    public void buildFromVector(Vector v) {
        buildFromArray(v.toArray());
    }
    public void buildFromArray(Object[] array) {
        Util.argCheckNull(array);
        for (int i = 0; i < array.length; i++) getDefaultModel().addElement(array[i]);
    }
    public void addItem(Object obj) {
        Util.argCheckNull(obj);
        getDefaultModel().addElement(obj);
    }
    public void removeItem(Object obj) {
        Util.argCheckNull(obj);
        getDefaultModel().removeElement(obj);
    }
    public void clear() {
        getDefaultModel().removeAllElements();
    }
    public int selectItem(String text) {
        for (int i = 0; i < getModel().getSize(); i++) {
            if (getModel().getElementAt(i).toString().equalsIgnoreCase(text)) {
                setSelectedIndex(i);
                return i;
            }
        }
        return -1;
    }
    public Dimension getPreferredSize() {
        return (_dimension == null) ? super.getPreferredSize() : _dimension;
    }
}
