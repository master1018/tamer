public class RandomAccessFile implements DataInput, DataOutput, Closeable {
    private FileDescriptor fd;
    private boolean syncMetadata = false;
    private FileChannel channel;
    private IFileSystem fileSystem = Platform.getFileSystem();
    private boolean isReadOnly;
    private int options;
    private static class RepositionLock {
    }
    private Object repositionLock = new RepositionLock();
    public RandomAccessFile(File file, String mode)
            throws FileNotFoundException {
        super();
        options = 0;
        fd = new FileDescriptor();
        if (mode.equals("r")) { 
            isReadOnly = true;
            fd.readOnly = true;
            options = IFileSystem.O_RDONLY;
        } else if (mode.equals("rw") || mode.equals("rws") || mode.equals("rwd")) { 
            isReadOnly = false;
            options = IFileSystem.O_RDWR;
            if (mode.equals("rws")) { 
                syncMetadata = true;
            } else if (mode.equals("rwd")) { 
                options = IFileSystem.O_RDWRSYNC;
            }
        } else {
            throw new IllegalArgumentException(Msg.getString("K0081")); 
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(file.getPath());
            if (!isReadOnly) {
                security.checkWrite(file.getPath());
            }
        }
        fd.descriptor = fileSystem.open(file.pathBytes, options);
        if (syncMetadata) {
            try {
                fd.sync();
            } catch (IOException e) {
            }
        }
    }
    public RandomAccessFile(String fileName, String mode)
            throws FileNotFoundException {
        this(new File(fileName), mode);
    }
    public void close() throws IOException {
        synchronized (this) {
            if (channel != null && channel.isOpen()) {
                channel.close();
                channel = null;
            }
            if (fd != null && fd.descriptor >= 0) {
                fileSystem.close(fd.descriptor);
                fd.descriptor = -1;
            }
        }
    }
    public final synchronized FileChannel getChannel() {
        if(channel == null) {
            channel = FileChannelFactory.getFileChannel(this, fd.descriptor,
                    options);
        }
        return channel;
    }
    public final FileDescriptor getFD() throws IOException {
        return fd;
    }
    public long getFilePointer() throws IOException {
        openCheck();
        return fileSystem.seek(fd.descriptor, 0L, IFileSystem.SEEK_CUR);
    }
    private synchronized void openCheck() throws IOException {
        if (fd.descriptor < 0) {
            throw new IOException();
        }
    }
    public long length() throws IOException {
        openCheck();
        synchronized (repositionLock) {
            long currentPosition = fileSystem.seek(fd.descriptor, 0L,
                    IFileSystem.SEEK_CUR);
            long endOfFilePosition = fileSystem.seek(fd.descriptor, 0L,
                    IFileSystem.SEEK_END);
            fileSystem.seek(fd.descriptor, currentPosition,
                    IFileSystem.SEEK_SET);
            return endOfFilePosition;
        }
    }
    public int read() throws IOException {
        openCheck();
        byte[] bytes = new byte[1];
        synchronized (repositionLock) {
            long readed = fileSystem.read(fd.descriptor, bytes, 0, 1);
            return readed == -1 ? -1 : bytes[0] & 0xff;
        }
    }
    public int read(byte[] buffer) throws IOException {
        return read(buffer, 0, buffer.length);
    }
    public int read(byte[] buffer, int offset, int count) throws IOException {
        if (buffer == null) {
            throw new NullPointerException(Msg.getString("K0047")); 
        }
        if ((offset | count) < 0 || count > buffer.length - offset) {
            throw new IndexOutOfBoundsException(Msg.getString("K002f")); 
        }
        if (0 == count) {
            return 0;
        }
        openCheck();
        synchronized (repositionLock) {
            return (int) fileSystem.read(fd.descriptor, buffer, offset, count);
        }
    }
    public final boolean readBoolean() throws IOException {
        int temp = this.read();
        if (temp < 0) {
            throw new EOFException();
        }
        return temp != 0;
    }
    public final byte readByte() throws IOException {
        int temp = this.read();
        if (temp < 0) {
            throw new EOFException();
        }
        return (byte) temp;
    }
    public final char readChar() throws IOException {
        byte[] buffer = new byte[2];
        if (read(buffer, 0, buffer.length) != buffer.length) {
            throw new EOFException();
        }
        return (char) (((buffer[0] & 0xff) << 8) + (buffer[1] & 0xff));
    }
    public final double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }
    public final float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }
    public final void readFully(byte[] buffer) throws IOException {
        readFully(buffer, 0, buffer.length);
    }
    public final void readFully(byte[] buffer, int offset, int count)
            throws IOException {
        if (buffer == null) {
            throw new NullPointerException(Msg.getString("K0047")); 
        }
        if ((offset | count) < 0 || count > buffer.length - offset) {
            throw new IndexOutOfBoundsException(Msg.getString("K002f")); 
        }
        while (count > 0) {
            int result = read(buffer, offset, count);
            if (result < 0) {
                throw new EOFException();
            }
            offset += result;
            count -= result;
        }
    }
    public final int readInt() throws IOException {
        byte[] buffer = new byte[4];
        if (read(buffer, 0, buffer.length) != buffer.length) {
            throw new EOFException();
        }
        return ((buffer[0] & 0xff) << 24) + ((buffer[1] & 0xff) << 16)
                + ((buffer[2] & 0xff) << 8) + (buffer[3] & 0xff);
    }
    public final String readLine() throws IOException {
        StringBuilder line = new StringBuilder(80); 
        boolean foundTerminator = false;
        long unreadPosition = 0;
        while (true) {
            int nextByte = read();
            switch (nextByte) {
                case -1:
                    return line.length() != 0 ? line.toString() : null;
                case (byte) '\r':
                    if (foundTerminator) {
                        seek(unreadPosition);
                        return line.toString();
                    }
                    foundTerminator = true;
                    unreadPosition = getFilePointer();
                    break;
                case (byte) '\n':
                    return line.toString();
                default:
                    if (foundTerminator) {
                        seek(unreadPosition);
                        return line.toString();
                    }
                    line.append((char) nextByte);
            }
        }
    }
    public final long readLong() throws IOException {
        byte[] buffer = new byte[8];
        if (read(buffer, 0, buffer.length) != buffer.length) {
            throw new EOFException();
        }
        return ((long) (((buffer[0] & 0xff) << 24) + ((buffer[1] & 0xff) << 16)
                + ((buffer[2] & 0xff) << 8) + (buffer[3] & 0xff)) << 32)
                + ((long) (buffer[4] & 0xff) << 24)
                + ((buffer[5] & 0xff) << 16)
                + ((buffer[6] & 0xff) << 8)
                + (buffer[7] & 0xff);
    }
    public final short readShort() throws IOException {
        byte[] buffer = new byte[2];
        if (read(buffer, 0, buffer.length) != buffer.length) {
            throw new EOFException();
        }
        return (short) (((buffer[0] & 0xff) << 8) + (buffer[1] & 0xff));
    }
    public final int readUnsignedByte() throws IOException {
        int temp = this.read();
        if (temp < 0) {
            throw new EOFException();
        }
        return temp;
    }
    public final int readUnsignedShort() throws IOException {
        byte[] buffer = new byte[2];
        if (read(buffer, 0, buffer.length) != buffer.length) {
            throw new EOFException();
        }
        return ((buffer[0] & 0xff) << 8) + (buffer[1] & 0xff);
    }
    public final String readUTF() throws IOException {
        int utfSize = readUnsignedShort();
        if (utfSize == 0) {
            return ""; 
        }
        byte[] buf = new byte[utfSize];
        if (read(buf, 0, buf.length) != buf.length) {
            throw new EOFException();
        }
        return Util.convertFromUTF8(buf, 0, utfSize);
    }
    public void seek(long pos) throws IOException {
        if (pos < 0) {
            throw new IOException(Msg.getString("K0347")); 
        }
        openCheck();
        synchronized (repositionLock) {
            fileSystem.seek(fd.descriptor, pos, IFileSystem.SEEK_SET);
        }
    }
    public void setLength(long newLength) throws IOException {
        openCheck();
        if (newLength < 0) {
            throw new IllegalArgumentException();
        }
        synchronized (repositionLock) {
            long position = fileSystem.seek(fd.descriptor, 0,
                    IFileSystem.SEEK_CUR);
            fileSystem.truncate(fd.descriptor, newLength);
            seek(position > newLength ? newLength : position);
        }
        if (syncMetadata) {
            fd.sync();
        }
    }
    public int skipBytes(int count) throws IOException {
        if (count > 0) {
            long currentPos = getFilePointer(), eof = length();
            int newCount = (int) ((currentPos + count > eof) ? eof - currentPos
                    : count);
            seek(currentPos + newCount);
            return newCount;
        }
        return 0;
    }
    public void write(byte[] buffer) throws IOException {
        write(buffer, 0, buffer.length);
    }
    public void write(byte[] buffer, int offset, int count) throws IOException {
        if (buffer == null) {
            throw new NullPointerException(Msg.getString("K0047")); 
        }
        if ((offset | count) < 0 || count > buffer.length - offset) {
            throw new IndexOutOfBoundsException(Msg.getString("K002f")); 
        }
        if (count == 0) {
            return;
        }
        openCheck();
        synchronized (repositionLock) {
            fileSystem.write(fd.descriptor, buffer, offset, count);
        }
        if (syncMetadata) {
            fd.sync();
        }
    }
    public void write(int oneByte) throws IOException {
        openCheck();
        byte[] bytes = new byte[1];
        bytes[0] = (byte) (oneByte & 0xff);
        synchronized (repositionLock) {
            fileSystem.write(fd.descriptor, bytes, 0, 1);
        }
        if (syncMetadata) {
            fd.sync();
        }
    }
    public final void writeBoolean(boolean val) throws IOException {
        write(val ? 1 : 0);
    }
    public final void writeByte(int val) throws IOException {
        write(val & 0xFF);
    }
    public final void writeBytes(String str) throws IOException {
        byte bytes[] = new byte[str.length()];
        for (int index = 0; index < str.length(); index++) {
            bytes[index] = (byte) (str.charAt(index) & 0xFF);
        }
        write(bytes);
    }
    public final void writeChar(int val) throws IOException {
        byte[] buffer = new byte[2];
        buffer[0] = (byte) (val >> 8);
        buffer[1] = (byte) val;
        write(buffer, 0, buffer.length);
    }
    public final void writeChars(String str) throws IOException {
        byte newBytes[] = new byte[str.length() * 2];
        for (int index = 0; index < str.length(); index++) {
            int newIndex = index == 0 ? index : index * 2;
            newBytes[newIndex] = (byte) ((str.charAt(index) >> 8) & 0xFF);
            newBytes[newIndex + 1] = (byte) (str.charAt(index) & 0xFF);
        }
        write(newBytes);
    }
    public final void writeDouble(double val) throws IOException {
        writeLong(Double.doubleToLongBits(val));
    }
    public final void writeFloat(float val) throws IOException {
        writeInt(Float.floatToIntBits(val));
    }
    public final void writeInt(int val) throws IOException {
        byte[] buffer = new byte[4];
        buffer[0] = (byte) (val >> 24);
        buffer[1] = (byte) (val >> 16);
        buffer[2] = (byte) (val >> 8);
        buffer[3] = (byte) val;
        write(buffer, 0, buffer.length);
    }
    public final void writeLong(long val) throws IOException {
        byte[] buffer = new byte[8];
        int t = (int) (val >> 32);
        buffer[0] = (byte) (t >> 24);
        buffer[1] = (byte) (t >> 16);
        buffer[2] = (byte) (t >> 8);
        buffer[3] = (byte) t;
        buffer[4] = (byte) (val >> 24);
        buffer[5] = (byte) (val >> 16);
        buffer[6] = (byte) (val >> 8);
        buffer[7] = (byte) val;
        write(buffer, 0, buffer.length);
    }
    public final void writeShort(int val) throws IOException {
        writeChar(val);
    }
    public final void writeUTF(String str) throws IOException {
        int utfCount = 0, length = str.length();
        for (int i = 0; i < length; i++) {
            int charValue = str.charAt(i);
            if (charValue > 0 && charValue <= 127) {
                utfCount++;
            } else if (charValue <= 2047) {
                utfCount += 2;
            } else {
                utfCount += 3;
            }
        }
        if (utfCount > 65535) {
            throw new UTFDataFormatException(Msg.getString("K0068")); 
        }
        byte utfBytes[] = new byte[utfCount + 2];
        int utfIndex = 2;
        for (int i = 0; i < length; i++) {
            int charValue = str.charAt(i);
            if (charValue > 0 && charValue <= 127) {
                utfBytes[utfIndex++] = (byte) charValue;
            } else if (charValue <= 2047) {
                utfBytes[utfIndex++] = (byte) (0xc0 | (0x1f & (charValue >> 6)));
                utfBytes[utfIndex++] = (byte) (0x80 | (0x3f & charValue));
            } else {
                utfBytes[utfIndex++] = (byte) (0xe0 | (0x0f & (charValue >> 12)));
                utfBytes[utfIndex++] = (byte) (0x80 | (0x3f & (charValue >> 6)));
                utfBytes[utfIndex++] = (byte) (0x80 | (0x3f & charValue));
            }
        }
        utfBytes[0] = (byte) (utfCount >> 8);
        utfBytes[1] = (byte) utfCount;
        write(utfBytes);
    }
}
