public class SolarisSystem {
    private native void getSolarisInfo();
    protected String username;
    protected long uid;
    protected long gid;
    protected long[] groups;
    public SolarisSystem() {
        System.loadLibrary("jaas_unix");
        getSolarisInfo();
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
