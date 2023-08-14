public class BytecodeViewAction extends AbstractAction {
    public BytecodeViewAction() {
        super(NbBundle.getMessage(BytecodeViewAction.class, "CTL_BytecodeViewAction"));
    }
    public void actionPerformed(ActionEvent evt) {
        TopComponent win = BytecodeViewTopComponent.findInstance();
        win.open();
        win.requestActive();
    }
}
