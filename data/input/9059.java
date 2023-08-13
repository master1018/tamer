public class SignWithOutputBuffer {
    public static void main(String[] args) throws Exception {
        int numBytes;
        KeyPairGenerator kpGen = KeyPairGenerator.getInstance("DSA");
        kpGen.initialize(512);
        KeyPair kp = kpGen.genKeyPair();
        Signature sig = Signature.getInstance("DSS");
        sig.initSign(kp.getPrivate());
        sig.update((byte)0xff);
        byte[] out = new byte[10];
        try {
            numBytes = sig.sign(out, 0, out.length);
        } catch (SignatureException e) {
            System.out.println(e);
        }
        sig = Signature.getInstance("DSS");
        sig.initSign(kp.getPrivate());
        sig.update((byte)0xff);
        out = new byte[48];
        numBytes = sig.sign(out, 0, out.length);
        System.out.println("Signature len="+numBytes);
    }
}
