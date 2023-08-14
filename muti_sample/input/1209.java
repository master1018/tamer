public abstract class SignatureAlgorithmSpi {
   protected abstract String engineGetURI();
   protected abstract String engineGetJCEAlgorithmString();
   protected abstract String engineGetJCEProviderName();
   protected abstract void engineUpdate(byte[] input)
      throws XMLSignatureException;
   protected abstract void engineUpdate(byte input)
      throws XMLSignatureException;
   protected abstract void engineUpdate(byte buf[], int offset, int len)
      throws XMLSignatureException;
   protected abstract void engineInitSign(Key signingKey)
      throws XMLSignatureException;
   protected abstract void engineInitSign(
      Key signingKey, SecureRandom secureRandom) throws XMLSignatureException;
   protected abstract void engineInitSign(
      Key signingKey, AlgorithmParameterSpec algorithmParameterSpec)
         throws XMLSignatureException;
   protected abstract byte[] engineSign() throws XMLSignatureException;
   protected abstract void engineInitVerify(Key verificationKey)
      throws XMLSignatureException;
   protected abstract boolean engineVerify(byte[] signature)
      throws XMLSignatureException;
   protected abstract void engineSetParameter(AlgorithmParameterSpec params)
      throws XMLSignatureException;
   protected void engineGetContextFromElement(Element element) {
   }
   protected abstract void engineSetHMACOutputLength(int HMACOutputLength)
      throws XMLSignatureException;
    public void reset() {
        }
}
