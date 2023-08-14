final class ProcessManager {
    private static final int WAIT_STATUS_UNKNOWN = -1;
    private static final int WAIT_STATUS_NO_CHILDREN = -2;
    private static final int WAIT_STATUS_STRANGE_ERRNO = -3;
    static native void staticInitialize();
    static {
        staticInitialize();
    }
    private final Map<Integer, ProcessReference> processReferences
            = new HashMap<Integer, ProcessReference>();
    private final ProcessReferenceQueue referenceQueue
            = new ProcessReferenceQueue();
    private ProcessManager() {
        Thread processThread = new Thread(ProcessManager.class.getName()) {
            @Override
            public void run() {
                watchChildren();
            }
        };
        processThread.setDaemon(true);
        processThread.start();
    }
    private static native void kill(int pid) throws IOException;
    void cleanUp() {
        ProcessReference reference;
        while ((reference = referenceQueue.poll()) != null) {
            synchronized (processReferences) {
                processReferences.remove(reference.processId);
            }
        }
    }
    native void watchChildren();
    void onExit(int pid, int exitValue) {
        ProcessReference processReference = null;
        synchronized (processReferences) {
            cleanUp();
            if (pid >= 0) {
                processReference = processReferences.remove(pid);
            } else if (exitValue == WAIT_STATUS_NO_CHILDREN) {
                if (processReferences.isEmpty()) {
                    try {
                        processReferences.wait();
                    } catch (InterruptedException ex) {
                        throw new AssertionError("unexpected interrupt");
                    }
                } else {
                }
            } else {
                throw new AssertionError("unexpected wait() behavior");
            }
        }
        if (processReference != null) {
            ProcessImpl process = processReference.get();
            if (process != null) {
                process.setExitValue(exitValue);
            }
        }
    }
    static native int exec(String[] command, String[] environment,
            String workingDirectory, FileDescriptor in, FileDescriptor out,
            FileDescriptor err, boolean redirectErrorStream) throws IOException;
    Process exec(String[] taintedCommand, String[] taintedEnvironment, File workingDirectory,
            boolean redirectErrorStream) throws IOException {
        if (taintedCommand == null) {
            throw new NullPointerException();
        }
        if (taintedCommand.length == 0) {
            throw new IndexOutOfBoundsException();
        }
        String[] command = taintedCommand.clone();
        String[] environment = taintedEnvironment != null ? taintedEnvironment.clone() : null;
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            securityManager.checkExec(command[0]);
        }
        for (String arg : command) {
            if (arg == null) {
                throw new NullPointerException();
            }
        }
        if (environment != null) {
            for (String env : environment) {
                if (env == null) {
                    throw new NullPointerException();
                }
            }
        }
        FileDescriptor in = new FileDescriptor();
        FileDescriptor out = new FileDescriptor();
        FileDescriptor err = new FileDescriptor();
        String workingPath = (workingDirectory == null)
                ? null
                : workingDirectory.getPath();
        synchronized (processReferences) {
            int pid;
            try {
                pid = exec(command, environment, workingPath, in, out, err, redirectErrorStream);
            } catch (IOException e) {
                IOException wrapper = new IOException("Error running exec()." 
                        + " Command: " + Arrays.toString(command)
                        + " Working Directory: " + workingDirectory
                        + " Environment: " + Arrays.toString(environment));
                wrapper.initCause(e);
                throw wrapper;
            }
            ProcessImpl process = new ProcessImpl(pid, in, out, err);
            ProcessReference processReference
                    = new ProcessReference(process, referenceQueue);
            processReferences.put(pid, processReference);
            processReferences.notifyAll();
            return process;
        }
    }
    static class ProcessImpl extends Process {
        final int id;
        final InputStream errorStream;
        final InputStream inputStream;
        final OutputStream outputStream;
        Integer exitValue = null;
        final Object exitValueMutex = new Object();
        ProcessImpl(int id, FileDescriptor in, FileDescriptor out,
                FileDescriptor err) {
            this.id = id;
            this.errorStream = new ProcessInputStream(err);
            this.inputStream = new ProcessInputStream(in);
            this.outputStream = new ProcessOutputStream(out);
        }
        public void destroy() {
            try {
                kill(this.id);
            } catch (IOException e) {
                Logger.getLogger(Runtime.class.getName()).log(Level.FINE,
                        "Failed to destroy process " + id + ".", e);
            }
        }
        public int exitValue() {
            synchronized (exitValueMutex) {
                if (exitValue == null) {
                    throw new IllegalThreadStateException(
                            "Process has not yet terminated.");
                }
                return exitValue;
            }
        }
        public InputStream getErrorStream() {
            return this.errorStream;
        }
        public InputStream getInputStream() {
            return this.inputStream;
        }
        public OutputStream getOutputStream() {
            return this.outputStream;
        }
        public int waitFor() throws InterruptedException {
            synchronized (exitValueMutex) {
                while (exitValue == null) {
                    exitValueMutex.wait();
                }
                return exitValue;
            }
        }
        void setExitValue(int exitValue) {
            synchronized (exitValueMutex) {
                this.exitValue = exitValue;
                exitValueMutex.notifyAll();
            }
        }
        @Override
        public String toString() {
            return "Process[id=" + id + "]";  
        }
    }
    static class ProcessReference extends WeakReference<ProcessImpl> {
        final int processId;
        public ProcessReference(ProcessImpl referent,
                ProcessReferenceQueue referenceQueue) {
            super(referent, referenceQueue);
            this.processId = referent.id;
        }
    }
    static class ProcessReferenceQueue extends ReferenceQueue<ProcessImpl> {
        @Override
        public ProcessReference poll() {
            Object reference = super.poll();
            return (ProcessReference) reference;
        }
    }
    static final ProcessManager instance = new ProcessManager();
    static ProcessManager getInstance() {
        return instance;
    }
    private static class ProcessInputStream extends FileInputStream {
        private FileDescriptor fd;
        private ProcessInputStream(FileDescriptor fd) {
            super(fd);
            this.fd = fd;
        }
        @Override
        public void close() throws IOException {
            try {
                super.close();
            } finally {
                synchronized (this) {
                    if (fd != null && fd.valid()) {
                        try {
                            ProcessManager.close(fd);
                        } finally {
                            fd = null;
                        }
                    }
                }
            }
        }
    }
    private static class ProcessOutputStream extends FileOutputStream {
        private FileDescriptor fd;
        private ProcessOutputStream(FileDescriptor fd) {
            super(fd);
            this.fd = fd;
        }
        @Override
        public void close() throws IOException {
            try {
                super.close();
            } finally {
                synchronized (this) {
                    if (fd != null && fd.valid()) {
                        try {
                            ProcessManager.close(fd);
                        } finally {
                            fd = null;
                        }
                    }
                }
            }
        }
    }
    private static native void close(FileDescriptor fd) throws IOException;
}
