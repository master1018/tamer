public class BasicManagedEntity extends HttpEntityWrapper
    implements ConnectionReleaseTrigger, EofSensorWatcher {
    protected ManagedClientConnection managedConn;
    protected final boolean attemptReuse;
    public BasicManagedEntity(HttpEntity entity,
                              ManagedClientConnection conn,
                              boolean reuse) {
        super(entity);
        if (conn == null)
            throw new IllegalArgumentException
                ("Connection may not be null.");
        this.managedConn = conn;
        this.attemptReuse = reuse;
    }
    @Override
    public boolean isRepeatable() {
        return false;
    }
    @Override
    public InputStream getContent() throws IOException {
        return new EofSensorInputStream(wrappedEntity.getContent(), this);
    }
    @Override
    public void consumeContent() throws IOException {
        if (managedConn == null)
            return;
        try {
            if (attemptReuse) {
                wrappedEntity.consumeContent();
                managedConn.markReusable();
            }
        } finally {
            releaseManagedConnection();
        }
    }
    @Override
    public void writeTo(final OutputStream outstream) throws IOException {
        super.writeTo(outstream);
        consumeContent();
    }
    public void releaseConnection()
        throws IOException {
        this.consumeContent();
    }
    public void abortConnection()
        throws IOException {
        if (managedConn != null) {
            try {
                managedConn.abortConnection();
            } finally {
                managedConn = null;
            }
        }
    }
    public boolean eofDetected(InputStream wrapped)
        throws IOException {
        try {
            if (attemptReuse && (managedConn != null)) {
                wrapped.close();
                managedConn.markReusable();
            }
        } finally {
            releaseManagedConnection();
        }
        return false;
    }
    public boolean streamClosed(InputStream wrapped)
        throws IOException {
        try {
            if (attemptReuse && (managedConn != null)) {
                wrapped.close();
                managedConn.markReusable();
            }
        } finally {
            releaseManagedConnection();
        }
        return false;
    }
    public boolean streamAbort(InputStream wrapped)
        throws IOException {
        if (managedConn != null) {
            managedConn.abortConnection();
        }
        return false;
    }
    protected void releaseManagedConnection()
        throws IOException {
        if (managedConn != null) {
            try {
                managedConn.releaseConnection();
            } finally {
                managedConn = null;
            }
        }
    }
} 
