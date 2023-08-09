public class StartServerAction extends BackgroundAction {
    public static final String ACTION_NAME = "startServer";
    private Workspace mWorkspace;
    public StartServerAction(Workspace workspace) {
        putValue(NAME, "Start Server");
        putValue(SHORT_DESCRIPTION, "Start");
        putValue(LONG_DESCRIPTION, "Start Server");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        this.mWorkspace = workspace;
    }
    public void actionPerformed(ActionEvent e) {
        executeBackgroundTask(mWorkspace.startServer());
    }
}
