public class LongTypeImpl extends PrimitiveTypeImpl implements LongType {
    LongTypeImpl(VirtualMachine vm) {
        super(vm);
    }
    public String signature() {
        return "J";
    }
    PrimitiveValue convert(PrimitiveValue value) throws InvalidTypeException {
        return vm.mirrorOf(((PrimitiveValueImpl)value).checkedLongValue());
    }
}
