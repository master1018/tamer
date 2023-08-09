public class FileURLConnection extends URLConnection {
    String fileName;
    private InputStream is;
    private int length = -1;
    private boolean isDir;
    private FilePermission permission;
    public FileURLConnection(URL url) {
        super(url);
        fileName = url.getFile();
        if (fileName == null) {
            fileName = ""; 
        }
        fileName = Util.decode(fileName, false);
    }
    @Override
    public void connect() throws IOException {
        File f = new File(fileName);
        if (f.isDirectory()) {
            isDir = true;
            is = getDirectoryListing(f);
        } else {
            is = new BufferedInputStream(new FileInputStream(f), 8192);
            length = is.available();
        }
        connected = true;
    }
    @Override
    public int getContentLength() {
        try {
            if (!connected) {
                connect();
            }
        } catch (IOException e) {
        }
        return length;
    }
    @Override
    public String getContentType() {
        try {
            if (!connected) {
                connect();
            }
        } catch (IOException e) {
            return MimeTable.UNKNOWN;
        }
        if (isDir) {
            return "text/plain"; 
        }
        String result = guessContentTypeFromName(url.getFile());
        if (result != null) {
            return result;
        }
        try {
            result = guessContentTypeFromStream(is);
        } catch (IOException e) {
        }
        if (result != null) {
            return result;
        }
        return MimeTable.UNKNOWN;
    }
    private InputStream getDirectoryListing(File f) {
        String fileList[] = f.list();
        ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
        PrintStream out = new PrintStream(bytes);
        out.print("<title>Directory Listing</title>\n"); 
        out.print("<base href=\"file:"); 
        out.print(f.getPath().replace('\\', '/') + "/\"><h1>" + f.getPath() 
                + "</h1>\n<hr>\n"); 
        int i;
        for (i = 0; i < fileList.length; i++) {
            out.print(fileList[i] + "<br>\n"); 
        }
        out.close();
        return new ByteArrayInputStream(bytes.toByteArray());
    }
    @Override
    public InputStream getInputStream() throws IOException {
        if (!connected) {
            connect();
        }
        return is;
    }
    @Override
    public java.security.Permission getPermission() throws IOException {
        if (permission == null) {
            String path = fileName;
            if (File.separatorChar != '/') {
                path = path.replace('/', File.separatorChar);
            }
            permission = new FilePermission(path, "read"); 
        }
        return permission;
    }
}
