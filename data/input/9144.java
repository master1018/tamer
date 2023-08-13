public class XAException extends java.lang.Exception {
    public int errorCode;
    public XAException()
    {
        super();
    }
    public XAException(String s)
    {
        super(s);
    }
    public XAException(int errcode)
    {
        super();
        errorCode = errcode;
    }
    public final static int XA_RBBASE = 100;
    public final static int XA_RBROLLBACK = XA_RBBASE;
    public final static int XA_RBCOMMFAIL = XA_RBBASE + 1;
    public final static int XA_RBDEADLOCK = XA_RBBASE + 2;
    public final static int XA_RBINTEGRITY = XA_RBBASE + 3;
    public final static int XA_RBOTHER = XA_RBBASE + 4;
    public final static int XA_RBPROTO = XA_RBBASE + 5;
    public final static int XA_RBTIMEOUT = XA_RBBASE + 6;
    public final static int XA_RBTRANSIENT = XA_RBBASE + 7;
    public final static int XA_RBEND = XA_RBTRANSIENT;
    public final static int XA_NOMIGRATE = 9;
    public final static int XA_HEURHAZ = 8;
    public final static int XA_HEURCOM = 7;
    public final static int XA_HEURRB = 6;
    public final static int XA_HEURMIX = 5;
    public final static int XA_RETRY = 4;
    public final static int XA_RDONLY = 3;
    public final static int XAER_ASYNC = -2;
    public final static int XAER_RMERR = -3;
    public final static int XAER_NOTA = -4;
    public final static int XAER_INVAL = -5;
    public final static int XAER_PROTO = -6;
    public final static int XAER_RMFAIL = -7;
    public final static int XAER_DUPID = -8;
    public final static int XAER_OUTSIDE = -9;
}
