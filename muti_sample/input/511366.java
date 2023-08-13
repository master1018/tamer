public class SSLSocketFactoryImpl extends SSLSocketFactory {
    public SSLSocketFactoryImpl() {
        super();
    }
    public Socket createSocket(Socket socket, String s, int i, boolean flag) 
                               throws IOException {
        if (socket == null) {
            throw new IOException("incorrect socket");
        }
        if (i < 0 || i > 65535) {
            throw new IOException("incorrect port");
        }
        if (s == null || s.equals("")) {
            throw new UnknownHostException("incorrect host");
        }
        if (!flag) {
            socket = new Socket(s, i);
        } else {
            socket = new Socket(s, i);
            socket.close();
        }
        return socket;
    }
    public String[] getDefaultCipherSuites() {
        return null;
    }
    public String[] getSupportedCipherSuites() {
        return null;
    }
    @Override
    public Socket createSocket(String arg0, int arg1) throws IOException, UnknownHostException {
        return null;
    }
    @Override
    public Socket createSocket(InetAddress arg0, int arg1) throws IOException {
        return null;
    }
    @Override
    public Socket createSocket(String arg0, int arg1, InetAddress arg2, int arg3) throws IOException, UnknownHostException {
        return null;
    }
    @Override
    public Socket createSocket(InetAddress arg0, int arg1, InetAddress arg2, int arg3) throws IOException {
        return null;
    }
}
