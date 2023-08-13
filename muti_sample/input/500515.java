public class BufferedReader extends Reader {
    private Reader in;
    private char[] buf;
    private int pos;
    private int end;
    private int mark = -1;
    private int markLimit = -1;
    public BufferedReader(Reader in) {
        super(in);
        this.in = in;
        buf = new char[8192];
        Logger.global.info(
                "Default buffer size used in BufferedReader " +
                "constructor. It would be " +
                "better to be explicit if an 8k-char buffer is required.");
    }
    public BufferedReader(Reader in, int size) {
        super(in);
        if (size <= 0) {
            throw new IllegalArgumentException(Msg.getString("K0058")); 
        }
        this.in = in;
        buf = new char[size];
    }
    @Override
    public void close() throws IOException {
        synchronized (lock) {
            if (!isClosed()) {
                in.close();
                buf = null;
            }
        }
    }
    private int fillBuf() throws IOException {
        if (mark == -1 || (pos - mark >= markLimit)) {
            int result = in.read(buf, 0, buf.length);
            if (result > 0) {
                mark = -1;
                pos = 0;
                end = result;
            }
            return result;
        }
        if (mark == 0 && markLimit > buf.length) {
            int newLength = buf.length * 2;
            if (newLength > markLimit) {
                newLength = markLimit;
            }
            char[] newbuf = new char[newLength];
            System.arraycopy(buf, 0, newbuf, 0, buf.length);
            buf = newbuf;
        } else if (mark > 0) {
            System.arraycopy(buf, mark, buf, 0, buf.length - mark);
            pos -= mark;
            end -= mark;
            mark = 0;
        }
        int count = in.read(buf, pos, buf.length - pos);
        if (count != -1) {
            end += count;
        }
        return count;
    }
    private boolean isClosed() {
        return buf == null;
    }
    @Override
    public void mark(int markLimit) throws IOException {
        if (markLimit < 0) {
            throw new IllegalArgumentException();
        }
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException(Msg.getString("K005b")); 
            }
            this.markLimit = markLimit;
            mark = pos;
        }
    }
    @Override
    public boolean markSupported() {
        return true;
    }
    @Override
    public int read() throws IOException {
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException(Msg.getString("K005b")); 
            }
            if (pos < end || fillBuf() != -1) {
                return buf[pos++];
            }
            return -1;
        }
    }
    @Override
    public int read(char[] buffer, int offset, int length) throws IOException {
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException(Msg.getString("K005b")); 
            }
            if (offset < 0 || offset > buffer.length - length || length < 0) {
                throw new IndexOutOfBoundsException();
            }
            int outstanding = length;
            while (outstanding > 0) {
                int available = end - pos;
                if (available > 0) {
                    int count = available >= outstanding ? outstanding : available;
                    System.arraycopy(buf, pos, buffer, offset, count);
                    pos += count;
                    offset += count;
                    outstanding -= count;
                }
                if (outstanding == 0 || (outstanding < length && !in.ready())) {
                    break;
                }
                if ((mark == -1 || (pos - mark >= markLimit))
                        && outstanding >= buf.length) {
                    int count = in.read(buffer, offset, outstanding);
                    if (count > 0) {
                        offset += count;
                        outstanding -= count;
                        mark = -1;
                    }
                    break; 
                }
                if (fillBuf() == -1) {
                    break; 
                }
            }
            int count = length - outstanding;
            return (count > 0 || count == length) ? count : -1;
        }
    }
    final void chompNewline() throws IOException {
        if ((pos != end || fillBuf() != -1)
                && buf[pos] == '\n') {
            pos++;
        }
    }
    public String readLine() throws IOException {
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException(Msg.getString("K005b")); 
            }
            if (pos == end && fillBuf() == -1) {
                return null;
            }
            for (int charPos = pos; charPos < end; charPos++) {
                char ch = buf[charPos];
                if (ch > '\r') {
                    continue;
                }
                if (ch == '\n') {
                    String res = new String(buf, pos, charPos - pos);
                    pos = charPos + 1;
                    return res;
                } else if (ch == '\r') {
                    String res = new String(buf, pos, charPos - pos);
                    pos = charPos + 1;
                    if (((pos < end) || (fillBuf() != -1))
                            && (buf[pos] == '\n')) {
                        pos++;
                    }
                    return res;
                }
            }
            char eol = '\0';
            StringBuilder result = new StringBuilder(80);
            result.append(buf, pos, end - pos);
            while (true) {
                pos = end;
                if (eol == '\n') {
                    return result.toString();
                }
                if (fillBuf() == -1) {
                    return result.length() > 0 || eol != '\0'
                            ? result.toString()
                            : null;
                }
                for (int charPos = pos; charPos < end; charPos++) {
                    char c = buf[charPos];
                    if (eol == '\0') {
                        if ((c == '\n' || c == '\r')) {
                            eol = c;
                        }
                    } else if (eol == '\r' && c == '\n') {
                        if (charPos > pos) {
                            result.append(buf, pos, charPos - pos - 1);
                        }
                        pos = charPos + 1;
                        return result.toString();
                    } else {
                        if (charPos > pos) {
                            result.append(buf, pos, charPos - pos - 1);
                        }
                        pos = charPos;
                        return result.toString();
                    }
                }
                if (eol == '\0') {
                    result.append(buf, pos, end - pos);
                } else {
                    result.append(buf, pos, end - pos - 1);
                }
            }
        }
    }
    @Override
    public boolean ready() throws IOException {
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException(Msg.getString("K005b")); 
            }
            return ((end - pos) > 0) || in.ready();
        }
    }
    @Override
    public void reset() throws IOException {
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException(Msg.getString("K005b")); 
            }
            if (mark == -1) {
                throw new IOException(Msg.getString("K005c")); 
            }
            pos = mark;
        }
    }
    @Override
    public long skip(long amount) throws IOException {
        if (amount < 0) {
            throw new IllegalArgumentException();
        }
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException(Msg.getString("K005b")); 
            }
            if (amount < 1) {
                return 0;
            }
            if (end - pos >= amount) {
                pos += amount;
                return amount;
            }
            long read = end - pos;
            pos = end;
            while (read < amount) {
                if (fillBuf() == -1) {
                    return read;
                }
                if (end - pos >= amount - read) {
                    pos += amount - read;
                    return amount;
                }
                read += (end - pos);
                pos = end;
            }
            return amount;
        }
    }
}
