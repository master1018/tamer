public class OutlineAction extends AbstractAction {
    public OutlineAction() {
        super(NbBundle.getMessage(OutlineAction.class, "CTL_OutlineAction"));
    }
    public void actionPerformed(ActionEvent evt) {
        TopComponent win = OutlineTopComponent.findInstance();
        win.open();
        win.requestActive();
    }
}
