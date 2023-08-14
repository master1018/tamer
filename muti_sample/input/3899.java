public class Canonicalizer20010315WithComments extends Canonicalizer20010315 {
   public Canonicalizer20010315WithComments() {
      super(true);
   }
   public final String engineGetURI() {
      return Canonicalizer.ALGO_ID_C14N_WITH_COMMENTS;
   }
   public final boolean engineGetIncludeComments() {
      return true;
   }
}
