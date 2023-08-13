public abstract class CipherSpi {
    public CipherSpi() {
    }
    protected abstract void engineSetMode(String mode)
            throws NoSuchAlgorithmException;
    protected abstract void engineSetPadding(String padding)
            throws NoSuchPaddingException;
    protected abstract int engineGetBlockSize();
    protected abstract int engineGetOutputSize(int inputLen);
    protected abstract byte[] engineGetIV();
    protected abstract AlgorithmParameters engineGetParameters();
    protected abstract void engineInit(int opmode, Key key, SecureRandom random)
            throws InvalidKeyException;
    protected abstract void engineInit(int opmode, Key key,
            AlgorithmParameterSpec params, SecureRandom random)
            throws InvalidKeyException, InvalidAlgorithmParameterException;
    protected abstract void engineInit(int opmode, Key key,
            AlgorithmParameters params, SecureRandom random)
            throws InvalidKeyException, InvalidAlgorithmParameterException;
    protected abstract byte[] engineUpdate(byte[] input, int inputOffset,
            int inputLen);
    protected abstract int engineUpdate(byte[] input, int inputOffset,
            int inputLen, byte[] output, int outputOffset)
            throws ShortBufferException;
    protected int engineUpdate(ByteBuffer input, ByteBuffer output)
            throws ShortBufferException {
        if (input == null) {
            throw new NullPointerException(Messages.getString("crypto.0C")); 
        }
        if (output == null) {
            throw new NullPointerException(Messages.getString("crypto.0D")); 
        }
        int position = input.position();
        int limit = input.limit();
        if ((limit - position) <= 0) {
            return 0;
        }
        byte[] bInput;
        byte[] bOutput;
        if (input.hasArray()) {
            bInput = input.array();
            int offset = input.arrayOffset();
            bOutput = engineUpdate(bInput, offset + position, limit - position);
            input.position(limit);
        } else {
            bInput = new byte[limit - position];
            input.get(bInput);
            bOutput = engineUpdate(bInput, 0, limit - position);
        }
        if (output.remaining() < bOutput.length) {
            throw new ShortBufferException(Messages.getString("crypto.0E")); 
        }
        try {
            output.put(bOutput);
        } catch (java.nio.BufferOverflowException e) {
            throw new ShortBufferException(Messages.getString("crypto.0F", e)); 
        }
        return bOutput.length;
    }
    protected abstract byte[] engineDoFinal(byte[] input, int inputOffset,
            int inputLen) throws IllegalBlockSizeException, BadPaddingException;
    protected abstract int engineDoFinal(byte[] input, int inputOffset,
            int inputLen, byte[] output, int outputOffset)
            throws ShortBufferException, IllegalBlockSizeException,
            BadPaddingException;
    protected int engineDoFinal(ByteBuffer input, ByteBuffer output)
            throws ShortBufferException, IllegalBlockSizeException,
            BadPaddingException {
        if (input == null) {
            throw new NullPointerException(Messages.getString("crypto.0C")); 
        }
        if (output == null) {
            throw new NullPointerException(Messages.getString("crypto.0D")); 
        }
        int position = input.position();
        int limit = input.limit();
        if ((limit - position) <= 0) {
            return 0;
        }
        byte[] bInput;
        byte[] bOutput;
        if (input.hasArray()) {
            bInput = input.array();
            int offset = input.arrayOffset();
            bOutput = engineDoFinal(bInput, offset + position, limit - position);
            input.position(limit);
        } else {
            bInput = new byte[limit - position];
            input.get(bInput);
            bOutput = engineDoFinal(bInput, 0, limit - position);
        }
        if (output.remaining() < bOutput.length) {
            throw new ShortBufferException(Messages.getString("crypto.0E")); 
        }
        try {
            output.put(bOutput);
        } catch (java.nio.BufferOverflowException e) {
            throw new ShortBufferException(Messages.getString("crypto.0F", e)); 
        }
        return bOutput.length;
    }
    protected byte[] engineWrap(Key key) throws IllegalBlockSizeException,
            InvalidKeyException {
        throw new UnsupportedOperationException(
                Messages.getString("crypto.10")); 
    }
    protected Key engineUnwrap(byte[] wrappedKey, String wrappedKeyAlgorithm,
            int wrappedKeyType) throws InvalidKeyException,
            NoSuchAlgorithmException {
        throw new UnsupportedOperationException(
                Messages.getString("crypto.11")); 
    }
    protected int engineGetKeySize(Key key) throws InvalidKeyException {
        throw new UnsupportedOperationException(
                Messages.getString("crypto.12")); 
    }
}