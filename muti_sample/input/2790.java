class FileContent implements Content {
    private static File ROOT = new File("root");
    private File fn;
    FileContent(URI uri) {
        fn = new File(ROOT,
                      uri.getPath()
                      .replace('/',
                               File.separatorChar));
    }
    private String type = null;
    public String type() {
        if (type != null)
            return type;
        String nm = fn.getName();
        if (nm.endsWith(".html"))
            type = "text/html; charset=iso-8859-1";
        else if ((nm.indexOf('.') < 0) || nm.endsWith(".txt"))
            type = "text/plain; charset=iso-8859-1";
        else
            type = "application/octet-stream";
        return type;
    }
    private FileChannel fc = null;
    private long length = -1;
    private long position = -1;         
    public long length() {
        return length;
    }
    public void prepare() throws IOException {
        if (fc == null)
            fc = new RandomAccessFile(fn, "r").getChannel();
        length = fc.size();
        position = 0;                   
    }
    public boolean send(ChannelIO cio) throws IOException {
        if (fc == null)
            throw new IllegalStateException();
        if (position < 0)               
            throw new IllegalStateException();
        if (position >= length) {
            return false;
        }
        position += cio.transferTo(fc, position, length - position);
        return (position < length);
    }
    public void release() throws IOException {
        if (fc != null) {
            fc.close();
            fc = null;
        }
    }
}
