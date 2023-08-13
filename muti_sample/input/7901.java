public class UnixSystem {
    private native void getUnixInfo();
    protected String username;
    protected long uid;
    protected long gid;
    protected long[] groups;
    public UnixSystem() {
        System.loadLibrary("jaas_unix");
        getUnixInfo();
    }
    public String getUsername() {
        return username;
    }
    public long getUid() {
        return uid;
    }
    public long getGid() {
        return gid;
    }
    public long[] getGroups() {
        return groups == null ? null : groups.clone();
    }
}
