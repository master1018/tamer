public class GeneralSubtree {
    private final GeneralName base;
    private final int minimum;
    private final int maximum;
    private byte[] encoding;
    public GeneralSubtree(GeneralName base) {
        this(base, 0, -1);
    }
    public GeneralSubtree(GeneralName base, int minimum) {
        this(base, minimum, -1);
    }
    public GeneralSubtree(GeneralName base, int minimum, int maximum) {
        this.base = base;
        this.minimum = minimum;
        this.maximum = maximum;
    }
    public GeneralName getBase() {
        return base;
    }
    public int getMaximum() {
        return maximum;
    }
    public int getMinimum() {
        return minimum;
    }
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }
    public void dumpValue(StringBuffer buffer, String prefix) {
        buffer.append(prefix).append("General Subtree: [\n"); 
        buffer.append(prefix).append("  base: ").append(base).append('\n'); 
        buffer.append(prefix).append("  minimum: ") 
            .append(minimum).append('\n');
        if (maximum >= 0) {
            buffer.append(prefix).append("  maximum: ") 
                .append(maximum).append('\n');
        }
        buffer.append(prefix).append("]\n"); 
    }
    public static final ASN1Sequence ASN1 = new ASN1Sequence(new ASN1Type[] {
            GeneralName.ASN1,
            new ASN1Implicit(0, ASN1Integer.getInstance()), 
            new ASN1Implicit(1, ASN1Integer.getInstance()) }) {
        {
            setDefault(new byte[] {0}, 1);  
            setOptional(2);                 
        }
        protected Object getDecodedObject(BerInputStream in) {
            Object[] values = (Object[]) in.content;
            int maximum = -1; 
            if (values[2] != null) {
                maximum = ASN1Integer.toIntValue(values[2]); 
            }
            return new GeneralSubtree((GeneralName) values[0],
                    ASN1Integer.toIntValue(values[1]),
                    maximum);
        }
        protected void getValues(Object object, Object[] values) {
            GeneralSubtree gs = (GeneralSubtree) object;
            values[0] = gs.base;
            values[1] = ASN1Integer.fromIntValue(gs.minimum);
            if (gs.maximum > -1) {
                values[2] = ASN1Integer.fromIntValue(gs.maximum);
            }
        }
    };
}
