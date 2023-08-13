public final class DESParameters extends AlgorithmParametersSpi {
    private BlockCipherParamsCore core;
    public DESParameters() {
        core = new BlockCipherParamsCore(DESConstants.DES_BLOCK_SIZE);
    }
    protected void engineInit(AlgorithmParameterSpec paramSpec)
        throws InvalidParameterSpecException {
        core.init(paramSpec);
    }
    protected void engineInit(byte[] encoded)
        throws IOException {
        core.init(encoded);
    }
    protected void engineInit(byte[] encoded, String decodingMethod)
        throws IOException {
        core.init(encoded, decodingMethod);
    }
    protected AlgorithmParameterSpec engineGetParameterSpec(Class paramSpec)
        throws InvalidParameterSpecException {
        return core.getParameterSpec(paramSpec);
    }
    protected byte[] engineGetEncoded() throws IOException {
        return core.getEncoded();
    }
    protected byte[] engineGetEncoded(String encodingMethod)
        throws IOException {
        return core.getEncoded();
    }
    protected String engineToString() {
        return core.toString();
    }
}
