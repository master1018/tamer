public class MacClone {
    public static void main(String[] args) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA1", "SunJCE");
        Mac macClone = (Mac)mac.clone();
        System.out.println(macClone.getProvider().toString());
        System.out.println(macClone.getAlgorithm());
        boolean thrown = false;
        try {
            macClone.update((byte)0x12);
        } catch (IllegalStateException ise) {
            thrown = true;
        }
        if (!thrown) {
            throw new Exception("Expected IllegalStateException not thrown");
        }
        KeyGenerator kgen = KeyGenerator.getInstance("DES");
        SecretKey skey = kgen.generateKey();
        mac = Mac.getInstance("HmacSHA1");
        mac.init(skey);
        macClone = (Mac)mac.clone();
        System.out.println(macClone.getProvider().toString());
        System.out.println(macClone.getAlgorithm());
        mac.update((byte)0x12);
        macClone.update((byte)0x12);
        byte[] macFinal = mac.doFinal();
        byte[] macCloneFinal = macClone.doFinal();
        if (!java.util.Arrays.equals(macFinal, macCloneFinal)) {
            throw new Exception("MAC results are different");
        }
    }
}
