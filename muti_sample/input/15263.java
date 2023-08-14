class LinuxThread implements ThreadProxy {
    private LinuxDebugger debugger;
    private int           lwp_id;
    LinuxThread(LinuxDebugger debugger, Address addr) {
        this.debugger = debugger;
        this.lwp_id = (int) addr.getCIntegerAt(0, 4, true);
    }
    LinuxThread(LinuxDebugger debugger, long id) {
        this.debugger = debugger;
        this.lwp_id = (int) id;
    }
    public boolean equals(Object obj) {
        if ((obj == null) || !(obj instanceof LinuxThread)) {
            return false;
        }
        return (((LinuxThread) obj).lwp_id == lwp_id);
    }
    public int hashCode() {
        return lwp_id;
    }
    public String toString() {
        return Integer.toString(lwp_id);
    }
    public ThreadContext getContext() throws IllegalThreadStateException {
        long[] data = debugger.getThreadIntegerRegisterSet(lwp_id);
        ThreadContext context = LinuxThreadContextFactory.createThreadContext(debugger);
        for (int i = 0; i < data.length; i++) {
            context.setRegister(i, data[i]);
        }
        return context;
    }
    public boolean canSetContext() throws DebuggerException {
        return false;
    }
    public void setContext(ThreadContext context)
      throws IllegalThreadStateException, DebuggerException {
        throw new DebuggerException("Unimplemented");
    }
}
