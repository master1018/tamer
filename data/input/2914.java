public abstract class TransformSpi {
    protected Transform _transformObject = null;
    protected void setTransform(Transform transform) {
        this._transformObject = transform;
    }
    protected XMLSignatureInput enginePerformTransform(
        XMLSignatureInput input, OutputStream os, Transform _transformObject)
        throws IOException,
               CanonicalizationException, InvalidCanonicalizerException,
               TransformationException, ParserConfigurationException,
               SAXException {
        return enginePerformTransform(input, _transformObject);
    }
    protected XMLSignatureInput enginePerformTransform(
        XMLSignatureInput input, Transform _transformObject)
        throws IOException,
               CanonicalizationException, InvalidCanonicalizerException,
               TransformationException, ParserConfigurationException,
               SAXException {
        try {
                TransformSpi tmp = (TransformSpi) getClass().newInstance();
            tmp.setTransform(_transformObject);
            return tmp.enginePerformTransform(input);
        } catch (InstantiationException e) {
            throw new TransformationException("",e);
        } catch (IllegalAccessException e) {
            throw new TransformationException("",e);
        }
    }
    protected XMLSignatureInput enginePerformTransform(
        XMLSignatureInput input)
        throws IOException,
               CanonicalizationException, InvalidCanonicalizerException,
               TransformationException, ParserConfigurationException,
               SAXException {
        throw new UnsupportedOperationException();
    }
    protected abstract String engineGetURI();
}
