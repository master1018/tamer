public class PropertiesTableModel extends DefaultTableModel {
    private List<ViewNode.Property> properties;
    private List<ViewNode.Property> privateProperties = new ArrayList<ViewNode.Property>();
    public PropertiesTableModel(ViewNode node) {
        properties = node.properties;
        loadPrivateProperties(node);
    }
    private void loadPrivateProperties(ViewNode node) {
        int x = node.left;
        int y = node.top;
        ViewNode p = node.parent;
        while (p != null) {
            x += p.left - p.scrollX;
            y += p.top - p.scrollY;
            p = p.parent;
        }
        ViewNode.Property property = new ViewNode.Property();
        property.name = "absolute_x";
        property.value = String.valueOf(x);
        privateProperties.add(property);
        property = new ViewNode.Property();
        property.name = "absolute_y";
        property.value = String.valueOf(y);
        privateProperties.add(property);
    }
    @Override
    public int getRowCount() {
        return (privateProperties == null ? 0 : privateProperties.size()) +
                (properties == null ? 0 : properties.size());
    }
    @Override
    public Object getValueAt(int row, int column) {
        ViewNode.Property property;
        if (row < privateProperties.size()) {
            property = privateProperties.get(row);
        } else {
            property = properties.get(row - privateProperties.size());
        }
        return column == 0 ? property.name : property.value;
    }
    @Override
    public int getColumnCount() {
        return 2;
    }
    @Override
    public String getColumnName(int column) {
        return column == 0 ? "Property" : "Value";
    }
    @Override
    public boolean isCellEditable(int arg0, int arg1) {
        return false;
    }
    @Override
    public void setValueAt(Object arg0, int arg1, int arg2) {
    }
}
