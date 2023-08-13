class InitManifest {
    private byte[] buf;
    private int pos;
    Attributes.Name name;
    String value;
    CharsetDecoder decoder = ThreadLocalCache.utf8Decoder.get();
    CharBuffer cBuf = ThreadLocalCache.charBuffer.get();
    InitManifest(byte[] buf, Attributes main, Attributes.Name ver)
            throws IOException {
        this.buf = buf;
        if (!readHeader() || (ver != null && !name.equals(ver))) {
            throw new IOException(Messages.getString(
                    "archive.2D", ver)); 
        }
        main.put(name, value);
        while (readHeader()) {
            main.put(name, value);
        }
    }
    void initEntries(Map<String, Attributes> entries,
            Map<String, Manifest.Chunk> chunks) throws IOException {
        int mark = pos;
        while (readHeader()) {
            if (!Attributes.Name.NAME.equals(name)) {
                throw new IOException(Messages.getString("archive.23")); 
            }
            String entryNameValue = value;
            Attributes entry = entries.get(entryNameValue);
            if (entry == null) {
                entry = new Attributes(12);
            }
            while (readHeader()) {
                entry.put(name, value);
            }
            if (chunks != null) {
                if (chunks.get(entryNameValue) != null) {
                    throw new IOException(Messages.getString("archive.34")); 
                }
                chunks.put(entryNameValue, new Manifest.Chunk(mark, pos));
                mark = pos;
            }
            entries.put(entryNameValue, entry);
        }
    }
    int getPos() {
        return pos;
    }
    int linebreak = 0;
    private boolean readHeader() throws IOException {
        if (linebreak > 1) {
            linebreak = 0;
            return false;
        }
        readName();
        linebreak = 0;
        readValue();
        return linebreak > 0;
    }
    private byte[] wrap(int mark, int pos) {
        byte[] buffer = new byte[pos - mark];
        System.arraycopy(buf, mark, buffer, 0, pos - mark);
        return buffer;
    }
    private void readName() throws IOException {
        int i = 0;
        int mark = pos;
        while (pos < buf.length) {
            byte b = buf[pos++];
            if (b == ':') {
                byte[] nameBuffer = wrap(mark, pos - 1);
                if (buf[pos++] != ' ') {
                    throw new IOException(Messages.getString(
                            "archive.30", nameBuffer)); 
                }
                name = new Attributes.Name(nameBuffer);
                return;
            }
            if (!((b >= 'a' && b <= 'z') || (b >= 'A' && b <= 'Z') || b == '_'
                    || b == '-' || (b >= '0' && b <= '9'))) {
                throw new IOException(Messages.getString("archive.30", b)); 
            }
        }
        if (i > 0) {
            throw new IOException(Messages.getString(
                    "archive.30", wrap(mark, buf.length))); 
        }
    }
    private void readValue() throws IOException {
        byte next;
        boolean lastCr = false;
        int mark = pos;
        int last = pos;
        decoder.reset();
        cBuf.clear();
        while (pos < buf.length) {
            next = buf[pos++];
            switch (next) {
            case 0:
                throw new IOException(Messages.getString("archive.2F")); 
            case '\n':
                if (lastCr) {
                    lastCr = false;
                } else {
                    linebreak++;
                }
                continue;
            case '\r':
                lastCr = true;
                linebreak++;
                continue;
            case ' ':
                if (linebreak == 1) {
                    decode(mark, last, false);
                    mark = pos;
                    linebreak = 0;
                    continue;
                }
            }
            if (linebreak >= 1) {
                pos--;
                break;
            }
            last = pos;
        }
        decode(mark, last, true);
        while (CoderResult.OVERFLOW == decoder.flush(cBuf)) {
            enlargeBuffer();
        }
        value = new String(cBuf.array(), cBuf.arrayOffset(), cBuf.position());
    }
    private void decode(int mark, int pos, boolean endOfInput)
            throws IOException {
        ByteBuffer bBuf = ByteBuffer.wrap(buf, mark, pos - mark);
        while (CoderResult.OVERFLOW == decoder.decode(bBuf, cBuf, endOfInput)) {
            enlargeBuffer();
        }
    }
    private void enlargeBuffer() {
        CharBuffer newBuf = CharBuffer.allocate(cBuf.capacity() * 2);
        newBuf.put(cBuf.array(), cBuf.arrayOffset(), cBuf.position());
        cBuf = newBuf;
        ThreadLocalCache.charBuffer.set(cBuf);
    }
}
