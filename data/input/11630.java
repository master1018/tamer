public class IDLNameTranslatorImpl implements IDLNameTranslator {
    private static String[] IDL_KEYWORDS = {
        "abstract", "any", "attribute", "boolean", "case", "char",
        "const", "context", "custom", "default", "double", "enum",
        "exception", "factory", "FALSE", "fixed", "float", "in", "inout",
        "interface", "long", "module", "native", "Object", "octet",
        "oneway", "out", "private", "public", "raises", "readonly", "sequence",
        "short", "string", "struct", "supports", "switch", "TRUE", "truncatable",
        "typedef", "unsigned", "union", "ValueBase", "valuetype", "void",
        "wchar", "wstring"
    };
    private static char[] HEX_DIGITS = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'A', 'B', 'C', 'D', 'E', 'F'
    };
    private static final String UNDERSCORE = "_";
    private static final String INNER_CLASS_SEPARATOR =
        UNDERSCORE + UNDERSCORE;
    private static final String[] BASE_IDL_ARRAY_MODULE_TYPE=
        new String[] { "org", "omg", "boxedRMI" } ;
    private static final String BASE_IDL_ARRAY_ELEMENT_TYPE = "seq";
    private static final String LEADING_UNDERSCORE_CHAR = "J";
    private static final String ID_CONTAINER_CLASH_CHAR = UNDERSCORE;
    private static final String OVERLOADED_TYPE_SEPARATOR =
        UNDERSCORE + UNDERSCORE;
    private static final String ATTRIBUTE_METHOD_CLASH_MANGLE_CHARS =
        UNDERSCORE + UNDERSCORE;
    private static final String GET_ATTRIBUTE_PREFIX = "_get_";
    private static final String SET_ATTRIBUTE_PREFIX = "_set_";
    private static final String IS_ATTRIBUTE_PREFIX  = "_get_";
    private static Set idlKeywords_;
    static {
        idlKeywords_ = new HashSet();
        for(int i = 0; i < IDL_KEYWORDS.length; i++) {
            String next = (String) IDL_KEYWORDS[i];
            String keywordAllCaps = next.toUpperCase();
            idlKeywords_.add(keywordAllCaps);
        }
    }
    private Class[] interf_;
    private Map methodToIDLNameMap_;
    private Map IDLNameToMethodMap_;
    private Method[] methods_;
    public static IDLNameTranslator get( Class interf )
    {
        return new IDLNameTranslatorImpl(new Class[] { interf } );
    }
    public static IDLNameTranslator get( Class[] interfaces )
    {
        return new IDLNameTranslatorImpl(interfaces );
    }
    public static String getExceptionId( Class cls )
    {
        IDLType itype = classToIDLType( cls ) ;
        return itype.getExceptionName() ;
    }
    public Class[] getInterfaces()
    {
        return interf_;
    }
    public Method[] getMethods()
    {
        return methods_ ;
    }
    public Method getMethod( String idlName )
    {
        return (Method) IDLNameToMethodMap_.get(idlName);
    }
    public String getIDLName( Method method )
    {
        return (String) methodToIDLNameMap_.get(method);
    }
    private IDLNameTranslatorImpl(Class[] interfaces)
    {
        SecurityManager s = System.getSecurityManager();
        if (s != null) {
            s.checkPermission(new DynamicAccessPermission("access"));
        }
        try {
            IDLTypesUtil idlTypesUtil = new IDLTypesUtil();
            for (int ctr=0; ctr<interfaces.length; ctr++)
                idlTypesUtil.validateRemoteInterface(interfaces[ctr]);
            interf_ = interfaces;
            buildNameTranslation();
        } catch( IDLTypeException ite) {
            String msg = ite.getMessage();
            IllegalStateException ise = new IllegalStateException(msg);
            ise.initCause(ite);
            throw ise;
        }
    }
    private void buildNameTranslation()
    {
        Map allMethodInfo = new HashMap() ;
        for (int ctr=0; ctr<interf_.length; ctr++) {
            Class interf = interf_[ctr] ;
            IDLTypesUtil idlTypesUtil = new IDLTypesUtil();
            final Method[] methods = interf.getMethods();
            AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    Method.setAccessible( methods, true ) ;
                    return null ;
                }
            } ) ;
            for(int i = 0; i < methods.length; i++) {
                Method nextMethod = methods[i];
                IDLMethodInfo methodInfo = new IDLMethodInfo();
                methodInfo.method = nextMethod;
                if (idlTypesUtil.isPropertyAccessorMethod(nextMethod, interf)) {
                    methodInfo.isProperty = true;
                    String attributeName = idlTypesUtil.
                        getAttributeNameForProperty(nextMethod.getName());
                    methodInfo.originalName = attributeName;
                    methodInfo.mangledName  = attributeName;
                } else {
                    methodInfo.isProperty = false;
                    methodInfo.originalName = nextMethod.getName();
                    methodInfo.mangledName  = nextMethod.getName();
                }
                allMethodInfo.put(nextMethod, methodInfo);
            }
        }
        for(Iterator outerIter=allMethodInfo.values().iterator();
            outerIter.hasNext();) {
            IDLMethodInfo outer = (IDLMethodInfo) outerIter.next();
            for(Iterator innerIter = allMethodInfo.values().iterator();
                innerIter.hasNext();) {
                IDLMethodInfo inner = (IDLMethodInfo) innerIter.next();
                if( (outer != inner) &&
                    (!outer.originalName.equals(inner.originalName)) &&
                    outer.originalName.equalsIgnoreCase(inner.originalName) ) {
                    outer.mangledName =
                        mangleCaseSensitiveCollision(outer.originalName);
                    break;
                }
            }
        }
        for(Iterator iter = allMethodInfo.values().iterator();
            iter.hasNext();) {
            IDLMethodInfo next = (IDLMethodInfo) iter.next();
            next.mangledName =
                mangleIdentifier(next.mangledName, next.isProperty);
        }
        for(Iterator outerIter=allMethodInfo.values().iterator();
            outerIter.hasNext();) {
            IDLMethodInfo outer = (IDLMethodInfo) outerIter.next();
            if( outer.isProperty ) {
                continue;
            }
            for(Iterator innerIter = allMethodInfo.values().iterator();
                innerIter.hasNext();) {
                IDLMethodInfo inner = (IDLMethodInfo) innerIter.next();
                if( (outer != inner) &&
                    !inner.isProperty &&
                    outer.originalName.equals(inner.originalName) ) {
                    outer.mangledName = mangleOverloadedMethod
                        (outer.mangledName, outer.method);
                    break;
                }
            }
        }
        for(Iterator outerIter=allMethodInfo.values().iterator();
            outerIter.hasNext();) {
            IDLMethodInfo outer = (IDLMethodInfo) outerIter.next();
            if( !outer.isProperty ) {
                continue;
            }
            for(Iterator innerIter = allMethodInfo.values().iterator();
                innerIter.hasNext();) {
                IDLMethodInfo inner = (IDLMethodInfo) innerIter.next();
                if( (outer != inner) &&
                    !inner.isProperty &&
                    outer.mangledName.equals(inner.mangledName) ) {
                    outer.mangledName = outer.mangledName +
                        ATTRIBUTE_METHOD_CLASH_MANGLE_CHARS;
                    break;
                }
            }
        }
        for (int ctr=0; ctr<interf_.length; ctr++ ) {
            Class interf = interf_[ctr] ;
            String mappedContainerName = getMappedContainerName(interf);
            for(Iterator iter = allMethodInfo.values().iterator();
                iter.hasNext();) {
                IDLMethodInfo next = (IDLMethodInfo) iter.next();
                if( !next.isProperty &&
                    identifierClashesWithContainer(mappedContainerName,
                                                   next.mangledName)) {
                    next.mangledName = mangleContainerClash(next.mangledName);
                }
            }
        }
        methodToIDLNameMap_ = new HashMap();
        IDLNameToMethodMap_ = new HashMap();
        methods_ = (Method[])allMethodInfo.keySet().toArray(
            new Method[0] ) ;
        for(Iterator iter = allMethodInfo.values().iterator();
            iter.hasNext();) {
            IDLMethodInfo next = (IDLMethodInfo) iter.next();
            String idlName = next.mangledName;
            if( next.isProperty ) {
                String origMethodName = next.method.getName();
                String prefix = "";
                if( origMethodName.startsWith("get") ) {
                    prefix = GET_ATTRIBUTE_PREFIX;
                } else if( origMethodName.startsWith("set") ) {
                    prefix = SET_ATTRIBUTE_PREFIX;
                } else {
                    prefix = IS_ATTRIBUTE_PREFIX;
                }
                idlName = prefix + next.mangledName;
            }
            methodToIDLNameMap_.put(next.method, idlName);
            if( IDLNameToMethodMap_.containsKey(idlName) ) {
                Method clash = (Method) IDLNameToMethodMap_.get(idlName);
                throw new IllegalStateException("Error : methods " +
                    clash + " and " + next.method +
                    " both result in IDL name '" + idlName + "'");
            } else {
                IDLNameToMethodMap_.put(idlName, next.method);
            }
        }
        return;
    }
    private static String mangleIdentifier(String identifier) {
        return mangleIdentifier(identifier, false);
    }
    private static String mangleIdentifier(String identifier, boolean attribute) {
        String mangledName = identifier;
        if( hasLeadingUnderscore(mangledName) ) {
            mangledName = mangleLeadingUnderscore(mangledName);
        }
        if( !attribute && isIDLKeyword(mangledName) ) {
            mangledName = mangleIDLKeywordClash(mangledName);
        }
        if( !isIDLIdentifier(mangledName) ) {
            mangledName = mangleUnicodeChars(mangledName);
        }
        return mangledName;
    }
    static boolean isIDLKeyword(String identifier) {
        String identifierAllCaps = identifier.toUpperCase();
        return idlKeywords_.contains(identifierAllCaps);
    }
    static String mangleIDLKeywordClash(String identifier) {
        return UNDERSCORE + identifier;
    }
    private static String mangleLeadingUnderscore(String identifier) {
        return LEADING_UNDERSCORE_CHAR + identifier;
    }
    private static boolean hasLeadingUnderscore(String identifier) {
        return identifier.startsWith(UNDERSCORE);
    }
    static String mangleUnicodeChars(String identifier) {
        StringBuffer mangledIdentifier = new StringBuffer();
        for(int i = 0; i < identifier.length(); i++) {
            char nextChar = identifier.charAt(i);
            if( isIDLIdentifierChar(nextChar) ) {
                mangledIdentifier.append(nextChar);
            } else {
                String unicode = charToUnicodeRepresentation(nextChar);
                mangledIdentifier.append(unicode);
            }
        }
        return mangledIdentifier.toString();
    }
    String mangleCaseSensitiveCollision(String identifier) {
        StringBuffer mangledIdentifier = new StringBuffer(identifier);
        mangledIdentifier.append(UNDERSCORE);
        boolean needUnderscore = false;
        for(int i = 0; i < identifier.length(); i++) {
            char next = identifier.charAt(i);
            if( Character.isUpperCase(next) ) {
                if( needUnderscore ) {
                    mangledIdentifier.append(UNDERSCORE);
                }
                mangledIdentifier.append(i);
                needUnderscore = true;
            }
        }
        return mangledIdentifier.toString();
    }
    private static String mangleContainerClash(String identifier) {
        return identifier + ID_CONTAINER_CLASH_CHAR;
    }
    private static boolean identifierClashesWithContainer
        (String mappedContainerName, String identifier) {
        return identifier.equalsIgnoreCase(mappedContainerName);
    }
    public static String charToUnicodeRepresentation(char c) {
        int orig = (int) c;
        StringBuffer hexString = new StringBuffer();
        int value = orig;
        while( value > 0 ) {
            int div = value / 16;
            int mod = value % 16;
            hexString.insert(0, HEX_DIGITS[mod]);
            value = div;
        }
        int numZerosToAdd = 4 - hexString.length();
        for(int i = 0; i < numZerosToAdd; i++) {
            hexString.insert(0, "0");
        }
        hexString.insert(0, "U");
        return hexString.toString();
    }
    private static boolean isIDLIdentifier(String identifier) {
        boolean isIdentifier = true;
        for(int i = 0; i < identifier.length(); i++) {
            char nextChar = identifier.charAt(i);
            isIdentifier  = (i == 0) ?
                isIDLAlphabeticChar(nextChar) :
                isIDLIdentifierChar(nextChar);
            if( !isIdentifier ) {
                break;
            }
        }
        return isIdentifier;
    }
    private static boolean isIDLIdentifierChar(char c) {
        return (isIDLAlphabeticChar(c) ||
                isIDLDecimalDigit(c)   ||
                isUnderscore(c));
    }
    private static boolean isIDLAlphabeticChar(char c) {
        boolean alphaChar =
            (
             ((c >= 0x0041) && (c <= 0x005A))
             ||
             ((c >= 0x0061) && (c <= 0x007A))
             ||
             ((c >= 0x00C0) && (c <= 0x00FF)
              && (c != 0x00D7) && (c != 0x00F7)));
        return alphaChar;
    }
    private static boolean isIDLDecimalDigit(char c) {
        return ( (c >= 0x0030) && (c <= 0x0039) );
    }
    private static boolean isUnderscore(char c) {
        return ( c == 0x005F );
    }
    private static String mangleOverloadedMethod(String mangledName, Method m) {
        IDLTypesUtil idlTypesUtil = new IDLTypesUtil();
        String newMangledName = mangledName + OVERLOADED_TYPE_SEPARATOR;
        Class[] parameterTypes = m.getParameterTypes();
        for(int i = 0; i < parameterTypes.length; i++) {
            Class nextParamType = parameterTypes[i];
            if( i > 0 ) {
                newMangledName = newMangledName + OVERLOADED_TYPE_SEPARATOR;
            }
            IDLType idlType = classToIDLType(nextParamType);
            String moduleName = idlType.getModuleName();
            String memberName = idlType.getMemberName();
            String typeName = (moduleName.length() > 0) ?
                moduleName + UNDERSCORE + memberName : memberName;
            if( !idlTypesUtil.isPrimitive(nextParamType) &&
                (idlTypesUtil.getSpecialCaseIDLTypeMapping(nextParamType)
                 == null) &&
                isIDLKeyword(typeName) ) {
                typeName = mangleIDLKeywordClash(typeName);
            }
            typeName = mangleUnicodeChars(typeName);
            newMangledName = newMangledName + typeName;
        }
        return newMangledName;
    }
    private static IDLType classToIDLType(Class c) {
        IDLType idlType = null;
        IDLTypesUtil idlTypesUtil = new IDLTypesUtil();
        if( idlTypesUtil.isPrimitive(c) ) {
            idlType = idlTypesUtil.getPrimitiveIDLTypeMapping(c);
        } else if( c.isArray() ) {
            Class componentType = c.getComponentType();
            int numArrayDimensions = 1;
            while(componentType.isArray()) {
                componentType = componentType.getComponentType();
                numArrayDimensions++;
            }
            IDLType componentIdlType = classToIDLType(componentType);
            String[] modules = BASE_IDL_ARRAY_MODULE_TYPE;
            if( componentIdlType.hasModule() ) {
                modules = (String[])ObjectUtility.concatenateArrays( modules,
                    componentIdlType.getModules() ) ;
            }
            String memberName = BASE_IDL_ARRAY_ELEMENT_TYPE +
                numArrayDimensions + UNDERSCORE +
                componentIdlType.getMemberName();
            idlType = new IDLType(c, modules, memberName);
        } else {
            idlType = idlTypesUtil.getSpecialCaseIDLTypeMapping(c);
            if (idlType == null) {
                String memberName = getUnmappedContainerName(c);
                memberName = memberName.replaceAll("\\$",
                                                   INNER_CLASS_SEPARATOR);
                if( hasLeadingUnderscore(memberName) ) {
                    memberName = mangleLeadingUnderscore(memberName);
                }
                String packageName = getPackageName(c);
                if (packageName == null) {
                    idlType = new IDLType( c, memberName ) ;
                } else {
                    if (idlTypesUtil.isEntity(c)) {
                        packageName = "org.omg.boxedIDL." + packageName ;
                    }
                    StringTokenizer tokenizer =
                        new StringTokenizer(packageName, ".");
                    String[] modules = new String[ tokenizer.countTokens() ] ;
                    int index = 0 ;
                    while (tokenizer.hasMoreElements()) {
                        String next = tokenizer.nextToken();
                        String moreMangled = hasLeadingUnderscore( next ) ?
                            mangleLeadingUnderscore( next ) : next;
                        modules[index++] = moreMangled ;
                    }
                    idlType = new IDLType(c, modules, memberName);
                }
            }
        }
        return idlType;
    }
    private static String getPackageName(Class c) {
        Package thePackage = c.getPackage();
        String packageName = null;
        if( thePackage != null ) {
            packageName = thePackage.getName();
        } else {
            String fullyQualifiedClassName = c.getName();
            int lastDot = fullyQualifiedClassName.indexOf('.');
            packageName = (lastDot == -1) ? null :
                fullyQualifiedClassName.substring(0, lastDot);
        }
        return packageName;
    }
    private static String getMappedContainerName(Class c) {
        String unmappedName = getUnmappedContainerName(c);
        return mangleIdentifier(unmappedName);
    }
    private static String getUnmappedContainerName(Class c) {
        String memberName  = null;
        String packageName = getPackageName(c);
        String fullyQualifiedClassName = c.getName();
        if( packageName != null ) {
            int packageLength = packageName.length();
            memberName = fullyQualifiedClassName.substring(packageLength + 1);
        } else {
            memberName = fullyQualifiedClassName;
        }
        return memberName;
    }
    private static class IDLMethodInfo
    {
        public Method method;
        public boolean isProperty;
        public String originalName;
        public String mangledName;
    }
    public String toString() {
        StringBuffer contents = new StringBuffer();
        contents.append("IDLNameTranslator[" );
        for( int ctr=0; ctr<interf_.length; ctr++) {
            if (ctr != 0)
                contents.append( " " ) ;
            contents.append( interf_[ctr].getName() ) ;
        }
        contents.append("]\n");
        for(Iterator iter = methodToIDLNameMap_.keySet().iterator();
            iter.hasNext();) {
            Method method  = (Method) iter.next();
            String idlName = (String) methodToIDLNameMap_.get(method);
            contents.append(idlName + ":" + method + "\n");
        }
        return contents.toString();
    }
    public static void main(String[] args) {
        Class remoteInterface = java.rmi.Remote.class;
        if( args.length > 0 ) {
            String className = args[0];
            try {
                remoteInterface = Class.forName(className);
            } catch(Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }
        System.out.println("Building name translation for " + remoteInterface);
        try {
            IDLNameTranslator nameTranslator =
                IDLNameTranslatorImpl.get(remoteInterface);
            System.out.println(nameTranslator);
        } catch(IllegalStateException ise) {
            ise.printStackTrace();
        }
    }
}
