class ServerHandler extends Handler
{
    private boolean     needClientAuth;
    static private int  threadCounter = 0;
    private static synchronized int getCounter ()
        { return ++threadCounter; }
    ServerHandler (SSLSocket sock)
    {
        super ("ServerHandler-" + getCounter ());
        s = sock;
        roleIsClient = false;
    }
    public void setNeedClientAuth (boolean flag)
    {
        needClientAuth = flag;
    }
    public void setReverseRole (boolean flag)
    {
        if (flag)
            roleIsClient = true;
        else
            roleIsClient = false;
    }
}
