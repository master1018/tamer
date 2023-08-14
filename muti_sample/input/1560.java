public class MailToURLConnection extends URLConnection {
    InputStream is = null;
    OutputStream os = null;
    SmtpClient client;
    Permission permission;
    private int connectTimeout = -1;
    private int readTimeout = -1;
    MailToURLConnection(URL u) {
        super(u);
        MessageHeader props = new MessageHeader();
        props.add("content-type", "text/html");
        setProperties(props);
    }
    String getFromAddress() {
        String str = System.getProperty("user.fromaddr");
        if (str == null) {
            str = System.getProperty("user.name");
            if (str != null) {
                String host = System.getProperty("mail.host");
                if (host == null) {
                    try {
                        host = InetAddress.getLocalHost().getHostName();
                    } catch (java.net.UnknownHostException e) {
                    }
                }
                str += "@" + host;
            } else {
                str = "";
            }
        }
        return str;
    }
    public void connect() throws IOException {
        client = new SmtpClient(connectTimeout);
        client.setReadTimeout(readTimeout);
    }
    @Override
    public synchronized OutputStream getOutputStream() throws IOException {
        if (os != null) {
            return os;
        } else if (is != null) {
            throw new IOException("Cannot write output after reading input.");
        }
        connect();
        String to = ParseUtil.decode(url.getPath());
        client.from(getFromAddress());
        client.to(to);
        os = client.startMessage();
        return os;
    }
    @Override
    public Permission getPermission() throws IOException {
        if (permission == null) {
            connect();
            String host = client.getMailHost() + ":" + 25;
            permission = new SocketPermission(host, "connect");
        }
        return permission;
    }
    @Override
    public void setConnectTimeout(int timeout) {
        if (timeout < 0)
            throw new IllegalArgumentException("timeouts can't be negative");
        connectTimeout = timeout;
    }
    @Override
    public int getConnectTimeout() {
        return (connectTimeout < 0 ? 0 : connectTimeout);
    }
    @Override
    public void setReadTimeout(int timeout) {
        if (timeout < 0)
            throw new IllegalArgumentException("timeouts can't be negative");
        readTimeout = timeout;
    }
    @Override
    public int getReadTimeout() {
        return readTimeout < 0 ? 0 : readTimeout;
    }
}
