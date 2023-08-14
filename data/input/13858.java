public class VMPanel extends JTabbedPane implements PropertyChangeListener {
    private ProxyClient proxyClient;
    private Timer timer;
    private int updateInterval;
    private String hostName;
    private int port;
    private int vmid;
    private String userName;
    private String password;
    private String url;
    private VMInternalFrame vmIF = null;
    private static final String windowsLaF =
            "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
    private static ArrayList<TabInfo> tabInfos = new ArrayList<TabInfo>();
    private boolean wasConnected = false;
    private boolean everConnected = false;
    private boolean initialUpdate = true;
    private Map<JConsolePlugin, SwingWorker<?, ?>> plugins = null;
    private boolean pluginTabsAdded = false;
    private JOptionPane optionPane;
    private JProgressBar progressBar;
    private long time0;
    static {
        tabInfos.add(new TabInfo(OverviewTab.class, OverviewTab.getTabName(), true));
        tabInfos.add(new TabInfo(MemoryTab.class, MemoryTab.getTabName(), true));
        tabInfos.add(new TabInfo(ThreadTab.class, ThreadTab.getTabName(), true));
        tabInfos.add(new TabInfo(ClassTab.class, ClassTab.getTabName(), true));
        tabInfos.add(new TabInfo(SummaryTab.class, SummaryTab.getTabName(), true));
        tabInfos.add(new TabInfo(MBeansTab.class, MBeansTab.getTabName(), true));
    }
    public static TabInfo[] getTabInfos() {
        return tabInfos.toArray(new TabInfo[tabInfos.size()]);
    }
    VMPanel(ProxyClient proxyClient, int updateInterval) {
        this.proxyClient = proxyClient;
        this.updateInterval = updateInterval;
        this.hostName = proxyClient.getHostName();
        this.port = proxyClient.getPort();
        this.vmid = proxyClient.getVmid();
        this.userName = proxyClient.getUserName();
        this.password = proxyClient.getPassword();
        this.url = proxyClient.getUrl();
        for (TabInfo tabInfo : tabInfos) {
            if (tabInfo.tabVisible) {
                addTab(tabInfo);
            }
        }
        plugins = new LinkedHashMap<JConsolePlugin, SwingWorker<?, ?>>();
        for (JConsolePlugin p : JConsole.getPlugins()) {
            p.setContext(proxyClient);
            plugins.put(p, null);
        }
        Utilities.updateTransparency(this);
        ToolTipManager.sharedInstance().registerComponent(this);
        proxyClient.addPropertyChangeListener(this);
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (connectedIconBounds != null && (e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0 && connectedIconBounds.contains(e.getPoint())) {
                    if (isConnected()) {
                        disconnect();
                        wasConnected = false;
                    } else {
                        connect();
                    }
                    repaint();
                }
            }
        });
    }
    private static Icon connectedIcon16 =
            new ImageIcon(VMPanel.class.getResource("resources/connected16.png"));
    private static Icon connectedIcon24 =
            new ImageIcon(VMPanel.class.getResource("resources/connected24.png"));
    private static Icon disconnectedIcon16 =
            new ImageIcon(VMPanel.class.getResource("resources/disconnected16.png"));
    private static Icon disconnectedIcon24 =
            new ImageIcon(VMPanel.class.getResource("resources/disconnected24.png"));
    private Rectangle connectedIconBounds;
    public void setUI(TabbedPaneUI ui) {
        Insets insets = (Insets) UIManager.getLookAndFeelDefaults().get("TabbedPane.tabAreaInsets");
        insets = (Insets) insets.clone();
        insets.right += connectedIcon24.getIconWidth() + 8;
        UIManager.put("TabbedPane.tabAreaInsets", insets);
        super.setUI(ui);
    }
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Icon icon;
        Component c0 = getComponent(0);
        if (c0 != null && c0.getY() > 24) {
            icon = isConnected() ? connectedIcon24 : disconnectedIcon24;
        } else {
            icon = isConnected() ? connectedIcon16 : disconnectedIcon16;
        }
        Insets insets = getInsets();
        int x = getWidth() - insets.right - icon.getIconWidth() - 4;
        int y = insets.top;
        if (c0 != null) {
            y = (c0.getY() - icon.getIconHeight()) / 2;
        }
        icon.paintIcon(this, g, x, y);
        connectedIconBounds = new Rectangle(x, y, icon.getIconWidth(), icon.getIconHeight());
    }
    public String getToolTipText(MouseEvent event) {
        if (connectedIconBounds.contains(event.getPoint())) {
            if (isConnected()) {
                return getText("Connected. Click to disconnect.");
            } else {
                return getText("Disconnected. Click to connect.");
            }
        } else {
            return super.getToolTipText(event);
        }
    }
    private synchronized void addTab(TabInfo tabInfo) {
        Tab tab = instantiate(tabInfo);
        if (tab != null) {
            addTab(tabInfo.name, tab);
        } else {
            tabInfo.tabVisible = false;
        }
    }
    private synchronized void insertTab(TabInfo tabInfo, int index) {
        Tab tab = instantiate(tabInfo);
        if (tab != null) {
            insertTab(tabInfo.name, null, tab, null, index);
        } else {
            tabInfo.tabVisible = false;
        }
    }
    public synchronized void removeTabAt(int index) {
        super.removeTabAt(index);
    }
    private Tab instantiate(TabInfo tabInfo) {
        try {
            Constructor con = tabInfo.tabClass.getConstructor(VMPanel.class);
            return (Tab) con.newInstance(this);
        } catch (Exception ex) {
            System.err.println(ex);
            return null;
        }
    }
    boolean isConnected() {
        return proxyClient.isConnected();
    }
    public int getUpdateInterval() {
        return updateInterval;
    }
    ProxyClient getProxyClient(boolean assertThread) {
        if (assertThread) {
            return getProxyClient();
        } else {
            return proxyClient;
        }
    }
    public ProxyClient getProxyClient() {
        String threadClass = Thread.currentThread().getClass().getName();
        if (threadClass.equals("java.awt.EventDispatchThread")) {
            String msg = "Calling VMPanel.getProxyClient() from the Event Dispatch Thread!";
            new RuntimeException(msg).printStackTrace();
            System.exit(1);
        }
        return proxyClient;
    }
    public void cleanUp() {
        for (Tab tab : getTabs()) {
            tab.dispose();
        }
        for (JConsolePlugin p : plugins.keySet()) {
            p.dispose();
        }
        if (timer != null) {
            timer.cancel();
        }
        proxyClient.removePropertyChangeListener(this);
    }
    public void connect() {
        if (isConnected()) {
            createPluginTabs();
            fireConnectedChange(true);
            initialUpdate = true;
            startUpdateTimer();
        } else {
            new Thread("VMPanel.connect") {
                public void run() {
                    proxyClient.connect();
                }
            }.start();
        }
    }
    public void disconnect() {
        proxyClient.disconnect();
        updateFrameTitle();
    }
    public void propertyChange(PropertyChangeEvent ev) {
        String prop = ev.getPropertyName();
        if (prop == CONNECTION_STATE_PROPERTY) {
            ConnectionState oldState = (ConnectionState) ev.getOldValue();
            ConnectionState newState = (ConnectionState) ev.getNewValue();
            switch (newState) {
                case CONNECTING:
                    onConnecting();
                    break;
                case CONNECTED:
                    if (progressBar != null) {
                        progressBar.setIndeterminate(false);
                        progressBar.setValue(100);
                    }
                    closeOptionPane();
                    updateFrameTitle();
                    createPluginTabs();
                    repaint();
                    fireConnectedChange(true);
                    initialUpdate = true;
                    startUpdateTimer();
                    break;
                case DISCONNECTED:
                    if (progressBar != null) {
                        progressBar.setIndeterminate(false);
                        progressBar.setValue(0);
                        closeOptionPane();
                    }
                    vmPanelDied();
                    if (oldState == ConnectionState.CONNECTED) {
                        fireConnectedChange(false);
                    }
                    break;
            }
        }
    }
    private void onConnecting() {
        time0 = System.currentTimeMillis();
        final JConsole jc = (JConsole) SwingUtilities.getWindowAncestor(this);
        String connectionName = getConnectionName();
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        JPanel progressPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        progressPanel.add(progressBar);
        Object[] message = {
            "<html><h3>" + getText("connectingTo1", connectionName) + "</h3></html>",
            progressPanel,
            "<html><b>" + getText("connectingTo2", connectionName) + "</b></html>"
        };
        optionPane =
                SheetDialog.showOptionDialog(this,
                message,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE, null,
                new String[]{getText("Cancel")},
                0);
    }
    private void closeOptionPane() {
        if (optionPane != null) {
            new Thread("VMPanel.sleeper") {
                public void run() {
                    long elapsed = System.currentTimeMillis() - time0;
                    if (elapsed < 2000) {
                        try {
                            sleep(2000 - elapsed);
                        } catch (InterruptedException ex) {
                        }
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            optionPane.setVisible(false);
                            progressBar = null;
                        }
                    });
                }
            }.start();
        }
    }
    void updateFrameTitle() {
        VMInternalFrame vmIF = getFrame();
        if (vmIF != null) {
            String displayName = getDisplayName();
            if (!proxyClient.isConnected()) {
                displayName = getText("ConnectionName (disconnected)", displayName);
            }
            vmIF.setTitle(displayName);
        }
    }
    private VMInternalFrame getFrame() {
        if (vmIF == null) {
            vmIF = (VMInternalFrame) SwingUtilities.getAncestorOfClass(VMInternalFrame.class,
                    this);
        }
        return vmIF;
    }
    synchronized List<Tab> getTabs() {
        ArrayList<Tab> list = new ArrayList<Tab>();
        int n = getTabCount();
        for (int i = 0; i < n; i++) {
            Component c = getComponentAt(i);
            if (c instanceof Tab) {
                list.add((Tab) c);
            }
        }
        return list;
    }
    private void startUpdateTimer() {
        if (timer != null) {
            timer.cancel();
        }
        TimerTask timerTask = new TimerTask() {
            public void run() {
                update();
            }
        };
        String timerName = "Timer-" + getConnectionName();
        timer = new Timer(timerName, true);
        timer.schedule(timerTask, 0, updateInterval);
    }
    private void vmPanelDied() {
        disconnect();
        final JConsole jc = (JConsole) SwingUtilities.getWindowAncestor(this);
        JOptionPane optionPane;
        final String connectStr = getText("Connect");
        final String reconnectStr = getText("Reconnect");
        final String cancelStr = getText("Cancel");
        String msgTitle, msgExplanation, buttonStr;
        if (wasConnected) {
            wasConnected = false;
            msgTitle = getText("connectionLost1");
            msgExplanation = getText("connectionLost2", getConnectionName());
            buttonStr = reconnectStr;
        } else {
            msgTitle = getText("connectionFailed1");
            msgExplanation = getText("connectionFailed2", getConnectionName());
            buttonStr = connectStr;
        }
        optionPane =
                SheetDialog.showOptionDialog(this,
                "<html><h3>" + msgTitle + "</h3>" +
                "<b>" + msgExplanation + "</b>",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE, null,
                new String[]{buttonStr, cancelStr},
                0);
        optionPane.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                if (event.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)) {
                    Object value = event.getNewValue();
                    if (value == reconnectStr || value == connectStr) {
                        connect();
                    } else if (!everConnected) {
                        try {
                            getFrame().setClosed(true);
                        } catch (PropertyVetoException ex) {
                        }
                    }
                }
            }
        });
    }
    private Object lockObject = new Object();
    private void update() {
        synchronized (lockObject) {
            if (!isConnected()) {
                if (wasConnected) {
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            vmPanelDied();
                        }
                    });
                }
                wasConnected = false;
                return;
            } else {
                wasConnected = true;
                everConnected = true;
            }
            proxyClient.flush();
            List<Tab> tabs = getTabs();
            final int n = tabs.size();
            for (int i = 0; i < n; i++) {
                final int index = i;
                try {
                    if (!proxyClient.isDead()) {
                        tabs.get(index).update();
                        if (initialUpdate) {
                            EventQueue.invokeLater(new Runnable() {
                                public void run() {
                                    setEnabledAt(index, true);
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    if (initialUpdate) {
                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                setEnabledAt(index, false);
                            }
                        });
                    }
                }
            }
            for (JConsolePlugin p : plugins.keySet()) {
                SwingWorker<?, ?> sw = p.newSwingWorker();
                SwingWorker<?, ?> prevSW = plugins.get(p);
                if (prevSW == null || prevSW.isDone()) {
                    if (sw == null || sw.getState() == SwingWorker.StateValue.PENDING) {
                        plugins.put(p, sw);
                        if (sw != null) {
                            sw.execute();
                        }
                    }
                }
            }
            if (initialUpdate) {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        int index = getSelectedIndex();
                        if (index < 0 || !isEnabledAt(index)) {
                            for (int i = 0; i < n; i++) {
                                if (isEnabledAt(i)) {
                                    setSelectedIndex(i);
                                    break;
                                }
                            }
                        }
                    }
                });
                initialUpdate = false;
            }
        }
    }
    public String getHostName() {
        return hostName;
    }
    public int getPort() {
        return port;
    }
    public String getUserName() {
        return userName;
    }
    public String getUrl() {
        return url;
    }
    public String getPassword() {
        return password;
    }
    public String getConnectionName() {
        return proxyClient.connectionName();
    }
    public String getDisplayName() {
        return proxyClient.getDisplayName();
    }
    static class TabInfo {
        Class<? extends Tab> tabClass;
        String name;
        boolean tabVisible;
        TabInfo(Class<? extends Tab> tabClass, String name, boolean tabVisible) {
            this.tabClass = tabClass;
            this.name = name;
            this.tabVisible = tabVisible;
        }
    }
    private static String getText(String key, Object... args) {
        return Resources.getText(key, args);
    }
    private void createPluginTabs() {
        if (!pluginTabsAdded) {
            for (JConsolePlugin p : plugins.keySet()) {
                Map<String, JPanel> tabs = p.getTabs();
                for (Map.Entry<String, JPanel> e : tabs.entrySet()) {
                    addTab(e.getKey(), e.getValue());
                }
            }
            pluginTabsAdded = true;
        }
    }
    private void fireConnectedChange(boolean connected) {
        for (Tab tab : getTabs()) {
            tab.firePropertyChange(JConsoleContext.CONNECTION_STATE_PROPERTY, !connected, connected);
        }
    }
}
