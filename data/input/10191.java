final class JceSecurityManager extends SecurityManager {
    private static final CryptoPermissions defaultPolicy;
    private static final CryptoPermissions exemptPolicy;
    private static final CryptoAllPermission allPerm;
    private static final Vector TrustedCallersCache = new Vector(2);
    private static final Map exemptCache = new HashMap();
    static final JceSecurityManager INSTANCE;
    static {
        defaultPolicy = JceSecurity.getDefaultPolicy();
        exemptPolicy = JceSecurity.getExemptPolicy();
        allPerm = CryptoAllPermission.INSTANCE;
        INSTANCE = (JceSecurityManager)
              AccessController.doPrivileged(new PrivilegedAction() {
                  public Object run() {
                      return new JceSecurityManager();
                  }
              });
    }
    private JceSecurityManager() {
    }
    CryptoPermission getCryptoPermission(String alg) {
        alg = alg.toUpperCase(Locale.ENGLISH);
        CryptoPermission defaultPerm = getDefaultPermission(alg);
        if (defaultPerm == CryptoAllPermission.INSTANCE) {
            return defaultPerm;
        }
        Class[] context = getClassContext();
        URL callerCodeBase = null;
        int i;
        for (i=0; i<context.length; i++) {
            Class cls = context[i];
            callerCodeBase = JceSecurity.getCodeBase(cls);
            if (callerCodeBase != null) {
                break;
            } else {
                if (cls.getName().startsWith("javax.crypto.")) {
                    continue;
                }
                return defaultPerm;
            }
        }
        if (i == context.length) {
            return defaultPerm;
        }
        CryptoPermissions appPerms;
        synchronized (this.getClass()) {
            if (exemptCache.containsKey(callerCodeBase)) {
                appPerms = (CryptoPermissions)exemptCache.get(callerCodeBase);
            } else {
                appPerms = getAppPermissions(callerCodeBase);
                exemptCache.put(callerCodeBase, appPerms);
            }
        }
        if (appPerms == null) {
            return defaultPerm;
        }
        if (appPerms.implies(allPerm)) {
            return allPerm;
        }
        PermissionCollection appPc = appPerms.getPermissionCollection(alg);
        if (appPc == null) {
            return defaultPerm;
        }
        Enumeration enum_ = appPc.elements();
        while (enum_.hasMoreElements()) {
            CryptoPermission cp = (CryptoPermission)enum_.nextElement();
            if (cp.getExemptionMechanism() == null) {
                return cp;
            }
        }
        PermissionCollection exemptPc =
            exemptPolicy.getPermissionCollection(alg);
        if (exemptPc == null) {
            return defaultPerm;
        }
        enum_ = exemptPc.elements();
        while (enum_.hasMoreElements()) {
            CryptoPermission cp = (CryptoPermission)enum_.nextElement();
            try {
                ExemptionMechanism.getInstance(cp.getExemptionMechanism());
                if (cp.getAlgorithm().equals(
                                      CryptoPermission.ALG_NAME_WILDCARD)) {
                    CryptoPermission newCp;
                    if (cp.getCheckParam()) {
                        newCp = new CryptoPermission(
                                alg, cp.getMaxKeySize(),
                                cp.getAlgorithmParameterSpec(),
                                cp.getExemptionMechanism());
                    } else {
                        newCp = new CryptoPermission(
                                alg, cp.getMaxKeySize(),
                                cp.getExemptionMechanism());
                    }
                    if (appPerms.implies(newCp)) {
                        return newCp;
                    }
                }
                if (appPerms.implies(cp)) {
                    return cp;
                }
            } catch (Exception e) {
                continue;
            }
        }
        return defaultPerm;
    }
    private static CryptoPermissions getAppPermissions(URL callerCodeBase) {
        try {
            return JceSecurity.verifyExemptJar(callerCodeBase);
        } catch (Exception e) {
            return null;
        }
    }
    private CryptoPermission getDefaultPermission(String alg) {
        Enumeration enum_ =
            defaultPolicy.getPermissionCollection(alg).elements();
        return (CryptoPermission)enum_.nextElement();
    }
    boolean isCallerTrusted() {
        Class[] context = getClassContext();
        URL callerCodeBase = null;
        int i;
        for (i=0; i<context.length; i++) {
            callerCodeBase = JceSecurity.getCodeBase(context[i]);
            if (callerCodeBase != null) {
                break;
            }
        }
        if (i == context.length) {
            return true;
        }
        if (TrustedCallersCache.contains(context[i])) {
            return true;
        }
        try {
            JceSecurity.verifyProviderJar(callerCodeBase);
        } catch (Exception e2) {
            return false;
        }
        TrustedCallersCache.addElement(context[i]);
        return true;
    }
}
