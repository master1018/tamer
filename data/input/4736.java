public final class NetHooks {
    public static abstract class Provider {
        protected Provider() {}
        public abstract void implBeforeTcpBind(FileDescriptor fdObj,
                                               InetAddress address,
                                               int port)
            throws IOException;
        public abstract void implBeforeTcpConnect(FileDescriptor fdObj,
                                                 InetAddress address,
                                                 int port)
            throws IOException;
    }
    private static final Provider provider = new sun.net.sdp.SdpProvider();
    public static void beforeTcpBind(FileDescriptor fdObj,
                                     InetAddress address,
                                     int port)
        throws IOException
    {
        provider.implBeforeTcpBind(fdObj, address, port);
    }
    public static void beforeTcpConnect(FileDescriptor fdObj,
                                        InetAddress address,
                                        int port)
        throws IOException
    {
        provider.implBeforeTcpConnect(fdObj, address, port);
    }
}
