public class SpecialInterfaceType extends InterfaceType {
    public static SpecialInterfaceType forSpecial ( ClassDefinition theClass,
                                                    ContextStack stack) {
        if (stack.anyErrors()) return null;
        sun.tools.java.Type type = theClass.getType();
        Type existing = getType(type,stack);
        if (existing != null) {
            if (!(existing instanceof SpecialInterfaceType)) return null; 
            return (SpecialInterfaceType) existing;
        }
        if (isSpecial(type,theClass,stack)) {
            SpecialInterfaceType result = new SpecialInterfaceType(stack,0,theClass);
            putType(type,result,stack);
            stack.push(result);
            if (result.initialize(type,stack)) {
                stack.pop(true);
                return result;
            } else {
                removeType(type,stack);
                stack.pop(false);
                return null;
            }
        }
        return null;
    }
    public String getTypeDescription () {
        return "Special interface";
    }
    private SpecialInterfaceType(ContextStack stack, int typeCode,
                                 ClassDefinition theClass) {
        super(stack,typeCode | TM_SPECIAL_INTERFACE | TM_INTERFACE | TM_COMPOUND, theClass);
        setNames(theClass.getName(),null,null); 
    }
    private static boolean isSpecial(sun.tools.java.Type type,
                                     ClassDefinition theClass,
                                     ContextStack stack) {
        if (type.isType(TC_CLASS)) {
            Identifier id = type.getClassName();
            if (id.equals(idRemote)) return true;
            if (id == idJavaIoSerializable) return true;
            if (id == idJavaIoExternalizable) return true;
            if (id == idCorbaObject) return true;
            if (id == idIDLEntity) return true;
            BatchEnvironment env = stack.getEnv();
            try {
                if (env.defCorbaObject.implementedBy(env,theClass.getClassDeclaration())) return true;
            } catch (ClassNotFound e) {
                classNotFound(stack,e);
            }
        }
        return false;
    }
    private boolean initialize(sun.tools.java.Type type, ContextStack stack) {
        int typeCode = TYPE_NONE;
        Identifier id = null;
        String idlName = null;
        String[] idlModuleName = null;
        boolean constant = stack.size() > 0 && stack.getContext().isConstant();
        if (type.isType(TC_CLASS)) {
            id = type.getClassName();
            if (id.equals(idRemote)) {
                typeCode = TYPE_JAVA_RMI_REMOTE;
                idlName = IDL_JAVA_RMI_REMOTE;
                idlModuleName = IDL_JAVA_RMI_MODULE;
            } else if (id == idJavaIoSerializable) {
                typeCode = TYPE_ANY;
                idlName = IDL_SERIALIZABLE;
                idlModuleName = IDL_JAVA_IO_MODULE;
            } else if (id == idJavaIoExternalizable) {
                typeCode = TYPE_ANY;
                idlName = IDL_EXTERNALIZABLE;
                idlModuleName = IDL_JAVA_IO_MODULE;
            } else if (id == idIDLEntity) {
                typeCode = TYPE_ANY;
                idlName = IDL_IDLENTITY;
                idlModuleName = IDL_ORG_OMG_CORBA_PORTABLE_MODULE;
            } else {
                typeCode = TYPE_CORBA_OBJECT;
                if (id == idCorbaObject) {
                    idlName = IDLNames.getTypeName(typeCode,constant);
                    idlModuleName = null;
                } else {
                    try {
                        idlName = IDLNames.getClassOrInterfaceName(id,env);
                        idlModuleName = IDLNames.getModuleNames(id,isBoxed(),env);
                    } catch (Exception e) {
                        failedConstraint(7,false,stack,id.toString(),e.getMessage());
                        throw new CompilerError("");
                    }
                }
            }
        }
        if (typeCode == TYPE_NONE) {
            return false;
        }
        setTypeCode(typeCode | TM_SPECIAL_INTERFACE | TM_INTERFACE | TM_COMPOUND);
        if (idlName == null) {
            throw new CompilerError("Not a special type");
        }
        setNames(id,idlModuleName,idlName);
        return initialize(null,null,null,stack,false);
    }
}
