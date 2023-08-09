public class ParseDouble {
    private static void check(String val, double expected) {
        double n = Double.parseDouble(val);
        if (n != expected)
            throw new RuntimeException("Double.parseDouble failed. String:" +
                                                val + " Result:" + n);
    }
    private static void rudimentaryTest() {
        check(new String(""+Double.MIN_VALUE), Double.MIN_VALUE);
        check(new String(""+Double.MAX_VALUE), Double.MAX_VALUE);
        check("10",     (double)  10.0);
        check("10.0",   (double)  10.0);
        check("10.01",  (double)  10.01);
        check("-10",    (double) -10.0);
        check("-10.00", (double) -10.0);
        check("-10.01", (double) -10.01);
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
        "\u0967e\u0967", 
        ".",
        "e42",
        ".e42",
        "d",
        ".d",
        "e42d",
        ".e42d",
        "1A01.01125e-10d",
        "2;3.01125e-10d",
        "1_34.01125e-10d",
        "202..01125e-10d",
        "202,01125e-10d",
        "202.03b4e-10d",
        "202.06_3e-10d",
        "202.01125e-f0d",
        "202.01125e_3d",
        "202.01125e -5d",
        "202.01125e-10r",
        "202.01125e-10ff",
        "1234L.01",
        "12ee-2",
        "12e-2.2.2",
        "12.01e+",
        "12.01E",
        "00x1.0p1",
        "1.0p1",
        "00010p1",
        "deadbeefp1",
        "0x1.0p",
        "0x1.0",
        "0x1.0pa",
        "0x1.0pf",
        "0x1.0e22",
        "0x1.0e22",
        "0xp22"
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
        "0",
        "-0",
        "+0",
        "00",
        "00",
        "-00",
        "+00",
        "0000000000",
        "-0000000000",
        "+0000000000",
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
        "-9223372036854775810",
        "54.07140d",
        "7.01e-324d",
        "2147483647.01d",
        "1.2147483647f",
        "000000000000000000000000001.F",
        "1.00000000000000000000000000e-2F",
        "2.",
        ".0909",
        "122112217090.0",
        "7090e-5",
        "2.E-20",
        ".0909e42",
        "122112217090.0E+100",
        "7090f",
        "2.F",
        ".0909d",
        "122112217090.0D",
        "7090e-5f",
        "2.E-20F",
        ".0909e42d",
        "122112217090.0E+100D",
        "\u0035\u0031\u0034\u0039\u0032\u0033\u0036\u0037\u0038\u0030.1102E-209D",
        "1290873\u002E12301e100",
        "1.1E-10\u0066",
        "0.0E-10",
        "1E10",
        "0.f",
        "1f",
        "0.F",
        "1F",
        "0.12d",
        "1e-0d",
        "12.e+1D",
        "0e-0D",
        "12.e+01",
        "1e-01",
        "0x1p1",
        "0X1p1",
        "0x1P1",
        "0X1P1",
        "0x1p1f",
        "0X1p1f",
        "0x1P1f",
        "0X1P1f",
        "0x1p1F",
        "0X1p1F",
        "0x1P1F",
        "0X1P1F",
        "0x1p1d",
        "0X1p1d",
        "0x1P1d",
        "0X1P1d",
        "0x1p1D",
        "0X1p1D",
        "0x1P1D",
        "0X1P1D",
        "-0x1p1",
        "-0X1p1",
        "-0x1P1",
        "-0X1P1",
        "-0x1p1f",
        "-0X1p1f",
        "-0x1P1f",
        "-0X1P1f",
        "-0x1p1F",
        "-0X1p1F",
        "-0x1P1F",
        "-0X1P1F",
        "-0x1p1d",
        "-0X1p1d",
        "-0x1P1d",
        "-0X1P1d",
        "-0x1p1D",
        "-0X1p1D",
        "-0x1P1D",
        "-0X1P1D",
        "0x1p-1",
        "0X1p-1",
        "0x1P-1",
        "0X1P-1",
        "0x1p-1f",
        "0X1p-1f",
        "0x1P-1f",
        "0X1P-1f",
        "0x1p-1F",
        "0X1p-1F",
        "0x1P-1F",
        "0X1P-1F",
        "0x1p-1d",
        "0X1p-1d",
        "0x1P-1d",
        "0X1P-1d",
        "0x1p-1D",
        "0X1p-1D",
        "0x1P-1D",
        "0X1P-1D",
        "-0x1p-1",
        "-0X1p-1",
        "-0x1P-1",
        "-0X1P-1",
        "-0x1p-1f",
        "-0X1p-1f",
        "-0x1P-1f",
        "-0X1P-1f",
        "-0x1p-1F",
        "-0X1p-1F",
        "-0x1P-1F",
        "-0X1P-1F",
        "-0x1p-1d",
        "-0X1p-1d",
        "-0x1P-1d",
        "-0X1P-1d",
        "-0x1p-1D",
        "-0X1p-1D",
        "-0x1P-1D",
        "-0X1P-1D",
        "0xap1",
        "0xbp1",
        "0xcp1",
        "0xdp1",
        "0xep1",
        "0xfp1",
        "0x1p1",
        "0x.1p1",
        "0x1.1p1",
        "0x001p23",
        "0x00.1p1",
        "0x001.1p1",
        "0x100p1",
        "0x.100p1",
        "0x1.100p1",
        "0x00100p1",
        "0x00.100p1",
        "0x001.100p1",
        "1.7976931348623157E308",     
        "4.9e-324",                   
        "2.2250738585072014e-308",    
        "2.2250738585072012e-308",    
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
                d = Double.parseDouble(input[i]);
            }
            catch (NumberFormatException e) {
                if (! exceptionalInput) {
                    throw new RuntimeException("Double.parseDouble rejected " +
                                               "good string `" + input[i] +
                                               "'.");
                }
                break;
            }
            if (exceptionalInput) {
                throw new RuntimeException("Double.parseDouble accepted " +
                                           "bad string `" + input[i] +
                                           "'.");
            }
        }
    }
    private static void testRegex(String [] input, boolean exceptionalInput) {
        final String Digits     = "(\\p{Digit}+)";
        final String HexDigits  = "(\\p{XDigit}+)";
        final String Exp        = "[eE][+-]?"+Digits;
        final String fpRegex    =
            ("[\\x00-\\x20]*"+  
             "[+-]?(" + 
             "NaN|" +           
             "Infinity|" +      
             "(((("+Digits+"(\\.)?("+Digits+"?)("+Exp+")?)|"+
             "(\\.("+Digits+")("+Exp+")?))|"+
            "((" +
             "(0[xX]" + HexDigits + "(\\.)?)|" +
             "(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")" +
             ")[pP][+-]?" + Digits + "))" +
             "[fFdD]?))" +
             "[\\x00-\\x20]*");
        Pattern fpPattern = Pattern.compile(fpRegex);
        for(int i = 0; i < input.length; i++) {
             Matcher m = fpPattern.matcher(input[i]);
             if (m.matches() != ! exceptionalInput) {
                 throw new RuntimeException("Regular expression " +
                                            (exceptionalInput?
                                             "accepted bad":
                                             "rejected good") +
                                            " string `" +
                                            input[i] + "'.");
             }
        }
    }
    private static void testSubnormalPowers() {
        BigDecimal TWO = BigDecimal.valueOf(2);
        BigDecimal ulp_BD = new BigDecimal(Double.MIN_VALUE);
        for(int i = -1074; i <= -1022; i++) {
            double d = Math.scalb(1.0, i);
            BigDecimal d_BD = new BigDecimal(d);
            BigDecimal lowerBound = d_BD.subtract(ulp_BD.divide(TWO));
            BigDecimal upperBound = d_BD.add(ulp_BD.divide(TWO));
            double convertedLowerBound = Double.parseDouble(lowerBound.toString());
            double convertedUpperBound = Double.parseDouble(upperBound.toString());
        }
    }
    private static void testStrictness() {
        final double expected = 0x0.0000008000001p-1022;
        boolean failed = false;
        double conversion = 0.0;
        double sum = 0.0; 
        String decimal = "6.631236871469758276785396630275967243399099947355303144249971758736286630139265439618068200788048744105960420552601852889715006376325666595539603330361800519107591783233358492337208057849499360899425128640718856616503093444922854759159988160304439909868291973931426625698663157749836252274523485312442358651207051292453083278116143932569727918709786004497872322193856150225415211997283078496319412124640111777216148110752815101775295719811974338451936095907419622417538473679495148632480391435931767981122396703443803335529756003353209830071832230689201383015598792184172909927924176339315507402234836120730914783168400715462440053817592702766213559042115986763819482654128770595766806872783349146967171293949598850675682115696218943412532098591327667236328125E-316";
        for(int i = 0; i <= 12_000; i++) {
            conversion = Double.parseDouble(decimal);
            sum += conversion;
            if (conversion != expected) {
                failed = true;
                System.out.printf("Iteration %d converts as %a%n",
                                  i, conversion);
            }
        }
        System.out.println("Sum = "  + sum);
        if (failed)
            throw new RuntimeException("Inconsistent conversion");
    }
    public static void main(String[] args) throws Exception {
        rudimentaryTest();
        testParsing(goodStrings, false);
        testParsing(paddedGoodStrings, false);
        testParsing(badStrings, true);
        testParsing(paddedBadStrings, true);
        testRegex(goodStrings, false);
        testRegex(paddedGoodStrings, false);
        testRegex(badStrings, true);
        testRegex(paddedBadStrings, true);
        testSubnormalPowers();
        testStrictness();
    }
}
