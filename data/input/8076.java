public class VoidTypeImpl extends TypeImpl implements VoidType {
    VoidTypeImpl(VirtualMachine vm) {
        super(vm);
    }
    public String signature() {
        return "V";
    }
    public String toString() {
        return name();
    }
}
