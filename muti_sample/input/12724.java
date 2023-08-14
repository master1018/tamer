public class VoidValueImpl extends ValueImpl implements VoidValue {
    VoidValueImpl(VirtualMachine aVm) {
        super(aVm);
    }
    public boolean equals(Object obj) {
        return (obj != null) && (obj instanceof VoidValue) && super.equals(obj);
    }
    public int hashCode() {
        return type().hashCode();
    }
    public Type type() {
        return vm.theVoidType();
    }
    ValueImpl prepareForAssignmentTo(ValueContainer destination)
                    throws InvalidTypeException {
        throw new InvalidTypeException();
    }
    public String toString() {
        return "<void value>";
    }
}
