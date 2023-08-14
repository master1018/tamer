public class PortableRemoteObject
        implements javax.rmi.CORBA.PortableRemoteObjectDelegate {
    public void exportObject(Remote obj)
        throws RemoteException {
        if (obj == null) {
            throw new NullPointerException("invalid argument");
        }
        if (Util.getTie(obj) != null) {
            throw new ExportException (obj.getClass().getName() + " already exported");
        }
        Tie theTie = Utility.loadTie(obj);
        if (theTie != null) {
            Util.registerTarget(theTie,obj);
        } else {
            UnicastRemoteObject.exportObject(obj);
        }
    }
    public Remote toStub (Remote obj)
        throws NoSuchObjectException
    {
        Remote result = null;
        if (obj == null) {
            throw new NullPointerException("invalid argument");
        }
        if (StubAdapter.isStub( obj )) {
            return obj;
        }
        if (obj instanceof java.rmi.server.RemoteStub) {
            return obj;
        }
        Tie theTie = Util.getTie(obj);
        if (theTie != null) {
            result = Utility.loadStub(theTie,null,null,true);
        } else {
            if (Utility.loadTie(obj) == null) {
                result = java.rmi.server.RemoteObject.toStub(obj);
            }
        }
        if (result == null) {
            throw new NoSuchObjectException("object not exported");
        }
        return result;
    }
    public void unexportObject(Remote obj)
        throws NoSuchObjectException {
        if (obj == null) {
            throw new NullPointerException("invalid argument");
        }
        if (StubAdapter.isStub(obj) ||
            obj instanceof java.rmi.server.RemoteStub) {
            throw new NoSuchObjectException(
                "Can only unexport a server object.");
        }
        Tie theTie = Util.getTie(obj);
        if (theTie != null) {
            Util.unexportObject(obj);
        } else {
            if (Utility.loadTie(obj) == null) {
                UnicastRemoteObject.unexportObject(obj,true);
            } else {
                throw new NoSuchObjectException("Object not exported.");
            }
        }
    }
    public java.lang.Object narrow ( java.lang.Object narrowFrom,
        java.lang.Class narrowTo) throws ClassCastException
    {
        java.lang.Object result = null;
        if (narrowFrom == null)
            return null;
        if (narrowTo == null)
            throw new NullPointerException("invalid argument");
        try {
            if (narrowTo.isAssignableFrom(narrowFrom.getClass()))
                return narrowFrom;
            if (narrowTo.isInterface() &&
                narrowTo != java.io.Serializable.class &&
                narrowTo != java.io.Externalizable.class) {
                org.omg.CORBA.Object narrowObj
                    = (org.omg.CORBA.Object) narrowFrom;
                String id = RepositoryId.createForAnyType(narrowTo);
                if (narrowObj._is_a(id)) {
                    return Utility.loadStub(narrowObj,narrowTo);
                } else {
                    throw new ClassCastException( "Object is not of remote type " +
                        narrowTo.getName() ) ;
                }
            } else {
                throw new ClassCastException( "Class " + narrowTo.getName() +
                    " is not a valid remote interface" ) ;
            }
        } catch(Exception error) {
            ClassCastException cce = new ClassCastException() ;
            cce.initCause( error ) ;
            throw cce ;
        }
    }
    public void connect (Remote target, Remote source)
        throws RemoteException
    {
        if (target == null || source == null) {
            throw new NullPointerException("invalid argument");
        }
        ORB orb = null;
        try {
            if (StubAdapter.isStub( source )) {
                orb = StubAdapter.getORB( source ) ;
            } else {
                Tie tie = Util.getTie(source);
                if (tie == null) {
                } else {
                    orb = tie.orb();
                }
            }
        } catch (SystemException e) {
            throw new RemoteException("'source' object not connected", e );
        }
        boolean targetIsIIOP = false ;
        Tie targetTie = null;
        if (StubAdapter.isStub(target)) {
            targetIsIIOP = true;
        } else {
            targetTie = Util.getTie(target);
            if (targetTie != null) {
                targetIsIIOP = true;
            } else {
            }
        }
        if (!targetIsIIOP) {
            if (orb != null) {
                throw new RemoteException(
                    "'source' object exported to IIOP, 'target' is JRMP");
            }
        } else {
            if (orb == null) {
                throw new RemoteException(
                    "'source' object is JRMP, 'target' is IIOP");
            }
            try {
                if (targetTie != null) {
                    try {
                        ORB existingOrb = targetTie.orb();
                        if (existingOrb == orb) {
                            return;
                        } else {
                            throw new RemoteException(
                                "'target' object was already connected");
                        }
                    } catch (SystemException e) {}
                    targetTie.orb(orb);
                } else {
                    StubAdapter.connect( target, orb ) ;
                }
            } catch (SystemException e) {
                throw new RemoteException(
                    "'target' object was already connected", e );
            }
        }
    }
}
