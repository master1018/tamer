public class ResourceContentProvider implements ITreeContentProvider {
    private IResourceRepository mResources;
    private boolean mFullLevels;
    public ResourceContentProvider(boolean fullLevels) {
        mFullLevels = fullLevels;
    }
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof ResourceType) {
            return mResources.getResources((ResourceType)parentElement);
        } else if (mFullLevels && parentElement instanceof ConfigurableResourceItem) {
            return ((ConfigurableResourceItem)parentElement).getSourceFileArray();
        }
        return null;
    }
    public Object getParent(Object element) {
        return null;
    }
    public boolean hasChildren(Object element) {
        if (element instanceof ResourceType) {
            return mResources.hasResources((ResourceType)element);
        } else if (mFullLevels && element instanceof ConfigurableResourceItem) {
            return ((ConfigurableResourceItem)element).hasAlternates();
        }
        return false;
    }
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof IResourceRepository) {
            if ((IResourceRepository)inputElement == mResources) {
                return mResources.getAvailableResourceTypes();
            }
        }
        return new Object[0];
    }
    public void dispose() {
    }
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (newInput instanceof IResourceRepository) {
             mResources = (IResourceRepository)newInput;
        }
    }
}
