public class ExtendedAttributesTableModel extends AbstractTableModel {
    public static final int NUM_COLUMNS = 2;
    private Properties props = null;
    private DataVariable variable = null;
    private Decomposition decomposition = null;
    private NetGraph graph = null;
    private Vector<ExtendedAttribute> rows = new Vector<ExtendedAttribute>();
    public ExtendedAttributesTableModel(DataVariable variable) throws IOException {
        this.variable = variable;
        if (variable != null && variable.getScope() != null) {
            this.decomposition = variable.getScope().getDecomposition();
            loadDefaultProperties();
            loadUserDefinedProperties();
            variable.setAttributes(removeDefunctAttributes(variable.getAttributes()));
        }
    }
    public ExtendedAttributesTableModel(Decomposition decomposition, NetGraph graph) throws IOException {
        this.decomposition = decomposition;
        this.graph = graph;
        if (this.decomposition != null) {
            loadDefaultProperties();
            loadUserDefinedProperties();
            decomposition.setAttributes(removeDefunctAttributes(decomposition.getAttributes()));
        }
    }
    private void loadUserDefinedProperties() throws IOException {
        Preferences prefs = Preferences.userNodeForPackage(YAWLEditor.class);
        String streamPath = (variable != null) ? prefs.get("ExtendedAttributeVariableFilePath", DataVariable.PROPERTY_LOCATION) : prefs.get("ExtendedAttributeDecompositionFilePath", Decomposition.PROPERTY_LOCATION);
        if (streamPath != null) {
            props = new Properties();
            try {
                props.load(new FileInputStream(streamPath));
                parseProperties();
            } catch (FileNotFoundException fnfe) {
            }
        }
    }
    private void loadDefaultProperties() {
        if (variable != null) {
            rows = new DefaultExtendedAttributes(decomposition, variable).getAttributes();
        } else rows = new DefaultExtendedAttributes(graph, decomposition).getAttributes();
    }
    private void parseProperties() {
        if (variable != null) {
            parseVariableProperties();
        } else if (decomposition != null) {
            parseDecompositionProperties();
        }
    }
    private void parseVariableProperties() {
        if (props != null) {
            Vector<ExtendedAttribute> udAttributes = new Vector<ExtendedAttribute>();
            for (Object o : props.keySet()) {
                String key = (String) o;
                String type = props.getProperty(key).trim().toLowerCase();
                String value = null;
                if (variable != null) {
                    value = (String) variable.getAttributes().get(key);
                    if (value == null) value = "";
                }
                ExtendedAttribute attribute = new ExtendedAttribute(variable, decomposition, key, type, value);
                if (attribute.getAttributeType() != ExtendedAttribute.SYSTEM_ATTRIBUTE) {
                    attribute.setAttributeType(ExtendedAttribute.USER_ATTRIBUTE);
                }
                udAttributes.add(attribute);
            }
            Collections.sort(udAttributes);
            rows.addAll(udAttributes);
        }
    }
    private void parseDecompositionProperties() {
        if (props != null) {
            Vector<ExtendedAttribute> udAttributes = new Vector<ExtendedAttribute>();
            for (Object o : props.keySet()) {
                String key = (String) o;
                String type = props.getProperty(key).trim().toLowerCase();
                String value = null;
                if (decomposition != null) {
                    value = (String) decomposition.getAttributes().get(key);
                    if (value == null) value = "";
                }
                ExtendedAttribute attribute = new ExtendedAttribute(graph, decomposition, key, type, value);
                if (attribute.getAttributeType() != ExtendedAttribute.SYSTEM_ATTRIBUTE) {
                    attribute.setAttributeType(ExtendedAttribute.USER_ATTRIBUTE);
                }
                udAttributes.add(attribute);
            }
            Collections.sort(udAttributes);
            rows.addAll(udAttributes);
        }
    }
    private Hashtable removeDefunctAttributes(Hashtable attributes) {
        List<String> toRemove = new ArrayList<String>();
        if (attributes != null) {
            for (Object o : attributes.keySet()) {
                String key = (String) o;
                if (!rowsContainsKey(key)) {
                    toRemove.add(key);
                }
            }
            for (String remKey : toRemove) {
                attributes.remove(remKey);
            }
        }
        return attributes;
    }
    private boolean rowsContainsKey(String key) {
        for (ExtendedAttribute exAttr : rows) {
            if (exAttr.getName().equals(key)) {
                return true;
            }
        }
        return false;
    }
    public int getColumnCount() {
        return NUM_COLUMNS;
    }
    public int getRowCount() {
        return rows.size();
    }
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch(columnIndex) {
            case 0:
                return rows.get(rowIndex).getName();
            case 1:
                return rows.get(rowIndex);
            default:
                return "";
        }
    }
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 1) {
            ExtendedAttribute attribute = rows.get(rowIndex);
            attribute.setValue(aValue.toString());
            if (variable != null) {
                variable.setAttribute(attribute.getName(), attribute.getValue());
                if (attribute.getGroup() != null) {
                    updateGroup(attribute.getGroup());
                }
            } else {
                decomposition.setAttribute(attribute.getName(), attribute.getValue());
                if (attribute.getGroup() != null) {
                    updateGroup(attribute.getGroup());
                }
            }
        }
        fireTableChanged(new TableModelEvent(this, rowIndex, rowIndex, columnIndex));
    }
    public String getColumnName(int column) {
        switch(column) {
            case 0:
                return "Name";
            case 1:
                return "Value";
            default:
                return "";
        }
    }
    public Class getColumnClass(int columnIndex) {
        return (columnIndex == 1) ? ExtendedAttribute.class : String.class;
    }
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 1;
    }
    public void setVariable(DataVariable variable) {
        this.variable = variable;
        this.decomposition = variable.getScope().getDecomposition();
        loadAndParseProperties();
        if (variable != null) {
            variable.setAttributes(removeDefunctAttributes(variable.getAttributes()));
        }
    }
    public void setDecomposition(Decomposition decomposition) {
        this.decomposition = decomposition;
        loadAndParseProperties();
        if (decomposition != null) {
            decomposition.setAttributes(removeDefunctAttributes(decomposition.getAttributes()));
        }
    }
    private void loadAndParseProperties() {
        loadDefaultProperties();
        try {
            loadUserDefinedProperties();
        } catch (IOException e) {
        } finally {
            fireTableDataChanged();
        }
    }
    private void updateGroup(ExtendedAttributeGroup group) {
        for (ExtendedAttribute attribute : group) {
            if (variable != null) {
                variable.setAttribute(attribute.getName(), attribute.getValue());
            } else {
                decomposition.setAttribute(attribute.getName(), attribute.getValue());
            }
        }
    }
}
