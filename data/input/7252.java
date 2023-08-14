public class SkipNegative {
    public static void main(String argv[]) throws Exception {
        char[] cbuf = "testString".toCharArray();
        CharArrayReader CAR = new CharArrayReader(cbuf);
        BufferedReader BR = new BufferedReader(CAR);
        long nchars = -1L;
        try {
            long actual = BR.skip(nchars);
        } catch(IllegalArgumentException e){
            return;
        }
        throw new Exception("Skip should not accept negative values");
    }
}
