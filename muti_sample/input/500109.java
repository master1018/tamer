public class DefaultPolicy extends Policy {
    public static final String JAVA_SECURITY_POLICY = "java.security.policy"; 
    public static final String POLICY_URL_PREFIX = "policy.url."; 
    private final Set<PolicyEntry> grants = new HashSet<PolicyEntry>();
    private final Map<Object, Collection<Permission>> cache = new WeakHashMap<Object, Collection<Permission>>();
    private final DefaultPolicyParser parser;
    private boolean initialized;
    public DefaultPolicy() {
        this(new DefaultPolicyParser());
    }
    public DefaultPolicy(DefaultPolicyParser dpr) {
        parser = dpr;
        initialized = false;
        refresh();
    }
    public PermissionCollection getPermissions(ProtectionDomain pd) {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    refresh();
                }
            }
        }
        Collection<Permission> pc = cache.get(pd);
        if (pc == null) {
            synchronized (cache) {
                pc = cache.get(pd);
                if (pc == null) {
                    pc = new HashSet<Permission>();
                    Iterator<PolicyEntry> it = grants.iterator();
                    while (it.hasNext()) {
                        PolicyEntry ge = (PolicyEntry)it.next();
                        if (ge.impliesPrincipals(pd == null ? null : pd.getPrincipals())
                            && ge.impliesCodeSource(pd == null ? null : pd.getCodeSource())) {
                            pc.addAll(ge.getPermissions());
                        }
                    }
                    cache.put(pd, pc);
                }
            }
        }
        return PolicyUtils.toPermissionCollection(pc);
    }
    public PermissionCollection getPermissions(CodeSource cs) {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    refresh();
                }
            }
        }
        Collection<Permission> pc = cache.get(cs);
        if (pc == null) {
            synchronized (cache) {
                pc = cache.get(cs);
                if (pc == null) {
                    pc = new HashSet<Permission>();
                    Iterator<PolicyEntry> it = grants.iterator();
                    while (it.hasNext()) {
                        PolicyEntry ge = (PolicyEntry)it.next();
                        if (ge.impliesPrincipals(null)
                            && ge.impliesCodeSource(cs)) {
                            pc.addAll(ge.getPermissions());
                        }
                    }
                    cache.put(cs, pc);
                }
            }
        }
        return PolicyUtils.toPermissionCollection(pc);
    }
    public synchronized void refresh() {
        Set<PolicyEntry> fresh = new HashSet<PolicyEntry>();
        Properties system = new Properties(AccessController
                .doPrivileged(new PolicyUtils.SystemKit()));
        system.setProperty("/", File.separator); 
        URL[] policyLocations = PolicyUtils.getPolicyURLs(system,
                                                          JAVA_SECURITY_POLICY,
                                                          POLICY_URL_PREFIX);
        for (int i = 0; i < policyLocations.length; i++) {
            try {
                fresh.addAll(parser.parse(policyLocations[i], system));
            } catch (Exception e) {
            }
        }
        synchronized (cache) {
            grants.clear();
            grants.addAll(fresh);
            cache.clear();
        }
        initialized = true;
    }
}
