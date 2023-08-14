final class BytecodeViewTopComponent extends TopComponent implements ExplorerManager.Provider, LookupListener {
    private static BytecodeViewTopComponent instance;
    private static final String PREFERRED_ID = "BytecodeViewTopComponent";
    private ExplorerManager manager;
    private BeanTreeView treeView;
    private Lookup.Result result = null;
    private MethodNode rootNode;
    private BytecodeViewTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(BytecodeViewTopComponent.class, "CTL_BytecodeViewTopComponent"));
        setToolTipText(NbBundle.getMessage(BytecodeViewTopComponent.class, "HINT_BytecodeViewTopComponent"));
        manager = new ExplorerManager();
        rootNode = new MethodNode(null, null, "");
        manager.setRootContext(rootNode);
        setLayout(new BorderLayout());
        treeView = new BeanTreeView();
        treeView.setRootVisible(false);
        this.add(BorderLayout.CENTER, treeView);
        associateLookup(ExplorerUtils.createLookup(manager, getActionMap()));
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
    public static synchronized BytecodeViewTopComponent getDefault() {
        if (instance == null) {
            instance = new BytecodeViewTopComponent();
        }
        return instance;
    }
    public static synchronized BytecodeViewTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING, "Cannot find BytecodeView component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof BytecodeViewTopComponent) {
            return (BytecodeViewTopComponent) win;
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
    @Override
    public Object writeReplace() {
        return new ResolvableHelper();
    }
    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    public void resultChanged(LookupEvent lookupEvent) {
        final InputGraphProvider p = Lookup.getDefault().lookup(InputGraphProvider.class);
        if (p != null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
            InputGraph graph = p.getGraph();
            if (graph != null) {
                Group g = graph.getGroup();
                rootNode.update(graph, g.getMethod());
            }
        }
            });
        }
    }
    final static class ResolvableHelper implements Serializable {
        private static final long serialVersionUID = 1L;
        public Object readResolve() {
            return BytecodeViewTopComponent.getDefault();
        }
    }
}
