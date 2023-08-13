public final class LocalRMIServerSocketFactory implements RMIServerSocketFactory {
    public ServerSocket createServerSocket(int port) throws IOException {
        return new ServerSocket(port) {
            @Override
            public Socket accept() throws IOException {
                final Socket socket = super.accept();
                final InetAddress remoteAddr = socket.getInetAddress();
                final String msg = "The server sockets created using the " +
                       "LocalRMIServerSocketFactory only accept connections " +
                       "from clients running on the host where the RMI " +
                       "remote objects have been exported.";
                if (remoteAddr == null) {
                    String details = "";
                    if (socket.isClosed()) {
                        details = " Socket is closed.";
                    } else if (!socket.isConnected()) {
                        details = " Socket is not connected";
                    }
                    try {
                        socket.close();
                    } catch (Exception ok) {
                    }
                    throw new IOException(msg +
                            " Couldn't determine client address." +
                            details);
                } else if (remoteAddr.isLoopbackAddress()) {
                    return socket;
                }
                Enumeration<NetworkInterface> nis;
                try {
                    nis = NetworkInterface.getNetworkInterfaces();
                } catch (SocketException e) {
                    try {
                        socket.close();
                    } catch (IOException ioe) {
                    }
                    throw new IOException(msg, e);
                }
                while (nis.hasMoreElements()) {
                    NetworkInterface ni = nis.nextElement();
                    Enumeration<InetAddress> addrs = ni.getInetAddresses();
                    while (addrs.hasMoreElements()) {
                        InetAddress localAddr = addrs.nextElement();
                        if (localAddr.equals(remoteAddr)) {
                            return socket;
                        }
                    }
                }
                try {
                    socket.close();
                } catch (IOException ioe) {
                }
                throw new IOException(msg);
            }
        };
    }
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof LocalRMIServerSocketFactory);
    }
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
