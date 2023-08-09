public abstract class Type implements sun.rmi.rmic.iiop.Constants, ContextElement, Cloneable {
    private int typeCode;
    private int fullTypeCode;
    private Identifier id;
    private String name;
    private String packageName;
    private String qualifiedName;
    private String idlName;
    private String[] idlModuleNames;
    private String qualifiedIDLName;
    private String repositoryID;
    private Class ourClass;
    private int status = STATUS_PENDING;
    protected BatchEnvironment env;     
    protected ContextStack stack;       
    protected boolean destroyed = false;
    public String getName() {
        return name;
    }
    public String getPackageName() {
        return packageName;
    }
    public String getQualifiedName() {
        return qualifiedName;
    }
    public abstract String getSignature();
    public String getIDLName() {
        return idlName;
    }
    public String[] getIDLModuleNames() {
        return idlModuleNames;
    }
    public String getQualifiedIDLName(boolean global) {
        if (global && getIDLModuleNames().length > 0) {
            return IDL_NAME_SEPARATOR + qualifiedIDLName;
        } else {
            return qualifiedIDLName;
        }
    }
    public Identifier getIdentifier() {
        return id;
    }
    public String getRepositoryID() {
        return repositoryID;
    }
    public String getBoxedRepositoryID() {
        return RepositoryId.createForJavaType(ourClass);
    }
    public Class getClassInstance() {
        if (ourClass == null) {
            initClass();
        }
        return ourClass;
    }
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public BatchEnvironment getEnv() {
        return env;
    }
    public int getTypeCode() {
        return typeCode;
    }
    public int getFullTypeCode() {
        return fullTypeCode;
    }
    public int getTypeCodeModifiers() {
        return fullTypeCode & TM_MASK;
    }
    public boolean isType(int typeCodeMask) {
        return (fullTypeCode & typeCodeMask) == typeCodeMask;
    }
    public boolean typeMatches(int typeCodeMask) {
        return (fullTypeCode & typeCodeMask) > 0;
    }
    public int getRootTypeCode() {
        if (isArray()) {
            return getElementType().getFullTypeCode();
        } else {
            return fullTypeCode;
        }
    }
    public boolean isInterface() {
        return (fullTypeCode & TM_INTERFACE) == TM_INTERFACE;
    }
    public boolean isClass() {
        return (fullTypeCode & TM_CLASS) == TM_CLASS;
    }
    public boolean isInner() {
        return (fullTypeCode & TM_INNER) == TM_INNER;
    }
    public boolean isSpecialInterface() {
        return (fullTypeCode & TM_SPECIAL_INTERFACE) == TM_SPECIAL_INTERFACE;
    }
    public boolean isSpecialClass() {
        return (fullTypeCode & TM_SPECIAL_CLASS) == TM_SPECIAL_CLASS;
    }
    public boolean isCompound() {
        return (fullTypeCode & TM_COMPOUND) == TM_COMPOUND;
    }
    public boolean isPrimitive() {
        return (fullTypeCode & TM_PRIMITIVE) == TM_PRIMITIVE;
    }
    public boolean isArray() {
        return (fullTypeCode & TYPE_ARRAY) == TYPE_ARRAY;
    }
    public boolean isConforming() {
        return (fullTypeCode & TM_NON_CONFORMING) == TM_NON_CONFORMING;
    }
    public String toString () {
        return getQualifiedName();
    }
    public Type getElementType () {
        return null;
    }
    public int getArrayDimension () {
        return 0;
    }
    public String getArrayBrackets () {
        return "";
    }
    public boolean equals(Object obj) {
        String us = toString();
        String them = ((Type)obj).toString();
        return us.equals(them);
    }
    public Type[] collectMatching (int typeCodeFilter) {
        return collectMatching(typeCodeFilter,new HashSet(env.allTypes.size()));
    }
    public Type[] collectMatching (int typeCodeFilter, HashSet alreadyChecked) {
        Vector matching = new Vector();
        addTypes(typeCodeFilter,alreadyChecked,matching);
        Type[] result = new Type[matching.size()];
        matching.copyInto(result);
        return result;
    }
    public abstract String getTypeDescription ();
    public String getTypeName ( boolean useQualifiedNames,
                                boolean useIDLNames,
                                boolean globalIDLNames) {
        if (useIDLNames) {
            if (useQualifiedNames) {
                return getQualifiedIDLName(globalIDLNames);
            } else {
                return getIDLName();
            }
        } else {
            if (useQualifiedNames) {
                return getQualifiedName();
            } else {
                return getName();
            }
        }
    }
    public void print ( IndentingWriter writer,
                        int typeCodeFilter,
                        boolean useQualifiedNames,
                        boolean useIDLNames,
                        boolean globalIDLNames) throws IOException {
        Type[] theTypes = collectMatching(typeCodeFilter);
        print(writer,theTypes,useQualifiedNames,useIDLNames,globalIDLNames);
    }
    public static void print (  IndentingWriter writer,
                                Type[] theTypes,
                                boolean useQualifiedNames,
                                boolean useIDLNames,
                                boolean globalIDLNames) throws IOException {
        for (int i = 0; i < theTypes.length; i++) {
            theTypes[i].println(writer,useQualifiedNames,useIDLNames,globalIDLNames);
        }
    }
    public void print ( IndentingWriter writer,
                        boolean useQualifiedNames,
                        boolean useIDLNames,
                        boolean globalIDLNames) throws IOException {
        printTypeName(writer,useQualifiedNames,useIDLNames,globalIDLNames);
    }
    public void println (       IndentingWriter writer,
                                boolean useQualifiedNames,
                                boolean useIDLNames,
                                boolean globalIDLNames) throws IOException  {
        print(writer,useQualifiedNames,useIDLNames,globalIDLNames);
        writer.pln();
    }
    public void printTypeName ( IndentingWriter writer,
                                boolean useQualifiedNames,
                                boolean useIDLNames,
                                boolean globalIDLNames) throws IOException {
        writer.p(getTypeName(useQualifiedNames,useIDLNames,globalIDLNames));
    }
    public String getElementName() {
        return getQualifiedName();
    }
    protected void printPackageOpen (   IndentingWriter writer,
                                        boolean useIDLNames) throws IOException {
        if (useIDLNames) {
            String[] moduleNames = getIDLModuleNames();
            for (int i = 0; i < moduleNames.length; i++ ) {
                writer.plnI("module " + moduleNames[i] + " {");
            }
        } else {
            String packageName = getPackageName();
            if (packageName != null) {
                writer.pln("package " + packageName + ";");
            }
        }
    }
    protected static Type getType (sun.tools.java.Type key, ContextStack stack) {
        return getType(key.toString(),stack);
    }
    protected static Type getType (String key, ContextStack stack) {
        Type result = (Type) stack.getEnv().allTypes.get(key);
        if (result != null) {
            stack.traceExistingType(result);
        }
        return result;
    }
    protected static void removeType (String key, ContextStack stack) {
        Type value = (Type) stack.getEnv().allTypes.remove(key);
        stack.getEnv().invalidTypes.put(value,key);
    }
    protected static void removeType (sun.tools.java.Type key, ContextStack stack) {
        String theKey = key.toString();
        Type old = (Type) stack.getEnv().allTypes.remove(theKey);
        putInvalidType(old,theKey,stack);
    }
    protected static void putType (sun.tools.java.Type key, Type value, ContextStack stack) {
        stack.getEnv().allTypes.put(key.toString(),value);
    }
    protected static void putType (String key, Type value, ContextStack stack) {
        stack.getEnv().allTypes.put(key,value);
    }
    protected static void putInvalidType (Type key, String value, ContextStack stack) {
        stack.getEnv().invalidTypes.put(key,value);
    }
    public void removeInvalidTypes () {
        if (env.invalidTypes.size() > 0) {
            env.invalidTypes.clear();
        }
    }
    protected static void updateAllInvalidTypes (ContextStack stack) {
        BatchEnvironment env = stack.getEnv();
        if (env.invalidTypes.size() > 0) {
            for (Enumeration e = env.allTypes.elements() ; e.hasMoreElements() ;) {
                Type it = (Type) e.nextElement();
                it.swapInvalidTypes();
            }
            env.invalidTypes.clear();
        }
    }
    protected int countTypes () {
        return env.allTypes.size();
    }
    void resetTypes () {
        env.reset();
    }
    protected void destroy () {
        if (!destroyed) {
            id = null;
            name = null;
            packageName = null;
            qualifiedName = null;
            idlName = null;
            idlModuleNames = null;
            qualifiedIDLName = null;
            repositoryID = null;
            ourClass = null;
            env = null;
            stack = null;
            destroyed = true;
        }
    }
    protected void swapInvalidTypes () {
    }
    protected Type getValidType (Type invalidType) {
        if (invalidType.getStatus() == STATUS_VALID) {
            return invalidType;
        }
        String key = (String)env.invalidTypes.get(invalidType);
        Type result = null;
        if (key != null) {
            result = (Type) env.allTypes.get(key);
        }
        if (result == null) {
            throw new Error("Failed to find valid type to swap for " + invalidType + " mis-identified as " + invalidType.getTypeDescription());
        }
        return result;
    }
    protected void printPackageClose (  IndentingWriter writer,
                                        boolean useIDLNames) throws IOException {
        if (useIDLNames) {
            String[] moduleNames = getIDLModuleNames();
            for (int i = 0; i < moduleNames.length; i++ ) {
                writer.pOln("};");
            }
        }
    }
    protected Type(ContextStack stack, int fullTypeCode) {
        this.env = stack.getEnv();
        this.stack = stack;
        this.fullTypeCode = fullTypeCode;
        typeCode = fullTypeCode & TYPE_MASK;
    }
    protected void setTypeCode(int fullTypeCode) {
        this.fullTypeCode = fullTypeCode;
        typeCode = fullTypeCode & TYPE_MASK;
    }
    protected void setNames(Identifier id, String[] idlModuleNames, String idlName) {
        this.id = id;
        name = Names.mangleClass(id).getName().toString();
        packageName = null;
        if (id.isQualified()) {
            packageName = id.getQualifier().toString();
            qualifiedName = packageName + NAME_SEPARATOR + name;
        } else {
            qualifiedName = name;
        }
        setIDLNames(idlModuleNames,idlName);
    }
    protected void setIDLNames(String[] idlModuleNames, String idlName) {
        this.idlName = idlName;
        if (idlModuleNames != null) {
            this.idlModuleNames = idlModuleNames;
        } else {
            this.idlModuleNames = new String[0];
        }
        qualifiedIDLName = IDLNames.getQualifiedName(idlModuleNames,idlName);
    }
    protected static void classNotFound(ContextStack stack,
                                        ClassNotFound e) {
        classNotFound(false,stack,e);
    }
    protected static void classNotFound(boolean quiet,
                                        ContextStack stack,
                                        ClassNotFound e) {
        if (!quiet) stack.getEnv().error(0, "rmic.class.not.found", e.name);
        stack.traceCallStack();
    }
    protected static boolean failedConstraint(int constraintNum,
                                              boolean quiet,
                                              ContextStack stack,
                                              Object arg0, Object arg1, Object arg2) {
        String message = "rmic.iiop.constraint." + constraintNum;
        if (!quiet) {
            stack.getEnv().error(0,message,
                                 (arg0 != null ? arg0.toString() : null),
                                 (arg1 != null ? arg1.toString() : null),
                                 (arg2 != null ? arg2.toString() : null));
        } else {
            String error = stack.getEnv().errorString(message,arg0,arg1,arg2);
            stack.traceln(error);
        }
        return false;
    }
    protected static boolean failedConstraint(int constraintNum,
                                              boolean quiet,
                                              ContextStack stack,
                                              Object arg0, Object arg1) {
        return failedConstraint(constraintNum,quiet,stack,arg0,arg1,null);
    }
    protected static boolean failedConstraint(int constraintNum,
                                              boolean quiet,
                                              ContextStack stack,
                                              Object arg0) {
        return failedConstraint(constraintNum,quiet,stack,arg0,null,null);
    }
    protected static boolean failedConstraint(int constraintNum,
                                              boolean quiet,
                                              ContextStack stack) {
        return failedConstraint(constraintNum,quiet,stack,null,null,null);
    }
    protected Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new Error("clone failed");
        }
    }
    protected boolean addTypes (int typeCodeFilter,
                                HashSet checked,
                                Vector matching) {
        boolean result;
        if (checked.contains(this)) {
            result = false;
        } else {
            checked.add(this);
            if (typeMatches(typeCodeFilter)) {
                matching.addElement(this);
            }
            result = true;
        }
        return result;
    }
    protected abstract Class loadClass();
    private boolean initClass() {
        if (ourClass == null) {
            ourClass = loadClass();
            if (ourClass == null) {
                failedConstraint(27,false,stack,getQualifiedName());
                return false;
            }
        }
        return true;
    }
    protected boolean setRepositoryID() {
        if (!initClass()) {
            return false;
        }
        repositoryID = RepositoryId.createForAnyType(ourClass);
        return true;
    }
    private Type () {} 
}
