public class test {
    public ShowInContext getShowInContext() {
        return new ShowInContext(getEditorInput(), getGraphicalViewer().getSelection());
    }
}
