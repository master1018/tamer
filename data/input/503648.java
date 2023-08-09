public final class MultihomePlainSocketFactory implements SocketFactory {
    private static final
    MultihomePlainSocketFactory DEFAULT_FACTORY = new MultihomePlainSocketFactory();
    public static MultihomePlainSocketFactory getSocketFactory() {
        return DEFAULT_FACTORY;
    }
    private MultihomePlainSocketFactory() {
        super();
    }
    public Socket createSocket() {
        return new Socket();
    }
    public Socket connectSocket(Socket sock, String host, int port, 
                                InetAddress localAddress, int localPort,
                                HttpParams params)
        throws IOException {
        if (host == null) {
            throw new IllegalArgumentException("Target host may not be null.");
        }
        if (params == null) {
            throw new IllegalArgumentException("Parameters may not be null.");
        }
        if (sock == null)
            sock = createSocket();
        if ((localAddress != null) || (localPort > 0)) {
            if (localPort < 0)
                localPort = 0; 
            InetSocketAddress isa =
                new InetSocketAddress(localAddress, localPort);
            sock.bind(isa);
        }
        int timeout = HttpConnectionParams.getConnectionTimeout(params);
        InetAddress[] inetadrs = InetAddress.getAllByName(host);
        List<InetAddress> addresses = new ArrayList<InetAddress>(inetadrs.length);
        addresses.addAll(Arrays.asList(inetadrs));
        Collections.shuffle(addresses);
        IOException lastEx = null;
        for (InetAddress address: addresses) {
            try {
                sock.connect(new InetSocketAddress(address, port), timeout);
                break;
            } catch (SocketTimeoutException ex) {
                throw ex;
            } catch (IOException ex) {
                sock = new Socket();
                lastEx = ex;
            }
        }
        if (lastEx != null) {
            throw lastEx;
        }
        return sock;
    } 
    public final boolean isSecure(Socket sock)
        throws IllegalArgumentException {
        if (sock == null) {
            throw new IllegalArgumentException("Socket may not be null.");
        }
        if (sock.getClass() != Socket.class) {
            throw new IllegalArgumentException
                ("Socket not created by this factory.");
        }
        if (sock.isClosed()) {
            throw new IllegalArgumentException("Socket is closed.");
        }
        return false;
    } 
    @Override
    public boolean equals(Object obj) {
        return (obj == this);
    }
    @Override
    public int hashCode() {
        return PlainSocketFactory.class.hashCode();
    }
}
