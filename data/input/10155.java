public class RepositoryId_1_3 {
    public static final RepositoryIdCache_1_3 cache = new RepositoryIdCache_1_3();
    public static final byte[] IDL_IDENTIFIER_CHARS = {
        0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 
        0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 
        0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,1,0, 
        1,1,1,1, 1,1,1,1, 1,1,0,0, 0,0,0,0, 
        0,1,1,1, 1,1,1,1, 1,1,1,1, 1,1,1,1, 
        1,1,1,1, 1,1,1,1, 1,1,1,0, 0,0,0,1, 
        0,1,1,1, 1,1,1,1, 1,1,1,1, 1,1,1,1, 
        1,1,1,1, 1,1,1,1, 1,1,1,0, 0,0,0,0, 
        0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 
        0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 
        0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 
        0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 
        1,1,1,1, 1,1,1,1, 1,1,1,1, 1,1,1,1, 
        0,1,1,1, 1,1,1,0, 1,1,1,1, 1,0,0,1, 
        1,1,1,1, 1,1,1,1, 1,1,1,1, 1,1,1,1, 
        0,1,1,1, 1,1,1,0, 1,1,1,1, 1,0,0,1, 
    };
    private static String defaultServerURL = null;
    private static boolean useCodebaseOnly = false;
    static {
        if (defaultServerURL == null)
            defaultServerURL = (String)JDKBridge.getLocalCodebase();
        useCodebaseOnly = JDKBridge.useCodebaseOnly();
    }
    private static IdentityHashtable classToRepStr = new IdentityHashtable();
    private static IdentityHashtable classIDLToRepStr = new IdentityHashtable();
    private static IdentityHashtable classSeqToRepStr = new IdentityHashtable();
    private static IdentityHashtable repStrToByteArray = new IdentityHashtable();
    private static Hashtable repStrToClass = new Hashtable();
    private String repId = null;
    private boolean isSupportedFormat = true;
    private String typeString = null;
    private String versionString = null;
    private boolean isSequence = false;
    private boolean isRMIValueType = false;
    private boolean isIDLType = false;
    private String completeClassName = null;
    private String unqualifiedName = null;
    private String definedInId = null;
    private Class clazz = null;
    private String suid = null, actualSuid = null;
    private long suidLong = ObjectStreamClass.kDefaultUID, actualSuidLong = ObjectStreamClass.kDefaultUID;
    private static final String kValuePrefix = "RMI:";
    private static final String kIDLPrefix = "IDL:";
    private static final String kIDLNamePrefix = "omg.org/";
    private static final String kIDLClassnamePrefix = "org.omg.";
    private static final String kSequencePrefix = "[";
    private static final String kCORBAPrefix = "CORBA/";
    private static final String kArrayPrefix = kValuePrefix + kSequencePrefix + kCORBAPrefix;
    private static final int kValuePrefixLength = kValuePrefix.length();
    private static final int kIDLPrefixLength = kIDLPrefix.length();
    private static final int kSequencePrefixLength = kSequencePrefix.length();
    private static final String kInterfaceHashCode = ":0000000000000000";
    private static final String kInterfaceOnlyHashStr = "0000000000000000";
    private static final String kExternalizableHashStr = "0000000000000001";
    public static final int kInitialValueTag= 0x7fffff00;
    public static final int kNoTypeInfo = 0;
    public static final int kSingleRepTypeInfo = 0x02;
    public static final int  kPartialListTypeInfo = 0x06;
    public static final int  kChunkedMask = 0x08;
    public static final String kWStringValueVersion = "1.0";
    public static final String kWStringValueHash = ":"+kWStringValueVersion;
    public static final String kWStringStubValue = "WStringValue";
    public static final String kWStringTypeStr = "omg.org/CORBA/"+kWStringStubValue;
    public static final String kWStringValueRepID = kIDLPrefix + kWStringTypeStr + kWStringValueHash;
    public static final String kAnyRepID = kIDLPrefix + "omg.org/CORBA/Any";
    public static final String kClassDescValueHash = ":" + Long.toHexString(
       ObjectStreamClass.getSerialVersionUID(javax.rmi.CORBA.ClassDesc.class));
    public static final String kClassDescStubValue = "ClassDesc";
    public static final String kClassDescTypeStr = "javax.rmi.CORBA."+kClassDescStubValue;
    public static final String kClassDescValueRepID = kValuePrefix + kClassDescTypeStr + kClassDescValueHash;
    public static final String kObjectValueHash = ":1.0";
    public static final String kObjectStubValue = "Object";
    public static final String kSequenceValueHash = ":1.0";
    public static final String kPrimitiveSequenceValueHash = ":0000000000000000";
    public static final String kSerializableValueHash = ":1.0";
    public static final String kSerializableStubValue = "Serializable";
    public static final String kExternalizableValueHash = ":1.0";
    public static final String kExternalizableStubValue = "Externalizable";
    public static final String kRemoteValueHash = "";
    public static final String kRemoteStubValue = "";
    public static final String kRemoteTypeStr = "";
    public static final String kRemoteValueRepID = "";
    public static final Hashtable kSpecialArrayTypeStrings = new Hashtable();
    static {
        kSpecialArrayTypeStrings.put("CORBA.WStringValue", new StringBuffer(java.lang.String.class.getName()));
        kSpecialArrayTypeStrings.put("javax.rmi.CORBA.ClassDesc", new StringBuffer(java.lang.Class.class.getName()));
        kSpecialArrayTypeStrings.put("CORBA.Object", new StringBuffer(java.rmi.Remote.class.getName()));
    }
    public static final Hashtable kSpecialCasesRepIDs = new Hashtable();
    static {
        kSpecialCasesRepIDs.put(java.lang.String.class, kWStringValueRepID);
        kSpecialCasesRepIDs.put(java.lang.Class.class, kClassDescValueRepID);
        kSpecialCasesRepIDs.put(java.rmi.Remote.class, kRemoteValueRepID);
    }
    public static final Hashtable kSpecialCasesStubValues = new Hashtable();
    static {
        kSpecialCasesStubValues.put(java.lang.String.class, kWStringStubValue);
        kSpecialCasesStubValues.put(java.lang.Class.class, kClassDescStubValue);
        kSpecialCasesStubValues.put(java.lang.Object.class, kObjectStubValue);
        kSpecialCasesStubValues.put(java.io.Serializable.class, kSerializableStubValue);
        kSpecialCasesStubValues.put(java.io.Externalizable.class, kExternalizableStubValue);
        kSpecialCasesStubValues.put(java.rmi.Remote.class, kRemoteStubValue);
    }
    public static final Hashtable kSpecialCasesVersions = new Hashtable();
    static {
        kSpecialCasesVersions.put(java.lang.String.class, kWStringValueHash);
        kSpecialCasesVersions.put(java.lang.Class.class, kClassDescValueHash);
        kSpecialCasesVersions.put(java.lang.Object.class, kObjectValueHash);
        kSpecialCasesVersions.put(java.io.Serializable.class, kSerializableValueHash);
        kSpecialCasesVersions.put(java.io.Externalizable.class, kExternalizableValueHash);
        kSpecialCasesVersions.put(java.rmi.Remote.class, kRemoteValueHash);
    }
    public static final Hashtable kSpecialCasesClasses = new Hashtable();
    static {
        kSpecialCasesClasses.put(kWStringTypeStr, java.lang.String.class);
        kSpecialCasesClasses.put(kClassDescTypeStr, java.lang.Class.class);
        kSpecialCasesClasses.put(kRemoteTypeStr, java.rmi.Remote.class);
        kSpecialCasesClasses.put("org.omg.CORBA.WStringValue", java.lang.String.class);
        kSpecialCasesClasses.put("javax.rmi.CORBA.ClassDesc", java.lang.Class.class);
    }
    public static final Hashtable kSpecialCasesArrayPrefix = new Hashtable();
    static {
        kSpecialCasesArrayPrefix.put(java.lang.String.class, kValuePrefix + kSequencePrefix + kCORBAPrefix);
        kSpecialCasesArrayPrefix.put(java.lang.Class.class, kValuePrefix + kSequencePrefix + "javax/rmi/CORBA/");
        kSpecialCasesArrayPrefix.put(java.lang.Object.class, kValuePrefix + kSequencePrefix + "java/lang/");
        kSpecialCasesArrayPrefix.put(java.io.Serializable.class, kValuePrefix + kSequencePrefix + "java/io/");
        kSpecialCasesArrayPrefix.put(java.io.Externalizable.class, kValuePrefix + kSequencePrefix + "java/io/");
        kSpecialCasesArrayPrefix.put(java.rmi.Remote.class, kValuePrefix + kSequencePrefix + kCORBAPrefix);
    }
    public static final Hashtable kSpecialPrimitives = new Hashtable();
    static {
        kSpecialPrimitives.put("int","long");
        kSpecialPrimitives.put("long","longlong");
        kSpecialPrimitives.put("byte","octet");
    }
    private static final byte ASCII_HEX[] =     {
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
    public static final String kjava_rmi_Remote = createForAnyType(java.rmi.Remote.class);
    public static final String korg_omg_CORBA_Object = createForAnyType(org.omg.CORBA.Object.class);
    public static final Class kNoParamTypes[] ={};
    public static final Object kNoArgs[] = {};
    RepositoryId_1_3(){}
    RepositoryId_1_3(String aRepId){
        init(aRepId);
    }
    RepositoryId_1_3 init(String aRepId){
        this.repId = aRepId;
        if (aRepId.length() == 0) {
            clazz = java.rmi.Remote.class;
            typeString = "";
            isRMIValueType = true;
            suid = kInterfaceOnlyHashStr;
            return this;
        }
        else if (aRepId.equals(kWStringValueRepID)) {
            clazz = java.lang.String.class;
            typeString = kWStringTypeStr;
            isIDLType = true;
            versionString = kWStringValueVersion;
            return this;
        }
        else {
        String repId = convertFromISOLatin1(aRepId);
        versionString = repId.substring(repId.indexOf(':', repId.indexOf(':')+1));
        if (repId.startsWith(kIDLPrefix)) {
            typeString =
                repId.substring(kIDLPrefixLength, repId.indexOf(':', kIDLPrefixLength));
            isIDLType = true;
            if (typeString.startsWith(kIDLNamePrefix))
                completeClassName = kIDLClassnamePrefix +
                    typeString.substring(kIDLNamePrefix.length()).replace('/','.');
            else completeClassName = typeString.replace('/','.');
        }
        else if (repId.startsWith(kValuePrefix)) {
            typeString =
                repId.substring(kValuePrefixLength, repId.indexOf(':', kValuePrefixLength));
            isRMIValueType = true;
            if (versionString.indexOf('.') == -1) {
                    actualSuid = versionString.substring(1);
                    suid = actualSuid;  
                    if (actualSuid.indexOf(':') != -1){
                        int pos = actualSuid.indexOf(':')+1;
                        suid = actualSuid.substring(pos);
                        actualSuid = actualSuid.substring(0, pos-1);
                }
            }
            else {
            }
        }
        else isSupportedFormat = false;
        if (typeString.startsWith(kSequencePrefix)) {
            isSequence = true;
        }
        return this;
    }
    }
    public final String getUnqualifiedName() {
        if (unqualifiedName == null){
            String className = getClassName();
            int index = (className != null) ? className.lastIndexOf('.') : -1;
            if (index == -1){
                unqualifiedName = className;
                definedInId = "IDL::1.0";
            }
            else {
                unqualifiedName = className.substring(index);
                definedInId = "IDL:" + className.substring(0, index).replace('.','/') + ":1.0";
            }
        }
        return unqualifiedName;
    }
    public final String getDefinedInId() {
        if (definedInId == null){
            getUnqualifiedName();
        }
        return definedInId;
    }
    public final String getTypeString() {
        return typeString;
    }
    public final String getVersionString() {
        return versionString;
    }
    public final String getSerialVersionUID() {
        return suid;
    }
    public final String getActualSerialVersionUID() {
        return actualSuid;
    }
    public final long getSerialVersionUIDAsLong() {
        return suidLong;
    }
    public final long getActualSerialVersionUIDAsLong() {
        return actualSuidLong;
    }
    public final boolean isRMIValueType() {
        return isRMIValueType;
    }
    public final boolean isIDLType() {
        return isIDLType;
    }
    public final String getRepositoryId() {
        return repId;
    }
    public static byte[] getByteArray(String repStr) {
        synchronized (repStrToByteArray){
            return (byte[]) repStrToByteArray.get(repStr);
        }
    }
    public static void setByteArray(String repStr, byte[] repStrBytes) {
        synchronized (repStrToByteArray){
            repStrToByteArray.put(repStr, repStrBytes);
        }
    }
    public final boolean isSequence() {
        return isSequence;
    }
    public final boolean isSupportedFormat() {
        return isSupportedFormat;
    }
    public final String getClassName() {
        if (isRMIValueType)
            return typeString;
        else if (isIDLType)
            return completeClassName;
        else return null;
    }
    public final Class getAnyClassFromType() throws ClassNotFoundException {
        try {
            return getClassFromType();
        } catch (ClassNotFoundException cnfe) {
            Class clz = (Class)repStrToClass.get(repId);
            if (clz != null)
                return clz;
            else
                throw cnfe;
        }
    }
    public final Class getClassFromType()
        throws ClassNotFoundException {
        if (clazz != null)
            return clazz;
        Class specialCase = (Class)kSpecialCasesClasses.get(getClassName());
        if (specialCase != null){
            clazz = specialCase;
            return specialCase;
        }
        else
            {
                try{
                    return Util.loadClass(getClassName(), null, null);
                }
                catch(ClassNotFoundException cnfe){
                    if (defaultServerURL != null) {
                        try{
                            return getClassFromType(defaultServerURL);
                        }
                        catch(MalformedURLException mue){
                            throw cnfe;
                        }
                    }
                    else throw cnfe;
                }
            }
    }
    public final Class getClassFromType(Class expectedType, String codebase)
        throws ClassNotFoundException {
        if (clazz != null)
            return clazz;
        Class specialCase = (Class)kSpecialCasesClasses.get(getClassName());
        if (specialCase != null){
            clazz = specialCase;
            return specialCase;
        } else {
            ClassLoader expectedTypeClassLoader = (expectedType == null ? null : expectedType.getClassLoader());
            return loadClassOfType(getClassName(),
                                            codebase,
                                            expectedTypeClassLoader,
                                            expectedType,
                                            expectedTypeClassLoader);
        }
    }
    public final Class getClassFromType(String url)
        throws ClassNotFoundException, MalformedURLException {
        return Util.loadClass(getClassName(), url, null);
    }
    public final String toString() {
        return repId;
    }
    private static String createHashString(java.io.Serializable ser) {
        return createHashString(ser.getClass());
    }
    private static String createHashString(java.lang.Class clazz) {
        if (clazz.isInterface() || !java.io.Serializable.class.isAssignableFrom(clazz))
            return kInterfaceHashCode;
        long actualLong = ObjectStreamClassUtil_1_3.computeStructuralUID(false, clazz);
        String hash = null;
        if (actualLong == 0)
            hash = kInterfaceOnlyHashStr;
        else if (actualLong == 1)
            hash = kExternalizableHashStr;
        else
            hash = Long.toHexString(actualLong).toUpperCase();
        while(hash.length() < 16){
            hash = "0" + hash;
        }
        long declaredLong = ObjectStreamClassUtil_1_3.computeSerialVersionUID(clazz);
        String declared = null;
        if (declaredLong == 0)
            declared = kInterfaceOnlyHashStr;
        else if (declaredLong == 1)
            declared = kExternalizableHashStr;
        else
            declared = Long.toHexString(declaredLong).toUpperCase();
        while (declared.length() < 16){
            declared = "0" + declared;
    }
        hash = hash + ":" + declared;
        return ":" + hash;
    }
    public static String createSequenceRepID(java.lang.Object ser){
        return createSequenceRepID(ser.getClass());
    }
    public static String createSequenceRepID(java.lang.Class clazz){
        synchronized (classSeqToRepStr){
        String repid = (String)classSeqToRepStr.get(clazz);
        if (repid != null)
            return repid;
        Class originalClazz = clazz;
        Class type = null;
        int numOfDims = 0;
        while ((type = clazz.getComponentType()) != null) {
            numOfDims++;
            clazz = type;
        }
        if (clazz.isPrimitive())
            repid = kValuePrefix + originalClazz.getName() + kPrimitiveSequenceValueHash;
        else {
            StringBuffer buf = new StringBuffer();
            buf.append(kValuePrefix);
            while(numOfDims-- > 0) {
                buf.append("[");
            }
            buf.append("L");
            buf.append(convertToISOLatin1(clazz.getName()));
            buf.append(";");
            buf.append(createHashString(clazz));
            repid = buf.toString();
        }
        classSeqToRepStr.put(originalClazz,repid);
        return repid;
        }
    }
    public static String createForSpecialCase(java.lang.Class clazz){
        if (clazz.isArray()){
            return createSequenceRepID(clazz);
        }
        else {
            return (String)kSpecialCasesRepIDs.get(clazz);
        }
    }
    public static String createForSpecialCase(java.io.Serializable ser){
        Class clazz = ser.getClass();
        if (clazz.isArray()){
            return createSequenceRepID(ser);
        }
        else
            return createForSpecialCase(clazz);
    }
    public static String createForJavaType(java.io.Serializable ser)
        throws com.sun.corba.se.impl.io.TypeMismatchException
    {
        synchronized (classToRepStr) {
        String repid = createForSpecialCase(ser);
        if (repid != null)
            return repid;
        Class clazz = ser.getClass();
        repid = (String)classToRepStr.get(clazz);
        if (repid != null)
            return repid;
        repid = kValuePrefix + convertToISOLatin1(clazz.getName()) +
            createHashString(clazz);
        classToRepStr.put(clazz, repid);
            repStrToClass.put(repid, clazz);
        return repid;
    }
    }
    public static String createForJavaType(Class clz)
        throws com.sun.corba.se.impl.io.TypeMismatchException
    {
        synchronized (classToRepStr){
        String repid = createForSpecialCase(clz);
        if (repid != null)
            return repid;
        repid = (String)classToRepStr.get(clz);
        if (repid != null)
            return repid;
        repid = kValuePrefix + convertToISOLatin1(clz.getName()) +
            createHashString(clz);
        classToRepStr.put(clz, repid);
            repStrToClass.put(repid, clz);
        return repid;
    }
    }
    public static String createForIDLType(Class ser, int major, int minor)
        throws com.sun.corba.se.impl.io.TypeMismatchException
    {
        synchronized (classIDLToRepStr){
        String repid = (String)classIDLToRepStr.get(ser);
        if (repid != null)
            return repid;
        repid = kIDLPrefix + convertToISOLatin1(ser.getName()).replace('.','/') +
            ":" + major + "." + minor;
        classIDLToRepStr.put(ser, repid);
        return repid;
    }
    }
    private static String getIdFromHelper(Class clazz){
        try {
            Class helperClazz = Utility.loadClassForClass(clazz.getName()+"Helper", null,
                                    clazz.getClassLoader(), clazz, clazz.getClassLoader());
            Method idMethod = helperClazz.getDeclaredMethod("id", kNoParamTypes);
            return (String)idMethod.invoke(null, kNoArgs);
        }
        catch(java.lang.ClassNotFoundException cnfe)
            {
                throw new org.omg.CORBA.MARSHAL(cnfe.toString());
            }
        catch(java.lang.NoSuchMethodException nsme)
            {
                throw new org.omg.CORBA.MARSHAL(nsme.toString());
            }
        catch(java.lang.reflect.InvocationTargetException ite)
            {
                throw new org.omg.CORBA.MARSHAL(ite.toString());
            }
        catch(java.lang.IllegalAccessException iae)
            {
                throw new org.omg.CORBA.MARSHAL(iae.toString());
    }
    }
    public static String createForAnyType(Class type) {
        try{
            if (type.isArray())
                return createSequenceRepID(type);
            else if (IDLEntity.class.isAssignableFrom(type))
                {
                    try{
                        return getIdFromHelper(type);
                    }
                    catch(Throwable t) {
                        return createForIDLType(type, 1, 0);
                    }
                }
            else return createForJavaType(type);
        }
        catch(com.sun.corba.se.impl.io.TypeMismatchException e){
            return null;
        }
    }
    public static boolean isAbstractBase(Class clazz) {
        return (clazz.isInterface() &&
                IDLEntity.class.isAssignableFrom(clazz) &&
                (!ValueBase.class.isAssignableFrom(clazz)) &&
                (!org.omg.CORBA.Object.class.isAssignableFrom(clazz)));
    }
    private static String convertToISOLatin1 (String name) {
        int length = name.length();
        if (length == 0) {
            return name;
        }
        StringBuffer buffer = null;
        for (int i = 0; i < length; i++) {
            char c = name.charAt(i);
            if (c > 255 || IDL_IDENTIFIER_CHARS[c] == 0) {
                if (buffer == null) {
                    buffer = new StringBuffer(name.substring(0,i));
                }
                buffer.append(
                              "\\U" +
                              (char)ASCII_HEX[(c & 0xF000) >>> 12] +
                              (char)ASCII_HEX[(c & 0x0F00) >>> 8] +
                              (char)ASCII_HEX[(c & 0x00F0) >>> 4] +
                              (char)ASCII_HEX[(c & 0x000F)]);
            } else {
                if (buffer != null) {
                    buffer.append(c);
                }
            }
        }
        if (buffer != null) {
            name = buffer.toString();
        }
        return name;
    }
    private static String convertFromISOLatin1 (String name) {
        int index = -1;
        StringBuffer buf = new StringBuffer(name);
        while ((index = buf.toString().indexOf("\\U")) != -1){
            String str = "0000" + buf.toString().substring(index+2, index+6);
            byte[] buffer = new byte[(str.length() - 4) / 2];
            for (int i=4, j=0; i < str.length(); i +=2, j++) {
                buffer[j] = (byte)((ORBUtility.hexOf(str.charAt(i)) << 4) & 0xF0);
                buffer[j] |= (byte)((ORBUtility.hexOf(str.charAt(i+1)) << 0) & 0x0F);
            }
            buf = new StringBuffer(delete(buf.toString(), index, index+6));
            buf.insert(index, (char)buffer[1]);
        }
        return buf.toString();
    }
    private static String delete(String str, int from, int to)
    {
        return str.substring(0, from) + str.substring(to, str.length());
    }
    private static String replace(String target, String arg, String source)
    {
        int i = 0;
        i = target.indexOf(arg);
        while(i != -1)
            {
                String left = target.substring(0, i);
                String right = target.substring(i+arg.length());
                target = new String(left+source+right);
                i = target.indexOf(arg);
            }
        return target;
    }
    private Class loadClassOfType (String className,
                                  String remoteCodebase,
                                  ClassLoader loader,
                                  Class expectedType,
                                  ClassLoader expectedTypeClassLoader)
        throws ClassNotFoundException {
        Class loadedClass = null;
        try {
            try{
                if(!PackagePrefixChecker
                   .hasOffendingPrefix(PackagePrefixChecker
                                       .withoutPackagePrefix(className))){
                    loadedClass = Util.loadClass
                        (PackagePrefixChecker.withoutPackagePrefix(className),
                         remoteCodebase,
                         loader);
                } else {
                    loadedClass = Util.loadClass
                        (className,
                         remoteCodebase,
                         loader);
                }
            } catch (ClassNotFoundException cnfe) {
                loadedClass = Util.loadClass
                    (className,
                     remoteCodebase,
                     loader);
            }
            if (expectedType == null)
                return loadedClass;
        } catch (ClassNotFoundException cnfe) {
            if (expectedType == null)
                throw cnfe;
        }
        if (loadedClass == null || !expectedType.isAssignableFrom(loadedClass)) {
            if (expectedType.getClassLoader() != expectedTypeClassLoader)
                throw new IllegalArgumentException("expectedTypeClassLoader not class loader of expectedType.");
            if (expectedTypeClassLoader != null)
                loadedClass = expectedTypeClassLoader.loadClass(className);
            else
                loadedClass = ORBClassLoader.loadClass(className);
        }
        return loadedClass;
    }
    public static boolean useFullValueDescription(Class clazz, String repositoryID)
        throws IOException{
        String clazzRepIDStr = createForAnyType(clazz);
        if (clazzRepIDStr.equals(repositoryID))
            return false;
        RepositoryId_1_3 targetRepid;
        RepositoryId_1_3 clazzRepid;
        synchronized(cache) {
            targetRepid = cache.getId(repositoryID);
            clazzRepid = cache.getId(clazzRepIDStr);
        }
        if ((targetRepid.isRMIValueType()) && (clazzRepid.isRMIValueType())){
            if (!targetRepid.getSerialVersionUID().equals(clazzRepid.getSerialVersionUID())) {
                String mssg = "Mismatched serialization UIDs : Source (Rep. ID" +
                    clazzRepid + ") = " +
                    clazzRepid.getSerialVersionUID() + " whereas Target (Rep. ID " + repositoryID +
                    ") = " + targetRepid.getSerialVersionUID();
                throw new IOException(mssg);
            } else {
                return true;
            }
        } else {
            throw new IOException("The repository ID is not of an RMI value type (Expected ID = " + clazzRepIDStr + "; Received ID = " + repositoryID +")");
        }
    }
}
