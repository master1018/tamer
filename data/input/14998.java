public class PSource {
    private String pSrcName;
    protected PSource(String pSrcName) {
        if (pSrcName == null) {
            throw new NullPointerException("pSource algorithm is null");
        }
        this.pSrcName = pSrcName;
    }
    public String getAlgorithm() {
        return pSrcName;
    }
    public static final class PSpecified extends PSource {
        private byte[] p = new byte[0];
        public static final PSpecified DEFAULT = new PSpecified(new byte[0]);
        public PSpecified(byte[] p) {
            super("PSpecified");
            this.p = (byte[]) p.clone();
        }
        public byte[] getValue() {
            return (p.length==0? p: (byte[])p.clone());
        }
    }
}
