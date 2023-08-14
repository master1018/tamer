final class MultiplexInputStream extends InputStream {
    private ConnectionMultiplexer manager;
    private MultiplexConnectionInfo info;
    private byte buffer[];
    private int present = 0;
    private int pos = 0;
    private int requested = 0;
    private boolean disconnected = false;
    private Object lock = new Object();
    private int waterMark;
    private byte temp[] = new byte[1];
    MultiplexInputStream(
        ConnectionMultiplexer    manager,
        MultiplexConnectionInfo  info,
        int                      bufferLength)
    {
        this.manager = manager;
        this.info    = info;
        buffer = new byte[bufferLength];
        waterMark = bufferLength / 2;
    }
    public synchronized int read() throws IOException
    {
        int n = read(temp, 0, 1);
        if (n != 1)
            return -1;
        return temp[0] & 0xFF;
    }
    public synchronized int read(byte b[], int off, int len) throws IOException
    {
        if (len <= 0)
            return 0;
        int moreSpace;
        synchronized (lock) {
            if (pos >= present)
                pos = present = 0;
            else if (pos >= waterMark) {
                System.arraycopy(buffer, pos, buffer, 0, present - pos);
                present -= pos;
                pos = 0;
            }
            int freeSpace = buffer.length - present;
            moreSpace = Math.max(freeSpace - requested, 0);
        }
        if (moreSpace > 0)
            manager.sendRequest(info, moreSpace);
        synchronized (lock) {
            requested += moreSpace;
            while ((pos >= present) && !disconnected) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                }
            }
            if (disconnected && pos >= present)
                return -1;
            int available = present - pos;
            if (len < available) {
                System.arraycopy(buffer, pos, b, off, len);
                pos += len;
                return len;
            }
            else {
                System.arraycopy(buffer, pos, b, off, available);
                pos = present = 0;
                return available;
            }
        }
    }
    public int available() throws IOException
    {
        synchronized (lock) {
            return present - pos;
        }
    }
    public void close() throws IOException
    {
        manager.sendClose(info);
    }
    void receive(int length, DataInputStream in)
        throws IOException
    {
        synchronized (lock) {
            if ((pos > 0) && ((buffer.length - present) < length)) {
                System.arraycopy(buffer, pos, buffer, 0, present - pos);
                present -= pos;
                pos = 0;
            }
            if ((buffer.length - present) < length)
                throw new IOException("Receive buffer overflow");
            in.readFully(buffer, present, length);
            present += length;
            requested -= length;
            lock.notifyAll();
        }
    }
    void disconnect()
    {
        synchronized (lock) {
            disconnected = true;
            lock.notifyAll();
        }
    }
}
