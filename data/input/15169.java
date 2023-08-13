public class SolarisVirtualMachine extends HotSpotVirtualMachine {
    private static final String tmpdir1 = System.getProperty("java.io.tmpdir");
    private static final String tmpdir =
        (tmpdir1.equals("/var/tmp") || tmpdir1.equals("/var/tmp/")) ? "/tmp" : tmpdir1;
    private int fd = -1;
    SolarisVirtualMachine(AttachProvider provider, String vmid)
        throws AttachNotSupportedException, IOException
    {
        super(provider, vmid);
        int pid;
        try {
            pid = Integer.parseInt(vmid);
        } catch (NumberFormatException x) {
            throw new AttachNotSupportedException("invalid process identifier");
        }
        try {
            fd = openDoor(pid);
        } catch (FileNotFoundException fnf1) {
            File f = createAttachFile(pid);
            try {
                sigquit(pid);
                int i = 0;
                long delay = 200;
                int retries = (int)(attachTimeout() / delay);
                do {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException x) { }
                    try {
                        fd = openDoor(pid);
                    } catch (FileNotFoundException fnf2) { }
                    i++;
                } while (i <= retries && fd == -1);
                if (fd == -1) {
                    throw new AttachNotSupportedException(
                        "Unable to open door: target process not responding or " +
                        "HotSpot VM not loaded");
                }
            } finally {
                f.delete();
            }
        }
        assert fd >= 0;
    }
    public void detach() throws IOException {
        synchronized (this) {
            if (fd != -1) {
                close(fd);
                fd = -1;
            }
        }
    }
    InputStream execute(String cmd, Object ... args) throws AgentLoadException, IOException {
        assert args.length <= 3;                
        int door;
        synchronized (this) {
            if (fd == -1) {
                throw new IOException("Detached from target VM");
            }
            door = fd;
        }
        int s = enqueue(door, cmd, args);
        assert s >= 0;                          
        SocketInputStream sis = new SocketInputStream(s);
        int completionStatus;
        try {
            completionStatus = readInt(sis);
        } catch (IOException ioe) {
            sis.close();
            throw ioe;
        }
        if (completionStatus != 0) {
            sis.close();
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
            return SolarisVirtualMachine.read(s, bs, off, len);
        }
        public void close() throws IOException {
            SolarisVirtualMachine.close(s);
        }
    }
    private int openDoor(int pid) throws IOException {
        String fn = ".java_pid" + pid;
        String path = "/proc/" + pid + "/cwd/" + fn;
        try {
            fd = open(path);
        } catch (FileNotFoundException fnf) {
            path = tmpdir + "/" + fn;
            fd = open(path);
        }
        try {
            checkPermissions(path);
        } catch (IOException ioe) {
            close(fd);
            throw ioe;
        }
        return fd;
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
    static native int open(String path) throws IOException;
    static native void close(int fd) throws IOException;
    static native int read(int fd, byte buf[], int off, int buflen) throws IOException;
    static native void checkPermissions(String path) throws IOException;
    static native void sigquit(int pid) throws IOException;
    static native int enqueue(int fd, String cmd, Object ... args)
        throws IOException;
    static {
        System.loadLibrary("attach");
    }
}
