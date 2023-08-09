public final class OutlineTopComponent extends TopComponent implements ExplorerManager.Provider, LookupListener {
    public static OutlineTopComponent instance;
    public static final String PREFERRED_ID = "OutlineTopComponent";
    private ExplorerManager manager;
    private GraphDocument document;
    private FolderNode root;
    private GroupOrganizer organizer;
    private OutlineTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(OutlineTopComponent.class, "CTL_OutlineTopComponent"));
        setToolTipText(NbBundle.getMessage(OutlineTopComponent.class, "HINT_OutlineTopComponent"));
        document = new GraphDocument();
        initListView();
        initToolbar();
        initReceivers();
    }
    private void initListView() {
        manager = new ExplorerManager();
        organizer = new StandardGroupOrganizer();
        root = new FolderNode("", organizer, new ArrayList<String>(), document.getGroups());
        manager.setRootContext(root);
        ((BeanTreeView) this.jScrollPane1).setRootVisible(false);
        document.getChangedEvent().addListener(new ChangedListener<GraphDocument>() {
            public void changed(GraphDocument document) {
                updateStructure();
            }
        });
        associateLookup(ExplorerUtils.createLookup(manager, getActionMap()));
    }
    private void initToolbar() {
        Toolbar toolbar = new Toolbar();
        Border b = (Border) UIManager.get("Nb.Editor.Toolbar.border"); 
        toolbar.setBorder(b);
        this.add(toolbar, BorderLayout.NORTH);
        toolbar.add(ImportAction.get(ImportAction.class));
        toolbar.add(((NodeAction) RemoveAction.get(RemoveAction.class)).createContextAwareInstance(this.getLookup()));
        toolbar.add(RemoveAllAction.get(RemoveAllAction.class));
        toolbar.add(((NodeAction) SaveAsAction.get(SaveAsAction.class)).createContextAwareInstance(this.getLookup()));
        toolbar.add(SaveAllAction.get(SaveAllAction.class));
        toolbar.add(StructuredViewAction.get(StructuredViewAction.class).getToolbarPresenter());
        for (Toolbar tb : ToolbarPool.getDefault().getToolbars()) {
            tb.setVisible(false);
        }
        initOrganizers();
    }
    public void setOrganizer(GroupOrganizer organizer) {
        this.organizer = organizer;
        updateStructure();
    }
    private void initOrganizers() {
    }
    private void initReceivers() {
        final GroupCallback callback = new GroupCallback() {
            public void started(Group g) {
                getDocument().addGroup(g);
            }
        };
        Collection<? extends GroupReceiver> receivers = Lookup.getDefault().lookupAll(GroupReceiver.class);
        if (receivers.size() > 0) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            for (GroupReceiver r : receivers) {
                Component c = r.init(callback);
                panel.add(c);
            }
            jPanel2.add(panel, BorderLayout.PAGE_START);
        }
    }
    private void updateStructure() {
        root.init("", organizer, new ArrayList<String>(), document.getGroups());
    }
    public void clear() {
        document.clear();
    }
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    public GraphDocument getDocument() {
        return document;
    }
    public static synchronized OutlineTopComponent getDefault() {
        if (instance == null) {
            instance = new OutlineTopComponent();
        }
        return instance;
    }
    public static synchronized OutlineTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING, "Cannot find Outline component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof OutlineTopComponent) {
            return (OutlineTopComponent) win;
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
        this.requestActive();
    }
    @Override
    public void componentClosed() {
    }
    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }
    public void resultChanged(LookupEvent lookupEvent) {
    }
    @Override
    public void readExternal(ObjectInput objectInput) throws IOException, ClassNotFoundException {
        super.readExternal(objectInput);
        ((BeanTreeView) this.jScrollPane1).setRootVisible(false);
    }
    @Override
    public void writeExternal(ObjectOutput objectOutput) throws IOException {
        super.writeExternal(objectOutput);
    }
    static final class ResolvableHelper implements Serializable {
        private static final long serialVersionUID = 1L;
        public Object readResolve() {
            return OutlineTopComponent.getDefault();
        }
    }
    private void initComponents() {
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new BeanTreeView();
        setLayout(new java.awt.BorderLayout());
        jPanel2.setLayout(new java.awt.BorderLayout());
        jPanel2.add(jScrollPane1, java.awt.BorderLayout.CENTER);
        add(jPanel2, java.awt.BorderLayout.CENTER);
    }
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
}
