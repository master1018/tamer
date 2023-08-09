public class X86RegisterDirectAddress extends Address {
    final private X86Register base;
    public X86RegisterDirectAddress(X86Register base) {
        this.base = base;
    }
    public String toString() {
        return base.toString();
    }
}
