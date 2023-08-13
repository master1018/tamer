public class NPE {
    public static void main(String args[]) throws Exception {
        System.setIn(new ByteArrayInputStream(new byte[0]));
        try {
            new TextCallbackHandler().handle(new Callback[]  {
                new NameCallback("Name: ") }
            );
        } catch (IOException ioe) {
        }
   }
}
