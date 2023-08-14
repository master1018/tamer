final class MultiplexOutputStream extends OutputStream {
    private ConnectionMultiplexer manager;
    private MultiplexConnectionInfo info;
    private byte buffer[];
    private int pos = 0;
    private int requested = 0;
    private boolean disconnected = false;
    private Object lock = new Object();
    MultiplexOutputStream(
        ConnectionMultiplexer    manager,
        MultiplexConnectionInfo  info,
        int                      bufferLength)
    {
        this.manager = manager;
        this.info    = info;
        buffer = new byte[bufferLength];
        pos = 0;
    }
    public synchronized void write(int b) throws IOException
    {
        while (pos >= buffer.length)
            push();
        buffer[pos ++] = (byte) b;
    }
    public synchronized void write(byte b[], int off, int len)
        throws IOException
    {
        if (len <= 0)
            return;
        int freeSpace = buffer.length - pos;
        if (len <= freeSpace) {
            System.arraycopy(b, off, buffer, pos, len);
            pos += len;
            return;
        }
        flush();
        int local_requested;
        while (true) {
            synchronized (lock) {
                while ((local_requested = requested) < 1 && !disconnected) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                    }
                }
                if (disconnected)
                    throw new IOException("Connection closed");
            }
            if (local_requested < len) {
                manager.sendTransmit(info, b, off, local_requested);
                off += local_requested;
                len -= local_requested;
                synchronized (lock) {
                    requested -= local_requested;
                }
            }
            else {
                manager.sendTransmit(info, b, off, len);
                synchronized (lock) {
                    requested -= len;
                }
                break;
            }
        }
    }
    public synchronized void flush() throws IOException {
        while (pos > 0)
            push();
    }
    public void close() throws IOException
    {
        manager.sendClose(info);
    }
    void request(int num)
    {
        synchronized (lock) {
            requested += num;
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
    private void push() throws IOException
    {
        int local_requested;
        synchronized (lock) {
            while ((local_requested = requested) < 1 && !disconnected) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                }
            }
            if (disconnected)
                throw new IOException("Connection closed");
        }
        if (local_requested < pos) {
            manager.sendTransmit(info, buffer, 0, local_requested);
            System.arraycopy(buffer, local_requested,
                             buffer, 0, pos - local_requested);
            pos -= local_requested;
            synchronized (lock) {
                requested -= local_requested;
            }
        }
        else {
            manager.sendTransmit(info, buffer, 0, pos);
            synchronized (lock) {
                requested -= pos;
            }
            pos = 0;
        }
    }
}
