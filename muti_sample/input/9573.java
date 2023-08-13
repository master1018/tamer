public class ProbablePrime {
    public static void main(String [] argv) {
                BigInteger num = new BigInteger("4");
                int certainties[] = {-1, 0, 1, 2, 100, Integer.MAX_VALUE-1,
                                     Integer.MAX_VALUE};
                boolean expectations[] = {true, true, false, false, false,
                                          false, false};
                for(int i = 0; i < certainties.length; i++) {
                    boolean b;
                    if((b=num.isProbablePrime(certainties[i])) !=
                       expectations[i])
                       throw new RuntimeException("Unexpected answer " + b +
                                                  " for certainty " +
                                                  certainties[i]);
                }
    }
}
