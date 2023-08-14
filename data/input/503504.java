public class GuardedObject implements Serializable {
    private static final long serialVersionUID = -5240450096227834308L;
    private final Object object;
    private final Guard guard;
    public GuardedObject(Object object, Guard guard) {
        this.object = object;
        this.guard = guard;
    }
    public Object getObject() throws SecurityException {
        if (guard != null) {
            guard.checkGuard(object);
        }
        return object;
    }
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        if (guard != null) {
            guard.checkGuard(object);
        }
        out.defaultWriteObject();
    }
}
