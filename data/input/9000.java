public final class PolicySpiFile extends PolicySpi {
    private PolicyFile pf;
    public PolicySpiFile(Policy.Parameters params) {
        if (params == null) {
            pf = new PolicyFile();
        } else {
            if (!(params instanceof URIParameter)) {
                throw new IllegalArgumentException
                        ("Unrecognized policy parameter: " + params);
            }
            URIParameter uriParam = (URIParameter)params;
            try {
                pf = new PolicyFile(uriParam.getURI().toURL());
            } catch (MalformedURLException mue) {
                throw new IllegalArgumentException("Invalid URIParameter", mue);
            }
        }
    }
    protected PermissionCollection engineGetPermissions(CodeSource codesource) {
        return pf.getPermissions(codesource);
    }
    protected PermissionCollection engineGetPermissions(ProtectionDomain d) {
        return pf.getPermissions(d);
    }
    protected boolean engineImplies(ProtectionDomain d, Permission p) {
        return pf.implies(d, p);
    }
    protected void engineRefresh() {
        pf.refresh();
    }
}
