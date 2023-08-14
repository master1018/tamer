public final class ZoomOutAction extends CallableSystemAction {
    public void performAction() {
        EditorTopComponent editor = EditorTopComponent.getActive();
        if (editor != null) {
            editor.zoomOut();
        }
    }
    public ZoomOutAction() {
        putValue(Action.SHORT_DESCRIPTION, "Zoom out");
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, Event.CTRL_MASK, false));
    }
    public String getName() {
        return "Zoom out";
    }
    @Override
    protected void initialize() {
        super.initialize();
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
        return "com/sun/hotspot/igv/view/images/zoomout.gif";
    }
}
