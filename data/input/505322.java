public class TrackContentProvider implements IStructuredContentProvider {
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof Track[]) {
            return (Track[])inputElement;
        }
        return new Object[0];
    }
    public void dispose() {
    }
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }
}
