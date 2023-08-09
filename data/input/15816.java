class ObjectStreamClassCorbaExt {
    static final boolean isAbstractInterface(Class cl) {
        if (!cl.isInterface() || 
                java.rmi.Remote.class.isAssignableFrom(cl)) { 
            return false;
        }
        Method[] methods = cl.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Class exceptions[] = methods[i].getExceptionTypes();
            boolean exceptionMatch = false;
            for (int j = 0; (j < exceptions.length) && !exceptionMatch; j++) {
                if ((java.rmi.RemoteException.class == exceptions[j]) ||
                    (java.lang.Throwable.class == exceptions[j]) ||
                    (java.lang.Exception.class == exceptions[j]) ||
                    (java.io.IOException.class == exceptions[j])) {
                    exceptionMatch = true;
                }
            }
            if (!exceptionMatch) {
                return false;
            }
        }
        return true;
    }
    static final boolean isAny(String typeString) {
        int isAny = 0;
        if ( (typeString != null) &&
            (typeString.equals("Ljava/lang/Object;") ||
             typeString.equals("Ljava/io/Serializable;") ||
             typeString.equals("Ljava/io/Externalizable;")) )
                isAny = 1;
        return (isAny==1);
    }
    private static final Method[] getDeclaredMethods(final Class clz) {
        return (Method[]) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return clz.getDeclaredMethods();
            }
        });
    }
}
