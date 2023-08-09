public class ByteTypeImpl extends PrimitiveTypeImpl implements ByteType {
    ByteTypeImpl(VirtualMachine vm) {
        super(vm);
    }
    public String signature() {
        return "B";
    }
    PrimitiveValue convert(PrimitiveValue value) throws InvalidTypeException {
        return vm.mirrorOf(((PrimitiveValueImpl)value).checkedByteValue());
    }
}
