public class FtpDirEntry {
    public enum Type {
        FILE, DIR, PDIR, CDIR, LINK
    };
    public enum Permission {
        USER(0), GROUP(1), OTHERS(2);
        int value;
        Permission(int v) {
            value = v;
        }
    };
    private final String name;
    private String user = null;
    private String group = null;
    private long size = -1;
    private java.util.Date created = null;
    private java.util.Date lastModified = null;
    private Type type = Type.FILE;
    private boolean[][] permissions = null;
    private HashMap<String, String> facts = new HashMap<String, String>();
    private FtpDirEntry() {
        name = null;
    }
    public FtpDirEntry(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public String getUser() {
        return user;
    }
    public FtpDirEntry setUser(String user) {
        this.user = user;
        return this;
    }
    public String getGroup() {
        return group;
    }
    public FtpDirEntry setGroup(String group) {
        this.group = group;
        return this;
    }
    public long getSize() {
        return size;
    }
    public FtpDirEntry setSize(long size) {
        this.size = size;
        return this;
    }
    public Type getType() {
        return type;
    }
    public FtpDirEntry setType(Type type) {
        this.type = type;
        return this;
    }
    public java.util.Date getLastModified() {
        return this.lastModified;
    }
    public FtpDirEntry setLastModified(Date lastModified) {
        this.lastModified = lastModified;
        return this;
    }
    public boolean canRead(Permission p) {
        if (permissions != null) {
            return permissions[p.value][0];
        }
        return false;
    }
    public boolean canWrite(Permission p) {
        if (permissions != null) {
            return permissions[p.value][1];
        }
        return false;
    }
    public boolean canExexcute(Permission p) {
        if (permissions != null) {
            return permissions[p.value][2];
        }
        return false;
    }
    public FtpDirEntry setPermissions(boolean[][] permissions) {
        this.permissions = permissions;
        return this;
    }
    public FtpDirEntry addFact(String fact, String value) {
        facts.put(fact.toLowerCase(), value);
        return this;
    }
    public String getFact(String fact) {
        return facts.get(fact.toLowerCase());
    }
    public Date getCreated() {
        return created;
    }
    public FtpDirEntry setCreated(Date created) {
        this.created = created;
        return this;
    }
    @Override
    public String toString() {
        if (lastModified == null) {
            return name + " [" + type + "] (" + user + " / " + group + ") " + size;
        }
        return name + " [" + type + "] (" + user + " / " + group + ") {" + size + "} " + java.text.DateFormat.getDateInstance().format(lastModified);
    }
}
