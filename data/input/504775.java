public class PSource {
    private String pSrcName;
    private PSource() {}
    protected PSource(String pSrcName) {
        if (pSrcName == null) {
            throw new NullPointerException(Messages.getString("crypto.42")); 
        }
        this.pSrcName = pSrcName;
    }
    public String getAlgorithm() {
        return pSrcName;
    }
    public static final class PSpecified extends PSource {
        private final byte[] p;
        public static final PSpecified DEFAULT = new PSpecified();
        private PSpecified() {
            super("PSpecified"); 
            p = new byte[0];
        }
        public PSpecified(byte[] p) {
            super("PSpecified"); 
            if (p == null) {
                throw new NullPointerException(Messages.getString("crypto.43")); 
            }
            this.p = new byte[p.length];
            System.arraycopy(p, 0, this.p, 0, p.length);
        }
        public byte[] getValue() {
            byte[] result = new byte[p.length];
            System.arraycopy(p, 0, result, 0, p.length);
            return result;
        }
    }
}
