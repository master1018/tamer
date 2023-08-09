public class FileURLConnection extends URLConnection {
    static String CONTENT_LENGTH = "content-length";
    static String CONTENT_TYPE = "content-type";
    static String TEXT_PLAIN = "text/plain";
    static String LAST_MODIFIED = "last-modified";
    String contentType;
    InputStream is;
    File file;
    String filename;
    boolean isDirectory = false;
    boolean exists = false;
    List<String> files;
    long length = -1;
    long lastModified = 0;
    protected FileURLConnection(URL u, File file) {
        super(u);
        this.file = file;
    }
    public void connect() throws IOException {
        if (!connected) {
            try {
                filename = file.toString();
                isDirectory = file.isDirectory();
                if (isDirectory) {
                    String[] fileList = file.list();
                    if (fileList == null)
                        throw new FileNotFoundException(filename + " exists, but is not accessible");
                    files = Arrays.<String>asList(fileList);
                } else {
                    is = new BufferedInputStream(new FileInputStream(filename));
                    boolean meteredInput = ProgressMonitor.getDefault().shouldMeterInput(url, "GET");
                    if (meteredInput)   {
                        ProgressSource pi = new ProgressSource(url, "GET", file.length());
                        is = new MeteredStream(is, pi, file.length());
                    }
                }
            } catch (IOException e) {
                throw e;
            }
            connected = true;
        }
    }
    private boolean initializedHeaders = false;
    private void initializeHeaders() {
        try {
            connect();
            exists = file.exists();
        } catch (IOException e) {
        }
        if (!initializedHeaders || !exists) {
            length = file.length();
            lastModified = file.lastModified();
            if (!isDirectory) {
                FileNameMap map = java.net.URLConnection.getFileNameMap();
                contentType = map.getContentTypeFor(filename);
                if (contentType != null) {
                    properties.add(CONTENT_TYPE, contentType);
                }
                properties.add(CONTENT_LENGTH, String.valueOf(length));
                if (lastModified != 0) {
                    Date date = new Date(lastModified);
                    SimpleDateFormat fo =
                        new SimpleDateFormat ("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
                    fo.setTimeZone(TimeZone.getTimeZone("GMT"));
                    properties.add(LAST_MODIFIED, fo.format(date));
                }
            } else {
                properties.add(CONTENT_TYPE, TEXT_PLAIN);
            }
            initializedHeaders = true;
        }
    }
    public String getHeaderField(String name) {
        initializeHeaders();
        return super.getHeaderField(name);
    }
    public String getHeaderField(int n) {
        initializeHeaders();
        return super.getHeaderField(n);
    }
    public int getContentLength() {
        initializeHeaders();
        if (length > Integer.MAX_VALUE)
            return -1;
        return (int) length;
    }
    public long getContentLengthLong() {
        initializeHeaders();
        return length;
    }
    public String getHeaderFieldKey(int n) {
        initializeHeaders();
        return super.getHeaderFieldKey(n);
    }
    public MessageHeader getProperties() {
        initializeHeaders();
        return super.getProperties();
    }
    public long getLastModified() {
        initializeHeaders();
        return lastModified;
    }
    public synchronized InputStream getInputStream()
        throws IOException {
        int iconHeight;
        int iconWidth;
        connect();
        if (is == null) {
            if (isDirectory) {
                FileNameMap map = java.net.URLConnection.getFileNameMap();
                StringBuffer buf = new StringBuffer();
                if (files == null) {
                    throw new FileNotFoundException(filename);
                }
                Collections.sort(files, Collator.getInstance());
                for (int i = 0 ; i < files.size() ; i++) {
                    String fileName = files.get(i);
                    buf.append(fileName);
                    buf.append("\n");
                }
                is = new ByteArrayInputStream(buf.toString().getBytes());
            } else {
                throw new FileNotFoundException(filename);
            }
        }
        return is;
    }
    Permission permission;
    public Permission getPermission() throws IOException {
        if (permission == null) {
            String decodedPath = ParseUtil.decode(url.getPath());
            if (File.separatorChar == '/') {
                permission = new FilePermission(decodedPath, "read");
            } else {
                permission = new FilePermission(
                        decodedPath.replace('/',File.separatorChar), "read");
            }
        }
        return permission;
    }
}
