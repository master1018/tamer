public class DefaultIORToSocketInfoImpl
    implements IORToSocketInfo
{
    public List getSocketInfo(IOR ior)
    {
        SocketInfo socketInfo;
        List result = new ArrayList();
        IIOPProfileTemplate iiopProfileTemplate = (IIOPProfileTemplate)
            ior.getProfile().getTaggedProfileTemplate() ;
        IIOPAddress primary = iiopProfileTemplate.getPrimaryAddress() ;
        String hostname = primary.getHost().toLowerCase();
        int    port     = primary.getPort();
        socketInfo = createSocketInfo(hostname, port);
        result.add(socketInfo);
        Iterator iterator = iiopProfileTemplate.iteratorById(
            TAG_ALTERNATE_IIOP_ADDRESS.value);
        while (iterator.hasNext()) {
            AlternateIIOPAddressComponent alternate =
                (AlternateIIOPAddressComponent) iterator.next();
            hostname = alternate.getAddress().getHost().toLowerCase();
            port     = alternate.getAddress().getPort();
            socketInfo= createSocketInfo(hostname, port);
            result.add(socketInfo);
        }
        return result;
    }
    private SocketInfo createSocketInfo(final String hostname, final int port)
    {
        return new SocketInfo() {
            public String getType() { return SocketInfo.IIOP_CLEAR_TEXT; }
            public String getHost() { return hostname; }
            public int    getPort() { return port; }};
    }
}
