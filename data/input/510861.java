public class PolicyEntry {
    private final CodeSource cs;
    private final Principal[] principals;
    private final Collection<Permission> permissions;
    public PolicyEntry(CodeSource cs, Collection<? extends Principal> prs,
            Collection<? extends Permission> permissions) {
        this.cs = (cs != null) ? normalizeCodeSource(cs) : null;
        this.principals = (prs == null || prs.isEmpty()) ? null
                : (Principal[]) prs.toArray(new Principal[prs.size()]);
        this.permissions = (permissions == null || permissions.isEmpty()) ? null
                : Collections.unmodifiableCollection(permissions);
    }
    public boolean impliesCodeSource(CodeSource codeSource) {
        if (cs == null) {
            return true;
        }
        if (codeSource == null) {
            return false;
        }
        return cs.implies(normalizeCodeSource(codeSource));
    }
    private CodeSource normalizeCodeSource(CodeSource codeSource) {
        URL codeSourceURL = PolicyUtils.normalizeURL(codeSource.getLocation());
        CodeSource result = codeSource;
        if (codeSourceURL != codeSource.getLocation()) {
            CodeSigner[] signers = codeSource.getCodeSigners();
            if (signers == null) {
                result = new CodeSource(codeSourceURL, codeSource
                        .getCertificates());
            } else {
                result = new CodeSource(codeSourceURL, signers);
            }
        }
        return result;
    }
    public boolean impliesPrincipals(Principal[] prs) {
        return PolicyUtils.matchSubset(principals, prs);
    }
    public Collection<Permission> getPermissions() {
        return permissions;
    }
    public boolean isVoid() {
        return permissions == null || permissions.size() == 0;
    }
}
