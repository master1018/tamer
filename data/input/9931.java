public class ArrayTypeImpl extends ReferenceTypeImpl implements ArrayType {
  protected ArrayTypeImpl(VirtualMachine aVm, ArrayKlass aRef) {
        super(aVm, aRef);
    }
    public ArrayReference newInstance(int length) {
        vm.throwNotReadOnlyException("ArrayType.newInstance(int)");
        return null;
    }
    public String componentSignature() {
        return signature().substring(1); 
    }
    public String componentTypeName() {
        JNITypeParser parser = new JNITypeParser(componentSignature());
        return parser.typeName();
    }
    public ClassLoaderReference classLoader() {
        if (ref() instanceof TypeArrayKlass) {
            return null;
        } else {
            Klass bottomKlass = ((ObjArrayKlass)ref()).getBottomKlass();
            if (bottomKlass instanceof TypeArrayKlass) {
                return null;
            } else {
                Instance xx = (Instance)(((InstanceKlass) bottomKlass).getClassLoader());
                return vm.classLoaderMirror(xx);
            }
        }
    }
    void addVisibleMethods(Map methodMap) {
    }
    List getAllMethods() {
        return new ArrayList(0);
    }
    public Type componentType() throws ClassNotLoadedException {
        ArrayKlass k = (ArrayKlass) ref();
        if (k instanceof ObjArrayKlass) {
            Klass elementKlass = ((ObjArrayKlass)k).getElementKlass();
            if (elementKlass == null) {
                throw new ClassNotLoadedException(componentSignature());
            } else {
                return vm.referenceType(elementKlass);
            }
        } else {
            return vm.primitiveTypeMirror(signature().charAt(1));
        }
    }
    static boolean isComponentAssignable(Type destination, Type source) {
        if (source instanceof PrimitiveType) {
            return source.equals(destination);
        } else {
           if (destination instanceof PrimitiveType) {
                return false;
            }
            ReferenceTypeImpl refSource = (ReferenceTypeImpl)source;
            ReferenceTypeImpl refDestination = (ReferenceTypeImpl)destination;
            return refSource.isAssignableTo(refDestination);
        }
    }
    boolean isAssignableTo(ReferenceType destType) {
        if (destType instanceof ArrayType) {
            try {
                Type destComponentType = ((ArrayType)destType).componentType();
                return isComponentAssignable(destComponentType, componentType());
            } catch (ClassNotLoadedException e) {
                return false;
            }
        } else {
            Symbol typeName = ((ReferenceTypeImpl)destType).typeNameAsSymbol();
            if (destType instanceof InterfaceType) {
                return typeName.equals(vm.javaLangCloneable()) ||
                       typeName.equals(vm.javaIoSerializable());
            } else {
                return typeName.equals(vm.javaLangObject());
            }
        }
    }
    List inheritedTypes() {
        return new ArrayList(0);
    }
    int getModifiers() {
        try {
            Type t = componentType();
            if (t instanceof PrimitiveType) {
                return VMModifiers.FINAL | VMModifiers.PUBLIC;
            } else {
                ReferenceType rt = (ReferenceType)t;
                return rt.modifiers();
            }
        } catch (ClassNotLoadedException cnle) {
            cnle.printStackTrace();
        }
        return -1;
    }
    public String toString() {
       return "array class " + name() + " (" + loaderString() + ")";
    }
    public boolean isPrepared() { return true; }
    public boolean isVerified() { return true; }
    public boolean isInitialized() { return true; }
    public boolean failedToInitialize() { return false; }
    public boolean isAbstract() { return false; }
    public boolean isFinal() { return true; }
}
