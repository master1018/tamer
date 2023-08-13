public class NaNInfinityParsing {
    static String NaNStrings[] = {
        "NaN",
        "+NaN",
        "-NaN"
    };
    static String infinityStrings[] = {
        "Infinity",
        "+Infinity",
        "-Infinity",
    };
    static String invalidStrings[] = {
        "+",
        "-",
        "@",
        "N",
        "Na",
        "Nan",
        "NaNf",
        "NaNd",
        "NaNF",
        "NaND",
        "+N",
        "+Na",
        "+Nan",
        "+NaNf",
        "+NaNd",
        "+NaNF",
        "+NaND",
        "-N",
        "-Na",
        "-Nan",
        "-NaNf",
        "-NaNd",
        "-NaNF",
        "-NaND",
        "I",
        "In",
        "Inf",
        "Infi",
        "Infin",
        "Infini",
        "Infinit",
        "InfinitY",
        "Infinityf",
        "InfinityF",
        "Infinityd",
        "InfinityD",
        "+I",
        "+In",
        "+Inf",
        "+Infi",
        "+Infin",
        "+Infini",
        "+Infinit",
        "+InfinitY",
        "+Infinityf",
        "+InfinityF",
        "+Infinityd",
        "+InfinityD",
        "-I",
        "-In",
        "-Inf",
        "-Infi",
        "-Infin",
        "-Infini",
        "-Infinit",
        "-InfinitY",
        "-Infinityf",
        "-InfinityF",
        "-Infinityd",
        "-InfinityD",
        "NaNInfinity",
        "InfinityNaN",
        "nan",
        "infinity"
    };
    public static void main(String [] argv) throws Exception {
        int i;
        double d;
        for(i = 0; i < NaNStrings.length; i++) {
            if(!Double.isNaN(d=Double.parseDouble(NaNStrings[i]))) {
                throw new RuntimeException("NaN string ``" + NaNStrings[i]
                                           + "'' did not parse as a NaN; returned " +
                                           d + " instead.");
            }
        }
        for(i = 0; i < infinityStrings.length; i++) {
            if(!Double.isInfinite(d=Double.parseDouble(infinityStrings[i]))) {
                throw new RuntimeException("Infinity string ``" +
                                           infinityStrings[i] +
                                           "'' did not parse as infinity; returned " +
                                           d + "instead.");
            }
            boolean negative = (infinityStrings[i].charAt(0) == '-');
            if(d != (negative?Double.NEGATIVE_INFINITY:
                          Double.POSITIVE_INFINITY))
                throw new RuntimeException("Infinity has wrong sign;" +
                                           (negative?"positive instead of negative.":
                                            "negative instead of positive."));
        }
        for(i = 0; i < invalidStrings.length; i++) {
            try {
                double result;
                d = Double.parseDouble(invalidStrings[i]);
                throw new RuntimeException("Invalid string ``" +
                                           invalidStrings[i]
                                           +"'' parsed as " + d + ".");
            }
            catch(NumberFormatException e) {
            }
        }
    }
}
