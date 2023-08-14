class HttpSendOutputStream extends FilterOutputStream {
    HttpSendSocket owner;
    public HttpSendOutputStream(OutputStream out, HttpSendSocket owner)
        throws IOException
    {
        super(out);
        this.owner = owner;
    }
    public void deactivate()
    {
        out = null;
    }
    public void write(int b) throws IOException
    {
        if (out == null)
            out = owner.writeNotify();
        out.write(b);
    }
    public void write(byte b[], int off, int len) throws IOException
    {
        if (len == 0)
            return;
        if (out == null)
            out = owner.writeNotify();
        out.write(b, off, len);
    }
    public void flush() throws IOException
    {
        if (out != null)
            out.flush();
    }
    public void close() throws IOException
    {
        flush();
        owner.close();
    }
}
