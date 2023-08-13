public class ProcAMD64ThreadFactory implements ProcThreadFactory {
    private ProcDebugger debugger;
    public ProcAMD64ThreadFactory(ProcDebugger debugger) {
        this.debugger = debugger;
    }
    public ThreadProxy createThreadWrapper(Address threadIdentifierAddr) {
        return new ProcAMD64Thread(debugger, threadIdentifierAddr);
    }
    public ThreadProxy createThreadWrapper(long id) {
        return new ProcAMD64Thread(debugger, id);
    }
}
