public abstract class CipherSpi {
    protected abstract void engineSetMode(String mode)
        throws NoSuchAlgorithmException;
    protected abstract void engineSetPadding(String padding)
        throws NoSuchPaddingException;
    protected abstract int engineGetBlockSize();
    protected abstract int engineGetOutputSize(int inputLen);
    protected abstract byte[] engineGetIV();
    protected abstract AlgorithmParameters engineGetParameters();
    protected abstract void engineInit(int opmode, Key key,
                                       SecureRandom random)
        throws InvalidKeyException;
    protected abstract void engineInit(int opmode, Key key,
                                       AlgorithmParameterSpec params,
                                       SecureRandom random)
        throws InvalidKeyException, InvalidAlgorithmParameterException;
    protected abstract void engineInit(int opmode, Key key,
                                       AlgorithmParameters params,
                                       SecureRandom random)
        throws InvalidKeyException, InvalidAlgorithmParameterException;
    protected abstract byte[] engineUpdate(byte[] input, int inputOffset,
                                           int inputLen);
    protected abstract int engineUpdate(byte[] input, int inputOffset,
                                        int inputLen, byte[] output,
                                        int outputOffset)
        throws ShortBufferException;
    protected int engineUpdate(ByteBuffer input, ByteBuffer output)
            throws ShortBufferException {
        try {
            return bufferCrypt(input, output, true);
        } catch (IllegalBlockSizeException e) {
            throw new ProviderException("Internal error in update()");
        } catch (BadPaddingException e) {
            throw new ProviderException("Internal error in update()");
        }
    }
    protected abstract byte[] engineDoFinal(byte[] input, int inputOffset,
                                            int inputLen)
        throws IllegalBlockSizeException, BadPaddingException;
    protected abstract int engineDoFinal(byte[] input, int inputOffset,
                                         int inputLen, byte[] output,
                                         int outputOffset)
        throws ShortBufferException, IllegalBlockSizeException,
               BadPaddingException;
    protected int engineDoFinal(ByteBuffer input, ByteBuffer output)
            throws ShortBufferException, IllegalBlockSizeException,
            BadPaddingException {
        return bufferCrypt(input, output, false);
    }
    static int getTempArraySize(int totalSize) {
        return Math.min(4096, totalSize);
    }
    private int bufferCrypt(ByteBuffer input, ByteBuffer output,
            boolean isUpdate) throws ShortBufferException,
            IllegalBlockSizeException, BadPaddingException {
        if ((input == null) || (output == null)) {
            throw new NullPointerException
                ("Input and output buffers must not be null");
        }
        int inPos = input.position();
        int inLimit = input.limit();
        int inLen = inLimit - inPos;
        if (isUpdate && (inLen == 0)) {
            return 0;
        }
        int outLenNeeded = engineGetOutputSize(inLen);
        if (output.remaining() < outLenNeeded) {
            throw new ShortBufferException("Need at least " + outLenNeeded
                + " bytes of space in output buffer");
        }
        boolean a1 = input.hasArray();
        boolean a2 = output.hasArray();
        if (a1 && a2) {
            byte[] inArray = input.array();
            int inOfs = input.arrayOffset() + inPos;
            byte[] outArray = output.array();
            int outPos = output.position();
            int outOfs = output.arrayOffset() + outPos;
            int n;
            if (isUpdate) {
                n = engineUpdate(inArray, inOfs, inLen, outArray, outOfs);
            } else {
                n = engineDoFinal(inArray, inOfs, inLen, outArray, outOfs);
            }
            input.position(inLimit);
            output.position(outPos + n);
            return n;
        } else if (!a1 && a2) {
            int outPos = output.position();
            byte[] outArray = output.array();
            int outOfs = output.arrayOffset() + outPos;
            byte[] inArray = new byte[getTempArraySize(inLen)];
            int total = 0;
            while (inLen > 0) {
                int chunk = Math.min(inLen, inArray.length);
                input.get(inArray, 0, chunk);
                int n;
                if (isUpdate || (inLen != chunk)) {
                    n = engineUpdate(inArray, 0, chunk, outArray, outOfs);
                } else {
                    n = engineDoFinal(inArray, 0, chunk, outArray, outOfs);
                }
                total += n;
                outOfs += n;
                inLen -= chunk;
            }
            output.position(outPos + total);
            return total;
        } else { 
            byte[] inArray;
            int inOfs;
            if (a1) {
                inArray = input.array();
                inOfs = input.arrayOffset() + inPos;
            } else {
                inArray = new byte[getTempArraySize(inLen)];
                inOfs = 0;
            }
            byte[] outArray = new byte[getTempArraySize(outLenNeeded)];
            int outSize = outArray.length;
            int total = 0;
            boolean resized = false;
            while (inLen > 0) {
                int chunk = Math.min(inLen, outSize);
                if ((a1 == false) && (resized == false)) {
                    input.get(inArray, 0, chunk);
                    inOfs = 0;
                }
                try {
                    int n;
                    if (isUpdate || (inLen != chunk)) {
                        n = engineUpdate(inArray, inOfs, chunk, outArray, 0);
                    } else {
                        n = engineDoFinal(inArray, inOfs, chunk, outArray, 0);
                    }
                    resized = false;
                    inOfs += chunk;
                    inLen -= chunk;
                    output.put(outArray, 0, n);
                    total += n;
                } catch (ShortBufferException e) {
                    if (resized) {
                        throw (ProviderException)new ProviderException
                            ("Could not determine buffer size").initCause(e);
                    }
                    resized = true;
                    int newOut = engineGetOutputSize(chunk);
                    outArray = new byte[newOut];
                }
            }
            input.position(inLimit);
            return total;
        }
    }
    protected byte[] engineWrap(Key key)
        throws IllegalBlockSizeException, InvalidKeyException
    {
        throw new UnsupportedOperationException();
    }
    protected Key engineUnwrap(byte[] wrappedKey,
                               String wrappedKeyAlgorithm,
                               int wrappedKeyType)
        throws InvalidKeyException, NoSuchAlgorithmException
    {
        throw new UnsupportedOperationException();
    }
    protected int engineGetKeySize(Key key)
        throws InvalidKeyException
    {
        throw new UnsupportedOperationException();
    }
    protected void engineUpdateAAD(byte[] src, int offset, int len) {
        throw new UnsupportedOperationException(
            "The underlying Cipher implementation "
            +  "does not support this method");
    }
    protected void engineUpdateAAD(ByteBuffer src) {
        throw new UnsupportedOperationException(
            "The underlying Cipher implementation "
            +  "does not support this method");
    }
}
