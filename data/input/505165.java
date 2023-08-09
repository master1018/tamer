public class LoadGraphAction extends BackgroundAction {
    public static final String ACTION_NAME = "loadGraph";
    private Workspace mWorkspace;
    public LoadGraphAction(Workspace workspace) {
        putValue(NAME, "Load View Hierarchy");
        putValue(SHORT_DESCRIPTION, "Load");
        putValue(LONG_DESCRIPTION, "Load View Hierarchy");
        putValue(MNEMONIC_KEY, KeyEvent.VK_L);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        this.mWorkspace = workspace;
    }
    public void actionPerformed(ActionEvent e) {
        executeBackgroundTask(mWorkspace.loadGraph());
    }
}
