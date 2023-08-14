public class Canonicalizer20010315OmitComments extends Canonicalizer20010315 {
   public Canonicalizer20010315OmitComments() {
      super(false);
   }
   public final String engineGetURI() {
      return Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS;
   }
   public final boolean engineGetIncludeComments() {
      return false;
   }
}
