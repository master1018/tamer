public final class PrevDiagramAction extends ContextAction<DiagramViewModel> implements ChangedListener<DiagramViewModel> {
    private DiagramViewModel model;
    public PrevDiagramAction() {
        this(Utilities.actionsGlobalContext());
    }
    public PrevDiagramAction(Lookup lookup) {
        putValue(Action.SHORT_DESCRIPTION, "Show previous graph of current group");
        putValue(Action.SMALL_ICON, new ImageIcon(Utilities.loadImage("com/sun/hotspot/igv/view/images/prev_diagram.png")));
    }
    public String getName() {
        return NbBundle.getMessage(PrevDiagramAction.class, "CTL_PrevDiagramAction");
    }
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    @Override
    public Class<DiagramViewModel> contextClass() {
        return DiagramViewModel.class;
    }
    @Override
    public void performAction(DiagramViewModel model) {
        int fp = model.getFirstPosition();
        int sp = model.getSecondPosition();
        if (fp != 0) {
            int nfp = fp - 1;
            int nsp = sp - 1;
            model.setPositions(nfp, nsp);
        }
    }
    @Override
    public void update(DiagramViewModel model) {
        super.update(model);
        if (this.model != model) {
            if (this.model != null) {
                this.model.getDiagramChangedEvent().removeListener(this);
            }
            this.model = model;
            if (this.model != null) {
                this.model.getDiagramChangedEvent().addListener(this);
            }
        }
    }
    @Override
    public boolean isEnabled(DiagramViewModel model) {
        return model.getFirstPosition() != 0;
    }
    public Action createContextAwareInstance(Lookup arg0) {
        return new PrevDiagramAction(arg0);
    }
    public void changed(DiagramViewModel source) {
        update(source);
    }
}
