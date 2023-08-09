public class Util implements javax.rmi.CORBA.UtilDelegate
{
    private static KeepAlive keepAlive = null;
    private static IdentityHashtable exportedServants = new IdentityHashtable();
    private static ValueHandlerImpl valueHandlerSingleton = new ValueHandlerImpl();
    private UtilSystemException utilWrapper = UtilSystemException.get(
                                                  CORBALogDomains.RPC_ENCODING);
    public static Util instance = null;
    public Util() {
        instance = this;
    }
    public void unregisterTargetsForORB(org.omg.CORBA.ORB orb)
    {
        for (Enumeration e = exportedServants.keys(); e.hasMoreElements(); )
        {
            java.lang.Object key = e.nextElement();
            Remote target = (Remote)(key instanceof Tie ? ((Tie)key).getTarget() : key);
            try {
                if (orb == getTie(target).orb()) {
                    try {
                        unexportObject(target);
                    } catch( java.rmi.NoSuchObjectException ex ) {
                    }
                }
            } catch (BAD_OPERATION bad) {
            }
        }
    }
    public RemoteException mapSystemException(SystemException ex)
    {
        if (ex instanceof UnknownException) {
            Throwable orig = ((UnknownException)ex).originalEx;
            if (orig instanceof Error) {
                return new ServerError("Error occurred in server thread",(Error)orig);
            } else if (orig instanceof RemoteException) {
                return new ServerException("RemoteException occurred in server thread",
                    (Exception)orig);
            } else if (orig instanceof RuntimeException) {
                throw (RuntimeException) orig;
            }
        }
        String name = ex.getClass().getName();
        String corbaName = name.substring(name.lastIndexOf('.')+1);
        String status;
        switch (ex.completed.value()) {
            case CompletionStatus._COMPLETED_YES:
                status = "Yes";
                break;
            case CompletionStatus._COMPLETED_NO:
                status = "No";
                break;
            case CompletionStatus._COMPLETED_MAYBE:
            default:
                status = "Maybe";
                break;
        }
        String message = "CORBA " + corbaName + " " + ex.minor + " " + status;
        if (ex instanceof COMM_FAILURE) {
            return new MarshalException(message, ex);
        } else if (ex instanceof INV_OBJREF) {
            RemoteException newEx = new NoSuchObjectException(message);
            newEx.detail = ex;
            return newEx;
        } else if (ex instanceof NO_PERMISSION) {
            return new AccessException(message, ex);
        } else if (ex instanceof MARSHAL) {
            return new MarshalException(message, ex);
        } else if (ex instanceof OBJECT_NOT_EXIST) {
            RemoteException newEx = new NoSuchObjectException(message);
            newEx.detail = ex;
            return newEx;
        } else if (ex instanceof TRANSACTION_REQUIRED) {
            RemoteException newEx = new TransactionRequiredException(message);
            newEx.detail = ex;
            return newEx;
        } else if (ex instanceof TRANSACTION_ROLLEDBACK) {
            RemoteException newEx = new TransactionRolledbackException(message);
            newEx.detail = ex;
            return newEx;
        } else if (ex instanceof INVALID_TRANSACTION) {
            RemoteException newEx = new InvalidTransactionException(message);
            newEx.detail = ex;
            return newEx;
        } else if (ex instanceof BAD_PARAM) {
            Exception inner = ex;
            if (ex.minor == ORBConstants.LEGACY_SUN_NOT_SERIALIZABLE ||
                ex.minor == OMGSystemException.NOT_SERIALIZABLE) {
                if (ex.getMessage() != null)
                    inner = new NotSerializableException(ex.getMessage());
                else
                    inner = new NotSerializableException();
                inner.initCause( ex ) ;
            }
            return new MarshalException(message,inner);
        } else if (ex instanceof ACTIVITY_REQUIRED) {
            try {
                Class cl = ORBClassLoader.loadClass(
                               "javax.activity.ActivityRequiredException");
                Class[] params = new Class[2];
                params[0] = java.lang.String.class;
                params[1] = java.lang.Throwable.class;
                Constructor cr = cl.getConstructor(params);
                Object[] args = new Object[2];
                args[0] = message;
                args[1] = ex;
                return (RemoteException) cr.newInstance(args);
            } catch (Throwable e) {
                utilWrapper.classNotFound(
                              e, "javax.activity.ActivityRequiredException");
            }
        } else if (ex instanceof ACTIVITY_COMPLETED) {
            try {
                Class cl = ORBClassLoader.loadClass(
                               "javax.activity.ActivityCompletedException");
                Class[] params = new Class[2];
                params[0] = java.lang.String.class;
                params[1] = java.lang.Throwable.class;
                Constructor cr = cl.getConstructor(params);
                Object[] args = new Object[2];
                args[0] = message;
                args[1] = ex;
                return (RemoteException) cr.newInstance(args);
              } catch (Throwable e) {
                  utilWrapper.classNotFound(
                                e, "javax.activity.ActivityCompletedException");
              }
        } else if (ex instanceof INVALID_ACTIVITY) {
            try {
                Class cl = ORBClassLoader.loadClass(
                               "javax.activity.InvalidActivityException");
                Class[] params = new Class[2];
                params[0] = java.lang.String.class;
                params[1] = java.lang.Throwable.class;
                Constructor cr = cl.getConstructor(params);
                Object[] args = new Object[2];
                args[0] = message;
                args[1] = ex;
                return (RemoteException) cr.newInstance(args);
              } catch (Throwable e) {
                  utilWrapper.classNotFound(
                                e, "javax.activity.InvalidActivityException");
              }
        }
        return new RemoteException(message, ex);
    }
    public void writeAny( org.omg.CORBA.portable.OutputStream out,
                         java.lang.Object obj)
    {
        org.omg.CORBA.ORB orb = out.orb();
        Any any = orb.create_any();
        java.lang.Object newObj = Utility.autoConnect(obj,orb,false);
        if (newObj instanceof org.omg.CORBA.Object) {
            any.insert_Object((org.omg.CORBA.Object)newObj);
        } else {
            if (newObj == null) {
                any.insert_Value(null, createTypeCodeForNull(orb));
            } else {
                if (newObj instanceof Serializable) {
                    TypeCode tc = createTypeCode((Serializable)newObj, any, orb);
                    if (tc == null)
                        any.insert_Value((Serializable)newObj);
                    else
                        any.insert_Value((Serializable)newObj, tc);
                } else if (newObj instanceof Remote) {
                    ORBUtility.throwNotSerializableForCorba(newObj.getClass().getName());
                } else {
                    ORBUtility.throwNotSerializableForCorba(newObj.getClass().getName());
                }
            }
        }
        out.write_any(any);
    }
    private TypeCode createTypeCode(Serializable obj,
                                    org.omg.CORBA.Any any,
                                    org.omg.CORBA.ORB orb) {
        if (any instanceof com.sun.corba.se.impl.corba.AnyImpl &&
            orb instanceof ORB) {
            com.sun.corba.se.impl.corba.AnyImpl anyImpl
                = (com.sun.corba.se.impl.corba.AnyImpl)any;
            ORB ourORB = (ORB)orb;
            return anyImpl.createTypeCodeForClass(obj.getClass(), ourORB);
        } else
            return null;
    }
    private TypeCode createTypeCodeForNull(org.omg.CORBA.ORB orb)
    {
        if (orb instanceof ORB) {
            ORB ourORB = (ORB)orb;
            if (!ORBVersionFactory.getFOREIGN().equals(ourORB.getORBVersion()) &&
                ORBVersionFactory.getNEWER().compareTo(ourORB.getORBVersion()) > 0) {
                return orb.get_primitive_tc(TCKind.tk_value);
            }
        }
        String abstractBaseID = "IDL:omg.org/CORBA/AbstractBase:1.0";
        return orb.create_abstract_interface_tc(abstractBaseID, "");
    }
    public Object readAny(InputStream in)
    {
        Any any = in.read_any();
        if ( any.type().kind().value() == TCKind._tk_objref )
            return any.extract_Object ();
        else
            return any.extract_Value();
    }
    public void writeRemoteObject(OutputStream out, java.lang.Object obj)
    {
        Object newObj = Utility.autoConnect(obj,out.orb(),false);
        out.write_Object((org.omg.CORBA.Object)newObj);
    }
    public void writeAbstractObject( OutputStream out, java.lang.Object obj )
    {
        Object newObj = Utility.autoConnect(obj,out.orb(),false);
        ((org.omg.CORBA_2_3.portable.OutputStream)out).write_abstract_interface(newObj);
    }
    public void registerTarget(javax.rmi.CORBA.Tie tie, java.rmi.Remote target)
    {
        synchronized (exportedServants) {
            if (lookupTie(target) == null) {
                exportedServants.put(target,tie);
                tie.setTarget(target);
                if (keepAlive == null) {
                    keepAlive = (KeepAlive)AccessController.doPrivileged(new PrivilegedAction() {
                        public java.lang.Object run() {
                            return new KeepAlive();
                        }
                    });
                    keepAlive.start();
                }
            }
        }
    }
    public void unexportObject(java.rmi.Remote target)
        throws java.rmi.NoSuchObjectException
    {
        synchronized (exportedServants) {
            Tie cachedTie = lookupTie(target);
            if (cachedTie != null) {
                exportedServants.remove(target);
                Utility.purgeStubForTie(cachedTie);
                Utility.purgeTieAndServant(cachedTie);
                try {
                    cleanUpTie(cachedTie);
                } catch (BAD_OPERATION e) {
                } catch (org.omg.CORBA.OBJ_ADAPTER e) {
                }
                if (exportedServants.isEmpty()) {
                    keepAlive.quit();
                    keepAlive = null;
                }
            } else {
                throw new java.rmi.NoSuchObjectException("Tie not found" );
            }
        }
    }
    protected void cleanUpTie(Tie cachedTie)
        throws java.rmi.NoSuchObjectException
    {
        cachedTie.setTarget(null);
        cachedTie.deactivate();
    }
    public Tie getTie (Remote target)
    {
        synchronized (exportedServants) {
            return lookupTie(target);
        }
    }
    private static Tie lookupTie (Remote target)
    {
        Tie result = (Tie)exportedServants.get(target);
        if (result == null && target instanceof Tie) {
            if (exportedServants.contains(target)) {
                result = (Tie)target;
            }
        }
        return result;
    }
    public ValueHandler createValueHandler()
    {
        return valueHandlerSingleton;
    }
    public String getCodebase(java.lang.Class clz) {
        return RMIClassLoader.getClassAnnotation(clz);
    }
    public Class loadClass( String className, String remoteCodebase,
        ClassLoader loader) throws ClassNotFoundException
    {
        return JDKBridge.loadClass(className,remoteCodebase,loader);
    }
    public boolean isLocal(javax.rmi.CORBA.Stub stub) throws RemoteException
    {
        boolean result = false ;
        try {
            org.omg.CORBA.portable.Delegate delegate = stub._get_delegate() ;
            if (delegate instanceof CorbaClientDelegate) {
                CorbaClientDelegate cdel = (CorbaClientDelegate)delegate ;
                ContactInfoList cil = cdel.getContactInfoList() ;
                if (cil instanceof CorbaContactInfoList) {
                    CorbaContactInfoList ccil = (CorbaContactInfoList)cil ;
                    LocalClientRequestDispatcher lcs = ccil.getLocalClientRequestDispatcher() ;
                    result = lcs.useLocalInvocation( null ) ;
                }
            } else {
                result = delegate.is_local( stub ) ;
            }
        } catch (SystemException e) {
            throw javax.rmi.CORBA.Util.mapSystemException(e);
        }
        return result ;
    }
    public RemoteException wrapException(Throwable orig)
    {
        if (orig instanceof SystemException) {
            return mapSystemException((SystemException)orig);
        }
        if (orig instanceof Error) {
            return new ServerError("Error occurred in server thread",(Error)orig);
        } else if (orig instanceof RemoteException) {
            return new ServerException("RemoteException occurred in server thread",
                                       (Exception)orig);
        } else if (orig instanceof RuntimeException) {
            throw (RuntimeException) orig;
        }
        if (orig instanceof Exception)
            return new UnexpectedException( orig.toString(), (Exception)orig );
        else
            return new UnexpectedException( orig.toString());
    }
    public Object[] copyObjects (Object[] obj, org.omg.CORBA.ORB orb)
        throws RemoteException
    {
        if (obj == null)
            throw new NullPointerException() ;
        Class compType = obj.getClass().getComponentType() ;
        if (Remote.class.isAssignableFrom( compType ) && !compType.isInterface()) {
            Remote[] result = new Remote[obj.length] ;
            System.arraycopy( (Object)obj, 0, (Object)result, 0, obj.length ) ;
            return (Object[])copyObject( result, orb ) ;
        } else
            return (Object[])copyObject( obj, orb ) ;
    }
    public Object copyObject (Object obj, org.omg.CORBA.ORB orb)
        throws RemoteException
    {
        if (orb instanceof ORB) {
            ORB lorb = (ORB)orb ;
            try {
                try {
                    return lorb.peekInvocationInfo().getCopierFactory().make().copy( obj ) ;
                } catch (java.util.EmptyStackException exc) {
                    CopierManager cm = lorb.getCopierManager() ;
                    ObjectCopier copier = cm.getDefaultObjectCopierFactory().make() ;
                    return copier.copy( obj ) ;
                }
            } catch (ReflectiveCopyException exc) {
                RemoteException rexc = new RemoteException() ;
                rexc.initCause( exc ) ;
                throw rexc ;
            }
        } else {
            org.omg.CORBA_2_3.portable.OutputStream out =
                (org.omg.CORBA_2_3.portable.OutputStream)orb.create_output_stream();
            out.write_value((Serializable)obj);
            org.omg.CORBA_2_3.portable.InputStream in =
                (org.omg.CORBA_2_3.portable.InputStream)out.create_input_stream();
            return in.read_value();
        }
    }
}
class KeepAlive extends Thread
{
    boolean quit = false;
    public KeepAlive ()
    {
        setDaemon(false);
    }
    public synchronized void run ()
    {
        while (!quit) {
            try {
                wait();
            } catch (InterruptedException e) {}
        }
    }
    public synchronized void quit ()
    {
        quit = true;
        notifyAll();
    }
}
