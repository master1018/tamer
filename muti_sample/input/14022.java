public class ForwardingJavaFileObject<F extends JavaFileObject>
    extends ForwardingFileObject<F>
    implements JavaFileObject
{
    protected ForwardingJavaFileObject(F fileObject) {
        super(fileObject);
    }
    public Kind getKind() {
        return fileObject.getKind();
    }
    public boolean isNameCompatible(String simpleName, Kind kind) {
        return fileObject.isNameCompatible(simpleName, kind);
    }
    public NestingKind getNestingKind() { return fileObject.getNestingKind(); }
    public Modifier getAccessLevel()  { return fileObject.getAccessLevel(); }
}
