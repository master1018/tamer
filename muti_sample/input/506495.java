public class MyExemptionMechanismSpi  extends ExemptionMechanismSpi {
    private static final int byteArrayLength = 5;
    public static final int getLength() {
        return byteArrayLength;
    }
    @Override
    protected byte[] engineGenExemptionBlob()
            throws ExemptionMechanismException {
        return new byte[byteArrayLength];
    }
    @Override
    protected int engineGenExemptionBlob(byte[] output, int outputOffset)
            throws ShortBufferException, ExemptionMechanismException {
        return byteArrayLength;
    }
    @Override
    protected int engineGetOutputSize(int inputLen) {
        return 10;
    }
    @Override
    protected void engineInit(Key key) throws InvalidKeyException,
            ExemptionMechanismException {
        if (key == null) {
            throw new InvalidKeyException("key is null");
        }
        if (!(key instanceof tmpKey)) {
            throw new ExemptionMechanismException("Incorrect key");
        }
    }
    @Override
    protected void engineInit(Key key, AlgorithmParameters params)
            throws InvalidKeyException, InvalidAlgorithmParameterException,
            ExemptionMechanismException {
        if (key == null) {
            throw new InvalidKeyException("key is null");            
        }
        if (!(key instanceof tmpKey)) {
            throw new ExemptionMechanismException("Incorrect key");
        }
        if (params == null) {
            throw new InvalidAlgorithmParameterException("params is null");
        }
    }
    @Override
    protected void engineInit(Key key, AlgorithmParameterSpec params)
            throws InvalidKeyException, InvalidAlgorithmParameterException,
            ExemptionMechanismException {
        if (key == null) {
            throw new InvalidKeyException("key is null");
        }
        if (!(key instanceof tmpKey)) {
            throw new ExemptionMechanismException("Incorrect key");
        }
        if (params == null) {
            throw new InvalidAlgorithmParameterException("params is null");
        }
    }
    @SuppressWarnings("serial")
    public class tmpKey implements Key {
        private String alg;
        private byte[] enc;
        public tmpKey(String alg, byte[] enc) {
            this.alg = alg;
            this.enc = enc;
        }
        public String getFormat() {
            return "tmpKey";
        }
        public String getAlgorithm() {
            return alg;
        }
        public byte[] getEncoded() {
            return enc;
        }
    }
    @SuppressWarnings("serial")
    public class tmp1Key implements Key {
        private byte[] enc;
        public tmp1Key(String alg, byte[] enc) {
            this.enc = enc;
        }
        public String getAlgorithm() {
            return "tmp1Key";
        }
        public String getFormat() {
            return "tmp1Key";
        }
        public byte[] getEncoded() {
            return enc;
        }
    }
}
