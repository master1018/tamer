class SlowStreamReader {
    public static void main(String[] args) throws Exception {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        if (factory.generateCertificates(System.in).size() != 5) {
            throw new Exception("Not all certs read");
        }
    }
}
class SlowStreamWriter {
    public static void main(String[] args) throws Exception {
        for (int i=0; i<5; i++) {
            FileInputStream fin = new FileInputStream(new File(new File(
                    System.getProperty("test.src", "."), "openssl"), "pem"));
            byte[] buffer = new byte[4096];
            while (true) {
                int len = fin.read(buffer);
                if (len < 0) break;
                System.out.write(buffer, 0, len);
            }
            Thread.sleep(2000);
        }
    }
}
