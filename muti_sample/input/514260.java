public class StopServerAction extends BackgroundAction {
    public static final String ACTION_NAME = "stopServer";
    private Workspace mWorkspace;
    public StopServerAction(Workspace workspace) {
        putValue(NAME, "Stop Server");
        putValue(SHORT_DESCRIPTION, "Stop");
        putValue(LONG_DESCRIPTION, "Stop Server");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
        this.mWorkspace = workspace;
    }
    public void actionPerformed(ActionEvent e) {
        executeBackgroundTask(mWorkspace.stopServer());
    }
}
