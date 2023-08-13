public class ImplementationType extends ClassType {
    public static ImplementationType forImplementation(ClassDefinition classDef,
                                                       ContextStack stack,
                                                       boolean quiet) {
        if (stack.anyErrors()) return null;
        boolean doPop = false;
        ImplementationType result = null;
        try {
            sun.tools.java.Type theType = classDef.getType();
            Type existing = getType(theType,stack);
            if (existing != null) {
                if (!(existing instanceof ImplementationType)) return null; 
                return (ImplementationType) existing;
            }
            if (couldBeImplementation(quiet,stack,classDef)) {
                ImplementationType it = new ImplementationType(stack, classDef);
                putType(theType,it,stack);
                stack.push(it);
                doPop = true;
                if (it.initialize(stack,quiet)) {
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
        return "Implementation";
    }
    private ImplementationType(ContextStack stack, ClassDefinition classDef) {
        super(TYPE_IMPLEMENTATION | TM_CLASS | TM_COMPOUND,classDef,stack); 
    }
    private static boolean couldBeImplementation(boolean quiet, ContextStack stack,
                                                 ClassDefinition classDef) {
        boolean result = false;
        BatchEnvironment env = stack.getEnv();
        try {
            if (!classDef.isClass()) {
                failedConstraint(17,quiet,stack,classDef.getName());
            } else {
                result = env.defRemote.implementedBy(env, classDef.getClassDeclaration());
                if (!result) failedConstraint(8,quiet,stack,classDef.getName());
            }
        } catch (ClassNotFound e) {
            classNotFound(stack,e);
        }
        return result;
    }
    private boolean initialize (ContextStack stack, boolean quiet) {
        boolean result = false;
        ClassDefinition theClass = getClassDefinition();
        if (initParents(stack)) {
            Vector directInterfaces = new Vector();
            Vector directMethods = new Vector();
            try {
                if (addRemoteInterfaces(directInterfaces,true,stack) != null) {
                    boolean haveRemote = false;
                    for (int i = 0; i < directInterfaces.size(); i++) {
                        InterfaceType theInt = (InterfaceType) directInterfaces.elementAt(i);
                        if (theInt.isType(TYPE_REMOTE) ||
                            theInt.isType(TYPE_JAVA_RMI_REMOTE)) {
                            haveRemote = true;
                        }
                        copyRemoteMethods(theInt,directMethods);
                    }
                    if (!haveRemote) {
                        failedConstraint(8,quiet,stack,getQualifiedName());
                        return false;
                    }
                    if (checkMethods(theClass,directMethods,stack,quiet)) {
                        result = initialize(directInterfaces,directMethods,null,stack,quiet);
                    }
                }
            } catch (ClassNotFound e) {
                classNotFound(stack,e);
            }
        }
        return result;
    }
    private static void copyRemoteMethods(InterfaceType type, Vector list) {
        if (type.isType(TYPE_REMOTE)) {
            Method[] allMethods = type.getMethods();
            for (int i = 0; i < allMethods.length; i++) {
                Method theMethod = allMethods[i];
                if (!list.contains(theMethod)) {
                    list.addElement(theMethod);
                }
            }
            InterfaceType[] allInterfaces = type.getInterfaces();
            for (int i = 0; i < allInterfaces.length; i++) {
                copyRemoteMethods(allInterfaces[i],list);
            }
        }
    }
    private boolean checkMethods(ClassDefinition theClass, Vector list,
                                 ContextStack stack, boolean quiet) {
        Method[] methods = new Method[list.size()];
        list.copyInto(methods);
        for (MemberDefinition member = theClass.getFirstMember();
             member != null;
             member = member.getNextMember()) {
            if (member.isMethod() && !member.isConstructor()
                && !member.isInitializer()) {
                if (!updateExceptions(member,methods,stack,quiet)) {
                    return false;
                }
            }
        }
        return true;
    }
    private boolean updateExceptions (MemberDefinition implMethod, Method[] list,
                                      ContextStack stack, boolean quiet) {
        int length = list.length;
        String implMethodSig = implMethod.toString();
        for (int i = 0; i < length; i++) {
            Method existingMethod = list[i];
            MemberDefinition existing = existingMethod.getMemberDefinition();
            if (implMethodSig.equals(existing.toString())) {
                try {
                    ValueType[] implExcept = getMethodExceptions(implMethod,quiet,stack);
                    existingMethod.setImplExceptions(implExcept);
                } catch (Exception e) {
                    return false;
                }
            }
        }
        return true;
    }
}
