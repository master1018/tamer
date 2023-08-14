public abstract class SelectionDependentViewPart extends ViewPart implements ISelectionListener {
    private SelectionDependentPanel mPanel;
    protected final void setSelectionDependentPanel(SelectionDependentPanel panel) {
        mPanel = panel;
        DdmsPlugin.getDefault().addSelectionListener(this);
    }
    @Override
    public void dispose() {
        DdmsPlugin.getDefault().removeSelectionListener(this);
        super.dispose();
    }
    public final void selectionChanged(Client selectedClient) {
        mPanel.clientSelected(selectedClient);
    }
    public final void selectionChanged(IDevice selectedDevice) {
        mPanel.deviceSelected(selectedDevice);
    }
}
