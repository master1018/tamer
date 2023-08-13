public class AttributeDescriptorLabelProvider implements ILabelProvider {
    private final static AttributeDescriptorLabelProvider sThis =
        new AttributeDescriptorLabelProvider();
    public static ILabelProvider getProvider() {
        return sThis;
    }
    public Image getImage(Object element) {
        if (element instanceof UiAbstractTextAttributeNode) {
            UiAbstractTextAttributeNode node = (UiAbstractTextAttributeNode) element;
            if (node.getDescriptor().isDeprecated()) {
                String v = node.getCurrentValue();
                if (v != null && v.length() > 0) {
                    IconFactory factory = IconFactory.getInstance();
                    return factory.getIcon("warning"); 
                }                
            }
        }
        return null;
    }
    public String getText(Object element) {
        if (element instanceof UiAbstractTextAttributeNode) {
            return ((UiAbstractTextAttributeNode)element).getCurrentValue();
        }
        return null;
    }
    public void addListener(ILabelProviderListener listener) {
    }
    public void dispose() {
    }
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }
    public void removeListener(ILabelProviderListener listener) {
    }
}
