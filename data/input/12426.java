public class TreeExpansionEvent extends EventObject
{
    protected TreePath              path;
    public TreeExpansionEvent(Object source, TreePath path) {
        super(source);
        this.path = path;
    }
    public TreePath getPath() { return path; }
}
