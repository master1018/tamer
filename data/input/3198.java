public abstract class TypeMirrorImpl implements TypeMirror {
    protected final AptEnv env;
    public final Type type;
    protected TypeMirrorImpl(AptEnv env, Type type) {
        this.env = env;
        this.type = type;
    }
    public String toString() {
        return type.toString();
    }
    public boolean equals(Object obj) {
        if (obj instanceof TypeMirrorImpl) {
            TypeMirrorImpl that = (TypeMirrorImpl) obj;
            return env.jctypes.isSameType(this.type, that.type);
        } else {
            return false;
        }
    }
    public int hashCode() {
        return Types.hashCode(type);
    }
}
