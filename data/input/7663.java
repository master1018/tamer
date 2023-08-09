public class CharTypeImpl extends PrimitiveTypeImpl implements CharType {
    CharTypeImpl(VirtualMachine vm) {
        super(vm);
    }
    public String signature() {
        return "C";
    }
    PrimitiveValue convert(PrimitiveValue value) throws InvalidTypeException {
        return vm.mirrorOf(((PrimitiveValueImpl)value).checkedCharValue());
    }
}
