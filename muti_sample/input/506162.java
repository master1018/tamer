public abstract class SignatureSpi {
    protected SecureRandom appRandom;
    protected abstract void engineInitVerify(PublicKey publicKey)
            throws InvalidKeyException;
    protected abstract void engineInitSign(PrivateKey privateKey)
            throws InvalidKeyException;
    protected void engineInitSign(PrivateKey privateKey, SecureRandom random)
            throws InvalidKeyException {
        appRandom = random;
        engineInitSign(privateKey);
    }
    protected abstract void engineUpdate(byte b) throws SignatureException;
    protected abstract void engineUpdate(byte[] b, int off, int len)
            throws SignatureException;
    protected void engineUpdate(ByteBuffer input) {
        if (!input.hasRemaining()) {
            return;
        }
        byte[] tmp;
        if (input.hasArray()) {
            tmp = input.array();
            int offset = input.arrayOffset();
            int position = input.position();
            int limit = input.limit();
            try {
                engineUpdate(tmp, offset + position, limit - position);
            } catch (SignatureException e) { 
                throw new RuntimeException(e); 
            }
            input.position(limit);
        } else {
            tmp = new byte[input.limit() - input.position()];
            input.get(tmp);
            try {
                engineUpdate(tmp, 0, tmp.length);
            } catch (SignatureException e) {
                throw new RuntimeException(e); 
            }
        }
    }
    protected abstract byte[] engineSign() throws SignatureException;
    protected int engineSign(byte[] outbuf, int offset, int len)
            throws SignatureException {
        byte tmp[] = engineSign();
        if (tmp == null) {
            return 0;
        }
        if (len < tmp.length) {
            throw new SignatureException(Messages.getString("security.2D")); 
        }
        if (offset < 0) {
            throw new SignatureException(Messages.getString("security.1C")); 
        }
        if (offset + len > outbuf.length) {
            throw new SignatureException(Messages.getString("security.05")); 
        }
        System.arraycopy(tmp, 0, outbuf, offset, tmp.length);
        return tmp.length;
    }
    protected abstract boolean engineVerify(byte[] sigBytes)
            throws SignatureException;
    protected boolean engineVerify(byte[] sigBytes, int offset, int length)
            throws SignatureException {
        byte tmp[] = new byte[length];
        System.arraycopy(sigBytes, offset, tmp, 0, length);
        return engineVerify(tmp);
    }
    @Deprecated
    protected abstract void engineSetParameter(String param, Object value)
            throws InvalidParameterException;
    protected void engineSetParameter(AlgorithmParameterSpec params)
            throws InvalidAlgorithmParameterException {
        throw new UnsupportedOperationException();
    }
    protected AlgorithmParameters engineGetParameters() {
        throw new UnsupportedOperationException();
    }
    @Deprecated
    protected abstract Object engineGetParameter(String param)
            throws InvalidParameterException;
    @Override
    public Object clone() throws CloneNotSupportedException {
        if (this instanceof Cloneable) {
            return super.clone();
        }
        throw new CloneNotSupportedException();
    }
}
