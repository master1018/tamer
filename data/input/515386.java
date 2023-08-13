public class InvalidateAction extends BackgroundAction {
    public static final String ACTION_NAME = "invalidate";
    private Workspace mWorkspace;
    public InvalidateAction(Workspace workspace) {
        putValue(NAME, "Invalidate");
        putValue(SHORT_DESCRIPTION, "Invalidate");
        putValue(LONG_DESCRIPTION, "Invalidate");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        this.mWorkspace = workspace;
    }
    public void actionPerformed(ActionEvent e) {
        executeBackgroundTask(mWorkspace.invalidateView());
    }
}
