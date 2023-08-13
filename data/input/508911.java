public class BasicConstraints extends ExtensionValue {
    private boolean cA = false;
    private int pathLenConstraint = Integer.MAX_VALUE;
    public BasicConstraints(boolean cA, int pathLenConstraint) {
        this.cA = cA;
        this.pathLenConstraint = pathLenConstraint;
    }
    public BasicConstraints(byte[] encoding) throws IOException {
        super(encoding);
        Object[] values = (Object[]) ASN1.decode(encoding);
        cA = ((Boolean) values[0]).booleanValue();
        if (values[1] != null) {
            pathLenConstraint = new BigInteger((byte[]) values[1]).intValue();
        }
    }
    public boolean getCA() {
        return cA;
    }
    public int getPathLenConstraint() {
        return pathLenConstraint;
    }
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(
                    new Object[] {Boolean.valueOf(cA),
                        BigInteger.valueOf(pathLenConstraint)});
        }
        return encoding;
    }
    public void dumpValue(StringBuffer buffer, String prefix) {
        buffer.append(prefix).append("BasicConstraints [\n").append(prefix) 
            .append("  CA: ").append(cA) 
            .append("\n  ").append(prefix).append("pathLenConstraint: ") 
            .append(pathLenConstraint).append('\n').append(prefix)
            .append("]\n"); 
    }
    public static final ASN1Type ASN1 = new ASN1Sequence(new ASN1Type[] {
            ASN1Boolean.getInstance(), ASN1Integer.getInstance() }) {
        {
            setDefault(Boolean.FALSE, 0);
            setOptional(1);
        }
        public Object getDecodedObject(BerInputStream in)
                throws IOException {
            return in.content;
        }
        protected void getValues(Object object, Object[] values) {
            Object[] vals = (Object[]) object;
            values[0] = (Boolean) vals[0];
            values[1] = ((BigInteger) vals[1]).toByteArray();
        }
    };
}
