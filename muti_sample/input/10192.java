public final class ZoomInAction extends CallableSystemAction {
    public void performAction() {
        EditorTopComponent editor = EditorTopComponent.getActive();
        if (editor != null) {
            editor.zoomIn();
        }
    }
    public String getName() {
        return "Zoom in";
    }
    public ZoomInAction() {
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, Event.CTRL_MASK, false));
        putValue(Action.SHORT_DESCRIPTION, "Zoom in");
    }
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    @Override
    protected boolean asynchronous() {
        return false;
    }
    @Override
    protected String iconResource() {
        return "com/sun/hotspot/igv/view/images/zoomin.gif";
    }
}
