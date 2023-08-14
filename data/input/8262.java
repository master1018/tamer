public final class StubInvocationHandlerImpl implements LinkedInvocationHandler
{
    private transient PresentationManager.ClassData classData ;
    private transient PresentationManager pm ;
    private transient org.omg.CORBA.Object stub ;
    private transient Proxy self ;
    public void setProxy( Proxy self )
    {
        this.self = self ;
    }
    public Proxy getProxy()
    {
        return self ;
    }
    public StubInvocationHandlerImpl( PresentationManager pm,
        PresentationManager.ClassData classData, org.omg.CORBA.Object stub )
    {
        SecurityManager s = System.getSecurityManager();
        if (s != null) {
            s.checkPermission(new DynamicAccessPermission("access"));
        }
        this.classData = classData ;
        this.pm = pm ;
        this.stub = stub ;
    }
    private boolean isLocal()
    {
        boolean result = false ;
        Delegate delegate = StubAdapter.getDelegate( stub ) ;
        if (delegate instanceof CorbaClientDelegate) {
            CorbaClientDelegate cdel = (CorbaClientDelegate)delegate ;
            ContactInfoList cil = cdel.getContactInfoList() ;
            if (cil instanceof CorbaContactInfoList) {
                CorbaContactInfoList ccil = (CorbaContactInfoList)cil ;
                LocalClientRequestDispatcher lcrd =
                    ccil.getLocalClientRequestDispatcher() ;
                result = lcrd.useLocalInvocation( null ) ;
            }
        }
        return result ;
    }
    public Object invoke( Object proxy, final Method method,
        Object[] args ) throws Throwable
    {
        String giopMethodName = classData.getIDLNameTranslator().
            getIDLName( method )  ;
        DynamicMethodMarshaller dmm =
            pm.getDynamicMethodMarshaller( method ) ;
        Delegate delegate = null ;
        try {
            delegate = StubAdapter.getDelegate( stub ) ;
        } catch (SystemException ex) {
            throw Util.mapSystemException(ex) ;
        }
        if (!isLocal()) {
            try {
                org.omg.CORBA_2_3.portable.InputStream in = null ;
                try {
                    org.omg.CORBA_2_3.portable.OutputStream out =
                        (org.omg.CORBA_2_3.portable.OutputStream)
                        delegate.request( stub, giopMethodName, true);
                    dmm.writeArguments( out, args ) ;
                    in = (org.omg.CORBA_2_3.portable.InputStream)
                        delegate.invoke( stub, out);
                    return dmm.readResult( in ) ;
                } catch (ApplicationException ex) {
                    throw dmm.readException( ex ) ;
                } catch (RemarshalException ex) {
                    return invoke( proxy, method, args ) ;
                } finally {
                    delegate.releaseReply( stub, in );
                }
            } catch (SystemException ex) {
                throw Util.mapSystemException(ex) ;
            }
        } else {
            ORB orb = (ORB)delegate.orb( stub ) ;
            ServantObject so = delegate.servant_preinvoke( stub, giopMethodName,
                method.getDeclaringClass() );
            if (so == null) {
                return invoke( stub, method, args ) ;
            }
            try {
                Object[] copies = dmm.copyArguments( args, orb ) ;
                if (!method.isAccessible()) {
                    AccessController.doPrivileged(new PrivilegedAction() {
                        public Object run() {
                            method.setAccessible( true ) ;
                            return null ;
                        }
                    } ) ;
                }
                Object result = method.invoke( so.servant, copies ) ;
                return dmm.copyResult( result, orb ) ;
            } catch (InvocationTargetException ex) {
                Throwable mex = ex.getCause() ;
                Throwable exCopy = (Throwable)Util.copyObject(mex,orb);
                if (dmm.isDeclaredException( exCopy ))
                    throw exCopy ;
                else
                    throw Util.wrapException(exCopy);
            } catch (Throwable thr) {
                if (thr instanceof ThreadDeath)
                    throw (ThreadDeath)thr ;
                throw Util.wrapException( thr ) ;
            } finally {
                delegate.servant_postinvoke( stub, so);
            }
        }
    }
}
