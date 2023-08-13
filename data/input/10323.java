public class RemoteType extends InterfaceType {
    public static RemoteType forRemote(ClassDefinition classDef,
                                       ContextStack stack,
                                       boolean quiet) {
        if (stack.anyErrors()) return null;
        boolean doPop = false;
        RemoteType result = null;
        try {
            sun.tools.java.Type theType = classDef.getType();
            Type existing = getType(theType,stack);
            if (existing != null) {
                if (!(existing instanceof RemoteType)) return null; 
                return (RemoteType) existing;
            }
            if (couldBeRemote(quiet,stack,classDef)) {
                RemoteType it = new RemoteType(stack,classDef);
                putType(theType,it,stack);
                stack.push(it);
                doPop = true;
                if (it.initialize(quiet,stack)) {
                    stack.pop(true);
                    result = it;
                } else {
                    removeType(theType,stack);
                    stack.pop(false);
                }
            }
        } catch (CompilerError e) {
            if (doPop) stack.pop(false);
        }
        return result;
    }
    public String getTypeDescription () {
        return "Remote interface";
    }
    protected RemoteType(ContextStack stack, ClassDefinition classDef) {
        super(stack,classDef,TYPE_REMOTE | TM_INTERFACE | TM_COMPOUND);
    }
    protected RemoteType(ContextStack stack, ClassDefinition classDef, int typeCode) {
        super(stack,classDef,typeCode);
    }
    private static boolean couldBeRemote (boolean quiet, ContextStack stack,
                                          ClassDefinition classDef) {
        boolean result = false;
        BatchEnvironment env = stack.getEnv();
        try {
            if (!classDef.isInterface()) {
                failedConstraint(16,quiet,stack,classDef.getName());
            } else {
                result = env.defRemote.implementedBy(env,classDef.getClassDeclaration());
                if (!result) failedConstraint(1,quiet,stack,classDef.getName());
            }
        } catch (ClassNotFound e) {
            classNotFound(stack,e);
        }
        return result;
    }
    private boolean initialize (boolean quiet,ContextStack stack) {
        boolean result = false;
        Vector directInterfaces = new Vector();
        Vector directMethods = new Vector();
        Vector directConstants = new Vector();
        if (isConformingRemoteInterface(directInterfaces,
                                        directMethods,
                                        directConstants,
                                        quiet,
                                        stack)){
            result = initialize(directInterfaces,directMethods,directConstants,stack,quiet);
        }
        return result;
    }
    private boolean isConformingRemoteInterface (       Vector directInterfaces,
                                                        Vector directMethods,
                                                        Vector directConstants,
                                                        boolean quiet,
                                                        ContextStack stack) {
        ClassDefinition theInterface = getClassDefinition();
        try {
            if (addRemoteInterfaces(directInterfaces,false,stack) == null ) {
                return false;
            }
            if (!addAllMembers(directConstants,true,quiet,stack)) {
                return false;
            }
            if (addAllMethods(theInterface,directMethods,true,quiet,stack) == null) {
                return false;
            }
            boolean methodsConform = true;
            for (int i = 0; i < directMethods.size(); i++) {
                if (! isConformingRemoteMethod((Method) directMethods.elementAt(i),quiet)) {
                    methodsConform = false;
                }
            }
            if (!methodsConform) {
                return false;
            }
        } catch (ClassNotFound e) {
            classNotFound(stack,e);
            return false;
        }
        return true;
    }
}
