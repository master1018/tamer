public class ModPow {
    public static void main(String[] args) {
        Random rnd = new Random(1234);
        for (int i=0; i<2000; i++) {
            BigInteger m = new BigInteger(800, rnd);
            BigInteger base = new BigInteger(16, rnd);
            if (rnd.nextInt() % 1 == 0)
                base = base.negate();
            BigInteger exp = new BigInteger(8, rnd);
            BigInteger z = base.modPow(exp, m);
            BigInteger w = base.pow(exp.intValue()).mod(m);
            if (!z.equals(w)){
                System.err.println(base +" ** " + exp + " mod "+ m);
                System.err.println("modPow : " + z);
                System.err.println("pow.mod: " + w);
                throw new RuntimeException("BigInteger modPow failure.");
            }
        }
    }
}
