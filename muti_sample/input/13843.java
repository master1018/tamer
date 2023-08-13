public class TransformXPointer extends TransformSpi {
   public static final String implementedTransformURI =
      Transforms.TRANSFORM_XPOINTER;
   protected String engineGetURI() {
      return implementedTransformURI;
   }
   protected XMLSignatureInput enginePerformTransform(XMLSignatureInput input, Transform _transformObject)
           throws  TransformationException {
      Object exArgs[] = { implementedTransformURI };
      throw new TransformationException(
         "signature.Transform.NotYetImplemented", exArgs);
   }
}
