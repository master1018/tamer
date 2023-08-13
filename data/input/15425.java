public class NullConstruct {
    public static void main(String[] args) throws Exception {
        try {
            Reader in = null;
            StreamTokenizer st = new StreamTokenizer(in);
            throw new Exception
                ("Failed test: constructor didn't catch null input");
        } catch (NullPointerException e) {
        }
    }
}
