public class SaveAction extends BackgroundAction {
    public static final String ACTION_NAME = "save";
    private MainFrame frame;
    public SaveAction(MainFrame frame) {
        this.frame = frame;
        putValue(NAME, "Save 9-patch...");
        putValue(SHORT_DESCRIPTION, "Save...");
        putValue(LONG_DESCRIPTION, "Save 9-patch...");
        putValue(MNEMONIC_KEY, KeyEvent.VK_S);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    }
    public void actionPerformed(ActionEvent e) {
        executeBackgroundTask(frame.save());
    }
}
