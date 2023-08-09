public class DoubleTypeImpl extends PrimitiveTypeImpl implements DoubleType {
    DoubleTypeImpl(VirtualMachine vm) {
        super(vm);
    }
    public String signature() {
        return "D";
    }
    PrimitiveValue convert(PrimitiveValue value) throws InvalidTypeException {
        return vm.mirrorOf(((PrimitiveValueImpl)value).checkedDoubleValue());
    }
}
