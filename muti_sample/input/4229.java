public class Canonicalizer20010315ExclWithComments
        extends Canonicalizer20010315Excl {
   public Canonicalizer20010315ExclWithComments() {
      super(true);
   }
   public final String engineGetURI() {
      return Canonicalizer.ALGO_ID_C14N_EXCL_WITH_COMMENTS;
   }
   public final boolean engineGetIncludeComments() {
      return true;
   }
}
