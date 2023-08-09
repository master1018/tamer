public class VMOption {
    private String name;
    private String value;
    private boolean writeable;
    private Origin origin;
    public enum Origin {
        DEFAULT,
        VM_CREATION,
        ENVIRON_VAR,
        CONFIG_FILE,
        MANAGEMENT,
        ERGONOMIC,
        OTHER
    }
    public VMOption(String name, String value, boolean writeable, Origin origin) {
        this.name = name;
        this.value = value;
        this.writeable = writeable;
        this.origin = origin;
    }
    private VMOption(CompositeData cd) {
        VMOptionCompositeData.validateCompositeData(cd);
        this.name = VMOptionCompositeData.getName(cd);
        this.value = VMOptionCompositeData.getValue(cd);
        this.writeable = VMOptionCompositeData.isWriteable(cd);
        this.origin = VMOptionCompositeData.getOrigin(cd);
    }
    public String getName() {
        return name;
    }
    public String getValue() {
        return value;
    }
    public Origin getOrigin() {
        return origin;
    }
    public boolean isWriteable() {
        return writeable;
    }
    public String toString() {
        return "VM option: " + getName() +
               " value: " + value + " " +
               " origin: " + origin + " " +
               (writeable ? "(read-only)" : "(read-write)");
    }
    public static VMOption from(CompositeData cd) {
        if (cd == null) {
            return null;
        }
        if (cd instanceof VMOptionCompositeData) {
            return ((VMOptionCompositeData) cd).getVMOption();
        } else {
            return new VMOption(cd);
        }
    }
}
