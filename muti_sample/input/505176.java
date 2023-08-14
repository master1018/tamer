class DumpPublicKey {
    static void check(RSAPublicKey key) throws Exception {
        BigInteger pubexp = key.getPublicExponent();
        BigInteger modulus = key.getModulus();
        if (!pubexp.equals(BigInteger.valueOf(3)))
                throw new Exception("Public exponent should be 3 but is " +
                        pubexp.toString(10) + ".");
        if (modulus.bitLength() != 2048)
             throw new Exception("Modulus should be 2048 bits long but is " +
                        modulus.bitLength() + " bits.");
    }
    static String print(RSAPublicKey key) throws Exception {
        check(key);
        BigInteger N = key.getModulus();
        StringBuilder result = new StringBuilder();
        int nwords = N.bitLength() / 32;    
        result.append("{");
        result.append(nwords);
        BigInteger B = BigInteger.valueOf(0x100000000L);  
        BigInteger N0inv = B.subtract(N.modInverse(B));   
        result.append(",0x");
        result.append(N0inv.toString(16));
        BigInteger R = BigInteger.valueOf(2).pow(N.bitLength());
        BigInteger RR = R.multiply(R).mod(N);    
        result.append(",{");
        for (int i = 0; i < nwords; ++i) {
            long n = N.mod(B).longValue();
            result.append(n);
            if (i != nwords - 1) {
                result.append(",");
            }
            N = N.divide(B);
        }
        result.append("}");
        result.append(",{");
        for (int i = 0; i < nwords; ++i) {
            long rr = RR.mod(B).longValue();
            result.append(rr);
            if (i != nwords - 1) {
                result.append(",");
            }
            RR = RR.divide(B);
        }
        result.append("}");
        result.append("}");
        return result.toString();
    }
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: DumpPublicKey certfile ... > source.c");
            System.exit(1);
        }
        try {
            for (int i = 0; i < args.length; i++) {
                FileInputStream input = new FileInputStream(args[i]);
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                Certificate cert = cf.generateCertificate(input);
                RSAPublicKey key = (RSAPublicKey) (cert.getPublicKey());
                check(key);
                System.out.print(print(key));
                System.out.println(i < args.length - 1 ? "," : "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }
}
