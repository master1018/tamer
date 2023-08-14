class HttpInputStream extends FilterInputStream {
    protected int bytesLeft;
    protected int bytesLeftAtMark;
    public HttpInputStream(InputStream in) throws IOException
    {
        super(in);
        if (in.markSupported())
            in.mark(0); 
        DataInputStream dis = new DataInputStream(in);
        String key = "Content-length:".toLowerCase();
        boolean contentLengthFound = false;
        String line;
        do {
            line = dis.readLine();
            if (RMIMasterSocketFactory.proxyLog.isLoggable(Log.VERBOSE)) {
                RMIMasterSocketFactory.proxyLog.log(Log.VERBOSE,
                    "received header line: \"" + line + "\"");
            }
            if (line == null)
                throw new EOFException();
            if (line.toLowerCase().startsWith(key)) {
                if (contentLengthFound)
                    ; 
                bytesLeft =
                    Integer.parseInt(line.substring(key.length()).trim());
                contentLengthFound = true;
            }
        } while ((line.length() != 0) &&
                 (line.charAt(0) != '\r') && (line.charAt(0) != '\n'));
        if (!contentLengthFound || bytesLeft < 0) {
            bytesLeft = Integer.MAX_VALUE;
        }
        bytesLeftAtMark = bytesLeft;
        if (RMIMasterSocketFactory.proxyLog.isLoggable(Log.VERBOSE)) {
            RMIMasterSocketFactory.proxyLog.log(Log.VERBOSE,
                "content length: " + bytesLeft);
        }
    }
    public int available() throws IOException
    {
        int bytesAvailable = in.available();
        if (bytesAvailable > bytesLeft)
            bytesAvailable = bytesLeft;
        return bytesAvailable;
    }
    public int read() throws IOException
    {
        if (bytesLeft > 0) {
            int data = in.read();
            if (data != -1)
                -- bytesLeft;
            if (RMIMasterSocketFactory.proxyLog.isLoggable(Log.VERBOSE)) {
                RMIMasterSocketFactory.proxyLog.log(Log.VERBOSE,
                   "received byte: '" +
                    ((data & 0x7F) < ' ' ? " " : String.valueOf((char) data)) +
                    "' " + data);
            }
            return data;
        }
        else {
            RMIMasterSocketFactory.proxyLog.log(Log.VERBOSE,
                                                "read past content length");
            return -1;
        }
    }
    public int read(byte b[], int off, int len) throws IOException
    {
        if (bytesLeft == 0 && len > 0) {
            RMIMasterSocketFactory.proxyLog.log(Log.VERBOSE,
                                                "read past content length");
            return -1;
        }
        if (len > bytesLeft)
            len = bytesLeft;
        int bytesRead = in.read(b, off, len);
        bytesLeft -= bytesRead;
        if (RMIMasterSocketFactory.proxyLog.isLoggable(Log.VERBOSE)) {
            RMIMasterSocketFactory.proxyLog.log(Log.VERBOSE,
                "read " + bytesRead + " bytes, " + bytesLeft + " remaining");
        }
        return bytesRead;
    }
    public void mark(int readlimit)
    {
        in.mark(readlimit);
        if (in.markSupported())
            bytesLeftAtMark = bytesLeft;
    }
    public void reset() throws IOException
    {
        in.reset();
        bytesLeft = bytesLeftAtMark;
    }
    public long skip(long n) throws IOException
    {
        if (n > bytesLeft)
            n = bytesLeft;
        long bytesSkipped = in.skip(n);
        bytesLeft -= bytesSkipped;
        return bytesSkipped;
    }
}
