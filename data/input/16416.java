public class DESParity extends PKCS11Test {
    public void main(Provider p) throws Exception {
        if (p.getService("SecretKeyFactory", "DES") == null) {
            System.out.println("Not supported by provider, skipping");
            return;
        }
        Random random = new Random();
        SecretKeyFactory kf;
        kf = SecretKeyFactory.getInstance("DES", p);
        for (int i = 0; i < 10; i++ ) {
            byte[] b = new byte[8];
            random.nextBytes(b);
            SecretKeySpec spec = new SecretKeySpec(b, "DES");
            SecretKey key = kf.generateSecret(spec);
            if (DESKeySpec.isParityAdjusted(key.getEncoded(), 0) == false) {
                throw new Exception("DES key not parity adjusted");
            }
        }
        kf = SecretKeyFactory.getInstance("DESede", p);
        for (int i = 0; i < 10; i++ ) {
            byte[] b = new byte[24];
            random.nextBytes(b);
            SecretKeySpec spec = new SecretKeySpec(b, "DESede");
            SecretKey key = kf.generateSecret(spec);
            if (DESedeKeySpec.isParityAdjusted(key.getEncoded(), 0) == false) {
                throw new Exception("DESede key not parity adjusted");
            }
        }
    }
    public static void main(String[] args) throws Exception {
        main(new DESParity());
    }
}
