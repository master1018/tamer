public final class Channels {
    private Channels() {
        super();
    }
    public static InputStream newInputStream(ReadableByteChannel channel) {
        return new ReadableByteChannelInputStream(channel);
    }
    public static OutputStream newOutputStream(WritableByteChannel channel) {
        return new WritableByteChannelOutputStream(channel);
    }
    public static ReadableByteChannel newChannel(InputStream inputStream) {
        return new ReadableByteChannelImpl(inputStream);
    }
    public static WritableByteChannel newChannel(OutputStream outputStream) {
        return new WritableByteChannelImpl(outputStream);
    }
    public static Reader newReader(ReadableByteChannel channel,
            CharsetDecoder decoder, int minBufferCapacity) {
        return new ByteChannelReader(new ReaderInputStream(channel), decoder,
                minBufferCapacity);
    }
    public static Reader newReader(ReadableByteChannel channel,
            String charsetName) {
        return newReader(channel, Charset.forName(charsetName).newDecoder(), -1);
    }
    public static Writer newWriter(WritableByteChannel channel,
            CharsetEncoder encoder, int minBufferCapacity) {
        return new ByteChannelWriter(new WritableByteChannelOutputStream(
                channel), encoder, minBufferCapacity);
    }
    public static Writer newWriter(WritableByteChannel channel,
            String charsetName) {
        return newWriter(channel, Charset.forName(charsetName).newEncoder(), -1);
    }
    static ByteBuffer wrapByteBuffer(byte[] bytes, int offset, int length) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        int newLimit = offset + length <= buffer.capacity() ? offset + length
                : buffer.capacity();
        buffer.limit(newLimit);
        buffer.position(offset);
        return buffer;
    }
    private static class ChannelInputStream extends InputStream {
        protected ReadableByteChannel channel;
        public ChannelInputStream(ReadableByteChannel aChannel) {
            super();
            channel = aChannel;
        }
        @Override
        public synchronized int read() throws IOException {
            byte[] oneByte = new byte[1];
            int n = read(oneByte);
            if (n == 1) {
                return oneByte[0] & 0xff;
            }
            return -1;
        }
        @Override
        public synchronized void close() throws IOException {
            channel.close();
        }
    }
    private static class ReadableByteChannelInputStream extends
            ChannelInputStream {
        public ReadableByteChannelInputStream(ReadableByteChannel aChannel) {
            super(aChannel);
        }
        @Override
        public synchronized int read(byte[] target, int offset, int length)
                throws IOException {
            if (length + offset > target.length || length < 0 || offset < 0) {
                throw new ArrayIndexOutOfBoundsException();
            }
            if (0 == length) {
                return 0;
            }
            if (channel instanceof SelectableChannel) {
                if (!((SelectableChannel) channel).isBlocking()) {
                    throw new IllegalBlockingModeException();
                }
            }
            ByteBuffer buffer = ByteBuffer.wrap(target, offset, length);
            return channel.read(buffer);
        }
    }
    private static class ReaderInputStream extends ChannelInputStream {
        public ReaderInputStream(ReadableByteChannel aChannel) {
            super(aChannel);
        }
        @Override
        public synchronized int read(byte[] target, int offset, int length)
                throws IOException {
            if (length + offset > target.length || length < 0 || offset < 0) {
                throw new ArrayIndexOutOfBoundsException();
            }
            if (0 == length) {
                return 0;
            }
            ByteBuffer buffer = ByteBuffer.wrap(target, offset, length);
            return channel.read(buffer);
        }
    }
    private static class WritableByteChannelOutputStream extends OutputStream {
        private WritableByteChannel channel;
        public WritableByteChannelOutputStream(WritableByteChannel aChannel) {
            super();
            channel = aChannel;
        }
        @Override
        public synchronized void write(int oneByte) throws IOException {
            byte[] wrappedByte = new byte[1];
            wrappedByte[0] = (byte) oneByte;
            write(wrappedByte);
        }
        @Override
        public synchronized void write(byte[] source, int offset, int length)
                throws IOException {
            if (length + offset > source.length || length < 0 || offset < 0) {
                throw new ArrayIndexOutOfBoundsException();
            }
            if (0 == length) {
                return;
            }
            if (channel instanceof SelectableChannel) {
                if (!((SelectableChannel) channel).isBlocking()) {
                    throw new IllegalBlockingModeException();
                }
            }
            ByteBuffer buffer = ByteBuffer.wrap(source, offset, length);
            channel.write(buffer);
        }
        @Override
        public synchronized void close() throws IOException {
            channel.close();
        }
    }
    private static class ReadableByteChannelImpl extends
            AbstractInterruptibleChannel implements ReadableByteChannel {
        private InputStream inputStream;
        ReadableByteChannelImpl(InputStream aInputStream) {
            super();
            inputStream = aInputStream;
        }
        public synchronized int read(ByteBuffer target) throws IOException {
            if (!isOpen()) {
                throw new ClosedChannelException();
            }
            int bytesRemain = target.remaining();
            byte[] bytes = new byte[bytesRemain];
            int readCount = 0;
            try {
                begin();
                readCount = inputStream.read(bytes);
            } finally {
                end(readCount >= 0);
            }
            if (readCount > 0) {
                target.put(bytes, 0, readCount);
            }
            return readCount;
        }
        @Override
        protected void implCloseChannel() throws IOException {
            inputStream.close();
        }
    }
    private static class WritableByteChannelImpl extends
            AbstractInterruptibleChannel implements WritableByteChannel {
        private OutputStream outputStream;
        WritableByteChannelImpl(OutputStream aOutputStream) {
            super();
            outputStream = aOutputStream;
        }
        public synchronized int write(ByteBuffer source) throws IOException {
            if (!isOpen()) {
                throw new ClosedChannelException();
            }
            int bytesRemain = source.remaining();
            if (bytesRemain == 0) {
                return 0;
            }
            byte[] buf = new byte[bytesRemain];
            source.get(buf);
            try {
                begin();
                outputStream.write(buf, 0, bytesRemain);
            } finally {
                end(bytesRemain >= 0);
            }
            return bytesRemain;
        }
        @Override
        protected void implCloseChannel() throws IOException {
            outputStream.close();
        }
    }
    private static class ByteChannelReader extends Reader {
        private InputStream inputStream;
        private static final int BUFFER_SIZE = 8192;
        CharsetDecoder decoder;
        ByteBuffer bytes;
        CharBuffer chars;
        public ByteChannelReader(InputStream aInputStream,
                CharsetDecoder aDecoder, int minBufferCapacity) {
            super(aInputStream);
            aDecoder.reset();
            inputStream = aInputStream;
            int bufferSize = Math.max(minBufferCapacity, BUFFER_SIZE);
            bytes = ByteBuffer.allocate(bufferSize);
            chars = CharBuffer.allocate(bufferSize);
            decoder = aDecoder;
            chars.limit(0);
        }
        @Override
        public void close() throws IOException {
            synchronized (lock) {
                decoder = null;
                if (inputStream != null) {
                    inputStream.close();
                    inputStream = null;
                }
            }
        }
        @Override
        public boolean ready() {
            synchronized (lock) {
                if (null == inputStream) {
                    return false;
                }
                try {
                    return chars.limit() > chars.position()
                            || inputStream.available() > 0;
                } catch (IOException e) {
                    return false;
                }
            }
        }
        @Override
        public int read() throws IOException {
            return IOUtil.readInputStreamReader(inputStream, bytes, chars,
                    decoder, lock);
        }
        @Override
        public int read(char[] buf, int offset, int length) throws IOException {
            return IOUtil.readInputStreamReader(buf, offset, length,
                    inputStream, bytes, chars, decoder, lock);
        }
    }
    private static class ByteChannelWriter extends Writer {
        private static final int BUFFER_SIZE = 8192;
        private OutputStream outputStream;
        private CharsetEncoder encoder;
        private ByteBuffer byteBuf;
        public ByteChannelWriter(OutputStream aOutputStream,
                CharsetEncoder aEncoder, int minBufferCap) {
            super(aOutputStream);
            aEncoder.charset();
            outputStream = aOutputStream;
            byteBuf = ByteBuffer.allocate(Math.max(minBufferCap, BUFFER_SIZE));
            encoder = aEncoder;
        }
        @Override
        public void close() throws IOException {
            synchronized (lock) {
                if (encoder != null) {
                    flush();
                    outputStream.flush();
                    outputStream.close();
                    encoder = null;
                    byteBuf = null;
                }
            }
        }
        @Override
        public void flush() throws IOException {
            IOUtil
                    .flushOutputStreamWriter(outputStream, byteBuf, encoder,
                            lock);
        }
        @Override
        public void write(char[] buf, int offset, int count) throws IOException {
            IOUtil.writeOutputStreamWriter(buf, offset, count, outputStream,
                    byteBuf, encoder, lock);
        }
        @Override
        public void write(int oneChar) throws IOException {
            IOUtil.writeOutputStreamWriter(oneChar, outputStream, byteBuf,
                    encoder, lock);
        }
        @Override
        public void write(String str, int offset, int count) throws IOException {
            IOUtil.writeOutputStreamWriter(str, offset, count, outputStream,
                    byteBuf, encoder, lock);
        }
    }
}
