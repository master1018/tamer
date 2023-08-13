final class GenericIPMreq {
    private InetAddress multiaddr;
    @SuppressWarnings("unused")
    private InetAddress interfaceAddr;
    @SuppressWarnings("unused")
    private boolean isIPV6Address;
    private int interfaceIdx;
    private static final Field networkInterfaceIndexField = getNetworkInterfaceField();
    private static Field getNetworkInterfaceField() {
        return AccessController.doPrivileged(new PrivilegedAction<Field>() {
            public Field run() {
                try {
                    Field field = NetworkInterface.class.getDeclaredField("interfaceIndex");
                    field.setAccessible(true);
                    return field;
                } catch (NoSuchFieldException e) {
                    throw new AssertionError(e);
                }
            }
        });
    }
    GenericIPMreq(InetAddress addr) {
        multiaddr = addr;
        interfaceAddr = null;
        interfaceIdx = 0;
        init();
    }
    GenericIPMreq(InetAddress addr, NetworkInterface netInterface) {
        multiaddr = addr;
        if (null != netInterface) {
            try {
                interfaceIdx = networkInterfaceIndexField.getInt(netInterface);
            } catch (IllegalAccessException ex) {
                throw new AssertionError(ex);
            }
            interfaceAddr = null;
            Enumeration<InetAddress> theAddresses = netInterface.getInetAddresses();
            if ((addr instanceof Inet4Address) && (theAddresses != null)) {
                boolean found = false;
                while ((theAddresses.hasMoreElements()) && (found != true)) {
                    InetAddress theAddress = theAddresses.nextElement();
                    if (theAddress instanceof Inet4Address) {
                        interfaceAddr = theAddress;
                        found = true;
                    }
                }
            }
        } else {
            interfaceIdx = 0;
            interfaceAddr = null;
        }
        init();
    }
    private void init() {
        isIPV6Address = ((multiaddr != null) && (multiaddr instanceof Inet6Address));
    }
}
