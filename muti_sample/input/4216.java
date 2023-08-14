public class IDLJavaSerializationInputStream extends CDRInputStreamBase {
    private ORB orb;
    private int bufSize;
    private ByteBuffer buffer;
    private byte encodingVersion;
    private ObjectInputStream is;
    private _ByteArrayInputStream bis;
    private BufferManagerRead bufferManager;
    private final int directReadLength = Message.GIOPMessageHeaderLength + 4;
    private boolean markOn;
    private int peekIndex, peekCount;
    private LinkedList markedItemQ = new LinkedList();
    protected ORBUtilSystemException wrapper;
    class _ByteArrayInputStream extends ByteArrayInputStream {
        _ByteArrayInputStream(byte[] buf) {
            super(buf);
        }
        int getPosition() {
            return this.pos;
        }
        void setPosition(int value) {
            if (value < 0 || value > count) {
                throw new IndexOutOfBoundsException();
            }
            this.pos = value;
        }
    }
    class MarshalObjectInputStream extends ObjectInputStream {
        ORB orb;
        MarshalObjectInputStream(java.io.InputStream out, ORB orb)
                throws IOException {
            super(out);
            this.orb = orb;
            java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction() {
                    public Object run() {
                        enableResolveObject(true);
                        return null;
                    }
                }
            );
        }
        protected final Object resolveObject(Object obj) throws IOException {
            try {
                if (StubAdapter.isStub(obj)) {
                    StubAdapter.connect(obj, orb);
                }
            } catch (java.rmi.RemoteException re) {
                IOException ie = new IOException("resolveObject failed");
                ie.initCause(re);
                throw ie;
            }
            return obj;
        }
    }
    public IDLJavaSerializationInputStream(byte encodingVersion) {
        super();
        this.encodingVersion = encodingVersion;
    }
    public void init(org.omg.CORBA.ORB orb,
                     ByteBuffer byteBuffer,
                     int bufSize,
                     boolean littleEndian,
                     BufferManagerRead bufferManager) {
        this.orb = (ORB) orb;
        this.bufSize = bufSize;
        this.bufferManager = bufferManager;
        buffer = byteBuffer;
        wrapper =
            ORBUtilSystemException.get((ORB)orb, CORBALogDomains.RPC_ENCODING);
        byte[] buf;
        if (buffer.hasArray()) {
            buf = buffer.array();
        } else {
            buf = new byte[bufSize];
            buffer.get(buf);
        }
        bis = new _ByteArrayInputStream(buf);
    }
    private void initObjectInputStream() {
        if (is != null) {
            throw wrapper.javaStreamInitFailed();
        }
        try {
            is = new MarshalObjectInputStream(bis, orb);
        } catch (Exception e) {
            throw wrapper.javaStreamInitFailed(e);
        }
    }
    public boolean read_boolean() {
        if (!markOn && !(markedItemQ.isEmpty())) { 
            return ((Boolean)markedItemQ.removeFirst()).booleanValue();
        }
        if (markOn && !(markedItemQ.isEmpty()) &&
                (peekIndex < peekCount)) { 
            return ((Boolean)markedItemQ.get(peekIndex++)).booleanValue();
        }
        try {
            boolean value = is.readBoolean();
            if (markOn) { 
                markedItemQ.addLast(Boolean.valueOf(value));
            }
            return value;
        } catch (Exception e) {
            throw wrapper.javaSerializationException(e, "read_boolean");
        }
    }
    public char read_char() {
        if (!markOn && !(markedItemQ.isEmpty())) { 
            return ((Character)markedItemQ.removeFirst()).charValue();
        }
        if (markOn && !(markedItemQ.isEmpty()) &&
                (peekIndex < peekCount)) { 
            return ((Character)markedItemQ.get(peekIndex++)).charValue();
        }
        try {
            char value = is.readChar();
            if (markOn) { 
                markedItemQ.addLast(new Character(value));
            }
            return value;
        } catch (Exception e) {
            throw wrapper.javaSerializationException(e, "read_char");
        }
    }
    public char read_wchar() {
        return this.read_char();
    }
    public byte read_octet() {
        if (bis.getPosition() < directReadLength) {
            byte b = (byte) bis.read();
            if (bis.getPosition() == directReadLength) {
                initObjectInputStream();
            }
            return b;
        }
        if (!markOn && !(markedItemQ.isEmpty())) { 
            return ((Byte)markedItemQ.removeFirst()).byteValue();
        }
        if (markOn && !(markedItemQ.isEmpty()) &&
                (peekIndex < peekCount)) { 
            return ((Byte)markedItemQ.get(peekIndex++)).byteValue();
        }
        try {
            byte value = is.readByte();
            if (markOn) { 
                markedItemQ.addLast(new Byte(value));
            }
            return value;
        } catch (Exception e) {
            throw wrapper.javaSerializationException(e, "read_octet");
        }
    }
    public short read_short() {
        if (!markOn && !(markedItemQ.isEmpty())) { 
            return ((Short)markedItemQ.removeFirst()).shortValue();
        }
        if (markOn && !(markedItemQ.isEmpty()) &&
                (peekIndex < peekCount)) { 
            return ((Short)markedItemQ.get(peekIndex++)).shortValue();
        }
        try {
            short value = is.readShort();
            if (markOn) { 
                markedItemQ.addLast(new Short(value));
            }
            return value;
        } catch (Exception e) {
            throw wrapper.javaSerializationException(e, "read_short");
        }
    }
    public short read_ushort() {
        return this.read_short();
    }
    public int read_long() {
        if (bis.getPosition() < directReadLength) {
            int b1 = (bis.read() << 24) & 0xFF000000;
            int b2 = (bis.read() << 16) & 0x00FF0000;
            int b3 = (bis.read() << 8)  & 0x0000FF00;
            int b4 = (bis.read() << 0)  & 0x000000FF;
            if (bis.getPosition() == directReadLength) {
                initObjectInputStream();
            } else if (bis.getPosition() > directReadLength) {
                wrapper.javaSerializationException("read_long");
            }
            return (b1 | b2 | b3 | b4);
        }
        if (!markOn && !(markedItemQ.isEmpty())) { 
            return ((Integer)markedItemQ.removeFirst()).intValue();
        }
        if (markOn && !(markedItemQ.isEmpty()) &&
                (peekIndex < peekCount)) { 
            return ((Integer)markedItemQ.get(peekIndex++)).intValue();
        }
        try {
            int value = is.readInt();
            if (markOn) { 
                markedItemQ.addLast(new Integer(value));
            }
            return value;
        } catch (Exception e) {
            throw wrapper.javaSerializationException(e, "read_long");
        }
    }
    public int read_ulong() {
        return this.read_long();
    }
    public long read_longlong() {
        if (!markOn && !(markedItemQ.isEmpty())) { 
            return ((Long)markedItemQ.removeFirst()).longValue();
        }
        if (markOn && !(markedItemQ.isEmpty()) &&
                (peekIndex < peekCount)) { 
            return ((Long)markedItemQ.get(peekIndex++)).longValue();
        }
        try {
            long value = is.readLong();
            if (markOn) { 
                markedItemQ.addLast(new Long(value));
            }
            return value;
        } catch (Exception e) {
            throw wrapper.javaSerializationException(e, "read_longlong");
        }
    }
    public long read_ulonglong() {
        return read_longlong();
    }
    public float read_float() {
        if (!markOn && !(markedItemQ.isEmpty())) { 
            return ((Float)markedItemQ.removeFirst()).floatValue();
        }
        if (markOn && !(markedItemQ.isEmpty()) &&
                (peekIndex < peekCount)) { 
            return ((Float)markedItemQ.get(peekIndex++)).floatValue();
        }
        try {
            float value = is.readFloat();
            if (markOn) { 
                markedItemQ.addLast(new Float(value));
            }
            return value;
        } catch (Exception e) {
            throw wrapper.javaSerializationException(e, "read_float");
        }
    }
    public double read_double() {
        if (!markOn && !(markedItemQ.isEmpty())) { 
            return ((Double)markedItemQ.removeFirst()).doubleValue();
        }
        if (markOn && !(markedItemQ.isEmpty()) &&
                (peekIndex < peekCount)) { 
            return ((Double)markedItemQ.get(peekIndex++)).doubleValue();
        }
        try {
            double value = is.readDouble();
            if (markOn) { 
                markedItemQ.addLast(new Double(value));
            }
            return value;
        } catch (Exception e) {
            throw wrapper.javaSerializationException(e, "read_double");
        }
    }
    public String read_string() {
        if (!markOn && !(markedItemQ.isEmpty())) { 
            return (String) markedItemQ.removeFirst();
        }
        if (markOn && !(markedItemQ.isEmpty()) &&
            (peekIndex < peekCount)) { 
            return (String) markedItemQ.get(peekIndex++);
        }
        try {
            String value = is.readUTF();
            if (markOn) { 
                markedItemQ.addLast(value);
            }
            return value;
        } catch (Exception e) {
            throw wrapper.javaSerializationException(e, "read_string");
        }
    }
    public String read_wstring() {
        if (!markOn && !(markedItemQ.isEmpty())) { 
            return (String) markedItemQ.removeFirst();
        }
        if (markOn && !(markedItemQ.isEmpty()) &&
                (peekIndex < peekCount)) { 
            return (String) markedItemQ.get(peekIndex++);
        }
        try {
            String value = (String) is.readObject();
            if (markOn) { 
                markedItemQ.addLast(value);
            }
            return value;
        } catch (Exception e) {
            throw wrapper.javaSerializationException(e, "read_wstring");
        }
    }
    public void read_boolean_array(boolean[] value, int offset, int length){
        for(int i = 0; i < length; i++) {
            value[i+offset] = read_boolean();
        }
    }
    public void read_char_array(char[] value, int offset, int length) {
        for(int i=0; i < length; i++) {
            value[i+offset] = read_char();
        }
    }
    public void read_wchar_array(char[] value, int offset, int length) {
        read_char_array(value, offset, length);
    }
    public void read_octet_array(byte[] value, int offset, int length) {
        for(int i=0; i < length; i++) {
            value[i+offset] = read_octet();
        }
    }
    public void read_short_array(short[] value, int offset, int length) {
        for(int i=0; i < length; i++) {
            value[i+offset] = read_short();
        }
    }
    public void read_ushort_array(short[] value, int offset, int length) {
        read_short_array(value, offset, length);
    }
    public void read_long_array(int[] value, int offset, int length) {
        for(int i=0; i < length; i++) {
            value[i+offset] = read_long();
        }
    }
    public void read_ulong_array(int[] value, int offset, int length) {
        read_long_array(value, offset, length);
    }
    public void read_longlong_array(long[] value, int offset, int length) {
        for(int i=0; i < length; i++) {
            value[i+offset] = read_longlong();
        }
    }
    public void read_ulonglong_array(long[] value, int offset, int length) {
        read_longlong_array(value, offset, length);
    }
    public void read_float_array(float[] value, int offset, int length) {
        for(int i=0; i < length; i++) {
            value[i+offset] = read_float();
        }
    }
    public void read_double_array(double[] value, int offset, int length) {
        for(int i=0; i < length; i++) {
            value[i+offset] = read_double();
        }
    }
    public org.omg.CORBA.Object read_Object() {
        return read_Object(null);
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
        } catch (org.omg.CORBA.MARSHAL ex) {
            if (tc.kind().value() != org.omg.CORBA.TCKind._tk_value) {
                throw ex;
            }
            ex.printStackTrace();
        }
        any.read_value(parent, tc);
        return any;
    }
    public Principal read_Principal() {
        int len = read_long();
        byte[] pvalue = new byte[len];
        read_octet_array(pvalue,0,len);
        Principal p = new com.sun.corba.se.impl.corba.PrincipalImpl();
        p.name(pvalue);
        return p;
    }
    public BigDecimal read_fixed() {
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
            doubleDigit = read_octet();
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
    public org.omg.CORBA.Object read_Object(java.lang.Class clz) {
        IOR ior = IORFactories.makeIOR(parent) ;
        if (ior.isNil()) {
            return null;
        }
        PresentationManager.StubFactoryFactory sff =
            ORB.getStubFactoryFactory();
        String codeBase = ior.getProfile().getCodebase();
        PresentationManager.StubFactory stubFactory = null;
        if (clz == null) {
            RepositoryId rid = RepositoryId.cache.getId(ior.getTypeId() );
            String className = rid.getClassName();
            boolean isIDLInterface = rid.isIDLType();
            if (className == null || className.equals( "" )) {
                stubFactory = null;
            } else {
                try {
                    stubFactory = sff.createStubFactory(className,
                        isIDLInterface, codeBase, (Class) null,
                        (ClassLoader) null);
                } catch (Exception exc) {
                    stubFactory = null ;
                }
            }
        } else if (StubAdapter.isStubClass(clz)) {
            stubFactory = PresentationDefaults.makeStaticStubFactory(clz);
        } else {
            boolean isIDL = IDLEntity.class.isAssignableFrom(clz);
            stubFactory = sff.createStubFactory(
                 clz.getName(), isIDL, codeBase, clz, clz.getClassLoader());
        }
        return CDRInputStream_1_0.internalIORToObject(ior, stubFactory, orb);
    }
    public org.omg.CORBA.ORB orb() {
        return this.orb;
    }
    public java.io.Serializable read_value() {
        if (!markOn && !(markedItemQ.isEmpty())) { 
            return (Serializable) markedItemQ.removeFirst();
        }
        if (markOn && !(markedItemQ.isEmpty()) &&
                (peekIndex < peekCount)) { 
            return (Serializable) markedItemQ.get(peekIndex++);
        }
        try {
            Serializable value = (java.io.Serializable) is.readObject();
            if (markOn) { 
                markedItemQ.addLast(value);
            }
            return value;
        } catch (Exception e) {
            throw wrapper.javaSerializationException(e, "read_value");
        }
    }
    public java.io.Serializable read_value(java.lang.Class clz) {
        return read_value();
    }
    public java.io.Serializable read_value(
            org.omg.CORBA.portable.BoxedValueHelper factory) {
        return read_value();
    }
    public java.io.Serializable read_value(java.lang.String rep_id) {
        return read_value();
    }
    public java.io.Serializable read_value(java.io.Serializable value) {
        return read_value();
    }
    public java.lang.Object read_abstract_interface() {
        return read_abstract_interface(null);
    }
    public java.lang.Object read_abstract_interface(java.lang.Class clz) {
        boolean isObject = read_boolean();
        if (isObject) {
            return read_Object(clz);
        } else {
            return read_value();
        }
    }
    public void consumeEndian() {
        throw wrapper.giopVersionError();
    }
    public int getPosition() {
        try {
            return bis.getPosition();
        } catch (Exception e) {
            throw wrapper.javaSerializationException(e, "getPosition");
        }
    }
    public java.lang.Object read_Abstract() {
        return read_abstract_interface();
    }
    public java.io.Serializable read_Value() {
        return read_value();
    }
    public void read_any_array (org.omg.CORBA.AnySeqHolder seq,
                                int offset, int length) {
        read_any_array(seq.value, offset, length);
    }
    private final void read_any_array(org.omg.CORBA.Any[] value,
                                     int offset, int length) {
        for(int i=0; i < length; i++) {
            value[i+offset] = read_any();
        }
    }
    public void read_boolean_array (org.omg.CORBA.BooleanSeqHolder seq,
                                    int offset, int length){
        read_boolean_array(seq.value, offset, length);
    }
    public void read_char_array (org.omg.CORBA.CharSeqHolder seq,
                                 int offset, int length){
        read_char_array(seq.value, offset, length);
    }
    public void read_wchar_array (org.omg.CORBA.WCharSeqHolder seq,
                                  int offset, int length){
        read_wchar_array(seq.value, offset, length);
    }
    public void read_octet_array (org.omg.CORBA.OctetSeqHolder seq,
                                  int offset, int length){
        read_octet_array(seq.value, offset, length);
    }
    public void read_short_array (org.omg.CORBA.ShortSeqHolder seq,
                                  int offset, int length){
        read_short_array(seq.value, offset, length);
    }
    public void read_ushort_array (org.omg.CORBA.UShortSeqHolder seq,
                                   int offset, int length){
        read_ushort_array(seq.value, offset, length);
    }
    public void read_long_array (org.omg.CORBA.LongSeqHolder seq,
                                 int offset, int length){
        read_long_array(seq.value, offset, length);
    }
    public void read_ulong_array (org.omg.CORBA.ULongSeqHolder seq,
                                  int offset, int length){
        read_ulong_array(seq.value, offset, length);
    }
    public void read_ulonglong_array (org.omg.CORBA.ULongLongSeqHolder seq,
                                      int offset, int length){
        read_ulonglong_array(seq.value, offset, length);
    }
    public void read_longlong_array (org.omg.CORBA.LongLongSeqHolder seq,
                                     int offset, int length){
        read_longlong_array(seq.value, offset, length);
    }
    public void read_float_array (org.omg.CORBA.FloatSeqHolder seq,
                                  int offset, int length){
        read_float_array(seq.value, offset, length);
    }
    public void read_double_array (org.omg.CORBA.DoubleSeqHolder seq,
                                   int offset, int length){
        read_double_array(seq.value, offset, length);
    }
    public String[] _truncatable_ids() {
        throw wrapper.giopVersionError();
    }
    public void mark(int readLimit) {
        if (markOn || is == null) {
            throw wrapper.javaSerializationException("mark");
        }
        markOn = true;
        if (!(markedItemQ.isEmpty())) {
            peekIndex = 0;
            peekCount = markedItemQ.size();
        }
    }
    public void reset() {
        markOn = false;
        peekIndex = 0;
        peekCount = 0;
    }
    public boolean markSupported() {
        return true;
    }
    public CDRInputStreamBase dup() {
        CDRInputStreamBase result = null ;
        try {
            result = (CDRInputStreamBase) this.getClass().newInstance();
        } catch (Exception e) {
            throw wrapper.couldNotDuplicateCdrInputStream(e);
        }
        result.init(this.orb, this.buffer, this.bufSize, false, null);
        ((IDLJavaSerializationInputStream)result).skipBytes(getPosition());
        ((IDLJavaSerializationInputStream)result).
            setMarkData(markOn, peekIndex, peekCount,
                        (LinkedList) markedItemQ.clone());
        return result;
    }
    void skipBytes(int len) {
        try {
            is.skipBytes(len);
        } catch (Exception e) {
            throw wrapper.javaSerializationException(e, "skipBytes");
        }
    }
    void setMarkData(boolean markOn, int peekIndex, int peekCount,
                     LinkedList markedItemQ) {
        this.markOn = markOn;
        this.peekIndex = peekIndex;
        this.peekCount = peekCount;
        this.markedItemQ = markedItemQ;
    }
    public java.math.BigDecimal read_fixed(short digits, short scale) {
        StringBuffer buffer = read_fixed_buffer();
        if (digits != buffer.length())
            throw wrapper.badFixed( new Integer(digits),
                new Integer(buffer.length()) ) ;
        buffer.insert(digits - scale, '.');
        return new BigDecimal(buffer.toString());
    }
    public boolean isLittleEndian() {
        throw wrapper.giopVersionError();
    }
    void setHeaderPadding(boolean headerPadding) {
    }
    public ByteBuffer getByteBuffer() {
        throw wrapper.giopVersionError();
    }
    public void setByteBuffer(ByteBuffer byteBuffer) {
        throw wrapper.giopVersionError();
    }
    public void setByteBufferWithInfo(ByteBufferWithInfo bbwi) {
        throw wrapper.giopVersionError();
    }
    public int getBufferLength() {
        return bufSize;
    }
    public void setBufferLength(int value) {
    }
    public int getIndex() {
        return bis.getPosition();
    }
    public void setIndex(int value) {
        try {
            bis.setPosition(value);
        } catch (IndexOutOfBoundsException e) {
            throw wrapper.javaSerializationException(e, "setIndex");
        }
    }
    public void orb(org.omg.CORBA.ORB orb) {
        this.orb = (ORB) orb;
    }
    public BufferManagerRead getBufferManager() {
        return bufferManager;
    }
    public GIOPVersion getGIOPVersion() {
        return GIOPVersion.V1_2;
    }
    com.sun.org.omg.SendingContext.CodeBase getCodeBase() {
        return parent.getCodeBase();
    }
    void printBuffer() {
        byte[] buf = this.buffer.array();
        System.out.println("+++++++ Input Buffer ++++++++");
        System.out.println();
        System.out.println("Current position: " + getPosition());
        System.out.println("Total length : " + this.bufSize);
        System.out.println();
        char[] charBuf = new char[16];
        try {
            for (int i = 0; i < buf.length; i += 16) {
                int j = 0;
                while (j < 16 && j + i < buf.length) {
                    int k = buf[i + j];
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
                while (x < 16 && x + i < buf.length) {
                    if (ORBUtility.isPrintable((char)buf[i + x])) {
                        charBuf[x] = (char) buf[i + x];
                    } else {
                        charBuf[x] = '.';
                    }
                    x++;
                }
                System.out.println(new String(charBuf, 0, x));
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        System.out.println("++++++++++++++++++++++++++++++");
    }
    void alignOnBoundary(int octetBoundary) {
        throw wrapper.giopVersionError();
    }
    void performORBVersionSpecificInit() {
    }
    public void resetCodeSetConverters() {
    }
    public void start_value() {
        throw wrapper.giopVersionError();
    }
    public void end_value() {
        throw wrapper.giopVersionError();
    }
}
