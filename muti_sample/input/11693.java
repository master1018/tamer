public class JavaSerializationComponent extends TaggedComponentBase {
    private byte version;
    private static JavaSerializationComponent singleton;
    public static JavaSerializationComponent singleton() {
        if (singleton == null) {
            synchronized (JavaSerializationComponent.class) {
                singleton =
                    new JavaSerializationComponent(Message.JAVA_ENC_VERSION);
            }
        }
        return singleton;
    }
    public JavaSerializationComponent(byte version) {
        this.version = version;
    }
    public byte javaSerializationVersion() {
        return this.version;
    }
    public void writeContents(OutputStream os) {
        os.write_octet(version);
    }
    public int getId() {
        return ORBConstants.TAG_JAVA_SERIALIZATION_ID;
    }
    public boolean equals(Object obj) {
        if (!(obj instanceof JavaSerializationComponent)) {
            return false;
        }
        JavaSerializationComponent other = (JavaSerializationComponent) obj;
        return this.version == other.version;
    }
    public int hashCode() {
        return this.version;
    }
}
