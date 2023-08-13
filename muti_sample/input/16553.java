public class TransformC14N11_WithComments extends TransformSpi {
    protected String engineGetURI() {
        return Transforms.TRANSFORM_C14N11_WITH_COMMENTS;
    }
    protected XMLSignatureInput enginePerformTransform
        (XMLSignatureInput input, Transform transform)
        throws CanonicalizationException {
        return enginePerformTransform(input, null, transform);
    }
    protected XMLSignatureInput enginePerformTransform
        (XMLSignatureInput input, OutputStream os, Transform transform)
        throws CanonicalizationException {
        Canonicalizer11_WithComments c14n = new Canonicalizer11_WithComments();
        if (os != null) {
            c14n.setWriter(os);
        }
        byte[] result = null;
        result = c14n.engineCanonicalize(input);
        XMLSignatureInput output = new XMLSignatureInput(result);
        if (os != null) {
            output.setOutputStream(os);
        }
        return output;
    }
}
