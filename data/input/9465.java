public class ByteBuffers {
    public static void main(String[] args) throws Exception {
        Provider p = Security.getProvider("SunJCE");
        Random random = new Random();
        int n = 10 * 1024;
        byte[] t = new byte[n];
        random.nextBytes(t);
        byte[] keyBytes = new byte[16];
        random.nextBytes(keyBytes);
        SecretKey key = new SecretKeySpec(keyBytes, "HmacMD5");
        Mac mac = Mac.getInstance("HmacMD5");
        mac.init(key);
        byte[] macValue = mac.doFinal(t);
        ByteBuffer b1 = ByteBuffer.allocate(n + 256);
        b1.position(random.nextInt(256));
        b1.limit(b1.position() + n);
        ByteBuffer b2 = b1.slice();
        b2.put(t);
        b2.clear();
        verify(mac, macValue, b2, random);
        ByteBuffer b3 = ByteBuffer.allocateDirect(t.length);
        b3.put(t);
        b3.clear();
        verify(mac, macValue, b3, random);
        b2.clear();
        ByteBuffer b4 = b2.asReadOnlyBuffer();
        verify(mac, macValue, b4, random);
        System.out.println("All tests passed");
    }
    private static void verify(Mac mac, byte[] macValue, ByteBuffer b, Random random) throws Exception {
        int lim = b.limit();
        b.limit(random.nextInt(lim));
        mac.update(b);
        if (b.hasRemaining()) {
            throw new Exception("Buffer not consumed");
        }
        b.limit(lim);
        mac.update(b);
        if (b.hasRemaining()) {
            throw new Exception("Buffer not consumed");
        }
        byte[] newMacValue = mac.doFinal();
        if (Arrays.equals(macValue, newMacValue) == false) {
            throw new Exception("Mac did not verify");
        }
    }
}
