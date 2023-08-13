public abstract class DirectoryWalker {
    private final FileFilter filter;
    private final int depthLimit;
    protected DirectoryWalker() {
        this(null, -1);
    }
    protected DirectoryWalker(FileFilter filter, int depthLimit) {
        this.filter = filter;
        this.depthLimit = depthLimit;
    }
    protected DirectoryWalker(IOFileFilter directoryFilter, IOFileFilter fileFilter, int depthLimit) {
        if (directoryFilter == null && fileFilter == null) {
            this.filter = null;
        } else {
            directoryFilter = (directoryFilter != null ? directoryFilter : TrueFileFilter.TRUE);
            fileFilter = (fileFilter != null ? fileFilter : TrueFileFilter.TRUE);
            directoryFilter = FileFilterUtils.makeDirectoryOnly(directoryFilter);
            fileFilter = FileFilterUtils.makeFileOnly(fileFilter);
            this.filter = FileFilterUtils.orFileFilter(directoryFilter, fileFilter);
        }
        this.depthLimit = depthLimit;
    }
    protected final void walk(File startDirectory, Collection results) throws IOException {
        if (startDirectory == null) {
            throw new NullPointerException("Start Directory is null");
        }
        try {
            handleStart(startDirectory, results);
            walk(startDirectory, 0, results);
            handleEnd(results);
        } catch(CancelException cancel) {
            handleCancelled(startDirectory, results, cancel);
        }
    }
    private void walk(File directory, int depth, Collection results) throws IOException {
        checkIfCancelled(directory, depth, results);
        if (handleDirectory(directory, depth, results)) {
            handleDirectoryStart(directory, depth, results);
            int childDepth = depth + 1;
            if (depthLimit < 0 || childDepth <= depthLimit) {
                checkIfCancelled(directory, depth, results);
                File[] childFiles = (filter == null ? directory.listFiles() : directory.listFiles(filter));
                if (childFiles == null) {
                    handleRestricted(directory, childDepth, results);
                } else {
                    for (int i = 0; i < childFiles.length; i++) {
                        File childFile = childFiles[i];
                        if (childFile.isDirectory()) {
                            walk(childFile, childDepth, results);
                        } else {
                            checkIfCancelled(childFile, childDepth, results);
                            handleFile(childFile, childDepth, results);
                            checkIfCancelled(childFile, childDepth, results);
                        }
                    }
                }
            }
            handleDirectoryEnd(directory, depth, results);
        }
        checkIfCancelled(directory, depth, results);
    }
    protected final void checkIfCancelled(File file, int depth, Collection results) throws IOException {
        if (handleIsCancelled(file, depth, results)) {
            throw new CancelException(file, depth);
        }
    }
    protected boolean handleIsCancelled(
            File file, int depth, Collection results) throws IOException {
        return false;  
    }
    protected void handleCancelled(File startDirectory, Collection results,
                       CancelException cancel) throws IOException {
        throw cancel;
    }
    protected void handleStart(File startDirectory, Collection results) throws IOException {
    }
    protected boolean handleDirectory(File directory, int depth, Collection results) throws IOException {
        return true;  
    }
    protected void handleDirectoryStart(File directory, int depth, Collection results) throws IOException {
    }
    protected void handleFile(File file, int depth, Collection results) throws IOException {
    }
    protected void handleRestricted(File directory, int depth, Collection results) throws IOException  {
    }
    protected void handleDirectoryEnd(File directory, int depth, Collection results) throws IOException {
    }
    protected void handleEnd(Collection results) throws IOException {
    }
    public static class CancelException extends IOException {
        private static final long serialVersionUID = 1347339620135041008L;
        private File file;
        private int depth = -1;
        public CancelException(File file, int depth) {
            this("Operation Cancelled", file, depth);
        }
        public CancelException(String message, File file, int depth) {
            super(message);
            this.file = file;
            this.depth = depth;
        }
        public File getFile() {
            return file;
        }
        public int getDepth() {
            return depth;
        }
    }
}
