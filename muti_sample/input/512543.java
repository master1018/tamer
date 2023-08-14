public final class FilePermission extends Permission implements Serializable {
    private static final long serialVersionUID = 7930732926638008763L;
    private transient String canonPath;
    private static final String[] actionList = { "read", "write", "execute", 
            "delete" }; 
    private String actions;
    transient int mask = -1;
    private transient boolean includeAll = false;
    private transient boolean allDir = false;
    private transient boolean allSubdir = false;
    public FilePermission(String path, String actions) {
        super(path);
        init(path, actions);
    }
    private void init(final String path, String pathActions) {
        if (pathActions == null || pathActions.equals("")) { 
            throw new IllegalArgumentException(Msg.getString("K006d")); 
        }
        this.actions = toCanonicalActionString(pathActions);
        if (path == null) {
            throw new NullPointerException(Msg.getString("K006e")); 
        }
        if (path.equals("<<ALL FILES>>")) { 
            includeAll = true;
        } else {
            canonPath = AccessController
                    .doPrivileged(new PrivilegedAction<String>() {
                        public String run() {
                            try {
                                return new File(path).getCanonicalPath();
                            } catch (IOException e) {
                                return path;
                            }
                        }
                    });
            if (path.equals("*") || path.endsWith(File.separator + "*")) { 
                allDir = true;
            }
            if (path.equals("-") || path.endsWith(File.separator + "-")) { 
                allSubdir = true;
            }
        }
    }
    private String toCanonicalActionString(String action) {
        actions = action.trim().toLowerCase();
        mask = getMask(actions);
        int len = actionList.length;
        int highestBitMask = 1 << (len - 1);
        StringBuilder result = new StringBuilder();
        boolean addedItem = false;
        for (int i = 0; i < len; i++) {
            if ((highestBitMask & mask) != 0) {
                if (addedItem) {
                    result.append(","); 
                }
                result.append(actionList[i]);
                addedItem = true;
            }
            highestBitMask = highestBitMask >> 1;
        }
        return result.toString();
    }
    private int getMask(String actionNames) {
        int actionInt = 0, head = 0, tail = 0;
        do {
            tail = actionNames.indexOf(",", head); 
            String action = tail > 0 ? actionNames.substring(head, tail).trim()
                    : actionNames.substring(head).trim();
            if (action.equals("read")) { 
                actionInt |= 8;
            } else if (action.equals("write")) { 
                actionInt |= 4;
            } else if (action.equals("execute")) { 
                actionInt |= 2;
            } else if (action.equals("delete")) { 
                actionInt |= 1;
            } else {
                throw new IllegalArgumentException(Msg.getString(
                        "K006f", action)); 
            }
            head = tail + 1;
        } while (tail > 0);
        return actionInt;
    }
    @Override
    public String getActions() {
        return actions;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FilePermission) {
            FilePermission fp = (FilePermission) obj;
            if (fp.actions != actions) {
                if (fp.actions == null || !fp.actions.equals(actions)) {
                    return false;
                }
            }
            if (fp.includeAll || includeAll) {
                return fp.includeAll == includeAll;
            }
            return fp.canonPath.equals(canonPath);
        }
        return false;
    }
    @Override
    public boolean implies(Permission p) {
        int match = impliesMask(p);
        return match != 0 && match == ((FilePermission) p).mask;
    }
    int impliesMask(Permission p) {
        if (!(p instanceof FilePermission)) {
            return 0;
        }
        FilePermission fp = (FilePermission) p;
        int matchedMask = mask & fp.mask;
        if (matchedMask == 0) {
            return 0;
        }
        if (includeAll) {
            return matchedMask;
        }
        if (fp.includeAll) {
            return 0;
        }
        int thisLength = canonPath.length();
        if (allSubdir && thisLength == 2
                && !fp.canonPath.equals(File.separator)) {
            return matchedMask;
        }
        if (fp.allSubdir && !allSubdir) {
            return 0;
        }
        if (fp.allDir && !allSubdir && !allDir) {
            return 0;
        }
        boolean includeDir = false;
        int pLength = fp.canonPath.length();
        if (allDir || allSubdir) {
            thisLength--;
        }
        if (fp.allDir || fp.allSubdir) {
            pLength--;
        }
        for (int i = 0; i < pLength; i++) {
            char pChar = fp.canonPath.charAt(i);
            if (i >= thisLength) {
                if (i == thisLength) {
                    if (allSubdir) {
                        return matchedMask;
                    }
                    if (allDir) {
                        includeDir = true;
                    }
                }
                if (!includeDir) {
                    return 0;
                }
                if (pChar == File.separatorChar) {
                    return 0;
                }
            } else {
                if (canonPath.charAt(i) != pChar) {
                    return 0;
                }
            }
        }
        if (pLength == thisLength) {
            if (allSubdir) {
                return fp.allSubdir || fp.allDir ? matchedMask : 0;
            }
            return allDir == fp.allDir ? matchedMask : 0;
        }
        return includeDir ? matchedMask : 0;
    }
    @Override
    public PermissionCollection newPermissionCollection() {
        return new FilePermissionCollection();
    }
    @Override
    public int hashCode() {
        return (canonPath == null ? getName().hashCode() : canonPath.hashCode())
                + mask;
    }
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
    }
    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        stream.defaultReadObject();
        init(getName(), actions);
    }
}
