public class SignerOutputStream extends ByteArrayOutputStream {
    final SignatureAlgorithm sa;
    static java.util.logging.Logger log =
        java.util.logging.Logger.getLogger
        (SignerOutputStream.class.getName());
    public SignerOutputStream(SignatureAlgorithm sa) {
        this.sa=sa;
    }
    public void write(byte[] arg0)  {
        super.write(arg0, 0, arg0.length);
        try {
            sa.update(arg0);
        } catch (XMLSignatureException e) {
            throw new RuntimeException(""+e);
        }
    }
    public void write(int arg0) {
        super.write(arg0);
        try {
            sa.update((byte)arg0);
        } catch (XMLSignatureException e) {
            throw new RuntimeException(""+e);
        }
    }
    public void write(byte[] arg0, int arg1, int arg2) {
        super.write(arg0, arg1, arg2);
        if (log.isLoggable(java.util.logging.Level.FINE)) {
            log.log(java.util.logging.Level.FINE, "Canonicalized SignedInfo:");
            StringBuffer sb = new StringBuffer(arg2);
            for (int i=arg1; i<(arg1+arg2); i++) {
                sb.append((char) arg0[i]);
            }
            log.log(java.util.logging.Level.FINE, sb.toString());
        }
        try {
            sa.update(arg0,arg1,arg2);
        } catch (XMLSignatureException e) {
            throw new RuntimeException(""+e);
        }
    }
}
