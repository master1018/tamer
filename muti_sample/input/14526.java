public class Offsets {
    private static void outOfBounds(MessageDigest md, int arrayLen, int ofs, int len) throws Exception {
        try {
            md.reset();
            md.update(new byte[arrayLen], ofs, len);
            throw new Exception("invalid call succeeded");
        } catch (RuntimeException e) {
        }
    }
    private static void test(String algorithm, int minOfs, int maxOfs, int minLen, int maxLen) throws Exception {
        Random random = new Random();
        MessageDigest md = MessageDigest.getInstance(algorithm, "SUN");
        System.out.println("Testing " + algorithm + "...");
        outOfBounds(md, 16, 0, 32);
        outOfBounds(md, 16, -8, 16);
        outOfBounds(md, 16, 8, -8);
        outOfBounds(md, 16, Integer.MAX_VALUE, 8);
        for (int n = minLen; n <= maxLen; n++) {
            System.out.print(n + " ");
            byte[] data = new byte[n];
            random.nextBytes(data);
            byte[] digest = null;
            for (int ofs = minOfs; ofs <= maxOfs; ofs++) {
                byte[] ofsData = new byte[n + maxOfs];
                random.nextBytes(ofsData);
                System.arraycopy(data, 0, ofsData, ofs, n);
                md.update(ofsData, ofs, n);
                byte[] ofsDigest = md.digest();
                if (digest == null) {
                    digest = ofsDigest;
                } else {
                    if (Arrays.equals(digest, ofsDigest) == false) {
                        throw new Exception("Digest mismatch " + algorithm + ", ofs: " + ofs + ", len: " + n);
                    }
                }
            }
        }
        System.out.println();
    }
    public static void main(String[] args) throws Exception {
        test("MD2", 0, 64, 0, 128);
        test("MD5", 0, 64, 0, 128);
        test("SHA1", 0, 64, 0, 128);
        test("SHA-256", 0, 64, 0, 128);
        test("SHA-384", 0, 128, 0, 256);
        test("SHA-512", 0, 128, 0, 256);
    }
}
