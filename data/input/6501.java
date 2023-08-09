public class SpecialClassType extends ClassType {
    public static SpecialClassType forSpecial (ClassDefinition theClass,
                                               ContextStack stack) {
        if (stack.anyErrors()) return null;
        sun.tools.java.Type type = theClass.getType();
        String typeKey = type.toString() + stack.getContextCodeString();
        Type existing = getType(typeKey,stack);
        if (existing != null) {
            if (!(existing instanceof SpecialClassType)) return null; 
            return (SpecialClassType) existing;
        }
        int typeCode = getTypeCode(type,theClass,stack);
        if (typeCode != TYPE_NONE) {
            SpecialClassType result = new SpecialClassType(stack,typeCode,theClass);
            putType(typeKey,result,stack);
            stack.push(result);
            stack.pop(true);
            return result;
        } else {
            return null;
        }
    }
    public String getTypeDescription () {
        return "Special class";
    }
    private SpecialClassType(ContextStack stack, int typeCode,
                             ClassDefinition theClass) {
        super(stack,typeCode | TM_SPECIAL_CLASS | TM_CLASS | TM_COMPOUND, theClass);
        Identifier id = theClass.getName();
        String idlName = null;
        String[] idlModuleName = null;
        boolean constant = stack.size() > 0 && stack.getContext().isConstant();
        switch (typeCode) {
        case TYPE_STRING:   {
            idlName = IDLNames.getTypeName(typeCode,constant);
            if (!constant) {
                idlModuleName = IDL_CORBA_MODULE;
            }
            break;
        }
        case TYPE_ANY:   {
            idlName = IDL_JAVA_LANG_OBJECT;
            idlModuleName = IDL_JAVA_LANG_MODULE;
            break;
        }
        }
        setNames(id,idlModuleName,idlName);
        if (!initParents(stack)) {
            throw new CompilerError("SpecialClassType found invalid parent.");
        }
        initialize(null,null,null,stack,false);
    }
    private static int getTypeCode(sun.tools.java.Type type, ClassDefinition theClass, ContextStack stack) {
        if (type.isType(TC_CLASS)) {
            Identifier id = type.getClassName();
            if (id == idJavaLangString) return TYPE_STRING;
            if (id == idJavaLangObject) return TYPE_ANY;
        }
        return TYPE_NONE;
    }
}
