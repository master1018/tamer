public class CDRInputStream_1_0 extends CDRInputStreamBase
    implements RestorableInputStream
{
    private static final String kReadMethod = "read";
    private static final int maxBlockLength = 0x7fffff00;
    protected BufferManagerRead bufferManagerRead;
    protected ByteBufferWithInfo bbwi;
    private boolean debug = false;
    protected boolean littleEndian;
    protected ORB orb;
    protected ORBUtilSystemException wrapper ;
    protected OMGSystemException omgWrapper ;
    protected ValueHandler valueHandler = null;
    private CacheTable valueCache = null;
    private CacheTable repositoryIdCache = null;
    private CacheTable codebaseCache = null;
    protected int blockLength = maxBlockLength;
    protected int end_flag = 0;
    private int chunkedValueNestingLevel = 0;
    protected int valueIndirection = 0;
    protected int stringIndirection = 0;
    protected boolean isChunked = false;
    private RepositoryIdUtility repIdUtil;
    private RepositoryIdStrings repIdStrs;
    private CodeSetConversion.BTCConverter charConverter;
    private CodeSetConversion.BTCConverter wcharConverter;
    private boolean specialNoOptionalDataState = false;
    public CDRInputStreamBase dup()
    {
        CDRInputStreamBase result = null ;
        try {
            result = (CDRInputStreamBase)this.getClass().newInstance();
        } catch (Exception e) {
            throw wrapper.couldNotDuplicateCdrInputStream( e ) ;
        }
        result.init(this.orb,
                    this.bbwi.byteBuffer,
                    this.bbwi.buflen,
                    this.littleEndian,
                    this.bufferManagerRead);
        ((CDRInputStream_1_0)result).bbwi.position(this.bbwi.position());
        ((CDRInputStream_1_0)result).bbwi.byteBuffer.limit(this.bbwi.buflen);
        return result;
    }
    public void init(org.omg.CORBA.ORB orb,
                     ByteBuffer byteBuffer,
                     int size,
                     boolean littleEndian,
                     BufferManagerRead bufferManager)
    {
        this.orb = (ORB)orb;
        this.wrapper = ORBUtilSystemException.get( (ORB)orb,
            CORBALogDomains.RPC_ENCODING ) ;
        this.omgWrapper = OMGSystemException.get( (ORB)orb,
            CORBALogDomains.RPC_ENCODING ) ;
        this.littleEndian = littleEndian;
        this.bufferManagerRead = bufferManager;
        this.bbwi = new ByteBufferWithInfo(orb,byteBuffer,0);
        this.bbwi.buflen = size;
        this.bbwi.byteBuffer.limit(bbwi.buflen);
        this.markAndResetHandler = bufferManagerRead.getMarkAndResetHandler();
        debug = ((ORB)orb).transportDebugFlag;
    }
    void performORBVersionSpecificInit() {
        createRepositoryIdHandlers();
    }
    private final void createRepositoryIdHandlers()
    {
        repIdUtil = RepositoryIdFactory.getRepIdUtility(orb);
        repIdStrs = RepositoryIdFactory.getRepIdStringsFactory(orb);
    }
    public GIOPVersion getGIOPVersion() {
        return GIOPVersion.V1_0;
    }
    void setHeaderPadding(boolean headerPadding) {
        throw wrapper.giopVersionError();
    }
    protected final int computeAlignment(int index, int align) {
        if (align > 1) {
            int incr = index & (align - 1);
            if (incr != 0)
                return align - incr;
        }
        return 0;
    }
    public int getSize()
    {
        return bbwi.position();
    }
    protected void checkBlockLength(int align, int dataSize) {
        if (!isChunked)
            return;
        if (specialNoOptionalDataState) {
            throw omgWrapper.rmiiiopOptionalDataIncompatible1() ;
        }
        boolean checkForEndTag = false;
        if (blockLength == get_offset()) {
            blockLength = maxBlockLength;
            start_block();
            if (blockLength == maxBlockLength)
                checkForEndTag = true;
        } else
        if (blockLength < get_offset()) {
            throw wrapper.chunkOverflow() ;
        }
        int requiredNumBytes =
                            computeAlignment(bbwi.position(), align) + dataSize;
        if (blockLength != maxBlockLength &&
            blockLength < get_offset() + requiredNumBytes) {
            throw omgWrapper.rmiiiopOptionalDataIncompatible2() ;
        }
        if (checkForEndTag) {
            int nextLong = read_long();
            bbwi.position(bbwi.position() - 4);
            if (nextLong < 0)
                throw omgWrapper.rmiiiopOptionalDataIncompatible3() ;
        }
    }
    protected void alignAndCheck(int align, int n) {
        checkBlockLength(align, n);
        int alignResult = computeAlignment(bbwi.position(), align);
        bbwi.position(bbwi.position() + alignResult);
        if (bbwi.position() + n > bbwi.buflen)
            grow(align, n);
    }
    protected void grow(int align, int n) {
        bbwi.needed = n;
        bbwi = bufferManagerRead.underflow(bbwi);
    }
    public final void consumeEndian() {
        littleEndian = read_boolean();
    }
    public final double read_longdouble() {
        throw wrapper.longDoubleNotImplemented( CompletionStatus.COMPLETED_MAYBE);
    }
    public final boolean read_boolean() {
        return (read_octet() != 0);
    }
    public final char read_char() {
        alignAndCheck(1, 1);
        return getConvertedChars(1, getCharConverter())[0];
    }
    public char read_wchar() {
        if (ORBUtility.isForeignORB((ORB)orb)) {
            throw wrapper.wcharDataInGiop10( CompletionStatus.COMPLETED_MAYBE);
        }
        int b1, b2;
        alignAndCheck(2, 2);
        if (littleEndian) {
            b2 = bbwi.byteBuffer.get(bbwi.position()) & 0x00FF;
            bbwi.position(bbwi.position() + 1);
            b1 = bbwi.byteBuffer.get(bbwi.position()) & 0x00FF;
            bbwi.position(bbwi.position() + 1);
        } else {
            b1 = bbwi.byteBuffer.get(bbwi.position()) & 0x00FF;
            bbwi.position(bbwi.position() + 1);
            b2 = bbwi.byteBuffer.get(bbwi.position()) & 0x00FF;
            bbwi.position(bbwi.position() + 1);
        }
        return (char)((b1 << 8) + (b2 << 0));
    }
    public final byte read_octet() {
        alignAndCheck(1, 1);
        byte b = bbwi.byteBuffer.get(bbwi.position());
        bbwi.position(bbwi.position() + 1);
        return b;
    }
    public final short read_short() {
        int b1, b2;
        alignAndCheck(2, 2);
        if (littleEndian) {
            b2 = (bbwi.byteBuffer.get(bbwi.position()) << 0) & 0x000000FF;
            bbwi.position(bbwi.position() + 1);
            b1 = (bbwi.byteBuffer.get(bbwi.position()) << 8) & 0x0000FF00;
            bbwi.position(bbwi.position() + 1);
        } else {
            b1 = (bbwi.byteBuffer.get(bbwi.position()) << 8) & 0x0000FF00;
            bbwi.position(bbwi.position() + 1);
            b2 = (bbwi.byteBuffer.get(bbwi.position()) << 0) & 0x000000FF;
            bbwi.position(bbwi.position() + 1);
        }
        return (short)(b1 | b2);
    }
    public final short read_ushort() {
        return read_short();
    }
    public final int read_long() {
        int b1, b2, b3, b4;
        alignAndCheck(4, 4);
        int bufPos = bbwi.position();
        if (littleEndian) {
            b4 = bbwi.byteBuffer.get(bufPos++) & 0xFF;
            b3 = bbwi.byteBuffer.get(bufPos++) & 0xFF;
            b2 = bbwi.byteBuffer.get(bufPos++) & 0xFF;
            b1 = bbwi.byteBuffer.get(bufPos++) & 0xFF;
        } else {
            b1 = bbwi.byteBuffer.get(bufPos++) & 0xFF;
            b2 = bbwi.byteBuffer.get(bufPos++) & 0xFF;
            b3 = bbwi.byteBuffer.get(bufPos++) & 0xFF;
            b4 = bbwi.byteBuffer.get(bufPos++) & 0xFF;
        }
        bbwi.position(bufPos);
        return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
    }
    public final int read_ulong() {
        return read_long();
    }
    public final long read_longlong() {
        long i1, i2;
        alignAndCheck(8, 8);
        if (littleEndian) {
            i2 = read_long() & 0xFFFFFFFFL;
            i1 = (long)read_long() << 32;
        } else {
            i1 = (long)read_long() << 32;
            i2 = read_long() & 0xFFFFFFFFL;
        }
        return (i1 | i2);
    }
    public final long read_ulonglong() {
        return read_longlong();
    }
    public final float read_float() {
        return Float.intBitsToFloat(read_long());
    }
    public final double read_double() {
        return Double.longBitsToDouble(read_longlong());
    }
    protected final void checkForNegativeLength(int length) {
        if (length < 0)
            throw wrapper.negativeStringLength( CompletionStatus.COMPLETED_MAYBE,
                new Integer(length) ) ;
    }
    protected final String readStringOrIndirection(boolean allowIndirection) {
        int len = read_long();
        if (allowIndirection) {
            if (len == 0xffffffff)
                return null;
            else
                stringIndirection = get_offset() - 4;
        }
        checkForNegativeLength(len);
        if (orb != null && ORBUtility.isLegacyORB((ORB)orb))
            return legacyReadString(len);
        else
            return internalReadString(len);
    }
    private final String internalReadString(int len) {
        if (len == 0)
            return new String("");
        char[] result = getConvertedChars(len - 1, getCharConverter());
        read_octet();
        return new String(result, 0, getCharConverter().getNumChars());
    }
    private final String legacyReadString(int len) {
        if (len == 0)
            return new String("");
        len--;
        char[] c = new char[len];
        int n = 0;
        while (n < len) {
            int avail;
            int bytes;
            int wanted;
            avail = bbwi.buflen - bbwi.position();
            if (avail <= 0) {
                grow(1, 1);
                avail = bbwi.buflen - bbwi.position();
            }
            wanted = len - n;
            bytes = (wanted < avail) ? wanted : avail;
            for (int i=0; i<bytes; i++) {
                c[n+i] = (char) (bbwi.byteBuffer.get(bbwi.position()+i) & 0xFF);
            }
            bbwi.position(bbwi.position() + bytes);
            n += bytes;
        }
        if (bbwi.position() + 1 > bbwi.buflen)
            alignAndCheck(1, 1);
        bbwi.position(bbwi.position() + 1);
        return new String(c);
    }
    public final String read_string() {
        return readStringOrIndirection(false);
    }
    public String read_wstring() {
        if (ORBUtility.isForeignORB((ORB)orb)) {
            throw wrapper.wcharDataInGiop10( CompletionStatus.COMPLETED_MAYBE);
        }
        int len = read_long();
        if (len == 0)
            return new String("");
        checkForNegativeLength(len);
        len--;
        char[] c = new char[len];
        for (int i = 0; i < len; i++)
            c[i] = read_wchar();
        read_wchar();
        return new String(c);
    }
    public final void read_octet_array(byte[] b, int offset, int length) {
        if ( b == null )
            throw wrapper.nullParam() ;
        if (length == 0)
            return;
        alignAndCheck(1, 1);
        int n = offset;
        while (n < length+offset) {
            int avail;
            int bytes;
            int wanted;
            avail = bbwi.buflen - bbwi.position();
            if (avail <= 0) {
                grow(1, 1);
                avail = bbwi.buflen - bbwi.position();
            }
            wanted = (length + offset) - n;
            bytes = (wanted < avail) ? wanted : avail;
            for (int i = 0; i < bytes; i++) {
                b[n+i] = bbwi.byteBuffer.get(bbwi.position() + i);
            }
            bbwi.position(bbwi.position() + bytes);
            n += bytes;
        }
    }
    public Principal read_Principal() {
        int len = read_long();
        byte[] pvalue = new byte[len];
        read_octet_array(pvalue,0,len);
        Principal p = new PrincipalImpl();
        p.name(pvalue);
        return p;
    }
    public TypeCode read_TypeCode() {
        TypeCodeImpl tc = new TypeCodeImpl(orb);
        tc.read_value(parent);
        return tc;
    }
    public Any read_any() {
        Any any = orb.create_any();
        TypeCodeImpl tc = new TypeCodeImpl(orb);
        try {
            tc.read_value(parent);
        } catch (MARSHAL ex) {
            if (tc.kind().value() != TCKind._tk_value)
                throw ex;
            dprintThrowable(ex);
        }
        any.read_value(parent, tc);
        return any;
    }
    public org.omg.CORBA.Object read_Object() {
        return read_Object(null);
    }
    public org.omg.CORBA.Object read_Object(Class clz)
    {
        IOR ior = IORFactories.makeIOR(parent) ;
        if (ior.isNil())
            return null ;
        PresentationManager.StubFactoryFactory sff = ORB.getStubFactoryFactory() ;
        String codeBase = ior.getProfile().getCodebase() ;
        PresentationManager.StubFactory stubFactory = null ;
        if (clz == null) {
            RepositoryId rid = RepositoryId.cache.getId( ior.getTypeId() ) ;
            String className = rid.getClassName() ;
            boolean isIDLInterface = rid.isIDLType() ;
            if (className == null || className.equals( "" ))
                stubFactory = null ;
            else
                try {
                    stubFactory = sff.createStubFactory( className,
                        isIDLInterface, codeBase, (Class)null,
                        (ClassLoader)null );
                } catch (Exception exc) {
                    stubFactory = null ;
                }
        } else if (StubAdapter.isStubClass( clz )) {
            stubFactory = PresentationDefaults.makeStaticStubFactory(
                clz ) ;
        } else {
            boolean isIDL = IDLEntity.class.isAssignableFrom( clz ) ;
            stubFactory = sff.createStubFactory( clz.getName(),
                isIDL, codeBase, clz, clz.getClassLoader() ) ;
        }
        return internalIORToObject( ior, stubFactory, orb ) ;
    }
    public static org.omg.CORBA.Object internalIORToObject(
        IOR ior, PresentationManager.StubFactory stubFactory, ORB orb)
    {
        ORBUtilSystemException wrapper = ORBUtilSystemException.get(
            (ORB)orb, CORBALogDomains.RPC_ENCODING ) ;
        java.lang.Object servant = ior.getProfile().getServant() ;
        if (servant != null ) {
            if (servant instanceof Tie) {
                String codebase = ior.getProfile().getCodebase();
                org.omg.CORBA.Object objref = (org.omg.CORBA.Object)
                    Utility.loadStub( (Tie)servant, stubFactory, codebase,
                        false);
                if (objref != null) {
                    return objref;
                } else {
                    throw wrapper.readObjectException() ;
                }
            } else if (servant instanceof org.omg.CORBA.Object) {
                if (!(servant instanceof
                        org.omg.CORBA.portable.InvokeHandler)) {
                    return (org.omg.CORBA.Object) servant;
                }
            } else
                throw wrapper.badServantReadObject() ;
        }
        CorbaClientDelegate del = ORBUtility.makeClientDelegate( ior ) ;
        org.omg.CORBA.Object objref = null ;
        try {
            objref = stubFactory.makeStub() ;
        } catch (Throwable e) {
            wrapper.stubCreateError( e ) ;
            if (e instanceof ThreadDeath) {
                throw (ThreadDeath) e;
            }
            objref = new CORBAObjectImpl() ;
        }
        StubAdapter.setDelegate( objref, del ) ;
        return objref;
    }
    public java.lang.Object read_abstract_interface()
    {
        return read_abstract_interface(null);
    }
    public java.lang.Object read_abstract_interface(java.lang.Class clz)
    {
        boolean object = read_boolean();
        if (object) {
            return read_Object(clz);
        } else {
            return read_value();
        }
    }
    public Serializable read_value()
    {
        return read_value((Class)null);
    }
    private Serializable handleIndirection() {
        int indirection = read_long() + get_offset() - 4;
        if (valueCache != null && valueCache.containsVal(indirection)) {
            java.io.Serializable cachedValue
                = (java.io.Serializable)valueCache.getKey(indirection);
            return cachedValue;
        } else {
            throw new IndirectionException(indirection);
        }
    }
    private String readRepositoryIds(int valueTag,
                                     Class expectedType,
                                     String expectedTypeRepId) {
        return readRepositoryIds(valueTag, expectedType,
                                 expectedTypeRepId, null);
    }
    private String readRepositoryIds(int valueTag,
                                     Class expectedType,
                                     String expectedTypeRepId,
                                     BoxedValueHelper factory) {
        switch(repIdUtil.getTypeInfo(valueTag)) {
            case RepositoryIdUtility.NO_TYPE_INFO :
                if (expectedType == null) {
                    if (expectedTypeRepId != null) {
                        return expectedTypeRepId;
                    } else if (factory != null) {
                        return factory.get_id();
                    } else {
                        throw wrapper.expectedTypeNullAndNoRepId(
                            CompletionStatus.COMPLETED_MAYBE);
                    }
                }
                return repIdStrs.createForAnyType(expectedType);
            case RepositoryIdUtility.SINGLE_REP_TYPE_INFO :
                return read_repositoryId();
            case RepositoryIdUtility.PARTIAL_LIST_TYPE_INFO :
                return read_repositoryIds();
            default:
                throw wrapper.badValueTag( CompletionStatus.COMPLETED_MAYBE,
                    Integer.toHexString(valueTag) ) ;
        }
    }
    public Serializable read_value(Class expectedType) {
        int vType = readValueTag();
        if (vType == 0)
            return null;
        if (vType == 0xffffffff)
            return handleIndirection();
        int indirection = get_offset() - 4;
        boolean saveIsChunked = isChunked;
        isChunked = repIdUtil.isChunkedEncoding(vType);
        java.lang.Object value = null;
        String codebase_URL = null;
        if (repIdUtil.isCodeBasePresent(vType)) {
            codebase_URL = read_codebase_URL();
        }
        String repositoryIDString
            = readRepositoryIds(vType, expectedType, null);
        start_block();
        end_flag--;
        if (isChunked)
            chunkedValueNestingLevel--;
        if (repositoryIDString.equals(repIdStrs.getWStringValueRepId())) {
            value = read_wstring();
        } else
        if (repositoryIDString.equals(repIdStrs.getClassDescValueRepId())) {
            value = readClass();
        } else {
            Class valueClass = expectedType;
            if (expectedType == null ||
                !repositoryIDString.equals(repIdStrs.createForAnyType(expectedType))) {
                valueClass = getClassFromString(repositoryIDString,
                                                codebase_URL,
                                                expectedType);
            }
            if (valueClass == null) {
                throw wrapper.couldNotFindClass(
                    CompletionStatus.COMPLETED_MAYBE,
                    new ClassNotFoundException());
            }
            if (valueClass != null &&
                org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(valueClass)) {
                value =  readIDLValue(indirection,
                                      repositoryIDString,
                                      valueClass,
                                      codebase_URL);
            } else {
                try {
                    if (valueHandler == null)
                        valueHandler = ORBUtility.createValueHandler(orb);
                    value = valueHandler.readValue(parent,
                                                   indirection,
                                                   valueClass,
                                                   repositoryIDString,
                                                   getCodeBase());
                } catch(SystemException sysEx) {
                    throw sysEx;
                } catch(Exception ex) {
                    throw wrapper.valuehandlerReadException(
                        CompletionStatus.COMPLETED_MAYBE, ex ) ;
                } catch(Error e) {
                    throw wrapper.valuehandlerReadError(
                        CompletionStatus.COMPLETED_MAYBE, e ) ;
                }
            }
        }
        handleEndOfValue();
        readEndTag();
        if (valueCache == null)
            valueCache = new CacheTable(orb,false);
        valueCache.put(value, indirection);
        isChunked = saveIsChunked;
        start_block();
        return (java.io.Serializable)value;
    }
    public Serializable read_value(BoxedValueHelper factory) {
        int vType = readValueTag();
        if (vType == 0)
            return null; 
        else if (vType == 0xffffffff) { 
            int indirection = read_long() + get_offset() - 4;
            if (valueCache != null && valueCache.containsVal(indirection))
                {
                    java.io.Serializable cachedValue =
                           (java.io.Serializable)valueCache.getKey(indirection);
                    return cachedValue;
                }
            else {
                throw new IndirectionException(indirection);
            }
        }
        else {
            int indirection = get_offset() - 4;
            boolean saveIsChunked = isChunked;
            isChunked = repIdUtil.isChunkedEncoding(vType);
            java.lang.Object value = null;
            String codebase_URL = null;
            if (repIdUtil.isCodeBasePresent(vType)){
                codebase_URL = read_codebase_URL();
            }
            String repositoryIDString
                = readRepositoryIds(vType, null, null, factory);
            if (!repositoryIDString.equals(factory.get_id()))
                factory = Utility.getHelper(null, codebase_URL, repositoryIDString);
            start_block();
            end_flag--;
            if (isChunked)
                chunkedValueNestingLevel--;
            if (factory instanceof ValueHelper) {
                value = readIDLValueWithHelper((ValueHelper)factory, indirection);
            } else {
                valueIndirection = indirection;  
                value = factory.read_value(parent);
            }
            handleEndOfValue();
            readEndTag();
            if (valueCache == null)
                valueCache = new CacheTable(orb,false);
            valueCache.put(value, indirection);
            isChunked = saveIsChunked;
            start_block();
            return (java.io.Serializable)value;
        }
    }
    private boolean isCustomType(ValueHelper helper) {
        try{
            TypeCode tc = helper.get_type();
            int kind = tc.kind().value();
            if (kind == TCKind._tk_value) {
                return (tc.type_modifier() == org.omg.CORBA.VM_CUSTOM.value);
            }
        } catch(BadKind ex) {
            throw wrapper.badKind(ex) ;
        }
        return false;
    }
    public java.io.Serializable read_value(java.io.Serializable value) {
        if (valueCache == null)
            valueCache = new CacheTable(orb,false);
        valueCache.put(value, valueIndirection);
        if (value instanceof StreamableValue)
            ((StreamableValue)value)._read(parent);
        else if (value instanceof CustomValue)
            ((CustomValue)value).unmarshal(parent);
        return value;
    }
    public java.io.Serializable read_value(java.lang.String repositoryId) {
        int vType = readValueTag();
        if (vType == 0)
            return null; 
        else if (vType == 0xffffffff) { 
            int indirection = read_long() + get_offset() - 4;
            if (valueCache != null && valueCache.containsVal(indirection))
                {
                    java.io.Serializable cachedValue =
                          (java.io.Serializable)valueCache.getKey(indirection);
                    return cachedValue;
                }
            else {
                throw new IndirectionException(indirection);
            }
        }
        else {
            int indirection = get_offset() - 4;
            boolean saveIsChunked = isChunked;
            isChunked = repIdUtil.isChunkedEncoding(vType);
            java.lang.Object value = null;
            String codebase_URL = null;
            if (repIdUtil.isCodeBasePresent(vType)){
                codebase_URL = read_codebase_URL();
            }
            String repositoryIDString
                = readRepositoryIds(vType, null, repositoryId);
            ValueFactory factory =
               Utility.getFactory(null, codebase_URL, orb, repositoryIDString);
            start_block();
            end_flag--;
            if (isChunked)
                chunkedValueNestingLevel--;
            valueIndirection = indirection;  
            value = factory.read_value(parent);
            handleEndOfValue();
            readEndTag();
            if (valueCache == null)
                valueCache = new CacheTable(orb,false);
            valueCache.put(value, indirection);
            isChunked = saveIsChunked;
            start_block();
            return (java.io.Serializable)value;
        }
    }
    private Class readClass() {
        String codebases = null, classRepId = null;
        if (orb == null ||
            ORBVersionFactory.getFOREIGN().equals(orb.getORBVersion()) ||
            ORBVersionFactory.getNEWER().compareTo(orb.getORBVersion()) <= 0) {
            codebases = (String)read_value(java.lang.String.class);
            classRepId = (String)read_value(java.lang.String.class);
        } else {
            classRepId = (String)read_value(java.lang.String.class);
            codebases = (String)read_value(java.lang.String.class);
        }
        if (debug) {
            dprint("readClass codebases: "
                   + codebases
                   + " rep Id: "
                   + classRepId);
        }
        Class cl = null;
        RepositoryIdInterface repositoryID
            = repIdStrs.getFromString(classRepId);
        try {
            cl = repositoryID.getClassFromType(codebases);
        } catch(ClassNotFoundException cnfe) {
            throw wrapper.cnfeReadClass( CompletionStatus.COMPLETED_MAYBE,
                cnfe, repositoryID.getClassName() ) ;
        } catch(MalformedURLException me) {
            throw wrapper.malformedUrl( CompletionStatus.COMPLETED_MAYBE,
                me, repositoryID.getClassName(), codebases ) ;
        }
        return cl;
    }
    private java.lang.Object readIDLValueWithHelper(ValueHelper helper, int indirection)
    {
        Method readMethod;
        try {
            Class argTypes[] = {org.omg.CORBA.portable.InputStream.class, helper.get_class()};
            readMethod = helper.getClass().getDeclaredMethod(kReadMethod, argTypes);
        }
        catch(NoSuchMethodException nsme) { 
            java.lang.Object result = helper.read_value(parent);
            return result;
        }
        java.lang.Object val = null;
        try {
            val = helper.get_class().newInstance();
        } catch(java.lang.InstantiationException ie) {
            throw wrapper.couldNotInstantiateHelper( ie,
                helper.get_class() ) ;
        } catch(IllegalAccessException iae){
            return helper.read_value(parent);
        }
        if (valueCache == null)
            valueCache = new CacheTable(orb,false);
        valueCache.put(val, indirection);
        if (val instanceof CustomMarshal && isCustomType(helper)) {
            ((CustomMarshal)val).unmarshal(parent);
            return val;
        }
        try {
            java.lang.Object args[] = {parent, val};
            readMethod.invoke(helper, args);
            return val;
        } catch(IllegalAccessException iae2) {
            throw wrapper.couldNotInvokeHelperReadMethod( iae2, helper.get_class() ) ;
        } catch(InvocationTargetException ite){
            throw wrapper.couldNotInvokeHelperReadMethod( ite, helper.get_class() ) ;
        }
    }
    private java.lang.Object readBoxedIDLEntity(Class clazz, String codebase)
    {
        Class cls = null ;
        try {
            ClassLoader clazzLoader = (clazz == null ? null : clazz.getClassLoader());
            cls = Utility.loadClassForClass(clazz.getName()+"Helper", codebase,
                                                   clazzLoader, clazz, clazzLoader);
            final Class helperClass = cls ;
            final Class argTypes[] = {org.omg.CORBA.portable.InputStream.class};
            Method readMethod = null;
            try {
                readMethod = (Method)AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public java.lang.Object run() throws NoSuchMethodException {
                            return helperClass.getDeclaredMethod(kReadMethod, argTypes);
                        }
                    }
                );
            } catch (PrivilegedActionException pae) {
                throw (NoSuchMethodException)pae.getException();
            }
            java.lang.Object args[] = {parent};
            return readMethod.invoke(null, args);
        } catch (ClassNotFoundException cnfe) {
            throw wrapper.couldNotInvokeHelperReadMethod( cnfe, cls ) ;
        } catch(NoSuchMethodException nsme) {
            throw wrapper.couldNotInvokeHelperReadMethod( nsme, cls ) ;
        } catch(IllegalAccessException iae) {
            throw wrapper.couldNotInvokeHelperReadMethod( iae, cls ) ;
        } catch(InvocationTargetException ite) {
            throw wrapper.couldNotInvokeHelperReadMethod( ite, cls ) ;
        }
    }
    private java.lang.Object readIDLValue(int indirection, String repId,
                                          Class clazz, String codebase)
    {
        ValueFactory factory ;
        try {
            factory = Utility.getFactory(clazz, codebase, orb, repId);
        } catch (MARSHAL marshal) {
            if (!StreamableValue.class.isAssignableFrom(clazz) &&
                !CustomValue.class.isAssignableFrom(clazz) &&
                ValueBase.class.isAssignableFrom(clazz)) {
                BoxedValueHelper helper = Utility.getHelper(clazz, codebase, repId);
                if (helper instanceof ValueHelper)
                    return readIDLValueWithHelper((ValueHelper)helper, indirection);
                else
                    return helper.read_value(parent);
            } else {
                return readBoxedIDLEntity(clazz, codebase);
            }
        }
        valueIndirection = indirection;  
        return factory.read_value(parent);
    }
    private void readEndTag() {
        if (isChunked) {
            int anEndTag = read_long();
            if (anEndTag >= 0) {
                throw wrapper.positiveEndTag( CompletionStatus.COMPLETED_MAYBE,
                    new Integer(anEndTag), new Integer( get_offset() - 4 ) ) ;
            }
            if (orb == null ||
                ORBVersionFactory.getFOREIGN().equals(orb.getORBVersion()) ||
                ORBVersionFactory.getNEWER().compareTo(orb.getORBVersion()) <= 0) {
                if (anEndTag < chunkedValueNestingLevel)
                    throw wrapper.unexpectedEnclosingValuetype(
                        CompletionStatus.COMPLETED_MAYBE, new Integer( anEndTag ),
                        new Integer( chunkedValueNestingLevel ) ) ;
                if (anEndTag != chunkedValueNestingLevel) {
                    bbwi.position(bbwi.position() - 4);
                 }
            } else {
                if (anEndTag != end_flag) {
                    bbwi.position(bbwi.position() - 4);
                }
            }
            chunkedValueNestingLevel++;
        }
        end_flag++;
    }
    protected int get_offset() {
        return bbwi.position();
    }
    private void start_block() {
        if (!isChunked)
            return;
        blockLength = maxBlockLength;
        blockLength = read_long();
        if (blockLength > 0 && blockLength < maxBlockLength) {
            blockLength += get_offset();  
        } else {
            blockLength = maxBlockLength;
            bbwi.position(bbwi.position() - 4);
        }
    }
    private void handleEndOfValue() {
        if (!isChunked)
            return;
        while (blockLength != maxBlockLength) {
            end_block();
            start_block();
        }
        int nextLong = read_long();
        bbwi.position(bbwi.position() - 4);
        if (nextLong < 0)
            return;
        if (nextLong == 0 || nextLong >= maxBlockLength) {
            read_value();
            handleEndOfValue();
        } else {
            throw wrapper.couldNotSkipBytes( CompletionStatus.COMPLETED_MAYBE,
                new Integer( nextLong ), new Integer( get_offset() ) ) ;
        }
    }
    private void end_block() {
        if (blockLength != maxBlockLength) {
            if (blockLength == get_offset()) {
                blockLength = maxBlockLength;
            } else {
                if (blockLength > get_offset()) {
                    skipToOffset(blockLength);
                } else {
                    throw wrapper.badChunkLength( new Integer( blockLength ),
                        new Integer( get_offset() ) ) ;
                }
            }
        }
    }
    private int readValueTag(){
        return read_long();
    }
    public org.omg.CORBA.ORB orb() {
        return orb;
    }
    public final void read_boolean_array(boolean[] value, int offset, int length) {
        for(int i=0; i < length; i++) {
            value[i+offset] = read_boolean();
        }
    }
    public final void read_char_array(char[] value, int offset, int length) {
        for(int i=0; i < length; i++) {
            value[i+offset] = read_char();
        }
    }
    public final void read_wchar_array(char[] value, int offset, int length) {
        for(int i=0; i < length; i++) {
            value[i+offset] = read_wchar();
        }
    }
    public final void read_short_array(short[] value, int offset, int length) {
        for(int i=0; i < length; i++) {
            value[i+offset] = read_short();
        }
    }
    public final void read_ushort_array(short[] value, int offset, int length) {
        read_short_array(value, offset, length);
    }
    public final void read_long_array(int[] value, int offset, int length) {
        for(int i=0; i < length; i++) {
            value[i+offset] = read_long();
        }
    }
    public final void read_ulong_array(int[] value, int offset, int length) {
        read_long_array(value, offset, length);
    }
    public final void read_longlong_array(long[] value, int offset, int length) {
        for(int i=0; i < length; i++) {
            value[i+offset] = read_longlong();
        }
    }
    public final void read_ulonglong_array(long[] value, int offset, int length) {
        read_longlong_array(value, offset, length);
    }
    public final void read_float_array(float[] value, int offset, int length) {
        for(int i=0; i < length; i++) {
            value[i+offset] = read_float();
        }
    }
    public final void read_double_array(double[] value, int offset, int length) {
        for(int i=0; i < length; i++) {
            value[i+offset] = read_double();
        }
    }
    public final void read_any_array(org.omg.CORBA.Any[] value, int offset, int length) {
        for(int i=0; i < length; i++) {
            value[i+offset] = read_any();
        }
    }
    private String read_repositoryIds() {
        int numRepIds = read_long();
        if (numRepIds == 0xffffffff) {
            int indirection = read_long() + get_offset() - 4;
            if (repositoryIdCache != null && repositoryIdCache.containsOrderedVal(indirection))
                return (String)repositoryIdCache.getKey(indirection);
            else
                throw wrapper.unableToLocateRepIdArray( new Integer( indirection ) ) ;
        } else {
            int indirection = get_offset();
            String repID = read_repositoryId();
            if (repositoryIdCache == null)
                repositoryIdCache = new CacheTable(orb,false);
            repositoryIdCache.put(repID, indirection);
            for (int i = 1; i < numRepIds; i++) {
                read_repositoryId();
            }
            return repID;
        }
    }
    private final String read_repositoryId()
    {
        String result = readStringOrIndirection(true);
        if (result == null) { 
            int indirection = read_long() + get_offset() - 4;
            if (repositoryIdCache != null && repositoryIdCache.containsOrderedVal(indirection))
                return (String)repositoryIdCache.getKey(indirection);
            else
                throw wrapper.badRepIdIndirection( CompletionStatus.COMPLETED_MAYBE,
                    new Integer(bbwi.position()) ) ;
        } else {
            if (repositoryIdCache == null)
                repositoryIdCache = new CacheTable(orb,false);
            repositoryIdCache.put(result, stringIndirection);
        }
        return result ;
    }
    private final String read_codebase_URL()
    {
        String result = readStringOrIndirection(true);
        if (result == null) { 
            int indirection = read_long() + get_offset() - 4;
            if (codebaseCache != null && codebaseCache.containsVal(indirection))
                return (String)codebaseCache.getKey(indirection);
            else
                throw wrapper.badCodebaseIndirection(
                    CompletionStatus.COMPLETED_MAYBE,
                    new Integer(bbwi.position()) ) ;
        } else {
            if (codebaseCache == null)
                codebaseCache = new CacheTable(orb,false);
            codebaseCache.put(result, stringIndirection);
        }
        return result;
    }
    public java.lang.Object read_Abstract () {
        return read_abstract_interface();
    }
    public java.io.Serializable read_Value () {
        return read_value();
    }
    public void read_any_array (org.omg.CORBA.AnySeqHolder seq, int offset, int length) {
        read_any_array(seq.value, offset, length);
    }
    public void read_boolean_array (org.omg.CORBA.BooleanSeqHolder seq, int offset, int length) {
        read_boolean_array(seq.value, offset, length);
    }
    public void read_char_array (org.omg.CORBA.CharSeqHolder seq, int offset, int length) {
        read_char_array(seq.value, offset, length);
    }
    public void read_wchar_array (org.omg.CORBA.WCharSeqHolder seq, int offset, int length) {
        read_wchar_array(seq.value, offset, length);
    }
    public void read_octet_array (org.omg.CORBA.OctetSeqHolder seq, int offset, int length) {
        read_octet_array(seq.value, offset, length);
    }
    public void read_short_array (org.omg.CORBA.ShortSeqHolder seq, int offset, int length) {
        read_short_array(seq.value, offset, length);
    }
    public void read_ushort_array (org.omg.CORBA.UShortSeqHolder seq, int offset, int length) {
        read_ushort_array(seq.value, offset, length);
    }
    public void read_long_array (org.omg.CORBA.LongSeqHolder seq, int offset, int length) {
        read_long_array(seq.value, offset, length);
    }
    public void read_ulong_array (org.omg.CORBA.ULongSeqHolder seq, int offset, int length) {
        read_ulong_array(seq.value, offset, length);
    }
    public void read_ulonglong_array (org.omg.CORBA.ULongLongSeqHolder seq, int offset, int length) {
        read_ulonglong_array(seq.value, offset, length);
    }
    public void read_longlong_array (org.omg.CORBA.LongLongSeqHolder seq, int offset, int length) {
        read_longlong_array(seq.value, offset, length);
    }
    public void read_float_array (org.omg.CORBA.FloatSeqHolder seq, int offset, int length) {
        read_float_array(seq.value, offset, length);
    }
    public void read_double_array (org.omg.CORBA.DoubleSeqHolder seq, int offset, int length) {
        read_double_array(seq.value, offset, length);
    }
    public java.math.BigDecimal read_fixed(short digits, short scale) {
        StringBuffer buffer = read_fixed_buffer();
        if (digits != buffer.length())
            throw wrapper.badFixed( new Integer(digits),
                new Integer(buffer.length()) ) ;
        buffer.insert(digits - scale, '.');
        return new BigDecimal(buffer.toString());
    }
    public java.math.BigDecimal read_fixed() {
        return new BigDecimal(read_fixed_buffer().toString());
    }
    private StringBuffer read_fixed_buffer() {
        StringBuffer buffer = new StringBuffer(64);
        byte doubleDigit;
        int firstDigit;
        int secondDigit;
        boolean wroteFirstDigit = false;
        boolean more = true;
        while (more) {
            doubleDigit = this.read_octet();
            firstDigit = (int)((doubleDigit & 0xf0) >> 4);
            secondDigit = (int)(doubleDigit & 0x0f);
            if (wroteFirstDigit || firstDigit != 0) {
                buffer.append(Character.forDigit(firstDigit, 10));
                wroteFirstDigit = true;
            }
            if (secondDigit == 12) {
                if ( ! wroteFirstDigit) {
                    return new StringBuffer("0.0");
                } else {
                }
                more = false;
            } else if (secondDigit == 13) {
                buffer.insert(0, '-');
                more = false;
            } else {
                buffer.append(Character.forDigit(secondDigit, 10));
                wroteFirstDigit = true;
            }
        }
        return buffer;
    }
    private final static String _id = "IDL:omg.org/CORBA/DataInputStream:1.0";
    private final static String[] _ids = { _id };
    public String[] _truncatable_ids() {
        if (_ids == null)
            return null;
        return (String[])_ids.clone();
    }
    public void printBuffer() {
        CDRInputStream_1_0.printBuffer(this.bbwi);
    }
    public static void printBuffer(ByteBufferWithInfo bbwi) {
        System.out.println("----- Input Buffer -----");
        System.out.println();
        System.out.println("Current position: " + bbwi.position());
        System.out.println("Total length : " + bbwi.buflen);
        System.out.println();
        try {
            char[] charBuf = new char[16];
            for (int i = 0; i < bbwi.buflen; i += 16) {
                int j = 0;
                while (j < 16 && j + i < bbwi.buflen) {
                    int k = bbwi.byteBuffer.get(i + j);
                    if (k < 0)
                        k = 256 + k;
                    String hex = Integer.toHexString(k);
                    if (hex.length() == 1)
                        hex = "0" + hex;
                    System.out.print(hex + " ");
                    j++;
                }
                while (j < 16) {
                    System.out.print("   ");
                    j++;
                }
                int x = 0;
                while (x < 16 && x + i < bbwi.buflen) {
                    if (ORBUtility.isPrintable((char)bbwi.byteBuffer.get(i + x)))
                        charBuf[x] = (char)bbwi.byteBuffer.get(i + x);
                    else
                        charBuf[x] = '.';
                    x++;
                }
                System.out.println(new String(charBuf, 0, x));
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        System.out.println("------------------------");
    }
    public ByteBuffer getByteBuffer() {
        ByteBuffer result = null;
        if (bbwi != null) {
            result = bbwi.byteBuffer;
        }
        return result;
    }
    public int getBufferLength() {
        return bbwi.buflen;
    }
    public void setBufferLength(int value) {
        bbwi.buflen = value;
        bbwi.byteBuffer.limit(bbwi.buflen);
    }
    public void setByteBufferWithInfo(ByteBufferWithInfo bbwi) {
        this.bbwi = bbwi;
    }
    public void setByteBuffer(ByteBuffer byteBuffer) {
        bbwi.byteBuffer = byteBuffer;
    }
    public int getIndex() {
        return bbwi.position();
    }
    public void setIndex(int value) {
        bbwi.position(value);
    }
    public boolean isLittleEndian() {
        return littleEndian;
    }
    public void orb(org.omg.CORBA.ORB orb) {
        this.orb = (ORB)orb;
    }
    public BufferManagerRead getBufferManager() {
        return bufferManagerRead;
    }
    private void skipToOffset(int offset) {
        int len = offset - get_offset();
        int n = 0;
        while (n < len) {
            int avail;
            int bytes;
            int wanted;
            avail = bbwi.buflen - bbwi.position();
            if (avail <= 0) {
                grow(1, 1);
                avail = bbwi.buflen - bbwi.position();
            }
            wanted = len - n;
            bytes = (wanted < avail) ? wanted : avail;
            bbwi.position(bbwi.position() + bytes);
            n += bytes;
        }
    }
    protected MarkAndResetHandler markAndResetHandler = null;
    protected class StreamMemento
    {
        private int blockLength_;
        private int end_flag_;
        private int chunkedValueNestingLevel_;
        private int valueIndirection_;
        private int stringIndirection_;
        private boolean isChunked_;
        private javax.rmi.CORBA.ValueHandler valueHandler_;
        private ByteBufferWithInfo bbwi_;
        private boolean specialNoOptionalDataState_;
        public StreamMemento()
        {
            blockLength_ = blockLength;
            end_flag_ = end_flag;
            chunkedValueNestingLevel_ = chunkedValueNestingLevel;
            valueIndirection_ = valueIndirection;
            stringIndirection_ = stringIndirection;
            isChunked_ = isChunked;
            valueHandler_ = valueHandler;
            specialNoOptionalDataState_ = specialNoOptionalDataState;
            bbwi_ = new ByteBufferWithInfo(bbwi);
        }
    }
    public java.lang.Object createStreamMemento() {
        return new StreamMemento();
    }
    public void restoreInternalState(java.lang.Object streamMemento) {
        StreamMemento mem = (StreamMemento)streamMemento;
        blockLength = mem.blockLength_;
        end_flag = mem.end_flag_;
        chunkedValueNestingLevel = mem.chunkedValueNestingLevel_;
        valueIndirection = mem.valueIndirection_;
        stringIndirection = mem.stringIndirection_;
        isChunked = mem.isChunked_;
        valueHandler = mem.valueHandler_;
        specialNoOptionalDataState = mem.specialNoOptionalDataState_;
        bbwi = mem.bbwi_;
    }
    public int getPosition() {
        return get_offset();
    }
    public void mark(int readlimit) {
        markAndResetHandler.mark(this);
    }
    public void reset() {
        markAndResetHandler.reset();
    }
    CodeBase getCodeBase() {
        return parent.getCodeBase();
    }
    private Class getClassFromString(String repositoryIDString,
                                     String codebaseURL,
                                     Class expectedType)
    {
        RepositoryIdInterface repositoryID
            = repIdStrs.getFromString(repositoryIDString);
        try {
            try {
                return repositoryID.getClassFromType(expectedType,
                                                     codebaseURL);
            } catch (ClassNotFoundException cnfeOuter) {
                try {
                    if (getCodeBase() == null) {
                        return null; 
                    }
                    codebaseURL = getCodeBase().implementation(repositoryIDString);
                    if (codebaseURL == null)
                        return null;
                    return repositoryID.getClassFromType(expectedType,
                                                         codebaseURL);
                } catch (ClassNotFoundException cnfeInner) {
                    dprintThrowable(cnfeInner);
                    return null;
                }
            }
        } catch (MalformedURLException mue) {
            throw wrapper.malformedUrl( CompletionStatus.COMPLETED_MAYBE,
                mue, repositoryIDString, codebaseURL ) ;
        }
    }
    private Class getClassFromString(String repositoryIDString,
                                     String codebaseURL)
    {
        RepositoryIdInterface repositoryID
            = repIdStrs.getFromString(repositoryIDString);
        for (int i = 0; i < 3; i++) {
            try {
                switch (i)
                {
                    case 0:
                        return repositoryID.getClassFromType();
                    case 1:
                        break;
                    case 2:
                        codebaseURL = getCodeBase().implementation(repositoryIDString);
                        break;
                }
                if (codebaseURL == null)
                    continue;
                return repositoryID.getClassFromType(codebaseURL);
            } catch(ClassNotFoundException cnfe) {
            } catch (MalformedURLException mue) {
                throw wrapper.malformedUrl( CompletionStatus.COMPLETED_MAYBE,
                    mue, repositoryIDString, codebaseURL ) ;
            }
        }
        dprint("getClassFromString failed with rep id "
               + repositoryIDString
               + " and codebase "
               + codebaseURL);
        return null;
    }
    char[] getConvertedChars(int numBytes,
                             CodeSetConversion.BTCConverter converter) {
        if (bbwi.buflen - bbwi.position() >= numBytes) {
            byte[] tmpBuf;
            if (bbwi.byteBuffer.hasArray())
            {
                tmpBuf = bbwi.byteBuffer.array();
            }
            else
            {
                 tmpBuf = new byte[bbwi.buflen];
                 for (int i = 0; i < bbwi.buflen; i++)
                     tmpBuf[i] = bbwi.byteBuffer.get(i);
            }
            char[] result = converter.getChars(tmpBuf,bbwi.position(),numBytes);
            bbwi.position(bbwi.position() + numBytes);
            return result;
        } else {
            byte[] bytes = new byte[numBytes];
            read_octet_array(bytes, 0, bytes.length);
            return converter.getChars(bytes, 0, numBytes);
        }
    }
    protected CodeSetConversion.BTCConverter getCharConverter() {
        if (charConverter == null)
            charConverter = parent.createCharBTCConverter();
        return charConverter;
    }
    protected CodeSetConversion.BTCConverter getWCharConverter() {
        if (wcharConverter == null)
            wcharConverter = parent.createWCharBTCConverter();
        return wcharConverter;
    }
    protected void dprintThrowable(Throwable t) {
        if (debug && t != null)
            t.printStackTrace();
    }
    protected void dprint(String msg) {
        if (debug) {
            ORBUtility.dprint(this, msg);
        }
    }
    void alignOnBoundary(int octetBoundary) {
        int needed = computeAlignment(bbwi.position(), octetBoundary);
        if (bbwi.position() + needed <= bbwi.buflen)
        {
            bbwi.position(bbwi.position() + needed);
        }
    }
    public void resetCodeSetConverters() {
        charConverter = null;
        wcharConverter = null;
    }
    public void start_value() {
        int vType = readValueTag();
        if (vType == 0) {
            specialNoOptionalDataState = true;
            return;
        }
        if (vType == 0xffffffff) {
            throw wrapper.customWrapperIndirection(
                CompletionStatus.COMPLETED_MAYBE);
        }
        if (repIdUtil.isCodeBasePresent(vType)) {
            throw wrapper.customWrapperWithCodebase(
                CompletionStatus.COMPLETED_MAYBE);
        }
        if (repIdUtil.getTypeInfo(vType)
            != RepositoryIdUtility.SINGLE_REP_TYPE_INFO) {
            throw wrapper.customWrapperNotSingleRepid(
                CompletionStatus.COMPLETED_MAYBE);
        }
        read_repositoryId();
        start_block();
        end_flag--;
        chunkedValueNestingLevel--;
    }
    public void end_value() {
        if (specialNoOptionalDataState) {
            specialNoOptionalDataState = false;
            return;
        }
        handleEndOfValue();
        readEndTag();
        start_block();
    }
    public void close() throws IOException
    {
        getBufferManager().close(bbwi);
        if (bbwi != null && getByteBuffer() != null)
        {
            int bbHash = System.identityHashCode(bbwi.byteBuffer);
            MessageMediator messageMediator = parent.getMessageMediator();
            if (messageMediator != null)
            {
                CDROutputObject outputObj =
                             (CDROutputObject)messageMediator.getOutputObject();
                if (outputObj != null)
                {
                    ByteBuffer outputBb = outputObj.getByteBuffer();
                    int oBbHash = 0;
                    if (outputBb != null)
                    {
                        oBbHash = System.identityHashCode(outputBb);
                        if (bbHash == oBbHash)  
                        {
                            outputObj.setByteBuffer(null);
                            outputObj.setByteBufferWithInfo(null);
                        }
                    }
                }
            }
            ByteBufferPool byteBufferPool = orb.getByteBufferPool();
            if (debug)
            {
                int bbAddress = System.identityHashCode(bbwi.byteBuffer);
                StringBuffer sb = new StringBuffer(80);
                sb.append(".close - releasing ByteBuffer id (");
                sb.append(bbAddress).append(") to ByteBufferPool.");
                String msg = sb.toString();
                dprint(msg);
            }
            byteBufferPool.releaseByteBuffer(bbwi.byteBuffer);
            bbwi.byteBuffer = null;
            bbwi = null;
        }
    }
}
