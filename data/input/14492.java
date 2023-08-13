public class AddressingDispositionException extends RuntimeException {
    private short expectedAddrDisp = KeyAddr.value;
    public AddressingDispositionException(short expectedAddrDisp) {
        this.expectedAddrDisp = expectedAddrDisp;
    }
    public short expectedAddrDisp() {
        return this.expectedAddrDisp;
    }
}
