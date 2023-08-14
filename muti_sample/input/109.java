    Class loadClass(String className, String remoteCodebase, ClassLoader loader)
        throws ClassNotFoundException;
    boolean isLocal(Stub stub) throws RemoteException;
    RemoteException wrapException(Throwable obj);
    Object copyObject(Object obj, ORB orb) throws RemoteException;
    Object[] copyObjects(Object[] obj, ORB orb) throws RemoteException;
}
