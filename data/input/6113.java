public class SyncProviderException extends java.sql.SQLException {
     private SyncResolver syncResolver = null;
    public SyncProviderException() {
        super();
    }
    public SyncProviderException(String msg)  {
        super(msg);
    }
    public SyncProviderException(SyncResolver syncResolver)  {
        if (syncResolver == null) {
            throw new IllegalArgumentException("Cannot instantiate a SyncProviderException " +
                "with a null SyncResolver object");
        } else {
            this.syncResolver = syncResolver;
        }
    }
    public SyncResolver getSyncResolver() {
        if (syncResolver != null) {
            return syncResolver;
        } else {
            try {
              syncResolver = new com.sun.rowset.internal.SyncResolverImpl();
            } catch (SQLException sqle) {
            }
            return syncResolver;
        }
    }
    public void setSyncResolver(SyncResolver syncResolver) {
        if (syncResolver == null) {
            throw new IllegalArgumentException("Cannot set a null SyncResolver " +
                "object");
        } else {
            this.syncResolver = syncResolver;
        }
    }
    static final long serialVersionUID = -939908523620640692L;
}
