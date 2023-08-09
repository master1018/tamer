public class Manifest implements Cloneable {
    static final int LINE_LENGTH_LIMIT = 72;
    private static final byte[] LINE_SEPARATOR = new byte[] { '\r', '\n' };
    private static final byte[] VALUE_SEPARATOR = new byte[] { ':', ' ' };
    private static final Attributes.Name NAME_ATTRIBUTE = new Attributes.Name(
            "Name"); 
    private Attributes mainAttributes = new Attributes();
    private HashMap<String, Attributes> entries = new HashMap<String, Attributes>();
    static class Chunk {
        int start;
        int end;
        Chunk(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
    private HashMap<String, Chunk> chunks;
    private InitManifest im;
    private int mainEnd;
    public Manifest() {
        super();
    }
    public Manifest(InputStream is) throws IOException {
        super();
        read(is);
    }
    @SuppressWarnings("unchecked")
    public Manifest(Manifest man) {
        mainAttributes = (Attributes) man.mainAttributes.clone();
        entries = (HashMap<String, Attributes>) ((HashMap<String, Attributes>) man
                .getEntries()).clone();
    }
    Manifest(InputStream is, boolean readChunks) throws IOException {
        if (readChunks) {
            chunks = new HashMap<String, Chunk>();
        }
        read(is);
    }
    public void clear() {
        im = null;
        entries.clear();
        mainAttributes.clear();
    }
    public Attributes getAttributes(String name) {
        return getEntries().get(name);
    }
    public Map<String, Attributes> getEntries() {
        initEntries();
        return entries;
    }
    private void initEntries() {
        if (im == null) {
            return;
        }
    }
    public Attributes getMainAttributes() {
        return mainAttributes;
    }
    @Override
    public Object clone() {
        return new Manifest(this);
    }
    public void write(OutputStream os) throws IOException {
        write(this, os);
    }
    public void read(InputStream is) throws IOException {
        byte[] buf;
        try {
            buf = InputStreamHelper.expose(is);
        } catch (UnsupportedOperationException uoe) {
            buf = readFully(is);
        }
        if (buf.length == 0) {
            return;
        }
        byte b = buf[buf.length - 1];
        if (0 == b || 26 == b) {
            buf[buf.length - 1] = '\n';
        }
        im = new InitManifest(buf, mainAttributes, null);
        mainEnd = im.getPos();
        im.initEntries(entries, chunks);
        im = null;
    }
    private byte[] readFully(InputStream is) throws IOException {
        byte[] buffer = new byte[4096];
        int count = is.read(buffer);
        int nextByte = is.read();
        if (nextByte == -1) {
            byte[] dest = new byte[count];
            System.arraycopy(buffer, 0, dest, 0, count);
            return dest;
        }
        if (!containsLine(buffer, count)) {
            throw new IOException(Messages.getString("archive.2E")); 
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream(count * 2);
        baos.write(buffer, 0, count);
        baos.write(nextByte);
        while (true) {
            count = is.read(buffer);
            if (count == -1) {
                return baos.toByteArray();
            }
            baos.write(buffer, 0, count);
        }
    }
    private boolean containsLine(byte[] buffer, int length) {
        for (int i = 0; i < length; i++) {
            if (buffer[i] == 0x0A || buffer[i] == 0x0D) {
                return true;
            }
        }
        return false;
    }
    @Override
    public int hashCode() {
        return mainAttributes.hashCode() ^ getEntries().hashCode();
    }
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o.getClass() != this.getClass()) {
            return false;
        }
        if (!mainAttributes.equals(((Manifest) o).mainAttributes)) {
            return false;
        }
        return getEntries().equals(((Manifest) o).getEntries());
    }
    Chunk getChunk(String name) {
        return chunks.get(name);
    }
    void removeChunks() {
        chunks = null;
    }
    int getMainAttributesEnd() {
        return mainEnd;
    }
    static void write(Manifest manifest, OutputStream out) throws IOException {
        CharsetEncoder encoder = ThreadLocalCache.utf8Encoder.get();
        ByteBuffer buffer = ThreadLocalCache.byteBuffer.get();
        String version = manifest.mainAttributes
                .getValue(Attributes.Name.MANIFEST_VERSION);
        if (version != null) {
            writeEntry(out, Attributes.Name.MANIFEST_VERSION, version, encoder,
                    buffer);
            Iterator<?> entries = manifest.mainAttributes.keySet().iterator();
            while (entries.hasNext()) {
                Attributes.Name name = (Attributes.Name) entries.next();
                if (!name.equals(Attributes.Name.MANIFEST_VERSION)) {
                    writeEntry(out, name, manifest.mainAttributes
                            .getValue(name), encoder, buffer);
                }
            }
        }
        out.write(LINE_SEPARATOR);
        Iterator<String> i = manifest.getEntries().keySet().iterator();
        while (i.hasNext()) {
            String key = i.next();
            writeEntry(out, NAME_ATTRIBUTE, key, encoder, buffer);
            Attributes attrib = manifest.entries.get(key);
            Iterator<?> entries = attrib.keySet().iterator();
            while (entries.hasNext()) {
                Attributes.Name name = (Attributes.Name) entries.next();
                writeEntry(out, name, attrib.getValue(name), encoder, buffer);
            }
            out.write(LINE_SEPARATOR);
        }
    }
    private static void writeEntry(OutputStream os, Attributes.Name name,
            String value, CharsetEncoder encoder, ByteBuffer bBuf)
            throws IOException {
        byte[] out = name.getBytes();
        if (out.length > LINE_LENGTH_LIMIT) {
            throw new IOException(Messages.getString(
                    "archive.33", name, Integer.valueOf(LINE_LENGTH_LIMIT))); 
        }
        os.write(out);
        os.write(VALUE_SEPARATOR);
        encoder.reset();
        bBuf.clear().limit(LINE_LENGTH_LIMIT - out.length - 2);
        CharBuffer cBuf = CharBuffer.wrap(value);
        CoderResult r;
        while (true) {
            r = encoder.encode(cBuf, bBuf, true);
            if (CoderResult.UNDERFLOW == r) {
                r = encoder.flush(bBuf);
            }
            os.write(bBuf.array(), bBuf.arrayOffset(), bBuf.position());
            os.write(LINE_SEPARATOR);
            if (CoderResult.UNDERFLOW == r) {
                break;
            }
            os.write(' ');
            bBuf.clear().limit(LINE_LENGTH_LIMIT - 1);
        }
    }
}
