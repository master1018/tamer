public final class AccessController {
    private AccessController() {
        throw new Error("statics only.");
    }
    private static final WeakHashMap<Thread, ArrayList<AccessControlContext>> contexts = new WeakHashMap<Thread, ArrayList<AccessControlContext>>();
    public static <T> T doPrivileged(PrivilegedAction<T> action) {
        if (action == null) {
            throw new NullPointerException("action can not be null");
        }
        return doPrivilegedImpl(action, null);
    }
    public static <T> T doPrivileged(PrivilegedAction<T> action,
            AccessControlContext context) {
        if (action == null) {
            throw new NullPointerException("action can not be null");
        }
        return doPrivilegedImpl(action, context);
    }
    public static <T> T doPrivileged(PrivilegedExceptionAction<T> action)
            throws PrivilegedActionException {
        if (action == null) {
            throw new NullPointerException("action can not be null");
        }
        return doPrivilegedImpl(action, null);
    }
    public static <T> T doPrivileged(PrivilegedExceptionAction<T> action,
            AccessControlContext context) throws PrivilegedActionException {
        if (action == null) {
            throw new NullPointerException("action can not be null");
        }
        return doPrivilegedImpl(action, context);
    }
    private static <T> T doPrivilegedImpl(PrivilegedExceptionAction<T> action,
            AccessControlContext context) throws PrivilegedActionException {
        Thread currThread = Thread.currentThread();
        ArrayList<AccessControlContext> a = null;
        try {
            if (currThread != null && contexts != null) {
                synchronized (contexts) {
                    a = contexts.get(currThread);
                    if (a == null) {
                        a = new ArrayList<AccessControlContext>();
                        contexts.put(currThread, a);
                    }
                }
                a.add(context);
            }
            return action.run();
        } catch (Exception ex) {
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new PrivilegedActionException(ex);
        } finally {
            if (currThread != null) {
                if (a != null) {
                    a.remove(a.size() - 1);
                }
            }
        }
    }
    private static <T> T doPrivilegedImpl(PrivilegedAction<T> action,
            AccessControlContext context) {
        Thread currThread = Thread.currentThread();
        if (currThread == null || contexts == null) {
            return action.run();
        }
        ArrayList<AccessControlContext> a = null;
        try {
            synchronized (contexts) {
                a = contexts.get(currThread);
                if (a == null) {
                    a = new ArrayList<AccessControlContext>();
                    contexts.put(currThread, a);
                }
            }
            a.add(context);
            return action.run();
        } finally {
            if (a != null) {
                a.remove(a.size() - 1);
            }
        }
    }
    public static void checkPermission(Permission perm)
            throws AccessControlException {
        if (perm == null) {
            throw new NullPointerException("permission can not be null");
        }
        getContext().checkPermission(perm);
    }
    private static native ProtectionDomain[] getStackDomains();
    public static AccessControlContext getContext() {
        ProtectionDomain[] stack = getStackDomains();
        Thread currThread = Thread.currentThread();
        if (currThread == null || contexts == null) {
            return new AccessControlContext(stack);
        }
        ArrayList<AccessControlContext> threadContexts;
        synchronized (contexts) {
            threadContexts = contexts.get(currThread);
        }
        AccessControlContext that;
        if ((threadContexts == null) || (threadContexts.size() == 0)) {
            that = SecurityUtils.getContext(currThread);
        } else {
            that = threadContexts.get(threadContexts.size() - 1);
        }
        if (that != null && that.combiner != null) {
            ProtectionDomain[] assigned = null;
            if (that.context != null && that.context.length != 0) {
                assigned = new ProtectionDomain[that.context.length];
                System.arraycopy(that.context, 0, assigned, 0, assigned.length);
            }
            ProtectionDomain[] allpds = that.combiner.combine(stack, assigned);
            if (allpds == null) {
                allpds = new ProtectionDomain[0];
            }
            return new AccessControlContext(allpds, that.combiner);
        }
        return new AccessControlContext(stack, that);
    }
}
