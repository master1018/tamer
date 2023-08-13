public class OpenAction extends BackgroundAction {
    public static final String ACTION_NAME = "open";
    private MainFrame frame;
    public OpenAction(MainFrame frame) {
        this.frame = frame;
        putValue(NAME, "Open 9-patch...");
        putValue(SHORT_DESCRIPTION, "Open...");
        putValue(LONG_DESCRIPTION, "Open 9-patch...");
        putValue(MNEMONIC_KEY, KeyEvent.VK_O);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    }
    public void actionPerformed(ActionEvent e) {
        executeBackgroundTask(frame.open(null));
    }
}
