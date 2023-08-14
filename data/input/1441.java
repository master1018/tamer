public class FormFeed {
   public static void main(String[] argv) {
      StringTokenizer st = new StringTokenizer("ABCD\tEFG\fHIJKLM PQR");
      if (st.countTokens() != 4)
         throw new RuntimeException("StringTokenizer does not treat form feed as whitespace.");
    }
}
