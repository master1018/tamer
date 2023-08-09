public class AbstractType extends RemoteType {
    public static AbstractType forAbstract(ClassDefinition classDef,
                                           ContextStack stack,
                                           boolean quiet)
    {
        boolean doPop = false;
        AbstractType result = null;
        try {
            sun.tools.java.Type theType = classDef.getType();
            Type existing = getType(theType,stack);
            if (existing != null) {
                if (!(existing instanceof AbstractType)) return null; 
                return (AbstractType) existing;
            }
            if (couldBeAbstract(stack,classDef,quiet)) {
                AbstractType it = new AbstractType(stack, classDef);
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
        return "Abstract interface";
    }
    private AbstractType(ContextStack stack, ClassDefinition classDef) {
        super(stack,classDef,TYPE_ABSTRACT | TM_INTERFACE | TM_COMPOUND);
    }
    private static boolean couldBeAbstract(ContextStack stack, ClassDefinition classDef,
                                           boolean quiet) {
        boolean result = false;
        if (classDef.isInterface()) {
            BatchEnvironment env = stack.getEnv();
            try {
                result = ! env.defRemote.implementedBy(env, classDef.getClassDeclaration());
                if (!result) failedConstraint(15,quiet,stack,classDef.getName());
            } catch (ClassNotFound e) {
                classNotFound(stack,e);
            }
        } else {
            failedConstraint(14,quiet,stack,classDef.getName());
        }
        return result;
    }
    private boolean initialize (boolean quiet,ContextStack stack) {
        boolean result = false;
        ClassDefinition self = getClassDefinition();
        try {
            Vector directMethods = new Vector();
            if (addAllMethods(self,directMethods,true,quiet,stack) != null) {
                boolean validMethods = true;
                if (directMethods.size() > 0) {
                    for (int i = 0; i < directMethods.size(); i++) {
                        if (! isConformingRemoteMethod((Method) directMethods.elementAt(i),true)) {
                            validMethods = false;
                        }
                    }
                }
                if (validMethods) {
                    result = initialize(null,directMethods,null,stack,quiet);
                }
            }
        } catch (ClassNotFound e) {
            classNotFound(stack,e);
        }
        return result;
    }
}
