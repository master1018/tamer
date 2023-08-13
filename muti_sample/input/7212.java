public class GetInstancePolicySpi extends PolicySpi {
    private Policy p;
    public GetInstancePolicySpi(final Policy.Parameters params) {
        p = AccessController.doPrivileged
            (new PrivilegedAction<Policy>() {
            public Policy run() {
                if (params instanceof URIParameter) {
                    URIParameter uriParam = (URIParameter)params;
                    try {
                        URL url = uriParam.getURI().toURL();
                        return new PolicyFile(url);
                    } catch (MalformedURLException mue) {
                        throw new IllegalArgumentException(mue);
                    }
                }
                return new PolicyFile();
            }
        });
    }
    public boolean engineImplies(ProtectionDomain domain, Permission perm) {
        return p.implies(domain, perm);
    }
}
