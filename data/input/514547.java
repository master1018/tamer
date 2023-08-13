public class PreferenceChangeEvent extends EventObject implements Serializable {
    private static final long serialVersionUID = 793724513368024975L;
    private final Preferences node;
    private final String key;
    private final String value;
    public PreferenceChangeEvent(Preferences p, String k, String v) {
        super(p);
        node = p;
        key = k;
        value = v;
    }
    public String getKey() {
        return key;
    }
    public String getNewValue() {
        return value;
    }
    public Preferences getNode() {
        return node;
    }
    private void writeObject(ObjectOutputStream out) throws IOException {
        throw new NotSerializableException();
    }
    private void readObject(ObjectInputStream in) throws IOException{
        throw new NotSerializableException();
    }
}
