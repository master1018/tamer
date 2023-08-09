public class FileWrapper extends File implements IAbstractFile {
    private static final long serialVersionUID = 1L;
    public FileWrapper(File parent, String child) {
        super(parent, child);
    }
    public FileWrapper(String pathname) {
        super(pathname);
    }
    public FileWrapper(String parent, String child) {
        super(parent, child);
    }
    public FileWrapper(URI uri) {
        super(uri);
    }
    public FileWrapper(File file) {
        super(file.getAbsolutePath());
    }
    public InputStream getContents() throws StreamException {
        try {
            return new FileInputStream(this);
        } catch (FileNotFoundException e) {
            throw new StreamException(e);
        }
    }
    public void setContents(InputStream source) throws StreamException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(this);
            byte[] buffer = new byte[1024];
            int count = 0;
            while ((count = source.read(buffer)) != -1) {
                fos.write(buffer, 0, count);
            }
        } catch (IOException e) {
            throw new StreamException(e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    throw new StreamException(e);
                }
            }
        }
    }
    public String getOsLocation() {
        return getAbsolutePath();
    }
    @Override
    public boolean exists() {
        return isFile();
    }
}
