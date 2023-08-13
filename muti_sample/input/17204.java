public class FSInfo {
    public static FSInfo instance(Context context) {
        FSInfo instance = context.get(FSInfo.class);
        if (instance == null)
            instance = new FSInfo();
        return instance;
    }
    protected FSInfo() {
    }
    protected FSInfo(Context context) {
        context.put(FSInfo.class, this);
    }
    public File getCanonicalFile(File file) {
        try {
            return file.getCanonicalFile();
        } catch (IOException e) {
            return file.getAbsoluteFile();
        }
    }
    public boolean exists(File file) {
        return file.exists();
    }
    public boolean isDirectory(File file) {
        return file.isDirectory();
    }
    public boolean isFile(File file) {
        return file.isFile();
    }
    public List<File> getJarClassPath(File file) throws IOException {
        String parent = file.getParent();
        JarFile jarFile = new JarFile(file);
        try {
            Manifest man = jarFile.getManifest();
            if (man == null)
                return Collections.emptyList();
            Attributes attr = man.getMainAttributes();
            if (attr == null)
                return Collections.emptyList();
            String path = attr.getValue(Attributes.Name.CLASS_PATH);
            if (path == null)
                return Collections.emptyList();
            List<File> list = new ArrayList<File>();
            for (StringTokenizer st = new StringTokenizer(path); st.hasMoreTokens(); ) {
                String elt = st.nextToken();
                File f = (parent == null ? new File(elt) : new File(parent, elt));
                list.add(f);
            }
            return list;
        } finally {
            jarFile.close();
        }
    }
}
