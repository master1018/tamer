class ClassPath {
    static final char dirSeparator = File.pathSeparatorChar;
    String pathstr;
    private ClassPathEntry[] path;
    public ClassPath(String pathstr) {
        init(pathstr);
    }
    public ClassPath(String[] patharray) {
        init(patharray);
    }
    public ClassPath() {
        String syscp = System.getProperty("sun.boot.class.path");
        String envcp = System.getProperty("env.class.path");
        if (envcp == null) envcp = ".";
        String cp = syscp + File.pathSeparator + envcp;
        init(cp);
    }
    private void init(String pathstr) {
        int i, j, n;
        this.pathstr = pathstr;
        if (pathstr.length() == 0) {
            this.path = new ClassPathEntry[0];
        }
        i = n = 0;
        while ((i = pathstr.indexOf(dirSeparator, i)) != -1) {
            n++; i++;
        }
        ClassPathEntry[] path = new ClassPathEntry[n+1];
        int len = pathstr.length();
        for (i = n = 0; i < len; i = j + 1) {
            if ((j = pathstr.indexOf(dirSeparator, i)) == -1) {
                j = len;
            }
            if (i == j) {
                path[n] = new ClassPathEntry();
                path[n++].dir = new File(".");
            } else {
                File file = new File(pathstr.substring(i, j));
                if (file.isFile()) {
                    try {
                        ZipFile zip = new ZipFile(file);
                        path[n] = new ClassPathEntry();
                        path[n++].zip = zip;
                    } catch (ZipException e) {
                    } catch (IOException e) {
                    }
                } else {
                    path[n] = new ClassPathEntry();
                    path[n++].dir = file;
                }
            }
        }
        this.path = new ClassPathEntry[n];
        System.arraycopy((Object)path, 0, (Object)this.path, 0, n);
    }
    private void init(String[] patharray) {
        if (patharray.length == 0) {
            this.pathstr = "";
        } else {
            StringBuilder sb = new StringBuilder(patharray[0]);
            for (int i = 1; i < patharray.length; i++) {
                sb.append(File.separator);
                sb.append(patharray[i]);
            }
            this.pathstr = sb.toString();
        }
        ClassPathEntry[] path = new ClassPathEntry[patharray.length];
        int n = 0;
        for (String name : patharray) {
            File file = new File(name);
            if (file.isFile()) {
                try {
                    ZipFile zip = new ZipFile(file);
                    path[n] = new ClassPathEntry();
                    path[n++].zip = zip;
                } catch (ZipException e) {
                } catch (IOException e) {
                }
            } else {
                path[n] = new ClassPathEntry();
                path[n++].dir = file;
            }
        }
        this.path = new ClassPathEntry[n];
        System.arraycopy((Object)path, 0, (Object)this.path, 0, n);
    }
    public ClassFile getDirectory(String name) {
        return getFile(name, true);
    }
    public ClassFile getFile(String name) {
        return getFile(name, false);
    }
    private final String fileSeparatorChar = "" + File.separatorChar;
    private ClassFile getFile(String name, boolean isDirectory) {
        String subdir = name;
        String basename = "";
        if (!isDirectory) {
            int i = name.lastIndexOf(File.separatorChar);
            subdir = name.substring(0, i + 1);
            basename = name.substring(i + 1);
        } else if (!subdir.equals("")
                   && !subdir.endsWith(fileSeparatorChar)) {
            subdir = subdir + File.separatorChar;
            name = subdir;      
        }
        for (int i = 0; i < path.length; i++) {
            if (path[i].zip != null) {
                String newname = name.replace(File.separatorChar, '/');
                ZipEntry entry = path[i].zip.getEntry(newname);
                if (entry != null) {
                    return new ClassFile(path[i].zip, entry);
                }
            } else {
                File file = new File(path[i].dir.getPath(), name);
                String list[] = path[i].getFiles(subdir);
                if (isDirectory) {
                    if (list.length > 0) {
                        return new ClassFile(file);
                    }
                } else {
                    for (int j = 0; j < list.length; j++) {
                        if (basename.equals(list[j])) {
                            return new ClassFile(file);
                        }
                    }
                }
            }
        }
        return null;
    }
    public Enumeration getFiles(String pkg, String ext) {
        Hashtable files = new Hashtable();
        for (int i = path.length; --i >= 0; ) {
            if (path[i].zip != null) {
                Enumeration e = path[i].zip.entries();
                while (e.hasMoreElements()) {
                    ZipEntry entry = (ZipEntry)e.nextElement();
                    String name = entry.getName();
                    name = name.replace('/', File.separatorChar);
                    if (name.startsWith(pkg) && name.endsWith(ext)) {
                        files.put(name, new ClassFile(path[i].zip, entry));
                    }
                }
            } else {
                String[] list = path[i].getFiles(pkg);
                for (int j = 0; j < list.length; j++) {
                    String name = list[j];
                    if (name.endsWith(ext)) {
                        name = pkg + File.separatorChar + name;
                        File file = new File(path[i].dir.getPath(), name);
                        files.put(name, new ClassFile(file));
                    }
                }
            }
        }
        return files.elements();
    }
    public void close() throws IOException {
        for (int i = path.length; --i >= 0; ) {
            if (path[i].zip != null) {
                path[i].zip.close();
            }
        }
    }
    public String toString() {
        return pathstr;
    }
}
class ClassPathEntry {
    File dir;
    ZipFile zip;
    Hashtable subdirs = new Hashtable(29); 
    String[] getFiles(String subdir) {
        String files[] = (String[]) subdirs.get(subdir);
        if (files == null) {
            File sd = new File(dir.getPath(), subdir);
            if (sd.isDirectory()) {
                files = sd.list();
                if (files == null) {
                    files = new String[0];
                }
                if (files.length == 0) {
                    String nonEmpty[] = { "" };
                    files = nonEmpty;
                }
            } else {
                files = new String[0];
            }
            subdirs.put(subdir, files);
        }
        return files;
    }
}
