public class XSheet extends JPanel
        implements ActionListener, NotificationListener {
    private JPanel mainPanel;
    private JPanel southPanel;
    private volatile DefaultMutableTreeNode currentNode;
    private volatile XMBean mbean;
    private XMBeanAttributes mbeanAttributes;
    private XMBeanOperations mbeanOperations;
    private XMBeanNotifications mbeanNotifications;
    private XMBeanInfo mbeanInfo;
    private JButton refreshButton;
    private JButton clearButton,  subscribeButton,  unsubscribeButton;
    private MBeansTab mbeansTab;
    public XSheet(MBeansTab mbeansTab) {
        this.mbeansTab = mbeansTab;
        setupScreen();
    }
    public void dispose() {
        clear();
        XDataViewer.dispose(mbeansTab);
        mbeanNotifications.dispose();
    }
    private void setupScreen() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createLineBorder(Color.GRAY));
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
        southPanel = new JPanel();
        add(southPanel, BorderLayout.SOUTH);
        String refreshButtonKey = "MBeansTab.refreshAttributesButton";
        refreshButton = new JButton(getText(refreshButtonKey));
        refreshButton.setMnemonic(getMnemonicInt(refreshButtonKey));
        refreshButton.setToolTipText(getText(refreshButtonKey + ".toolTip"));
        refreshButton.addActionListener(this);
        String clearButtonKey = "MBeansTab.clearNotificationsButton";
        clearButton = new JButton(getText(clearButtonKey));
        clearButton.setMnemonic(getMnemonicInt(clearButtonKey));
        clearButton.setToolTipText(getText(clearButtonKey + ".toolTip"));
        clearButton.addActionListener(this);
        String subscribeButtonKey = "MBeansTab.subscribeNotificationsButton";
        subscribeButton = new JButton(getText(subscribeButtonKey));
        subscribeButton.setMnemonic(getMnemonicInt(subscribeButtonKey));
        subscribeButton.setToolTipText(getText(subscribeButtonKey + ".toolTip"));
        subscribeButton.addActionListener(this);
        String unsubscribeButtonKey = "MBeansTab.unsubscribeNotificationsButton";
        unsubscribeButton = new JButton(getText(unsubscribeButtonKey));
        unsubscribeButton.setMnemonic(getMnemonicInt(unsubscribeButtonKey));
        unsubscribeButton.setToolTipText(getText(unsubscribeButtonKey + ".toolTip"));
        unsubscribeButton.addActionListener(this);
        mbeanAttributes = new XMBeanAttributes(mbeansTab);
        mbeanOperations = new XMBeanOperations(mbeansTab);
        mbeanOperations.addOperationsListener(this);
        mbeanNotifications = new XMBeanNotifications();
        mbeanNotifications.addNotificationsListener(this);
        mbeanInfo = new XMBeanInfo();
    }
    private boolean isSelectedNode(DefaultMutableTreeNode n, DefaultMutableTreeNode cn) {
        return (cn == n);
    }
    private void showErrorDialog(Object message, String title) {
        new ThreadDialog(this, message, title, JOptionPane.ERROR_MESSAGE).run();
    }
    public boolean isMBeanNode(DefaultMutableTreeNode node) {
        Object userObject = node.getUserObject();
        if (userObject instanceof XNodeInfo) {
            XNodeInfo uo = (XNodeInfo) userObject;
            return uo.getType().equals(Type.MBEAN);
        }
        return false;
    }
    public synchronized void displayNode(DefaultMutableTreeNode node) {
        clear();
        displayEmptyNode();
        if (node == null) {
            return;
        }
        currentNode = node;
        Object userObject = node.getUserObject();
        if (userObject instanceof XNodeInfo) {
            XNodeInfo uo = (XNodeInfo) userObject;
            switch (uo.getType()) {
                case MBEAN:
                    displayMBeanNode(node);
                    break;
                case NONMBEAN:
                    displayEmptyNode();
                    break;
                case ATTRIBUTES:
                    displayMBeanAttributesNode(node);
                    break;
                case OPERATIONS:
                    displayMBeanOperationsNode(node);
                    break;
                case NOTIFICATIONS:
                    displayMBeanNotificationsNode(node);
                    break;
                case ATTRIBUTE:
                case OPERATION:
                case NOTIFICATION:
                    displayMetadataNode(node);
                    break;
                default:
                    displayEmptyNode();
                    break;
            }
        } else {
            displayEmptyNode();
        }
    }
    private void displayMBeanNode(final DefaultMutableTreeNode node) {
        final XNodeInfo uo = (XNodeInfo) node.getUserObject();
        if (!uo.getType().equals(Type.MBEAN)) {
            return;
        }
        mbean = (XMBean) uo.getData();
        SwingWorker<MBeanInfo, Void> sw = new SwingWorker<MBeanInfo, Void>() {
            @Override
            public MBeanInfo doInBackground() throws InstanceNotFoundException,
                    IntrospectionException, ReflectionException, IOException {
                return mbean.getMBeanInfo();
            }
            @Override
            protected void done() {
                try {
                    MBeanInfo mbi = get();
                    if (mbi != null) {
                        if (!isSelectedNode(node, currentNode)) {
                            return;
                        }
                        mbeanInfo.addMBeanInfo(mbean, mbi);
                        invalidate();
                        mainPanel.removeAll();
                        mainPanel.add(mbeanInfo, BorderLayout.CENTER);
                        southPanel.setVisible(false);
                        southPanel.removeAll();
                        validate();
                        repaint();
                    }
                } catch (Exception e) {
                    Throwable t = Utils.getActualException(e);
                    if (JConsole.isDebug()) {
                        System.err.println("Couldn't get MBeanInfo for MBean [" +
                                mbean.getObjectName() + "]");
                        t.printStackTrace();
                    }
                    showErrorDialog(t.toString(),
                            Resources.getText("Problem displaying MBean"));
                }
            }
        };
        sw.execute();
    }
    private void displayMetadataNode(final DefaultMutableTreeNode node) {
        final XNodeInfo uo = (XNodeInfo) node.getUserObject();
        final XMBeanInfo mbi = mbeanInfo;
        switch (uo.getType()) {
            case ATTRIBUTE:
                SwingWorker<MBeanAttributeInfo, Void> sw =
                        new SwingWorker<MBeanAttributeInfo, Void>() {
                            @Override
                            public MBeanAttributeInfo doInBackground() {
                                Object attrData = uo.getData();
                                mbean = (XMBean) ((Object[]) attrData)[0];
                                MBeanAttributeInfo mbai =
                                        (MBeanAttributeInfo) ((Object[]) attrData)[1];
                                mbeanAttributes.loadAttributes(mbean, new MBeanInfo(
                                        null, null, new MBeanAttributeInfo[]{mbai},
                                        null, null, null));
                                return mbai;
                            }
                            @Override
                            protected void done() {
                                try {
                                    MBeanAttributeInfo mbai = get();
                                    if (!isSelectedNode(node, currentNode)) {
                                        return;
                                    }
                                    invalidate();
                                    mainPanel.removeAll();
                                    JPanel attributePanel =
                                            new JPanel(new BorderLayout());
                                    JPanel attributeBorderPanel =
                                            new JPanel(new BorderLayout());
                                    attributeBorderPanel.setBorder(
                                            BorderFactory.createTitledBorder(
                                            Resources.getText("Attribute value")));
                                    JPanel attributeValuePanel =
                                            new JPanel(new BorderLayout());
                                    attributeValuePanel.setBorder(
                                            LineBorder.createGrayLineBorder());
                                    attributeValuePanel.add(mbeanAttributes.getTableHeader(),
                                            BorderLayout.PAGE_START);
                                    attributeValuePanel.add(mbeanAttributes,
                                            BorderLayout.CENTER);
                                    attributeBorderPanel.add(attributeValuePanel,
                                            BorderLayout.CENTER);
                                    JPanel refreshButtonPanel = new JPanel();
                                    refreshButtonPanel.add(refreshButton);
                                    attributeBorderPanel.add(refreshButtonPanel,
                                            BorderLayout.SOUTH);
                                    refreshButton.setEnabled(true);
                                    attributePanel.add(attributeBorderPanel,
                                            BorderLayout.NORTH);
                                    mbi.addMBeanAttributeInfo(mbai);
                                    attributePanel.add(mbi, BorderLayout.CENTER);
                                    mainPanel.add(attributePanel,
                                            BorderLayout.CENTER);
                                    southPanel.setVisible(false);
                                    southPanel.removeAll();
                                    validate();
                                    repaint();
                                } catch (Exception e) {
                                    Throwable t = Utils.getActualException(e);
                                    if (JConsole.isDebug()) {
                                        System.err.println("Problem displaying MBean " +
                                                "attribute for MBean [" +
                                                mbean.getObjectName() + "]");
                                        t.printStackTrace();
                                    }
                                    showErrorDialog(t.toString(),
                                            Resources.getText("Problem displaying MBean"));
                                }
                            }
                        };
                sw.execute();
                break;
            case OPERATION:
                Object operData = uo.getData();
                mbean = (XMBean) ((Object[]) operData)[0];
                MBeanOperationInfo mboi =
                        (MBeanOperationInfo) ((Object[]) operData)[1];
                mbeanOperations.loadOperations(mbean,
                        new MBeanInfo(null, null, null, null,
                        new MBeanOperationInfo[]{mboi}, null));
                invalidate();
                mainPanel.removeAll();
                JPanel operationPanel = new JPanel(new BorderLayout());
                JPanel operationBorderPanel = new JPanel(new BorderLayout());
                operationBorderPanel.setBorder(BorderFactory.createTitledBorder(
                        Resources.getText("Operation invocation")));
                operationBorderPanel.add(new JScrollPane(mbeanOperations));
                operationPanel.add(operationBorderPanel, BorderLayout.NORTH);
                mbi.addMBeanOperationInfo(mboi);
                operationPanel.add(mbi, BorderLayout.CENTER);
                mainPanel.add(operationPanel, BorderLayout.CENTER);
                southPanel.setVisible(false);
                southPanel.removeAll();
                validate();
                repaint();
                break;
            case NOTIFICATION:
                Object notifData = uo.getData();
                invalidate();
                mainPanel.removeAll();
                mbi.addMBeanNotificationInfo((MBeanNotificationInfo) notifData);
                mainPanel.add(mbi, BorderLayout.CENTER);
                southPanel.setVisible(false);
                southPanel.removeAll();
                validate();
                repaint();
                break;
        }
    }
    private void displayMBeanAttributesNode(final DefaultMutableTreeNode node) {
        final XNodeInfo uo = (XNodeInfo) node.getUserObject();
        if (!uo.getType().equals(Type.ATTRIBUTES)) {
            return;
        }
        mbean = (XMBean) uo.getData();
        final XMBean xmb = mbean;
        SwingWorker<MBeanInfo,Void> sw = new SwingWorker<MBeanInfo,Void>() {
            @Override
            public MBeanInfo doInBackground() throws InstanceNotFoundException,
                    IntrospectionException, ReflectionException, IOException {
                MBeanInfo mbi = xmb.getMBeanInfo();
                return mbi;
            }
            @Override
            protected void done() {
                try {
                    MBeanInfo mbi = get();
                    if (mbi != null && mbi.getAttributes() != null &&
                            mbi.getAttributes().length > 0) {
                        mbeanAttributes.loadAttributes(xmb, mbi);
                        if (!isSelectedNode(node, currentNode)) {
                            return;
                        }
                        invalidate();
                        mainPanel.removeAll();
                        JPanel borderPanel = new JPanel(new BorderLayout());
                        borderPanel.setBorder(BorderFactory.createTitledBorder(
                                Resources.getText("Attribute values")));
                        borderPanel.add(new JScrollPane(mbeanAttributes));
                        mainPanel.add(borderPanel, BorderLayout.CENTER);
                        southPanel.removeAll();
                        southPanel.add(refreshButton, BorderLayout.SOUTH);
                        southPanel.setVisible(true);
                        refreshButton.setEnabled(true);
                        validate();
                        repaint();
                    }
                } catch (Exception e) {
                    Throwable t = Utils.getActualException(e);
                    if (JConsole.isDebug()) {
                        System.err.println("Problem displaying MBean " +
                                "attributes for MBean [" +
                                mbean.getObjectName() + "]");
                        t.printStackTrace();
                    }
                    showErrorDialog(t.toString(),
                            Resources.getText("Problem displaying MBean"));
                }
            }
        };
        sw.execute();
    }
    private void displayMBeanOperationsNode(final DefaultMutableTreeNode node) {
        final XNodeInfo uo = (XNodeInfo) node.getUserObject();
        if (!uo.getType().equals(Type.OPERATIONS)) {
            return;
        }
        mbean = (XMBean) uo.getData();
        SwingWorker<MBeanInfo, Void> sw = new SwingWorker<MBeanInfo, Void>() {
            @Override
            public MBeanInfo doInBackground() throws InstanceNotFoundException,
                    IntrospectionException, ReflectionException, IOException {
                return mbean.getMBeanInfo();
            }
            @Override
            protected void done() {
                try {
                    MBeanInfo mbi = get();
                    if (mbi != null) {
                        if (!isSelectedNode(node, currentNode)) {
                            return;
                        }
                        mbeanOperations.loadOperations(mbean, mbi);
                        invalidate();
                        mainPanel.removeAll();
                        JPanel borderPanel = new JPanel(new BorderLayout());
                        borderPanel.setBorder(BorderFactory.createTitledBorder(
                                Resources.getText("Operation invocation")));
                        borderPanel.add(new JScrollPane(mbeanOperations));
                        mainPanel.add(borderPanel, BorderLayout.CENTER);
                        southPanel.setVisible(false);
                        southPanel.removeAll();
                        validate();
                        repaint();
                    }
                } catch (Exception e) {
                    Throwable t = Utils.getActualException(e);
                    if (JConsole.isDebug()) {
                        System.err.println("Problem displaying MBean " +
                                "operations for MBean [" +
                                mbean.getObjectName() + "]");
                        t.printStackTrace();
                    }
                    showErrorDialog(t.toString(),
                            Resources.getText("Problem displaying MBean"));
                }
            }
        };
        sw.execute();
    }
    private void displayMBeanNotificationsNode(DefaultMutableTreeNode node) {
        final XNodeInfo uo = (XNodeInfo) node.getUserObject();
        if (!uo.getType().equals(Type.NOTIFICATIONS)) {
            return;
        }
        mbean = (XMBean) uo.getData();
        mbeanNotifications.loadNotifications(mbean);
        updateNotifications();
        invalidate();
        mainPanel.removeAll();
        JPanel borderPanel = new JPanel(new BorderLayout());
        borderPanel.setBorder(BorderFactory.createTitledBorder(
                Resources.getText("Notification buffer")));
        borderPanel.add(new JScrollPane(mbeanNotifications));
        mainPanel.add(borderPanel, BorderLayout.CENTER);
        southPanel.removeAll();
        southPanel.add(subscribeButton, BorderLayout.WEST);
        southPanel.add(unsubscribeButton, BorderLayout.CENTER);
        southPanel.add(clearButton, BorderLayout.EAST);
        southPanel.setVisible(true);
        subscribeButton.setEnabled(true);
        unsubscribeButton.setEnabled(true);
        clearButton.setEnabled(true);
        validate();
        repaint();
    }
    private void displayEmptyNode() {
        invalidate();
        mainPanel.removeAll();
        southPanel.removeAll();
        validate();
        repaint();
    }
    private void registerListener() {
        new SwingWorker<Void, Void>() {
            @Override
            public Void doInBackground()
                    throws InstanceNotFoundException, IOException {
                mbeanNotifications.registerListener(currentNode);
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    updateNotifications();
                    validate();
                } catch (Exception e) {
                    Throwable t = Utils.getActualException(e);
                    if (JConsole.isDebug()) {
                        System.err.println("Problem adding listener");
                        t.printStackTrace();
                    }
                    showErrorDialog(t.getMessage(),
                            Resources.getText("Problem adding listener"));
                }
            }
        }.execute();
    }
    private void unregisterListener() {
        new SwingWorker<Boolean, Void>() {
            @Override
            public Boolean doInBackground() {
                return mbeanNotifications.unregisterListener(currentNode);
            }
            @Override
            protected void done() {
                try {
                    if (get()) {
                        updateNotifications();
                        validate();
                    }
                } catch (Exception e) {
                    Throwable t = Utils.getActualException(e);
                    if (JConsole.isDebug()) {
                        System.err.println("Problem removing listener");
                        t.printStackTrace();
                    }
                    showErrorDialog(t.getMessage(),
                            Resources.getText("Problem removing listener"));
                }
            }
        }.execute();
    }
    private void refreshAttributes() {
        mbeanAttributes.refreshAttributes();
    }
    private void updateNotifications() {
        if (mbeanNotifications.isListenerRegistered(mbean)) {
            long received = mbeanNotifications.getReceivedNotifications(mbean);
            updateReceivedNotifications(currentNode, received, false);
        } else {
            clearNotifications();
        }
    }
    private void updateReceivedNotifications(
            DefaultMutableTreeNode emitter, long received, boolean bold) {
        String text = Resources.getText("Notifications") + "[" + received + "]";
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) mbeansTab.getTree().getLastSelectedPathComponent();
        if (bold && emitter != selectedNode) {
            text = "<html><b>" + text + "</b></html>";
        }
        updateNotificationsNodeLabel(emitter, text);
    }
    private void clearNotifications() {
        updateNotificationsNodeLabel(currentNode,
                Resources.getText("Notifications"));
    }
    private void clearNotifications0() {
        updateNotificationsNodeLabel(currentNode,
                Resources.getText("Notifications") + "[0]");
    }
    private void updateNotificationsNodeLabel(
            DefaultMutableTreeNode node, String label) {
        synchronized (mbeansTab.getTree()) {
            invalidate();
            XNodeInfo oldUserObject = (XNodeInfo) node.getUserObject();
            XNodeInfo newUserObject = new XNodeInfo(
                    oldUserObject.getType(), oldUserObject.getData(),
                    label, oldUserObject.getToolTipText());
            node.setUserObject(newUserObject);
            DefaultTreeModel model =
                    (DefaultTreeModel) mbeansTab.getTree().getModel();
            model.nodeChanged(node);
            validate();
            repaint();
        }
    }
    private void clearCurrentNotifications() {
        mbeanNotifications.clearCurrentNotifications();
        if (mbeanNotifications.isListenerRegistered(mbean)) {
            clearNotifications0();
        } else {
            clearNotifications();
        }
    }
    private void clear() {
        mbeanAttributes.stopCellEditing();
        mbeanAttributes.emptyTable();
        mbeanAttributes.removeAttributes();
        mbeanOperations.removeOperations();
        mbeanNotifications.stopCellEditing();
        mbeanNotifications.emptyTable();
        mbeanNotifications.disableNotifications();
        mbean = null;
        currentNode = null;
    }
    public void handleNotification(Notification e, Object handback) {
        if (e.getType().equals(XOperations.OPERATION_INVOCATION_EVENT)) {
            final Object message;
            if (handback == null) {
                JTextArea textArea = new JTextArea("null");
                textArea.setEditable(false);
                textArea.setEnabled(true);
                textArea.setRows(textArea.getLineCount());
                message = textArea;
            } else {
                Component comp = mbeansTab.getDataViewer().
                        createOperationViewer(handback, mbean);
                if (comp == null) {
                    JTextArea textArea = new JTextArea(handback.toString());
                    textArea.setEditable(false);
                    textArea.setEnabled(true);
                    textArea.setRows(textArea.getLineCount());
                    JScrollPane scrollPane = new JScrollPane(textArea);
                    Dimension d = scrollPane.getPreferredSize();
                    if (d.getWidth() > 400 || d.getHeight() > 250) {
                        scrollPane.setPreferredSize(new Dimension(400, 250));
                    }
                    message = scrollPane;
                } else {
                    if (!(comp instanceof JScrollPane)) {
                        comp = new JScrollPane(comp);
                    }
                    Dimension d = comp.getPreferredSize();
                    if (d.getWidth() > 400 || d.getHeight() > 250) {
                        comp.setPreferredSize(new Dimension(400, 250));
                    }
                    message = comp;
                }
            }
            new ThreadDialog(
                    (Component) e.getSource(),
                    message,
                    Resources.getText("Operation return value"),
                    JOptionPane.INFORMATION_MESSAGE).run();
        } 
        else if (e.getType().equals(
                XMBeanNotifications.NOTIFICATION_RECEIVED_EVENT)) {
            DefaultMutableTreeNode emitter = (DefaultMutableTreeNode) handback;
            Long received = (Long) e.getUserData();
            updateReceivedNotifications(emitter, received.longValue(), true);
        }
    }
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JButton) {
            JButton button = (JButton) e.getSource();
            if (button == refreshButton) {
                refreshAttributes();
                return;
            }
            if (button == clearButton) {
                clearCurrentNotifications();
                return;
            }
            if (button == subscribeButton) {
                registerListener();
                return;
            }
            if (button == unsubscribeButton) {
                unregisterListener();
                return;
            }
        }
    }
}
