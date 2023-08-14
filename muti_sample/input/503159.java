public abstract class JarURLConnection extends URLConnection {
    protected URLConnection jarFileURLConnection;
    private String entryName;
    private URL fileURL;
    private String file;
    protected JarURLConnection(URL url) throws MalformedURLException {
        super(url);
        file = url.getFile();
        int sepIdx;
        if ((sepIdx = file.indexOf("!/")) < 0) { 
            throw new MalformedURLException();
        }
        fileURL = new URL(url.getFile().substring(0,sepIdx));
        sepIdx += 2;
        if (file.length() == sepIdx) {
            return;
        }
        entryName = file.substring(sepIdx, file.length());
        if (null != url.getRef()) {
            entryName += "#" + url.getRef(); 
        }
    }
    public Attributes getAttributes() throws java.io.IOException {
        JarEntry jEntry = getJarEntry();
        return (jEntry == null) ? null : jEntry.getAttributes();
    }
    public Certificate[] getCertificates() throws java.io.IOException {
        JarEntry jEntry = getJarEntry();
        if (jEntry == null) {
            return null;
        }
        return jEntry.getCertificates();
    }
    public String getEntryName() {
        return entryName;
    }
    public JarEntry getJarEntry() throws IOException {
        if (!connected) {
            connect();
        }
        if (entryName == null) {
            return null;
        }
        return getJarFile().getJarEntry(entryName);
    }
    public Manifest getManifest() throws java.io.IOException {
        return (Manifest)getJarFile().getManifest().clone();
    }
    public abstract JarFile getJarFile() throws java.io.IOException;
    public URL getJarFileURL() {
        return fileURL;
    }
    public Attributes getMainAttributes() throws java.io.IOException {
        Manifest m = getJarFile().getManifest();
        return (m == null) ? null : m.getMainAttributes();
    }
}
