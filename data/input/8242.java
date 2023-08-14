public class LinuxVirtualMachine extends HotSpotVirtualMachine {
    private static final String tmpdir = System.getProperty("java.io.tmpdir");
    static boolean isLinuxThreads;
    String path;
    LinuxVirtualMachine(AttachProvider provider, String vmid)
        throws AttachNotSupportedException, IOException
    {
        super(provider, vmid);
        int pid;
        try {
            pid = Integer.parseInt(vmid);
        } catch (NumberFormatException x) {
            throw new AttachNotSupportedException("Invalid process identifier");
        }
        path = findSocketFile(pid);
        if (path == null) {
            File f = createAttachFile(pid);
            try {
                if (isLinuxThreads) {
                    int mpid;
                    try {
                        mpid = getLinuxThreadsManager(pid);
                    } catch (IOException x) {
                        throw new AttachNotSupportedException(x.getMessage());
                    }
                    assert(mpid >= 1);
                    sendQuitToChildrenOf(mpid);
                } else {
                    sendQuitTo(pid);
                }
                int i = 0;
                long delay = 200;
                int retries = (int)(attachTimeout() / delay);
                do {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException x) { }
                    path = findSocketFile(pid);
                    i++;
                } while (i <= retries && path == null);
                if (path == null) {
                    throw new AttachNotSupportedException(
                        "Unable to open socket file: target process not responding " +
                        "or HotSpot VM not loaded");
                }
            } finally {
                f.delete();
            }
        }
        checkPermissions(path);
        int s = socket();
        try {
            connect(s, path);
        } finally {
            close(s);
        }
    }
    public void detach() throws IOException {
        synchronized (this) {
            if (this.path != null) {
                this.path = null;
            }
        }
    }
    private final static String PROTOCOL_VERSION = "1";
    private final static int ATTACH_ERROR_BADVERSION = 101;
    InputStream execute(String cmd, Object ... args) throws AgentLoadException, IOException {
        assert args.length <= 3;                
        String p;
        synchronized (this) {
            if (this.path == null) {
                throw new IOException("Detached from target VM");
            }
            p = this.path;
        }
        int s = socket();
        try {
            connect(s, p);
        } catch (IOException x) {
            close(s);
            throw x;
        }
        IOException ioe = null;
        try {
            writeString(s, PROTOCOL_VERSION);
            writeString(s, cmd);
            for (int i=0; i<3; i++) {
                if (i < args.length && args[i] != null) {
                    writeString(s, (String)args[i]);
                } else {
                    writeString(s, "");
                }
            }
        } catch (IOException x) {
            ioe = x;
        }
        SocketInputStream sis = new SocketInputStream(s);
        int completionStatus;
        try {
            completionStatus = readInt(sis);
        } catch (IOException x) {
            sis.close();
            if (ioe != null) {
                throw ioe;
            } else {
                throw x;
            }
        }
        if (completionStatus != 0) {
            sis.close();
            if (completionStatus == ATTACH_ERROR_BADVERSION) {
                throw new IOException("Protocol mismatch with target VM");
            }
            if (cmd.equals("load")) {
                throw new AgentLoadException("Failed to load agent library");
            } else {
                throw new IOException("Command failed in target VM");
            }
        }
        return sis;
    }
    private class SocketInputStream extends InputStream {
        int s;
        public SocketInputStream(int s) {
            this.s = s;
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
            return LinuxVirtualMachine.read(s, bs, off, len);
        }
        public void close() throws IOException {
            LinuxVirtualMachine.close(s);
        }
    }
    private String findSocketFile(int pid) {
        String fn = ".java_pid" + pid;
        String path = "/proc/" + pid + "/cwd/" + fn;
        File f = new File(path);
        if (!f.exists()) {
            f = new File(tmpdir, fn);
            path = f.exists() ? f.getPath() : null;
        }
        return path;
    }
    private File createAttachFile(int pid) throws IOException {
        String fn = ".attach_pid" + pid;
        String path = "/proc/" + pid + "/cwd/" + fn;
        File f = new File(path);
        try {
            f.createNewFile();
        } catch (IOException x) {
            f = new File(tmpdir, fn);
            f.createNewFile();
        }
        return f;
    }
    private void writeString(int fd, String s) throws IOException {
        if (s.length() > 0) {
            byte b[];
            try {
                b = s.getBytes("UTF-8");
            } catch (java.io.UnsupportedEncodingException x) {
                throw new InternalError();
            }
            LinuxVirtualMachine.write(fd, b, 0, b.length);
        }
        byte b[] = new byte[1];
        b[0] = 0;
        write(fd, b, 0, 1);
    }
    static native boolean isLinuxThreads();
    static native int getLinuxThreadsManager(int pid) throws IOException;
    static native void sendQuitToChildrenOf(int pid) throws IOException;
    static native void sendQuitTo(int pid) throws IOException;
    static native void checkPermissions(String path) throws IOException;
    static native int socket() throws IOException;
    static native void connect(int fd, String path) throws IOException;
    static native void close(int fd) throws IOException;
    static native int read(int fd, byte buf[], int off, int bufLen) throws IOException;
    static native void write(int fd, byte buf[], int off, int bufLen) throws IOException;
    static {
        System.loadLibrary("attach");
        isLinuxThreads = isLinuxThreads();
    }
}
