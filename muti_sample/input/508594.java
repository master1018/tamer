public class UiModelTreeLabelProvider implements ILabelProvider {
    public UiModelTreeLabelProvider() {
    }
    public Image getImage(Object element) {
        ElementDescriptor desc = null;
        if (element instanceof ElementDescriptor) {
            Image img = ((ElementDescriptor) element).getIcon();
            if (img != null) {
                return img;
            }
        } else if (element instanceof UiElementNode) {
            UiElementNode node = (UiElementNode) element;
            desc = node.getDescriptor();
            if (desc != null) {
                Image img = desc.getIcon();
                if (img != null) {
                    if (node.hasError()) {
                        return new ErrorImageComposite(img).createImage();
                    } else {
                        return img;
                    }
                }
            }
        }
        return AdtPlugin.getAndroidLogo();
    }
    public String getText(Object element) {
        if (element instanceof ElementDescriptor) {
            ElementDescriptor desc = (ElementDescriptor) element;
            return desc.getUiName();
        } else if (element instanceof UiElementNode) {
            UiElementNode node = (UiElementNode) element;
            return node.getShortDescription();
        }
        return element.toString();
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
