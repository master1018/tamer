public class IllegalCertiticates {
    public static void main(String[] args) throws Exception {
        List certs = new Vector();
        certs.add("The 1st certificate");
        certs.add("The 2nd certificate");
        certs.add("The 3rd certificate");
        certs.add("The 4th certificate");
        try {
            X509CertPath cp = new X509CertPath(certs);
            throw new Exception("No expected CertificateException thrown");
        } catch (CertificateException ce) {
        } catch (Exception e) {
            throw new Exception("No expected CertificateException thrown", e);
        }
    }
}
