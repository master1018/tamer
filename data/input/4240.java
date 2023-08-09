public class EllipticCurveMatch {
    static String primeP256 =
        "0FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFF";
    static String aP256 =
        "0FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFC";
    static String bP256 =
        "05AC635D8AA3A93E7B3EBBD55769886BC651D06B0CC53B0F63BCE3C3E27D2604B";
    static String seedP256 =
        "0C49D360886E704936A6678E1139D26B7819F7E90";
    private static EllipticCurve addSeedToCurve(EllipticCurve curve)
    {
        SecureRandom rand = new SecureRandom();
        byte[] seed = new byte[12];
        rand.nextBytes(seed);
        return new EllipticCurve (curve.getField(), curve.getA(), curve.getB(),
            seed);
    }
    private static EllipticCurve getP256Curve()
    {
        ECFieldFp field = new ECFieldFp(new BigInteger (primeP256,16));
        BigInteger a = new BigInteger (aP256, 16);
        BigInteger b = new BigInteger (bP256, 16);
        return new EllipticCurve (field, a, b);
    }
    public static void main (String[] argv) throws Exception {
        EllipticCurve firstCurve = getP256Curve();
        EllipticCurve secondCurve = addSeedToCurve(firstCurve);
        EllipticCurve thirdCurve = addSeedToCurve(firstCurve);
        if (!firstCurve.equals(firstCurve))
            throw new Exception("Original curve doesn't equal itself");
        if (!firstCurve.equals(secondCurve))
            throw new Exception ("Original curve doesn't equal seeded curve");
        if (!secondCurve.equals(secondCurve))
            throw new Exception ("Seeded curve doesn't equal itself");
        if (!secondCurve.equals(thirdCurve))
            throw new Exception ("Seeded curve doesn't equal differently " +
                "seeded curve");
        System.out.println("Curve equals test passed");
    }
}
