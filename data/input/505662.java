public class RefreshWindowsAction extends BackgroundAction {
    public static final String ACTION_NAME = "refreshWindows";
    private Workspace mWorkspace;
    public RefreshWindowsAction(Workspace workspace) {
        putValue(NAME, "Refresh Windows");
        putValue(SHORT_DESCRIPTION, "Refresh");
        putValue(LONG_DESCRIPTION, "Refresh Windows");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
        this.mWorkspace = workspace;
    }
    public void actionPerformed(ActionEvent e) {
        executeBackgroundTask(mWorkspace.loadWindows());
    }
}
