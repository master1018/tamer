public class ValueType extends ClassType {
    private boolean isCustom;
    public static ValueType forValue(ClassDefinition classDef,
                                     ContextStack stack,
                                     boolean quiet) {
        if (stack.anyErrors()) return null;
        sun.tools.java.Type theType = classDef.getType();
        String typeKey = theType.toString();
        Type existing = getType(typeKey,stack);
        if (existing != null) {
            if (!(existing instanceof ValueType)) return null; 
            return (ValueType) existing;
        }
        boolean javaLangClass = false;
        if (classDef.getClassDeclaration().getName() == idJavaLangClass) {
            javaLangClass = true;
            BatchEnvironment env = stack.getEnv();
            ClassDeclaration decl = env.getClassDeclaration(idClassDesc);
            ClassDefinition def = null;
            try {
                def = decl.getClassDefinition(env);
            } catch (ClassNotFound ex) {
                classNotFound(stack,ex);
                return null;
            }
            classDef = def;
        }
        if (couldBeValue(stack,classDef)) {
            ValueType it = new ValueType(classDef,stack,javaLangClass);
            putType(typeKey,it,stack);
            stack.push(it);
            if (it.initialize(stack,quiet)) {
                stack.pop(true);
                return it;
            } else {
                removeType(typeKey,stack);
                stack.pop(false);
                return null;
            }
        } else {
            return null;
        }
    }
    public String getTypeDescription () {
        String result = addExceptionDescription("Value");
        if (isCustom) {
            result = "Custom " + result;
        }
        if (isIDLEntity) {
            result = result + " [IDLEntity]";
        }
        return result;
    }
    public boolean isCustom () {
        return isCustom;
    }
    private ValueType(ClassDefinition classDef,
                      ContextStack stack,
                      boolean isMappedJavaLangClass) {
        super(stack,classDef,TYPE_VALUE | TM_CLASS | TM_COMPOUND);
        isCustom = false;
        if (isMappedJavaLangClass) {
            setNames(idJavaLangClass,IDL_CLASS_MODULE,IDL_CLASS);
        }
    }
    private static boolean couldBeValue(ContextStack stack, ClassDefinition classDef) {
        boolean result = false;
        ClassDeclaration classDecl = classDef.getClassDeclaration();
        BatchEnvironment env = stack.getEnv();
        try {
            if (env.defRemote.implementedBy(env, classDecl)) {
                failedConstraint(10,false,stack,classDef.getName());
            } else {
                if (!env.defSerializable.implementedBy(env, classDecl)) {
                    failedConstraint(11,false,stack,classDef.getName());
                } else {
                    result = true;
                }
            }
        } catch (ClassNotFound e) {
            classNotFound(stack,e);
        }
        return result;
    }
    private boolean initialize (ContextStack stack, boolean quiet) {
        ClassDefinition ourDef = getClassDefinition();
        ClassDeclaration ourDecl = getClassDeclaration();
        try {
            if (!initParents(stack)) {
                failedConstraint(12,quiet,stack,getQualifiedName());
                return false;
            }
            Vector directInterfaces = new Vector();
            Vector directMethods = new Vector();
            Vector directMembers = new Vector();
            if (addNonRemoteInterfaces(directInterfaces,stack) != null) {
                if (addAllMethods(ourDef,directMethods,false,false,stack) != null) {
                    if (updateParentClassMethods(ourDef,directMethods,false,stack) != null) {
                    if (addAllMembers(directMembers,false,false,stack)) {
                        if (!initialize(directInterfaces,directMethods,directMembers,stack,quiet)) {
                            return false;
                        }
                        boolean externalizable = false;
                        if (!env.defExternalizable.implementedBy(env, ourDecl)) {
                            if (!checkPersistentFields(getClassInstance(),quiet)) {
                                return false;
                            }
                        } else {
                            externalizable = true;
                        }
                        if (externalizable) {
                            isCustom = true;
                        } else {
                            for (MemberDefinition member = ourDef.getFirstMember();
                                 member != null;
                                 member = member.getNextMember()) {
                                if (member.isMethod() &&
                                    !member.isInitializer() &&
                                    member.isPrivate() &&
                                    member.getName().toString().equals("writeObject")) {
                                    sun.tools.java.Type methodType = member.getType();
                                    sun.tools.java.Type rtnType = methodType.getReturnType();
                                    if (rtnType == sun.tools.java.Type.tVoid) {
                                        sun.tools.java.Type[] args = methodType.getArgumentTypes();
                                        if (args.length == 1 &&
                                            args[0].getTypeSignature().equals("Ljava/io/ObjectOutputStream;")) {
                                            isCustom = true;
                                        }
                                    }
                                }
                            }
                        }
                        }
                        return true;
                    }
                }
            }
        } catch (ClassNotFound e) {
            classNotFound(stack,e);
        }
        return false;
    }
    private boolean checkPersistentFields (Class clz, boolean quiet) {
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals("writeObject") &&
                methods[i].getArguments().length == 1) {
                Type returnType = methods[i].getReturnType();
                Type arg = methods[i].getArguments()[0];
                String id = arg.getQualifiedName();
                if (returnType.isType(TYPE_VOID) &&
                    id.equals("java.io.ObjectOutputStream")) {
                    return true;
                }
            }
        }
        MemberDefinition spfDef = null;
        for (int i = 0; i < members.length; i++) {
            if (members[i].getName().equals("serialPersistentFields")) {
                Member member = members[i];
                Type type = member.getType();
                Type elementType = type.getElementType();
                if (elementType != null &&
                    elementType.getQualifiedName().equals(
                                                          "java.io.ObjectStreamField")
                    ) {
                    if (member.isStatic() &&
                        member.isFinal() &&
                        member.isPrivate()) {
                        spfDef = member.getMemberDefinition();
                    } else {
                        failedConstraint(4,quiet,stack,getQualifiedName());
                        return false;
                    }
                }
            }
        }
        if (spfDef == null) {
            return true;
        }
        Hashtable fields = getPersistentFields(clz);
        boolean result = true;
        for (int i = 0; i < members.length; i++) {
            String fieldName = members[i].getName();
            String fieldType = members[i].getType().getSignature();
            String type = (String) fields.get(fieldName);
            if (type == null) {
                members[i].setTransient();
            } else {
                if (type.equals(fieldType)) {
                    fields.remove(fieldName);
                } else {
                    result = false;
                    failedConstraint(2,quiet,stack,fieldName,getQualifiedName());
                }
            }
        }
        if (result && fields.size() > 0) {
            result = false;
            failedConstraint(9,quiet,stack,getQualifiedName());
        }
        return result;
    }
    private Hashtable getPersistentFields (Class clz) {
        Hashtable result = new Hashtable();
        ObjectStreamClass osc = ObjectStreamClass.lookup(clz);
        if (osc != null) {
            ObjectStreamField[] fields = osc.getFields();
            for (int i = 0; i < fields.length; i++) {
                String typeSig;
                String typePrefix = String.valueOf(fields[i].getTypeCode());
                if (fields[i].isPrimitive()) {
                    typeSig = typePrefix;
                } else {
                    if (fields[i].getTypeCode() == '[') {
                        typePrefix = "";
                    }
                    typeSig = typePrefix + fields[i].getType().getName().replace('.','/');
                    if (typeSig.endsWith(";")) {
                        typeSig = typeSig.substring(0,typeSig.length()-1);
                    }
                }
                result.put(fields[i].getName(),typeSig);
            }
        }
        return result;
    }
}
