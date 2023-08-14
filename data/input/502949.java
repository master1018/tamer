public class ETC1Util {
    public static void loadTexture(int target, int level, int border,
            int fallbackFormat, int fallbackType, InputStream input)
        throws IOException {
        loadTexture(target, level, border, fallbackFormat, fallbackType, createTexture(input));
    }
    public static void loadTexture(int target, int level, int border,
            int fallbackFormat, int fallbackType, ETC1Texture texture) {
        if (fallbackFormat != GLES10.GL_RGB) {
            throw new IllegalArgumentException("fallbackFormat must be GL_RGB");
        }
        if (! (fallbackType == GLES10.GL_UNSIGNED_SHORT_5_6_5
                || fallbackType == GLES10.GL_UNSIGNED_BYTE)) {
            throw new IllegalArgumentException("Unsupported fallbackType");
        }
        int width = texture.getWidth();
        int height = texture.getHeight();
        Buffer data = texture.getData();
        if (isETC1Supported()) {
            int imageSize = data.remaining();
            GLES10.glCompressedTexImage2D(target, level, ETC1.ETC1_RGB8_OES, width, height,
                    border, imageSize, data);
        } else {
            boolean useShorts = fallbackType != GLES10.GL_UNSIGNED_BYTE;
            int pixelSize = useShorts ? 2 : 3;
            int stride = pixelSize * width;
            ByteBuffer decodedData = ByteBuffer.allocateDirect(stride*height)
                .order(ByteOrder.nativeOrder());
            ETC1.decodeImage(data, decodedData, width, height, pixelSize, stride);
            GLES10.glTexImage2D(target, level, fallbackFormat, width, height, border,
                    fallbackFormat, fallbackType, decodedData);
        }
    }
    public static boolean isETC1Supported() {
        int[] results = new int[20];
        GLES10.glGetIntegerv(GLES10.GL_NUM_COMPRESSED_TEXTURE_FORMATS, results, 0);
        int numFormats = results[0];
        if (numFormats > results.length) {
            results = new int[numFormats];
        }
        GLES10.glGetIntegerv(GLES10.GL_COMPRESSED_TEXTURE_FORMATS, results, 0);
        for (int i = 0; i < numFormats; i++) {
            if (results[i] == ETC1.ETC1_RGB8_OES) {
                return true;
            }
        }
        return false;
    }
    public static class ETC1Texture {
        public ETC1Texture(int width, int height, ByteBuffer data) {
            mWidth = width;
            mHeight = height;
            mData = data;
        }
        public int getWidth() { return mWidth; }
        public int getHeight() { return mHeight; }
        public ByteBuffer getData() { return mData; }
        private int mWidth;
        private int mHeight;
        private ByteBuffer mData;
    }
    public static ETC1Texture createTexture(InputStream input) throws IOException {
        int width = 0;
        int height = 0;
        byte[] ioBuffer = new byte[4096];
        {
            if (input.read(ioBuffer, 0, ETC1.ETC_PKM_HEADER_SIZE) != ETC1.ETC_PKM_HEADER_SIZE) {
                throw new IOException("Unable to read PKM file header.");
            }
            ByteBuffer headerBuffer = ByteBuffer.allocateDirect(ETC1.ETC_PKM_HEADER_SIZE)
                .order(ByteOrder.nativeOrder());
            headerBuffer.put(ioBuffer, 0, ETC1.ETC_PKM_HEADER_SIZE).position(0);
            if (!ETC1.isValid(headerBuffer)) {
                throw new IOException("Not a PKM file.");
            }
            width = ETC1.getWidth(headerBuffer);
            height = ETC1.getHeight(headerBuffer);
        }
        int encodedSize = ETC1.getEncodedDataSize(width, height);
        ByteBuffer dataBuffer = ByteBuffer.allocateDirect(encodedSize).order(ByteOrder.nativeOrder());
        for (int i = 0; i < encodedSize; ) {
            int chunkSize = Math.min(ioBuffer.length, encodedSize - i);
            if (input.read(ioBuffer, 0, chunkSize) != chunkSize) {
                throw new IOException("Unable to read PKM file data.");
            }
            dataBuffer.put(ioBuffer, 0, chunkSize);
            i += chunkSize;
        }
        dataBuffer.position(0);
        return new ETC1Texture(width, height, dataBuffer);
    }
    public static ETC1Texture compressTexture(Buffer input, int width, int height, int pixelSize, int stride){
        int encodedImageSize = ETC1.getEncodedDataSize(width, height);
        ByteBuffer compressedImage = ByteBuffer.allocateDirect(encodedImageSize).
            order(ByteOrder.nativeOrder());
        ETC1.encodeImage(input, width, height, 3, stride, compressedImage);
        return new ETC1Texture(width, height, compressedImage);
    }
    public static void writeTexture(ETC1Texture texture, OutputStream output) throws IOException {
        ByteBuffer dataBuffer = texture.getData();
        int originalPosition = dataBuffer.position();
        try {
            int width = texture.getWidth();
            int height = texture.getHeight();
            ByteBuffer header = ByteBuffer.allocateDirect(ETC1.ETC_PKM_HEADER_SIZE).order(ByteOrder.nativeOrder());
            ETC1.formatHeader(header, width, height);
            byte[] ioBuffer = new byte[4096];
            header.get(ioBuffer, 0, ETC1.ETC_PKM_HEADER_SIZE);
            output.write(ioBuffer, 0, ETC1.ETC_PKM_HEADER_SIZE);
            int encodedSize = ETC1.getEncodedDataSize(width, height);
            for (int i = 0; i < encodedSize; ) {
                int chunkSize = Math.min(ioBuffer.length, encodedSize - i);
                dataBuffer.get(ioBuffer, 0, chunkSize);
                output.write(ioBuffer, 0, chunkSize);
                i += chunkSize;
            }
        } finally {
            dataBuffer.position(originalPosition);
        }
    }
}
