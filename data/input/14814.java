public class Canonicalizer20010315ExclOmitComments
        extends Canonicalizer20010315Excl {
        public Canonicalizer20010315ExclOmitComments() {
      super(false);
   }
   public final String engineGetURI() {
      return Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS;
   }
   public final boolean engineGetIncludeComments() {
      return false;
   }
}
