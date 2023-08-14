public class PrintStream extends FilterOutputStream implements Appendable,
        Closeable {
    private static final String TOKEN_NULL = "null"; 
    private boolean ioError;
    private boolean autoflush;
    private String encoding;
    private final String lineSeparator = AccessController
            .doPrivileged(new PriviAction<String>("line.separator")); 
    public PrintStream(OutputStream out) {
        super(out);
        if (out == null) {
            throw new NullPointerException();
        }
    }
    public PrintStream(OutputStream out, boolean autoflush) {
        super(out);
        if (out == null) {
            throw new NullPointerException();
        }
        this.autoflush = autoflush;
    }
    public PrintStream(OutputStream out, boolean autoflush, String enc)
            throws UnsupportedEncodingException {
        super(out);
        if (out == null || enc == null) {
            throw new NullPointerException();
        }
        this.autoflush = autoflush;
        try {
            if (!Charset.isSupported(enc)) {
                throw new UnsupportedEncodingException(enc);
            }
        } catch (IllegalCharsetNameException e) {
            throw new UnsupportedEncodingException(enc);
        }
        encoding = enc;
    }
    public PrintStream(File file) throws FileNotFoundException {
        super(new FileOutputStream(file));
    }
    public PrintStream(File file, String csn) throws FileNotFoundException,
            UnsupportedEncodingException {
        super(new FileOutputStream(file));
        if (csn == null) {
            throw new NullPointerException();
        }
        if (!Charset.isSupported(csn)) {
            throw new UnsupportedEncodingException();
        }
        encoding = csn;
    }
    public PrintStream(String fileName) throws FileNotFoundException {
        this(new File(fileName));
    }
    public PrintStream(String fileName, String csn)
            throws FileNotFoundException, UnsupportedEncodingException {
        this(new File(fileName), csn);
    }
    public boolean checkError() {
        OutputStream delegate = out;
        if (delegate == null) {
            return ioError;
        }
        flush();
        return ioError || delegate.checkError();
    }
    @Override
    public synchronized void close() {
        flush();
        if (out != null) {
            try {
                out.close();
                out = null;
            } catch (IOException e) {
                setError();
            }
        }
    }
    @Override
    public synchronized void flush() {
        if (out != null) {
            try {
                out.flush();
                return;
            } catch (IOException e) {
            }
        }
        setError();
    }
    public PrintStream format(String format, Object... args) {
        return format(Locale.getDefault(), format, args);
    }
    public PrintStream format(Locale l, String format, Object... args) {
        if (format == null) {
            throw new NullPointerException(Msg.getString("K0351")); 
        }
        new Formatter(this, l).format(format, args);
        return this;
    }
    public PrintStream printf(String format, Object... args) {
        return format(format, args);
    }
    public PrintStream printf(Locale l, String format, Object... args) {
        return format(l, format, args);
    }
    private void newline() {
        print(lineSeparator);
    }
    public void print(char[] charArray) {
        print(new String(charArray, 0, charArray.length));
    }
    public void print(char ch) {
        print(String.valueOf(ch));
    }
    public void print(double dnum) {
        print(String.valueOf(dnum));
    }
    public void print(float fnum) {
        print(String.valueOf(fnum));
    }
    public void print(int inum) {
        print(String.valueOf(inum));
    }
    public void print(long lnum) {
        print(String.valueOf(lnum));
    }
    public void print(Object obj) {
        print(String.valueOf(obj));
    }
    public synchronized void print(String str) {
        if (out == null) {
            setError();
            return;
        }
        if (str == null) {
            print("null"); 
            return;
        }
        try {
            if (encoding == null) {
                write(str.getBytes());
            } else {
                write(str.getBytes(encoding));
            }
        } catch (IOException e) {
            setError();
        }
    }
    public void print(boolean bool) {
        print(String.valueOf(bool));
    }
    public void println() {
        newline();
    }
    public void println(char[] charArray) {
        println(new String(charArray, 0, charArray.length));
    }
    public void println(char ch) {
        println(String.valueOf(ch));
    }
    public void println(double dnum) {
        println(String.valueOf(dnum));
    }
   public void println(float fnum) {
        println(String.valueOf(fnum));
    }
    public void println(int inum) {
        println(String.valueOf(inum));
    }
    public void println(long lnum) {
        println(String.valueOf(lnum));
    }
    public void println(Object obj) {
        println(String.valueOf(obj));
    }
    public synchronized void println(String str) {
        print(str);
        newline();
    }
    public void println(boolean bool) {
        println(String.valueOf(bool));
    }
    protected void setError() {
        ioError = true;
    }
    @Override
    public void write(byte[] buffer, int offset, int length) {
        if (offset > buffer.length || offset < 0) {
            throw new ArrayIndexOutOfBoundsException(Msg.getString("K002e", offset)); 
        }
        if (length < 0 || length > buffer.length - offset) {
            throw new ArrayIndexOutOfBoundsException(Msg.getString("K0031", length)); 
        }
        synchronized (this) {
            if (out == null) {
                setError();
                return;
            }
            try {
                out.write(buffer, offset, length);
                if (autoflush) {
                    flush();
                }
            } catch (IOException e) {
                setError();
            }
        }
    }
    @Override
    public synchronized void write(int oneByte) {
        if (out == null) {
            setError();
            return;
        }
        try {
            out.write(oneByte);
            int b = oneByte & 0xFF;
            boolean isNewline = b == 0x0A || b == 0x15; 
            if (autoflush && isNewline) {
                flush();
            }
        } catch (IOException e) {
            setError();
        }
    }
    public PrintStream append(char c) {
        print(c);
        return this;
    }
    public PrintStream append(CharSequence csq) {
        if (null == csq) {
            print(TOKEN_NULL);
        } else {
            print(csq.toString());
        }
        return this;
    }
    public PrintStream append(CharSequence csq, int start, int end) {
        if (null == csq) {
            print(TOKEN_NULL.substring(start, end));
        } else {
            print(csq.subSequence(start, end).toString());
        }
        return this;
    }
}
