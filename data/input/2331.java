public final class AccessControlContext {
    private ProtectionDomain context[];
    private boolean isPrivileged;
    private AccessControlContext privilegedContext;
    private DomainCombiner combiner = null;
    private static boolean debugInit = false;
    private static Debug debug = null;
    static Debug getDebug()
    {
        if (debugInit)
            return debug;
        else {
            if (Policy.isSet()) {
                debug = Debug.getInstance("access");
                debugInit = true;
            }
            return debug;
        }
    }
    public AccessControlContext(ProtectionDomain context[])
    {
        if (context.length == 0) {
            this.context = null;
        } else if (context.length == 1) {
            if (context[0] != null) {
                this.context = context.clone();
            } else {
                this.context = null;
            }
        } else {
            List<ProtectionDomain> v = new ArrayList<>(context.length);
            for (int i =0; i< context.length; i++) {
                if ((context[i] != null) &&  (!v.contains(context[i])))
                    v.add(context[i]);
            }
            if (!v.isEmpty()) {
                this.context = new ProtectionDomain[v.size()];
                this.context = v.toArray(this.context);
            }
        }
    }
    public AccessControlContext(AccessControlContext acc,
                                DomainCombiner combiner) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(SecurityConstants.CREATE_ACC_PERMISSION);
        }
        this.context = acc.context;
        this.combiner = combiner;
    }
    AccessControlContext(ProtectionDomain context[], DomainCombiner combiner) {
        if (context != null) {
            this.context = context.clone();
        }
        this.combiner = combiner;
    }
    AccessControlContext(ProtectionDomain context[],
                                 boolean isPrivileged)
    {
        this.context = context;
        this.isPrivileged = isPrivileged;
    }
    AccessControlContext(ProtectionDomain[] context,
                         AccessControlContext privilegedContext)
    {
        this.context = context;
        this.privilegedContext = privilegedContext;
        this.isPrivileged = true;
    }
    ProtectionDomain[] getContext() {
        return context;
    }
    boolean isPrivileged()
    {
        return isPrivileged;
    }
    DomainCombiner getAssignedCombiner() {
        AccessControlContext acc;
        if (isPrivileged) {
            acc = privilegedContext;
        } else {
            acc = AccessController.getInheritedAccessControlContext();
        }
        if (acc != null) {
            return acc.combiner;
        }
        return null;
    }
    public DomainCombiner getDomainCombiner() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(SecurityConstants.GET_COMBINER_PERMISSION);
        }
        return combiner;
    }
    public void checkPermission(Permission perm)
        throws AccessControlException
    {
        boolean dumpDebug = false;
        if (perm == null) {
            throw new NullPointerException("permission can't be null");
        }
        if (getDebug() != null) {
            dumpDebug = !Debug.isOn("codebase=");
            if (!dumpDebug) {
                for (int i = 0; context != null && i < context.length; i++) {
                    if (context[i].getCodeSource() != null &&
                        context[i].getCodeSource().getLocation() != null &&
                        Debug.isOn("codebase=" + context[i].getCodeSource().getLocation().toString())) {
                        dumpDebug = true;
                        break;
                    }
                }
            }
            dumpDebug &= !Debug.isOn("permission=") ||
                Debug.isOn("permission=" + perm.getClass().getCanonicalName());
            if (dumpDebug && Debug.isOn("stack")) {
                Thread.currentThread().dumpStack();
            }
            if (dumpDebug && Debug.isOn("domain")) {
                if (context == null) {
                    debug.println("domain (context is null)");
                } else {
                    for (int i=0; i< context.length; i++) {
                        debug.println("domain "+i+" "+context[i]);
                    }
                }
            }
        }
        if (context == null)
            return;
        for (int i=0; i< context.length; i++) {
            if (context[i] != null &&  !context[i].implies(perm)) {
                if (dumpDebug) {
                    debug.println("access denied " + perm);
                }
                if (Debug.isOn("failure") && debug != null) {
                    if (!dumpDebug) {
                        debug.println("access denied " + perm);
                    }
                    Thread.currentThread().dumpStack();
                    final ProtectionDomain pd = context[i];
                    final Debug db = debug;
                    AccessController.doPrivileged (new PrivilegedAction<Void>() {
                        public Void run() {
                            db.println("domain that failed "+pd);
                            return null;
                        }
                    });
                }
                throw new AccessControlException("access denied "+perm, perm);
            }
        }
        if (dumpDebug) {
            debug.println("access allowed "+perm);
        }
        return;
    }
    AccessControlContext optimize() {
        AccessControlContext acc;
        if (isPrivileged) {
            acc = privilegedContext;
        } else {
            acc = AccessController.getInheritedAccessControlContext();
        }
        boolean skipStack = (context == null);
        boolean skipAssigned = (acc == null || acc.context == null);
        if (acc != null && acc.combiner != null) {
            return goCombiner(context, acc);
        }
        if (skipAssigned && skipStack) {
            return this;
        }
        if (skipStack) {
            return acc;
        }
        int slen = context.length;
        if (skipAssigned && slen <= 2) {
            return this;
        }
        if ((slen == 1) && (context[0] == acc.context[0])) {
            return acc;
        }
        int n = (skipAssigned) ? 0 : acc.context.length;
        ProtectionDomain pd[] = new ProtectionDomain[slen + n];
        if (!skipAssigned) {
            System.arraycopy(acc.context, 0, pd, 0, n);
        }
    outer:
        for (int i = 0; i < context.length; i++) {
            ProtectionDomain sd = context[i];
            if (sd != null) {
                for (int j = 0; j < n; j++) {
                    if (sd == pd[j]) {
                        continue outer;
                    }
                }
                pd[n++] = sd;
            }
        }
        if (n != pd.length) {
            if (!skipAssigned && n == acc.context.length) {
                return acc;
            } else if (skipAssigned && n == slen) {
                return this;
            }
            ProtectionDomain tmp[] = new ProtectionDomain[n];
            System.arraycopy(pd, 0, tmp, 0, n);
            pd = tmp;
        }
        this.context = pd;
        this.combiner = null;
        this.isPrivileged = false;
        return this;
    }
    private AccessControlContext goCombiner(ProtectionDomain[] current,
                                        AccessControlContext assigned) {
        if (getDebug() != null) {
            debug.println("AccessControlContext invoking the Combiner");
        }
        ProtectionDomain[] combinedPds = assigned.combiner.combine(
            current, assigned.context);
        this.context = combinedPds;
        this.combiner = assigned.combiner;
        this.isPrivileged = false;
        return this;
    }
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (! (obj instanceof AccessControlContext))
            return false;
        AccessControlContext that = (AccessControlContext) obj;
        if (context == null) {
            return (that.context == null);
        }
        if (that.context == null)
            return false;
        if (!(this.containsAllPDs(that) && that.containsAllPDs(this)))
            return false;
        if (this.combiner == null)
            return (that.combiner == null);
        if (that.combiner == null)
            return false;
        if (!this.combiner.equals(that.combiner))
            return false;
        return true;
    }
    private boolean containsAllPDs(AccessControlContext that) {
        boolean match = false;
        ProtectionDomain thisPd;
        for (int i = 0; i < context.length; i++) {
            match = false;
            if ((thisPd = context[i]) == null) {
                for (int j = 0; (j < that.context.length) && !match; j++) {
                    match = (that.context[j] == null);
                }
            } else {
                Class thisPdClass = thisPd.getClass();
                ProtectionDomain thatPd;
                for (int j = 0; (j < that.context.length) && !match; j++) {
                    thatPd = that.context[j];
                    match = (thatPd != null &&
                        thisPdClass == thatPd.getClass() && thisPd.equals(thatPd));
                }
            }
            if (!match) return false;
        }
        return match;
    }
    public int hashCode() {
        int hashCode = 0;
        if (context == null)
            return hashCode;
        for (int i =0; i < context.length; i++) {
            if (context[i] != null)
                hashCode ^= context[i].hashCode();
        }
        return hashCode;
    }
}
