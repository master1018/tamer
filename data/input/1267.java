public class MBeansTab extends Tab implements
        NotificationListener, PropertyChangeListener,
        TreeSelectionListener, TreeWillExpandListener {
    private XTree tree;
    private XSheet sheet;
    private XDataViewer viewer;
    public static String getTabName() {
        return Resources.getText("MBeans");
    }
    public MBeansTab(final VMPanel vmPanel) {
        super(vmPanel, getTabName());
        addPropertyChangeListener(this);
        setupTab();
    }
    public XDataViewer getDataViewer() {
        return viewer;
    }
    public XTree getTree() {
        return tree;
    }
    public XSheet getSheet() {
        return sheet;
    }
    @Override
    public void dispose() {
        super.dispose();
        sheet.dispose();
    }
    public int getUpdateInterval() {
        return vmPanel.getUpdateInterval();
    }
    private void buildMBeanServerView() {
        new SwingWorker<Set<ObjectName>, Void>() {
            @Override
            public Set<ObjectName> doInBackground() {
                try {
                    getMBeanServerConnection().addNotificationListener(
                            MBeanServerDelegate.DELEGATE_NAME,
                            MBeansTab.this,
                            null,
                            null);
                } catch (InstanceNotFoundException e) {
                    if (JConsole.isDebug()) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    if (JConsole.isDebug()) {
                        e.printStackTrace();
                    }
                    vmPanel.getProxyClient().markAsDead();
                    return null;
                }
                Set<ObjectName> mbeans = null;
                try {
                    mbeans = getMBeanServerConnection().queryNames(null, null);
                } catch (IOException e) {
                    if (JConsole.isDebug()) {
                        e.printStackTrace();
                    }
                    vmPanel.getProxyClient().markAsDead();
                    return null;
                }
                return mbeans;
            }
            @Override
            protected void done() {
                try {
                    Set<ObjectName> mbeans = get();
                    tree.setVisible(false);
                    tree.removeAll();
                    tree.addMBeansToView(mbeans);
                    tree.setVisible(true);
                } catch (Exception e) {
                    Throwable t = Utils.getActualException(e);
                    if (JConsole.isDebug()) {
                        System.err.println("Problem at MBean tree construction");
                        t.printStackTrace();
                    }
                }
            }
        }.execute();
    }
    public MBeanServerConnection getMBeanServerConnection() {
        return vmPanel.getProxyClient().getMBeanServerConnection();
    }
    public SnapshotMBeanServerConnection getSnapshotMBeanServerConnection() {
        return vmPanel.getProxyClient().getSnapshotMBeanServerConnection();
    }
    @Override
    public void update() {
        try {
            getMBeanServerConnection().getDefaultDomain();
        } catch (IOException ex) {
            vmPanel.getProxyClient().markAsDead();
        }
    }
    private void setupTab() {
        setLayout(new BorderLayout());
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setDividerLocation(160);
        mainSplit.setBorder(BorderFactory.createEmptyBorder());
        tree = new XTree(this);
        tree.setCellRenderer(new XTreeRenderer());
        tree.getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(this);
        tree.addTreeWillExpandListener(this);
        tree.addMouseListener(ml);
        JScrollPane theScrollPane = new JScrollPane(
                tree,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JPanel treePanel = new JPanel(new BorderLayout());
        treePanel.add(theScrollPane, BorderLayout.CENTER);
        mainSplit.add(treePanel, JSplitPane.LEFT, 0);
        viewer = new XDataViewer(this);
        sheet = new XSheet(this);
        mainSplit.add(sheet, JSplitPane.RIGHT, 0);
        add(mainSplit);
    }
    public void handleNotification(
            final Notification notification, Object handback) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                if (notification instanceof MBeanServerNotification) {
                    ObjectName mbean =
                            ((MBeanServerNotification) notification).getMBeanName();
                    if (notification.getType().equals(
                            MBeanServerNotification.REGISTRATION_NOTIFICATION)) {
                        tree.addMBeanToView(mbean);
                    } else if (notification.getType().equals(
                            MBeanServerNotification.UNREGISTRATION_NOTIFICATION)) {
                        tree.removeMBeanFromView(mbean);
                    }
                }
            }
        });
    }
    public void propertyChange(PropertyChangeEvent evt) {
        if (JConsoleContext.CONNECTION_STATE_PROPERTY.equals(evt.getPropertyName())) {
            boolean connected = (Boolean) evt.getNewValue();
            if (connected) {
                buildMBeanServerView();
            } else {
                sheet.dispose();
            }
        }
    }
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        sheet.displayNode(node);
    }
    private MouseListener ml = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getClickCount() == 1) {
                int selRow = tree.getRowForLocation(e.getX(), e.getY());
                if (selRow != -1) {
                    TreePath selPath =
                            tree.getPathForLocation(e.getX(), e.getY());
                    DefaultMutableTreeNode node =
                            (DefaultMutableTreeNode) selPath.getLastPathComponent();
                    if (sheet.isMBeanNode(node)) {
                        tree.expandPath(selPath);
                    }
                }
            }
        }
    };
    public void treeWillExpand(TreeExpansionEvent e)
            throws ExpandVetoException {
        TreePath path = e.getPath();
        if (!tree.hasBeenExpanded(path)) {
            DefaultMutableTreeNode node =
                    (DefaultMutableTreeNode) path.getLastPathComponent();
            if (sheet.isMBeanNode(node) && !tree.hasMetadataNodes(node)) {
                tree.addMetadataNodes(node);
            }
        }
    }
    public void treeWillCollapse(TreeExpansionEvent e)
            throws ExpandVetoException {
    }
}
