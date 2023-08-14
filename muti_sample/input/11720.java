public abstract class FtpClient implements java.io.Closeable {
    private static final int FTP_PORT = 21;
    public static enum TransferType {
        ASCII, BINARY, EBCDIC
    };
    public static final int defaultPort() {
        return FTP_PORT;
    }
    protected FtpClient() {
    }
    public static FtpClient create() {
        FtpClientProvider provider = FtpClientProvider.provider();
        return provider.createFtpClient();
    }
    public static FtpClient create(InetSocketAddress dest) throws FtpProtocolException, IOException {
        FtpClient client = create();
        if (dest != null) {
            client.connect(dest);
        }
        return client;
    }
    public static FtpClient create(String dest) throws FtpProtocolException, IOException {
        return create(new InetSocketAddress(dest, FTP_PORT));
    }
    public abstract FtpClient enablePassiveMode(boolean passive);
    public abstract boolean isPassiveModeEnabled();
    public abstract FtpClient setConnectTimeout(int timeout);
    public abstract int getConnectTimeout();
    public abstract FtpClient setReadTimeout(int timeout);
    public abstract int getReadTimeout();
    public abstract FtpClient setProxy(Proxy p);
    public abstract Proxy getProxy();
    public abstract boolean isConnected();
    public abstract FtpClient connect(SocketAddress dest) throws FtpProtocolException, IOException;
    public abstract FtpClient connect(SocketAddress dest, int timeout) throws FtpProtocolException, IOException;
    public abstract SocketAddress getServerAddress();
    public abstract FtpClient login(String user, char[] password) throws FtpProtocolException, IOException;
    public abstract FtpClient login(String user, char[] password, String account) throws FtpProtocolException, IOException;
    public abstract void close() throws IOException;
    public abstract boolean isLoggedIn();
    public abstract FtpClient changeDirectory(String remoteDirectory) throws FtpProtocolException, IOException;
    public abstract FtpClient changeToParentDirectory() throws FtpProtocolException, IOException;
    public abstract String getWorkingDirectory() throws FtpProtocolException, IOException;
    public abstract FtpClient setRestartOffset(long offset);
    public abstract FtpClient getFile(String name, OutputStream local) throws FtpProtocolException, IOException;
    public abstract InputStream getFileStream(String name) throws FtpProtocolException, IOException;
    public OutputStream putFileStream(String name) throws FtpProtocolException, IOException {
        return putFileStream(name, false);
    }
    public abstract OutputStream putFileStream(String name, boolean unique) throws FtpProtocolException, IOException;
    public FtpClient putFile(String name, InputStream local) throws FtpProtocolException, IOException {
        return putFile(name, local, false);
    }
    public abstract FtpClient putFile(String name, InputStream local, boolean unique) throws FtpProtocolException, IOException;
    public abstract FtpClient appendFile(String name, InputStream local) throws FtpProtocolException, IOException;
    public abstract FtpClient rename(String from, String to) throws FtpProtocolException, IOException;
    public abstract FtpClient deleteFile(String name) throws FtpProtocolException, IOException;
    public abstract FtpClient makeDirectory(String name) throws FtpProtocolException, IOException;
    public abstract FtpClient removeDirectory(String name) throws FtpProtocolException, IOException;
    public abstract FtpClient noop() throws FtpProtocolException, IOException;
    public abstract String getStatus(String name) throws FtpProtocolException, IOException;
    public abstract List<String> getFeatures() throws FtpProtocolException, IOException;
    public abstract FtpClient abort() throws FtpProtocolException, IOException;
    public abstract FtpClient completePending() throws FtpProtocolException, IOException;
    public abstract FtpClient reInit() throws FtpProtocolException, IOException;
    public abstract FtpClient setType(TransferType type) throws FtpProtocolException, IOException;
    public FtpClient setBinaryType() throws FtpProtocolException, IOException {
        setType(TransferType.BINARY);
        return this;
    }
    public FtpClient setAsciiType() throws FtpProtocolException, IOException {
        setType(TransferType.ASCII);
        return this;
    }
    public abstract InputStream list(String path) throws FtpProtocolException, IOException;
    public abstract InputStream nameList(String path) throws FtpProtocolException, IOException;
    public abstract long getSize(String path) throws FtpProtocolException, IOException;
    public abstract Date getLastModified(String path) throws FtpProtocolException, IOException;
    public abstract FtpClient setDirParser(FtpDirParser p);
    public abstract Iterator<FtpDirEntry> listFiles(String path) throws FtpProtocolException, IOException;
    public abstract FtpClient useKerberos() throws FtpProtocolException, IOException;
    public abstract String getWelcomeMsg();
    public abstract FtpReplyCode getLastReplyCode();
    public abstract String getLastResponseString();
    public abstract long getLastTransferSize();
    public abstract String getLastFileName();
    public abstract FtpClient startSecureSession() throws FtpProtocolException, IOException;
    public abstract FtpClient endSecureSession() throws FtpProtocolException, IOException;
    public abstract FtpClient allocate(long size) throws FtpProtocolException, IOException;
    public abstract FtpClient structureMount(String struct) throws FtpProtocolException, IOException;
    public abstract String getSystem() throws FtpProtocolException, IOException;
    public abstract String getHelp(String cmd) throws FtpProtocolException, IOException;
    public abstract FtpClient siteCmd(String cmd) throws FtpProtocolException, IOException;
}
