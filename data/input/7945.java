public class UnexpectedNPE {
    CertificateFactory cf = null ;
    public UnexpectedNPE() {}
    public static void main( String[] av ) {
        byte[] encoded_1 = { 0x00, 0x00, 0x00, 0x00 };
        byte[] encoded_2 = { 0x30, 0x01, 0x00, 0x00 };
        byte[] encoded_3 = { 0x30, 0x01, 0x00 };
        UnexpectedNPE unpe = new UnexpectedNPE() ;
        if(!unpe.run(encoded_1)) {
            throw new SecurityException("CRLException has not been thrown");
        }
        if(!unpe.run(encoded_2)) {
            throw new SecurityException("CRLException has not been thrown");
        }
        if(!unpe.run(encoded_2)) {
            throw new SecurityException("CRLException has not been thrown");
        }
    }
    private boolean run(byte[] buf) {
        if (cf == null) {
            try {
                cf = CertificateFactory.getInstance("X.509", "SUN");
            } catch (CertificateException e) {
                throw new SecurityException("Cannot get CertificateFactory");
            } catch (NoSuchProviderException npe) {
                throw new SecurityException("Cannot get CertificateFactory");
            }
        }
        try {
            cf.generateCRL(new ByteArrayInputStream(buf));
        } catch (CRLException ce) {
            System.out.println("NPE checking passed");
            return true;
        }
        System.out.println("CRLException has not been thrown");
        return false;
    }
}
