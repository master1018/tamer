public final class Perf {
    private static Perf instance;
    private static final int PERF_MODE_RO = 0;
    private static final int PERF_MODE_RW = 1;
    private Perf() { }    
    public static class GetPerfAction implements PrivilegedAction<Perf>
    {
        public Perf run() {
            return getPerf();
        }
    }
    public static Perf getPerf()
    {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            Permission perm = new RuntimePermission("sun.misc.Perf.getPerf");
            security.checkPermission(perm);
        }
        return instance;
    }
    public ByteBuffer attach(int lvmid, String mode)
           throws IllegalArgumentException, IOException
    {
        if (mode.compareTo("r") == 0) {
            return attachImpl(null, lvmid, PERF_MODE_RO);
        }
        else if (mode.compareTo("rw") == 0) {
            return attachImpl(null, lvmid, PERF_MODE_RW);
        }
        else {
            throw new IllegalArgumentException("unknown mode");
        }
    }
    public ByteBuffer attach(String user, int lvmid, String mode)
           throws IllegalArgumentException, IOException
    {
        if (mode.compareTo("r") == 0) {
            return attachImpl(user, lvmid, PERF_MODE_RO);
        }
        else if (mode.compareTo("rw") == 0) {
            return attachImpl(user, lvmid, PERF_MODE_RW);
        }
        else {
            throw new IllegalArgumentException("unknown mode");
        }
    }
    private ByteBuffer attachImpl(String user, int lvmid, int mode)
            throws IllegalArgumentException, IOException
    {
        final ByteBuffer b = attach(user, lvmid, mode);
        if (lvmid == 0) {
            return b;
        }
        else {
            final ByteBuffer dup = b.duplicate();
            Cleaner.create(dup, new Runnable() {
                    public void run() {
                        try {
                            instance.detach(b);
                        }
                        catch (Throwable th) {
                            assert false : th.toString();
                        }
                    }
                });
            return dup;
        }
    }
    private native ByteBuffer attach(String user, int lvmid, int mode)
                   throws IllegalArgumentException, IOException;
    private native void detach(ByteBuffer bb);
    public native ByteBuffer createLong(String name, int variability,
                                        int units, long value);
    public ByteBuffer createString(String name, int variability,
                                   int units, String value, int maxLength)
    {
        byte[] v = getBytes(value);
        byte[] v1 = new byte[v.length+1];
        System.arraycopy(v, 0, v1, 0, v.length);
        v1[v.length] = '\0';
        return createByteArray(name, variability, units, v1, Math.max(v1.length, maxLength));
    }
    public ByteBuffer createString(String name, int variability,
                                   int units, String value)
    {
        byte[] v = getBytes(value);
        byte[] v1 = new byte[v.length+1];
        System.arraycopy(v, 0, v1, 0, v.length);
        v1[v.length] = '\0';
        return createByteArray(name, variability, units, v1, v1.length);
    }
    public native ByteBuffer createByteArray(String name, int variability,
                                             int units, byte[] value,
                                             int maxLength);
    private static byte[] getBytes(String s)
    {
        byte[] bytes = null;
        try {
            bytes = s.getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException e) {
        }
        return bytes;
    }
    public native long highResCounter();
    public native long highResFrequency();
    private static native void registerNatives();
    static {
        registerNatives();
        instance = new Perf();
    }
}
