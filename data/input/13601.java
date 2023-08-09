public class CDROutputStream_1_0 extends CDROutputStreamBase
{
    private static final int INDIRECTION_TAG = 0xffffffff;
    protected boolean littleEndian;
    protected BufferManagerWrite bufferManagerWrite;
    ByteBufferWithInfo bbwi;
    protected ORB orb;
    protected ORBUtilSystemException wrapper ;
    protected boolean debug = false;
    protected int blockSizeIndex = -1;
    protected int blockSizePosition = 0;
    protected byte streamFormatVersion;
    private static final int DEFAULT_BUFFER_SIZE = 1024;
    private static final String kWriteMethod = "write";
    private CacheTable codebaseCache = null;
    private CacheTable valueCache = null;
    private CacheTable repositoryIdCache = null;
    private int end_flag = 0;
    private int chunkedValueNestingLevel = 0;
    private boolean mustChunk = false;
    protected boolean inBlock = false;
    private int end_flag_position = 0;
    private int end_flag_index = 0;
    private ValueHandler valueHandler = null;
    private RepositoryIdUtility repIdUtil;
    private RepositoryIdStrings repIdStrs;
    private CodeSetConversion.CTBConverter charConverter;
    private CodeSetConversion.CTBConverter wcharConverter;
    public void init(org.omg.CORBA.ORB orb,
                        boolean littleEndian,
                        BufferManagerWrite bufferManager,
                        byte streamFormatVersion,
                        boolean usePooledByteBuffers)
    {
        this.orb = (ORB)orb;
        this.wrapper = ORBUtilSystemException.get( this.orb,
            CORBALogDomains.RPC_ENCODING ) ;
        debug = this.orb.transportDebugFlag;
        this.littleEndian = littleEndian;
        this.bufferManagerWrite = bufferManager;
        this.bbwi = new ByteBufferWithInfo(orb, bufferManager, usePooledByteBuffers);
        this.streamFormatVersion = streamFormatVersion;
        createRepositoryIdHandlers();
    }
    public void init(org.omg.CORBA.ORB orb,
                        boolean littleEndian,
                        BufferManagerWrite bufferManager,
                        byte streamFormatVersion)
   {
       init(orb, littleEndian, bufferManager, streamFormatVersion, true);
   }
    private final void createRepositoryIdHandlers()
    {
        if (orb != null) {
            repIdUtil
                = RepositoryIdFactory.getRepIdUtility(orb);
            repIdStrs
                = RepositoryIdFactory.getRepIdStringsFactory(orb);
        } else {
            repIdUtil = RepositoryIdFactory.getRepIdUtility();
            repIdStrs = RepositoryIdFactory.getRepIdStringsFactory();
        }
    }
    public BufferManagerWrite getBufferManager()
    {
        return bufferManagerWrite;
    }
    public byte[] toByteArray() {
        byte[] it;
        it = new byte[bbwi.position()];
        for (int i = 0; i < bbwi.position(); i++)
            it[i] = bbwi.byteBuffer.get(i);
        return it;
    }
    public GIOPVersion getGIOPVersion() {
        return GIOPVersion.V1_0;
    }
    void setHeaderPadding(boolean headerPadding) {
        throw wrapper.giopVersionError();
    }
    protected void handleSpecialChunkBegin(int requiredSize)
    {
    }
    protected void handleSpecialChunkEnd()
    {
    }
    protected final int computeAlignment(int align) {
        if (align > 1) {
            int incr = bbwi.position() & (align - 1);
            if (incr != 0)
                return align - incr;
        }
        return 0;
    }
    protected void alignAndReserve(int align, int n) {
        bbwi.position(bbwi.position() + computeAlignment(align));
        if (bbwi.position() + n  > bbwi.buflen)
            grow(align, n);
    }
    protected void grow(int align, int n)
    {
        bbwi.needed = n;
        bufferManagerWrite.overflow(bbwi);
    }
    public final void putEndian() throws SystemException {
        write_boolean(littleEndian);
    }
    public final boolean littleEndian() {
        return littleEndian;
    }
    void freeInternalCaches() {
        if (codebaseCache != null)
            codebaseCache.done();
        if (valueCache != null)
            valueCache.done();
        if (repositoryIdCache != null)
            repositoryIdCache.done();
    }
    public final void write_longdouble(double x)
    {
        throw wrapper.longDoubleNotImplemented(
            CompletionStatus.COMPLETED_MAYBE ) ;
    }
    public void write_octet(byte x)
    {
            alignAndReserve(1, 1);
        bbwi.byteBuffer.put(bbwi.position(), x);
        bbwi.position(bbwi.position() + 1);
    }
    public final void write_boolean(boolean x)
    {
        write_octet(x? (byte)1:(byte)0);
    }
    public void write_char(char x)
    {
        CodeSetConversion.CTBConverter converter = getCharConverter();
        converter.convert(x);
        if (converter.getNumBytes() > 1)
            throw wrapper.invalidSingleCharCtb(CompletionStatus.COMPLETED_MAYBE);
        write_octet(converter.getBytes()[0]);
    }
    private final void writeLittleEndianWchar(char x) {
        bbwi.byteBuffer.put(bbwi.position(), (byte)(x & 0xFF));
        bbwi.byteBuffer.put(bbwi.position() + 1, (byte)((x >>> 8) & 0xFF));
        bbwi.position(bbwi.position() + 2);
    }
    private final void writeBigEndianWchar(char x) {
        bbwi.byteBuffer.put(bbwi.position(), (byte)((x >>> 8) & 0xFF));
        bbwi.byteBuffer.put(bbwi.position() + 1, (byte)(x & 0xFF));
        bbwi.position(bbwi.position() + 2);
    }
    private final void writeLittleEndianShort(short x) {
        bbwi.byteBuffer.put(bbwi.position(), (byte)(x & 0xFF));
        bbwi.byteBuffer.put(bbwi.position() + 1, (byte)((x >>> 8) & 0xFF));
        bbwi.position(bbwi.position() + 2);
    }
    private final void writeBigEndianShort(short x) {
        bbwi.byteBuffer.put(bbwi.position(), (byte)((x >>> 8) & 0xFF));
        bbwi.byteBuffer.put(bbwi.position() + 1, (byte)(x & 0xFF));
        bbwi.position(bbwi.position() + 2);
    }
    private final void writeLittleEndianLong(int x) {
        bbwi.byteBuffer.put(bbwi.position(), (byte)(x & 0xFF));
        bbwi.byteBuffer.put(bbwi.position() + 1, (byte)((x >>> 8) & 0xFF));
        bbwi.byteBuffer.put(bbwi.position() + 2, (byte)((x >>> 16) & 0xFF));
        bbwi.byteBuffer.put(bbwi.position() + 3, (byte)((x >>> 24) & 0xFF));
        bbwi.position(bbwi.position() + 4);
    }
    private final void writeBigEndianLong(int x) {
        bbwi.byteBuffer.put(bbwi.position(), (byte)((x >>> 24) & 0xFF));
        bbwi.byteBuffer.put(bbwi.position() + 1, (byte)((x >>> 16) & 0xFF));
        bbwi.byteBuffer.put(bbwi.position() + 2, (byte)((x >>> 8) & 0xFF));
        bbwi.byteBuffer.put(bbwi.position() + 3, (byte)(x & 0xFF));
        bbwi.position(bbwi.position() + 4);
    }
    private final void writeLittleEndianLongLong(long x) {
        bbwi.byteBuffer.put(bbwi.position(), (byte)(x & 0xFF));
        bbwi.byteBuffer.put(bbwi.position() + 1, (byte)((x >>> 8) & 0xFF));
        bbwi.byteBuffer.put(bbwi.position() + 2, (byte)((x >>> 16) & 0xFF));
        bbwi.byteBuffer.put(bbwi.position() + 3, (byte)((x >>> 24) & 0xFF));
        bbwi.byteBuffer.put(bbwi.position() + 4, (byte)((x >>> 32) & 0xFF));
        bbwi.byteBuffer.put(bbwi.position() + 5, (byte)((x >>> 40) & 0xFF));
        bbwi.byteBuffer.put(bbwi.position() + 6, (byte)((x >>> 48) & 0xFF));
        bbwi.byteBuffer.put(bbwi.position() + 7, (byte)((x >>> 56) & 0xFF));
        bbwi.position(bbwi.position() + 8);
    }
    private final void writeBigEndianLongLong(long x) {
        bbwi.byteBuffer.put(bbwi.position(), (byte)((x >>> 56) & 0xFF));
        bbwi.byteBuffer.put(bbwi.position() + 1, (byte)((x >>> 48) & 0xFF));
        bbwi.byteBuffer.put(bbwi.position() + 2, (byte)((x >>> 40) & 0xFF));
        bbwi.byteBuffer.put(bbwi.position() + 3, (byte)((x >>> 32) & 0xFF));
        bbwi.byteBuffer.put(bbwi.position() + 4, (byte)((x >>> 24) & 0xFF));
        bbwi.byteBuffer.put(bbwi.position() + 5, (byte)((x >>> 16) & 0xFF));
        bbwi.byteBuffer.put(bbwi.position() + 6, (byte)((x >>> 8) & 0xFF));
        bbwi.byteBuffer.put(bbwi.position() + 7, (byte)(x & 0xFF));
        bbwi.position(bbwi.position() + 8);
    }
    public void write_wchar(char x)
    {
        if (ORBUtility.isForeignORB(orb)) {
            throw wrapper.wcharDataInGiop10(CompletionStatus.COMPLETED_MAYBE);
        }
        alignAndReserve(2, 2);
        if (littleEndian) {
            writeLittleEndianWchar(x);
        } else {
            writeBigEndianWchar(x);
        }
    }
    public void write_short(short x)
    {
        alignAndReserve(2, 2);
        if (littleEndian) {
            writeLittleEndianShort(x);
        } else {
            writeBigEndianShort(x);
        }
    }
    public final void write_ushort(short x)
    {
        write_short(x);
    }
    public void write_long(int x)
    {
        alignAndReserve(4, 4);
        if (littleEndian) {
            writeLittleEndianLong(x);
        } else {
            writeBigEndianLong(x);
        }
    }
    public final void write_ulong(int x)
    {
        write_long(x);
    }
    public void write_longlong(long x)
    {
        alignAndReserve(8, 8);
        if (littleEndian) {
            writeLittleEndianLongLong(x);
        } else {
            writeBigEndianLongLong(x);
        }
    }
    public final void write_ulonglong(long x)
    {
        write_longlong(x);
    }
    public final void write_float(float x)
    {
        write_long(Float.floatToIntBits(x));
    }
    public final void write_double(double x)
    {
        write_longlong(Double.doubleToLongBits(x));
    }
    public void write_string(String value)
    {
      writeString(value);
    }
    protected int writeString(String value)
    {
        if (value == null) {
            throw wrapper.nullParam(CompletionStatus.COMPLETED_MAYBE);
        }
        CodeSetConversion.CTBConverter converter = getCharConverter();
        converter.convert(value);
        int len = converter.getNumBytes() + 1;
        handleSpecialChunkBegin(computeAlignment(4) + 4 + len);
        write_long(len);
        int indirection = get_offset() - 4;
        internalWriteOctetArray(converter.getBytes(), 0, converter.getNumBytes());
        write_octet((byte)0);
        handleSpecialChunkEnd();
        return indirection;
    }
    public void write_wstring(String value)
    {
        if (value == null)
            throw wrapper.nullParam(CompletionStatus.COMPLETED_MAYBE);
        if (ORBUtility.isForeignORB(orb)) {
            throw wrapper.wcharDataInGiop10(CompletionStatus.COMPLETED_MAYBE);
        }
        int len = value.length() + 1;
        handleSpecialChunkBegin(4 + (len * 2) + computeAlignment(4));
        write_long(len);
        for (int i = 0; i < len - 1; i++)
            write_wchar(value.charAt(i));
        write_short((short)0);
        handleSpecialChunkEnd();
    }
    void internalWriteOctetArray(byte[] value, int offset, int length)
    {
        int n = offset;
        boolean align = true;
        while (n < length+offset) {
            int avail;
            int bytes;
            int wanted;
            if ((bbwi.position() + 1 > bbwi.buflen) || align) {
                align = false;
                alignAndReserve(1, 1);
            }
            avail = bbwi.buflen - bbwi.position();
            wanted = (length + offset) - n;
            bytes = (wanted < avail) ? wanted : avail;
            for (int i = 0; i < bytes; i++)
                bbwi.byteBuffer.put(bbwi.position() + i, value[n+i]);
            bbwi.position(bbwi.position() + bytes);
            n += bytes;
        }
    }
    public final void write_octet_array(byte b[], int offset, int length)
    {
        if ( b == null )
            throw wrapper.nullParam(CompletionStatus.COMPLETED_MAYBE);
        handleSpecialChunkBegin(length);
        internalWriteOctetArray(b, offset, length);
        handleSpecialChunkEnd();
    }
    public void write_Principal(Principal p)
    {
        write_long(p.name().length);
        write_octet_array(p.name(), 0, p.name().length);
    }
    public void write_any(Any any)
    {
        if ( any == null )
            throw wrapper.nullParam(CompletionStatus.COMPLETED_MAYBE);
        write_TypeCode(any.type());
        any.write_value(parent);
    }
    public void write_TypeCode(TypeCode tc)
    {
        if ( tc == null ) {
            throw wrapper.nullParam(CompletionStatus.COMPLETED_MAYBE);
        }
        TypeCodeImpl tci;
        if (tc instanceof TypeCodeImpl) {
            tci = (TypeCodeImpl)tc;
        }
        else {
            tci = new TypeCodeImpl(orb, tc);
        }
        tci.write_value((org.omg.CORBA_2_3.portable.OutputStream)parent);
    }
    public void write_Object(org.omg.CORBA.Object ref)
    {
        if (ref == null) {
            IOR nullIOR = IORFactories.makeIOR( orb ) ;
            nullIOR.write(parent);
            return;
        }
        if (ref instanceof org.omg.CORBA.LocalObject)
            throw wrapper.writeLocalObject(CompletionStatus.COMPLETED_MAYBE);
        IOR ior = ORBUtility.connectAndGetIOR( orb, ref ) ;
        ior.write(parent);
        return;
    }
    public void write_abstract_interface(java.lang.Object obj) {
        boolean corbaObject = false; 
        org.omg.CORBA.Object theObject = null;
        if (obj != null && obj instanceof org.omg.CORBA.Object) {
            theObject = (org.omg.CORBA.Object)obj;
            corbaObject = true;
        }
        write_boolean(corbaObject);
        if (corbaObject) {
            write_Object(theObject);
        } else {
            try {
                write_value((java.io.Serializable)obj);
            } catch(ClassCastException cce) {
                if (obj instanceof java.io.Serializable)
                    throw cce;
                else
                    ORBUtility.throwNotSerializableForCorba(obj.getClass().getName());
            }
        }
    }
    public void write_value(Serializable object, Class clz) {
        write_value(object);
    }
    private void writeWStringValue(String string) {
        int indirection = writeValueTag(mustChunk, true, null);
        write_repositoryId(repIdStrs.getWStringValueRepId());
        updateIndirectionTable(indirection, string, string);
        if (mustChunk) {
            start_block();
            end_flag--;
            chunkedValueNestingLevel--;
        } else
            end_flag--;
        write_wstring(string);
        if (mustChunk)
            end_block();
        writeEndTag(mustChunk);
    }
    private void writeArray(Serializable array, Class clazz) {
        if (valueHandler == null)
            valueHandler = ORBUtility.createValueHandler(orb); 
        int indirection = writeValueTag(mustChunk, true,
                                        Util.getCodebase(clazz));
        write_repositoryId(repIdStrs.createSequenceRepID(clazz));
        updateIndirectionTable(indirection, array, array);
        if (mustChunk) {
            start_block();
            end_flag--;
            chunkedValueNestingLevel--;
        } else
            end_flag--;
        if (valueHandler instanceof ValueHandlerMultiFormat) {
            ValueHandlerMultiFormat vh = (ValueHandlerMultiFormat)valueHandler;
            vh.writeValue(parent, array, streamFormatVersion);
        } else
            valueHandler.writeValue(parent, array);
        if (mustChunk)
            end_block();
        writeEndTag(mustChunk);
    }
    private void writeValueBase(org.omg.CORBA.portable.ValueBase object,
                                Class clazz) {
        mustChunk = true;
        int indirection = writeValueTag(true, true, Util.getCodebase(clazz));
        String repId = ((ValueBase)object)._truncatable_ids()[0];
        write_repositoryId(repId);
        updateIndirectionTable(indirection, object, object);
        start_block();
        end_flag--;
        chunkedValueNestingLevel--;
        writeIDLValue(object, repId);
        end_block();
        writeEndTag(true);
    }
    private void writeRMIIIOPValueType(Serializable object, Class clazz) {
        if (valueHandler == null)
            valueHandler = ORBUtility.createValueHandler(orb); 
        Serializable key = object;
        object = valueHandler.writeReplace(key);
        if (object == null) {
            write_long(0);
            return;
        }
        if (object != key) {
            if (valueCache != null && valueCache.containsKey(object)) {
                writeIndirection(INDIRECTION_TAG, valueCache.getVal(object));
                return;
            }
            clazz = object.getClass();
        }
        if (mustChunk || valueHandler.isCustomMarshaled(clazz)) {
            mustChunk = true;
        }
        int indirection = writeValueTag(mustChunk, true, Util.getCodebase(clazz));
        write_repositoryId(repIdStrs.createForJavaType(clazz));
        updateIndirectionTable(indirection, object, key);
        if (mustChunk) {
            end_flag--;
            chunkedValueNestingLevel--;
            start_block();
        } else
            end_flag--;
        if (valueHandler instanceof ValueHandlerMultiFormat) {
            ValueHandlerMultiFormat vh = (ValueHandlerMultiFormat)valueHandler;
            vh.writeValue(parent, object, streamFormatVersion);
        } else
            valueHandler.writeValue(parent, object);
        if (mustChunk)
            end_block();
        writeEndTag(mustChunk);
    }
    public void write_value(Serializable object, String repository_id) {
        if (object == null) {
            write_long(0);
            return;
        }
        if (valueCache != null && valueCache.containsKey(object)) {
            writeIndirection(INDIRECTION_TAG, valueCache.getVal(object));
            return;
        }
        Class clazz = object.getClass();
        boolean oldMustChunk = mustChunk;
        if (mustChunk)
            mustChunk = true;
        if (inBlock)
            end_block();
        if (clazz.isArray()) {
            writeArray(object, clazz);
        } else if (object instanceof org.omg.CORBA.portable.ValueBase) {
            writeValueBase((org.omg.CORBA.portable.ValueBase)object, clazz);
        } else if (shouldWriteAsIDLEntity(object)) {
            writeIDLEntity((IDLEntity)object);
        } else if (object instanceof java.lang.String) {
            writeWStringValue((String)object);
        } else if (object instanceof java.lang.Class) {
            writeClass(repository_id, (Class)object);
        } else {
            writeRMIIIOPValueType(object, clazz);
        }
        mustChunk = oldMustChunk;
        if (mustChunk)
            start_block();
    }
    public void write_value(Serializable object)
    {
        write_value(object, (String)null);
    }
    public void write_value(Serializable object, org.omg.CORBA.portable.BoxedValueHelper factory)
    {
        if (object == null) {
            write_long(0);
            return;
        }
        if ((valueCache != null) && valueCache.containsKey(object)) {
            writeIndirection(INDIRECTION_TAG, valueCache.getVal(object));
            return;
        }
        boolean oldMustChunk = mustChunk;
        boolean isCustom = false;
        if (factory instanceof ValueHelper) {
            short modifier;
            try {
                modifier = ((ValueHelper)factory).get_type().type_modifier();
            } catch(BadKind ex) {  
                modifier = VM_NONE.value;
            }
            if (object instanceof CustomMarshal &&
                modifier == VM_CUSTOM.value) {
                isCustom = true;
                mustChunk = true;
            }
            if (modifier == VM_TRUNCATABLE.value)
                mustChunk = true;
        }
        if (mustChunk) {
            if (inBlock)
                end_block();
            int indirection = writeValueTag(true,
                                            orb.getORBData().useRepId(),
                                            Util.getCodebase(object.getClass())
                                           );
            if (orb.getORBData().useRepId()) {
                write_repositoryId(factory.get_id());
            }
            updateIndirectionTable(indirection, object, object);
            start_block();
            end_flag--;
            chunkedValueNestingLevel--;
            if (isCustom)
                ((CustomMarshal)object).marshal(parent);
            else
                factory.write_value(parent, object);
            end_block();
            writeEndTag(true);
        }
        else {
            int indirection = writeValueTag(false,
                                            orb.getORBData().useRepId(),
                                            Util.getCodebase(object.getClass())
                                           );
            if (orb.getORBData().useRepId()) {
                write_repositoryId(factory.get_id());
            }
            updateIndirectionTable(indirection, object, object);
            end_flag--;
            factory.write_value(parent, object);
            writeEndTag(false);
        }
        mustChunk = oldMustChunk;
        if (mustChunk)
            start_block();
    }
    public int get_offset() {
        return bbwi.position();
    }
    public void start_block() {
        if (debug) {
            dprint("CDROutputStream_1_0 start_block, position" + bbwi.position());
        }
        write_long(0);
        inBlock = true;
        blockSizePosition = get_offset();
        blockSizeIndex = bbwi.position();
        if (debug) {
            dprint("CDROutputStream_1_0 start_block, blockSizeIndex "
                   + blockSizeIndex);
        }
    }
    protected void writeLongWithoutAlign(int x) {
        if (littleEndian) {
            writeLittleEndianLong(x);
        } else {
            writeBigEndianLong(x);
        }
    }
    public void end_block() {
        if (debug) {
            dprint("CDROutputStream_1_0.java end_block");
        }
        if (!inBlock)
            return;
        if (debug) {
            dprint("CDROutputStream_1_0.java end_block, in a block");
        }
        inBlock = false;
        if (get_offset() == blockSizePosition) {
            bbwi.position(bbwi.position() - 4);
            blockSizeIndex = -1;
            blockSizePosition = -1;
            return;
        }
        int oldSize = bbwi.position();
        bbwi.position(blockSizeIndex - 4);
        writeLongWithoutAlign(oldSize - blockSizeIndex);
        bbwi.position(oldSize);
        blockSizeIndex = -1;
        blockSizePosition = -1;
    }
    public org.omg.CORBA.ORB orb()
    {
        return orb;
    }
    public final void write_boolean_array(boolean[]value, int offset, int length) {
        if ( value == null )
            throw wrapper.nullParam(CompletionStatus.COMPLETED_MAYBE);
        handleSpecialChunkBegin(length);
        for (int i = 0; i < length; i++)
            write_boolean(value[offset + i]);
        handleSpecialChunkEnd();
    }
    public final void write_char_array(char[]value, int offset, int length) {
        if ( value == null )
            throw wrapper.nullParam(CompletionStatus.COMPLETED_MAYBE);
        handleSpecialChunkBegin(length);
        for (int i = 0; i < length; i++)
            write_char(value[offset + i]);
        handleSpecialChunkEnd();
    }
    public void write_wchar_array(char[]value, int offset, int length) {
        if ( value == null )
            throw wrapper.nullParam(CompletionStatus.COMPLETED_MAYBE);
        handleSpecialChunkBegin(computeAlignment(2) + (length * 2));
        for (int i = 0; i < length; i++)
            write_wchar(value[offset + i]);
        handleSpecialChunkEnd();
    }
    public final void write_short_array(short[]value, int offset, int length) {
        if ( value == null )
            throw wrapper.nullParam(CompletionStatus.COMPLETED_MAYBE);
        handleSpecialChunkBegin(computeAlignment(2) + (length * 2));
        for (int i = 0; i < length; i++)
            write_short(value[offset + i]);
        handleSpecialChunkEnd();
    }
    public final void write_ushort_array(short[]value, int offset, int length) {
        write_short_array(value, offset, length);
    }
    public final void write_long_array(int[]value, int offset, int length) {
        if ( value == null )
            throw wrapper.nullParam(CompletionStatus.COMPLETED_MAYBE);
        handleSpecialChunkBegin(computeAlignment(4) + (length * 4));
        for (int i = 0; i < length; i++)
            write_long(value[offset + i]);
        handleSpecialChunkEnd();
    }
    public final void write_ulong_array(int[]value, int offset, int length) {
        write_long_array(value, offset, length);
    }
    public final void write_longlong_array(long[]value, int offset, int length) {
        if ( value == null )
            throw wrapper.nullParam(CompletionStatus.COMPLETED_MAYBE);
        handleSpecialChunkBegin(computeAlignment(8) + (length * 8));
        for (int i = 0; i < length; i++)
            write_longlong(value[offset + i]);
        handleSpecialChunkEnd();
    }
    public final void write_ulonglong_array(long[]value, int offset, int length) {
        write_longlong_array(value, offset, length);
    }
    public final void write_float_array(float[]value, int offset, int length) {
        if ( value == null )
            throw wrapper.nullParam(CompletionStatus.COMPLETED_MAYBE);
        handleSpecialChunkBegin(computeAlignment(4) + (length * 4));
        for (int i = 0; i < length; i++)
            write_float(value[offset + i]);
        handleSpecialChunkEnd();
    }
    public final void write_double_array(double[]value, int offset, int length) {
        if ( value == null )
            throw wrapper.nullParam(CompletionStatus.COMPLETED_MAYBE);
        handleSpecialChunkBegin(computeAlignment(8) + (length * 8));
        for (int i = 0; i < length; i++)
            write_double(value[offset + i]);
        handleSpecialChunkEnd();
    }
    public void write_string_array(String[] value, int offset, int length) {
        if ( value == null )
            throw wrapper.nullParam(CompletionStatus.COMPLETED_MAYBE);
        for(int i = 0; i < length; i++)
            write_string(value[offset + i]);
    }
    public void write_wstring_array(String[] value, int offset, int length) {
        if ( value == null )
            throw wrapper.nullParam(CompletionStatus.COMPLETED_MAYBE);
        for(int i = 0; i < length; i++)
            write_wstring(value[offset + i]);
    }
    public final void write_any_array(org.omg.CORBA.Any value[], int offset, int length)
    {
        for(int i = 0; i < length; i++)
            write_any(value[offset + i]);
    }
    public void writeTo(java.io.OutputStream s)
        throws java.io.IOException
    {
        byte[] tmpBuf = null;
        if (bbwi.byteBuffer.hasArray())
        {
            tmpBuf = bbwi.byteBuffer.array();
        }
        else
        {
            int size = bbwi.position();
            tmpBuf = new byte[size];
            for (int i = 0; i < size; i++)
                tmpBuf[i] = bbwi.byteBuffer.get(i);
        }
        s.write(tmpBuf, 0, bbwi.position());
    }
    public void writeOctetSequenceTo(org.omg.CORBA.portable.OutputStream s) {
        byte[] buf = null;
        if (bbwi.byteBuffer.hasArray())
        {
            buf = bbwi.byteBuffer.array();
        }
        else
        {
            int size = bbwi.position();
            buf = new byte[size];
            for (int i = 0; i < size; i++)
                buf[i] = bbwi.byteBuffer.get(i);
        }
        s.write_long(bbwi.position());
        s.write_octet_array(buf, 0, bbwi.position());
    }
    public final int getSize() {
        return bbwi.position();
    }
    public int getIndex() {
        return bbwi.position();
    }
    public boolean isLittleEndian() {
        return littleEndian;
    }
    public void setIndex(int value) {
        bbwi.position(value);
    }
    public ByteBufferWithInfo getByteBufferWithInfo() {
        return bbwi;
    }
    public void setByteBufferWithInfo(ByteBufferWithInfo bbwi) {
        this.bbwi = bbwi;
    }
    public ByteBuffer getByteBuffer() {
        ByteBuffer result = null;;
        if (bbwi != null) {
            result = bbwi.byteBuffer;
        }
        return result;
    }
    public void setByteBuffer(ByteBuffer byteBuffer) {
        bbwi.byteBuffer = byteBuffer;
    }
    private final void updateIndirectionTable(int indirection, java.lang.Object object,
                                              java.lang.Object key) {
        if (valueCache == null)
            valueCache = new CacheTable(orb,true);
        valueCache.put(object, indirection);
        if (key != object)
            valueCache.put(key, indirection);
    }
    private final void write_repositoryId(String id) {
        if (repositoryIdCache != null && repositoryIdCache.containsKey(id)) {
            writeIndirection(INDIRECTION_TAG, repositoryIdCache.getVal(id));
            return;
        }
        int indirection = writeString(id);
        if (repositoryIdCache == null)
        repositoryIdCache = new CacheTable(orb,true);
        repositoryIdCache.put(id, indirection);
    }
    private void write_codebase(String str, int pos) {
        if (codebaseCache != null && codebaseCache.containsKey(str)) {
            writeIndirection(INDIRECTION_TAG, codebaseCache.getVal(str));
        }
        else {
            write_string(str);
            if (codebaseCache == null)
                codebaseCache = new CacheTable(orb,true);
            codebaseCache.put(str, pos);
        }
    }
    private final int writeValueTag(boolean chunkIt, boolean useRepId,
                                    String codebase) {
        int indirection = 0;
        if (chunkIt && !useRepId){
            if (codebase == null) {
                write_long(repIdUtil.getStandardRMIChunkedNoRepStrId());
                indirection = get_offset() - 4;
            } else {
                write_long(repIdUtil.getCodeBaseRMIChunkedNoRepStrId());
                indirection = get_offset() - 4;
                write_codebase(codebase, get_offset());
            }
        } else if (chunkIt && useRepId){
            if (codebase == null) {
                write_long(repIdUtil.getStandardRMIChunkedId());
                indirection = get_offset() - 4;
            } else {
                write_long(repIdUtil.getCodeBaseRMIChunkedId());
                indirection = get_offset() - 4;
                write_codebase(codebase, get_offset());
            }
        } else if (!chunkIt && !useRepId) {
            if (codebase == null) {
                write_long(repIdUtil.getStandardRMIUnchunkedNoRepStrId());
                indirection = get_offset() - 4;
            } else {
                write_long(repIdUtil.getCodeBaseRMIUnchunkedNoRepStrId());
                indirection = get_offset() - 4;
                write_codebase(codebase, get_offset());
            }
        } else if (!chunkIt && useRepId) {
            if (codebase == null) {
                write_long(repIdUtil.getStandardRMIUnchunkedId());
                indirection = get_offset() - 4;
            } else {
                write_long(repIdUtil.getCodeBaseRMIUnchunkedId());
                indirection = get_offset() - 4;
                write_codebase(codebase, get_offset());
            }
        }
        return indirection;
    }
    private void writeIDLValue(Serializable object, String repID)
    {
        if (object instanceof StreamableValue) {
            ((StreamableValue)object)._write(parent);
        } else if (object instanceof CustomValue) {
            ((CustomValue)object).marshal(parent);
        } else {
            BoxedValueHelper helper = Utility.getHelper(object.getClass(), null, repID);
            boolean isCustom = false;
            if (helper instanceof ValueHelper && object instanceof CustomMarshal) {
                try {
                    if (((ValueHelper)helper).get_type().type_modifier() == VM_CUSTOM.value)
                        isCustom = true;
                } catch(BadKind ex) {
                    throw wrapper.badTypecodeForCustomValue( CompletionStatus.COMPLETED_MAYBE,
                        ex ) ;
                }
            }
            if (isCustom)
                ((CustomMarshal)object).marshal(parent);
            else
                helper.write_value(parent, object);
        }
    }
    private void writeEndTag(boolean chunked){
        if (chunked) {
            if (get_offset() == end_flag_position) {
                if (bbwi.position() == end_flag_index) {
                    bbwi.position(bbwi.position() - 4);
                } else {
                }
            }
            writeNestingLevel();
            end_flag_index = bbwi.position();
            end_flag_position = get_offset();
            chunkedValueNestingLevel++;
        }
        end_flag++;
    }
    private void writeNestingLevel() {
        if (orb == null ||
            ORBVersionFactory.getFOREIGN().equals(orb.getORBVersion()) ||
            ORBVersionFactory.getNEWER().compareTo(orb.getORBVersion()) <= 0) {
            write_long(chunkedValueNestingLevel);
        } else {
            write_long(end_flag);
        }
    }
    private void writeClass(String repository_id, Class clz) {
        if (repository_id == null)
            repository_id = repIdStrs.getClassDescValueRepId();
        int indirection = writeValueTag(mustChunk, true, null);
        updateIndirectionTable(indirection, clz, clz);
        write_repositoryId(repository_id);
        if (mustChunk) {
            start_block();
            end_flag--;
            chunkedValueNestingLevel--;
        } else
            end_flag--;
        writeClassBody(clz);
        if (mustChunk)
            end_block();
        writeEndTag(mustChunk);
    }
    private void writeClassBody(Class clz) {
        if (orb == null ||
            ORBVersionFactory.getFOREIGN().equals(orb.getORBVersion()) ||
            ORBVersionFactory.getNEWER().compareTo(orb.getORBVersion()) <= 0) {
            write_value(Util.getCodebase(clz));
            write_value(repIdStrs.createForAnyType(clz));
        } else {
            write_value(repIdStrs.createForAnyType(clz));
            write_value(Util.getCodebase(clz));
        }
    }
    private boolean shouldWriteAsIDLEntity(Serializable object)
    {
        return ((object instanceof IDLEntity) && (!(object instanceof ValueBase)) &&
                (!(object instanceof org.omg.CORBA.Object)));
    }
    private void writeIDLEntity(IDLEntity object) {
        mustChunk = true;
        String repository_id = repIdStrs.createForJavaType(object);
        Class clazz = object.getClass();
        String codebase = Util.getCodebase(clazz);
        int indirection = writeValueTag(true, true, codebase);
        updateIndirectionTable(indirection, object, object);
        write_repositoryId(repository_id);
        end_flag--;
        chunkedValueNestingLevel--;
        start_block();
        try {
            ClassLoader clazzLoader = (clazz == null ? null : clazz.getClassLoader());
            final Class helperClass = Utility.loadClassForClass(clazz.getName()+"Helper", codebase,
                                                   clazzLoader, clazz, clazzLoader);
            final Class argTypes[] = {org.omg.CORBA.portable.OutputStream.class, clazz};
            Method writeMethod = null;
            try {
                writeMethod = (Method)AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public java.lang.Object run() throws NoSuchMethodException {
                            return helperClass.getDeclaredMethod(kWriteMethod, argTypes);
                        }
                    }
                );
            } catch (PrivilegedActionException pae) {
                throw (NoSuchMethodException)pae.getException();
            }
            java.lang.Object args[] = {parent, object};
            writeMethod.invoke(null, args);
        } catch (ClassNotFoundException cnfe) {
            throw wrapper.errorInvokingHelperWrite( CompletionStatus.COMPLETED_MAYBE, cnfe ) ;
        } catch(NoSuchMethodException nsme) {
            throw wrapper.errorInvokingHelperWrite( CompletionStatus.COMPLETED_MAYBE, nsme ) ;
        } catch(IllegalAccessException iae) {
            throw wrapper.errorInvokingHelperWrite( CompletionStatus.COMPLETED_MAYBE, iae ) ;
        } catch(InvocationTargetException ite) {
            throw wrapper.errorInvokingHelperWrite( CompletionStatus.COMPLETED_MAYBE, ite ) ;
        }
        end_block();
        writeEndTag(true);
    }
    public void write_Abstract (java.lang.Object value) {
        write_abstract_interface(value);
    }
    public void write_Value (java.io.Serializable value) {
        write_value(value);
    }
    public void write_fixed(java.math.BigDecimal bigDecimal, short digits, short scale) {
        String string = bigDecimal.toString();
        String integerPart;
        String fractionPart;
        StringBuffer stringBuffer;
        if (string.charAt(0) == '-' || string.charAt(0) == '+') {
            string = string.substring(1);
        }
        int dotIndex = string.indexOf('.');
        if (dotIndex == -1) {
            integerPart = string;
            fractionPart = null;
        } else if (dotIndex == 0 ) {
            integerPart = null;
            fractionPart = string;
        } else {
            integerPart = string.substring(0, dotIndex);
            fractionPart = string.substring(dotIndex + 1);
        }
        stringBuffer = new StringBuffer(digits);
        if (fractionPart != null) {
            stringBuffer.append(fractionPart);
        }
        while (stringBuffer.length() < scale) {
            stringBuffer.append('0');
        }
        if (integerPart != null) {
            stringBuffer.insert(0, integerPart);
        }
        while (stringBuffer.length() < digits) {
            stringBuffer.insert(0, '0');
        }
        this.write_fixed(stringBuffer.toString(), bigDecimal.signum());
    }
    public void write_fixed(java.math.BigDecimal bigDecimal) {
        this.write_fixed(bigDecimal.toString(), bigDecimal.signum());
    }
    public void write_fixed(String string, int signum) {
        int stringLength = string.length();
        byte doubleDigit = 0;
        char ch;
        byte digit;
        int numDigits = 0;
        for (int i=0; i<stringLength; i++) {
            ch = string.charAt(i);
            if (ch == '-' || ch == '+' || ch == '.')
                continue;
            numDigits++;
        }
        for (int i=0; i<stringLength; i++) {
            ch = string.charAt(i);
            if (ch == '-' || ch == '+' || ch == '.')
                continue;
            digit = (byte)Character.digit(ch, 10);
            if (digit == -1) {
                throw wrapper.badDigitInFixed( CompletionStatus.COMPLETED_MAYBE ) ;
            }
            if (numDigits % 2 == 0) {
                doubleDigit |= digit;
                this.write_octet(doubleDigit);
                doubleDigit = 0;
            } else {
                doubleDigit |= (digit << 4);
            }
            numDigits--;
        }
        if (signum == -1) {
            doubleDigit |= 0xd;
        } else {
            doubleDigit |= 0xc;
        }
        this.write_octet(doubleDigit);
    }
    private final static String _id = "IDL:omg.org/CORBA/DataOutputStream:1.0";
    private final static String[] _ids = { _id };
    public String[] _truncatable_ids() {
        if (_ids == null)
            return null;
        return (String[])_ids.clone();
    }
    public void printBuffer() {
        CDROutputStream_1_0.printBuffer(this.bbwi);
    }
    public static void printBuffer(ByteBufferWithInfo bbwi) {
        System.out.println("+++++++ Output Buffer ++++++++");
        System.out.println();
        System.out.println("Current position: " + bbwi.position());
        System.out.println("Total length : " + bbwi.buflen);
        System.out.println();
        char[] charBuf = new char[16];
        try {
            for (int i = 0; i < bbwi.position(); i += 16) {
                int j = 0;
                while (j < 16 && j + i < bbwi.position()) {
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
                while (x < 16 && x + i < bbwi.position()) {
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
        System.out.println("++++++++++++++++++++++++++++++");
    }
    public void writeIndirection(int tag, int posIndirectedTo)
    {
        handleSpecialChunkBegin(computeAlignment(4) + 8);
        write_long(tag);
        write_long(posIndirectedTo - parent.getRealIndex(get_offset()));
        handleSpecialChunkEnd();
    }
    protected CodeSetConversion.CTBConverter getCharConverter() {
        if (charConverter == null)
            charConverter = parent.createCharCTBConverter();
        return charConverter;
    }
    protected CodeSetConversion.CTBConverter getWCharConverter() {
        if (wcharConverter == null)
            wcharConverter = parent.createWCharCTBConverter();
        return wcharConverter;
    }
    protected void dprint(String msg) {
        if (debug)
            ORBUtility.dprint(this, msg);
    }
    void alignOnBoundary(int octetBoundary) {
        alignAndReserve(octetBoundary, 0);
    }
    public void start_value(String rep_id) {
        if (debug) {
            dprint("start_value w/ rep id "
                   + rep_id
                   + " called at pos "
                   + get_offset()
                   + " position "
                   + bbwi.position());
        }
        if (inBlock)
            end_block();
        writeValueTag(true, true, null);
        write_repositoryId(rep_id);
        end_flag--;
        chunkedValueNestingLevel--;
        start_block();
    }
    public void end_value() {
        if (debug) {
            dprint("end_value called at pos "
                   + get_offset()
                   + " position "
                   + bbwi.position());
        }
        end_block();
        writeEndTag(true);
        if (debug) {
            dprint("mustChunk is " + mustChunk);
        }
        if (mustChunk) {
            start_block();
        }
    }
    public void close() throws IOException
    {
        getBufferManager().close();
        if (getByteBufferWithInfo() != null && getByteBuffer() != null)
        {
            int bbHash = System.identityHashCode(bbwi.byteBuffer);
            MessageMediator messageMediator = parent.getMessageMediator();
            if (messageMediator != null)
            {
                CDRInputObject inputObj =
                               (CDRInputObject)messageMediator.getInputObject();
                if (inputObj != null)
                {
                    ByteBuffer inputBb = inputObj.getByteBuffer();
                    int iBbHash = 0;
                    if (inputBb != null)
                    {
                        iBbHash = System.identityHashCode(inputBb);
                        if (bbHash == iBbHash)  
                        {
                            inputObj.setByteBuffer(null);
                            inputObj.setByteBufferWithInfo(null);
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
             byteBufferPool.releaseByteBuffer(getByteBuffer());
             bbwi.byteBuffer = null;
             bbwi = null;
        }
    }
}
