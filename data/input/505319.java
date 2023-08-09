@TestTargetClass(java.net.Socket.class) 
public abstract class SocketTestCase extends junit.framework.TestCase {
    public static final int SO_MULTICAST = 0;
    public static final int SO_MULTICAST_INTERFACE = 1;
    public static final int SO_LINGER = 2;
    public static final int SO_RCVBUF = 3;
    public static final int SO_TIMEOUT = 4;
    public static final int SO_SNDBUF = 5;
    public static final int TCP_NODELAY = 6;
    public static final int SO_KEEPALIVE = 7;
    public static final int SO_REUSEADDR = 8;
    public static final int SO_OOBINLINE = 9;
    public static final int IP_TOS = 10;
    public static final int SO_BROADCAST = 11;
    public static final int SO_USELOOPBACK = 12;
    public static final String LINUX = "Linux";
    private static final String osDoesNotSupportOperationString = "The socket does not support the operation";
    private static final String osDoesNotSupportOptionString = "The socket option is not supported";
    private static final String osDoesNotSupportOptionArgumentString = "The socket option arguments are invalid";
    public SocketTestCase() {
    }
    public SocketTestCase(String name) {
        super(name);
    }
    public boolean getOptionIsSupported(int option) {
        switch (option) {
        case SO_RCVBUF:
        case SO_SNDBUF:
            return true;
        case SO_MULTICAST:
        case SO_MULTICAST_INTERFACE:
        case SO_LINGER:
            return true;
        case TCP_NODELAY:
        case SO_TIMEOUT:
            return true;
        case SO_KEEPALIVE:
        case SO_REUSEADDR:
            return true;
        case SO_OOBINLINE:
            return true;
        case IP_TOS:
            return true;
        case SO_BROADCAST:
            return true;
        case SO_USELOOPBACK:
            return true;
        }
        return false;
    }
    public void handleException(Exception e, int option) {
        if (!getOptionIsSupported(option)) {
            String message = e.getMessage();
            if (message != null
                    && (message.equals(osDoesNotSupportOperationString)
                            || message.equals(osDoesNotSupportOptionString) || message
                            .equals(osDoesNotSupportOptionArgumentString))) {
            } else {
                fail("Threw \""
                        + e
                        + "\" instead of correct exception for unsupported socket option: "
                        + getSocketOptionString(option));
            }
        } else {
            fail("Exception during test : " + e.getMessage());
        }
    }
    public void ensureExceptionThrownIfOptionIsUnsupportedOnOS(int option) {
        if (!getOptionIsSupported(option)) {
            String platform = System.getProperty("os.name");
            String version = System.getProperty("os.version");
            fail("Failed to throw exception for unsupported socket option: "
                    + getSocketOptionString(option));
        }
    }
    private String getSocketOptionString(int option) {
        switch (option) {
        case SO_MULTICAST:
            return "Multicast";
        case SO_LINGER:
            return "Linger";
        case SO_RCVBUF:
            return "Receive buffer size";
        case SO_TIMEOUT:
            return "Socket timeout";
        case SO_SNDBUF:
            return "Send buffer size";
        case TCP_NODELAY:
            return "TCP no delay";
        case SO_KEEPALIVE:
            return "Keepalive";
        case SO_REUSEADDR:
            return "Reuse address";
        case SO_OOBINLINE:
            return "out of band data inline";
        case IP_TOS:
            return "Traffic class";
        case SO_BROADCAST:
            return "broadcast";
        case SO_USELOOPBACK:
            return "loopback";
        }
        return "Unknown socket option";
    }
}
