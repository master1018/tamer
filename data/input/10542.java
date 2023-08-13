public final class AccessController {
    private AccessController() { }
    public static native <T> T doPrivileged(PrivilegedAction<T> action);
    public static <T> T doPrivilegedWithCombiner(PrivilegedAction<T> action) {
        DomainCombiner dc = null;
        AccessControlContext acc = getStackAccessControlContext();
        if (acc == null || (dc = acc.getAssignedCombiner()) == null) {
            return AccessController.doPrivileged(action);
        }
        return AccessController.doPrivileged(action, preserveCombiner(dc));
    }
    public static native <T> T doPrivileged(PrivilegedAction<T> action,
                                            AccessControlContext context);
    public static native <T> T
        doPrivileged(PrivilegedExceptionAction<T> action)
        throws PrivilegedActionException;
    public static <T> T doPrivilegedWithCombiner
        (PrivilegedExceptionAction<T> action) throws PrivilegedActionException {
        DomainCombiner dc = null;
        AccessControlContext acc = getStackAccessControlContext();
        if (acc == null || (dc = acc.getAssignedCombiner()) == null) {
            return AccessController.doPrivileged(action);
        }
        return AccessController.doPrivileged(action, preserveCombiner(dc));
    }
    private static AccessControlContext preserveCombiner
                                        (DomainCombiner combiner) {
        final Class callerClass = sun.reflect.Reflection.getCallerClass(3);
        ProtectionDomain callerPd = doPrivileged
            (new PrivilegedAction<ProtectionDomain>() {
            public ProtectionDomain run() {
                return callerClass.getProtectionDomain();
            }
        });
        ProtectionDomain[] pds = new ProtectionDomain[] {callerPd};
        return new AccessControlContext(combiner.combine(pds, null), combiner);
    }
    public static native <T> T
        doPrivileged(PrivilegedExceptionAction<T> action,
                     AccessControlContext context)
        throws PrivilegedActionException;
    private static native AccessControlContext getStackAccessControlContext();
    static native AccessControlContext getInheritedAccessControlContext();
    public static AccessControlContext getContext()
    {
        AccessControlContext acc = getStackAccessControlContext();
        if (acc == null) {
            return new AccessControlContext(null, true);
        } else {
            return acc.optimize();
        }
    }
    public static void checkPermission(Permission perm)
                 throws AccessControlException
    {
        if (perm == null) {
            throw new NullPointerException("permission can't be null");
        }
        AccessControlContext stack = getStackAccessControlContext();
        if (stack == null) {
            Debug debug = AccessControlContext.getDebug();
            boolean dumpDebug = false;
            if (debug != null) {
                dumpDebug = !Debug.isOn("codebase=");
                dumpDebug &= !Debug.isOn("permission=") ||
                    Debug.isOn("permission=" + perm.getClass().getCanonicalName());
            }
            if (dumpDebug && Debug.isOn("stack")) {
                Thread.currentThread().dumpStack();
            }
            if (dumpDebug && Debug.isOn("domain")) {
                debug.println("domain (context is null)");
            }
            if (dumpDebug) {
                debug.println("access allowed "+perm);
            }
            return;
        }
        AccessControlContext acc = stack.optimize();
        acc.checkPermission(perm);
    }
}
