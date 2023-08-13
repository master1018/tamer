public class ProcAMD64ThreadContext extends AMD64ThreadContext {
    private ProcDebugger debugger;
    public ProcAMD64ThreadContext(ProcDebugger debugger) {
        super();
        this.debugger = debugger;
    }
    public void setRegisterAsAddress(int index, Address value) {
        setRegister(index, debugger.getAddressValue(value));
    }
    public Address getRegisterAsAddress(int index) {
        return debugger.newAddress(getRegister(index));
    }
}
