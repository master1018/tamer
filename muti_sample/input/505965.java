public abstract class UiAbstractTextAttributeNode extends UiAttributeNode
    implements IUiSettableAttributeNode {
    protected static final String DEFAULT_VALUE = "";  
    private boolean mInternalTextModification;
    private String mCurrentValue = DEFAULT_VALUE;
    public UiAbstractTextAttributeNode(AttributeDescriptor attributeDescriptor,
            UiElementNode uiParent) {
        super(attributeDescriptor, uiParent);
    }
    @Override
    public final String getCurrentValue() {
        return mCurrentValue;
    }
    public final void setCurrentValue(String value) {
        mCurrentValue = value;
    }
    public abstract boolean isValid();
    public abstract String getTextWidgetValue();
    public abstract void setTextWidgetValue(String value);
    @Override
    public void updateValue(Node xml_attribute_node) {
        mCurrentValue = DEFAULT_VALUE;
        if (xml_attribute_node != null) {
            mCurrentValue = xml_attribute_node.getNodeValue();
        }
        if (isValid() && !getTextWidgetValue().equals(mCurrentValue)) {
            try {
                mInternalTextModification = true;
                setTextWidgetValue(mCurrentValue);
                setDirty(false);
            } finally {
                mInternalTextModification = false;
            }
        }
    }
    @Override
    public void commit() {
        UiElementNode parent = getUiParent();
        if (parent != null && isValid() && isDirty()) {
            String value = getTextWidgetValue();
            if (parent.commitAttributeToXml(this, value)) {
                mCurrentValue = value;
                setDirty(false);
            }
        }
    }
    protected final boolean isInInternalTextModification() {
        return mInternalTextModification;
    }
    protected final void setInInternalTextModification(boolean internalTextModification) {
        mInternalTextModification = internalTextModification;
    }
}
