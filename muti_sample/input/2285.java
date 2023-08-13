public class ControlFlowAction extends AbstractAction {
    public ControlFlowAction() {
        super(NbBundle.getMessage(ControlFlowAction.class, "CTL_ControlFlowAction"));
    }
    public void actionPerformed(ActionEvent evt) {
        TopComponent win = ControlFlowTopComponent.findInstance();
        win.open();
        win.requestActive();
    }
}
