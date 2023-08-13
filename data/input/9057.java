public class ServiceContextData {
    private void dprint( String msg )
    {
        ORBUtility.dprint( this, msg ) ;
    }
    private void throwBadParam( String msg, Throwable exc )
    {
        BAD_PARAM error = new BAD_PARAM( msg ) ;
        if (exc != null)
            error.initCause( exc ) ;
        throw error ;
    }
    public ServiceContextData( Class cls )
    {
        if (ORB.ORBInitDebug)
            dprint( "ServiceContextData constructor called for class " + cls ) ;
        scClass = cls ;
        try {
            if (ORB.ORBInitDebug)
                dprint( "Finding constructor for " + cls ) ;
            Class[] args = new Class[2] ;
            args[0] = InputStream.class ;
            args[1] = GIOPVersion.class;
            try {
                scConstructor = cls.getConstructor( args ) ;
            } catch (NoSuchMethodException nsme) {
                throwBadParam( "Class does not have an InputStream constructor", nsme ) ;
            }
            if (ORB.ORBInitDebug)
                dprint( "Finding SERVICE_CONTEXT_ID field in " + cls ) ;
            Field fld = null ;
            try {
                fld = cls.getField( "SERVICE_CONTEXT_ID" ) ;
            } catch (NoSuchFieldException nsfe) {
                throwBadParam( "Class does not have a SERVICE_CONTEXT_ID member", nsfe ) ;
            } catch (SecurityException se) {
                throwBadParam( "Could not access SERVICE_CONTEXT_ID member", se ) ;
            }
            if (ORB.ORBInitDebug)
                dprint( "Checking modifiers of SERVICE_CONTEXT_ID field in " + cls ) ;
            int mod = fld.getModifiers() ;
            if (!Modifier.isPublic(mod) || !Modifier.isStatic(mod) ||
                !Modifier.isFinal(mod) )
                throwBadParam( "SERVICE_CONTEXT_ID field is not public static final", null ) ;
            if (ORB.ORBInitDebug)
                dprint( "Getting value of SERVICE_CONTEXT_ID in " + cls ) ;
            try {
                scId = fld.getInt( null ) ;
            } catch (IllegalArgumentException iae) {
                throwBadParam( "SERVICE_CONTEXT_ID not convertible to int", iae ) ;
            } catch (IllegalAccessException iae2) {
                throwBadParam( "Could not access value of SERVICE_CONTEXT_ID", iae2 ) ;
            }
        } catch (BAD_PARAM nssc) {
            if (ORB.ORBInitDebug)
                dprint( "Exception in ServiceContextData constructor: " + nssc ) ;
            throw nssc ;
        } catch (Throwable thr) {
            if (ORB.ORBInitDebug)
                dprint( "Unexpected Exception in ServiceContextData constructor: " +
                        thr ) ;
        }
        if (ORB.ORBInitDebug)
            dprint( "ServiceContextData constructor completed" ) ;
    }
    public ServiceContext makeServiceContext(InputStream is, GIOPVersion gv)
    {
        Object[] args = new Object[2];
        args[0] = is ;
        args[1] = gv;
        ServiceContext sc = null ;
        try {
            sc = (ServiceContext)(scConstructor.newInstance( args )) ;
        } catch (IllegalArgumentException iae) {
            throwBadParam( "InputStream constructor argument error", iae ) ;
        } catch (IllegalAccessException iae2) {
            throwBadParam( "InputStream constructor argument error", iae2 ) ;
        } catch (InstantiationException ie) {
            throwBadParam( "InputStream constructor called for abstract class", ie ) ;
        } catch (InvocationTargetException ite) {
            throwBadParam( "InputStream constructor threw exception " +
                ite.getTargetException(), ite ) ;
        }
        return sc ;
    }
    int getId()
    {
        return scId ;
    }
    public String toString()
    {
        return "ServiceContextData[ scClass=" + scClass + " scConstructor=" +
            scConstructor + " scId=" + scId + " ]" ;
    }
    private Class       scClass ;
    private Constructor scConstructor ;
    private int         scId ;
}
