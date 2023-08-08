public class SVGPropertyItem {
    private LinkedList nodeList;
    private String propertyType, propertyName, propertyValueType, defaultPropertyValue = "", propertyConstraint = "", generalPropertyValue = "";
    private String propertyLabel;
    private LinkedHashMap propertyValues = new LinkedHashMap();
    private LinkedHashMap valuesMap;
    private LinkedHashMap valuesLabelMap;
    private String undoredoproperties = "";
    private ResourceBundle bundle = null;
    private SVGProperties properties = null;
    public SVGPropertyItem(SVGProperties properties, LinkedList nodeList, String propertyType, String propName, String propertyValueType, String defaultPropertyValue, String propertyConstraint, LinkedHashMap valuesMap) {
        this.properties = properties;
        this.nodeList = nodeList;
        this.propertyType = propertyType;
        try {
            this.propertyName = propName.substring(propName.indexOf("_") + 1, propName.length());
        } catch (Exception ex) {
            this.propertyName = propName;
        }
        this.propertyValueType = propertyValueType;
        this.defaultPropertyValue = defaultPropertyValue;
        this.propertyConstraint = propertyConstraint;
        this.valuesMap = valuesMap;
        this.bundle = ResourcesManager.bundle;
        if (bundle != null) {
            try {
                propertyLabel = bundle.getString(propName);
                undoredoproperties = bundle.getString("undoredoproperties");
            } catch (Exception ex) {
            }
            if (propertyLabel == null || (propertyLabel != null && propertyLabel.equals(""))) {
                propertyLabel = this.propertyName;
            }
        }
        if (valuesMap != null && valuesMap.size() > 0 && bundle != null) {
            valuesLabelMap = new LinkedHashMap();
            String name = "", label = "";
            for (Iterator it = valuesMap.keySet().iterator(); it.hasNext(); ) {
                try {
                    name = (String) it.next();
                } catch (Exception ex) {
                    name = null;
                }
                if (name != null && !name.equals("")) {
                    try {
                        label = bundle.getString(name);
                    } catch (Exception ex) {
                        label = "";
                    }
                    if (label == null || (label != null && label.equals(""))) {
                        label = name;
                    }
                    valuesLabelMap.put(name, label);
                }
            }
        }
        if (propertyType != null && this.propertyName != null) {
            if (propertyType.equals("style")) {
                generalPropertyValue = getStylePropertyValue(this.propertyName);
            } else if (propertyType.equals("attribute")) {
                generalPropertyValue = getAttributeValue(this.propertyName);
            } else if (propertyType.equals("child")) {
                generalPropertyValue = getChildValue(this.propertyName);
            }
        }
    }
    public SVGProperties getProperties() {
        return properties;
    }
    public Collection getNodeList() {
        return nodeList;
    }
    public String getPropertyType() {
        return propertyType;
    }
    public String getPropertyName() {
        return propertyName;
    }
    public String getPropertyValueType() {
        return propertyValueType;
    }
    public String getDefaultPropertyValue() {
        return defaultPropertyValue;
    }
    public String getPropertyConstraint() {
        return propertyConstraint;
    }
    public String getGeneralPropertyValue() {
        return generalPropertyValue;
    }
    public String getPropertyValue(Node node) {
        String val = "";
        if (node != null) {
            try {
                val = (String) propertyValues.get(node);
            } catch (Exception ex) {
            }
        }
        return val;
    }
    public String getPropertyLabel() {
        return propertyLabel;
    }
    public LinkedHashMap getPropertyValuesMap() {
        return valuesMap;
    }
    public LinkedHashMap getPropertyValuesLabelMap() {
        return valuesLabelMap;
    }
    public void changePropertyValue(final String value) {
        String newValue = value;
        if ((newValue == null || newValue.equals("") && (propertyConstraint != null && propertyConstraint.equals("required")))) {
            newValue = defaultPropertyValue;
        }
        final SVGHandle handle = properties.getSVGEditor().getHandlesManager().getCurrentHandle();
        if (handle != null && propertyType != null) {
            final LinkedHashMap oldPropertyValues = new LinkedHashMap(propertyValues);
            LinkedHashMap values = new LinkedHashMap();
            Node node = null;
            for (Iterator it = propertyValues.keySet().iterator(); it.hasNext(); ) {
                try {
                    node = (Node) it.next();
                } catch (Exception ex) {
                    node = null;
                }
                if (node != null) {
                    values.put(node, newValue);
                }
            }
            final LinkedHashMap oldValues = new LinkedHashMap(propertyValues);
            final LinkedHashMap newValues = new LinkedHashMap(values);
            final LinkedHashMap fvalues = values;
            final Set<Element> selectedElements = new HashSet<Element>(handle.getSelection().getSelectedElements());
            final Set<Element> elements = new HashSet<Element>();
            for (Object object : values.keySet()) {
                if (object != null && object instanceof Element) {
                    elements.add((Element) object);
                }
            }
            final Runnable refreshRunnable = new Runnable() {
                public void run() {
                    AbstractShape shape = null;
                    for (Element el : elements) {
                        shape = ShapeToolkit.getShapeModule(el);
                        if (shape != null) {
                            shape.refresh(el);
                        }
                    }
                }
            };
            Runnable executeRunnable = new Runnable() {
                public void run() {
                    if (propertyType.equals("style")) {
                        setStylePropertyValue(propertyName, fvalues);
                    } else if (propertyType.equals("attribute")) {
                        setAttributeValue(propertyName, fvalues);
                    } else if (propertyType.equals("child")) {
                        setChildValue(propertyName, fvalues);
                    }
                    refreshRunnable.run();
                    handle.getScrollPane().getSVGCanvas().doRepaint(null);
                    handle.getSelection().refreshSelection(false);
                }
            };
            Runnable undoRunnable = new Runnable() {
                public void run() {
                    if (propertyType.equals("style")) {
                        setStylePropertyValue(propertyName, oldValues);
                    } else if (propertyType.equals("attribute")) {
                        setAttributeValue(propertyName, oldValues);
                    } else if (propertyType.equals("child")) {
                        setChildValue(propertyName, oldValues);
                    }
                    refreshRunnable.run();
                    handle.getSelection().handleSelection(selectedElements, false, true);
                }
            };
            Runnable redoRunnable = new Runnable() {
                public void run() {
                    if (propertyType.equals("style")) {
                        setStylePropertyValue(propertyName, newValues);
                    } else if (propertyType.equals("attribute")) {
                        setAttributeValue(propertyName, newValues);
                    } else if (propertyType.equals("child")) {
                        setChildValue(propertyName, newValues);
                    }
                    refreshRunnable.run();
                    handle.getSelection().handleSelection(selectedElements, false, true);
                }
            };
            UndoRedoAction action = new UndoRedoAction(undoredoproperties, executeRunnable, undoRunnable, redoRunnable, elements);
            UndoRedoActionList actionlist = new UndoRedoActionList(undoredoproperties, true);
            actionlist.add(action);
            handle.getUndoRedo().addActionList(actionlist, false);
        }
    }
    public String getStylePropertyValue(String name) {
        propertyValues.clear();
        if (nodeList != null) {
            String value = "";
            Element element = null;
            for (Iterator it = nodeList.iterator(); it.hasNext(); ) {
                try {
                    element = (Element) it.next();
                } catch (Exception ex) {
                    element = null;
                }
                value = properties.getSVGEditor().getSVGToolkit().getStyleProperty(element, name);
                if (value == null || (value != null && value.equals(""))) {
                    value = defaultPropertyValue;
                }
                if (element != null) {
                    propertyValues.put(element, value);
                }
            }
        }
        String returnedValue = "";
        if (nodeList.size() == 1) {
            try {
                returnedValue = (String) propertyValues.get(nodeList.getFirst());
            } catch (Exception ex) {
                returnedValue = "";
            }
        }
        return returnedValue;
    }
    public String getAttributeValue(String name) {
        propertyValues.clear();
        String value = "";
        Element element = null;
        for (Iterator it = nodeList.iterator(); it.hasNext(); ) {
            try {
                element = (Element) it.next();
            } catch (Exception ex) {
                element = null;
            }
            if (element != null && name != null && !name.equals("")) {
                value = element.getAttribute(name);
            }
            if (value == null || (value != null && value.equals(""))) {
                value = defaultPropertyValue;
            }
            if (element != null) {
                propertyValues.put(element, value);
            }
        }
        String returnedValue = "";
        if (nodeList.size() == 1) {
            try {
                returnedValue = (String) propertyValues.get(nodeList.getFirst());
            } catch (Exception ex) {
                returnedValue = "";
            }
        }
        return returnedValue;
    }
    public String getChildValue(String name) {
        propertyValues.clear();
        String value = "";
        Node node = null, cur = null;
        for (Iterator it = nodeList.iterator(); it.hasNext(); ) {
            try {
                node = (Node) it.next();
            } catch (Exception ex) {
                node = null;
            }
            if (node != null && name != null && !name.equals("")) {
                for (cur = node.getFirstChild(); cur != null; cur = cur.getNextSibling()) {
                    if (cur.getNodeName().equals(name)) {
                        value = cur.getNodeValue();
                        break;
                    }
                }
            }
            if (value == null || (value != null && value.equals(""))) {
                value = defaultPropertyValue;
            }
            if (node != null) {
                propertyValues.put(node, value);
            }
        }
        String returnedValue = "";
        if (nodeList.size() == 1) {
            try {
                returnedValue = (String) propertyValues.get(nodeList.getFirst());
            } catch (Exception ex) {
                returnedValue = "";
            }
        }
        return returnedValue;
    }
    public void setStylePropertyValue(String name, LinkedHashMap values) {
        if (nodeList != null) {
            Element element = null;
            String value = "", oldValue = "";
            for (Iterator it = nodeList.iterator(); it.hasNext(); ) {
                try {
                    element = (Element) it.next();
                    value = (String) values.get(element);
                    oldValue = (String) propertyValues.get(element);
                } catch (Exception ex) {
                    element = null;
                    value = null;
                    oldValue = "";
                }
                if (element != null && name != null && !name.equals("") && value != null && !value.equals(oldValue)) {
                    properties.getSVGEditor().getSVGToolkit().setStyleProperty(element, name, value);
                }
            }
        }
        generalPropertyValue = getStylePropertyValue(name);
    }
    public void setAttributeValue(String name, LinkedHashMap values) {
        if (nodeList != null) {
            Element element = null;
            String value = "", oldValue = "";
            for (Iterator it = nodeList.iterator(); it.hasNext(); ) {
                try {
                    element = (Element) it.next();
                    value = (String) values.get(element);
                    oldValue = (String) propertyValues.get(element);
                } catch (Exception ex) {
                    element = null;
                    value = null;
                    oldValue = "";
                }
                if (element != null && name != null && !name.equals("") && value != null && !value.equals(oldValue)) {
                    element.setAttribute(name, value);
                }
            }
        }
        generalPropertyValue = getAttributeValue(name);
    }
    public void setChildValue(String name, LinkedHashMap values) {
        if (nodeList != null) {
            Element element = null;
            String value = "", oldValue = "";
            for (Iterator it = nodeList.iterator(); it.hasNext(); ) {
                try {
                    element = (Element) it.next();
                    value = (String) values.get(element);
                    oldValue = (String) propertyValues.get(element);
                } catch (Exception ex) {
                    element = null;
                    value = null;
                    oldValue = "";
                }
                if (element != null && name != null && !name.equals("") && value != null && !value.equals(oldValue)) {
                    for (Node cur = element.getFirstChild(); cur != null; cur = cur.getNextSibling()) {
                        if (cur.getNodeName().equals(name)) {
                            cur.setNodeValue(value);
                            break;
                        }
                    }
                }
            }
        }
        generalPropertyValue = getChildValue(name);
    }
}
