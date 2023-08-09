public class BasicControl implements Control {
    protected String id;
    protected boolean criticality = false; 
    protected byte[] value = null;
    private static final long serialVersionUID = -5914033725246428413L;
    public BasicControl(String id) {
        this.id = id;
    }
    public BasicControl(String id, boolean criticality, byte[] value) {
        this.id = id;
        this.criticality = criticality;
        if (value != null) {
            this.value = value;
        }
    }
    public String getID() {
        return id;
    }
    public boolean isCritical() {
        return criticality;
    }
    public byte[] getEncodedValue() {
        return value == null ? null : value.clone();
    }
}
