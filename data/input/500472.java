public class WayPointContentProvider implements IStructuredContentProvider {
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof WayPoint[]) {
            return (WayPoint[])inputElement;
        }
        return new Object[0];
    }
    public void dispose() {
    }
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }
}
