public class OperatorNpeTests {
    public static void main(String... argv) {
        BigInteger[] specialValues = {ZERO, ONE, TEN};
        for (BigInteger bd : specialValues) {
            BigInteger result;
            try {
                result = bd.multiply(null);
                throw new RuntimeException("Instead of NPE got " + result);
            } catch (NullPointerException npe) {
                ; 
            }
            try {
                result = bd.divide(null);
                throw new RuntimeException("Instead of NPE got " + result);
            } catch (NullPointerException npe) {
                ; 
            }
            try {
                result = bd.add(null);
                throw new RuntimeException("Instead of NPE got " + result);
            } catch (NullPointerException npe) {
                ; 
            }
            try {
                result = bd.subtract(null);
                throw new RuntimeException("Instead of NPE got " + result);
            } catch (NullPointerException npe) {
                ; 
            }
        }
    }
}
