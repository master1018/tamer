public abstract class BaseFileObject implements JavaFileObject {
    protected BaseFileObject(JavacFileManager fileManager) {
        this.fileManager = fileManager;
    }
    public abstract String getShortName();
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + getName() + "]";
    }
    public NestingKind getNestingKind() { return null; }
    public Modifier getAccessLevel()  { return null; }
    public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
        return new InputStreamReader(openInputStream(), getDecoder(ignoreEncodingErrors));
    }
    protected CharsetDecoder getDecoder(boolean ignoreEncodingErrors) {
        throw new UnsupportedOperationException();
    }
    protected abstract String inferBinaryName(Iterable<? extends File> path);
    protected static JavaFileObject.Kind getKind(String filename) {
        return BaseFileManager.getKind(filename);
    }
    protected static String removeExtension(String fileName) {
        int lastDot = fileName.lastIndexOf(".");
        return (lastDot == -1 ? fileName : fileName.substring(0, lastDot));
    }
    protected static URI createJarUri(File jarFile, String entryName) {
        URI jarURI = jarFile.toURI().normalize();
        String separator = entryName.startsWith("/") ? "!" : "!/";
        try {
            return new URI("jar:" + jarURI + separator + entryName);
        } catch (URISyntaxException e) {
            throw new CannotCreateUriError(jarURI + separator + entryName, e);
        }
    }
    protected static class CannotCreateUriError extends Error {
        private static final long serialVersionUID = 9101708840997613546L;
        public CannotCreateUriError(String value, Throwable cause) {
            super(value, cause);
        }
    }
    public static String getSimpleName(FileObject fo) {
        URI uri = fo.toUri();
        String s = uri.getSchemeSpecificPart();
        return s.substring(s.lastIndexOf("/") + 1); 
    }
    @Override
    public abstract boolean equals(Object other);
    @Override
    public abstract int hashCode();
    protected final JavacFileManager fileManager;
}
