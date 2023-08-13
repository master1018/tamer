public class SimpleTempStorage extends TempStorage {
    private static Log log = LogFactory.getLog(SimpleTempStorage.class);
    private TempPath rootPath = null;
    private Random random = new Random();
    public SimpleTempStorage() {
        rootPath = new SimpleTempPath(System.getProperty("java.io.tmpdir"));
    }
    private TempPath createTempPath(TempPath parent, String prefix) 
            throws IOException {
        if (prefix == null) {
            prefix = "";
        }
        File p = null;
        int count = 1000;
        do {
            long n = Math.abs(random.nextLong());
            p = new File(parent.getAbsolutePath(), prefix + n);
            count--;
        } while (p.exists() && count > 0);
        if (p.exists() || !p.mkdirs()) {
            log.error("Unable to mkdirs on " + p.getAbsolutePath());
            throw new IOException("Creating dir '" 
                                    + p.getAbsolutePath() + "' failed."); 
        }
        return new SimpleTempPath(p);
    }
    private TempFile createTempFile(TempPath parent, String prefix, 
                                    String suffix) throws IOException {
        if (prefix == null) {
            prefix = "";
        }
        if (suffix == null) {
            suffix = ".tmp";
        }
        File f = null;
        int count = 1000;
        synchronized (this) {
            do {
                long n = Math.abs(random.nextLong());
                f = new File(parent.getAbsolutePath(), prefix + n + suffix);
                count--;
            } while (f.exists() && count > 0);
            if (f.exists()) {
                throw new IOException("Creating temp file failed: "
                                         + "Unable to find unique file name");
            }
            try {
                f.createNewFile();
            } catch (IOException e) {
                throw new IOException("Creating dir '" 
                                        + f.getAbsolutePath() + "' failed."); 
            }
        }
        return new SimpleTempFile(f);
    }
    public TempPath getRootTempPath() {
        return rootPath;
    }
    private class SimpleTempPath implements TempPath {
        private File path = null;
        private SimpleTempPath(String path) {
            this.path = new File(path);
        }
        private SimpleTempPath(File path) {
            this.path = path;
        }
        public TempFile createTempFile() throws IOException {
            return SimpleTempStorage.this.createTempFile(this, null, null);
        }
        public TempFile createTempFile(String prefix, String suffix) 
                throws IOException {
            return SimpleTempStorage.this.createTempFile(this, prefix, suffix);
        }
        public TempFile createTempFile(String prefix, String suffix, 
                                       boolean allowInMemory) 
            throws IOException {
            return SimpleTempStorage.this.createTempFile(this, prefix, suffix);
        }
        public String getAbsolutePath() {
            return path.getAbsolutePath();
        }
        public void delete() {
        }
        public TempPath createTempPath() throws IOException {
            return SimpleTempStorage.this.createTempPath(this, null);
        }
        public TempPath createTempPath(String prefix) throws IOException {
            return SimpleTempStorage.this.createTempPath(this, prefix);
        }
    }
    private class SimpleTempFile implements TempFile {
        private File file = null;
        private SimpleTempFile(File file) {
            this.file = file;
            this.file.deleteOnExit();
        }
        public InputStream getInputStream() throws IOException {
            return new BufferedInputStream(new FileInputStream(file));
        }
        public OutputStream getOutputStream() throws IOException {
            return new BufferedOutputStream(new FileOutputStream(file));
        }
        public String getAbsolutePath() {
            return file.getAbsolutePath();
        }
        public void delete() {
        }
        public boolean isInMemory() {
            return false;
        }
        public long length() {
            return file.length();
        }
    }
}
