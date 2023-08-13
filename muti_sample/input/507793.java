public class JarOutputStream extends ZipOutputStream {
    private Manifest manifest;
    public JarOutputStream(OutputStream os, Manifest mf) throws IOException {
        super(os);
        if (mf == null) {
            throw new NullPointerException();
        }
        manifest = mf;
        ZipEntry ze = new ZipEntry(JarFile.MANIFEST_NAME);
        putNextEntry(ze);
        manifest.write(this);
        closeEntry();
    }
    public JarOutputStream(OutputStream os) throws IOException {
        super(os);
    }
    @Override
    public void putNextEntry(ZipEntry ze) throws IOException {
        super.putNextEntry(ze);
    }
}
