public class SetNameConstraintsEmptySequence {
    public static void main(String[] args) throws Exception {
         X509CertSelector certSel = new X509CertSelector();
         byte[] data = {0x30, 0x00};        
         certSel.setNameConstraints(data);
    }
}
