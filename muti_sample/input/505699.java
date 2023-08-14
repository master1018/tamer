class UiModelTreeContentProvider implements ITreeContentProvider {
    private ElementDescriptor[] mDescriptorFilters;
    private final UiElementNode mUiRootNode;
    public UiModelTreeContentProvider(UiElementNode uiRootNode,
            ElementDescriptor[] descriptorFilters) {
        mUiRootNode = uiRootNode;
        mDescriptorFilters = descriptorFilters;
    }
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof UiElementNode) {
            UiElementNode node = (UiElementNode) parentElement;
            return node.getUiChildren().toArray();
        }
        return null;
    }
    public Object getParent(Object element) {
        if (element instanceof UiElementNode) {
            UiElementNode node = (UiElementNode) element;
            return node.getUiParent();
        }
        return null;
    }
    public boolean hasChildren(Object element) {
        if (element instanceof UiElementNode) {
            UiElementNode node = (UiElementNode) element;
            return node.getUiChildren().size() > 0;
        }
        return false;
    }
    public Object[] getElements(Object inputElement) {
        ArrayList<UiElementNode> roots = new ArrayList<UiElementNode>();
        if (mUiRootNode != null) {
            for (UiElementNode ui_node : mUiRootNode.getUiChildren()) {
                if (mDescriptorFilters == null || mDescriptorFilters.length == 0) {
                    roots.add(ui_node);
                } else {
                    for (ElementDescriptor filter : mDescriptorFilters) {
                        if (ui_node.getDescriptor() == filter) {
                            roots.add(ui_node);
                        }
                    }
                }
            }
        }
        return roots.toArray();
    }
    public void dispose() {
    }
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }
}
