class DefaultDatagramSocketImplFactory {
    static Class prefixImplClass = null;
    static {
        String prefix = null;
        try {
            prefix = AccessController.doPrivileged(
                new sun.security.action.GetPropertyAction("impl.prefix", null));
            if (prefix != null)
                prefixImplClass = Class.forName("java.net."+prefix+"DatagramSocketImpl");
        } catch (Exception e) {
            System.err.println("Can't find class: java.net." +
                                prefix +
                                "DatagramSocketImpl: check impl.prefix property");
        }
    }
    static DatagramSocketImpl createDatagramSocketImpl(boolean isMulticast )
        throws SocketException {
        if (prefixImplClass != null) {
            try {
                return (DatagramSocketImpl)prefixImplClass.newInstance();
            } catch (Exception e) {
                throw new SocketException("can't instantiate DatagramSocketImpl");
            }
        } else {
            return new java.net.PlainDatagramSocketImpl();
        }
    }
}
