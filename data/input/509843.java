public class UiTextValueNode extends UiTextAttributeNode {
    public UiTextValueNode(TextValueDescriptor attributeDescriptor, UiElementNode uiParent) {
        super(attributeDescriptor, uiParent);
    }
    @Override
    public void updateValue(Node xml_attribute_node) {
        setCurrentValue(DEFAULT_VALUE);
        UiElementNode parent = getUiParent();
        if (parent != null) {
            Node xml_node = parent.getXmlNode();
            if (xml_node != null) {
                for (Node xml_child = xml_node.getFirstChild();
                    xml_child != null;
                    xml_child = xml_child.getNextSibling()) {
                    if (xml_child.getNodeType() == Node.TEXT_NODE) {
                        setCurrentValue(xml_child.getNodeValue());
                        break;
                    }
                }
            }
        }
        if (isValid() && !getTextWidgetValue().equals(getCurrentValue())) {
            try {
                setInInternalTextModification(true);
                setTextWidgetValue(getCurrentValue());
                setDirty(false);
            } finally {
                setInInternalTextModification(false);
            }
        }
    }
    @Override
    public void commit() {
        UiElementNode parent = getUiParent();
        if (parent != null && isValid() && isDirty()) {
            Node element = parent.prepareCommit();
            if (element != null) {
                String value = getTextWidgetValue();
                boolean updated = false;
                for (Node xml_child = element.getFirstChild();
                        xml_child != null;
                        xml_child = xml_child.getNextSibling()) {
                    if (xml_child.getNodeType() == Node.TEXT_NODE) {
                        xml_child.setNodeValue(value);
                        updated = true;
                        break;
                    }
                }
                if (!updated) {
                    Document doc = element.getOwnerDocument();
                    if (doc != null) {
                        Text text = doc.createTextNode(value);
                        element.appendChild(text);
                    }
                }
                setCurrentValue(value);
            }
        }
        setDirty(false);
    }
}
