public class WindowsVirtualMachine extends HotSpotVirtualMachine {
    private static byte[] stub;
    private volatile long hProcess;     
    WindowsVirtualMachine(AttachProvider provider, String id)
        throws AttachNotSupportedException, IOException
    {
        super(provider, id);
        int pid;
        try {
            pid = Integer.parseInt(id);
        } catch (NumberFormatException x) {
            throw new AttachNotSupportedException("Invalid process identifier");
        }
        hProcess = openProcess(pid);
        try {
            enqueue(hProcess, stub, null, null);
        } catch (IOException x) {
            throw new AttachNotSupportedException(x.getMessage());
        }
    }
    public void detach() throws IOException {
        synchronized (this) {
            if (hProcess != -1) {
                closeProcess(hProcess);
                hProcess = -1;
            }
        }
    }
    InputStream execute(String cmd, Object ... args)
        throws AgentLoadException, IOException
    {
        assert args.length <= 3;        
        int r = (new Random()).nextInt();
        String pipename = "\\\\.\\pipe\\javatool" + r;
        long hPipe = createPipe(pipename);
        if (hProcess == -1) {
            closePipe(hPipe);
            throw new IOException("Detached from target VM");
        }
        try {
            enqueue(hProcess, stub, cmd, pipename, args);
            connectPipe(hPipe);
            PipedInputStream is = new PipedInputStream(hPipe);
            int status = readInt(is);
            if (status != 0) {
                if (cmd.equals("load")) {
                    throw new AgentLoadException("Failed to load agent library");
                } else {
                    throw new IOException("Command failed in target VM");
                }
            }
            return is;
        } catch (IOException ioe) {
            closePipe(hPipe);
            throw ioe;
        }
    }
    private class PipedInputStream extends InputStream {
        private long hPipe;
        public PipedInputStream(long hPipe) {
            this.hPipe = hPipe;
        }
        public synchronized int read() throws IOException {
            byte b[] = new byte[1];
            int n = this.read(b, 0, 1);
            if (n == 1) {
                return b[0] & 0xff;
            } else {
                return -1;
            }
        }
        public synchronized int read(byte[] bs, int off, int len) throws IOException {
            if ((off < 0) || (off > bs.length) || (len < 0) ||
                ((off + len) > bs.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0)
                return 0;
            return WindowsVirtualMachine.readPipe(hPipe, bs, off, len);
        }
        public void close() throws IOException {
            if (hPipe != -1) {
                WindowsVirtualMachine.closePipe(hPipe);
                hPipe = -1;
           }
        }
    }
    static native void init();
    static native byte[] generateStub();
    static native long openProcess(int pid) throws IOException;
    static native void closeProcess(long hProcess) throws IOException;
    static native long createPipe(String name) throws IOException;
    static native void closePipe(long hPipe) throws IOException;
    static native void connectPipe(long hPipe) throws IOException;
    static native int readPipe(long hPipe, byte buf[], int off, int buflen) throws IOException;
    static native void enqueue(long hProcess, byte[] stub,
        String cmd, String pipename, Object ... args) throws IOException;
    static {
        System.loadLibrary("attach");
        init();                                 
        stub = generateStub();                  
    }
}
