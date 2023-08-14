public class UnsyncBufferedOutputStream extends OutputStream {
        final OutputStream out;
        final byte[] buf;
        static final int size=8*1024;
        private static ThreadLocal bufCahce = new ThreadLocal() {
        protected synchronized Object initialValue() {
            return new byte[size];
        }
    };
        int pointer=0;
        public UnsyncBufferedOutputStream(OutputStream out) {
                buf=(byte[])bufCahce.get();
                this.out=out;
        }
        public void write(byte[] arg0) throws IOException {
                write(arg0,0,arg0.length);
        }
        public void write(byte[] arg0, int arg1, int len) throws IOException {
                int newLen=pointer+len;
                if (newLen> size) {
                        flushBuffer();
                        if (len>size) {
                                out.write(arg0,arg1,len);
                                return;
                        }
                        newLen=len;
                }
                System.arraycopy(arg0,arg1,buf,pointer,len);
                pointer=newLen;
        }
        private final void flushBuffer() throws IOException {
                if (pointer>0)
                        out.write(buf,0,pointer);
                pointer=0;
        }
        public void write(int arg0) throws IOException {
                if (pointer>= size) {
                        flushBuffer();
                }
                buf[pointer++]=(byte)arg0;
        }
        public void flush() throws IOException {
                flushBuffer();
                out.flush();
        }
        public void close() throws IOException {
                flush();
        }
}
