public abstract class ClientDisplayPanel extends SelectionDependentPanel
        implements IClientChangeListener {
    @Override
    protected void postCreation() {
        AndroidDebugBridge.addClientChangeListener(this);
    }
    public void dispose() {
        AndroidDebugBridge.removeClientChangeListener(this);
    }
}
