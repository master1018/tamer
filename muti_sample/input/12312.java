public class BrokenRSAPrivateCrtKey {
    public static void main(String[] args) throws Exception {
        KeyPairGenerator generator =
                KeyPairGenerator.getInstance("RSA", "SunJSSE");
        generator.initialize(512);
        KeyPair pair = generator.generateKeyPair();
        RSAPrivateCrtKey privatekey = (RSAPrivateCrtKey) pair.getPrivate();
        RSAPrivateCrtKeySpec spec =
                new RSAPrivateCrtKeySpec(privatekey.getModulus(),
                privatekey.getPublicExponent(),
                privatekey.getPrivateExponent(),
                privatekey.getPrimeP(), privatekey.getPrimeQ(),
                privatekey.getPrimeExponentP(),
                privatekey.getPrimeExponentQ(),
                privatekey.getCrtCoefficient());
        KeyFactory factory = KeyFactory.getInstance("RSA", "SunJSSE");
        PrivateKey privatekey2 = factory.generatePrivate(spec);
        BigInteger pe =
                ((RSAPrivateCrtKey) privatekey2).getPublicExponent();
        System.out.println("public exponent: " + pe);
    }
}
