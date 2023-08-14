public class ZipArchive implements Archive {
    public ZipArchive(JavacFileManager fm, ZipFile zfile) throws IOException {
        this(fm, zfile, true);
    }
    protected ZipArchive(JavacFileManager fm, ZipFile zfile, boolean initMap) throws IOException {
        this.fileManager = fm;
        this.zfile = zfile;
        this.map = new HashMap<RelativeDirectory,List<String>>();
        if (initMap)
            initMap();
    }
    protected void initMap() throws IOException {
        for (Enumeration<? extends ZipEntry> e = zfile.entries(); e.hasMoreElements(); ) {
            ZipEntry entry;
            try {
                entry = e.nextElement();
            } catch (InternalError ex) {
                IOException io = new IOException();
                io.initCause(ex); 
                throw io;
            }
            addZipEntry(entry);
        }
    }
    void addZipEntry(ZipEntry entry) {
        String name = entry.getName();
        int i = name.lastIndexOf('/');
        RelativeDirectory dirname = new RelativeDirectory(name.substring(0, i+1));
        String basename = name.substring(i+1);
        if (basename.length() == 0)
            return;
        List<String> list = map.get(dirname);
        if (list == null)
            list = List.nil();
        list = list.prepend(basename);
        map.put(dirname, list);
    }
    public boolean contains(RelativePath name) {
        RelativeDirectory dirname = name.dirname();
        String basename = name.basename();
        if (basename.length() == 0)
            return false;
        List<String> list = map.get(dirname);
        return (list != null && list.contains(basename));
    }
    public List<String> getFiles(RelativeDirectory subdirectory) {
        return map.get(subdirectory);
    }
    public JavaFileObject getFileObject(RelativeDirectory subdirectory, String file) {
        ZipEntry ze = new RelativeFile(subdirectory, file).getZipEntry(zfile);
        return new ZipFileObject(this, file, ze);
    }
    public Set<RelativeDirectory> getSubdirectories() {
        return map.keySet();
    }
    public void close() throws IOException {
        zfile.close();
    }
    @Override
    public String toString() {
        return "ZipArchive[" + zfile.getName() + "]";
    }
    private File getAbsoluteFile() {
        File absFile = (absFileRef == null ? null : absFileRef.get());
        if (absFile == null) {
            absFile = new File(zfile.getName()).getAbsoluteFile();
            absFileRef = new SoftReference<File>(absFile);
        }
        return absFile;
    }
    protected JavacFileManager fileManager;
    protected final Map<RelativeDirectory,List<String>> map;
    protected final ZipFile zfile;
    protected Reference<File> absFileRef;
    public static class ZipFileObject extends BaseFileObject {
        private String name;
        ZipArchive zarch;
        ZipEntry entry;
        protected ZipFileObject(ZipArchive zarch, String name, ZipEntry entry) {
            super(zarch.fileManager);
            this.zarch = zarch;
            this.name = name;
            this.entry = entry;
        }
        public URI toUri() {
            File zipFile = new File(zarch.zfile.getName());
            return createJarUri(zipFile, entry.getName());
        }
        @Override
        public String getName() {
            return zarch.zfile.getName() + "(" + entry.getName() + ")";
        }
        @Override
        public String getShortName() {
            return new File(zarch.zfile.getName()).getName() + "(" + entry + ")";
        }
        @Override
        public JavaFileObject.Kind getKind() {
            return getKind(entry.getName());
        }
        @Override
        public InputStream openInputStream() throws IOException {
            return zarch.zfile.getInputStream(entry);
        }
        @Override
        public OutputStream openOutputStream() throws IOException {
            throw new UnsupportedOperationException();
        }
        @Override
        public CharBuffer getCharContent(boolean ignoreEncodingErrors) throws IOException {
            CharBuffer cb = fileManager.getCachedContent(this);
            if (cb == null) {
                InputStream in = zarch.zfile.getInputStream(entry);
                try {
                    ByteBuffer bb = fileManager.makeByteBuffer(in);
                    JavaFileObject prev = fileManager.log.useSource(this);
                    try {
                        cb = fileManager.decode(bb, ignoreEncodingErrors);
                    } finally {
                        fileManager.log.useSource(prev);
                    }
                    fileManager.recycleByteBuffer(bb);
                    if (!ignoreEncodingErrors) {
                        fileManager.cache(this, cb);
                    }
                } finally {
                    in.close();
                }
            }
            return cb;
        }
        @Override
        public Writer openWriter() throws IOException {
            throw new UnsupportedOperationException();
        }
        @Override
        public long getLastModified() {
            return entry.getTime();
        }
        @Override
        public boolean delete() {
            throw new UnsupportedOperationException();
        }
        @Override
        protected CharsetDecoder getDecoder(boolean ignoreEncodingErrors) {
            return fileManager.getDecoder(fileManager.getEncodingName(), ignoreEncodingErrors);
        }
        @Override
        protected String inferBinaryName(Iterable<? extends File> path) {
            String entryName = entry.getName();
            return removeExtension(entryName).replace('/', '.');
        }
        @Override
        public boolean isNameCompatible(String cn, JavaFileObject.Kind k) {
            cn.getClass();
            if (k == Kind.OTHER && getKind() != k) {
                return false;
            }
            return name.equals(cn + k.extension);
        }
        @Override
        public boolean equals(Object other) {
            if (this == other)
                return true;
            if (!(other instanceof ZipFileObject))
                return false;
            ZipFileObject o = (ZipFileObject) other;
            return zarch.getAbsoluteFile().equals(o.zarch.getAbsoluteFile())
                    && name.equals(o.name);
        }
        @Override
        public int hashCode() {
            return zarch.getAbsoluteFile().hashCode() + name.hashCode();
        }
    }
}
