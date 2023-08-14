public final class ReflectiveTie extends Servant implements Tie
{
    private Remote target = null ;
    private PresentationManager pm ;
    private PresentationManager.ClassData classData = null ;
    private ORBUtilSystemException wrapper = null ;
    public ReflectiveTie( PresentationManager pm, ORBUtilSystemException wrapper )
    {
        SecurityManager s = System.getSecurityManager();
        if (s != null) {
            s.checkPermission(new DynamicAccessPermission("access"));
        }
        this.pm = pm ;
        this.wrapper = wrapper ;
    }
    public String[] _all_interfaces(org.omg.PortableServer.POA poa,
        byte[] objectId)
    {
        return classData.getTypeIds() ;
    }
    public void setTarget(Remote target)
    {
        this.target = target;
        if (target == null) {
            classData = null ;
        } else {
            Class targetClass = target.getClass() ;
            classData = pm.getClassData( targetClass ) ;
        }
    }
    public Remote getTarget()
    {
        return target;
    }
    public org.omg.CORBA.Object thisObject()
    {
        return _this_object();
    }
    public void deactivate()
    {
        try{
            _poa().deactivate_object(_poa().servant_to_id(this));
        } catch (org.omg.PortableServer.POAPackage.WrongPolicy exception){
        } catch (org.omg.PortableServer.POAPackage.ObjectNotActive exception){
        } catch (org.omg.PortableServer.POAPackage.ServantNotActive exception){
        }
    }
    public org.omg.CORBA.ORB orb() {
        return _orb();
    }
    public void orb(org.omg.CORBA.ORB orb) {
        try {
            ORB myORB = (ORB)orb ;
            ((org.omg.CORBA_2_3.ORB)orb).set_delegate(this);
        } catch (ClassCastException e) {
            throw wrapper.badOrbForServant( e ) ;
        }
    }
    public org.omg.CORBA.portable.OutputStream  _invoke(String method,
        org.omg.CORBA.portable.InputStream _in, ResponseHandler reply)
    {
        Method javaMethod = null ;
        DynamicMethodMarshaller dmm = null;
        try {
            InputStream in = (InputStream) _in;
            javaMethod = classData.getIDLNameTranslator().getMethod( method ) ;
            if (javaMethod == null)
                throw wrapper.methodNotFoundInTie( method,
                    target.getClass().getName() ) ;
            dmm = pm.getDynamicMethodMarshaller( javaMethod ) ;
            Object[] args = dmm.readArguments( in ) ;
            Object result = javaMethod.invoke( target, args ) ;
            OutputStream os = (OutputStream)reply.createReply() ;
            dmm.writeResult( os, result ) ;
            return os ;
        } catch (IllegalAccessException ex) {
            throw wrapper.invocationErrorInReflectiveTie( ex,
                javaMethod.getName(),
                    javaMethod.getDeclaringClass().getName() ) ;
        } catch (IllegalArgumentException ex) {
            throw wrapper.invocationErrorInReflectiveTie( ex,
                javaMethod.getName(),
                    javaMethod.getDeclaringClass().getName() ) ;
        } catch (InvocationTargetException ex) {
            Throwable thr = ex.getCause() ;
            if (thr instanceof SystemException)
                throw (SystemException)thr ;
            else if ((thr instanceof Exception) &&
                dmm.isDeclaredException( thr )) {
                OutputStream os = (OutputStream)reply.createExceptionReply() ;
                dmm.writeException( os, (Exception)thr ) ;
                return os ;
            } else
                throw new UnknownException( thr ) ;
        }
    }
}
