public class DigesterOutputStream extends ByteArrayOutputStream {
    final MessageDigestAlgorithm mda;
    static java.util.logging.Logger log =
        java.util.logging.Logger.getLogger
        (DigesterOutputStream.class.getName());
    public DigesterOutputStream(MessageDigestAlgorithm mda) {
        this.mda=mda;
    }
    public void write(byte[] arg0) {
        write(arg0, 0, arg0.length);
    }
    public void write(int arg0) {
        mda.update((byte)arg0);
    }
    public void write(byte[] arg0, int arg1, int arg2) {
        if (log.isLoggable(java.util.logging.Level.FINE)) {
            log.log(java.util.logging.Level.FINE, "Pre-digested input:");
            StringBuffer sb = new StringBuffer(arg2);
            for (int i=arg1; i<(arg1+arg2); i++) {
                sb.append((char) arg0[i]);
            }
            log.log(java.util.logging.Level.FINE, sb.toString());
        }
        mda.update(arg0, arg1, arg2);
    }
    public byte[] getDigestValue() {
         return mda.digest();
    }
}
