public class PrintWriter extends Writer {
    protected Writer out;
    private boolean ioError;
    private boolean autoflush;
    private final String lineSeparator = AccessController
            .doPrivileged(new PriviAction<String>("line.separator")); 
    public PrintWriter(OutputStream out) {
        this(new OutputStreamWriter(out), false);
    }
    public PrintWriter(OutputStream out, boolean autoflush) {
        this(new OutputStreamWriter(out), autoflush);
    }
    public PrintWriter(Writer wr) {
        this(wr, false);
    }
    public PrintWriter(Writer wr, boolean autoflush) {
        super(wr);
        this.autoflush = autoflush;
        out = wr;
    }
    public PrintWriter(File file) throws FileNotFoundException {
        this(new OutputStreamWriter(
                     new BufferedOutputStream(
                             new FileOutputStream(file), 8192)),
                false);
    }
    public PrintWriter(File file, String csn) throws FileNotFoundException,
            UnsupportedEncodingException {
        this(new OutputStreamWriter(
                     new BufferedOutputStream(
                             new FileOutputStream(file), 8192), csn),
                false);
    }
    public PrintWriter(String fileName) throws FileNotFoundException {
        this(new OutputStreamWriter(
                     new BufferedOutputStream(
                             new FileOutputStream(fileName), 8192)),
                false);
    }
    public PrintWriter(String fileName, String csn)
            throws FileNotFoundException, UnsupportedEncodingException {
        this(new OutputStreamWriter(
                     new BufferedOutputStream(
                             new FileOutputStream(fileName), 8192), csn),
                false);
    }
    public boolean checkError() {
        Writer delegate = out;
        if (delegate == null) {
            return ioError;
        }
        flush();
        return ioError || delegate.checkError();
    }
    @Override
    public void close() {
        synchronized (lock) {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    setError();
                }
                out = null;
            }
        }
    }
    @Override
    public void flush() {
        synchronized (lock) {
            if (out != null) {
                try {
                    out.flush();
                } catch (IOException e) {
                    setError();
                }
            } else {
                setError();
            }
        }
    }
    public PrintWriter format(String format, Object... args) {
        return format(Locale.getDefault(), format, args);
    }
    public PrintWriter format(Locale l, String format, Object... args) {
        if (format == null) {
            throw new NullPointerException(Msg.getString("K0351")); 
        }
        new Formatter(this, l).format(format, args);
        if (autoflush) {
            flush();
        }
        return this;
    }
    public PrintWriter printf(String format, Object... args) {
        return format(format, args);
    }
    public PrintWriter printf(Locale l, String format, Object... args) {
        return format(l, format, args);
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
    public void print(String str) {
        write(str != null ? str : String.valueOf((Object) null));
    }
    public void print(boolean bool) {
        print(String.valueOf(bool));
    }
    public void println() {
        synchronized (lock) {
            print(lineSeparator);
            if (autoflush) {
                flush();
            }
        }
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
    public void println(String str) {
        synchronized (lock) {
            print(str);
            println();
        }
    }
    public void println(boolean bool) {
        println(String.valueOf(bool));
    }
    protected void setError() {
        synchronized (lock) {
            ioError = true;
        }
    }
    @Override
    public void write(char[] buf) {
        write(buf, 0, buf.length);
    }
    @Override
    public void write(char[] buf, int offset, int count) {
        doWrite(buf, offset, count);
    }
    @Override
    public void write(int oneChar) {
        doWrite(new char[] { (char) oneChar }, 0, 1);
    }
    private final void doWrite(char[] buf, int offset, int count) {
        synchronized (lock) {
            if (out != null) {
                try {
                    out.write(buf, offset, count);
                } catch (IOException e) {
                    setError();
                }
            } else {
                setError();
            }
        }
    }
    @Override
    public void write(String str) {
        write(str.toCharArray());
    }
    @Override
    public void write(String str, int offset, int count) {
        write(str.substring(offset, offset + count).toCharArray());
    }
    @Override
    public PrintWriter append(char c) {
        write(c);
        return this;
    }
    @Override
    public PrintWriter append(CharSequence csq) {
        if (null == csq) {
            append(TOKEN_NULL, 0, TOKEN_NULL.length());
        } else {
            append(csq, 0, csq.length());
        }
        return this;
    }
    @Override
    public PrintWriter append(CharSequence csq, int start, int end) {
        if (null == csq) {
            csq = TOKEN_NULL;
        }
        String output = csq.subSequence(start, end).toString();
        write(output, 0, output.length());
        return this;
    }
}
