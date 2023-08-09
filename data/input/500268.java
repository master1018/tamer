public class RequestLayoutAction extends BackgroundAction {
    public static final String ACTION_NAME = "requestLayout";
    private Workspace mWorkspace;
    public RequestLayoutAction(Workspace workspace) {
        putValue(NAME, "Request Layout");
        putValue(SHORT_DESCRIPTION, "Request Layout");
        putValue(LONG_DESCRIPTION, "Request Layout");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        this.mWorkspace = workspace;
    }
    public void actionPerformed(ActionEvent e) {
        executeBackgroundTask(mWorkspace.requestLayout());
    }
}
