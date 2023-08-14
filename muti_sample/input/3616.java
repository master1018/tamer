public class NCClassType extends ClassType {
    public static NCClassType forNCClass(ClassDefinition classDef,
                                         ContextStack stack) {
        if (stack.anyErrors()) return null;
        boolean doPop = false;
        try {
            sun.tools.java.Type theType = classDef.getType();
            Type existing = getType(theType,stack);
            if (existing != null) {
                if (!(existing instanceof NCClassType)) return null; 
                return (NCClassType) existing;
            }
            NCClassType it = new NCClassType(stack, classDef);
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
        return addExceptionDescription("Non-conforming class");
    }
    private NCClassType(ContextStack stack, ClassDefinition classDef) {
        super(stack,classDef,TYPE_NC_CLASS | TM_CLASS | TM_COMPOUND);
    }
    private boolean initialize (ContextStack stack) {
        if (!initParents(stack)) {
            return false;
        }
        if (stack.getEnv().getParseNonConforming()) {
            Vector directInterfaces = new Vector();
            Vector directMethods = new Vector();
            Vector directMembers = new Vector();
            try {
                if (addAllMethods(getClassDefinition(),directMethods,false,false,stack) != null) {
                    if (updateParentClassMethods(getClassDefinition(),directMethods,false,stack) != null) {
                    if (addConformingConstants(directMembers,false,stack)) {
                        if (!initialize(directInterfaces,directMethods,directMembers,stack,false)) {
                            return false;
                        }
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
