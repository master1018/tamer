public class FloatTypeImpl extends PrimitiveTypeImpl implements FloatType {
    FloatTypeImpl(VirtualMachine vm) {
        super(vm);
    }
    public String signature() {
        return "F";
    }
    PrimitiveValue convert(PrimitiveValue value) throws InvalidTypeException {
        return vm.mirrorOf(((PrimitiveValueImpl)value).checkedFloatValue());
    }
}
