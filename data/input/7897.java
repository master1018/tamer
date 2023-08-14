public class TestExponentSize {
    private enum Sizes {
        two56(256), three84(384), five12(512), seven68(768), ten24(1024);
        private final int intSize;
        private final BigInteger bigIntValue;
        Sizes(int size) {
            intSize = size;
            byte [] bits = new byte[intSize/8];
            Arrays.fill(bits, (byte)0xff);
            bigIntValue = new BigInteger(1, bits);
        }
        int getIntSize() {
            return intSize;
        }
        BigInteger getBigIntValue() {
            return bigIntValue;
        }
    }
    public static void main(String[] args) throws Exception {
        KeyPair kp;
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DH", "SunJCE");
        kp = kpg.generateKeyPair();
        checkKeyPair(kp, Sizes.ten24, Sizes.five12);
        DHPublicKey publicKey = (DHPublicKey)kp.getPublic();
        BigInteger p = publicKey.getParams().getP();
        BigInteger g = publicKey.getParams().getG();
        kpg.initialize(Sizes.ten24.getIntSize());
        kp = kpg.generateKeyPair();
        checkKeyPair(kp, Sizes.ten24, Sizes.five12);
        kpg.initialize(new DHParameterSpec(p, g, Sizes.ten24.getIntSize()));
        kp = kpg.generateKeyPair();
        checkKeyPair(kp, Sizes.ten24, Sizes.ten24);
        kpg.initialize(new DHParameterSpec(p, g, Sizes.five12.getIntSize()));
        kp = kpg.generateKeyPair();
        checkKeyPair(kp, Sizes.ten24, Sizes.five12);
        kpg.initialize(new DHParameterSpec(p, g, Sizes.two56.getIntSize()));
        kp = kpg.generateKeyPair();
        checkKeyPair(kp, Sizes.ten24, Sizes.two56);
        kpg.initialize(Sizes.five12.getIntSize());
        kp = kpg.generateKeyPair();
        checkKeyPair(kp, Sizes.five12, Sizes.three84);
        kpg.initialize(Sizes.seven68.getIntSize());
        kp = kpg.generateKeyPair();
        checkKeyPair(kp, Sizes.seven68, Sizes.three84);
        System.out.println("OK");
    }
    private static void checkKeyPair(KeyPair kp, Sizes modulusSize,
            Sizes exponentSize) throws Exception {
        System.out.println("Checking (" + modulusSize + ", " +
            exponentSize + ")");
        DHPrivateKey privateKey = (DHPrivateKey)kp.getPrivate();
        BigInteger p = privateKey.getParams().getP();
        BigInteger x = privateKey.getX();
        if (p.bitLength() != modulusSize.getIntSize()) {
            throw new Exception("Invalid modulus size: " + p.bitLength());
        }
        if (x.bitLength() > exponentSize.getIntSize()) {
            throw new Exception("X has more bits than expected: " +
                x.bitLength());
        }
        BigInteger pMinus2 =
            p.subtract(BigInteger.ONE).subtract(BigInteger.ONE);
        if ((x.compareTo(BigInteger.ONE) < 0 ||
                (x.compareTo(pMinus2)) > 0)) {
            throw new Exception(
                "X outside range 1<=x<p-2:  x: " + x + " p: " + p);
        }
    }
}
