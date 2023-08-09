public class FileImageSource extends InputStreamImageSource {
    String imagefile;
    public FileImageSource(String filename) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(filename);
        }
        imagefile = filename;
    }
    final boolean checkSecurity(Object context, boolean quiet) {
        return true;
    }
    protected ImageDecoder getDecoder() {
        InputStream is;
        try {
            is = new BufferedInputStream(new FileInputStream(imagefile));
        } catch (FileNotFoundException e) {
            return null;
        }
        return getDecoder(is);
    }
}
