public class Tests {
    private Tests(){}
    static int test(String testName,
                    double input,
                    double result,
                    double expected) {
        if (Double.compare(expected, result ) != 0) {
            System.err.println("Failure for " + testName + ":\n" +
                               "\tFor input "   + input    + "\t(" + Double.toHexString(input) + ")\n" +
                               "\texpected  " + expected + "\t(" + Double.toHexString(expected) + ")\n" +
                               "\tgot       " + result   + "\t(" + Double.toHexString(result) + ").");
            return 1;
        }
        else
            return 0;
    }
    static int test(String testName, double input1,  double input2,
                    double result, double expected) {
        if (Double.compare(expected, result ) != 0) {
            System.err.println("Failure for " + testName + ":\n" +
                               "\tFor input "   + input1   + "\t(" + Double.toHexString(input1) + "), " +
                                                + input2   + "\t(" + Double.toHexString(input2) + ")\n" +
                               "\texpected  " + expected + "\t(" + Double.toHexString(expected) + ")\n" +
                               "\tgot       " + result   + "\t(" + Double.toHexString(result) + ").");
            return 1;
        }
        else
            return 0;
    }
}
