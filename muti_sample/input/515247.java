public class MimeTable implements FileNameMap {
    public static final String UNKNOWN = "content/unknown"; 
    public static final Properties types = new Properties();
    static {
        types.setProperty("text", "text/plain"); 
        types.setProperty("txt", "text/plain"); 
        types.setProperty("htm", "text/html"); 
        types.setProperty("html", "text/html"); 
    }
    public MimeTable() {
        InputStream str = AccessController
                .doPrivileged(new PrivilegedAction<InputStream>() {
                    public InputStream run() {
                        return getContentTypes();
                    }
                });
        if (str != null) {
            try {
                try {
                    types.load(str);
                } finally {
                    str.close();
                }
            } catch (IOException ex) {
            }
        }
    }
    private InputStream getContentTypes() {
        String userTable = System.getProperty("content.types.user.table"); 
        if (userTable != null) {
            try {
                return new FileInputStream(userTable);
            } catch (IOException e) {
            }
        }
        String javahome = System.getProperty("java.home"); 
        File contentFile = new File(javahome, "lib" 
                + File.separator + "content-types.properties"); 
        try {
            return new FileInputStream(contentFile);
        } catch (IOException e) {
        }
        return null;
    }
    public String getContentTypeFor(String filename) {
        if (filename.endsWith("/")) { 
            return (String) types.get("html"); 
        }
        int lastCharInExtension = filename.lastIndexOf('#');
        if (lastCharInExtension < 0) {
            lastCharInExtension = filename.length();
        }
        int firstCharInExtension = filename.lastIndexOf('.') + 1;
        String ext = ""; 
        if (firstCharInExtension > filename.lastIndexOf('/')) {
            ext = filename.substring(firstCharInExtension, lastCharInExtension);
        }
        return types.getProperty(ext.toLowerCase());
    }
}
