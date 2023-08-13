public class QuoteTest {
    static String testStr = "token1 token2 \"The test string\" token4";
        public static void main(String[] args) throws Exception {
        System.err.println("Parsing String: " + testStr);
        StreamTokenizer st = new StreamTokenizer(new StringReader(testStr));
        boolean foundToken = false;
        String matchStr = null;
        while(st.nextToken() != StreamTokenizer.TT_EOF)
        {
            switch (st.ttype) {
                case '\"':
                    foundToken = true;
                    matchStr = st.toString();
                    System.err.println("Found token " + matchStr);
                    break;
                default:
                    System.err.println("Found token " + st);
                    break;
            }
        }
        if (!foundToken)
            throw new RuntimeException("Test failed to recognize Quote type");
        if (!matchStr.equals("Token[The test string], line 1"))
            throw new RuntimeException("Test failed parse quoted string");
    }
}
