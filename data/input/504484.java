public abstract class Panel {
    public final Control createPanel(Composite parent) {
        Control panelControl = createControl(parent);
        postCreation();
        return panelControl;
    }
    protected abstract void postCreation();
    protected abstract Control createControl(Composite parent);
    public abstract void setFocus();
}
