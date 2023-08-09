public class SubjectDomainCombiner implements java.security.DomainCombiner {
    private Subject subject;
    private WeakKeyValueMap<ProtectionDomain, ProtectionDomain> cachedPDs =
                new WeakKeyValueMap<>();
    private Set<Principal> principalSet;
    private Principal[] principals;
    private static final sun.security.util.Debug debug =
        sun.security.util.Debug.getInstance("combiner",
                                        "\t[SubjectDomainCombiner]");
    private static final boolean useJavaxPolicy =
        javax.security.auth.Policy.isCustomPolicySet(debug);
    private static final boolean allowCaching =
                                        (useJavaxPolicy && cachePolicy());
    public SubjectDomainCombiner(Subject subject) {
        this.subject = subject;
        if (subject.isReadOnly()) {
            principalSet = subject.getPrincipals();
            principals = principalSet.toArray
                        (new Principal[principalSet.size()]);
        }
    }
    public Subject getSubject() {
        java.lang.SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new AuthPermission
                ("getSubjectFromDomainCombiner"));
        }
        return subject;
    }
    public ProtectionDomain[] combine(ProtectionDomain[] currentDomains,
                                ProtectionDomain[] assignedDomains) {
        if (debug != null) {
            if (subject == null) {
                debug.println("null subject");
            } else {
                final Subject s = subject;
                AccessController.doPrivileged
                    (new java.security.PrivilegedAction<Void>() {
                    public Void run() {
                        debug.println(s.toString());
                        return null;
                    }
                });
            }
            printInputDomains(currentDomains, assignedDomains);
        }
        if (currentDomains == null || currentDomains.length == 0) {
            return assignedDomains;
        }
        currentDomains = optimize(currentDomains);
        if (debug != null) {
            debug.println("after optimize");
            printInputDomains(currentDomains, assignedDomains);
        }
        if (currentDomains == null && assignedDomains == null) {
            return null;
        }
        if (useJavaxPolicy) {
            return combineJavaxPolicy(currentDomains, assignedDomains);
        }
        int cLen = (currentDomains == null ? 0 : currentDomains.length);
        int aLen = (assignedDomains == null ? 0 : assignedDomains.length);
        ProtectionDomain[] newDomains = new ProtectionDomain[cLen + aLen];
        boolean allNew = true;
        synchronized(cachedPDs) {
            if (!subject.isReadOnly() &&
                !subject.getPrincipals().equals(principalSet)) {
                Set<Principal> newSet = subject.getPrincipals();
                synchronized(newSet) {
                    principalSet = new java.util.HashSet<Principal>(newSet);
                }
                principals = principalSet.toArray
                        (new Principal[principalSet.size()]);
                cachedPDs.clear();
                if (debug != null) {
                    debug.println("Subject mutated - clearing cache");
                }
            }
            ProtectionDomain subjectPd;
            for (int i = 0; i < cLen; i++) {
                ProtectionDomain pd = currentDomains[i];
                subjectPd = cachedPDs.getValue(pd);
                if (subjectPd == null) {
                    subjectPd = new ProtectionDomain(pd.getCodeSource(),
                                                pd.getPermissions(),
                                                pd.getClassLoader(),
                                                principals);
                    cachedPDs.putValue(pd, subjectPd);
                } else {
                    allNew = false;
                }
                newDomains[i] = subjectPd;
            }
        }
        if (debug != null) {
            debug.println("updated current: ");
            for (int i = 0; i < cLen; i++) {
                debug.println("\tupdated[" + i + "] = " +
                                printDomain(newDomains[i]));
            }
        }
        if (aLen > 0) {
            System.arraycopy(assignedDomains, 0, newDomains, cLen, aLen);
            if (!allNew) {
                newDomains = optimize(newDomains);
            }
        }
        if (debug != null) {
            if (newDomains == null || newDomains.length == 0) {
                debug.println("returning null");
            } else {
                debug.println("combinedDomains: ");
                for (int i = 0; i < newDomains.length; i++) {
                    debug.println("newDomain " + i + ": " +
                                  printDomain(newDomains[i]));
                }
            }
        }
        if (newDomains == null || newDomains.length == 0) {
            return null;
        } else {
            return newDomains;
        }
    }
    private ProtectionDomain[] combineJavaxPolicy(
        ProtectionDomain[] currentDomains,
        ProtectionDomain[] assignedDomains) {
        if (!allowCaching) {
            java.security.AccessController.doPrivileged
                (new PrivilegedAction<Void>() {
                    public Void run() {
                        javax.security.auth.Policy.getPolicy().refresh();
                        return null;
                    }
                });
        }
        int cLen = (currentDomains == null ? 0 : currentDomains.length);
        int aLen = (assignedDomains == null ? 0 : assignedDomains.length);
        ProtectionDomain[] newDomains = new ProtectionDomain[cLen + aLen];
        synchronized(cachedPDs) {
            if (!subject.isReadOnly() &&
                !subject.getPrincipals().equals(principalSet)) {
                Set<Principal> newSet = subject.getPrincipals();
                synchronized(newSet) {
                    principalSet = new java.util.HashSet<Principal>(newSet);
                }
                principals = principalSet.toArray
                        (new Principal[principalSet.size()]);
                cachedPDs.clear();
                if (debug != null) {
                    debug.println("Subject mutated - clearing cache");
                }
            }
            for (int i = 0; i < cLen; i++) {
                ProtectionDomain pd = currentDomains[i];
                ProtectionDomain subjectPd = cachedPDs.getValue(pd);
                if (subjectPd == null) {
                    Permissions perms = new Permissions();
                    PermissionCollection coll = pd.getPermissions();
                    java.util.Enumeration e;
                    if (coll != null) {
                        synchronized (coll) {
                            e = coll.elements();
                            while (e.hasMoreElements()) {
                                Permission newPerm =
                                        (Permission)e.nextElement();
                                 perms.add(newPerm);
                            }
                        }
                    }
                    final java.security.CodeSource finalCs = pd.getCodeSource();
                    final Subject finalS = subject;
                    PermissionCollection newPerms =
                        java.security.AccessController.doPrivileged
                        (new PrivilegedAction<PermissionCollection>() {
                        public PermissionCollection run() {
                          return
                          javax.security.auth.Policy.getPolicy().getPermissions
                                (finalS, finalCs);
                        }
                    });
                    synchronized (newPerms) {
                        e = newPerms.elements();
                        while (e.hasMoreElements()) {
                            Permission newPerm = (Permission)e.nextElement();
                            if (!perms.implies(newPerm)) {
                                perms.add(newPerm);
                                if (debug != null)
                                    debug.println (
                                        "Adding perm " + newPerm + "\n");
                            }
                        }
                    }
                    subjectPd = new ProtectionDomain
                        (finalCs, perms, pd.getClassLoader(), principals);
                    if (allowCaching)
                        cachedPDs.putValue(pd, subjectPd);
                }
                newDomains[i] = subjectPd;
            }
        }
        if (debug != null) {
            debug.println("updated current: ");
            for (int i = 0; i < cLen; i++) {
                debug.println("\tupdated[" + i + "] = " + newDomains[i]);
            }
        }
        if (aLen > 0) {
            System.arraycopy(assignedDomains, 0, newDomains, cLen, aLen);
        }
        if (debug != null) {
            if (newDomains == null || newDomains.length == 0) {
                debug.println("returning null");
            } else {
                debug.println("combinedDomains: ");
                for (int i = 0; i < newDomains.length; i++) {
                    debug.println("newDomain " + i + ": " +
                        newDomains[i].toString());
                }
            }
        }
        if (newDomains == null || newDomains.length == 0) {
            return null;
        } else {
            return newDomains;
        }
    }
    private static ProtectionDomain[] optimize(ProtectionDomain[] domains) {
        if (domains == null || domains.length == 0)
            return null;
        ProtectionDomain[] optimized = new ProtectionDomain[domains.length];
        ProtectionDomain pd;
        int num = 0;
        for (int i = 0; i < domains.length; i++) {
            if ((pd = domains[i]) != null) {
                boolean found = false;
                for (int j = 0; j < num && !found; j++) {
                    found = (optimized[j] == pd);
                }
                if (!found) {
                    optimized[num++] = pd;
                }
            }
        }
        if (num > 0 && num < domains.length) {
            ProtectionDomain[] downSize = new ProtectionDomain[num];
            System.arraycopy(optimized, 0, downSize, 0, downSize.length);
            optimized = downSize;
        }
        return ((num == 0 || optimized.length == 0) ? null : optimized);
    }
    private static boolean cachePolicy() {
        String s = AccessController.doPrivileged
            (new PrivilegedAction<String>() {
            public String run() {
                return Security.getProperty("cache.auth.policy");
            }
        });
        if (s != null) {
            return Boolean.parseBoolean(s);
        }
        return true;
    }
    private static void printInputDomains(ProtectionDomain[] currentDomains,
                                ProtectionDomain[] assignedDomains) {
        if (currentDomains == null || currentDomains.length == 0) {
            debug.println("currentDomains null or 0 length");
        } else {
            for (int i = 0; currentDomains != null &&
                        i < currentDomains.length; i++) {
                if (currentDomains[i] == null) {
                    debug.println("currentDomain " + i + ": SystemDomain");
                } else {
                    debug.println("currentDomain " + i + ": " +
                                printDomain(currentDomains[i]));
                }
            }
        }
        if (assignedDomains == null || assignedDomains.length == 0) {
            debug.println("assignedDomains null or 0 length");
        } else {
            debug.println("assignedDomains = ");
            for (int i = 0; assignedDomains != null &&
                        i < assignedDomains.length; i++) {
                if (assignedDomains[i] == null) {
                    debug.println("assignedDomain " + i + ": SystemDomain");
                } else {
                    debug.println("assignedDomain " + i + ": " +
                                printDomain(assignedDomains[i]));
                }
            }
        }
    }
    private static String printDomain(final ProtectionDomain pd) {
        if (pd == null) {
            return "null";
        }
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                return pd.toString();
            }
        });
    }
    private static class WeakKeyValueMap<K,V> extends
                                        WeakHashMap<K,WeakReference<V>> {
        public V getValue(K key) {
            WeakReference<V> wr = super.get(key);
            if (wr != null) {
                return wr.get();
            }
            return null;
        }
        public V putValue(K key, V value) {
            WeakReference<V> wr = super.put(key, new WeakReference<V>(value));
            if (wr != null) {
                return wr.get();
            }
            return null;
        }
    }
}
