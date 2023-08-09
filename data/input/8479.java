final class ControlFlowTopComponent extends TopComponent implements LookupListener {
    private static ControlFlowTopComponent instance;
    private Lookup.Result result = null;
    private static final String PREFERRED_ID = "ControlFlowTopComponent";
    private ControlFlowScene scene;
    private ControlFlowTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(ControlFlowTopComponent.class, "CTL_ControlFlowTopComponent"));
        setToolTipText(NbBundle.getMessage(ControlFlowTopComponent.class, "HINT_ControlFlowTopComponent"));
        scene = new ControlFlowScene();
        this.setLayout(new BorderLayout());
        this.associateLookup(scene.getLookup());
        JScrollPane panel = new JScrollPane(scene.createView());
        this.add(panel, BorderLayout.CENTER);
    }
    @Override
    public void requestFocus() {
        super.requestFocus();
        scene.getView().requestFocus();
    }
    @Override
    public boolean requestFocusInWindow() {
        super.requestFocusInWindow();
        return scene.getView().requestFocusInWindow();
    }
    private void initComponents() {
        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 300, Short.MAX_VALUE)
        );
    }
    public static synchronized ControlFlowTopComponent getDefault() {
        if (instance == null) {
            instance = new ControlFlowTopComponent();
        }
        return instance;
    }
    public static synchronized ControlFlowTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING, "Cannot find ControlFlow component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof ControlFlowTopComponent) {
            return (ControlFlowTopComponent) win;
        }
        ErrorManager.getDefault().log(ErrorManager.WARNING, "There seem to be multiple components with the '" + PREFERRED_ID + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }
    @Override
    public void componentOpened() {
        Lookup.Template tpl = new Lookup.Template(Object.class);
        result = Utilities.actionsGlobalContext().lookup(tpl);
        result.addLookupListener(this);
    }
    @Override
    public void componentClosed() {
        result.removeLookupListener(this);
        result = null;
    }
    public void resultChanged(LookupEvent lookupEvent) {
        final InputGraphProvider p = Lookup.getDefault().lookup(InputGraphProvider.class);
        if (p != null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
            InputGraph g = p.getGraph();
            if (g != null) {
                scene.setGraph(g);
            }
        }
            });
        }
    }
    @Override
    public Object writeReplace() {
        return new ResolvableHelper();
    }
    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }
    @Override
    public void requestActive() {
        scene.getView().requestFocusInWindow();
        super.requestActive();
    }
    final static class ResolvableHelper implements Serializable {
        private static final long serialVersionUID = 1L;
        public Object readResolve() {
            return ControlFlowTopComponent.getDefault();
        }
    }
}
