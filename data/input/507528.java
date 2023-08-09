public class EofSensorInputStream extends InputStream
    implements ConnectionReleaseTrigger {
    protected InputStream wrappedStream;
    private boolean selfClosed;
    private EofSensorWatcher eofWatcher;
    public EofSensorInputStream(final InputStream in,
                                final EofSensorWatcher watcher) {
        if (in == null) {
            throw new IllegalArgumentException
                ("Wrapped stream may not be null.");
        }
        wrappedStream = in;
        selfClosed = false;
        eofWatcher = watcher;
    }
    protected boolean isReadAllowed() throws IOException {
        if (selfClosed) {
            throw new IOException("Attempted read on closed stream.");
        }
        return (wrappedStream != null);
    }
    @Override
    public int read() throws IOException {
        int l = -1;
        if (isReadAllowed()) {
            try {
                l = wrappedStream.read();
                checkEOF(l);
            } catch (IOException ex) {
                checkAbort();
                throw ex;
            }
        }
        return l;
    }
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int l = -1;
        if (isReadAllowed()) {
            try {
                l = wrappedStream.read(b,  off,  len);
                checkEOF(l);
            } catch (IOException ex) {
                checkAbort();
                throw ex;
            }
        }
        return l;
    }
    @Override
    public int read(byte[] b) throws IOException {
        int l = -1;
        if (isReadAllowed()) {
            try {
                l = wrappedStream.read(b);
                checkEOF(l);
            } catch (IOException ex) {
                checkAbort();
                throw ex;
            }
        }
        return l;
    }
    @Override
    public int available() throws IOException {
        int a = 0; 
        if (isReadAllowed()) {
            try {
                a = wrappedStream.available();
            } catch (IOException ex) {
                checkAbort();
                throw ex;
            }
        }
        return a;
    }
    @Override
    public void close() throws IOException {
        selfClosed = true;
        checkClose();
    }
    protected void checkEOF(int eof) throws IOException {
        if ((wrappedStream != null) && (eof < 0)) {
            try {
                boolean scws = true; 
                if (eofWatcher != null)
                    scws = eofWatcher.eofDetected(wrappedStream);
                if (scws)
                    wrappedStream.close();
            } finally {
                wrappedStream = null;
            }
        }
    }
    protected void checkClose() throws IOException {
        if (wrappedStream != null) {
            try {
                boolean scws = true; 
                if (eofWatcher != null)
                    scws = eofWatcher.streamClosed(wrappedStream);
                if (scws)
                    wrappedStream.close();
            } finally {
                wrappedStream = null;
            }
        }
    }
    protected void checkAbort() throws IOException {
        if (wrappedStream != null) {
            try {
                boolean scws = true; 
                if (eofWatcher != null)
                    scws = eofWatcher.streamAbort(wrappedStream);
                if (scws)
                    wrappedStream.close();
            } finally {
                wrappedStream = null;
            }
        }
    }
    public void releaseConnection() throws IOException {
        this.close();
    }
    public void abortConnection() throws IOException {
        selfClosed = true;
        checkAbort();
    }
} 
