public final class ExtractAction extends CallableSystemAction {
    public void performAction() {
        EditorTopComponent editor = EditorTopComponent.getActive();
        if (editor != null) {
            editor.extract();
        }
    }
    public ExtractAction() {
        putValue(Action.SHORT_DESCRIPTION, "Extract current set of selected nodes");
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.CTRL_MASK, false));
    }
    public String getName() {
        return "Extract action";
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
        return "com/sun/hotspot/igv/view/images/extract.gif";
    }
}
