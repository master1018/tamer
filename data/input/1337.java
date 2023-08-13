public class WrappingJavaFileManager<M extends JavaFileManager> extends ForwardingJavaFileManager<M> {
    protected WrappingJavaFileManager(M fileManager) {
        super(fileManager);
    }
    protected FileObject wrap(FileObject fileObject) {
        return fileObject;
    }
    protected JavaFileObject wrap(JavaFileObject fileObject) {
        return (JavaFileObject)wrap((FileObject)fileObject);
    }
    protected FileObject unwrap(FileObject fileObject) {
        return fileObject;
    }
    protected JavaFileObject unwrap(JavaFileObject fileObject) {
        return (JavaFileObject)unwrap((FileObject)fileObject);
    }
    protected Iterable<JavaFileObject> wrap(Iterable<JavaFileObject> fileObjects) {
        List<JavaFileObject> mapped = new ArrayList<JavaFileObject>();
        for (JavaFileObject fileObject : fileObjects)
            mapped.add(wrap(fileObject));
        return Collections.unmodifiableList(mapped);
    }
    protected URI unwrap(URI uri) {
        return uri;
    }
    public Iterable<JavaFileObject> list(Location location,
                                         String packageName,
                                         Set<Kind> kinds,
                                         boolean recurse)
        throws IOException
    {
        return wrap(super.list(location, packageName, kinds, recurse));
    }
    public String inferBinaryName(Location location, JavaFileObject file) {
        return super.inferBinaryName(location, unwrap(file));
    }
    public JavaFileObject getJavaFileForInput(Location location,
                                              String className,
                                              Kind kind)
        throws IOException
    {
        return wrap(super.getJavaFileForInput(location, className, kind));
    }
    public JavaFileObject getJavaFileForOutput(Location location,
                                               String className,
                                               Kind kind,
                                               FileObject sibling)
        throws IOException
    {
        return wrap(super.getJavaFileForOutput(location, className, kind, unwrap(sibling)));
    }
    public FileObject getFileForInput(Location location,
                                      String packageName,
                                      String relativeName)
        throws IOException
    {
        return wrap(super.getFileForInput(location, packageName, relativeName));
    }
    public FileObject getFileForOutput(Location location,
                                       String packageName,
                                       String relativeName,
                                       FileObject sibling)
        throws IOException
    {
        return wrap(super.getFileForOutput(location,
                                           packageName,
                                           relativeName,
                                           unwrap(sibling)));
    }
}
