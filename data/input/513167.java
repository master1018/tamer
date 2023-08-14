public class FtpURLConnection extends URLConnection {
    private static final int FTP_PORT = 21;
    private static final int FTP_DATAOPEN = 125;
    private static final int FTP_OPENDATA = 150;
    private static final int FTP_OK = 200;
    private static final int FTP_USERREADY = 220;
    private static final int FTP_TRANSFEROK = 226;
    private static final int FTP_LOGGEDIN = 230;
    private static final int FTP_FILEOK = 250;
    private static final int FTP_PASWD = 331;
    private static final int FTP_NOTFOUND = 550;
    private Socket controlSocket;
    private Socket dataSocket;
    private ServerSocket acceptSocket;
    private InputStream ctrlInput;
    private InputStream inputStream;
    private OutputStream ctrlOutput;
    private int dataPort;
    private String username = "anonymous"; 
    private String password = ""; 
    private String replyCode;
    private String hostName;
    private Proxy proxy;
    private Proxy currentProxy;
    private URI uri;
    protected FtpURLConnection(URL url) {
        super(url);
        hostName = url.getHost();
        String parse = url.getUserInfo();
        if (parse != null) {
            int split = parse.indexOf(':');
            if (split >= 0) {
                username = parse.substring(0, split);
                password = parse.substring(split + 1);
            } else {
                username = parse;
            }
        }
        uri = null;
        try {
            uri = url.toURI();
        } catch (URISyntaxException e) {
        }
    }
    protected FtpURLConnection(URL url, Proxy proxy) {
        this(url);
        this.proxy = proxy;
    }
    private void cd() throws IOException {
        int idx = url.getFile().lastIndexOf('/');
        if (idx > 0) {
            String dir = url.getFile().substring(0, idx);
            write("CWD " + dir + "\r\n"); 
            int reply = getReply();
            if (reply != FTP_FILEOK && dir.length() > 0 && dir.charAt(0) == '/') {
                write("CWD " + dir.substring(1) + "\r\n"); 
                reply = getReply();
            }
            if (reply != FTP_FILEOK) {
                throw new IOException(Msg.getString("K0094")); 
            }
        }
    }
    @Override
    public void connect() throws IOException {
        List<Proxy> proxyList = null;
        if (null != proxy) {
            proxyList = new ArrayList<Proxy>(1);
            proxyList.add(proxy);
        } else {
            proxyList = NetUtil.getProxyList(uri);
        }
        if (null == proxyList) {
            currentProxy = null;
            connectInternal();
        } else {
            ProxySelector selector = ProxySelector.getDefault();
            Iterator<Proxy> iter = proxyList.iterator();
            boolean connectOK = false;
            String failureReason = ""; 
            while (iter.hasNext() && !connectOK) {
                currentProxy = iter.next();
                try {
                    connectInternal();
                    connectOK = true;
                } catch (IOException ioe) {
                    failureReason = ioe.getLocalizedMessage();
                    if (null != selector && Proxy.NO_PROXY != currentProxy) {
                        selector.connectFailed(uri, currentProxy.address(), ioe);
                    }
                }
            }
            if (!connectOK) {
                throw new IOException(Msg.getString("K0097", failureReason)); 
            }
        }
    }
    private void connectInternal() throws IOException {
        int port = url.getPort();
        int connectTimeout = getConnectTimeout();
        if (port <= 0) {
            port = FTP_PORT;
        }
        if (null == currentProxy || Proxy.Type.HTTP == currentProxy.type()) {
            controlSocket = new Socket();
        } else {
            controlSocket = new Socket(currentProxy);
        }
        InetSocketAddress addr = new InetSocketAddress(hostName, port);
        controlSocket.connect(addr, connectTimeout);
        connected = true;
        ctrlOutput = controlSocket.getOutputStream();
        ctrlInput = controlSocket.getInputStream();
        login();
        setType();
        if (!getDoInput()) {
            cd();
        }
        try {
            acceptSocket = new ServerSocket(0);
            dataPort = acceptSocket.getLocalPort();
            port();
            if (connectTimeout == 0) {
                connectTimeout = 3000;
            }
            acceptSocket.setSoTimeout(getConnectTimeout());
            if (getDoInput()) {
                getFile();
            } else {
                sendFile();
            }
            dataSocket = acceptSocket.accept();
            dataSocket.setSoTimeout(getReadTimeout());
            acceptSocket.close();
        } catch (InterruptedIOException e) {
            throw new IOException(Msg.getString("K0095")); 
        }
        if (getDoInput()) {
            inputStream = new FtpURLInputStream(
                    new BufferedInputStream(dataSocket.getInputStream(), 8192),
                    controlSocket);
        }
    }
    @Override
    public String getContentType() {
        String result = guessContentTypeFromName(url.getFile());
        if (result == null) {
            return MimeTable.UNKNOWN;
        }
        return result;
    }
    private void getFile() throws IOException {
        int reply;
        String file = url.getFile();
        write("RETR " + file + "\r\n"); 
        reply = getReply();
        if (reply == FTP_NOTFOUND && file.length() > 0 && file.charAt(0) == '/') {
            write("RETR " + file.substring(1) + "\r\n"); 
            reply = getReply();
        }
        if (!(reply == FTP_OPENDATA || reply == FTP_TRANSFEROK)) {
            throw new FileNotFoundException(Msg.getString("K0096", reply)); 
        }
    }
    @Override
    public InputStream getInputStream() throws IOException {
        if (!connected) {
            connect();
        }
        return inputStream;
    }
    @Override
    public Permission getPermission() throws IOException {
        int port = url.getPort();
        if (port <= 0) {
            port = FTP_PORT;
        }
        return new SocketPermission(hostName + ":" + port, "connect, resolve"); 
    }
    @Override
    public OutputStream getOutputStream() throws IOException {
        if (!connected) {
            connect();
        }
        return dataSocket.getOutputStream();
    }
    private int getReply() throws IOException {
        byte[] code = new byte[3];
        for (int i = 0; i < code.length; i++) {
            final int tmp = ctrlInput.read();
            if (tmp == -1) {
                throw new EOFException();
            }
            code[i] = (byte) tmp;
        }
        replyCode = new String(code, "ISO8859_1"); 
        boolean multiline = false;
        if (ctrlInput.read() == '-') {
            multiline = true;
        }
        readLine(); 
        if (multiline) {
            while (readMultiLine()) {
            }
        }
        try {
            return Integer.parseInt(replyCode);
        } catch (NumberFormatException e) {
            throw (IOException)(new IOException("reply code is invalid").initCause(e));
        }
    }
    private void login() throws IOException {
        int reply;
        reply = getReply();
        if (reply == FTP_USERREADY) {
        } else {
            throw new IOException(Msg.getString("K0097", url.getHost())); 
        }
        write("USER " + username + "\r\n"); 
        reply = getReply();
        if (reply == FTP_PASWD || reply == FTP_LOGGEDIN) {
        } else {
            throw new IOException(Msg.getString("K0098", url.getHost())); 
        }
        if (reply == FTP_PASWD) {
            write("PASS " + password + "\r\n"); 
            reply = getReply();
            if (!(reply == FTP_OK || reply == FTP_USERREADY || reply == FTP_LOGGEDIN)) {
                throw new IOException(Msg.getString("K0098", url.getHost())); 
            }
        }
    }
    private void port() throws IOException {
        write("PORT " 
                + controlSocket.getLocalAddress().getHostAddress().replace('.',
                        ',') + ',' + (dataPort >> 8) + ','
                + (dataPort & 255)
                + "\r\n"); 
        if (getReply() != FTP_OK) {
            throw new IOException(Msg.getString("K0099")); 
        }
    }
    private String readLine() throws IOException {
        StringBuilder sb = new StringBuilder();
        int c;
        while ((c = ctrlInput.read()) != '\n') {
            sb.append((char) c);
        }
        return sb.toString();
    }
    private boolean readMultiLine() throws IOException {
        String line = readLine();
        if (line.length() < 4) {
            return true;
        }
        if (line.substring(0, 3).equals(replyCode)
                && (line.charAt(3) == (char) 32)) {
            return false;
        }
        return true;
    }
    private void sendFile() throws IOException {
        int reply;
        write("STOR " 
                + url.getFile().substring(url.getFile().lastIndexOf('/') + 1,
                        url.getFile().length()) + "\r\n"); 
        reply = getReply();
        if (!(reply == FTP_OPENDATA || reply == FTP_OK || reply == FTP_DATAOPEN)) {
            throw new IOException(Msg.getString("K009a")); 
        }
    }
    @Override
    public void setDoInput(boolean newValue) {
        if (connected) {
            throw new IllegalAccessError();
        }
        this.doInput = newValue;
        this.doOutput = !newValue;
    }
    @Override
    public void setDoOutput(boolean newValue) {
        if (connected) {
            throw new IllegalAccessError();
        }
        this.doOutput = newValue;
        this.doInput = !newValue;
    }
    private void setType() throws IOException {
        write("TYPE I\r\n"); 
        if (getReply() != FTP_OK) {
            throw new IOException(Msg.getString("K009b")); 
        }
    }
    private void write(String command) throws IOException {
        ctrlOutput.write(command.getBytes("ISO8859_1")); 
    }
}
