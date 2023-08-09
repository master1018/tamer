public class CacheFSInfo extends FSInfo {
    public static void preRegister(Context context) {
        context.put(FSInfo.class, new Context.Factory<FSInfo>() {
            public FSInfo make(Context c) {
                FSInfo instance = new CacheFSInfo();
                c.put(FSInfo.class, instance);
                return instance;
            }
        });
    }
    public void clearCache() {
        cache.clear();
    }
    @Override
    public File getCanonicalFile(File file) {
        Entry e = getEntry(file);
        return e.canonicalFile;
    }
    @Override
    public boolean exists(File file) {
        Entry e = getEntry(file);
        return e.exists;
    }
    @Override
    public boolean isDirectory(File file) {
        Entry e = getEntry(file);
        return e.isDirectory;
    }
    @Override
    public boolean isFile(File file) {
        Entry e = getEntry(file);
        return e.isFile;
    }
    @Override
    public List<File> getJarClassPath(File file) throws IOException {
        Entry e = getEntry(file);
        if (e.jarClassPath == null)
            e.jarClassPath = super.getJarClassPath(file);
        return e.jarClassPath;
    }
    private Entry getEntry(File file) {
        Entry e = cache.get(file);
        if (e == null) {
            e = new Entry();
            e.canonicalFile = super.getCanonicalFile(file);
            e.exists = super.exists(file);
            e.isDirectory = super.isDirectory(file);
            e.isFile = super.isFile(file);
            cache.put(file, e);
        }
        return e;
    }
    private Map<File,Entry> cache = new ConcurrentHashMap<File,Entry>();
    private static class Entry {
        File canonicalFile;
        boolean exists;
        boolean isFile;
        boolean isDirectory;
        List<File> jarClassPath;
    }
}
