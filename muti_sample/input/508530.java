public final class ReferenceAttributeDescriptor extends TextAttributeDescriptor {
    private ResourceType mResourceType;
    public ReferenceAttributeDescriptor(String xmlLocalName, String uiName, String nsUri,
            String tooltip) {
        super(xmlLocalName, uiName, nsUri, tooltip);
    }
    public ReferenceAttributeDescriptor(ResourceType resourceType,
            String xmlLocalName, String uiName, String nsUri,
            String tooltip) {
        super(xmlLocalName, uiName, nsUri, tooltip);
        mResourceType = resourceType;
    }
    public ResourceType getResourceType() {
        return mResourceType;
    }
    @Override
    public UiAttributeNode createUiNode(UiElementNode uiParent) {
        return new UiResourceAttributeNode(mResourceType, this, uiParent);
    }
    @Override
    public CellEditor createPropertyEditor(Composite parent) {
        return new ResourceValueCellEditor(parent);
    }
}
