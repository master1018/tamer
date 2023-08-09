public class NTSystem {
    private native void getCurrent(boolean debug);
    private String userName;
    private String domain;
    private String domainSID;
    private String userSID;
    private String groupIDs[];
    private String primaryGroupID;
    private long   impersonationToken;
    public NTSystem() {
        this(false);
    }
    NTSystem(boolean debug) {
        loadNative();
        getCurrent(debug);
    }
    public String getName() {
        return userName;
    }
    public String getDomain() {
        return domain;
    }
    public String getDomainSID() {
        return domainSID;
    }
    public String getUserSID() {
        return userSID;
    }
    public String getPrimaryGroupID() {
        return primaryGroupID;
    }
    public String[] getGroupIDs() {
        return groupIDs == null ? null : groupIDs.clone();
    }
    public long getImpersonationToken() {
        return impersonationToken;
    }
    private void loadNative() {
        System.loadLibrary("jaas_nt");
    }
}
