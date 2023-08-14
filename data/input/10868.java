public class FilterAction extends AbstractAction {
    public FilterAction() {
        super(NbBundle.getMessage(FilterAction.class, "CTL_FilterAction"));
    }
    public void actionPerformed(ActionEvent evt) {
        TopComponent win = FilterTopComponent.findInstance();
        win.open();
        win.requestActive();
    }
}
