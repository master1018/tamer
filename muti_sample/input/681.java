public class BooleanTypeImpl extends PrimitiveTypeImpl implements BooleanType {
    BooleanTypeImpl(VirtualMachine vm) {
        super(vm);
    }
    public String signature() {
        return "Z";
    }
    PrimitiveValue convert(PrimitiveValue value) throws InvalidTypeException {
        return vm.mirrorOf(((PrimitiveValueImpl)value).checkedBooleanValue());
    }
}
