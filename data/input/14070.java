public class DecodeNonEncoded {
    static String[] errorStrings
        = {"%", "%A", "Hello%", "%xy", "%az", "%ab%q"};
    static String[] ignoreStrings = {"#", "X@Y", "Hello There"};
    public static void main(String[] args) throws Exception {
        for (int i = 0; i < errorStrings.length; i++) {
            try {
                URLDecoder.decode(errorStrings[i]);
                throw new Exception("String \"" + errorStrings[i]
                    + "\" should have failed in URLDecoder.decode!");
            } catch (IllegalArgumentException e) {
                System.out.println("String \"" + errorStrings[i]
                   + "\" correctly threw IllegalArgumentException: "
                   + e.getMessage());
            }
        }
        String temp;
        for (int i = 0; i < ignoreStrings.length; i++) {
            temp = URLDecoder.decode(ignoreStrings[i]);
            if (!temp.equals(ignoreStrings[i]))
                throw new Exception("String \"" + ignoreStrings[i]
                        + "\" was converted to " + temp
                        +" by URLDecoder.decode to ");
            else
                System.out.println("String \"" + temp
                      + "\" was left unchanged by URLDecoder.decode.");
        }
    }
}
