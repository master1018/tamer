public class JTopPlugin extends JConsolePlugin implements PropertyChangeListener
{
    private JTop jtop = null;
    private Map<String, JPanel> tabs = null;
    public JTopPlugin() {
        addContextPropertyChangeListener(this);
    }
    @Override
    public synchronized Map<String, JPanel> getTabs() {
        if (tabs == null) {
            jtop = new JTop();
            jtop.setMBeanServerConnection(
                getContext().getMBeanServerConnection());
            tabs = new LinkedHashMap<String, JPanel>();
            tabs.put("JTop", jtop);
        }
        return tabs;
    }
    @Override
    public SwingWorker<?,?> newSwingWorker() {
        return jtop.newSwingWorker();
    }
    @Override
    public void propertyChange(PropertyChangeEvent ev) {
        String prop = ev.getPropertyName();
        if (prop == JConsoleContext.CONNECTION_STATE_PROPERTY) {
            ConnectionState newState = (ConnectionState)ev.getNewValue();
            if (newState == ConnectionState.CONNECTED && jtop != null) {
                jtop.setMBeanServerConnection(
                    getContext().getMBeanServerConnection());
            }
        }
    }
}
