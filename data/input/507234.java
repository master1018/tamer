class LocalSdkAdapter  {
    private final UpdaterData mUpdaterData;
    public LocalSdkAdapter(UpdaterData updaterData) {
        mUpdaterData = updaterData;
    }
    public ILabelProvider getLabelProvider() {
        return new ViewerLabelProvider();
    }
    public IContentProvider getContentProvider() {
        return new TableContentProvider();
    }
    public class ViewerLabelProvider extends LabelProvider {
        @Override
        public Image getImage(Object element) {
            ImageFactory imgFactory = mUpdaterData.getImageFactory();
            if (imgFactory != null) {
                return imgFactory.getImageForObject(element);
            }
            return super.getImage(element);
        }
        @Override
        public String getText(Object element) {
            if (element instanceof IDescription) {
                return ((IDescription) element).getShortDescription();
            }
            return super.getText(element);
        }
    }
    private class TableContentProvider implements IStructuredContentProvider {
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            assert newInput == LocalSdkAdapter.this;
        }
        public Object[] getElements(Object inputElement) {
            if (inputElement == LocalSdkAdapter.this) {
                Package[] packages = mUpdaterData.getInstalledPackage();
                if (packages != null) {
                    return packages;
                }
            }
            return new Object[0];
        }
    }
}
