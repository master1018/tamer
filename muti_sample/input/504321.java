public class MultiplexingLogListener implements ILogListener {
    private ArrayList<ILogListener> listeners = new ArrayList<ILogListener>();
    public void addListener(ILogListener listener) {
        listeners.add(listener);
    }
    public void removeListener(ILogListener listener) {
        listeners.remove(listener);
    }
    public void newData(byte[] data, int offset, int length) {
        for (ILogListener listener : listeners) {
            listener.newData(data, offset, length);
        }
    }
    public void newEntry(LogEntry entry) {
        for (ILogListener listener : listeners) {
            listener.newEntry(entry);
        }
    }
}
