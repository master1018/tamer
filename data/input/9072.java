public class Compress {
    interface CompressConstants {
        static final int NOP  = 0;      
        static final int RAW  = 1;      
        static final int BASE = 2;      
        static final String codeTable =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ ,.!?\"'()";
    }
    public static class CompressRMIClientSocketFactory
        implements java.rmi.server.RMIClientSocketFactory, Serializable {
        public Socket createSocket(String host, int port)
            throws IOException {
            return ((Socket) new CompressSocket(host, port));
        }
    }
    public static class CompressRMIServerSocketFactory
        implements RMIServerSocketFactory,
                   Serializable {
        public ServerSocket createServerSocket(int port)
            throws IOException {
            return ((ServerSocket) new CompressServerSocket(port));
        }
    }
    public static class CompressSocket extends Socket {
        private InputStream in;
        private OutputStream out;
        public CompressSocket() { super(); }
        public CompressSocket(String host, int port) throws IOException {
            super(host, port);
        }
        public InputStream getInputStream() throws IOException {
            if (in == null) {
                in = new CompressInputStream(super.getInputStream());
            }
            return in;
        }
        public OutputStream getOutputStream() throws IOException {
            if (out == null) {
                out = new CompressOutputStream(super.getOutputStream());
            }
            return out;
        }
    }
    public static class CompressServerSocket extends ServerSocket {
        public CompressServerSocket(int port) throws IOException {
            super(port);
        }
        public Socket accept() throws IOException {
            Socket s = new CompressSocket();
            implAccept(s);
            return s;
        }
    }
    public static class CompressInputStream extends FilterInputStream
        implements CompressConstants
    {
        public CompressInputStream(InputStream in) {
            super(in);
        }
        int buf[] = new int[5];
        int bufPos = 5;
        public int read() throws IOException {
            try {
                int code;
                do {
                    code = readCode();
                } while (code == NOP);  
                if (code >= BASE)
                    return codeTable.charAt(code - BASE);
                else if (code == RAW) {
                    int high = readCode();
                    int low = readCode();
                    return (high << 4) | low;
                } else
                    throw new IOException("unknown compression code: " + code);
            } catch (EOFException e) {
                return -1;
            }
        }
        public int read(byte b[], int off, int len) throws IOException {
            if (len <= 0) {
                return 0;
            }
            int c = read();
            if (c == -1) {
                return -1;
            }
            b[off] = (byte)c;
            int i = 1;
            return i;
        }
        private int readCode() throws IOException {
            if (bufPos == 5) {
                int b1 = in.read();
                int b2 = in.read();
                int b3 = in.read();
                int b4 = in.read();
                if ((b1 | b2 | b3 | b4) < 0)
                    throw new EOFException();
                int pack = (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
                buf[0] = (pack >>> 24) & 0x3F;
                buf[1] = (pack >>> 18) & 0x3F;
                buf[2] = (pack >>> 12) & 0x3F;
                buf[3] = (pack >>>  6) & 0x3F;
                buf[4] = (pack >>>  0) & 0x3F;
                bufPos = 0;
            }
            return buf[bufPos++];
        }
    }
    public static class CompressOutputStream extends FilterOutputStream
        implements CompressConstants
    {
        public CompressOutputStream(OutputStream out) {
            super(out);
        }
        int buf[] = new int[5];
        int bufPos = 0;
        public void write(int b) throws IOException {
            b &= 0xFF;                  
            int pos = codeTable.indexOf((char)b);
            if (pos != -1)
                writeCode(BASE + pos);
            else {
                writeCode(RAW);
                writeCode(b >> 4);
                writeCode(b & 0xF);
            }
        }
        public void write(byte b[], int off, int len) throws IOException {
            for (int i = 0; i < len; i++)
                write(b[off + i]);
        }
        public void flush() throws IOException {
            while (bufPos > 0)
                writeCode(NOP);
        }
        private void writeCode(int c) throws IOException {
            buf[bufPos++] = c;
            if (bufPos == 5) {  
                int pack = (buf[0] << 24) | (buf[1] << 18) | (buf[2] << 12) |
                    (buf[3] << 6) | buf[4];
                out.write((pack >>> 24) & 0xFF);
                out.write((pack >>> 16) & 0xFF);
                out.write((pack >>> 8)  & 0xFF);
                out.write((pack >>> 0)  & 0xFF);
                bufPos = 0;
            }
        }
    }
}
