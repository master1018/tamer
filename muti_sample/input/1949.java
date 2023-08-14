public class NamingEvent extends java.util.EventObject {
    public static final int OBJECT_ADDED = 0;
    public static final int OBJECT_REMOVED = 1;
    public static final int OBJECT_RENAMED = 2;
    public static final int OBJECT_CHANGED = 3;
    protected Object changeInfo;
    protected int type;
    protected Binding oldBinding;
    protected Binding newBinding;
    public NamingEvent(EventContext source, int type,
        Binding newBd, Binding oldBd, Object changeInfo) {
        super(source);
        this.type = type;
        oldBinding = oldBd;
        newBinding = newBd;
        this.changeInfo = changeInfo;
    }
    public int getType() {
        return type;
    }
    public EventContext getEventContext() {
        return (EventContext)getSource();
    }
    public Binding getOldBinding() {
        return oldBinding;
    }
    public Binding getNewBinding() {
        return newBinding;
    }
    public Object getChangeInfo() {
        return changeInfo;
    }
    public void dispatch(NamingListener listener) {
        switch (type) {
        case OBJECT_ADDED:
            ((NamespaceChangeListener)listener).objectAdded(this);
            break;
        case OBJECT_REMOVED:
            ((NamespaceChangeListener)listener).objectRemoved(this);
            break;
        case OBJECT_RENAMED:
            ((NamespaceChangeListener)listener).objectRenamed(this);
            break;
        case OBJECT_CHANGED:
            ((ObjectChangeListener)listener).objectChanged(this);
            break;
        }
    }
    private static final long serialVersionUID = -7126752885365133499L;
}
