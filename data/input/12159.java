class Win32FileSystem extends FileSystem {
    private final char slash;
    private final char altSlash;
    private final char semicolon;
    public Win32FileSystem() {
        slash = AccessController.doPrivileged(
            new GetPropertyAction("file.separator")).charAt(0);
        semicolon = AccessController.doPrivileged(
            new GetPropertyAction("path.separator")).charAt(0);
        altSlash = (this.slash == '\\') ? '/' : '\\';
    }
    private boolean isSlash(char c) {
        return (c == '\\') || (c == '/');
    }
    private boolean isLetter(char c) {
        return ((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z'));
    }
    private String slashify(String p) {
        if ((p.length() > 0) && (p.charAt(0) != slash)) return slash + p;
        else return p;
    }
    public char getSeparator() {
        return slash;
    }
    public char getPathSeparator() {
        return semicolon;
    }
    private int normalizePrefix(String path, int len, StringBuffer sb) {
        int src = 0;
        while ((src < len) && isSlash(path.charAt(src))) src++;
        char c;
        if ((len - src >= 2)
            && isLetter(c = path.charAt(src))
            && path.charAt(src + 1) == ':') {
            sb.append(c);
            sb.append(':');
            src += 2;
        } else {
            src = 0;
            if ((len >= 2)
                && isSlash(path.charAt(0))
                && isSlash(path.charAt(1))) {
                src = 1;
                sb.append(slash);
            }
        }
        return src;
    }
    private String normalize(String path, int len, int off) {
        if (len == 0) return path;
        if (off < 3) off = 0;   
        int src;
        char slash = this.slash;
        StringBuffer sb = new StringBuffer(len);
        if (off == 0) {
            src = normalizePrefix(path, len, sb);
        } else {
            src = off;
            sb.append(path.substring(0, off));
        }
        while (src < len) {
            char c = path.charAt(src++);
            if (isSlash(c)) {
                while ((src < len) && isSlash(path.charAt(src))) src++;
                if (src == len) {
                    int sn = sb.length();
                    if ((sn == 2) && (sb.charAt(1) == ':')) {
                        sb.append(slash);
                        break;
                    }
                    if (sn == 0) {
                        sb.append(slash);
                        break;
                    }
                    if ((sn == 1) && (isSlash(sb.charAt(0)))) {
                        sb.append(slash);
                        break;
                    }
                    break;
                } else {
                    sb.append(slash);
                }
            } else {
                sb.append(c);
            }
        }
        String rv = sb.toString();
        return rv;
    }
    public String normalize(String path) {
        int n = path.length();
        char slash = this.slash;
        char altSlash = this.altSlash;
        char prev = 0;
        for (int i = 0; i < n; i++) {
            char c = path.charAt(i);
            if (c == altSlash)
                return normalize(path, n, (prev == slash) ? i - 1 : i);
            if ((c == slash) && (prev == slash) && (i > 1))
                return normalize(path, n, i - 1);
            if ((c == ':') && (i > 1))
                return normalize(path, n, 0);
            prev = c;
        }
        if (prev == slash) return normalize(path, n, n - 1);
        return path;
    }
    public int prefixLength(String path) {
        char slash = this.slash;
        int n = path.length();
        if (n == 0) return 0;
        char c0 = path.charAt(0);
        char c1 = (n > 1) ? path.charAt(1) : 0;
        if (c0 == slash) {
            if (c1 == slash) return 2;  
            return 1;                   
        }
        if (isLetter(c0) && (c1 == ':')) {
            if ((n > 2) && (path.charAt(2) == slash))
                return 3;               
            return 2;                   
        }
        return 0;                       
    }
    public String resolve(String parent, String child) {
        int pn = parent.length();
        if (pn == 0) return child;
        int cn = child.length();
        if (cn == 0) return parent;
        String c = child;
        int childStart = 0;
        int parentEnd = pn;
        if ((cn > 1) && (c.charAt(0) == slash)) {
            if (c.charAt(1) == slash) {
                childStart = 2;
            } else {
                childStart = 1;
            }
            if (cn == childStart) { 
                if (parent.charAt(pn - 1) == slash)
                    return parent.substring(0, pn - 1);
                return parent;
            }
        }
        if (parent.charAt(pn - 1) == slash)
            parentEnd--;
        int strlen = parentEnd + cn - childStart;
        char[] theChars = null;
        if (child.charAt(childStart) == slash) {
            theChars = new char[strlen];
            parent.getChars(0, parentEnd, theChars, 0);
            child.getChars(childStart, cn, theChars, parentEnd);
        } else {
            theChars = new char[strlen + 1];
            parent.getChars(0, parentEnd, theChars, 0);
            theChars[parentEnd] = slash;
            child.getChars(childStart, cn, theChars, parentEnd + 1);
        }
        return new String(theChars);
    }
    public String getDefaultParent() {
        return ("" + slash);
    }
    public String fromURIPath(String path) {
        String p = path;
        if ((p.length() > 2) && (p.charAt(2) == ':')) {
            p = p.substring(1);
            if ((p.length() > 3) && p.endsWith("/"))
                p = p.substring(0, p.length() - 1);
        } else if ((p.length() > 1) && p.endsWith("/")) {
            p = p.substring(0, p.length() - 1);
        }
        return p;
    }
    public boolean isAbsolute(File f) {
        int pl = f.getPrefixLength();
        return (((pl == 2) && (f.getPath().charAt(0) == slash))
                || (pl == 3));
    }
    protected native String getDriveDirectory(int drive);
    private static String[] driveDirCache = new String[26];
    private static int driveIndex(char d) {
        if ((d >= 'a') && (d <= 'z')) return d - 'a';
        if ((d >= 'A') && (d <= 'Z')) return d - 'A';
        return -1;
    }
    private String getDriveDirectory(char drive) {
        int i = driveIndex(drive);
        if (i < 0) return null;
        String s = driveDirCache[i];
        if (s != null) return s;
        s = getDriveDirectory(i + 1);
        driveDirCache[i] = s;
        return s;
    }
    private String getUserPath() {
        return normalize(System.getProperty("user.dir"));
    }
    private String getDrive(String path) {
        int pl = prefixLength(path);
        return (pl == 3) ? path.substring(0, 2) : null;
    }
    public String resolve(File f) {
        String path = f.getPath();
        int pl = f.getPrefixLength();
        if ((pl == 2) && (path.charAt(0) == slash))
            return path;                        
        if (pl == 3)
            return path;                        
        if (pl == 0)
            return getUserPath() + slashify(path); 
        if (pl == 1) {                          
            String up = getUserPath();
            String ud = getDrive(up);
            if (ud != null) return ud + path;
            return up + path;                   
        }
        if (pl == 2) {                          
            String up = getUserPath();
            String ud = getDrive(up);
            if ((ud != null) && path.startsWith(ud))
                return up + slashify(path.substring(2));
            char drive = path.charAt(0);
            String dir = getDriveDirectory(drive);
            String np;
            if (dir != null) {
                String p = drive + (':' + dir + slashify(path.substring(2)));
                SecurityManager security = System.getSecurityManager();
                try {
                    if (security != null) security.checkRead(p);
                } catch (SecurityException x) {
                    throw new SecurityException("Cannot resolve path " + path);
                }
                return p;
            }
            return drive + ":" + slashify(path.substring(2)); 
        }
        throw new InternalError("Unresolvable path: " + path);
    }
    private ExpiringCache cache       = new ExpiringCache();
    private ExpiringCache prefixCache = new ExpiringCache();
    public String canonicalize(String path) throws IOException {
        int len = path.length();
        if ((len == 2) &&
            (isLetter(path.charAt(0))) &&
            (path.charAt(1) == ':')) {
            char c = path.charAt(0);
            if ((c >= 'A') && (c <= 'Z'))
                return path;
            return "" + ((char) (c-32)) + ':';
        } else if ((len == 3) &&
                   (isLetter(path.charAt(0))) &&
                   (path.charAt(1) == ':') &&
                   (path.charAt(2) == '\\')) {
            char c = path.charAt(0);
            if ((c >= 'A') && (c <= 'Z'))
                return path;
            return "" + ((char) (c-32)) + ':' + '\\';
        }
        if (!useCanonCaches) {
            return canonicalize0(path);
        } else {
            String res = cache.get(path);
            if (res == null) {
                String dir = null;
                String resDir = null;
                if (useCanonPrefixCache) {
                    dir = parentOrNull(path);
                    if (dir != null) {
                        resDir = prefixCache.get(dir);
                        if (resDir != null) {
                            String filename = path.substring(1 + dir.length());
                            res = canonicalizeWithPrefix(resDir, filename);
                            cache.put(dir + File.separatorChar + filename, res);
                        }
                    }
                }
                if (res == null) {
                    res = canonicalize0(path);
                    cache.put(path, res);
                    if (useCanonPrefixCache && dir != null) {
                        resDir = parentOrNull(res);
                        if (resDir != null) {
                            File f = new File(res);
                            if (f.exists() && !f.isDirectory()) {
                                prefixCache.put(dir, resDir);
                            }
                        }
                    }
                }
            }
            return res;
        }
    }
    protected native String canonicalize0(String path)
                                                throws IOException;
    protected String canonicalizeWithPrefix(String canonicalPrefix,
                                            String filename) throws IOException
    {
        return canonicalizeWithPrefix0(canonicalPrefix,
                                       canonicalPrefix + File.separatorChar + filename);
    }
    protected native String canonicalizeWithPrefix0(String canonicalPrefix,
                                                    String pathWithCanonicalPrefix)
                                                throws IOException;
    static String parentOrNull(String path) {
        if (path == null) return null;
        char sep = File.separatorChar;
        char altSep = '/';
        int last = path.length() - 1;
        int idx = last;
        int adjacentDots = 0;
        int nonDotCount = 0;
        while (idx > 0) {
            char c = path.charAt(idx);
            if (c == '.') {
                if (++adjacentDots >= 2) {
                    return null;
                }
                if (nonDotCount == 0) {
                    return null;
                }
            } else if (c == sep) {
                if (adjacentDots == 1 && nonDotCount == 0) {
                    return null;
                }
                if (idx == 0 ||
                    idx >= last - 1 ||
                    path.charAt(idx - 1) == sep ||
                    path.charAt(idx - 1) == altSep) {
                    return null;
                }
                return path.substring(0, idx);
            } else if (c == altSep) {
                return null;
            } else if (c == '*' || c == '?') {
                return null;
            } else {
                ++nonDotCount;
                adjacentDots = 0;
            }
            --idx;
        }
        return null;
    }
    public native int getBooleanAttributes(File f);
    public native boolean checkAccess(File f, int access);
    public native long getLastModifiedTime(File f);
    public native long getLength(File f);
    public native boolean setPermission(File f, int access, boolean enable, boolean owneronly);
    public native boolean createFileExclusively(String path)
        throws IOException;
    public boolean delete(File f) {
        cache.clear();
        prefixCache.clear();
        return delete0(f);
    }
    protected native boolean delete0(File f);
    public native String[] list(File f);
    public native boolean createDirectory(File f);
    public boolean rename(File f1, File f2) {
        cache.clear();
        prefixCache.clear();
        return rename0(f1, f2);
    }
    protected native boolean rename0(File f1, File f2);
    public native boolean setLastModifiedTime(File f, long time);
    public native boolean setReadOnly(File f);
    private boolean access(String path) {
        try {
            SecurityManager security = System.getSecurityManager();
            if (security != null) security.checkRead(path);
            return true;
        } catch (SecurityException x) {
            return false;
        }
    }
    private static native int listRoots0();
    public File[] listRoots() {
        int ds = listRoots0();
        int n = 0;
        for (int i = 0; i < 26; i++) {
            if (((ds >> i) & 1) != 0) {
                if (!access((char)('A' + i) + ":" + slash))
                    ds &= ~(1 << i);
                else
                    n++;
            }
        }
        File[] fs = new File[n];
        int j = 0;
        char slash = this.slash;
        for (int i = 0; i < 26; i++) {
            if (((ds >> i) & 1) != 0)
                fs[j++] = new File((char)('A' + i) + ":" + slash);
        }
        return fs;
    }
    public long getSpace(File f, int t) {
        if (f.exists()) {
            File file = (f.isDirectory() ? f : f.getParentFile());
            return getSpace0(file, t);
        }
        return 0;
    }
    private native long getSpace0(File f, int t);
    public int compare(File f1, File f2) {
        return f1.getPath().compareToIgnoreCase(f2.getPath());
    }
    public int hashCode(File f) {
        return f.getPath().toLowerCase(Locale.ENGLISH).hashCode() ^ 1234321;
    }
    private static native void initIDs();
    static {
        initIDs();
    }
}
