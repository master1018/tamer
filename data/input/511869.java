public class NodeChangeEvent extends EventObject implements Serializable {
    private static final long serialVersionUID = 8068949086596572957L;
    private final Preferences parent;
    private final Preferences child;
    public NodeChangeEvent (Preferences p, Preferences c) {
        super(p);
        parent = p;
        child = c;
    }
    public Preferences getParent() {
        return parent;
    }
    public Preferences getChild() {
        return child;
    }
    private void writeObject (ObjectOutputStream out) throws IOException {
        throw new NotSerializableException();
    }
    private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException {
        throw new NotSerializableException();
    }
}
