public class StreamHandler extends Handler {
    private OutputStream os;
    private Writer writer;
    private boolean writerNotInitialized;
    public StreamHandler() {
        initProperties("INFO", null, "java.util.logging.SimpleFormatter", 
                null);
        this.os = null;
        this.writer = null;
        this.writerNotInitialized = true;
    }
    StreamHandler(OutputStream os) {
        this();
        this.os = os;
    }
    StreamHandler(String defaultLevel, String defaultFilter,
            String defaultFormatter, String defaultEncoding) {
        initProperties(defaultLevel, defaultFilter, defaultFormatter,
                defaultEncoding);
        this.os = null;
        this.writer = null;
        this.writerNotInitialized = true;
    }
    public StreamHandler(OutputStream os, Formatter formatter) {
        this();
        if (os == null) {
            throw new NullPointerException(Messages.getString("logging.2")); 
        }
        if (formatter == null) {
            throw new NullPointerException(Messages.getString("logging.3")); 
        }
        this.os = os;
        internalSetFormatter(formatter);
    }
    private void initializeWritter() {
        this.writerNotInitialized = false;
        if (null == getEncoding()) {
            this.writer = new OutputStreamWriter(this.os);
        } else {
            try {
                this.writer = new OutputStreamWriter(this.os, getEncoding());
            } catch (UnsupportedEncodingException e) {
            }
        }
        write(getFormatter().getHead(this));
    }
    private void write(String s) {
        try {
            this.writer.write(s);
        } catch (Exception e) {
            getErrorManager().error(Messages.getString("logging.14"), e, 
                    ErrorManager.WRITE_FAILURE);
        }
    }
    void internalSetOutputStream(OutputStream newOs) {
        this.os = newOs;
    }
    protected void setOutputStream(OutputStream os) {
        if (null == os) {
            throw new NullPointerException();
        }
        LogManager.getLogManager().checkAccess();
        close(true);
        this.writer = null;
        this.os = os;
        this.writerNotInitialized = true;
    }
    @Override
    public void setEncoding(String encoding) throws SecurityException,
            UnsupportedEncodingException {
        this.flush();
        super.setEncoding(encoding);
        if (null != this.writer) {
            if (null == getEncoding()) {
                this.writer = new OutputStreamWriter(this.os);
            } else {
                try {
                    this.writer = new OutputStreamWriter(this.os, getEncoding());
                } catch (UnsupportedEncodingException e) {
                    throw new AssertionError(e);
                }
            }
        }
    }
    void close(boolean closeStream) {
        if (null != this.os) {
            if (this.writerNotInitialized) {
                initializeWritter();
            }
            write(getFormatter().getTail(this));
            try {
                this.writer.flush();
                if (closeStream) {
                    this.writer.close();
                    this.writer = null;
                    this.os = null;
                }
            } catch (Exception e) {
                getErrorManager().error(Messages.getString("logging.15"), e, 
                        ErrorManager.CLOSE_FAILURE);
            }
        }
    }
    @Override
    public void close() {
        LogManager.getLogManager().checkAccess();
        close(true);
    }
    @Override
    public void flush() {
        if (null != this.os) {
            try {
                if (null != this.writer) {
                    this.writer.flush();
                } else {
                    this.os.flush();
                }
            } catch (Exception e) {
                getErrorManager().error(Messages.getString("logging.16"), 
                        e, ErrorManager.FLUSH_FAILURE);
            }
        }
    }
    @Override
    public synchronized void publish(LogRecord record) {
        try {
            if (this.isLoggable(record)) {
                if (this.writerNotInitialized) {
                    initializeWritter();
                }
                String msg = null;
                try {
                    msg = getFormatter().format(record);
                } catch (Exception e) {
                    getErrorManager().error(Messages.getString("logging.17"), 
                            e, ErrorManager.FORMAT_FAILURE);
                }
                write(msg);
            }
        } catch (Exception e) {
            getErrorManager().error(Messages.getString("logging.18"), e, 
                    ErrorManager.GENERIC_FAILURE);
        }
    }
    @Override
    public boolean isLoggable(LogRecord record) {
        if (null == record) {
            return false;
        }
        if (null != this.os && super.isLoggable(record)) {
            return true;
        }
        return false;
    }
}
