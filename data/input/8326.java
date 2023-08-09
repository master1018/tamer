public class MonitorListModel extends AbstractListModel {
    private final List<String> monitors = new ArrayList<String>();
    MonitorListModel(Environment env) {
        MonitorListListener listener = new MonitorListListener();
        env.getContextManager().addContextListener(listener);
    }
    @Override
    public Object getElementAt(int index) {
        return monitors.get(index);
    }
    @Override
    public int getSize() {
        return monitors.size();
    }
    public void add(String expr) {
        monitors.add(expr);
        int newIndex = monitors.size()-1;  
        fireIntervalAdded(this, newIndex, newIndex);
    }
    public void remove(String expr) {
        int index = monitors.indexOf(expr);
        remove(index);
    }
    public void remove(int index) {
        monitors.remove(index);
        fireIntervalRemoved(this, index, index);
    }
    public List<String> monitors() {
        return Collections.unmodifiableList(monitors);
    }
    public Iterator<?> iterator() {
        return monitors().iterator();
    }
    private void invalidate() {
        fireContentsChanged(this, 0, monitors.size()-1);
    }
    private class MonitorListListener implements ContextListener {
        @Override
        public void currentFrameChanged(final CurrentFrameChangedEvent e) {
            invalidate();
        }
    }
}
