public class FolderWrapper extends File implements IAbstractFolder {
    private static final long serialVersionUID = 1L;
    public FolderWrapper(File parent, String child) {
        super(parent, child);
    }
    public FolderWrapper(String pathname) {
        super(pathname);
    }
    public FolderWrapper(String parent, String child) {
        super(parent, child);
    }
    public FolderWrapper(URI uri) {
        super(uri);
    }
    public FolderWrapper(File file) {
        super(file.getAbsolutePath());
    }
    public IAbstractResource[] listMembers() {
        File[] files = listFiles();
        final int count = files.length;
        IAbstractResource[] afiles = new IAbstractResource[count];
        for (int i = 0 ; i < count ; i++) {
            File f = files[i];
            if (f.isFile()) {
                afiles[i] = new FileWrapper(f);
            } else {
                afiles[i] = new FolderWrapper(f);
            }
        }
        return afiles;
    }
    public boolean hasFile(final String name) {
        String[] match = list(new FilenameFilter() {
            public boolean accept(File dir, String filename) {
                return name.equals(filename);
            }
        });
        return match.length > 0;
    }
    public IAbstractFile getFile(String name) {
        return new FileWrapper(this, name);
    }
    @Override
    public boolean exists() {
        return isDirectory();
    }
}
