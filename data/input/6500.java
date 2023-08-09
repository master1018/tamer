public class MirroredTypeException extends RuntimeException {
    private static final long serialVersionUID = 1;
    private transient TypeMirror type;          
    private String name;                        
    public MirroredTypeException(TypeMirror type) {
        super("Attempt to access Class object for TypeMirror " + type);
        this.type = type;
        name = type.toString();
    }
    public TypeMirror getTypeMirror() {
        return type;
    }
    public String getQualifiedName() {
        return name;
    }
}
