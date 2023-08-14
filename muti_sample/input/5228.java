public abstract class RemoteObject implements Remote, java.io.Serializable {
    transient protected RemoteRef ref;
    private static final long serialVersionUID = -3215090123894869218L;
    protected RemoteObject() {
        ref = null;
    }
    protected RemoteObject(RemoteRef newref) {
        ref = newref;
    }
    public RemoteRef getRef() {
        return ref;
    }
    public static Remote toStub(Remote obj) throws NoSuchObjectException {
        if (obj instanceof RemoteStub ||
            (obj != null &&
             Proxy.isProxyClass(obj.getClass()) &&
             Proxy.getInvocationHandler(obj) instanceof
             RemoteObjectInvocationHandler))
        {
            return obj;
        } else {
            return sun.rmi.transport.ObjectTable.getStub(obj);
        }
    }
    public int hashCode() {
        return (ref == null) ? super.hashCode() : ref.remoteHashCode();
    }
    public boolean equals(Object obj) {
        if (obj instanceof RemoteObject) {
            if (ref == null) {
                return obj == this;
            } else {
                return ref.remoteEquals(((RemoteObject)obj).ref);
            }
        } else if (obj != null) {
            return obj.equals(this);
        } else {
            return false;
        }
    }
    public String toString() {
        String classname = Util.getUnqualifiedName(getClass());
        return (ref == null) ? classname :
            classname + "[" + ref.remoteToString() + "]";
    }
    private void writeObject(java.io.ObjectOutputStream out)
        throws java.io.IOException, java.lang.ClassNotFoundException
    {
        if (ref == null) {
            throw new java.rmi.MarshalException("Invalid remote object");
        } else {
            String refClassName = ref.getRefClass(out);
            if (refClassName == null || refClassName.length() == 0) {
                out.writeUTF("");
                out.writeObject(ref);
            } else {
                out.writeUTF(refClassName);
                ref.writeExternal(out);
            }
        }
    }
    private void readObject(java.io.ObjectInputStream in)
        throws java.io.IOException, java.lang.ClassNotFoundException
    {
        String refClassName = in.readUTF();
        if (refClassName == null || refClassName.length() == 0) {
            ref = (RemoteRef) in.readObject();
        } else {
            String internalRefClassName =
                RemoteRef.packagePrefix + "." + refClassName;
            Class refClass = Class.forName(internalRefClassName);
            try {
                ref = (RemoteRef) refClass.newInstance();
            } catch (InstantiationException e) {
                throw new ClassNotFoundException(internalRefClassName, e);
            } catch (IllegalAccessException e) {
                throw new ClassNotFoundException(internalRefClassName, e);
            } catch (ClassCastException e) {
                throw new ClassNotFoundException(internalRefClassName, e);
            }
            ref.readExternal(in);
        }
    }
}
