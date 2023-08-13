public class ResourceLabelProvider implements ILabelProvider, ITableLabelProvider {
    private Image mWarningImage;
    public ResourceLabelProvider() {
        mWarningImage = PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
                ISharedImages.IMG_OBJS_WARN_TSK).createImage();
    }
    public Image getImage(Object element) {
        return null;
    }
    public String getText(Object element) {
        return getColumnText(element, 0);
    }
    public void addListener(ILabelProviderListener listener) {
    }
    public void dispose() {
        mWarningImage.dispose();
    }
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }
    public void removeListener(ILabelProviderListener listener) {
    }
    public Image getColumnImage(Object element, int columnIndex) {
        if (columnIndex == 1) {
            if (element instanceof ConfigurableResourceItem) {
                ConfigurableResourceItem item = (ConfigurableResourceItem)element;
                if (item.hasDefault() == false) {
                    return mWarningImage;
                }
            }
        }
        return null;
    }
    public String getColumnText(Object element, int columnIndex) {
        switch (columnIndex) {
            case 0:
                if (element instanceof ResourceType) {
                    return ((ResourceType)element).getDisplayName();
                } else if (element instanceof ResourceItem) {
                    return ((ResourceItem)element).getName();
                } else if (element instanceof ResourceFile) {
                    return ((ResourceFile)element).getFolder().getConfiguration().toDisplayString();
                }
                break;
            case 1:
                if (element instanceof ConfigurableResourceItem) {
                    ConfigurableResourceItem item = (ConfigurableResourceItem)element;
                    int count = item.getAlternateCount();
                    if (count > 0) {
                        if (item.hasDefault()) {
                            count++;
                        }
                        return String.format("%1$d version(s)", count);
                    }
                } else if (element instanceof IIdResourceItem) {
                    IIdResourceItem idResource = (IIdResourceItem)element;
                    if (idResource.isDeclaredInline()) {
                        return "Declared inline";
                    }
                }
                return null;
        }
        return null;
    }
}
