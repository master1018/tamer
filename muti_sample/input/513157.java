public class Serializer {
    private static final String TAG = "Serializer";
    private boolean logging = false;    
    private static final int NOT_PENDING = -1;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    String pending;
    int pendingTag = NOT_PENDING;
    int depth;
    String name;
    String[] nameStack = new String[20];
    Hashtable<String, Object> tagTable = new Hashtable<String, Object>();
    private int tagPage;
    public Serializer() {
        this(true);
    }
    public Serializer(boolean startDocument, boolean _logging) {
        this(true);
        logging = _logging;
    }
    public Serializer(boolean startDocument) {
        super();
        if (startDocument) {
            try {
                startDocument();
            } catch (IOException e) {
            }
        } else {
            out.write(0);
        }
    }
    void log(String str) {
        int cr = str.indexOf('\n');
        if (cr > 0) {
            str = str.substring(0, cr);
        }
        Log.v(TAG, str);
        if (Eas.FILE_LOG) {
            FileLogger.log(TAG, str);
        }
    }
    public void done() throws IOException {
        if (depth != 0) {
            throw new IOException("Done received with unclosed tags");
        }
        writeInteger(out, 0);
        out.write(buf.toByteArray());
        out.flush();
    }
    public void startDocument() throws IOException{
        out.write(0x03); 
        out.write(0x01); 
        out.write(106);
    }
    public void checkPendingTag(boolean degenerated) throws IOException {
        if (pendingTag == NOT_PENDING)
            return;
        int page = pendingTag >> Tags.PAGE_SHIFT;
        int tag = pendingTag & Tags.PAGE_MASK;
        if (page != tagPage) {
            tagPage = page;
            buf.write(Wbxml.SWITCH_PAGE);
            buf.write(page);
        }
        buf.write(degenerated ? tag : tag | 64);
        if (logging) {
            String name = Tags.pages[page][tag - 5];
            nameStack[depth] = name;
            log("<" + name + '>');
        }
        pendingTag = NOT_PENDING;
    }
    public Serializer start(int tag) throws IOException {
        checkPendingTag(false);
        pendingTag = tag;
        depth++;
        return this;
    }
    public Serializer end() throws IOException {
        if (pendingTag >= 0) {
            checkPendingTag(true);
        } else {
            buf.write(Wbxml.END);
            if (logging) {
                log("</" + nameStack[depth] + '>');
            }
        }
        depth--;
        return this;
    }
    public Serializer tag(int t) throws IOException {
        start(t);
        end();
        return this;
    }
    public Serializer data(int tag, String value) throws IOException {
        if (value == null) {
            Log.e(TAG, "Writing null data for tag: " + tag);
        }
        start(tag);
        text(value);
        end();
        return this;
    }
    @Override
    public String toString() {
        return out.toString();
    }
    public byte[] toByteArray() {
        return out.toByteArray();
    }
    public Serializer text(String text) throws IOException {
        if (text == null) {
            Log.e(TAG, "Writing null text for pending tag: " + pendingTag);
        }
        checkPendingTag(false);
        buf.write(Wbxml.STR_I);
        writeLiteralString(buf, text);
        if (logging) {
            log(text);
        }
        return this;
    }
    void writeInteger(OutputStream out, int i) throws IOException {
        byte[] buf = new byte[5];
        int idx = 0;
        do {
            buf[idx++] = (byte) (i & 0x7f);
            i = i >> 7;
        } while (i != 0);
        while (idx > 1) {
            out.write(buf[--idx] | 0x80);
        }
        out.write(buf[0]);
        if (logging) {
            log(Integer.toString(i));
        }
    }
    void writeLiteralString(OutputStream out, String s) throws IOException {
        byte[] data = s.getBytes("UTF-8");
        out.write(data);
        out.write(0);
    }
    void writeStringValue (ContentValues cv, String key, int tag) throws IOException {
        String value = cv.getAsString(key);
        if (value != null && value.length() > 0) {
            data(tag, value);
        }
    }
}
