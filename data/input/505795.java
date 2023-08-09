public class Runtime {
    private static final Runtime mRuntime = new Runtime();
    private final String[] mLibPaths;
    private List<Thread> shutdownHooks = new ArrayList<Thread>();
    private static boolean finalizeOnExit;
    private boolean shuttingDown;
    private boolean tracingMethods;
    private Runtime(){
        String pathList = System.getProperty("java.library.path", ".");
        String pathSep = System.getProperty("path.separator", ":");
        String fileSep = System.getProperty("file.separator", "/");
        mLibPaths = pathList.split(pathSep);
        int i;
        if (false)
            System.out.println("Runtime paths:");
        for (i = 0; i < mLibPaths.length; i++) {
            if (!mLibPaths[i].endsWith(fileSep))
                mLibPaths[i] += fileSep;
            if (false)
                System.out.println("  " + mLibPaths[i]);
        }
    }
    public Process exec(String[] progArray) throws java.io.IOException {
        return exec(progArray, null, null);
    }
    public Process exec(String[] progArray, String[] envp) throws java.io.IOException {
        return exec(progArray, envp, null);
    }
    public Process exec(String[] progArray, String[] envp, File directory) throws IOException {
        return ProcessManager.getInstance().exec(progArray, envp, directory, false);
    }
    public Process exec(String prog) throws java.io.IOException {
        return exec(prog, null, null);
    }
    public Process exec(String prog, String[] envp) throws java.io.IOException {
        return exec(prog, envp, null);
    }
    public Process exec(String prog, String[] envp, File directory) throws java.io.IOException {
        if (prog == null) {
            throw new NullPointerException();
        } else if (prog.length() == 0) {
            throw new IllegalArgumentException();
        }
        StringTokenizer tokenizer = new StringTokenizer(prog);
        int length = tokenizer.countTokens();
        String[] progArray = new String[length];
        for (int i = 0; i < length; i++) {
            progArray[i] = tokenizer.nextToken();
        }
        return exec(progArray, envp, directory);
    }
    public void exit(int code) {
        SecurityManager smgr = System.getSecurityManager();
        if (smgr != null) {
            smgr.checkExit(code);
        }
        synchronized(this) {
            if (!shuttingDown) {
                shuttingDown = true;
                Thread[] hooks;
                synchronized (shutdownHooks) {
                    hooks = new Thread[shutdownHooks.size()];
                    shutdownHooks.toArray(hooks);
                }
                for (int i = 0; i < hooks.length; i++) {
                    hooks[i].start();
                }
                for (Thread hook : hooks) {
                    try {
                        hook.join();
                    } catch (InterruptedException ex) {
                    }
                }
                if (finalizeOnExit) {
                    runFinalization(true);
                }
                nativeExit(code, true);
            }
        }
    }
    public native long freeMemory();
    public native void gc();
    public static Runtime getRuntime() {
        return mRuntime;
    }
    public void load(String pathName) {
        SecurityManager smgr = System.getSecurityManager();
        if (smgr != null) {
            smgr.checkLink(pathName);
        }
        load(pathName, VMStack.getCallingClassLoader());
    }
    void load(String filename, ClassLoader loader) {
        if (filename == null) {
            throw new NullPointerException("library path was null.");
        }
        if (!nativeLoad(filename, loader)) {
            throw new UnsatisfiedLinkError(
                    "Library " + filename + " not found");
        }
    }
    public void loadLibrary(String libName) {
        SecurityManager smgr = System.getSecurityManager();
        if (smgr != null) {
            smgr.checkLink(libName);
        }
        loadLibrary(libName, VMStack.getCallingClassLoader());
    }
    void loadLibrary(String libname, ClassLoader loader) {
        String filename;
        int i;
        if (loader != null) {
            filename = loader.findLibrary(libname);
            if (filename != null && nativeLoad(filename, loader))
                return;
        } else {
            filename = System.mapLibraryName(libname);
            for (i = 0; i < mLibPaths.length; i++) {
                if (false)
                    System.out.println("Trying " + mLibPaths[i] + filename);
                if (nativeLoad(mLibPaths[i] + filename, loader))
                    return;
            }
        }
        throw new UnsatisfiedLinkError("Library " + libname + " not found");
    }
    private static native void nativeExit(int code, boolean isExit);
    private static native boolean nativeLoad(String filename,
            ClassLoader loader);
    private native void runFinalization(boolean forced);
    public void runFinalization() {
        runFinalization(false);
    }
    @Deprecated
    public static void runFinalizersOnExit(boolean run) {
        SecurityManager smgr = System.getSecurityManager();
        if (smgr != null) {
            smgr.checkExit(0);
        }
        finalizeOnExit = run;
    }
    public native long totalMemory();
    public void traceInstructions(boolean enable) {
        return;
    }
    public void traceMethodCalls(boolean enable) {
        if (enable != tracingMethods) {
            if (enable) {
                VMDebug.startMethodTracing();
            } else {
                VMDebug.stopMethodTracing();
            }
            tracingMethods = enable;
        }
    }
    @Deprecated
    public InputStream getLocalizedInputStream(InputStream stream) {
        if (System.getProperty("file.encoding", "UTF-8").equals("UTF-8")) {
            return stream;
        }
        return new ReaderInputStream(stream);
    }
    @Deprecated
    public OutputStream getLocalizedOutputStream(OutputStream stream) {
        if (System.getProperty("file.encoding", "UTF-8").equals("UTF-8")) {
            return stream;
        }
        return new WriterOutputStream(stream );
    }
    public void addShutdownHook(Thread hook) {
        if (hook == null) {
            throw new NullPointerException("Hook may not be null.");
        }
        if (shuttingDown) {
            throw new IllegalStateException("VM already shutting down");
        }
        if (hook.hasBeenStarted) {
            throw new IllegalArgumentException("Hook has already been started");
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("shutdownHooks"));
        }
        synchronized (shutdownHooks) {
            if (shutdownHooks.contains(hook)) {
                throw new IllegalArgumentException("Hook already registered.");
            }
            shutdownHooks.add(hook);
        }
    }
    public boolean removeShutdownHook(Thread hook) {
        if (hook == null) {
            throw new NullPointerException("Hook may not be null.");
        }
        if (shuttingDown) {
            throw new IllegalStateException("VM already shutting down");
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("shutdownHooks"));
        }
        synchronized (shutdownHooks) {
            return shutdownHooks.remove(hook);
        }
    }
    public void halt(int code) {
        SecurityManager smgr = System.getSecurityManager();
        if (smgr != null) {
            smgr.checkExit(code);
        }
        nativeExit(code, false);
    }
    public int availableProcessors() {
        return 1;
    }
    public native long maxMemory();
}
class ReaderInputStream extends InputStream {
    private Reader reader;
    private Writer writer;
    ByteArrayOutputStream out = new ByteArrayOutputStream(256);
    private byte[] bytes;
    private int nextByte;
    private int numBytes;
    String encoding = System.getProperty("file.encoding", "UTF-8");
    public ReaderInputStream(InputStream stream) {
        try {
            reader = new InputStreamReader(stream, "UTF-8");
            writer = new OutputStreamWriter(out, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public int read() throws IOException {
        if (nextByte >= numBytes) {
            readBuffer();
        }
        return (numBytes < 0) ? -1 : bytes[nextByte++];
    }
    private void readBuffer() throws IOException {
        char[] chars = new char[128];
        int read = reader.read(chars);
        if (read < 0) {
            numBytes = read;
            return;
        }
        writer.write(chars, 0, read);
        writer.flush();
        bytes = out.toByteArray();
        numBytes = bytes.length;
        nextByte = 0;
    }        
}
class WriterOutputStream extends OutputStream {
    private Reader reader;
    private Writer writer;
    private PipedOutputStream out;
    private PipedInputStream pipe;
    private int numBytes;
    private String enc = System.getProperty("file.encoding", "UTF-8");
    public WriterOutputStream(OutputStream stream) {
        try {
            this.writer = new OutputStreamWriter(stream, enc);
            out = new PipedOutputStream();
            pipe = new PipedInputStream(out);
            this.reader = new InputStreamReader(pipe, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void write(int b) throws IOException {
        out.write(b);
        if( ++numBytes > 256) {
            flush();
            numBytes = 0;
        }
    }
    @Override
    public void flush() throws IOException {
        out.flush();
        char[] chars = new char[128];
        if (pipe.available() > 0) {
            int read = reader.read(chars);
            if (read > 0) {
                writer.write(chars, 0, read);
            }
        }
        writer.flush();
    }
    @Override
    public void close() throws IOException {
        out.close();
        flush();
        writer.close();
    }
}
