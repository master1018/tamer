public class DirectoryManager {
    public static final String URL_FILE_SEPARATOR = "/";
    private DirectoryManager() {
    }
    public static String createPathString(PackageDoc pd) {
        if (pd == null) {
            return "";
        }
        return getPath(pd.name());
    }
    public static String createPathString(ClassDoc cd) {
        if (cd == null) {
            return "";
        }
        PackageDoc pd = cd.containingPackage();
        return (pd == null)? "": getPath(pd.name());
    }
    public static String getDirectoryPath(PackageDoc pd) {
        return pd == null || pd.name().length() == 0 ? "" : getDirectoryPath(pd.name());
    }
    public static String getDirectoryPath(String packageName) {
        if (packageName == null || packageName.length() == 0) {
            return "";
        }
        StringBuffer pathstr = new StringBuffer();
        for (int i = 0; i < packageName.length(); i++) {
            char ch = packageName.charAt(i);
            if (ch == '.') {
                pathstr.append(URL_FILE_SEPARATOR);
            } else {
                pathstr.append(ch);
            }
        }
        if (pathstr.length() > 0 && ! pathstr.toString().endsWith(URL_FILE_SEPARATOR)) {
            pathstr.append(URL_FILE_SEPARATOR);
        }
        return pathstr.toString();
    }
    public static String getPath(String name) {
        if (name == null || name.length() == 0) {
            return "";
        }
        StringBuffer pathstr = new StringBuffer();
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (ch == '.') {
                pathstr.append(URL_FILE_SEPARATOR);
            } else {
                pathstr.append(ch);
            }
        }
        return pathstr.toString();
    }
    public static String getRelativePath(String from, String to) {
        StringBuffer pathstr = new StringBuffer();
        pathstr.append(getRelativePath(from));
        pathstr.append(getPath(to));
        pathstr.append(URL_FILE_SEPARATOR);
        return pathstr.toString();
    }
    public static String getRelativePath(PackageDoc from) {
        return from == null || from.name().length() == 0 ? "" : getRelativePath(from.name());
    }
    public static String getRelativePath(String from) {
        if (from == null || from.length() == 0) {
            return "";
        }
        StringBuffer pathstr = new StringBuffer();
        for (int i = 0; i < from.length(); i++) {
            char ch = from.charAt(i);
            if (ch == '.') {
                pathstr.append(".." + URL_FILE_SEPARATOR);
            }
        }
        pathstr.append(".." + URL_FILE_SEPARATOR);
        return pathstr.toString();
    }
    public static String getPathNoTrailingSlash(String path) {
        if ( path.equals("") ) {
            return ".";
        }
        if ( path.equals("/") ) {
            return "/.";
        }
        if ( path.endsWith("/") ) {
            path = path.substring(0, path.length() -1);
        }
        return path;
    }
    public static void createDirectory(Configuration configuration,
                                       String path) {
        if (path == null || path.length() == 0) {
            return;
        }
        File dir = new File(path);
        if (dir.exists()) {
            return;
        } else {
            if (dir.mkdirs()) {
                return;
            } else {
                configuration.message.error(
                       "doclet.Unable_to_create_directory_0", path);
                throw new DocletAbortException();
            }
        }
    }
    public static String getPathToPackage(PackageDoc pd, String filename) {
        StringBuffer buf = new StringBuffer();
        String pathstr = createPathString(pd);
        if (pathstr.length() > 0) {
            buf.append(pathstr);
            buf.append(URL_FILE_SEPARATOR);
        }
        buf.append(filename);
        return buf.toString();
    }
    public static String getPathToClass(ClassDoc cd) {
        return getPathToPackage(cd.containingPackage(), cd.name() + ".html");
    }
}
