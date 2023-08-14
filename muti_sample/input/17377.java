public class FileURLMapper {
    URL url;
    String path;
    public FileURLMapper (URL url) {
        this.url = url;
    }
    public String getPath () {
        if (path != null) {
            return path;
        }
        String host = url.getHost();
        if (host == null || "".equals(host) || "localhost".equalsIgnoreCase (host)) {
            path = url.getFile();
            path = ParseUtil.decode (path);
        }
        return path;
    }
    public boolean exists () {
        String s = getPath ();
        if (s == null) {
            return false;
        } else {
            File f = new File (s);
            return f.exists();
        }
    }
}
