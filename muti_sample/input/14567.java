public class SPARCIndirectCallInstruction extends SPARCJmplInstruction
    implements CallInstruction {
    public SPARCIndirectCallInstruction(SPARCRegisterIndirectAddress addr, SPARCRegister rd) {
        super("call", addr, rd);
    }
    protected String getDescription() {
        StringBuffer buf = new StringBuffer();
        buf.append(getName());
        buf.append(spaces);
        String addrStr = addr.toString();
        buf.append(addrStr.substring(1, addrStr.length() - 1));
        return buf.toString();
    }
}
