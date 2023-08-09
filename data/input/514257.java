public class ShowDevicesAction extends AbstractAction {
    public static final String ACTION_NAME = "showDevices";
    private Workspace mWorkspace;
    public ShowDevicesAction(Workspace workspace) {
        putValue(NAME, "Devices");
        putValue(SHORT_DESCRIPTION, "Devices");
        putValue(LONG_DESCRIPTION, "Show Devices");
        putValue(MNEMONIC_KEY, KeyEvent.VK_D);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_D,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        this.mWorkspace = workspace;
    }
    public void actionPerformed(ActionEvent e) {
        mWorkspace.showDevicesSelector();
    }
}
