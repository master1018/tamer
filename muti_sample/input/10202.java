public class SymbolArchive extends ZipArchive {
    final File origFile;
    final RelativeDirectory prefix;
    public SymbolArchive(JavacFileManager fileManager, File orig, ZipFile zdir, RelativeDirectory prefix) throws IOException {
        super(fileManager, zdir, false);
        this.origFile = orig;
        this.prefix = prefix;
        initMap();
    }
    @Override
    void addZipEntry(ZipEntry entry) {
        String name = entry.getName();
        if (!name.startsWith(prefix.path)) {
            return;
        }
        name = name.substring(prefix.path.length());
        int i = name.lastIndexOf('/');
        RelativeDirectory dirname = new RelativeDirectory(name.substring(0, i+1));
        String basename = name.substring(i + 1);
        if (basename.length() == 0) {
            return;
        }
        List<String> list = map.get(dirname);
        if (list == null)
            list = List.nil();
        list = list.prepend(basename);
        map.put(dirname, list);
    }
    @Override
    public JavaFileObject getFileObject(RelativeDirectory subdirectory, String file) {
        RelativeDirectory prefix_subdir = new RelativeDirectory(prefix, subdirectory.path);
        ZipEntry ze = new RelativeFile(prefix_subdir, file).getZipEntry(zfile);
        return new SymbolFileObject(this, file, ze);
    }
    @Override
    public String toString() {
        return "SymbolArchive[" + zfile.getName() + "]";
    }
    public static class SymbolFileObject extends ZipFileObject {
        protected SymbolFileObject(SymbolArchive zarch, String name, ZipEntry entry) {
            super(zarch, name, entry);
        }
        @Override
        protected String inferBinaryName(Iterable<? extends File> path) {
            String entryName = entry.getName();
            String prefix = ((SymbolArchive) zarch).prefix.path;
            if (entryName.startsWith(prefix))
                entryName = entryName.substring(prefix.length());
            return removeExtension(entryName).replace('/', '.');
        }
    }
}
