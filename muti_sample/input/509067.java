public abstract class SelectionDependentPanel extends Panel {
    private IDevice mCurrentDevice = null;
    private Client mCurrentClient = null;
    protected final IDevice getCurrentDevice() {
        return mCurrentDevice;
    }
    protected final Client getCurrentClient() {
        return mCurrentClient;
    }
    public final void deviceSelected(IDevice selectedDevice) {
        if (selectedDevice != mCurrentDevice) {
            mCurrentDevice = selectedDevice;
            deviceSelected();
        }
    }
    public final void clientSelected(Client selectedClient) {
        if (selectedClient != mCurrentClient) {
            mCurrentClient = selectedClient;
            clientSelected();
        }
    }
    public abstract void deviceSelected();
    public abstract void clientSelected();
}
