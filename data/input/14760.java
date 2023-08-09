public class NCInterfaceType extends InterfaceType {
    public static NCInterfaceType forNCInterface( ClassDefinition classDef,
                                                  ContextStack stack) {
        if (stack.anyErrors()) return null;
        boolean doPop = false;
        try {
            sun.tools.java.Type theType = classDef.getType();
            Type existing = getType(theType,stack);
            if (existing != null) {
                if (!(existing instanceof NCInterfaceType)) return null; 
                return (NCInterfaceType) existing;
            }
            NCInterfaceType it = new NCInterfaceType(stack, classDef);
            putType(theType,it,stack);
            stack.push(it);
            doPop = true;
            if (it.initialize(stack)) {
                stack.pop(true);
                return it;
            } else {
                removeType(theType,stack);
                stack.pop(false);
                return null;
            }
        } catch (CompilerError e) {
            if (doPop) stack.pop(false);
            return null;
        }
    }
    public String getTypeDescription () {
        return "Non-conforming interface";
    }
    private NCInterfaceType(ContextStack stack, ClassDefinition classDef) {
        super(stack,classDef,TYPE_NC_INTERFACE | TM_INTERFACE | TM_COMPOUND);
    }
    private boolean initialize (ContextStack stack) {
        if (stack.getEnv().getParseNonConforming()) {
            Vector directInterfaces = new Vector();
            Vector directMethods = new Vector();
            Vector directMembers = new Vector();
            try {
                addNonRemoteInterfaces( directInterfaces,stack );
                if (addAllMethods(getClassDefinition(),directMethods,false,false,stack) != null) {
                    if (addConformingConstants(directMembers,false,stack)) {
                        if (!initialize(directInterfaces,directMethods,directMembers,stack,false)) {
                            return false;
                        }
                    }
                }
                return true;
            } catch (ClassNotFound e) {
                classNotFound(stack,e);
            }
            return false;
        } else {
            return initialize(null,null,null,stack,false);
        }
    }
}
