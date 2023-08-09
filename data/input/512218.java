public class FileEntity extends AbstractHttpEntity implements Cloneable {
    protected final File file; 
    public FileEntity(final File file, final String contentType) {
        super();
        if (file == null) {
            throw new IllegalArgumentException("File may not be null");
        }
        this.file = file;
        setContentType(contentType);
    }
    public boolean isRepeatable() {
        return true;
    }
    public long getContentLength() {
        return this.file.length();
    }
    public InputStream getContent() throws IOException {
        return new FileInputStream(this.file);
    }
    public void writeTo(final OutputStream outstream) throws IOException {
        if (outstream == null) {
            throw new IllegalArgumentException("Output stream may not be null");
        }
        InputStream instream = new FileInputStream(this.file);
        try {
            byte[] tmp = new byte[4096];
            int l;
            while ((l = instream.read(tmp)) != -1) {
                outstream.write(tmp, 0, l);
            }
            outstream.flush();
        } finally {
            instream.close();
        }
    }
    public boolean isStreaming() {
        return false;
    }
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
} 
