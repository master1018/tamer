public class ByteBuffersNull {
    static final int bufSize = 1024;
    public void testCase010() throws Exception {
        CipherSpiImpl c = new CipherSpiImpl();
        BufferDescr[] inBuffers = getByteBuffersForTest(bufSize);
        int failureCount = 0;
        for (int i = 0; i < inBuffers.length; i++) {
            String key = inBuffers[i].descr;
            ByteBuffer bb = inBuffers[i].buf;
            try {
                c.engineUpdate(bb, null);
                throw new Exception("No Exception?!");
            }  catch (NullPointerException npe) {
                System.out.println("OK: " + npe);
            }
        }
    }
    ByteBuffer getByteBuffer(int capacity, int position,
            boolean limitAt0) {
        ByteBuffer bb = ByteBuffer.allocate(capacity);
        bb.position(position);
        if (limitAt0)
            bb.limit(0);
        return bb;
    }
    BufferDescr[] getByteBuffersForTest(int defaultSize) {
        int caseNum = 4;
        BufferDescr[] buffers = new BufferDescr[caseNum];
        buffers[0]= new BufferDescr("ByteBuffer with capacity == 0",
            getByteBuffer(0,0,false));
        buffers[1] = new BufferDescr(
            "ByteBuffer with some capacity but limit == 0",
            getByteBuffer(defaultSize, 0, true));
        buffers[2] = new BufferDescr("ByteBuffer with some data",
            getByteBuffer(defaultSize,0,false));
        buffers[3] = new BufferDescr(
            "ByteBuffer with data but position at the limit",
            getByteBuffer(defaultSize, defaultSize,false));
        return buffers;
    }
    public static void main(String argv[]) throws Exception {
        ByteBuffersNull test = new ByteBuffersNull();
        test.testCase010();
    }
    class BufferDescr {
        BufferDescr(String d, ByteBuffer b) {
            descr = d;
            buf = b;
        }
        public String descr;
        public ByteBuffer buf;
    }
    public class CipherSpiImpl extends CipherSpi {
        public CipherSpiImpl() {
            super();
        }
        public void engineSetMode(String mode)
            throws NoSuchAlgorithmException { }
        public void engineSetPadding(String padding)
            throws NoSuchPaddingException { }
        public int engineGetBlockSize() {
            return 0;
        }
        public int engineGetOutputSize(int inputLen) {
            return 0;
        }
        public byte[] engineGetIV() {
            return null;
        }
        public AlgorithmParameters engineGetParameters() {
            return null;
        }
        public void engineInit(int opmode, Key key, SecureRandom random)
            throws InvalidKeyException { }
        public void engineInit(int opmode, Key key,
                               AlgorithmParameterSpec params, SecureRandom random)
            throws InvalidKeyException, InvalidAlgorithmParameterException { }
        public void engineInit(int opmode, Key key, AlgorithmParameters params,
                               SecureRandom random)
            throws InvalidKeyException, InvalidAlgorithmParameterException { }
        public byte[] engineUpdate(byte[] input, int offset, int len) {
            return null;
        }
        public int engineUpdate(byte[] input, int inputOffset, int inputLen,
                                byte[] output, int outputOffset)
            throws ShortBufferException {
            return 0;
        }
        public byte[] engineDoFinal(byte[] input, int inputOffset, int inputLen)
            throws IllegalBlockSizeException, BadPaddingException {
            return null;
        }
        public int engineDoFinal(byte[] input, int inputOffset, int inputLen,
                                 byte[] output, int outputOffset)
            throws ShortBufferException, IllegalBlockSizeException,
            BadPaddingException {
            return 0;
        }
        public byte[] engineWrap(Key key)
            throws IllegalBlockSizeException, InvalidKeyException {
            return super.engineWrap(key);
        }
        public Key engineUnwrap(byte[] wKey, String wKeyAlgorithm,
                                int wKeyType) throws InvalidKeyException,
            NoSuchAlgorithmException {
            return super.engineUnwrap(wKey, wKeyAlgorithm, wKeyType);
        }
        public int engineGetKeySize(Key key) throws InvalidKeyException {
            return super.engineGetKeySize(key);
        }
        public int engineDoFinal(ByteBuffer input, ByteBuffer output)
            throws ShortBufferException, IllegalBlockSizeException,
            BadPaddingException {
            return super.engineDoFinal(input, output);
        }
        public int engineUpdate(ByteBuffer input, ByteBuffer output)
            throws ShortBufferException {
            return super.engineUpdate(input, output);
        }
    }
}
