class ClientThread extends Handler
    implements HandshakeCompletedListener
{
    private int         port;
    private InetAddress server;
    private SSLSocketFactory factory;
    static private int  threadCounter = 0;
    private static synchronized int getCounter ()
        { return ++threadCounter; }
    ClientThread (int port, SSLContext ctx)
    {
        super ("Client-" + getCounter ());
        roleIsClient = true;
        factory = ctx.getSocketFactory();
        try {
            this.server = InetAddress.getLocalHost ();
            this.port = port;
        } catch (UnknownHostException e) {
            synchronized (out) {
                out.println ("%% " + getName ());
                e.printStackTrace (out);
            }
        }
    }
    ClientThread (InetAddress server, int port, SSLContext ctx)
    {
        super ("Client-" + getCounter ());
        roleIsClient = true;
        factory = ctx.getSocketFactory();
        this.server = server;
        this.port = port;
    }
    public void setReverseRole (boolean flag)
    {
        if (flag)
            roleIsClient = false;
        else
            roleIsClient = true;
    }
    public void run ()
    {
        try {
            s = (SSLSocket) factory.createSocket(server, port);
        } catch (Throwable t) {
            synchronized (out) {
                out.println ("%% " + getName ());
                t.printStackTrace (out);
            }
            return;
        }
        if (basicCipherSuites != null) {
            s.setEnabledCipherSuites (basicCipherSuites);
            if (basicCipherSuites.length == 1)
                System.out.println ("%% " + getName () + " trying "
                        + basicCipherSuites [0]);
        }
        super.run ();
        out.println ("%% " + getName ()
            + (passed () ? ", Passed!" : " ... FAILED"));
    }
}
