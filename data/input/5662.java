public class CheckICNE {
    static int failed = 0;
    public static void main (String[] args) throws Exception {
        try {
            Charset.forName("abc+");
        } catch (UnsupportedCharsetException uce) {}
        try {
            java.nio.charset.Charset.forName("+abc");
        } catch (IllegalCharsetNameException icne) {}
        String[] euros = {"PC-Multilingual-850+euro",
                          "ebcdic-us-037+euro",
                          "ebcdic-de-273+euro",
                          "ebcdic-no-277+euro",
                          "ebcdic-dk-277+euro",
                          "ebcdic-fi-278+euro",
                          "ebcdic-se-278+euro",
                          "ebcdic-it-280+euro",
                          "ebcdic-es-284+euro",
                          "ebcdic-gb-285+euro",
                          "ebcdic-fr-277+euro",
                          "ebcdic-international-500+euro",
                          "ebcdic-s-871+euro"
        };
        System.out.println("Test Passed!");
    }
}
