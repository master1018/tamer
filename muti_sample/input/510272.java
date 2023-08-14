abstract class LoggingPrintStream extends PrintStream {
    private final StringBuilder builder = new StringBuilder();
    private ByteBuffer encodedBytes;
    private CharBuffer decodedChars;
    private CharsetDecoder decoder;
    protected LoggingPrintStream() {
        super(new OutputStream() {
            public void write(int oneByte) throws IOException {
                throw new AssertionError();
            }
        });
    }
    protected abstract void log(String line);
    @Override
    public synchronized void flush() {
        flush(true);
    }
    private void flush(boolean completely) {
        int length = builder.length();
        int start = 0;
        int nextBreak;
        while (start < length
                && (nextBreak = builder.indexOf("\n", start)) != -1) {
            log(builder.substring(start, nextBreak));
            start = nextBreak + 1;
        }
        if (completely) {
            if (start < length) {
                log(builder.substring(start));
            }
            builder.setLength(0);
        } else {
            builder.delete(0, start);
        }
    }
    public void write(int oneByte) {
        write(new byte[] { (byte) oneByte }, 0, 1);
    }
    @Override
    public void write(byte[] buffer) {
        write(buffer, 0, buffer.length);
    }
    @Override
    public synchronized void write(byte bytes[], int start, int count) {
        if (decoder == null) {
            encodedBytes = ByteBuffer.allocate(80);
            decodedChars = CharBuffer.allocate(80);
            decoder = Charset.defaultCharset().newDecoder()
                    .onMalformedInput(CodingErrorAction.REPLACE)
                    .onUnmappableCharacter(CodingErrorAction.REPLACE);
        }
        int end = start + count;
        while (start < end) {
            int numBytes = Math.min(encodedBytes.remaining(), end - start);
            encodedBytes.put(bytes, start, numBytes);
            start += numBytes;
            encodedBytes.flip();
            CoderResult coderResult;
            do {
                coderResult = decoder.decode(encodedBytes, decodedChars, false);
                decodedChars.flip();
                builder.append(decodedChars);
                decodedChars.clear();
            } while (coderResult.isOverflow());
            encodedBytes.compact();
        }
        flush(false);
    }
    @Override
    public boolean checkError() {
        return false;
    }
    @Override
    protected void setError() {  }
    @Override
    public void close() {  }
    @Override
    public PrintStream format(String format, Object... args) {
        return format(Locale.getDefault(), format, args);
    }
    @Override
    public PrintStream printf(String format, Object... args) {
        return format(format, args);
    }
    @Override
    public PrintStream printf(Locale l, String format, Object... args) {
        return format(l, format, args);
    }
    private final Formatter formatter = new Formatter(builder, null);
    @Override
    public synchronized PrintStream format(
            Locale l, String format, Object... args) {
        if (format == null) {
            throw new NullPointerException("format");
        }
        formatter.format(l, format, args);
        flush(false);
        return this;
    }
    @Override
    public synchronized void print(char[] charArray) {
        builder.append(charArray);
        flush(false);
    }
    @Override
    public synchronized void print(char ch) {
        builder.append(ch);
        if (ch == '\n') {
            flush(false);
        }
    }
    @Override
    public synchronized void print(double dnum) {
        builder.append(dnum);
    }
    @Override
    public synchronized void print(float fnum) {
        builder.append(fnum);
    }
    @Override
    public synchronized void print(int inum) {
        builder.append(inum);
    }
    @Override
    public synchronized void print(long lnum) {
        builder.append(lnum);
    }
    @Override
    public synchronized void print(Object obj) {
        builder.append(obj);
        flush(false);
    }
    @Override
    public synchronized void print(String str) {
        builder.append(str);
        flush(false);
    }
    @Override
    public synchronized void print(boolean bool) {
        builder.append(bool);
    }
    @Override
    public synchronized void println() {
        flush(true);
    }
    @Override
    public synchronized void println(char[] charArray) {
        builder.append(charArray);
        flush(true);
    }
    @Override
    public synchronized void println(char ch) {
        builder.append(ch);
        flush(true);
    }
    @Override
    public synchronized void println(double dnum) {
        builder.append(dnum);
        flush(true);
    }
    @Override
    public synchronized void println(float fnum) {
        builder.append(fnum);
        flush(true);
    }
    @Override
    public synchronized void println(int inum) {
        builder.append(inum);
        flush(true);
    }
    @Override
    public synchronized void println(long lnum) {
        builder.append(lnum);
        flush(true);
    }
    @Override
    public synchronized void println(Object obj) {
        builder.append(obj);
        flush(true);
    }
    @Override
    public synchronized void println(String s) {
        if (builder.length() == 0) {
            int length = s.length();
            int start = 0;
            int nextBreak;
            while (start < length
                    && (nextBreak = s.indexOf('\n', start)) != -1) {
                log(s.substring(start, nextBreak));
                start = nextBreak + 1;
            }
            if (start < length) {
                log(s.substring(start));
            }
        } else {
            builder.append(s);
            flush(true);
        }
    }
    @Override
    public synchronized void println(boolean bool) {
        builder.append(bool);
        flush(true);
    }
    @Override
    public synchronized PrintStream append(char c) {
        print(c);
        return this;
    }
    @Override
    public synchronized PrintStream append(CharSequence csq) {
        builder.append(csq);
        flush(false);
        return this;
    }
    @Override
    public synchronized PrintStream append(
            CharSequence csq, int start, int end) {
        builder.append(csq, start, end);
        flush(false);
        return this;
    }
}
