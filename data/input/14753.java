public class EnableBlockLayoutAction extends AbstractAction {
    private boolean state;
    public static final String STATE = "state";
    public EnableBlockLayoutAction() {
        state = true;
        putValue(AbstractAction.SMALL_ICON, new ImageIcon(org.openide.util.Utilities.loadImage(iconResource())));
        putValue(STATE, true);
        putValue(Action.SHORT_DESCRIPTION, "Cluster nodes into blocks");
    }
    public void actionPerformed(ActionEvent ev) {
        this.state = !state;
        this.putValue(STATE, state);
    }
    protected String iconResource() {
        return "com/sun/hotspot/igv/view/images/blocks.gif";
    }
}
