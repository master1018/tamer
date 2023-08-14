class FileReadBuffer implements ReadBuffer {
    private RandomAccessFile file;
    FileReadBuffer(RandomAccessFile file) {
        this.file = file;
    }
    private void seek(long pos) throws IOException {
        file.getChannel().position(pos);
    }
    public synchronized void get(long pos, byte[] buf) throws IOException {
        seek(pos);
        file.read(buf);
    }
    public synchronized char getChar(long pos) throws IOException {
        seek(pos);
        return file.readChar();
    }
    public synchronized byte getByte(long pos) throws IOException {
        seek(pos);
        return (byte) file.read();
    }
    public synchronized short getShort(long pos) throws IOException {
        seek(pos);
        return file.readShort();
    }
    public synchronized int getInt(long pos) throws IOException {
        seek(pos);
        return file.readInt();
    }
    public synchronized long getLong(long pos) throws IOException {
        seek(pos);
        return file.readLong();
    }
}
