public class OverviewAction extends AbstractAction {
    private boolean state;
    public static final String STATE = "state";
    public OverviewAction() {
        putValue(AbstractAction.SMALL_ICON, new ImageIcon(org.openide.util.Utilities.loadImage(iconResource())));
        putValue(Action.SHORT_DESCRIPTION, "Show satellite view of whole graph");
        setState(false);
    }
    public void actionPerformed(ActionEvent ev) {
        setState(!state);
    }
    public void setState(boolean b) {
        this.putValue(STATE, b);
        this.state = b;
    }
    protected String iconResource() {
        return "com/sun/hotspot/igv/view/images/overview.gif";
    }
}
