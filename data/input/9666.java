public class ReliableLog {
    public final static int PreferredMajorVersion = 0;
    public final static int PreferredMinorVersion = 2;
    private boolean Debug = false;
    private static String snapshotPrefix = "Snapshot.";
    private static String logfilePrefix = "Logfile.";
    private static String versionFile = "Version_Number";
    private static String newVersionFile = "New_Version_Number";
    private static int    intBytes = 4;
    private static long   diskPageSize = 512;
    private File dir;                   
    private int version = 0;            
    private String logName = null;
    private LogFile log = null;
    private long snapshotBytes = 0;
    private long logBytes = 0;
    private int logEntries = 0;
    private long lastSnapshot = 0;
    private long lastLog = 0;
    private LogHandler handler;
    private final byte[] intBuf = new byte[4];
    private int majorFormatVersion = 0;
    private int minorFormatVersion = 0;
    private static final Constructor<? extends LogFile>
        logClassConstructor = getLogClassConstructor();
    public ReliableLog(String dirPath,
                     LogHandler handler,
                     boolean pad)
        throws IOException
    {
        super();
        this.Debug = AccessController.doPrivileged(
            new GetBooleanAction("sun.rmi.log.debug")).booleanValue();
        dir = new File(dirPath);
        if (!(dir.exists() && dir.isDirectory())) {
            if (!dir.mkdir()) {
                throw new IOException("could not create directory for log: " +
                                      dirPath);
            }
        }
        this.handler = handler;
        lastSnapshot = 0;
        lastLog = 0;
        getVersion();
        if (version == 0) {
            try {
                snapshot(handler.initialSnapshot());
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw new IOException("initial snapshot failed with " +
                                      "exception: " + e);
            }
        }
    }
    public ReliableLog(String dirPath,
                     LogHandler handler)
        throws IOException
    {
        this(dirPath, handler, false);
    }
    public synchronized Object recover()
        throws IOException
    {
        if (Debug)
            System.err.println("log.debug: recover()");
        if (version == 0)
            return null;
        Object snapshot;
        String fname = versionName(snapshotPrefix);
        File snapshotFile = new File(fname);
        InputStream in =
                new BufferedInputStream(new FileInputStream(snapshotFile));
        if (Debug)
            System.err.println("log.debug: recovering from " + fname);
        try {
            try {
                snapshot = handler.recover(in);
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                if (Debug)
                    System.err.println("log.debug: recovery failed: " + e);
                throw new IOException("log recover failed with " +
                                      "exception: " + e);
            }
            snapshotBytes = snapshotFile.length();
        } finally {
            in.close();
        }
        return recoverUpdates(snapshot);
    }
    public synchronized void update(Object value) throws IOException {
        update(value, true);
    }
    public synchronized void update(Object value, boolean forceToDisk)
        throws IOException
    {
        if (log == null) {
            throw new IOException("log is inaccessible, " +
                "it may have been corrupted or closed");
        }
        long entryStart = log.getFilePointer();
        boolean spansBoundary = log.checkSpansBoundary(entryStart);
        writeInt(log, spansBoundary? 1<<31 : 0);
        try {
            handler.writeUpdate(new LogOutputStream(log), value);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw (IOException)
                new IOException("write update failed").initCause(e);
        }
        log.sync();
        long entryEnd = log.getFilePointer();
        int updateLen = (int) ((entryEnd - entryStart) - intBytes);
        log.seek(entryStart);
        if (spansBoundary) {
            writeInt(log, updateLen | 1<<31);
            log.sync();
            log.seek(entryStart);
            log.writeByte(updateLen >> 24);
            log.sync();
        } else {
            writeInt(log, updateLen);
            log.sync();
        }
        log.seek(entryEnd);
        logBytes = entryEnd;
        lastLog = System.currentTimeMillis();
        logEntries++;
    }
    private static Constructor<? extends LogFile>
        getLogClassConstructor() {
        String logClassName = AccessController.doPrivileged(
            new GetPropertyAction("sun.rmi.log.class"));
        if (logClassName != null) {
            try {
                ClassLoader loader =
                    AccessController.doPrivileged(
                        new PrivilegedAction<ClassLoader>() {
                            public ClassLoader run() {
                               return ClassLoader.getSystemClassLoader();
                            }
                        });
                Class cl = loader.loadClass(logClassName);
                if (LogFile.class.isAssignableFrom(cl)) {
                    return cl.getConstructor(String.class, String.class);
                }
            } catch (Exception e) {
                System.err.println("Exception occurred:");
                e.printStackTrace();
            }
        }
        return null;
    }
    public synchronized void snapshot(Object value)
        throws IOException
    {
        int oldVersion = version;
        incrVersion();
        String fname = versionName(snapshotPrefix);
        File snapshotFile = new File(fname);
        FileOutputStream out = new FileOutputStream(snapshotFile);
        try {
            try {
                handler.snapshot(out, value);
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw new IOException("snapshot failed", e);
            }
            lastSnapshot = System.currentTimeMillis();
        } finally {
            out.close();
            snapshotBytes = snapshotFile.length();
        }
        openLogFile(true);
        writeVersionFile(true);
        commitToNewVersion();
        deleteSnapshot(oldVersion);
        deleteLogFile(oldVersion);
    }
    public synchronized void close() throws IOException {
        if (log == null) return;
        try {
            log.close();
        } finally {
            log = null;
        }
    }
    public long snapshotSize() {
        return snapshotBytes;
    }
    public long logSize() {
        return logBytes;
    }
    private void writeInt(DataOutput out, int val)
        throws IOException
    {
        intBuf[0] = (byte) (val >> 24);
        intBuf[1] = (byte) (val >> 16);
        intBuf[2] = (byte) (val >> 8);
        intBuf[3] = (byte) val;
        out.write(intBuf);
    }
    private String fName(String name) {
        return dir.getPath() + File.separator + name;
    }
    private String versionName(String name) {
        return versionName(name, 0);
    }
    private String versionName(String prefix, int ver) {
        ver = (ver == 0) ? version : ver;
        return fName(prefix) + String.valueOf(ver);
    }
    private void incrVersion() {
        do { version++; } while (version==0);
    }
    private void deleteFile(String name) throws IOException {
        File f = new File(name);
        if (!f.delete())
            throw new IOException("couldn't remove file: " + name);
    }
    private void deleteNewVersionFile() throws IOException {
        deleteFile(fName(newVersionFile));
    }
    private void deleteSnapshot(int ver) throws IOException {
        if (ver == 0) return;
        deleteFile(versionName(snapshotPrefix, ver));
    }
    private void deleteLogFile(int ver) throws IOException {
        if (ver == 0) return;
        deleteFile(versionName(logfilePrefix, ver));
    }
    private void openLogFile(boolean truncate) throws IOException {
        try {
            close();
        } catch (IOException e) { 
        }
        logName = versionName(logfilePrefix);
        try {
            log = (logClassConstructor == null ?
                   new LogFile(logName, "rw") :
                   logClassConstructor.newInstance(logName, "rw"));
        } catch (Exception e) {
            throw (IOException) new IOException(
                "unable to construct LogFile instance").initCause(e);
        }
        if (truncate) {
            initializeLogFile();
        }
    }
    private void initializeLogFile()
        throws IOException
    {
        log.setLength(0);
        majorFormatVersion = PreferredMajorVersion;
        writeInt(log, PreferredMajorVersion);
        minorFormatVersion = PreferredMinorVersion;
        writeInt(log, PreferredMinorVersion);
        logBytes = intBytes * 2;
        logEntries = 0;
    }
    private void writeVersionFile(boolean newVersion) throws IOException {
        String name;
        if (newVersion) {
            name = newVersionFile;
        } else {
            name = versionFile;
        }
        DataOutputStream out =
            new DataOutputStream(new FileOutputStream(fName(name)));
        writeInt(out, version);
        out.close();
    }
    private void createFirstVersion() throws IOException {
        version = 0;
        writeVersionFile(false);
    }
    private void commitToNewVersion() throws IOException {
        writeVersionFile(false);
        deleteNewVersionFile();
    }
    private int readVersion(String name) throws IOException {
        DataInputStream in = new DataInputStream(new FileInputStream(name));
        try {
            return in.readInt();
        } finally {
            in.close();
        }
    }
    private void getVersion() throws IOException {
        try {
            version = readVersion(fName(newVersionFile));
            commitToNewVersion();
        } catch (IOException e) {
            try {
                deleteNewVersionFile();
            }
            catch (IOException ex) {
            }
            try {
                version = readVersion(fName(versionFile));
            }
            catch (IOException ex) {
                createFirstVersion();
            }
        }
    }
    private Object recoverUpdates(Object state)
        throws IOException
    {
        logBytes = 0;
        logEntries = 0;
        if (version == 0) return state;
        String fname = versionName(logfilePrefix);
        InputStream in =
                new BufferedInputStream(new FileInputStream(fname));
        DataInputStream dataIn = new DataInputStream(in);
        if (Debug)
            System.err.println("log.debug: reading updates from " + fname);
        try {
            majorFormatVersion = dataIn.readInt(); logBytes += intBytes;
            minorFormatVersion = dataIn.readInt(); logBytes += intBytes;
        } catch (EOFException e) {
            openLogFile(true);  
            in = null;
        }
        if (majorFormatVersion != PreferredMajorVersion) {
            if (Debug) {
                System.err.println("log.debug: major version mismatch: " +
                        majorFormatVersion + "." + minorFormatVersion);
            }
            throw new IOException("Log file " + logName + " has a " +
                                  "version " + majorFormatVersion +
                                  "." + minorFormatVersion +
                                  " format, and this implementation " +
                                  " understands only version " +
                                  PreferredMajorVersion + "." +
                                  PreferredMinorVersion);
        }
        try {
            while (in != null) {
                int updateLen = 0;
                try {
                    updateLen = dataIn.readInt();
                } catch (EOFException e) {
                    if (Debug)
                        System.err.println("log.debug: log was sync'd cleanly");
                    break;
                }
                if (updateLen <= 0) {
                    if (Debug) {
                        System.err.println(
                            "log.debug: last update incomplete, " +
                            "updateLen = 0x" +
                            Integer.toHexString(updateLen));
                    }
                    break;
                }
                if (in.available() < updateLen) {
                    if (Debug)
                        System.err.println("log.debug: log was truncated");
                    break;
                }
                if (Debug)
                    System.err.println("log.debug: rdUpdate size " + updateLen);
                try {
                    state = handler.readUpdate(new LogInputStream(in, updateLen),
                                          state);
                } catch (IOException e) {
                    throw e;
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new IOException("read update failed with " +
                                          "exception: " + e);
                }
                logBytes += (intBytes + updateLen);
                logEntries++;
            } 
        } finally {
            if (in != null)
                in.close();
        }
        if (Debug)
            System.err.println("log.debug: recovered updates: " + logEntries);
        openLogFile(false);
        if (log == null) {
            throw new IOException("rmid's log is inaccessible, " +
                "it may have been corrupted or closed");
        }
        log.seek(logBytes);
        log.setLength(logBytes);
        return state;
    }
    public static class LogFile extends RandomAccessFile {
        private final FileDescriptor fd;
        public LogFile(String name, String mode)
            throws FileNotFoundException, IOException
        {
            super(name, mode);
            this.fd = getFD();
        }
        protected void sync() throws IOException {
            fd.sync();
        }
        protected boolean checkSpansBoundary(long fp) {
            return  fp % 512 > 508;
        }
    }
}
