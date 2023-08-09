public class ArrayType extends Type {
    private Type type;
    private int arrayDimension;
    private String brackets;
    private String bracketsSig;
    public static ArrayType forArray(   sun.tools.java.Type theType,
                                        ContextStack stack) {
        ArrayType result = null;
        sun.tools.java.Type arrayType = theType;
        if (arrayType.getTypeCode() == TC_ARRAY) {
            while (arrayType.getTypeCode() == TC_ARRAY) {
                arrayType = arrayType.getElementType();
            }
            Type existing = getType(theType,stack);
            if (existing != null) {
                if (!(existing instanceof ArrayType)) return null; 
                return (ArrayType) existing;
            }
            Type temp = CompoundType.makeType(arrayType,null,stack);
            if (temp != null) {
                result = new ArrayType(stack,temp,theType.getArrayDimension());
                putType(theType,result,stack);
                stack.push(result);
                stack.pop(true);
            }
        }
        return result;
    }
    public String getSignature() {
        return bracketsSig + type.getSignature();
    }
    public Type getElementType () {
        return type;
    }
    public int getArrayDimension () {
        return arrayDimension;
    }
    public String getArrayBrackets () {
        return brackets;
    }
    public String toString () {
        return getQualifiedName() + brackets;
    }
    public String getTypeDescription () {
        return "Array of " + type.getTypeDescription();
    }
    public String getTypeName ( boolean useQualifiedNames,
                                boolean useIDLNames,
                                boolean globalIDLNames) {
        if (useIDLNames) {
            return super.getTypeName(useQualifiedNames,useIDLNames,globalIDLNames);
        } else {
            return super.getTypeName(useQualifiedNames,useIDLNames,globalIDLNames) + brackets;
        }
    }
    protected void swapInvalidTypes () {
        if (type.getStatus() != STATUS_VALID) {
            type = getValidType(type);
        }
    }
    protected boolean addTypes (int typeCodeFilter,
                                HashSet checked,
                                Vector matching) {
        boolean result = super.addTypes(typeCodeFilter,checked,matching);
        if (result) {
            getElementType().addTypes(typeCodeFilter,checked,matching);
        }
        return result;
    }
    private ArrayType(ContextStack stack, Type type, int arrayDimension) {
        super(stack,TYPE_ARRAY);
        this.type = type;
        this.arrayDimension = arrayDimension;
        brackets = "";
        bracketsSig = "";
        for (int i = 0; i < arrayDimension; i ++) {
            brackets += "[]";
            bracketsSig += "[";
        }
        String idlName = IDLNames.getArrayName(type,arrayDimension);
        String[] module = IDLNames.getArrayModuleNames(type);
        setNames(type.getIdentifier(),module,idlName);
        setRepositoryID();
    }
    protected Class loadClass() {
        Class result = null;
        Class elementClass = type.getClassInstance();
        if (elementClass != null) {
            result = Array.newInstance(elementClass, new int[arrayDimension]).getClass();
        }
        return result;
    }
    protected void destroy () {
        super.destroy();
        if (type != null) {
            type.destroy();
            type = null;
        }
        brackets = null;
        bracketsSig = null;
    }
}
