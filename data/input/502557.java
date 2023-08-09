public class ExitAction extends AbstractAction {
    public static final String ACTION_NAME = "exit";
    private JFrame frame;
    public ExitAction(JFrame frame) {
        putValue(NAME, "Quit");
        putValue(SHORT_DESCRIPTION, "Quit");
        putValue(LONG_DESCRIPTION, "Quit");
        putValue(MNEMONIC_KEY, KeyEvent.VK_Q);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        this.frame = frame;
    }
    public void actionPerformed(ActionEvent e) {
        frame.dispose();
        System.exit(0);
    }
}
