public class SocketHandler extends StreamHandler {
    private static final String DEFAULT_LEVEL = "ALL"; 
    private static final String DEFAULT_FORMATTER = "java.util.logging.XMLFormatter"; 
    private Socket socket;
    public SocketHandler() throws IOException {
        super(DEFAULT_LEVEL, null, DEFAULT_FORMATTER, null);
        initSocket(LogManager.getLogManager().getProperty(
                "java.util.logging.SocketHandler.host"), LogManager 
                .getLogManager().getProperty(
                        "java.util.logging.SocketHandler.port")); 
    }
    public SocketHandler(String host, int port) throws IOException {
        super(DEFAULT_LEVEL, null, DEFAULT_FORMATTER, null);
        initSocket(host, String.valueOf(port));
    }
    private void initSocket(String host, String port) throws IOException {
        if (null == host || "".equals(host)) { 
            throw new IllegalArgumentException(Messages.getString("logging.C")); 
        }
        int p = 0;
        try {
            p = Integer.parseInt(port);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(Messages.getString("logging.D")); 
        }
        if (p <= 0) {
            throw new IllegalArgumentException(Messages.getString("logging.D")); 
        }
        try {
            this.socket = new Socket(host, p);
        } catch (IOException e) {
            getErrorManager().error(Messages.getString("logging.E"), e, 
                    ErrorManager.OPEN_FAILURE);
            throw e;
        }
        super.internalSetOutputStream(new BufferedOutputStream(this.socket
                        .getOutputStream(), 8192));
    }
    @Override
    public void close() {
        try {
            super.close();
            if (null != this.socket) {
                this.socket.close();
                this.socket = null;
            }
        } catch (Exception e) {
            getErrorManager().error(Messages.getString("logging.F"), e, 
                    ErrorManager.CLOSE_FAILURE);
        }
    }
    @Override
    public void publish(LogRecord record) {
        super.publish(record);
        super.flush();
    }
}
