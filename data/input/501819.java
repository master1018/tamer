public class SequenceInputStream extends InputStream {
    private Enumeration<? extends InputStream> e;
    private InputStream in;
    public SequenceInputStream(InputStream s1, InputStream s2) {
        if (s1 == null) {
            throw new NullPointerException();
        }
        Vector<InputStream> inVector = new Vector<InputStream>(1);
        inVector.addElement(s2);
        e = inVector.elements();
        in = s1;
    }
    public SequenceInputStream(Enumeration<? extends InputStream> e) {
        this.e = e;
        if (e.hasMoreElements()) {
            in = e.nextElement();
            if (in == null) {
                throw new NullPointerException();
            }
        }
    }
    @Override
    public int available() throws IOException {
        if (e != null && in != null) {
            return in.available();
        }
        return 0;
    }
    @Override
    public void close() throws IOException {
        while (in != null) {
            nextStream();
        }
        e = null;
    }
    private void nextStream() throws IOException {
        if (in != null) {
            in.close();
        }
        if (e.hasMoreElements()) {
            in = e.nextElement();
            if (in == null) {
                throw new NullPointerException();
            }
        } else {
            in = null;
        }
    }
    @Override
    public int read() throws IOException {
        while (in != null) {
            int result = in.read();
            if (result >= 0) {
                return result;
            }
            nextStream();
        }
        return -1;
    }
    @Override
    public int read(byte[] buffer, int offset, int count) throws IOException {
        if (in == null) {
            return -1;
        }
        if (buffer == null) {
            throw new NullPointerException(Msg.getString("K0047")); 
        }
        if ((offset | count) < 0 || offset > buffer.length - count) {
            throw new IndexOutOfBoundsException(Msg.getString("K002f")); 
        }
        while (in != null) {
            int result = in.read(buffer, offset, count);
            if (result >= 0) {
                return result;
            }
            nextStream();
        }
        return -1;
    }
}
