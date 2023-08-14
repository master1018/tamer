public class ZipFileIndexArchive implements Archive {
    private final ZipFileIndex zfIndex;
    private JavacFileManager fileManager;
    public ZipFileIndexArchive(JavacFileManager fileManager, ZipFileIndex zdir) throws IOException {
        super();
        this.fileManager = fileManager;
        this.zfIndex = zdir;
    }
    public boolean contains(RelativePath name) {
        return zfIndex.contains(name);
    }
    public List<String> getFiles(RelativeDirectory subdirectory) {
        return zfIndex.getFiles(subdirectory);
    }
    public JavaFileObject getFileObject(RelativeDirectory subdirectory, String file) {
        RelativeFile fullZipFileName = new RelativeFile(subdirectory, file);
        ZipFileIndex.Entry entry = zfIndex.getZipIndexEntry(fullZipFileName);
        JavaFileObject ret = new ZipFileIndexFileObject(fileManager, zfIndex, entry, zfIndex.getZipFile());
        return ret;
    }
    public Set<RelativeDirectory> getSubdirectories() {
        return zfIndex.getAllDirectories();
    }
    public void close() throws IOException {
        zfIndex.close();
    }
    @Override
    public String toString() {
        return "ZipFileIndexArchive[" + zfIndex + "]";
    }
    public static class ZipFileIndexFileObject extends BaseFileObject {
        private String name;
        ZipFileIndex zfIndex;
        ZipFileIndex.Entry entry;
        InputStream inputStream = null;
        File zipName;
        ZipFileIndexFileObject(JavacFileManager fileManager, ZipFileIndex zfIndex, ZipFileIndex.Entry entry, File zipFileName) {
            super(fileManager);
            this.name = entry.getFileName();
            this.zfIndex = zfIndex;
            this.entry = entry;
            this.zipName = zipFileName;
        }
        @Override
        public URI toUri() {
            return createJarUri(zipName, getPrefixedEntryName());
        }
        @Override
        public String getName() {
            return zipName + "(" + getPrefixedEntryName() + ")";
        }
        @Override
        public String getShortName() {
            return zipName.getName() + "(" + entry.getName() + ")";
        }
        @Override
        public JavaFileObject.Kind getKind() {
            return getKind(entry.getName());
        }
        @Override
        public InputStream openInputStream() throws IOException {
            if (inputStream == null) {
                Assert.checkNonNull(entry); 
                inputStream = new ByteArrayInputStream(zfIndex.read(entry));
            }
            return inputStream;
        }
        @Override
        public OutputStream openOutputStream() throws IOException {
            throw new UnsupportedOperationException();
        }
        @Override
        public CharBuffer getCharContent(boolean ignoreEncodingErrors) throws IOException {
            CharBuffer cb = fileManager.getCachedContent(this);
            if (cb == null) {
                InputStream in = new ByteArrayInputStream(zfIndex.read(entry));
                try {
                    ByteBuffer bb = fileManager.makeByteBuffer(in);
                    JavaFileObject prev = fileManager.log.useSource(this);
                    try {
                        cb = fileManager.decode(bb, ignoreEncodingErrors);
                    } finally {
                        fileManager.log.useSource(prev);
                    }
                    fileManager.recycleByteBuffer(bb); 
                    if (!ignoreEncodingErrors)
                        fileManager.cache(this, cb);
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
            return entry.getLastModified();
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
            if (zfIndex.symbolFilePrefix != null) {
                String prefix = zfIndex.symbolFilePrefix.path;
                if (entryName.startsWith(prefix))
                    entryName = entryName.substring(prefix.length());
            }
            return removeExtension(entryName).replace('/', '.');
        }
        @Override
        public boolean isNameCompatible(String cn, JavaFileObject.Kind k) {
            cn.getClass(); 
            if (k == Kind.OTHER && getKind() != k)
                return false;
            return name.equals(cn + k.extension);
        }
        @Override
        public boolean equals(Object other) {
            if (this == other)
                return true;
            if (!(other instanceof ZipFileIndexFileObject))
                return false;
            ZipFileIndexFileObject o = (ZipFileIndexFileObject) other;
            return zfIndex.getAbsoluteFile().equals(o.zfIndex.getAbsoluteFile())
                    && name.equals(o.name);
        }
        @Override
        public int hashCode() {
            return zfIndex.getAbsoluteFile().hashCode() + name.hashCode();
        }
        private String getPrefixedEntryName() {
            if (zfIndex.symbolFilePrefix != null)
                return zfIndex.symbolFilePrefix.path + entry.getName();
            else
                return entry.getName();
        }
    }
}
