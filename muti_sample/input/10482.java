class ClientId {
    final private int version;
    final private String hostname;
    final private int port;
    final private String protocol;
    final private Control[] bindCtls;
    final private OutputStream trace;
    final private String socketFactory;
    final private int myHash;
    final private int ctlHash;
    private SocketFactory factory = null;
    private Method sockComparator = null;
    private boolean isDefaultSockFactory = false;
    final public static boolean debug = false;
    ClientId(int version, String hostname, int port, String protocol,
            Control[] bindCtls, OutputStream trace, String socketFactory) {
        this.version = version;
        this.hostname = hostname.toLowerCase();  
        this.port = port;
        this.protocol = protocol;
        this.bindCtls = (bindCtls != null ? (Control[]) bindCtls.clone() : null);
        this.trace = trace;
        this.socketFactory = socketFactory;
        if ((socketFactory != null) &&
             !socketFactory.equals(LdapCtx.DEFAULT_SSL_FACTORY)) {
            try {
                Class socketFactoryClass = Obj.helper.loadClass(socketFactory);
                Class objClass = Class.forName("java.lang.Object");
                this.sockComparator = socketFactoryClass.getMethod(
                                "compare", new Class[]{objClass, objClass});
                Method getDefault =
                    socketFactoryClass.getMethod("getDefault", new Class[]{});
                this.factory = (SocketFactory) getDefault.invoke(null, new Object[]{});
            } catch (Exception e) {
                if (debug) {
                    System.out.println("ClientId received an exception");
                    e.printStackTrace();
                }
            }
        } else {
             isDefaultSockFactory = true;
        }
        myHash = version + port
            + (trace != null ? trace.hashCode() : 0)
            + (this.hostname != null ? this.hostname.hashCode() : 0)
            + (protocol != null ? protocol.hashCode() : 0)
            + (ctlHash=hashCodeControls(bindCtls));
    }
    public boolean equals(Object obj) {
        if (!(obj instanceof ClientId)) {
            return false;
        }
        ClientId other = (ClientId)obj;
        return myHash == other.myHash
            && version == other.version
            && port == other.port
            && trace == other.trace
            && (hostname == other.hostname 
                || (hostname != null && hostname.equals(other.hostname)))
            && (protocol == other.protocol 
                || (protocol != null && protocol.equals(other.protocol)))
            && ctlHash == other.ctlHash
            && (equalsControls(bindCtls, other.bindCtls))
            && (equalsSockFactory(other));
    }
    public int hashCode() {
        return myHash;
    }
    private static int hashCodeControls(Control[] c) {
        if (c == null) {
            return 0;
        }
        int code = 0;
        for (int i = 0; i < c.length; i++) {
            code = code * 31 + c[i].getID().hashCode();
        }
        return code;
    }
    private static boolean equalsControls(Control[] a, Control[] b) {
        if (a == b) {
            return true;  
        }
        if (a == null || b == null) {
            return false; 
        }
        if (a.length != b.length) {
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            if (!a[i].getID().equals(b[i].getID())
                || a[i].isCritical() != b[i].isCritical()
                || !Arrays.equals(a[i].getEncodedValue(),
                    b[i].getEncodedValue())) {
                return false;
            }
        }
        return true;
    }
    private boolean equalsSockFactory(ClientId other) {
        if (this.isDefaultSockFactory && other.isDefaultSockFactory) {
            return true;
        }
        else if (!other.isDefaultSockFactory) {
             return invokeComparator(other, this);
        } else {
             return invokeComparator(this, other);
        }
    }
    private boolean invokeComparator(ClientId c1, ClientId c2) {
        Object ret;
        try {
            ret = (c1.sockComparator).invoke(
                        c1.factory, c1.socketFactory, c2.socketFactory);
        } catch(Exception e) {
            if (debug) {
                System.out.println("ClientId received an exception");
                e.printStackTrace();
            }
            return false;
        }
        if (((Integer) ret) == 0) {
            return true;
        }
        return false;
    }
    private static String toStringControls(Control[] ctls) {
        if (ctls == null) {
            return "";
        }
        StringBuffer str = new StringBuffer();
        for (int i = 0; i < ctls.length; i++) {
            str.append(ctls[i].getID());
            str.append(' ');
        }
        return str.toString();
    }
    public String toString() {
        return (hostname + ":" + port + ":" +
            (protocol != null ? protocol : "") + ":" +
            toStringControls(bindCtls) + ":" +
            socketFactory);
    }
}
