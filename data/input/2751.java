public final class ORBUtility {
    private ORBUtility() {}
    private static ORBUtilSystemException wrapper = ORBUtilSystemException.get(
        CORBALogDomains.UTIL ) ;
    private static OMGSystemException omgWrapper = OMGSystemException.get(
        CORBALogDomains.UTIL ) ;
    private static StructMember[] members = null;
    private static StructMember[] systemExceptionMembers (ORB orb) {
        if (members == null) {
            members = new StructMember[3];
            members[0] = new StructMember("id", orb.create_string_tc(0), null);
            members[1] = new StructMember("minor", orb.get_primitive_tc(TCKind.tk_long), null);
            members[2] = new StructMember("completed", orb.get_primitive_tc(TCKind.tk_long), null);
        }
        return members;
    }
    private static TypeCode getSystemExceptionTypeCode(ORB orb, String repID, String name) {
        synchronized (TypeCode.class) {
            return orb.create_exception_tc(repID, name, systemExceptionMembers(orb));
        }
    }
    private static boolean isSystemExceptionTypeCode(TypeCode type, ORB orb) {
        StructMember[] systemExceptionMembers = systemExceptionMembers(orb);
        try {
            return (type.kind().value() == TCKind._tk_except &&
                    type.member_count() == 3 &&
                    type.member_type(0).equal(systemExceptionMembers[0].type) &&
                    type.member_type(1).equal(systemExceptionMembers[1].type) &&
                    type.member_type(2).equal(systemExceptionMembers[2].type));
        } catch (BadKind ex) {
            return false;
        } catch (org.omg.CORBA.TypeCodePackage.Bounds ex) {
            return false;
        }
    }
    public static void insertSystemException(SystemException ex, Any any) {
        OutputStream out = any.create_output_stream();
        ORB orb = (ORB)(out.orb());
        String name = ex.getClass().getName();
        String repID = ORBUtility.repositoryIdOf(name);
        out.write_string(repID);
        out.write_long(ex.minor);
        out.write_long(ex.completed.value());
        any.read_value(out.create_input_stream(),
            getSystemExceptionTypeCode(orb, repID, name));
    }
    public static SystemException extractSystemException(Any any) {
        InputStream in = any.create_input_stream();
        ORB orb = (ORB)(in.orb());
        if ( ! isSystemExceptionTypeCode(any.type(), orb)) {
            throw wrapper.unknownDsiSysex(CompletionStatus.COMPLETED_MAYBE);
        }
        return ORBUtility.readSystemException(in);
    }
    public static ValueHandler createValueHandler(ORB orb) {
        if (orb == null)
            return Util.createValueHandler();
        ORBVersion version = orb.getORBVersion();
        if (version == null)
            return Util.createValueHandler();
        if (version.equals(ORBVersionFactory.getOLD()))
            return new ValueHandlerImpl_1_3();
        if (version.equals(ORBVersionFactory.getNEW()))
            return new ValueHandlerImpl_1_3_1();
        return Util.createValueHandler();
    }
    public static boolean isLegacyORB(ORB orb)
    {
        try {
            ORBVersion currentORB = orb.getORBVersion();
            return currentORB.equals( ORBVersionFactory.getOLD() ) ;
        } catch (SecurityException se) {
            return false;
        }
    }
    public static boolean isForeignORB(ORB orb)
    {
        if (orb == null)
            return false;
        try {
            return orb.getORBVersion().equals(ORBVersionFactory.getFOREIGN());
        } catch (SecurityException se) {
            return false;
        }
    }
    public static int bytesToInt(byte[] array, int offset)
    {
        int b1, b2, b3, b4;
        b1 = (array[offset++] << 24) & 0xFF000000;
        b2 = (array[offset++] << 16) & 0x00FF0000;
        b3 = (array[offset++] << 8)  & 0x0000FF00;
        b4 = (array[offset++] << 0)  & 0x000000FF;
        return (b1 | b2 | b3 | b4);
    }
    public static void intToBytes(int value, byte[] array, int offset)
    {
        array[offset++] = (byte)((value >>> 24) & 0xFF);
        array[offset++] = (byte)((value >>> 16) & 0xFF);
        array[offset++] = (byte)((value >>> 8) & 0xFF);
        array[offset++] = (byte)((value >>> 0) & 0xFF);
    }
    public static int hexOf( char x )
    {
        int val;
        val = x - '0';
        if (val >=0 && val <= 9)
            return val;
        val = (x - 'a') + 10;
        if (val >= 10 && val <= 15)
            return val;
        val = (x - 'A') + 10;
        if (val >= 10 && val <= 15)
            return val;
        throw wrapper.badHexDigit() ;
    }
    public static void writeSystemException(SystemException ex, OutputStream strm)
    {
        String s;
        s = repositoryIdOf(ex.getClass().getName());
        strm.write_string(s);
        strm.write_long(ex.minor);
        strm.write_long(ex.completed.value());
    }
    public static SystemException readSystemException(InputStream strm)
    {
        try {
            String name = classNameOf(strm.read_string());
            SystemException ex
                = (SystemException)ORBClassLoader.loadClass(name).newInstance();
            ex.minor = strm.read_long();
            ex.completed = CompletionStatus.from_int(strm.read_long());
            return ex;
        } catch ( Exception ex ) {
            throw wrapper.unknownSysex( CompletionStatus.COMPLETED_MAYBE, ex );
        }
    }
    public static String classNameOf(String repositoryId)
    {
        String className=null;
        className = (String) exceptionClassNames.get(repositoryId);
        if (className == null)
            className = "org.omg.CORBA.UNKNOWN";
        return className;
    }
    public static boolean isSystemException(String repositoryId)
    {
        String className=null;
        className = (String) exceptionClassNames.get(repositoryId);
        if (className == null)
            return false;
        else
            return true;
    }
    public static byte getEncodingVersion(ORB orb, IOR ior) {
        if (orb.getORBData().isJavaSerializationEnabled()) {
            IIOPProfile prof = ior.getProfile();
            IIOPProfileTemplate profTemp =
                (IIOPProfileTemplate) prof.getTaggedProfileTemplate();
            java.util.Iterator iter = profTemp.iteratorById(
                                  ORBConstants.TAG_JAVA_SERIALIZATION_ID);
            if (iter.hasNext()) {
                JavaSerializationComponent jc =
                    (JavaSerializationComponent) iter.next();
                byte jcVersion = jc.javaSerializationVersion();
                if (jcVersion >= Message.JAVA_ENC_VERSION) {
                    return Message.JAVA_ENC_VERSION;
                } else if (jcVersion > Message.CDR_ENC_VERSION) {
                    return jc.javaSerializationVersion();
                } else {
                }
            }
        }
        return Message.CDR_ENC_VERSION; 
    }
    public static String repositoryIdOf(String name)
    {
        String id;
        id = (String) exceptionRepositoryIds.get(name);
        if (id == null)
            id = "IDL:omg.org/CORBA/UNKNOWN:1.0";
        return id;
    }
    private static final Hashtable exceptionClassNames = new Hashtable();
    private static final Hashtable exceptionRepositoryIds = new Hashtable();
    static {
        exceptionClassNames.put("IDL:omg.org/CORBA/BAD_CONTEXT:1.0",
                                "org.omg.CORBA.BAD_CONTEXT");
        exceptionClassNames.put("IDL:omg.org/CORBA/BAD_INV_ORDER:1.0",
                                "org.omg.CORBA.BAD_INV_ORDER");
        exceptionClassNames.put("IDL:omg.org/CORBA/BAD_OPERATION:1.0",
                                "org.omg.CORBA.BAD_OPERATION");
        exceptionClassNames.put("IDL:omg.org/CORBA/BAD_PARAM:1.0",
                                "org.omg.CORBA.BAD_PARAM");
        exceptionClassNames.put("IDL:omg.org/CORBA/BAD_TYPECODE:1.0",
                                "org.omg.CORBA.BAD_TYPECODE");
        exceptionClassNames.put("IDL:omg.org/CORBA/COMM_FAILURE:1.0",
                                "org.omg.CORBA.COMM_FAILURE");
        exceptionClassNames.put("IDL:omg.org/CORBA/DATA_CONVERSION:1.0",
                                "org.omg.CORBA.DATA_CONVERSION");
        exceptionClassNames.put("IDL:omg.org/CORBA/IMP_LIMIT:1.0",
                                "org.omg.CORBA.IMP_LIMIT");
        exceptionClassNames.put("IDL:omg.org/CORBA/INTF_REPOS:1.0",
                                "org.omg.CORBA.INTF_REPOS");
        exceptionClassNames.put("IDL:omg.org/CORBA/INTERNAL:1.0",
                                "org.omg.CORBA.INTERNAL");
        exceptionClassNames.put("IDL:omg.org/CORBA/INV_FLAG:1.0",
                                "org.omg.CORBA.INV_FLAG");
        exceptionClassNames.put("IDL:omg.org/CORBA/INV_IDENT:1.0",
                                "org.omg.CORBA.INV_IDENT");
        exceptionClassNames.put("IDL:omg.org/CORBA/INV_OBJREF:1.0",
                                "org.omg.CORBA.INV_OBJREF");
        exceptionClassNames.put("IDL:omg.org/CORBA/MARSHAL:1.0",
                                "org.omg.CORBA.MARSHAL");
        exceptionClassNames.put("IDL:omg.org/CORBA/NO_MEMORY:1.0",
                                "org.omg.CORBA.NO_MEMORY");
        exceptionClassNames.put("IDL:omg.org/CORBA/FREE_MEM:1.0",
                                "org.omg.CORBA.FREE_MEM");
        exceptionClassNames.put("IDL:omg.org/CORBA/NO_IMPLEMENT:1.0",
                                "org.omg.CORBA.NO_IMPLEMENT");
        exceptionClassNames.put("IDL:omg.org/CORBA/NO_PERMISSION:1.0",
                                "org.omg.CORBA.NO_PERMISSION");
        exceptionClassNames.put("IDL:omg.org/CORBA/NO_RESOURCES:1.0",
                                "org.omg.CORBA.NO_RESOURCES");
        exceptionClassNames.put("IDL:omg.org/CORBA/NO_RESPONSE:1.0",
                                "org.omg.CORBA.NO_RESPONSE");
        exceptionClassNames.put("IDL:omg.org/CORBA/OBJ_ADAPTER:1.0",
                                "org.omg.CORBA.OBJ_ADAPTER");
        exceptionClassNames.put("IDL:omg.org/CORBA/INITIALIZE:1.0",
                                "org.omg.CORBA.INITIALIZE");
        exceptionClassNames.put("IDL:omg.org/CORBA/PERSIST_STORE:1.0",
                                "org.omg.CORBA.PERSIST_STORE");
        exceptionClassNames.put("IDL:omg.org/CORBA/TRANSIENT:1.0",
                                "org.omg.CORBA.TRANSIENT");
        exceptionClassNames.put("IDL:omg.org/CORBA/UNKNOWN:1.0",
                                "org.omg.CORBA.UNKNOWN");
        exceptionClassNames.put("IDL:omg.org/CORBA/OBJECT_NOT_EXIST:1.0",
                                "org.omg.CORBA.OBJECT_NOT_EXIST");
        exceptionClassNames.put("IDL:omg.org/CORBA/INVALID_TRANSACTION:1.0",
                                "org.omg.CORBA.INVALID_TRANSACTION");
        exceptionClassNames.put("IDL:omg.org/CORBA/TRANSACTION_REQUIRED:1.0",
                                "org.omg.CORBA.TRANSACTION_REQUIRED");
        exceptionClassNames.put("IDL:omg.org/CORBA/TRANSACTION_ROLLEDBACK:1.0",
                                "org.omg.CORBA.TRANSACTION_ROLLEDBACK");
        exceptionClassNames.put("IDL:omg.org/CORBA/INV_POLICY:1.0",
                                "org.omg.CORBA.INV_POLICY");
        exceptionClassNames.
            put("IDL:omg.org/CORBA/TRANSACTION_UNAVAILABLE:1.0",
                                "org.omg.CORBA.TRANSACTION_UNAVAILABLE");
        exceptionClassNames.put("IDL:omg.org/CORBA/TRANSACTION_MODE:1.0",
                                "org.omg.CORBA.TRANSACTION_MODE");
        exceptionClassNames.put("IDL:omg.org/CORBA/CODESET_INCOMPATIBLE:1.0",
                                "org.omg.CORBA.CODESET_INCOMPATIBLE");
        exceptionClassNames.put("IDL:omg.org/CORBA/REBIND:1.0",
                                "org.omg.CORBA.REBIND");
        exceptionClassNames.put("IDL:omg.org/CORBA/TIMEOUT:1.0",
                                "org.omg.CORBA.TIMEOUT");
        exceptionClassNames.put("IDL:omg.org/CORBA/BAD_QOS:1.0",
                                "org.omg.CORBA.BAD_QOS");
        exceptionClassNames.put("IDL:omg.org/CORBA/INVALID_ACTIVITY:1.0",
                                "org.omg.CORBA.INVALID_ACTIVITY");
        exceptionClassNames.put("IDL:omg.org/CORBA/ACTIVITY_COMPLETED:1.0",
                                "org.omg.CORBA.ACTIVITY_COMPLETED");
        exceptionClassNames.put("IDL:omg.org/CORBA/ACTIVITY_REQUIRED:1.0",
                                "org.omg.CORBA.ACTIVITY_REQUIRED");
        Enumeration keys = exceptionClassNames.keys();
        java.lang.Object s;
        String rId;
        String cName;
        try{
            while (keys.hasMoreElements()) {
                s = keys.nextElement();
                rId = (String) s;
                cName = (String) exceptionClassNames.get(rId);
                exceptionRepositoryIds.put (cName, rId);
            }
        } catch (NoSuchElementException e) { }
    }
    public static int[] parseVersion(String version) {
        if (version == null)
            return new int[0];
        char[] s = version.toCharArray();
        int start = 0;
        for (; start < s.length  && (s[start] < '0' || s[start] > '9'); ++start)
            if (start == s.length)      
                return new int[0];
        int end = start + 1;
        int size = 1;
        for (; end < s.length; ++end)
            if (s[end] == '.')
                ++size;
            else if (s[end] < '0' || s[end] > '9')
                break;
        int[] val = new int[size];
        for (int i = 0; i < size; ++i) {
            int dot = version.indexOf('.', start);
            if (dot == -1 || dot > end)
                dot = end;
            if (start >= dot)   
                val[i] = 0;     
            else
                val[i] = Integer.parseInt(version.substring(start, dot));
            start = dot + 1;
        }
        return val;
    }
    public static int compareVersion(int[] v1, int[] v2) {
        if (v1 == null)
            v1 = new int[0];
        if (v2 == null)
            v2 = new int[0];
        for (int i = 0; i < v1.length; ++i) {
            if (i >= v2.length || v1[i] > v2[i])        
                return 1;
            if (v1[i] < v2[i])
                return -1;
        }
        return v1.length == v2.length ? 0 : -1;
    }
    public static synchronized int compareVersion(String v1, String v2) {
        return compareVersion(parseVersion(v1), parseVersion(v2));
    }
    private static String compressClassName( String name )
    {
        String prefix = "com.sun.corba.se." ;
        if (name.startsWith( prefix ) ) {
            return "(ORB)." + name.substring( prefix.length() ) ;
        } else
            return name ;
    }
    public static String getThreadName( Thread thr )
    {
        if (thr == null)
            return "null" ;
        String name = thr.getName() ;
        StringTokenizer st = new StringTokenizer( name ) ;
        int numTokens = st.countTokens() ;
        if (numTokens != 5)
            return name ;
        String[] tokens = new String[numTokens] ;
        for (int ctr=0; ctr<numTokens; ctr++ )
            tokens[ctr] = st.nextToken() ;
        if( !tokens[0].equals("SelectReaderThread"))
            return name ;
        return "SelectReaderThread[" + tokens[2] + ":" + tokens[3] + "]" ;
    }
    private static String formatStackTraceElement( StackTraceElement ste )
    {
        return compressClassName( ste.getClassName() ) + "." + ste.getMethodName() +
            (ste.isNativeMethod() ? "(Native Method)" :
             (ste.getFileName() != null && ste.getLineNumber() >= 0 ?
              "(" + ste.getFileName() + ":" + ste.getLineNumber() + ")" :
              (ste.getFileName() != null ?  "("+ste.getFileName()+")" : "(Unknown Source)")));
    }
    private static void printStackTrace( StackTraceElement[] trace )
    {
        System.out.println( "    Stack Trace:" ) ;
        for ( int ctr = 1; ctr < trace.length; ctr++ ) {
            System.out.print( "        >" ) ;
            System.out.println( formatStackTraceElement( trace[ctr] ) ) ;
        }
    }
    public static synchronized void dprint(java.lang.Object obj, String msg) {
        System.out.println(
            compressClassName( obj.getClass().getName() ) + "("  +
            getThreadName( Thread.currentThread() ) + "): " + msg);
    }
    public static synchronized void dprint(String className, String msg) {
        System.out.println(
            compressClassName( className ) + "("  +
            getThreadName( Thread.currentThread() ) + "): " + msg);
    }
    public synchronized void dprint(String msg) {
        ORBUtility.dprint(this, msg);
    }
    public static synchronized void dprintTrace(Object obj, String msg) {
        ORBUtility.dprint(obj, msg);
        Throwable thr = new Throwable() ;
        printStackTrace( thr.getStackTrace() ) ;
    }
    public static synchronized void dprint(java.lang.Object caller,
        String msg, Throwable t)
    {
        System.out.println(
            compressClassName( caller.getClass().getName() ) +
            '(' + Thread.currentThread() + "): " + msg);
        if (t != null)
            printStackTrace( t.getStackTrace() ) ;
    }
    public static String[] concatenateStringArrays( String[] arr1, String[] arr2 )
    {
        String[] result = new String[
            arr1.length + arr2.length ] ;
        for (int ctr = 0; ctr<arr1.length; ctr++)
            result[ctr] = arr1[ctr] ;
        for (int ctr = 0; ctr<arr2.length; ctr++)
            result[ctr + arr1.length] = arr2[ctr] ;
        return result ;
    }
    public static void throwNotSerializableForCorba(String className) {
        throw omgWrapper.notSerializable( CompletionStatus.COMPLETED_MAYBE,
            className ) ;
    }
    public static byte getMaxStreamFormatVersion() {
        ValueHandler vh = Util.createValueHandler();
        if (!(vh instanceof javax.rmi.CORBA.ValueHandlerMultiFormat))
            return ORBConstants.STREAM_FORMAT_VERSION_1;
        else
            return ((ValueHandlerMultiFormat)vh).getMaximumStreamFormatVersion();
    }
    public static CorbaClientDelegate makeClientDelegate( IOR ior )
    {
        ORB orb = ior.getORB() ;
        CorbaContactInfoList ccil = orb.getCorbaContactInfoListFactory().create( ior ) ;
        CorbaClientDelegate del = orb.getClientDelegateFactory().create(ccil);
        return del ;
    }
    public static org.omg.CORBA.Object makeObjectReference( IOR ior )
    {
        CorbaClientDelegate del = makeClientDelegate( ior ) ;
        org.omg.CORBA.Object objectImpl = new CORBAObjectImpl() ;
        StubAdapter.setDelegate( objectImpl, del ) ;
        return objectImpl ;
    }
    public static IOR getIOR( org.omg.CORBA.Object obj )
    {
        if (obj == null)
            throw wrapper.nullObjectReference() ;
        IOR ior = null ;
        if (StubAdapter.isStub(obj)) {
            org.omg.CORBA.portable.Delegate del = StubAdapter.getDelegate(
                obj ) ;
            if (del instanceof CorbaClientDelegate) {
                CorbaClientDelegate cdel = (CorbaClientDelegate)del ;
                ContactInfoList cil = cdel.getContactInfoList() ;
                if (cil instanceof CorbaContactInfoList) {
                    CorbaContactInfoList ccil = (CorbaContactInfoList)cil ;
                    ior = ccil.getTargetIOR() ;
                    if (ior == null)
                        throw wrapper.nullIor() ;
                    return ior ;
                } else {
                    throw new INTERNAL() ;
                }
            }
            throw wrapper.objrefFromForeignOrb() ;
        } else
            throw wrapper.localObjectNotAllowed() ;
    }
    public static IOR connectAndGetIOR( ORB orb, org.omg.CORBA.Object obj )
    {
        IOR result ;
        try {
            result = getIOR( obj ) ;
        } catch (BAD_OPERATION bop) {
            if (StubAdapter.isStub(obj)) {
                try {
                    StubAdapter.connect( obj, orb ) ;
                } catch (java.rmi.RemoteException exc) {
                    throw wrapper.connectingServant( exc ) ;
                }
            } else {
                orb.connect( obj ) ;
            }
            result = getIOR( obj ) ;
        }
        return result ;
    }
    public static String operationNameAndRequestId(CorbaMessageMediator m)
    {
        return "op/" + m.getOperationName() + " id/" + m.getRequestId();
    }
    public static boolean isPrintable(char c)
    {
        if (Character.isJavaIdentifierStart(c)) {
            return true;
        }
        if (Character.isDigit(c)) {
            return true;
        }
        switch (Character.getType(c)) {
        case Character.MODIFIER_SYMBOL : return true; 
        case Character.DASH_PUNCTUATION : return true; 
        case Character.MATH_SYMBOL : return true; 
        case Character.OTHER_PUNCTUATION : return true; 
        case Character.START_PUNCTUATION : return true; 
        case Character.END_PUNCTUATION : return true; 
        }
        return false;
    }
    public static String getClassSecurityInfo(final Class cl)
    {
        String result =
            (String)AccessController.doPrivileged(new PrivilegedAction() {
                public java.lang.Object run() {
                    StringBuffer sb = new StringBuffer(500);
                    ProtectionDomain pd = cl.getProtectionDomain();
                    Policy policy = Policy.getPolicy();
                    PermissionCollection pc = policy.getPermissions(pd);
                    sb.append("\nPermissionCollection ");
                    sb.append(pc.toString());
                    sb.append(pd.toString());
                    return sb.toString();
                }
            });
        return result;
    }
}
