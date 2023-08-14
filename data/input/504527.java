public class CaptureNodeAction extends BackgroundAction {
    public static final String ACTION_NAME = "captureNode";
    private Workspace mWorkspace;
    public CaptureNodeAction(Workspace workspace) {
        putValue(NAME, "Display View");
        putValue(SHORT_DESCRIPTION, "Display View");
        putValue(LONG_DESCRIPTION, "Display View");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_D,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        this.mWorkspace = workspace;
    }
    public void actionPerformed(ActionEvent e) {
        executeBackgroundTask(mWorkspace.showNodeCapture());
    }
}
