public class AnyImpl extends Any
{
    private static final class AnyInputStream extends EncapsInputStream
    {
        public AnyInputStream(EncapsInputStream theStream )
        {
            super( theStream );
        }
    }
    private static final class AnyOutputStream extends EncapsOutputStream
    {
        public AnyOutputStream(ORB orb)
        {
            super((ORB)orb);
        }
        public org.omg.CORBA.portable.InputStream create_input_stream()
        {
            return new AnyInputStream(
                (com.sun.corba.se.impl.encoding.EncapsInputStream)
                    super.create_input_stream());
        }
    }
    private TypeCodeImpl typeCode;
    protected ORB orb;
    private ORBUtilSystemException wrapper ;
    private CDRInputStream stream;
    private long value;
    private java.lang.Object object;
    private boolean isInitialized = false;
    private static final int DEFAULT_BUFFER_SIZE = 32;
    static boolean isStreamed[] = {
        false,  
        false,  
        false,  
        false,  
        false,  
        false,  
        false,  
        false,  
        false,  
        false,  
        false,  
        false,  
        false,  
        true,   
        false,  
        true,   
        true,   
        false,  
        false,  
        true,   
        true,   
        true,   
        true,   
        false,  
        false,  
        false,  
        false,  
        false,  
        false,  
        false,  
        false,  
        false,  
        false   
    };
    static AnyImpl convertToNative(ORB orb, Any any) {
        if (any instanceof AnyImpl) {
            return (AnyImpl)any;
        } else {
            AnyImpl anyImpl = new AnyImpl(orb, any);
            anyImpl.typeCode = TypeCodeImpl.convertToNative(orb, anyImpl.typeCode);
            return anyImpl;
        }
    }
    public AnyImpl(ORB orb)
    {
        this.orb = orb;
        wrapper = ORBUtilSystemException.get( (com.sun.corba.se.spi.orb.ORB)orb,
            CORBALogDomains.RPC_PRESENTATION ) ;
        typeCode = orb.get_primitive_tc(TCKind._tk_null);
        stream = null;
        object = null;
        value = 0;
        isInitialized = true;
    }
    public AnyImpl(ORB orb, Any obj) {
        this(orb);
        if ((obj instanceof AnyImpl)) {
            AnyImpl objImpl = (AnyImpl)obj;
            typeCode = objImpl.typeCode;
            value = objImpl.value;
            object = objImpl.object;
            isInitialized = objImpl.isInitialized;
            if (objImpl.stream != null)
                stream = objImpl.stream.dup();
        } else {
            read_value(obj.create_input_stream(), obj.type());
        }
    }
    public TypeCode type() {
        return typeCode;
    }
    private TypeCode realType() {
        return realType(typeCode);
    }
    private TypeCode realType(TypeCode aType) {
        TypeCode realType = aType;
        try {
            while (realType.kind().value() == TCKind._tk_alias) {
                realType = realType.content_type();
            }
        } catch (BadKind bad) { 
            throw wrapper.badkindCannotOccur( bad ) ;
        }
        return realType;
    }
    public void type(TypeCode tc)
    {
        typeCode = TypeCodeImpl.convertToNative(orb, tc);
        stream = null;
        value = 0;
        object = null;
        isInitialized = (tc.kind().value() == TCKind._tk_null);
    }
    public boolean equal(Any otherAny)
    {
        if (otherAny == this)
            return true;
        if (!typeCode.equal(otherAny.type()))
            return false;
        TypeCode realType = realType();
        switch (realType.kind().value()) {
            case TCKind._tk_null:
            case TCKind._tk_void:
                return true;
            case TCKind._tk_short:
                return (extract_short() == otherAny.extract_short());
            case TCKind._tk_long:
                return (extract_long() == otherAny.extract_long());
            case TCKind._tk_ushort:
                return (extract_ushort() == otherAny.extract_ushort());
            case TCKind._tk_ulong:
                return (extract_ulong() == otherAny.extract_ulong());
            case TCKind._tk_float:
                return (extract_float() == otherAny.extract_float());
            case TCKind._tk_double:
                return (extract_double() == otherAny.extract_double());
            case TCKind._tk_boolean:
                return (extract_boolean() == otherAny.extract_boolean());
            case TCKind._tk_char:
                return (extract_char() == otherAny.extract_char());
            case TCKind._tk_wchar:
                return (extract_wchar() == otherAny.extract_wchar());
            case TCKind._tk_octet:
                return (extract_octet() == otherAny.extract_octet());
            case TCKind._tk_any:
                return extract_any().equal(otherAny.extract_any());
            case TCKind._tk_TypeCode:
                return extract_TypeCode().equal(otherAny.extract_TypeCode());
            case TCKind._tk_string:
                return extract_string().equals(otherAny.extract_string());
            case TCKind._tk_wstring:
                return (extract_wstring().equals(otherAny.extract_wstring()));
            case TCKind._tk_longlong:
                return (extract_longlong() == otherAny.extract_longlong());
            case TCKind._tk_ulonglong:
                return (extract_ulonglong() == otherAny.extract_ulonglong());
            case TCKind._tk_objref:
                return (extract_Object().equals(otherAny.extract_Object()));
            case TCKind._tk_Principal:
                return (extract_Principal().equals(otherAny.extract_Principal()));
            case TCKind._tk_enum:
                return (extract_long() == otherAny.extract_long());
            case TCKind._tk_fixed:
                return (extract_fixed().compareTo(otherAny.extract_fixed()) == 0);
            case TCKind._tk_except:
            case TCKind._tk_struct:
            case TCKind._tk_union:
            case TCKind._tk_sequence:
            case TCKind._tk_array:
                InputStream copyOfMyStream = this.create_input_stream();
                InputStream copyOfOtherStream = otherAny.create_input_stream();
                return equalMember(realType, copyOfMyStream, copyOfOtherStream);
            case TCKind._tk_value:
            case TCKind._tk_value_box:
                return extract_Value().equals(otherAny.extract_Value());
            case TCKind._tk_alias:
                throw wrapper.errorResolvingAlias() ;
            case TCKind._tk_longdouble:
                throw wrapper.tkLongDoubleNotSupported() ;
            default:
                throw wrapper.typecodeNotSupported() ;
        }
    }
    private boolean equalMember(TypeCode memberType, InputStream myStream, InputStream otherStream) {
        TypeCode realType = realType(memberType);
        try {
            switch (realType.kind().value()) {
                case TCKind._tk_null:
                case TCKind._tk_void:
                    return true;
                case TCKind._tk_short:
                    return (myStream.read_short() == otherStream.read_short());
                case TCKind._tk_long:
                    return (myStream.read_long() == otherStream.read_long());
                case TCKind._tk_ushort:
                    return (myStream.read_ushort() == otherStream.read_ushort());
                case TCKind._tk_ulong:
                    return (myStream.read_ulong() == otherStream.read_ulong());
                case TCKind._tk_float:
                    return (myStream.read_float() == otherStream.read_float());
                case TCKind._tk_double:
                    return (myStream.read_double() == otherStream.read_double());
                case TCKind._tk_boolean:
                    return (myStream.read_boolean() == otherStream.read_boolean());
                case TCKind._tk_char:
                    return (myStream.read_char() == otherStream.read_char());
                case TCKind._tk_wchar:
                    return (myStream.read_wchar() == otherStream.read_wchar());
                case TCKind._tk_octet:
                    return (myStream.read_octet() == otherStream.read_octet());
                case TCKind._tk_any:
                    return myStream.read_any().equal(otherStream.read_any());
                case TCKind._tk_TypeCode:
                    return myStream.read_TypeCode().equal(otherStream.read_TypeCode());
                case TCKind._tk_string:
                    return myStream.read_string().equals(otherStream.read_string());
                case TCKind._tk_wstring:
                    return (myStream.read_wstring().equals(otherStream.read_wstring()));
                case TCKind._tk_longlong:
                    return (myStream.read_longlong() == otherStream.read_longlong());
                case TCKind._tk_ulonglong:
                    return (myStream.read_ulonglong() == otherStream.read_ulonglong());
                case TCKind._tk_objref:
                    return (myStream.read_Object().equals(otherStream.read_Object()));
                case TCKind._tk_Principal:
                    return (myStream.read_Principal().equals(otherStream.read_Principal()));
                case TCKind._tk_enum:
                    return (myStream.read_long() == otherStream.read_long());
                case TCKind._tk_fixed:
                    return (myStream.read_fixed().compareTo(otherStream.read_fixed()) == 0);
                case TCKind._tk_except:
                case TCKind._tk_struct: {
                    int length = realType.member_count();
                    for (int i=0; i<length; i++) {
                        if ( ! equalMember(realType.member_type(i), myStream, otherStream)) {
                            return false;
                        }
                    }
                    return true;
                }
                case TCKind._tk_union: {
                    Any myDiscriminator = orb.create_any();
                    Any otherDiscriminator = orb.create_any();
                    myDiscriminator.read_value(myStream, realType.discriminator_type());
                    otherDiscriminator.read_value(otherStream, realType.discriminator_type());
                    if ( ! myDiscriminator.equal(otherDiscriminator)) {
                        return false;
                    }
                    TypeCodeImpl realTypeCodeImpl = TypeCodeImpl.convertToNative(orb, realType);
                    int memberIndex = realTypeCodeImpl.currentUnionMemberIndex(myDiscriminator);
                    if (memberIndex == -1)
                        throw wrapper.unionDiscriminatorError() ;
                    if ( ! equalMember(realType.member_type(memberIndex), myStream, otherStream)) {
                        return false;
                    }
                    return true;
                }
                case TCKind._tk_sequence: {
                    int length = myStream.read_long();
                    otherStream.read_long(); 
                    for (int i=0; i<length; i++) {
                        if ( ! equalMember(realType.content_type(), myStream, otherStream)) {
                            return false;
                        }
                    }
                    return true;
                }
                case TCKind._tk_array: {
                    int length = realType.member_count();
                    for (int i=0; i<length; i++) {
                        if ( ! equalMember(realType.content_type(), myStream, otherStream)) {
                            return false;
                        }
                    }
                    return true;
                }
                case TCKind._tk_value:
                case TCKind._tk_value_box:
                    org.omg.CORBA_2_3.portable.InputStream mine =
                        (org.omg.CORBA_2_3.portable.InputStream)myStream;
                    org.omg.CORBA_2_3.portable.InputStream other =
                        (org.omg.CORBA_2_3.portable.InputStream)otherStream;
                    return mine.read_value().equals(other.read_value());
                case TCKind._tk_alias:
                    throw wrapper.errorResolvingAlias() ;
                case TCKind._tk_longdouble:
                    throw wrapper.tkLongDoubleNotSupported() ;
                default:
                    throw wrapper.typecodeNotSupported() ;
            }
        } catch (BadKind badKind) { 
            throw wrapper.badkindCannotOccur() ;
        } catch (Bounds bounds) { 
            throw wrapper.boundsCannotOccur() ;
        }
    }
    public org.omg.CORBA.portable.OutputStream create_output_stream()
    {
        return new AnyOutputStream(orb);
    }
    public org.omg.CORBA.portable.InputStream create_input_stream()
    {
        if (AnyImpl.isStreamed[realType().kind().value()]) {
            return stream.dup();
        } else {
            OutputStream os = (OutputStream)orb.create_output_stream();
            TCUtility.marshalIn(os, realType(), value, object);
            return os.create_input_stream();
        }
    }
    public void read_value(org.omg.CORBA.portable.InputStream in, TypeCode tc)
    {
        typeCode = TypeCodeImpl.convertToNative(orb, tc);
        int kind = realType().kind().value();
        if (kind >= isStreamed.length) {
            throw wrapper.invalidIsstreamedTckind( CompletionStatus.COMPLETED_MAYBE,
                new Integer(kind)) ;
        }
        if (AnyImpl.isStreamed[kind]) {
            if ( in instanceof AnyInputStream ) {
                stream = (CDRInputStream)in;
            } else {
                org.omg.CORBA_2_3.portable.OutputStream out =
                    (org.omg.CORBA_2_3.portable.OutputStream)orb.create_output_stream();
                typeCode.copy((org.omg.CORBA_2_3.portable.InputStream)in, out);
                stream = (CDRInputStream)out.create_input_stream();
            }
        } else {
            java.lang.Object[] objholder = new java.lang.Object[1];
            objholder[0] = object;
            long[] longholder = new long[1];
            TCUtility.unmarshalIn(in, typeCode, longholder, objholder);
            value = longholder[0];
            object = objholder[0];
            stream = null;
        }
        isInitialized = true;
    }
    public void write_value(OutputStream out)
    {
        if (AnyImpl.isStreamed[realType().kind().value()]) {
            typeCode.copy(stream.dup(), out);
        } else {
            TCUtility.marshalIn(out, realType(), value, object);
        }
    }
    public void insert_Streamable(Streamable s)
    {
        typeCode = TypeCodeImpl.convertToNative(orb, s._type());
        object = s;
        isInitialized = true;
    }
    public Streamable extract_Streamable()
    {
        return (Streamable)object;
    }
    public void insert_short(short s)
    {
        typeCode = orb.get_primitive_tc(TCKind._tk_short);
        value = s;
        isInitialized = true;
    }
    private String getTCKindName( int tc )
    {
        if ((tc >= 0) && (tc < TypeCodeImpl.kindNames.length))
            return TypeCodeImpl.kindNames[tc] ;
        else
            return "UNKNOWN(" + tc + ")" ;
    }
    private void checkExtractBadOperation( int expected )
    {
        if (!isInitialized)
            throw wrapper.extractNotInitialized() ;
        int tc = realType().kind().value() ;
        if (tc != expected) {
            String tcName = getTCKindName( tc ) ;
            String expectedName = getTCKindName( expected ) ;
            throw wrapper.extractWrongType( expectedName, tcName ) ;
        }
    }
    private void checkExtractBadOperationList( int[] expected )
    {
        if (!isInitialized)
            throw wrapper.extractNotInitialized() ;
        int tc = realType().kind().value() ;
        for (int ctr=0; ctr<expected.length; ctr++)
            if (tc == expected[ctr])
                return ;
        List list = new ArrayList() ;
        for (int ctr=0; ctr<expected.length; ctr++)
            list.add( getTCKindName( expected[ctr] ) ) ;
        String tcName = getTCKindName( tc ) ;
        throw wrapper.extractWrongTypeList( list, tcName ) ;
    }
    public short extract_short()
    {
        checkExtractBadOperation( TCKind._tk_short ) ;
        return (short)value;
    }
    public void insert_long(int l)
    {
        int kind = realType().kind().value();
        if (kind != TCKind._tk_long && kind != TCKind._tk_enum) {
            typeCode = orb.get_primitive_tc(TCKind._tk_long);
        }
        value = l;
        isInitialized = true;
    }
    public int extract_long()
    {
        checkExtractBadOperationList( new int[] { TCKind._tk_long, TCKind._tk_enum } ) ;
        return (int)value;
    }
    public void insert_ushort(short s)
    {
        typeCode = orb.get_primitive_tc(TCKind._tk_ushort);
        value = s;
        isInitialized = true;
    }
    public short extract_ushort()
    {
        checkExtractBadOperation( TCKind._tk_ushort ) ;
        return (short)value;
    }
    public void insert_ulong(int l)
    {
        typeCode = orb.get_primitive_tc(TCKind._tk_ulong);
        value = l;
        isInitialized = true;
    }
    public int extract_ulong()
    {
        checkExtractBadOperation( TCKind._tk_ulong ) ;
        return (int)value;
    }
    public void insert_float(float f)
    {
        typeCode = orb.get_primitive_tc(TCKind._tk_float);
        value = Float.floatToIntBits(f);
        isInitialized = true;
    }
    public float extract_float()
    {
        checkExtractBadOperation( TCKind._tk_float ) ;
        return Float.intBitsToFloat((int)value);
    }
    public void insert_double(double d)
    {
        typeCode = orb.get_primitive_tc(TCKind._tk_double);
        value = Double.doubleToLongBits(d);
        isInitialized = true;
    }
    public double extract_double()
    {
        checkExtractBadOperation( TCKind._tk_double ) ;
        return Double.longBitsToDouble(value);
    }
    public void insert_longlong(long l)
    {
        typeCode = orb.get_primitive_tc(TCKind._tk_longlong);
        value = l;
        isInitialized = true;
    }
    public long extract_longlong()
    {
        checkExtractBadOperation( TCKind._tk_longlong ) ;
        return value;
    }
    public void insert_ulonglong(long l)
    {
        typeCode = orb.get_primitive_tc(TCKind._tk_ulonglong);
        value = l;
        isInitialized = true;
    }
    public long extract_ulonglong()
    {
        checkExtractBadOperation( TCKind._tk_ulonglong ) ;
        return value;
    }
    public void insert_boolean(boolean b)
    {
        typeCode = orb.get_primitive_tc(TCKind._tk_boolean);
        value = (b)? 1:0;
        isInitialized = true;
    }
    public boolean extract_boolean()
    {
        checkExtractBadOperation( TCKind._tk_boolean ) ;
        return (value == 0)? false: true;
    }
    public void insert_char(char c)
    {
        typeCode = orb.get_primitive_tc(TCKind._tk_char);
        value = c;
        isInitialized = true;
    }
    public char extract_char()
    {
        checkExtractBadOperation( TCKind._tk_char ) ;
        return (char)value;
    }
    public void insert_wchar(char c)
    {
        typeCode = orb.get_primitive_tc(TCKind._tk_wchar);
        value = c;
        isInitialized = true;
    }
    public char extract_wchar()
    {
        checkExtractBadOperation( TCKind._tk_wchar ) ;
        return (char)value;
    }
    public void insert_octet(byte b)
    {
        typeCode = orb.get_primitive_tc(TCKind._tk_octet);
        value = b;
        isInitialized = true;
    }
    public byte extract_octet()
    {
        checkExtractBadOperation( TCKind._tk_octet ) ;
        return (byte)value;
    }
    public void insert_string(String s)
    {
        if (typeCode.kind() == TCKind.tk_string) {
            int length = 0;
            try {
                length = typeCode.length();
            } catch (BadKind bad) {
                throw wrapper.badkindCannotOccur() ;
            }
            if (length != 0 && s != null && s.length() > length) {
                throw wrapper.badStringBounds( new Integer(s.length()),
                    new Integer(length) ) ;
            }
        } else {
            typeCode = orb.get_primitive_tc(TCKind._tk_string);
        }
        object = s;
        isInitialized = true;
    }
    public String extract_string()
    {
        checkExtractBadOperation( TCKind._tk_string ) ;
        return (String)object;
    }
    public void insert_wstring(String s)
    {
        if (typeCode.kind() == TCKind.tk_wstring) {
            int length = 0;
            try {
                length = typeCode.length();
            } catch (BadKind bad) {
                throw wrapper.badkindCannotOccur() ;
            }
            if (length != 0 && s != null && s.length() > length) {
                throw wrapper.badStringBounds( new Integer(s.length()),
                    new Integer(length) ) ;
            }
        } else {
            typeCode = orb.get_primitive_tc(TCKind._tk_wstring);
        }
        object = s;
        isInitialized = true;
    }
    public String extract_wstring()
    {
        checkExtractBadOperation( TCKind._tk_wstring ) ;
        return (String)object;
    }
    public void insert_any(Any a)
    {
        typeCode = orb.get_primitive_tc(TCKind._tk_any);
        object = a;
        stream = null;
        isInitialized = true;
    }
    public Any extract_any()
    {
        checkExtractBadOperation( TCKind._tk_any ) ;
        return (Any)object;
    }
    public void insert_Object(org.omg.CORBA.Object o)
    {
        if ( o == null ) {
            typeCode = orb.get_primitive_tc(TCKind._tk_objref);
        } else {
            if (StubAdapter.isStub(o)) {
                String[] ids = StubAdapter.getTypeIds( o ) ;
                typeCode = new TypeCodeImpl(orb, TCKind._tk_objref, ids[0], "");
            } else {
                throw wrapper.badInsertobjParam(
                    CompletionStatus.COMPLETED_MAYBE, o.getClass().getName() ) ;
            }
        }
        object = o;
        isInitialized = true;
    }
    public void insert_Object(org.omg.CORBA.Object o, TypeCode tc)
    {
        try {
            if ( tc.id().equals("IDL:omg.org/CORBA/Object:1.0") || o._is_a(tc.id()) )
                {
                    typeCode = TypeCodeImpl.convertToNative(orb, tc);
                    object = o;
                }
            else {
                throw wrapper.insertObjectIncompatible() ;
            }
        } catch ( Exception ex ) {
            throw wrapper.insertObjectFailed(ex) ;
        }
        isInitialized = true;
    }
    public org.omg.CORBA.Object extract_Object()
    {
        if (!isInitialized)
            throw wrapper.extractNotInitialized() ;
        org.omg.CORBA.Object obj = null;
        try {
            obj = (org.omg.CORBA.Object) object;
            if (typeCode.id().equals("IDL:omg.org/CORBA/Object:1.0") || obj._is_a(typeCode.id())) {
                return obj;
            } else {
                throw wrapper.extractObjectIncompatible() ;
            }
        } catch ( Exception ex ) {
            throw wrapper.extractObjectFailed(ex);
        }
    }
    public void insert_TypeCode(TypeCode tc)
    {
        typeCode = orb.get_primitive_tc(TCKind._tk_TypeCode);
        object = tc;
        isInitialized = true;
    }
    public TypeCode extract_TypeCode()
    {
        checkExtractBadOperation( TCKind._tk_TypeCode ) ;
        return (TypeCode)object;
    }
    @Deprecated
    public void insert_Principal(Principal p)
    {
        typeCode = orb.get_primitive_tc(TCKind._tk_Principal);
        object = p;
        isInitialized = true;
    }
    @Deprecated
    public Principal extract_Principal()
    {
        checkExtractBadOperation( TCKind._tk_Principal ) ;
        return (Principal)object;
    }
    public Serializable extract_Value()
    {
        checkExtractBadOperationList( new int[] { TCKind._tk_value,
            TCKind._tk_value_box, TCKind._tk_abstract_interface } ) ;
        return (Serializable)object;
    }
    public void insert_Value(Serializable v)
    {
        object = v;
        TypeCode tc;
        if ( v == null ) {
            tc = orb.get_primitive_tc (TCKind.tk_value);
        } else {
            tc = createTypeCodeForClass (v.getClass(), (ORB)ORB.init());
        }
        typeCode = TypeCodeImpl.convertToNative(orb, tc);
        isInitialized = true;
    }
    public void insert_Value(Serializable v, org.omg.CORBA.TypeCode t)
    {
        object = v;
        typeCode = TypeCodeImpl.convertToNative(orb, t);
        isInitialized = true;
    }
    public void insert_fixed(java.math.BigDecimal value) {
        typeCode = TypeCodeImpl.convertToNative(orb,
            orb.create_fixed_tc(TypeCodeImpl.digits(value), TypeCodeImpl.scale(value)));
        object = value;
        isInitialized = true;
    }
    public void insert_fixed(java.math.BigDecimal value, org.omg.CORBA.TypeCode type)
    {
        try {
            if (TypeCodeImpl.digits(value) > type.fixed_digits() ||
                TypeCodeImpl.scale(value) > type.fixed_scale())
            {
                throw wrapper.fixedNotMatch() ;
            }
        } catch (org.omg.CORBA.TypeCodePackage.BadKind bk) {
            throw wrapper.fixedBadTypecode( bk ) ;
        }
        typeCode = TypeCodeImpl.convertToNative(orb, type);
        object = value;
        isInitialized = true;
    }
    public java.math.BigDecimal extract_fixed() {
        checkExtractBadOperation( TCKind._tk_fixed ) ;
        return (BigDecimal)object;
    }
    public TypeCode createTypeCodeForClass (java.lang.Class c, ORB tcORB)
    {
        TypeCodeImpl classTC = tcORB.getTypeCodeForClass(c);
        if (classTC != null)
            return classTC;
        RepositoryIdStrings repStrs
            = RepositoryIdFactory.getRepIdStringsFactory(tcORB);
        if ( c.isArray() ) {
            Class componentClass = c.getComponentType();
            TypeCode embeddedType;
            if ( componentClass.isPrimitive() ) {
                embeddedType = getPrimitiveTypeCodeForClass(componentClass,
                                                            tcORB);
            } else {
                embeddedType = createTypeCodeForClass (componentClass,
                                                       tcORB);
            }
            TypeCode t = tcORB.create_sequence_tc (0, embeddedType);
            String id = repStrs.createForJavaType(c);
            return tcORB.create_value_box_tc (id, "Sequence", t);
        } else if ( c == java.lang.String.class ) {
            TypeCode t = tcORB.create_string_tc (0);
            String id = repStrs.createForJavaType(c);
            return tcORB.create_value_box_tc (id, "StringValue", t);
        }
        classTC = (TypeCodeImpl)ValueUtility.createTypeCodeForClass(
            tcORB, c, ORBUtility.createValueHandler(tcORB));
        classTC.setCaching(true);
        tcORB.setTypeCodeForClass(c, classTC);
        return classTC;
    }
    private TypeCode getPrimitiveTypeCodeForClass (Class c, ORB tcORB)
    {
        if (c == Integer.TYPE) {
            return tcORB.get_primitive_tc (TCKind.tk_long);
        } else if (c == Byte.TYPE) {
            return tcORB.get_primitive_tc (TCKind.tk_octet);
        } else if (c == Long.TYPE) {
            return tcORB.get_primitive_tc (TCKind.tk_longlong);
        } else if (c == Float.TYPE) {
            return tcORB.get_primitive_tc (TCKind.tk_float);
        } else if (c == Double.TYPE) {
            return tcORB.get_primitive_tc (TCKind.tk_double);
        } else if (c == Short.TYPE) {
            return tcORB.get_primitive_tc (TCKind.tk_short);
        } else if (c == Character.TYPE) {
            if (ORBVersionFactory.getFOREIGN().compareTo(tcORB.getORBVersion()) == 0 ||
                ORBVersionFactory.getNEWER().compareTo(tcORB.getORBVersion()) <= 0)
                return tcORB.get_primitive_tc(TCKind.tk_wchar);
            else
                return tcORB.get_primitive_tc(TCKind.tk_char);
        } else if (c == Boolean.TYPE) {
            return tcORB.get_primitive_tc (TCKind.tk_boolean);
        } else {
            return tcORB.get_primitive_tc (TCKind.tk_any);
        }
    }
    public Any extractAny(TypeCode memberType, ORB orb) {
        Any returnValue = orb.create_any();
        OutputStream out = returnValue.create_output_stream();
        TypeCodeImpl.convertToNative(orb, memberType).copy((InputStream)stream, out);
        returnValue.read_value(out.create_input_stream(), memberType);
        return returnValue;
    }
    static public Any extractAnyFromStream(TypeCode memberType, InputStream input, ORB orb) {
        Any returnValue = orb.create_any();
        OutputStream out = returnValue.create_output_stream();
        TypeCodeImpl.convertToNative(orb, memberType).copy(input, out);
        returnValue.read_value(out.create_input_stream(), memberType);
        return returnValue;
    }
    public boolean isInitialized() {
        return isInitialized;
    }
}
