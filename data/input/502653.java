public abstract class ASN1TypeCollection extends ASN1Constructured {
    public final ASN1Type[] type; 
    public final boolean[] OPTIONAL; 
    public final Object[] DEFAULT; 
    public ASN1TypeCollection(int tagNumber, ASN1Type[] type) {
        super(tagNumber);
        this.type = type;
        this.OPTIONAL = new boolean[type.length];
        this.DEFAULT = new Object[type.length];
    }
    protected final void setOptional(int index) {
        OPTIONAL[index] = true;
    }
    protected final void setDefault(Object object, int index) {
        OPTIONAL[index] = true;
        DEFAULT[index] = object;
    }
    protected void getValues(Object object, Object[] values) {
        throw new RuntimeException(Messages.getString("security.101", getClass().getName())); 
    }
}
