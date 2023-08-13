public class GenerateCertificatesEmptyCollection {
    public static void main(String[] args) throws Exception {
        byte[] b = { 0x30, 0x23,
                     0x06, 0x09, 0x2A, (byte)0x86, 0x48,
                     (byte)0x86, (byte)0xF7, 0x0D,
                     0x01, 0x07, 0x02,
                     0x00, 0x16,
                     0x30, 0x14,                
                     0x02, 0x01, 0x01,          
                     0x31, 0x00,                
                     0x30, 0x0B,                
                     0x06, 0x09, 0x2A, (byte)0x86, 0x48,
                     (byte)0x86, (byte)0xF7, 0x0D,
                     0x01, 0x07, 0x01,
                     0x31, 0x00                 
                   };
        CertificateFactory cf = CertificateFactory.getInstance( "X509", "SUN");
        Collection c = cf.generateCertificates( new ByteArrayInputStream(b));
        if (!c.isEmpty())
            throw new Exception("CertificateFactory.generateCertificates() "
                + "did not return an empty Collection");
    }
}
