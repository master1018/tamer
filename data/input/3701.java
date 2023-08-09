public class IDLNames implements sun.rmi.rmic.iiop.Constants {
    public static final byte ASCII_HEX[] =      {
        (byte)'0',
        (byte)'1',
        (byte)'2',
        (byte)'3',
        (byte)'4',
        (byte)'5',
        (byte)'6',
        (byte)'7',
        (byte)'8',
        (byte)'9',
        (byte)'A',
        (byte)'B',
        (byte)'C',
        (byte)'D',
        (byte)'E',
        (byte)'F',
    };
    public static String getMemberOrMethodName (NameContext nameContext,
                                                String name,
                                                BatchEnvironment env) {
        String result = (String) env.namesCache.get(name);
        if (result == null) {
            result = nameContext.get(name);
            result = convertLeadingUnderscores(result);
            result = convertIDLKeywords(result);
            result = convertToISOLatin1(result);
            env.namesCache.put(name,result);
        }
        return result;
    }
    public static String convertToISOLatin1 (String name) {
        String result = replace(name,"x\\u","U");
        result = replace(result,"x\\U","U");
        int length = result.length();
        StringBuffer buffer = null;
        for (int i = 0; i < length; i++) {
            char c = result.charAt(i);
            if (c > 255 || RepositoryId.IDL_IDENTIFIER_CHARS[c] == 0) {
                if (buffer == null) {
                    buffer = new StringBuffer(result.substring(0,i));
                }
                buffer.append("U");
                buffer.append((char)ASCII_HEX[(c & 0xF000) >>> 12]);
                buffer.append((char)ASCII_HEX[(c & 0x0F00) >>> 8]);
                buffer.append((char)ASCII_HEX[(c & 0x00F0) >>> 4]);
                buffer.append((char)ASCII_HEX[(c & 0x000F)]);
            } else {
                if (buffer != null) {
                    buffer.append(c);
                }
            }
        }
        if (buffer != null) {
            result = buffer.toString();
        }
        return result;
    }
    public static String convertIDLKeywords (String name) {
        for (int i = 0; i < IDL_KEYWORDS.length; i++) {
            if (name.equalsIgnoreCase(IDL_KEYWORDS[i])) {
                return "_" + name;
            }
        }
        return name;
    }
    public static String convertLeadingUnderscores (String name) {
        if (name.startsWith("_")) {
            return "J" + name;
        }
        return name;
    }
    public static String getClassOrInterfaceName (Identifier id,
                                                  BatchEnvironment env) throws Exception {
        String typeName = id.getName().toString();
        String packageName = null;
        if (id.isQualified()) {
            packageName = id.getQualifier().toString();
        }
        String result = (String) env.namesCache.get(typeName);
        if (result == null) {
            result = replace(typeName,". ","__");
            result = convertToISOLatin1(result);
            NameContext context = NameContext.forName(packageName,false,env);
            context.assertPut(result);
            result = getTypeOrModuleName(result);
            env.namesCache.put(typeName,result);
        }
        return result;
    }
    public static String getExceptionName (String idlName) {
        String result = idlName;
        if (idlName.endsWith(EXCEPTION_SUFFIX)) {
            result = stripLeadingUnderscore(idlName.substring(0,idlName.lastIndexOf(EXCEPTION_SUFFIX)) + EX_SUFFIX);
        } else {
            result = idlName + EX_SUFFIX;
        }
        return result;
    }
    public static String[] getModuleNames (Identifier theID,
                                           boolean boxIt,
                                           BatchEnvironment env) throws Exception {
        String[] result = null;
        if (theID.isQualified()) {
            Identifier id = theID.getQualifier();
            env.modulesContext.assertPut(id.toString());
            int count = 1;
            Identifier current = id;
            while (current.isQualified()) {
                current = current.getQualifier();
                count++;
            }
            result = new String[count];
            int index = count-1;
            current = id;
            for (int i = 0; i < count; i++) {
                String item = current.getName().toString();
                String cachedItem = (String) env.namesCache.get(item);
                if (cachedItem == null) {
                    cachedItem = convertToISOLatin1(item);
                    cachedItem = getTypeOrModuleName(cachedItem);
                    env.namesCache.put(item,cachedItem);
                }
                result[index--] = cachedItem;
                current = current.getQualifier();
            }
        }
        if (boxIt) {
            if (result == null) {
                result = IDL_BOXEDIDL_MODULE;
            } else {
            String[] boxed = new String[result.length+IDL_BOXEDIDL_MODULE.length];
            System.arraycopy(IDL_BOXEDIDL_MODULE,0,boxed,0,IDL_BOXEDIDL_MODULE.length);
            System.arraycopy(result,0,boxed,IDL_BOXEDIDL_MODULE.length,result.length);
            result = boxed;
        }
        }
        return result;
    }
    public static String getArrayName (Type theType, int arrayDimension) {
        StringBuffer idlName = new StringBuffer(64);
        idlName.append(IDL_SEQUENCE);
        idlName.append(Integer.toString(arrayDimension));
        idlName.append("_");
        idlName.append(replace(stripLeadingUnderscore(theType.getIDLName())," ","_"));
        return idlName.toString();
    }
    public static String[] getArrayModuleNames (Type theType) {
        String[] moduleName;
        String[] typeModule = theType.getIDLModuleNames();
        int typeModuleLength = typeModule.length;
        if (typeModuleLength == 0) {
            moduleName = IDL_SEQUENCE_MODULE;
        } else {
            moduleName = new String[typeModuleLength + IDL_SEQUENCE_MODULE.length];
            System.arraycopy(IDL_SEQUENCE_MODULE,0,moduleName,0,IDL_SEQUENCE_MODULE.length);
            System.arraycopy(typeModule,0,moduleName,IDL_SEQUENCE_MODULE.length,typeModuleLength);
        }
        return moduleName;
    }
    private static int getInitialAttributeKind (CompoundType.Method method,
                                                BatchEnvironment env) throws ClassNotFound {
        int result = ATTRIBUTE_NONE;
        if (!method.isConstructor()) {
            boolean validExceptions = true;
            ClassType[] exceptions = method.getExceptions();
            if (exceptions.length > 0) {
                for (int i = 0; i < exceptions.length; i++) {
                    if (exceptions[i].isCheckedException() &&
                        !exceptions[i].isRemoteExceptionOrSubclass()) {
                        validExceptions = false;
                        break;
                    }
                }
            } else {
                validExceptions = method.getEnclosing().isType(TYPE_VALUE);
            }
            if (validExceptions) {
                String name = method.getName();
                int nameLength = name.length();
                int argCount = method.getArguments().length;
                Type returnType = method.getReturnType();
                boolean voidReturn = returnType.isType(TYPE_VOID);
                boolean booleanReturn = returnType.isType(TYPE_BOOLEAN);
                if (name.startsWith("get") && nameLength > 3 && argCount == 0 && !voidReturn) {
                    result = ATTRIBUTE_GET;
                } else {
                    if (name.startsWith("is") && nameLength > 2 && argCount == 0 && booleanReturn) {
                        result = ATTRIBUTE_IS;
                    } else {
                        if (name.startsWith("set") && nameLength > 3 && argCount == 1 && voidReturn) {
                            result = ATTRIBUTE_SET;
                        }
                    }
                }
            }
        }
        return result;
    }
    private static void setAttributeKinds (CompoundType.Method[] methods,
                                           int[] kinds,
                                           String[] names) {
        int count = methods.length;
        for (int i = 0; i < count; i++) {
            switch (kinds[i]) {
                case ATTRIBUTE_GET: names[i] = names[i].substring(3); break;
                case ATTRIBUTE_IS: names[i] = names[i].substring(2); break;
                case ATTRIBUTE_SET: names[i] = names[i].substring(3); break;
            }
        }
        for (int i = 0; i < count; i++) {
            if (kinds[i] == ATTRIBUTE_IS) {
                for (int j = 0; j < count; j++) {
                    if (j != i &&
                        (kinds[j] == ATTRIBUTE_GET || kinds[j] == ATTRIBUTE_SET) &&
                        names[i].equals(names[j])) {
                        Type isType = methods[i].getReturnType();
                        Type targetType;
                        if (kinds[j] == ATTRIBUTE_GET) {
                            targetType = methods[j].getReturnType();
                        } else {
                            targetType = methods[j].getArguments()[0];
                        }
                        if (!isType.equals(targetType)) {
                            kinds[i] = ATTRIBUTE_NONE;
                            names[i] = methods[i].getName();
                            break;
                        }
                    }
                }
            }
        }
        for (int i = 0; i < count; i++) {
            if (kinds[i] == ATTRIBUTE_SET) {
                int getterIndex = -1;
                int isGetterIndex = -1;
                for (int j = 0; j < count; j++) {
                    if (j != i && names[i].equals(names[j])) {
                        Type getterReturn = methods[j].getReturnType();
                        Type setterArg = methods[i].getArguments()[0];
                        if (getterReturn.equals(setterArg)) {
                            if (kinds[j] == ATTRIBUTE_IS) {
                                isGetterIndex = j;
                            } else if (kinds[j] == ATTRIBUTE_GET) {
                                getterIndex = j;
                            }
                        }
                    }
                }
                if (getterIndex > -1) {
                    if (isGetterIndex > -1) {
                        kinds[isGetterIndex] = ATTRIBUTE_IS_RW;
                        methods[isGetterIndex].setAttributePairIndex(i);
                        methods[i].setAttributePairIndex(isGetterIndex);
                        kinds[getterIndex] = ATTRIBUTE_NONE;
                        names[getterIndex] = methods[getterIndex].getName();
                    } else {
                        kinds[getterIndex] = ATTRIBUTE_GET_RW;
                        methods[getterIndex].setAttributePairIndex(i);
                        methods[i].setAttributePairIndex(getterIndex);
                    }
                } else {
                    if (isGetterIndex > -1) {
                        kinds[isGetterIndex] = ATTRIBUTE_IS_RW;
                        methods[isGetterIndex].setAttributePairIndex(i);
                        methods[i].setAttributePairIndex(isGetterIndex);
                    } else {
                        kinds[i] = ATTRIBUTE_NONE;
                        names[i] = methods[i].getName();
                    }
                }
            }
        }
        for (int i = 0; i < count; i++) {
            if (kinds[i] != ATTRIBUTE_NONE) {
                String name = names[i];
                if (Character.isUpperCase(name.charAt(0))) {
                    if (name.length() == 1 || Character.isLowerCase(name.charAt(1))) {
                        StringBuffer buffer = new StringBuffer(name);
                        buffer.setCharAt(0,Character.toLowerCase(name.charAt(0)));
                        names[i] = buffer.toString();
                    }
                }
            }
            methods[i].setAttributeKind(kinds[i]);
        }
    }
    public static void setMethodNames (CompoundType container,
                                       CompoundType.Method[] allMethods,
                                       BatchEnvironment env)
        throws Exception {
        int count = allMethods.length;
        if (count == 0) return;
        String[] names = new String[count];
        for (int i = 0; i < count; i++) {
            names[i] = allMethods[i].getName();
        }
        CompoundType enclosing = allMethods[0].getEnclosing();
        if (enclosing.isType(TYPE_REMOTE) ||
            enclosing.isType(TYPE_ABSTRACT) ||
            enclosing.isType(TYPE_VALUE)) {
            int[] kinds = new int[count];
            for (int i = 0; i < count; i++) {
                kinds[i] = getInitialAttributeKind(allMethods[i],env);
            }
            setAttributeKinds(allMethods,kinds,names);
        }
        NameContext context = new NameContext(true);
        for (int i = 0; i < count; i++) {
            context.put(names[i]);
        }
        boolean haveConstructor = false;
        for (int i = 0; i < count; i++) {
            if (!allMethods[i].isConstructor()) {
                names[i] = getMemberOrMethodName(context,names[i],env);
            } else {
                names[i] = IDL_CONSTRUCTOR;
                haveConstructor = true;
            }
        }
        boolean overloaded[] = new boolean[count];
        for (int i = 0; i < count; i++) {
            overloaded[i] = (!allMethods[i].isAttribute() &&
                             !allMethods[i].isConstructor() &&
                         doesMethodCollide(names[i],allMethods[i],allMethods,names,true));
        }
        convertOverloadedMethods(allMethods,names,overloaded);
        for (int i = 0; i < count; i++) {
            overloaded[i] = (!allMethods[i].isAttribute() &&
                             allMethods[i].isConstructor() &&
                             doesConstructorCollide(names[i],allMethods[i],allMethods,names,true));
        }
        convertOverloadedMethods(allMethods,names,overloaded);
        for (int i = 0; i < count; i++) {
                CompoundType.Method method = allMethods[i];
            if (method.isAttribute() &&
                doesMethodCollide(names[i],method,allMethods,names,true)) {
                    names[i] += "__";
                }
            }
        if (haveConstructor) {
        for (int i = 0; i < count; i++) {
            CompoundType.Method method = allMethods[i];
                if (method.isConstructor() &&
                    doesConstructorCollide(names[i],method,allMethods,names,false)) {
                names[i] += "__";
            }
        }
        }
        String containerName = container.getIDLName();
        for (int i = 0; i < count; i++) {
            if (names[i].equalsIgnoreCase(containerName)) {
                if (! allMethods[i].isAttribute()) {
                    names[i] += "_";
                }
            }
        }
        for (int i = 0; i < count; i++) {
            if (doesMethodCollide(names[i],allMethods[i],allMethods,names,false)) {
                throw new Exception(allMethods[i].toString());
            }
        }
        for (int i = 0; i < count; i++) {
            CompoundType.Method method = allMethods[i];
            String wireName = names[i];
            if (method.isAttribute()) {
                wireName = ATTRIBUTE_WIRE_PREFIX[method.getAttributeKind()] +
                    stripLeadingUnderscore(wireName);
                String attributeName = names[i];
                method.setAttributeName(attributeName);
            }
            method.setIDLName(wireName);
        }
    }
    private static String stripLeadingUnderscore (String name) {
        if (name != null && name.length() > 1
            && name.charAt(0) == '_')
        {
            return name.substring(1);
        }
        return name;
    }
    private static String stripTrailingUnderscore (String name) {
        if (name != null && name.length() > 1 &&
            name.charAt(name.length() - 1) == '_')
        {
            return name.substring(0, name.length() - 1);
        }
        return name;
    }
    private static void convertOverloadedMethods(CompoundType.Method[] allMethods,
                                                 String[] names,
                                                 boolean[] overloaded) {
        for (int i = 0; i < names.length; i++) {
            if (overloaded[i]) {
                CompoundType.Method method = allMethods[i];
                Type[] args = method.getArguments();
                for (int k = 0; k < args.length; k++) {
                    names[i] += "__";
                    String argIDLName = args[k].getQualifiedIDLName(false);
                    argIDLName = replace(argIDLName,"::_","_");
                    argIDLName = replace(argIDLName,"::","_");
                    argIDLName = replace(argIDLName," ","_");
                    names[i] += argIDLName;
                }
                if (args.length == 0) {
                    names[i] += "__";
                }
                names[i] = stripLeadingUnderscore(names[i]);
            }
        }
    }
    private static boolean doesMethodCollide (String name,
                                              CompoundType.Method method,
                                              CompoundType.Method[] allMethods,
                                              String[] allNames,
                                              boolean ignoreAttributes) {
        for (int i = 0; i < allMethods.length; i++) {
            CompoundType.Method target = allMethods[i];
            if (method != target &&                                 
                !target.isConstructor() &&                      
                (!ignoreAttributes || !target.isAttribute()) && 
                name.equals(allNames[i])) {                         
                int kind1 = method.getAttributeKind();
                int kind2 = target.getAttributeKind();
                if ((kind1 != ATTRIBUTE_NONE && kind2 != ATTRIBUTE_NONE) &&
                    ((kind1 == ATTRIBUTE_SET && kind2 != ATTRIBUTE_SET) ||
                     (kind1 != ATTRIBUTE_SET && kind2 == ATTRIBUTE_SET) ||
                     (kind1 == ATTRIBUTE_IS_RW && kind2 == ATTRIBUTE_GET) ||
                     (kind1 == ATTRIBUTE_GET && kind2 == ATTRIBUTE_IS_RW))) {
                } else {
                    return true;
                }
            }
        }
        return false;
    }
    private static boolean doesConstructorCollide (String name,
                                                   CompoundType.Method method,
                                                   CompoundType.Method[] allMethods,
                                                   String[] allNames,
                                                   boolean compareConstructors) {
        for (int i = 0; i < allMethods.length; i++) {
            CompoundType.Method target = allMethods[i];
            if (method != target &&                                     
                (target.isConstructor() == compareConstructors) &&  
                name.equals(allNames[i])) {                             
                return true;
            }
        }
        return false;
    }
    public static void setMemberNames (CompoundType container,
                                       CompoundType.Member[] allMembers,
                                       CompoundType.Method[] allMethods,
                                       BatchEnvironment env)
        throws Exception {
        NameContext context = new NameContext(true);
        for (int i = 0; i < allMembers.length; i++) {
            context.put(allMembers[i].getName());
        }
        for (int i = 0; i < allMembers.length; i++) {
            CompoundType.Member member = allMembers[i];
            String idlName = getMemberOrMethodName(context,member.getName(),env);
            member.setIDLName(idlName);
        }
        String containerName = container.getIDLName();
        for (int i = 0; i < allMembers.length; i++) {
            String name = allMembers[i].getIDLName();
            if (name.equalsIgnoreCase(containerName)) {
                allMembers[i].setIDLName(name+"_");
            }
        }
        for (int i = 0; i < allMembers.length; i++) {
            String name = allMembers[i].getIDLName();
            for (int j = 0; j < allMembers.length; j++) {
                if (i != j && allMembers[j].getIDLName().equals(name)) {
                    throw new Exception(name);
                }
            }
        }
        boolean changed;
        do {
            changed = false;
            for (int i = 0; i < allMembers.length; i++) {
                String name = allMembers[i].getIDLName();
                for (int j = 0; j < allMethods.length; j++) {
                    if (allMethods[j].getIDLName().equals(name)) {
                        allMembers[i].setIDLName(name+"_");
                        changed = true;
                        break;
                    }
                }
            }
        } while (changed);
    }
    public static String getTypeName(int typeCode, boolean isConstant) {
        String idlName = null;
        switch (typeCode) {
        case TYPE_VOID:             idlName = IDL_VOID; break;
        case TYPE_BOOLEAN:          idlName = IDL_BOOLEAN; break;
        case TYPE_BYTE:             idlName = IDL_BYTE; break;
        case TYPE_CHAR:             idlName = IDL_CHAR; break;
        case TYPE_SHORT:            idlName = IDL_SHORT; break;
        case TYPE_INT:              idlName = IDL_INT; break;
        case TYPE_LONG:             idlName = IDL_LONG; break;
        case TYPE_FLOAT:            idlName = IDL_FLOAT; break;
        case TYPE_DOUBLE:           idlName = IDL_DOUBLE; break;
        case TYPE_ANY:                  idlName = IDL_ANY; break;
        case TYPE_CORBA_OBJECT: idlName = IDL_CORBA_OBJECT; break;
        case TYPE_STRING:
            {
                if (isConstant) {
                    idlName = IDL_CONSTANT_STRING;
                } else {
                    idlName = IDL_STRING;
                }
                break;
            }
        }
        return idlName;
    }
    public static String getQualifiedName (String[] idlModuleNames, String idlName) {
        String result = null;
        if (idlModuleNames != null && idlModuleNames.length > 0) {
            for (int i = 0; i < idlModuleNames.length;i++) {
                if (i == 0) {
                    result = idlModuleNames[0];
                } else {
                    result += IDL_NAME_SEPARATOR;
                    result += idlModuleNames[i];
                }
            }
            result += IDL_NAME_SEPARATOR;
            result += idlName;
        } else {
            result = idlName;
        }
        return result;
    }
    public static String replace (String source, String match, String replace) {
        int index = source.indexOf(match,0);
        if (index >=0) {
            StringBuffer result = new StringBuffer(source.length() + 16);
            int matchLength = match.length();
            int startIndex = 0;
            while (index >= 0) {
                result.append(source.substring(startIndex,index));
                result.append(replace);
                startIndex = index + matchLength;
                index = source.indexOf(match,startIndex);
            }
            if (startIndex < source.length()) {
                result.append(source.substring(startIndex));
            }
            return result.toString();
        } else {
            return source;
        }
    }
    public static String getIDLRepositoryID (String idlName) {
        return  IDL_REPOSITORY_ID_PREFIX +
            replace(idlName,"::", "/") +
            IDL_REPOSITORY_ID_VERSION;
    }
    private static String getTypeOrModuleName (String name) {
        String result = convertLeadingUnderscores(name);
        return convertIDLKeywords(result);
    }
}
