public class DerIsConstructor {
    public static void main(String[] args) {
        try {
            X500Principal p = new X500Principal("o=sun, cn=duke");
            X500Principal p2 = new X500Principal("o=sun, cn=dukette");
            byte[] encoded = p.getEncoded();
            byte[] encoded2 = p2.getEncoded();
            byte[] all = new byte[encoded.length + encoded2.length];
            System.arraycopy(encoded, 0, all, 0, encoded.length);
            System.arraycopy(encoded2, 0, all, encoded.length, encoded2.length);
            ByteArrayInputStream bais = new ByteArrayInputStream(all);
            X500Principal pp = new X500Principal(bais);
            X500Principal pp2 = new X500Principal(bais);
            if (p.equals(pp) && p2.equals(pp2) && !pp.equals(pp2)) {
                System.out.println("Test 1 passed");
            } else {
                throw new SecurityException("Test 1 failed");
            }
            byte[] all2 = new byte[all.length];
            System.arraycopy(all, 0, all2, 0, all.length);
            all2[encoded.length + 2] = (byte)-1;
            bais = new ByteArrayInputStream(all2);
            X500Principal ppp = new X500Principal(bais);
            int origAvailable = bais.available();
            try {
                X500Principal ppp2 = new X500Principal(bais);
                throw new SecurityException("Test 2 (part a) failed");
            } catch (IllegalArgumentException iae) {
                if (bais.available() == origAvailable) {
                    System.out.println("Test 2 passed");
                } else {
                    throw new SecurityException("Test 2 (part b) failed");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SecurityException(e.getMessage());
        }
    }
}
