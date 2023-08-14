public class ArrayReferenceImpl extends ObjectReferenceImpl
    implements ArrayReference
{
    private int length;
    ArrayReferenceImpl(VirtualMachine aVm, sun.jvm.hotspot.oops.Array aRef) {
        super(aVm, aRef);
        length = (int) aRef.getLength();
    }
    ArrayTypeImpl arrayType() {
        return (ArrayTypeImpl)type();
    }
    public int length() {
        return length;
    }
    public Value getValue(int index) {
        List list = getValues(index, 1);
        return (Value)list.get(0);
    }
    public List getValues() {
        return getValues(0, -1);
    }
    private void validateArrayAccess(int index, int len) {
        if ((index < 0) || (index > length())) {
            throw new IndexOutOfBoundsException(
                        "Invalid array index: " + index);
        }
        if (len < 0) {
            throw new IndexOutOfBoundsException(
                        "Invalid array range length: " + len);
        }
        if (index + len > length()) {
            throw new IndexOutOfBoundsException(
                        "Invalid array range: " +
                        index + " to " + (index + len - 1));
        }
    }
    public List getValues(int index, int len) {
        if (len == -1) { 
           len = length() - index;
        }
        validateArrayAccess(index, len);
        List vals = new ArrayList();
        if (len == 0) {
            return vals;
        }
        sun.jvm.hotspot.oops.TypeArray typeArray = null;
        sun.jvm.hotspot.oops.ObjArray objArray = null;
        if (ref() instanceof sun.jvm.hotspot.oops.TypeArray) {
            typeArray = (sun.jvm.hotspot.oops.TypeArray)ref();
        } else if (ref() instanceof sun.jvm.hotspot.oops.ObjArray) {
            objArray = (sun.jvm.hotspot.oops.ObjArray)ref();
        } else {
            throw new RuntimeException("should not reach here");
        }
        char c = arrayType().componentSignature().charAt(0);
        BasicType variableType = BasicType.charToBasicType(c);
        final int limit = index + len;
        for (int ii = index; ii < limit; ii++) {
            ValueImpl valueImpl;
            if (variableType == BasicType.T_BOOLEAN) {
                valueImpl = (BooleanValueImpl) vm.mirrorOf(typeArray.getBooleanAt(ii));
            } else if (variableType == BasicType.T_CHAR) {
                valueImpl = (CharValueImpl) vm.mirrorOf(typeArray.getCharAt(ii));
            } else if (variableType == BasicType.T_FLOAT) {
                valueImpl = (FloatValueImpl) vm.mirrorOf(typeArray.getFloatAt(ii));
            } else if (variableType == BasicType.T_DOUBLE) {
                valueImpl =  (DoubleValueImpl) vm.mirrorOf(typeArray.getDoubleAt(ii));
            } else if (variableType == BasicType.T_BYTE) {
                valueImpl =  (ByteValueImpl) vm.mirrorOf(typeArray.getByteAt(ii));
            } else if (variableType == BasicType.T_SHORT) {
                valueImpl =  (ShortValueImpl) vm.mirrorOf(typeArray.getShortAt(ii));
            } else if (variableType == BasicType.T_INT) {
                valueImpl =  (IntegerValueImpl) vm.mirrorOf(typeArray.getIntAt(ii));
            } else if (variableType == BasicType.T_LONG) {
                valueImpl =  (LongValueImpl) vm.mirrorOf(typeArray.getLongAt(ii));
            } else if (variableType == BasicType.T_OBJECT) {
                valueImpl = (ObjectReferenceImpl) vm.objectMirror(objArray.getObjAt(ii));
            } else if (variableType == BasicType.T_ARRAY) {
                valueImpl = (ArrayReferenceImpl) vm.arrayMirror((Array) objArray.getObjAt(ii));
            } else {
                throw new RuntimeException("should not reach here");
            }
            vals.add (valueImpl);
        }
        return vals;
    }
    public void setValue(int index, Value value)
            throws InvalidTypeException,
                   ClassNotLoadedException {
        vm.throwNotReadOnlyException("ArrayReference.setValue(...)");
    }
    public void setValues(List values)
            throws InvalidTypeException,
                   ClassNotLoadedException {
        setValues(0, values, 0, -1);
    }
    public void setValues(int index, List values,
                          int srcIndex, int length)
            throws InvalidTypeException,
                   ClassNotLoadedException {
        vm.throwNotReadOnlyException("ArrayReference.setValue(...)");
    }
    public String toString() {
        return "instance of " + arrayType().componentTypeName() +
               "[" + length() + "] (id=" + uniqueID() + ")";
    }
}
