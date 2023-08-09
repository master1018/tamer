public class CaptureLayersAction extends BackgroundAction {
    public static final String ACTION_NAME = "captureLayers";
    private Workspace mWorkspace;
    public CaptureLayersAction(Workspace workspace) {
        putValue(NAME, "Capture PSD");
        putValue(SHORT_DESCRIPTION, "Capture PSD");
        putValue(LONG_DESCRIPTION, "Capture current window into a Photoshop PSD file");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        this.mWorkspace = workspace;
    }
    public void actionPerformed(ActionEvent e) {
        executeBackgroundTask(mWorkspace.captureLayers());
    }
}
