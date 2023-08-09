public class DefaultPolicyParser {
    private final DefaultPolicyScanner scanner;
    public DefaultPolicyParser() {
        scanner = new DefaultPolicyScanner();
    }
    public DefaultPolicyParser(DefaultPolicyScanner s) {
        this.scanner = s;
    }
    public Collection<PolicyEntry>parse(URL location, Properties system)
            throws Exception {
        boolean resolve = PolicyUtils.canExpandProperties();
        Reader r =
            new BufferedReader(
                    new InputStreamReader(
                            AccessController.doPrivileged(
                                    new PolicyUtils.URLLoader(location))),
                    8192);
        Collection<GrantEntry> grantEntries = new HashSet<GrantEntry>();
        List<KeystoreEntry> keystores = new ArrayList<KeystoreEntry>();
        try {
            scanner.scanStream(r, grantEntries, keystores);
        }
        finally {
            r.close();
        }
        KeyStore ks = initKeyStore(keystores, location, system, resolve);
        Collection<PolicyEntry> result = new HashSet<PolicyEntry>();
        for (Iterator<GrantEntry> iter = grantEntries.iterator(); iter.hasNext();) {
            DefaultPolicyScanner.GrantEntry ge = iter
                    .next();
            try {
                PolicyEntry pe = resolveGrant(ge, ks, system, resolve);
                if (!pe.isVoid()) {
                    result.add(pe);
                }
            }
            catch (Exception e) {
            }
        }
        return result;
    }
    protected PolicyEntry resolveGrant(DefaultPolicyScanner.GrantEntry ge,
            KeyStore ks, Properties system, boolean resolve) throws Exception {
        URL codebase = null;
        Certificate[] signers = null;
        Set<Principal>principals = new HashSet<Principal>();
        Set<Permission>permissions = new HashSet<Permission>();
        if (ge.codebase != null) {
            codebase = new URL(resolve ? PolicyUtils.expandURL(ge.codebase,
                    system) : ge.codebase);
        }
        if (ge.signers != null) {
            if (resolve) {
                ge.signers = PolicyUtils.expand(ge.signers, system);
            }
            signers = resolveSigners(ks, ge.signers);
        }
        if (ge.principals != null) {
            for (Iterator<PrincipalEntry> iter = ge.principals.iterator(); iter.hasNext();) {
                DefaultPolicyScanner.PrincipalEntry pe = iter
                        .next();
                if (resolve) {
                    pe.name = PolicyUtils.expand(pe.name, system);
                }
                if (pe.klass == null) {
                    principals.add(getPrincipalByAlias(ks, pe.name));
                } else {
                    principals.add(new UnresolvedPrincipal(pe.klass, pe.name));
                }
            }
        }
        if (ge.permissions != null) {
            for (Iterator<PermissionEntry> iter = ge.permissions.iterator(); iter.hasNext();) {
                DefaultPolicyScanner.PermissionEntry pe = iter
                        .next();
                try {
                    permissions.add(resolvePermission(pe, ge, ks, system,
                            resolve));
                }
                catch (Exception e) {
                }
            }
        }
        return new PolicyEntry(new CodeSource(codebase, signers), principals,
                permissions);
    }
    protected Permission resolvePermission(
            DefaultPolicyScanner.PermissionEntry pe,
            DefaultPolicyScanner.GrantEntry ge, KeyStore ks, Properties system,
            boolean resolve) throws Exception {
        if (pe.name != null) {
            pe.name = PolicyUtils.expandGeneral(pe.name,
                    new PermissionExpander().configure(ge, ks));
        }
        if (resolve) {
            if (pe.name != null) {
                pe.name = PolicyUtils.expand(pe.name, system);
            }
            if (pe.actions != null) {
                pe.actions = PolicyUtils.expand(pe.actions, system);
            }
            if (pe.signers != null) {
                pe.signers = PolicyUtils.expand(pe.signers, system);
            }
        }
        Certificate[] signers = (pe.signers == null) ? null : resolveSigners(
                ks, pe.signers);
        try {
            Class<?> klass = Class.forName(pe.klass);
            if (PolicyUtils.matchSubset(signers, klass.getSigners())) {
                return PolicyUtils.instantiatePermission(klass, pe.name,
                        pe.actions);
            }
        }
        catch (ClassNotFoundException cnfe) {}
        return new UnresolvedPermission(pe.klass, pe.name, pe.actions, signers);
    }
    class PermissionExpander implements PolicyUtils.GeneralExpansionHandler {
        private KeyStore ks;
        private DefaultPolicyScanner.GrantEntry ge;
        public PermissionExpander configure(DefaultPolicyScanner.GrantEntry ge,
                KeyStore ks) {
            this.ge = ge;
            this.ks = ks;
            return this;
        }
        public String resolve(String protocol, String data)
                throws PolicyUtils.ExpansionFailedException {
            if ("self".equals(protocol)) { 
                if (ge.principals != null && ge.principals.size() != 0) {
                    StringBuilder sb = new StringBuilder();
                    for (Iterator<PrincipalEntry> iter = ge.principals.iterator(); iter
                            .hasNext();) {
                        DefaultPolicyScanner.PrincipalEntry pr = iter
                                .next();
                        if (pr.klass == null) {
                            try {
                                sb.append(pc2str(getPrincipalByAlias(ks,
                                        pr.name)));
                            }
                            catch (Exception e) {
                                throw new PolicyUtils.ExpansionFailedException(
                                        Messages.getString("security.143", pr.name), e); 
                            }
                        } else {
                            sb.append(pr.klass).append(" \"").append(pr.name) 
                                    .append("\" "); 
                        }
                    }
                    return sb.toString();
                } else {
                    throw new PolicyUtils.ExpansionFailedException(
                            Messages.getString("security.144")); 
                }
            }
            if ("alias".equals(protocol)) { 
                try {
                    return pc2str(getPrincipalByAlias(ks, data));
                }
                catch (Exception e) {
                    throw new PolicyUtils.ExpansionFailedException(
                            Messages.getString("security.143", data), e); 
                }
            }
            throw new PolicyUtils.ExpansionFailedException(
                    Messages.getString("security.145", protocol)); 
        }
        private String pc2str(Principal pc) {
            String klass = pc.getClass().getName();
            String name = pc.getName();
            StringBuilder sb = new StringBuilder(klass.length() + name.length()
                    + 5);
            return sb.append(klass).append(" \"").append(name).append("\"") 
                    .toString();
        }
    }
    protected Certificate[] resolveSigners(KeyStore ks, String signers)
            throws Exception {
        if (ks == null) {
            throw new KeyStoreException(Messages.getString("security.146", 
                    signers));
        }
        Collection<Certificate> certs = new HashSet<Certificate>();
        StringTokenizer snt = new StringTokenizer(signers, ","); 
        while (snt.hasMoreTokens()) {
            certs.add(ks.getCertificate(snt.nextToken().trim()));
        }
        return certs.toArray(new Certificate[certs.size()]);
    }
    protected Principal getPrincipalByAlias(KeyStore ks, String alias)
            throws KeyStoreException, CertificateException {
        if (ks == null) {
            throw new KeyStoreException(
                    Messages.getString("security.147", alias)); 
        }
        Certificate x509 = ks.getCertificate(alias);
        if (x509 instanceof X509Certificate) {
            return ((X509Certificate) x509).getSubjectX500Principal();
        } else {
            throw new CertificateException(Messages.getString("security.148", 
                    alias, x509));
        }
    }
    protected KeyStore initKeyStore(List<KeystoreEntry>keystores,
            URL base, Properties system, boolean resolve) {
        for (int i = 0; i < keystores.size(); i++) {
            try {
                DefaultPolicyScanner.KeystoreEntry ke = keystores
                        .get(i);
                if (resolve) {
                    ke.url = PolicyUtils.expandURL(ke.url, system);
                    if (ke.type != null) {
                        ke.type = PolicyUtils.expand(ke.type, system);
                    }
                }
                if (ke.type == null || ke.type.length() == 0) {
                    ke.type = KeyStore.getDefaultType();
                }
                KeyStore ks = KeyStore.getInstance(ke.type);
                URL location = new URL(base, ke.url);
                InputStream is = AccessController
                        .doPrivileged(new PolicyUtils.URLLoader(location));
                try {
                    ks.load(is, null);
                }
                finally {
                    is.close();
                }
                return ks;
            }
            catch (Exception e) {
            }
        }
        return null;
    }
}
