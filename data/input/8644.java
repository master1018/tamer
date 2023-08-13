public class ParseFloat {
    private static void check(String val, float expected) {
        float n = Float.parseFloat(val);
        if (n != expected)
            throw new RuntimeException("Float.parseFloat failed. String:" +
                                                val + " Result:" + n);
    }
    private static void rudimentaryTest() {
        check(new String(""+Float.MIN_VALUE), Float.MIN_VALUE);
        check(new String(""+Float.MAX_VALUE), Float.MAX_VALUE);
        check("10",     (float)  10.0);
        check("10.0",   (float)  10.0);
        check("10.01",  (float)  10.01);
        check("-10",    (float) -10.0);
        check("-10.00", (float) -10.0);
        check("-10.01", (float) -10.01);
    }
    static  String badStrings[] = {
        "",
        "+",
        "-",
        "+e",
        "-e",
        "+e170",
        "-e170",
        "1234   e10",
        "-1234   e10",
        "1\u0007e1",
        "1e\u00071",
        "NaNf",
        "NaNF",
        "NaNd",
        "NaND",
        "-NaNf",
        "-NaNF",
        "-NaNd",
        "-NaND",
        "+NaNf",
        "+NaNF",
        "+NaNd",
        "+NaND",
        "Infinityf",
        "InfinityF",
        "Infinityd",
        "InfinityD",
        "-Infinityf",
        "-InfinityF",
        "-Infinityd",
        "-InfinityD",
        "+Infinityf",
        "+InfinityF",
        "+Infinityd",
        "+InfinityD",
        "NaNe10",
        "-NaNe10",
        "+NaNe10",
        "Infinitye10",
        "-Infinitye10",
        "+Infinitye10",
        "\u0661e\u0661", 
        "\u06F1e\u06F1", 
        "\u0967e\u0967" 
    };
    static String goodStrings[] = {
        "NaN",
        "+NaN",
        "-NaN",
        "Infinity",
        "+Infinity",
        "-Infinity",
        "1.1e-23f",
        ".1e-23f",
        "1e-23",
        "1f",
        "1",
        "2",
        "1234",
        "-1234",
        "+1234",
        "2147483647",   
        "2147483648",
        "-2147483648",  
        "-2147483649",
        "16777215",
        "16777216",     
        "16777217",
        "-16777215",
        "-16777216",    
        "-16777217",
        "9007199254740991",
        "9007199254740992",     
        "9007199254740993",
        "-9007199254740991",
        "-9007199254740992",    
        "-9007199254740993",
        "9223372036854775807",
        "9223372036854775808",  
        "9223372036854775809",
        "-9223372036854775808",
        "-9223372036854775809", 
        "-9223372036854775810"
    };
    static String paddedBadStrings[];
    static String paddedGoodStrings[];
    static {
        String pad = " \t\n\r\f\u0001\u000b\u001f";
        paddedBadStrings = new String[badStrings.length];
        for(int i = 0 ; i <  badStrings.length; i++)
            paddedBadStrings[i] = pad + badStrings[i] + pad;
        paddedGoodStrings = new String[goodStrings.length];
        for(int i = 0 ; i <  goodStrings.length; i++)
            paddedGoodStrings[i] = pad + goodStrings[i] + pad;
    }
    private static void testParsing(String [] input,
                                    boolean exceptionalInput) {
        for(int i = 0; i < input.length; i++) {
            double d;
            try {
                d = Float.parseFloat(input[i]);
            }
            catch (NumberFormatException e) {
                if (! exceptionalInput) {
                    throw new RuntimeException("Float.parseFloat rejected " +
                                               "good string `" + input[i] +
                                               "'.");
                }
                break;
            }
            if (exceptionalInput) {
                throw new RuntimeException("Float.parseFloat accepted " +
                                           "bad string `" + input[i] +
                                           "'.");
            }
        }
    }
    public static void main(String[] args) throws Exception {
        rudimentaryTest();
        testParsing(goodStrings, false);
        testParsing(paddedGoodStrings, false);
        testParsing(badStrings, true);
        testParsing(paddedBadStrings, true);
    }
}
