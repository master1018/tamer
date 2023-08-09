public final class TypeCodeImpl extends TypeCode
{
    protected static final int tk_indirect = 0xFFFFFFFF;
    private static final int EMPTY = 0; 
    private static final int SIMPLE = 1;        
    private static final int COMPLEX = 2; 
    private static final int typeTable[] = {
        EMPTY,  
        EMPTY,  
        EMPTY,  
        EMPTY,  
        EMPTY,  
        EMPTY,  
        EMPTY,  
        EMPTY,  
        EMPTY,  
        EMPTY,  
        EMPTY,  
        EMPTY,  
        EMPTY,  
        EMPTY,  
        COMPLEX,        
        COMPLEX,        
        COMPLEX,        
        COMPLEX,        
        SIMPLE, 
        COMPLEX,        
        COMPLEX,        
        COMPLEX,        
        COMPLEX,        
        EMPTY,  
        EMPTY,  
        EMPTY,  
        EMPTY,  
        SIMPLE, 
        SIMPLE, 
        COMPLEX,        
        COMPLEX,        
        COMPLEX,        
        COMPLEX 
    };
    static final String[] kindNames = {
        "null",
        "void",
        "short",
        "long",
        "ushort",
        "ulong",
        "float",
        "double",
        "boolean",
        "char",
        "octet",
        "any",
        "typecode",
        "principal",
        "objref",
        "struct",
        "union",
        "enum",
        "string",
        "sequence",
        "array",
        "alias",
        "exception",
        "longlong",
        "ulonglong",
        "longdouble",
        "wchar",
        "wstring",
        "fixed",
        "value",
        "valueBox",
        "native",
        "abstractInterface"
    };
    private int                 _kind           = 0;    
    private String          _id             = "";   
    private String          _name           = "";   
    private int             _memberCount    = 0;    
    private String          _memberNames[]  = null; 
    private TypeCodeImpl    _memberTypes[]  = null; 
    private AnyImpl         _unionLabels[]  = null; 
    private TypeCodeImpl    _discriminator  = null; 
    private int             _defaultIndex   = -1;   
    private int             _length         = 0;    
    private TypeCodeImpl    _contentType    = null; 
    private short           _digits         = 0;
    private short           _scale          = 0;
    private short           _type_modifier  = -1;   
    private TypeCodeImpl    _concrete_base  = null; 
    private short           _memberAccess[] = null; 
    private TypeCodeImpl    _parent         = null; 
    private int             _parentOffset   = 0;    
    private TypeCodeImpl    _indirectType   = null;
    private byte[] outBuffer                = null;
    private boolean cachingEnabled          = false;
    private ORB _orb;
    private ORBUtilSystemException wrapper ;
    public TypeCodeImpl(ORB orb)
    {
        _orb = orb;
        wrapper = ORBUtilSystemException.get(
            (com.sun.corba.se.spi.orb.ORB)orb, CORBALogDomains.RPC_PRESENTATION ) ;
    }
    public TypeCodeImpl(ORB orb, TypeCode tc)
    {
        this(orb) ;
        if (tc instanceof TypeCodeImpl) {
            TypeCodeImpl tci = (TypeCodeImpl)tc;
            if (tci._kind == tk_indirect)
                throw wrapper.badRemoteTypecode() ;
            if (tci._kind == TCKind._tk_sequence && tci._contentType == null)
                throw wrapper.badRemoteTypecode() ;
        }
        _kind   = tc.kind().value();
        try {
            switch (_kind) {
            case TCKind._tk_value:
                _type_modifier = tc.type_modifier();
                TypeCode tccb = tc.concrete_base_type();
                if (tccb != null) {
                    _concrete_base = convertToNative(_orb, tccb);
                } else {
                    _concrete_base = null;
                }
                _memberAccess = new short[tc.member_count()];
                for (int i=0; i < tc.member_count(); i++) {
                    _memberAccess[i] = tc.member_visibility(i);
                }
            case TCKind._tk_except:
            case TCKind._tk_struct:
            case TCKind._tk_union:
                _memberTypes = new TypeCodeImpl[tc.member_count()];
                for (int i=0; i < tc.member_count(); i++) {
                    _memberTypes[i] = convertToNative(_orb, tc.member_type(i));
                    _memberTypes[i].setParent(this);
                }
            case TCKind._tk_enum:
                _memberNames = new String[tc.member_count()];
                for (int i=0; i < tc.member_count(); i++) {
                    _memberNames[i] = tc.member_name(i);
                }
                _memberCount = tc.member_count();
            case TCKind._tk_objref:
            case TCKind._tk_alias:
            case TCKind._tk_value_box:
            case TCKind._tk_native:
            case TCKind._tk_abstract_interface:
                setId(tc.id());
                _name = tc.name();
                break;
            }
            switch (_kind) {
            case TCKind._tk_union:
                _discriminator = convertToNative(_orb, tc.discriminator_type());
                _defaultIndex  = tc.default_index();
                _unionLabels = new AnyImpl[_memberCount];
                for (int i=0; i < _memberCount; i++)
                    _unionLabels[i] = new AnyImpl(_orb, tc.member_label(i));
                break;
            }
            switch (_kind) {
            case TCKind._tk_string:
            case TCKind._tk_wstring:
            case TCKind._tk_sequence:
            case TCKind._tk_array:
                _length = tc.length();
            }
            switch (_kind) {
            case TCKind._tk_sequence:
            case TCKind._tk_array:
            case TCKind._tk_alias:
            case TCKind._tk_value_box:
                _contentType = convertToNative(_orb, tc.content_type());
            }
        } catch (org.omg.CORBA.TypeCodePackage.Bounds e) {} catch (BadKind e) {}
    }
    public TypeCodeImpl(ORB orb, int creationKind)
    {
        this(orb);
        _kind = creationKind;
        switch (_kind) {
        case TCKind._tk_objref:
            {
                setId("IDL:omg.org/CORBA/Object:1.0");
                _name = "Object";
                break;
            }
        case TCKind._tk_string:
        case TCKind._tk_wstring:
            {
                _length =0;
                break;
            }
        case TCKind._tk_value:
            {
                _concrete_base = null;
                break;
            }
        }
    }
    public TypeCodeImpl(ORB orb,
                        int creationKind,
                        String id,
                        String name,
                        StructMember[] members)
    {
        this(orb);
        if ((creationKind == TCKind._tk_struct) || (creationKind == TCKind._tk_except)) {
            _kind               = creationKind;
            setId(id);
            _name               = name;
            _memberCount        = members.length;
            _memberNames = new String[_memberCount];
            _memberTypes = new TypeCodeImpl[_memberCount];
            for (int i = 0 ; i < _memberCount ; i++) {
                _memberNames[i] = members[i].name;
                _memberTypes[i] = convertToNative(_orb, members[i].type);
                _memberTypes[i].setParent(this);
            }
        } 
    }
    public TypeCodeImpl(ORB orb,
                        int creationKind,
                        String id,
                        String name,
                        TypeCode discriminator_type,
                        UnionMember[] members)
    {
        this(orb) ;
        if (creationKind == TCKind._tk_union) {
            _kind               = creationKind;
            setId(id);
            _name               = name;
            _memberCount        = members.length;
            _discriminator      = convertToNative(_orb, discriminator_type);
            _memberNames = new String[_memberCount];
            _memberTypes = new TypeCodeImpl[_memberCount];
            _unionLabels = new AnyImpl[_memberCount];
            for (int i = 0 ; i < _memberCount ; i++) {
                _memberNames[i] = members[i].name;
                _memberTypes[i] = convertToNative(_orb, members[i].type);
                _memberTypes[i].setParent(this);
                _unionLabels[i] = new AnyImpl(_orb, members[i].label);
                if (_unionLabels[i].type().kind() == TCKind.tk_octet) {
                    if (_unionLabels[i].extract_octet() == (byte)0) {
                        _defaultIndex = i;
                    }
                }
            }
        } 
    }
    public TypeCodeImpl(ORB orb,
                        int creationKind,
                        String id,
                        String name,
                        short type_modifier,
                        TypeCode concrete_base,
                        ValueMember[] members)
    {
        this(orb) ;
        if (creationKind == TCKind._tk_value) {
            _kind               = creationKind;
            setId(id);
            _name               = name;
            _type_modifier      = type_modifier;
            if (concrete_base != null) {
                _concrete_base = convertToNative(_orb, concrete_base);
            }
            _memberCount        = members.length;
            _memberNames = new String[_memberCount];
            _memberTypes = new TypeCodeImpl[_memberCount];
            _memberAccess = new short[_memberCount];
            for (int i = 0 ; i < _memberCount ; i++) {
                _memberNames[i] = members[i].name;
                _memberTypes[i] = convertToNative(_orb, members[i].type);
                _memberTypes[i].setParent(this);
                _memberAccess[i] = members[i].access;
            }
        } 
    }
    public TypeCodeImpl(ORB orb,
                        int creationKind,
                        String id,
                        String name,
                        String[] members)
    {
        this(orb) ;
        if (creationKind == TCKind._tk_enum)
            {
                _kind           = creationKind;
                setId(id);
                _name           = name;
                _memberCount    = members.length;
                _memberNames = new String[_memberCount];
                for (int i = 0 ; i < _memberCount ; i++)
                    _memberNames[i] = members[i];
            } 
    }
    public TypeCodeImpl(ORB orb,
                        int creationKind,
                        String id,
                        String name,
                        TypeCode original_type)
    {
        this(orb) ;
        if ( creationKind == TCKind._tk_alias || creationKind == TCKind._tk_value_box )
            {
                _kind           = creationKind;
                setId(id);
                _name           = name;
                _contentType    = convertToNative(_orb, original_type);
            }
    }
    public TypeCodeImpl(ORB orb,
                        int creationKind,
                        String id,
                        String name)
    {
        this(orb) ;
        if (creationKind == TCKind._tk_objref ||
            creationKind == TCKind._tk_native ||
            creationKind == TCKind._tk_abstract_interface)
            {
                _kind           = creationKind;
                setId(id);
                _name           = name;
            } 
    }
    public TypeCodeImpl(ORB orb,
                        int creationKind,
                        int bound)
    {
        this(orb) ;
        if (bound < 0)
            throw wrapper.negativeBounds() ;
        if ((creationKind == TCKind._tk_string) || (creationKind == TCKind._tk_wstring)) {
            _kind               = creationKind;
            _length             = bound;
        } 
    }
    public TypeCodeImpl(ORB orb,
                        int creationKind,
                        int bound,
                        TypeCode element_type)
    {
        this(orb) ;
        if ( creationKind == TCKind._tk_sequence || creationKind == TCKind._tk_array ) {
            _kind               = creationKind;
            _length             = bound;
            _contentType        = convertToNative(_orb, element_type);
        } 
    }
    public TypeCodeImpl(ORB orb,
                        int creationKind,
                        int bound,
                        int offset)
    {
        this(orb) ;
        if (creationKind == TCKind._tk_sequence) {
            _kind               = creationKind;
            _length             = bound;
            _parentOffset       = offset;
        } 
    }
    public TypeCodeImpl(ORB orb,
                        String id)
    {
        this(orb) ;
        _kind   = tk_indirect;
        _id             = id;
        tryIndirectType();
    }
    public TypeCodeImpl(ORB orb,
                        int creationKind,
                        short digits,
                        short scale)
    {
        this(orb) ;
        if (creationKind == TCKind._tk_fixed) {
            _kind               = creationKind;
            _digits             = digits;
            _scale              = scale;
        } 
    }
    protected static TypeCodeImpl convertToNative(ORB orb,
                                                  TypeCode tc)
    {
        if (tc instanceof TypeCodeImpl)
            return (TypeCodeImpl) tc;
        else
            return new TypeCodeImpl(orb, tc);
    }
    public static CDROutputStream newOutputStream(ORB orb) {
        TypeCodeOutputStream tcos = new TypeCodeOutputStream((ORB)orb);
        return tcos;
    }
    private TypeCodeImpl indirectType() {
        _indirectType = tryIndirectType();
        if (_indirectType == null) {
            throw wrapper.unresolvedRecursiveTypecode() ;
        }
        return _indirectType;
    }
    private TypeCodeImpl tryIndirectType() {
        if (_indirectType != null)
            return _indirectType;
        setIndirectType(_orb.getTypeCode(_id));
        return _indirectType;
    }
    private void setIndirectType(TypeCodeImpl newType) {
        _indirectType = newType;
        if (_indirectType != null) {
            try {
                _id = _indirectType.id();
            } catch (BadKind e) {
                throw wrapper.badkindCannotOccur() ;
            }
        }
    }
    private void setId(String newID) {
        _id = newID;
        if (_orb instanceof TypeCodeFactory) {
            ((TypeCodeFactory)_orb).setTypeCode(_id, this);
        }
    }
    private void setParent(TypeCodeImpl parent) {
        _parent = parent;
    }
    private TypeCodeImpl getParentAtLevel(int level) {
        if (level == 0)
            return this;
        if (_parent == null)
            throw wrapper.unresolvedRecursiveTypecode() ;
        return _parent.getParentAtLevel(level - 1);
    }
    private TypeCodeImpl lazy_content_type() {
        if (_contentType == null) {
            if (_kind == TCKind._tk_sequence && _parentOffset > 0 && _parent != null) {
                TypeCodeImpl realParent = getParentAtLevel(_parentOffset);
                if (realParent != null && realParent._id != null) {
                    _contentType = new TypeCodeImpl((ORB)_orb, realParent._id);
                }
            }
        }
        return _contentType;
    }
    private TypeCode realType(TypeCode aType) {
        TypeCode realType = aType;
        try {
            while (realType.kind().value() == TCKind._tk_alias) {
                realType = realType.content_type();
            }
        } catch (BadKind bad) {
            throw wrapper.badkindCannotOccur() ;
        }
        return realType;
    }
    public final boolean equal(TypeCode tc)
    {
        if (tc == this)
            return true;
        try {
            if (_kind == tk_indirect) {
                if (_id != null && tc.id() != null)
                    return _id.equals(tc.id());
                return (_id == null && tc.id() == null);
            }
            if (_kind != tc.kind().value()) {
                return false;
            }
            switch (typeTable[_kind]) {
            case EMPTY:
                return true;
            case SIMPLE:
                switch (_kind) {
                case TCKind._tk_string:
                case TCKind._tk_wstring:
                    return (_length == tc.length());
                case TCKind._tk_fixed:
                    return (_digits == tc.fixed_digits() && _scale == tc.fixed_scale());
                default:
                    return false;
                }
            case COMPLEX:
                switch(_kind) {
                case TCKind._tk_objref:
                    {
                        if (_id.compareTo(tc.id()) == 0) {
                            return true;
                        }
                        if (_id.compareTo(
                            (_orb.get_primitive_tc(_kind)).id()) == 0)
                        {
                            return true;
                        }
                        if (tc.id().compareTo(
                            (_orb.get_primitive_tc(_kind)).id()) == 0)
                        {
                            return true;
                        }
                        return false;
                    }
                case TCKind._tk_native:
                case TCKind._tk_abstract_interface:
                    {
                        if (_id.compareTo(tc.id()) != 0) {
                            return false;
                        }
                        return true;
                    }
                case TCKind._tk_struct:
                case TCKind._tk_except:
                    {
                        if (_memberCount != tc.member_count())
                            return false;
                        if (_id.compareTo(tc.id()) != 0)
                            return false;
                        for (int i = 0 ; i < _memberCount ; i++)
                            if (! _memberTypes[i].equal(tc.member_type(i)))
                                return false;
                        return true;
                    }
                case TCKind._tk_union:
                    {
                        if (_memberCount != tc.member_count())
                            return false;
                        if (_id.compareTo(tc.id()) != 0)
                            return false;
                        if (_defaultIndex != tc.default_index())
                            return false;
                        if (!_discriminator.equal(tc.discriminator_type()))
                            return false;
                        for (int i = 0 ; i < _memberCount ; i++)
                            if (! _unionLabels[i].equal(tc.member_label(i)))
                                return false;
                        for (int i = 0 ; i < _memberCount ; i++)
                            if (! _memberTypes[i].equal(tc.member_type(i)))
                                return false;
                        return true;
                    }
                case TCKind._tk_enum:
                    {
                        if (_id.compareTo(tc.id()) != 0)
                            return false;
                        if (_memberCount != tc.member_count())
                            return false;
                        return true;
                    }
                case TCKind._tk_sequence:
                case TCKind._tk_array:
                    {
                        if (_length != tc.length()) {
                            return false;
                        }
                        if (! lazy_content_type().equal(tc.content_type())) {
                            return false;
                        }
                        return true;
                    }
                case TCKind._tk_value:
                    {
                        if (_memberCount != tc.member_count())
                            return false;
                        if (_id.compareTo(tc.id()) != 0)
                            return false;
                        for (int i = 0 ; i < _memberCount ; i++)
                            if (_memberAccess[i] != tc.member_visibility(i) ||
                                ! _memberTypes[i].equal(tc.member_type(i)))
                                return false;
                        if (_type_modifier == tc.type_modifier())
                            return false;
                        TypeCode tccb = tc.concrete_base_type();
                        if ((_concrete_base == null && tccb != null) ||
                            (_concrete_base != null && tccb == null) ||
                            ! _concrete_base.equal(tccb))
                        {
                            return false;
                        }
                        return true;
                    }
                case TCKind._tk_alias:
                case TCKind._tk_value_box:
                    {
                        if (_id.compareTo(tc.id()) != 0) {
                            return false;
                        }
                        return _contentType.equal(tc.content_type());
                    }
                }
            }
        } catch (org.omg.CORBA.TypeCodePackage.Bounds e) {} catch (BadKind e) {}
        return false;
    }
    public boolean equivalent(TypeCode tc) {
        if (tc == this) {
            return true;
        }
        TypeCode myRealType = (_kind == tk_indirect ? indirectType() : this);
        myRealType = realType(myRealType);
        TypeCode otherRealType = realType(tc);
        if (myRealType.kind().value() != otherRealType.kind().value()) {
            return false;
        }
        String myID = null;
        String otherID = null;
        try {
            myID = this.id();
            otherID = tc.id();
            if (myID != null && otherID != null) {
                return (myID.equals(otherID));
            }
        } catch (BadKind e) {
        }
        int myKind = myRealType.kind().value();
        try {
            if (myKind == TCKind._tk_struct ||
                myKind == TCKind._tk_union ||
                myKind == TCKind._tk_enum ||
                myKind == TCKind._tk_except ||
                myKind == TCKind._tk_value)
            {
                if (myRealType.member_count() != otherRealType.member_count())
                    return false;
            }
            if (myKind == TCKind._tk_union)
            {
                if (myRealType.default_index() != otherRealType.default_index())
                    return false;
            }
            if (myKind == TCKind._tk_string ||
                myKind == TCKind._tk_wstring ||
                myKind == TCKind._tk_sequence ||
                myKind == TCKind._tk_array)
            {
                if (myRealType.length() != otherRealType.length())
                    return false;
            }
            if (myKind == TCKind._tk_fixed)
            {
                if (myRealType.fixed_digits() != otherRealType.fixed_digits() ||
                    myRealType.fixed_scale() != otherRealType.fixed_scale())
                    return false;
            }
            if (myKind == TCKind._tk_union)
            {
                for (int i=0; i<myRealType.member_count(); i++) {
                    if (myRealType.member_label(i) != otherRealType.member_label(i))
                        return false;
                }
                if ( ! myRealType.discriminator_type().equivalent(
                    otherRealType.discriminator_type()))
                    return false;
            }
            if (myKind == TCKind._tk_alias ||
                myKind == TCKind._tk_value_box ||
                myKind == TCKind._tk_sequence ||
                myKind == TCKind._tk_array)
            {
                if ( ! myRealType.content_type().equivalent(otherRealType.content_type()))
                    return false;
            }
            if (myKind == TCKind._tk_struct ||
                myKind == TCKind._tk_union ||
                myKind == TCKind._tk_except ||
                myKind == TCKind._tk_value)
            {
                for (int i=0; i<myRealType.member_count(); i++) {
                    if ( ! myRealType.member_type(i).equivalent(
                        otherRealType.member_type(i)))
                        return false;
                }
            }
        } catch (BadKind e) {
            throw wrapper.badkindCannotOccur() ;
        } catch (org.omg.CORBA.TypeCodePackage.Bounds e) {
            throw wrapper.boundsCannotOccur() ;
        }
        return true;
    }
    public TypeCode get_compact_typecode() {
        return this;
    }
    public TCKind kind()
    {
        if (_kind == tk_indirect)
            return indirectType().kind();
        return TCKind.from_int(_kind);
    }
    public boolean is_recursive()
    {
        return (_kind == tk_indirect);
    }
    public String id()
        throws BadKind
    {
        switch (_kind) {
        case tk_indirect:
        case TCKind._tk_except:
        case TCKind._tk_objref:
        case TCKind._tk_struct:
        case TCKind._tk_union:
        case TCKind._tk_enum:
        case TCKind._tk_alias:
        case TCKind._tk_value:
        case TCKind._tk_value_box:
        case TCKind._tk_native:
        case TCKind._tk_abstract_interface:
            return _id;
        default:
            throw new BadKind();
        }
    }
    public String name()
        throws BadKind
    {
        switch (_kind) {
        case tk_indirect:
            return indirectType().name();
        case TCKind._tk_except:
        case TCKind._tk_objref:
        case TCKind._tk_struct:
        case TCKind._tk_union:
        case TCKind._tk_enum:
        case TCKind._tk_alias:
        case TCKind._tk_value:
        case TCKind._tk_value_box:
        case TCKind._tk_native:
        case TCKind._tk_abstract_interface:
            return _name;
        default:
            throw new BadKind();
        }
    }
    public int member_count()
        throws BadKind
    {
        switch (_kind) {
        case tk_indirect:
            return indirectType().member_count();
        case TCKind._tk_except:
        case TCKind._tk_struct:
        case TCKind._tk_union:
        case TCKind._tk_enum:
        case TCKind._tk_value:
            return _memberCount;
        default:
            throw new BadKind();
        }
    }
    public String member_name(int index)
        throws BadKind, org.omg.CORBA.TypeCodePackage.Bounds
    {
        switch (_kind) {
        case tk_indirect:
            return indirectType().member_name(index);
        case TCKind._tk_except:
        case TCKind._tk_struct:
        case TCKind._tk_union:
        case TCKind._tk_enum:
        case TCKind._tk_value:
            try {
                return _memberNames[index];
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new org.omg.CORBA.TypeCodePackage.Bounds();
            }
        default:
            throw new BadKind();
        }
    }
    public TypeCode member_type(int index)
        throws BadKind, org.omg.CORBA.TypeCodePackage.Bounds
    {
        switch (_kind) {
        case tk_indirect:
            return indirectType().member_type(index);
        case TCKind._tk_except:
        case TCKind._tk_struct:
        case TCKind._tk_union:
        case TCKind._tk_value:
            try {
                return _memberTypes[index];
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new org.omg.CORBA.TypeCodePackage.Bounds();
            }
        default:
            throw new BadKind();
        }
    }
    public Any member_label(int index)
        throws BadKind, org.omg.CORBA.TypeCodePackage.Bounds
    {
        switch (_kind) {
        case tk_indirect:
            return indirectType().member_label(index);
        case TCKind._tk_union:
            try {
                return new AnyImpl(_orb, _unionLabels[index]);
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new org.omg.CORBA.TypeCodePackage.Bounds();
            }
        default:
            throw new BadKind();
        }
    }
    public TypeCode discriminator_type()
        throws BadKind
    {
        switch (_kind) {
        case tk_indirect:
            return indirectType().discriminator_type();
        case TCKind._tk_union:
            return _discriminator;
        default:
            throw new BadKind();
        }
    }
    public int default_index()
        throws BadKind
    {
        switch (_kind) {
        case tk_indirect:
            return indirectType().default_index();
        case TCKind._tk_union:
            return _defaultIndex;
        default:
            throw new BadKind();
        }
    }
    public int length()
        throws BadKind
    {
        switch (_kind) {
        case tk_indirect:
            return indirectType().length();
        case TCKind._tk_string:
        case TCKind._tk_wstring:
        case TCKind._tk_sequence:
        case TCKind._tk_array:
            return _length;
        default:
            throw new BadKind();
        }
    }
    public TypeCode content_type()
        throws BadKind
    {
        switch (_kind) {
        case tk_indirect:
            return indirectType().content_type();
        case TCKind._tk_sequence:
            return lazy_content_type();
        case TCKind._tk_array:
        case TCKind._tk_alias:
        case TCKind._tk_value_box:
            return _contentType;
        default:
            throw new BadKind();
        }
    }
    public short fixed_digits() throws BadKind {
        switch (_kind) {
        case TCKind._tk_fixed:
            return _digits;
        default:
            throw new BadKind();
        }
    }
    public short fixed_scale() throws BadKind {
        switch (_kind) {
        case TCKind._tk_fixed:
            return _scale;
        default:
            throw new BadKind();
        }
    }
    public short member_visibility(int index) throws BadKind,
        org.omg.CORBA.TypeCodePackage.Bounds {
        switch (_kind) {
        case tk_indirect:
            return indirectType().member_visibility(index);
        case TCKind._tk_value:
            try {
                return _memberAccess[index];
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new org.omg.CORBA.TypeCodePackage.Bounds();
            }
        default:
            throw new BadKind();
        }
    }
    public short type_modifier() throws BadKind {
        switch (_kind) {
        case tk_indirect:
            return indirectType().type_modifier();
        case TCKind._tk_value:
            return _type_modifier;
        default:
            throw new BadKind();
        }
    }
    public TypeCode concrete_base_type() throws BadKind {
        switch (_kind) {
        case tk_indirect:
            return indirectType().concrete_base_type();
        case TCKind._tk_value:
            return _concrete_base;
        default:
            throw new BadKind();
        }
    }
    public void read_value(InputStream is) {
        if (is instanceof TypeCodeReader) {
            if (read_value_kind((TypeCodeReader)is))
                read_value_body(is);
        } else if (is instanceof CDRInputStream) {
            WrapperInputStream wrapper = new WrapperInputStream((CDRInputStream)is);
            if (read_value_kind((TypeCodeReader)wrapper))
                read_value_body(wrapper);
        } else {
            read_value_kind(is);
            read_value_body(is);
        }
    }
    private void read_value_recursive(TypeCodeInputStream is) {
        if (is instanceof TypeCodeReader) {
            if (read_value_kind((TypeCodeReader)is))
                read_value_body(is);
        } else {
            read_value_kind((InputStream)is);
            read_value_body(is);
        }
    }
    boolean read_value_kind(TypeCodeReader tcis)
    {
        _kind = tcis.read_long();
        int myPosition = tcis.getTopLevelPosition()-4;
        if ((_kind < 0 || _kind > typeTable.length) && _kind != tk_indirect) {
            throw wrapper.cannotMarshalBadTckind() ;
        }
        if (_kind == TCKind._tk_native)
            throw wrapper.cannotMarshalNative() ;
        TypeCodeReader topStream = tcis.getTopLevelStream();
        if (_kind == tk_indirect) {
            int streamOffset = tcis.read_long();
            if (streamOffset > -4)
                throw wrapper.invalidIndirection( new Integer(streamOffset) ) ;
            int topPos = tcis.getTopLevelPosition();
            int indirectTypePosition = topPos - 4 + streamOffset;
            TypeCodeImpl type = topStream.getTypeCodeAtPosition(indirectTypePosition);
            if (type == null)
                throw wrapper.indirectionNotFound( new Integer(indirectTypePosition) ) ;
            setIndirectType(type);
            return false;
        }
        topStream.addTypeCodeAtPosition(this, myPosition);
        return true;
    }
    void read_value_kind(InputStream is) {
        _kind = is.read_long();
        if ((_kind < 0 || _kind > typeTable.length) && _kind != tk_indirect) {
            throw wrapper.cannotMarshalBadTckind() ;
        }
        if (_kind == TCKind._tk_native)
            throw wrapper.cannotMarshalNative() ;
        if (_kind == tk_indirect) {
            throw wrapper.recursiveTypecodeError() ;
        }
    }
    void read_value_body(InputStream is) {
        switch (typeTable[_kind]) {
        case EMPTY:
            break;
        case SIMPLE:
            switch (_kind) {
            case TCKind._tk_string:
            case TCKind._tk_wstring:
                _length = is.read_long();
                break;
            case TCKind._tk_fixed:
                _digits = is.read_ushort();
                _scale = is.read_short();
                break;
            default:
                throw wrapper.invalidSimpleTypecode() ;
            }
            break;
        case COMPLEX:
            {
                TypeCodeInputStream _encap = TypeCodeInputStream.readEncapsulation(is,
                    is.orb());
                switch(_kind) {
                case TCKind._tk_objref:
                case TCKind._tk_abstract_interface:
                    {
                        setId(_encap.read_string());
                        _name = _encap.read_string();
                    }
                    break;
                case TCKind._tk_union:
                    {
                        setId(_encap.read_string());
                        _name = _encap.read_string();
                        _discriminator = new TypeCodeImpl((ORB)is.orb());
                        _discriminator.read_value_recursive(_encap);
                        _defaultIndex = _encap.read_long();
                        _memberCount = _encap.read_long();
                        _unionLabels = new AnyImpl[_memberCount];
                        _memberNames = new String[_memberCount];
                        _memberTypes = new TypeCodeImpl[_memberCount];
                        for (int i=0; i < _memberCount; i++) {
                            _unionLabels[i] = new AnyImpl((ORB)is.orb());
                            if (i == _defaultIndex)
                                _unionLabels[i].insert_octet(_encap.read_octet());
                            else {
                                switch (realType(_discriminator).kind().value()) {
                                case TCKind._tk_short:
                                    _unionLabels[i].insert_short(_encap.read_short());
                                    break;
                                case TCKind._tk_long:
                                    _unionLabels[i].insert_long(_encap.read_long());
                                    break;
                                case TCKind._tk_ushort:
                                    _unionLabels[i].insert_ushort(_encap.read_short());
                                    break;
                                case TCKind._tk_ulong:
                                    _unionLabels[i].insert_ulong(_encap.read_long());
                                    break;
                                case TCKind._tk_float:
                                    _unionLabels[i].insert_float(_encap.read_float());
                                    break;
                                case TCKind._tk_double:
                                    _unionLabels[i].insert_double(_encap.read_double());
                                    break;
                                case TCKind._tk_boolean:
                                    _unionLabels[i].insert_boolean(_encap.read_boolean());
                                    break;
                                case TCKind._tk_char:
                                    _unionLabels[i].insert_char(_encap.read_char());
                                    break;
                                case TCKind._tk_enum:
                                    _unionLabels[i].type(_discriminator);
                                    _unionLabels[i].insert_long(_encap.read_long());
                                    break;
                                case TCKind._tk_longlong:
                                    _unionLabels[i].insert_longlong(_encap.read_longlong());
                                    break;
                                case TCKind._tk_ulonglong:
                                    _unionLabels[i].insert_ulonglong(_encap.read_longlong());
                                    break;
                                case TCKind._tk_wchar:
                                    _unionLabels[i].insert_wchar(_encap.read_wchar());
                                    break;
                                default:
                                    throw wrapper.invalidComplexTypecode() ;
                                }
                            }
                            _memberNames[i] = _encap.read_string();
                            _memberTypes[i] = new TypeCodeImpl((ORB)is.orb());
                            _memberTypes[i].read_value_recursive(_encap);
                            _memberTypes[i].setParent(this);
                        }
                    }
                    break;
                case TCKind._tk_enum:
                    {
                        setId(_encap.read_string());
                        _name = _encap.read_string();
                        _memberCount = _encap.read_long();
                        _memberNames = new String[_memberCount];
                        for (int i=0; i < _memberCount; i++)
                            _memberNames[i] = _encap.read_string();
                    }
                    break;
                case TCKind._tk_sequence:
                    {
                        _contentType = new TypeCodeImpl((ORB)is.orb());
                        _contentType.read_value_recursive(_encap);
                        _length = _encap.read_long();
                    }
                    break;
                case TCKind._tk_array:
                    {
                        _contentType = new TypeCodeImpl((ORB)is.orb());
                        _contentType.read_value_recursive(_encap);
                        _length = _encap.read_long();
                    }
                    break;
                case TCKind._tk_alias:
                case TCKind._tk_value_box:
                    {
                        setId(_encap.read_string());
                        _name = _encap.read_string();
                        _contentType = new TypeCodeImpl((ORB)is.orb());
                        _contentType.read_value_recursive(_encap);
                    }
                    break;
                case TCKind._tk_except:
                case TCKind._tk_struct:
                    {
                        setId(_encap.read_string());
                        _name = _encap.read_string();
                        _memberCount = _encap.read_long();
                        _memberNames = new String[_memberCount];
                        _memberTypes = new TypeCodeImpl[_memberCount];
                        for (int i=0; i < _memberCount; i++) {
                            _memberNames[i] = _encap.read_string();
                            _memberTypes[i] = new TypeCodeImpl((ORB)is.orb());
                            _memberTypes[i].read_value_recursive(_encap);
                            _memberTypes[i].setParent(this);
                        }
                    }
                    break;
                case TCKind._tk_value:
                    {
                        setId(_encap.read_string());
                        _name = _encap.read_string();
                        _type_modifier = _encap.read_short();
                        _concrete_base = new TypeCodeImpl((ORB)is.orb());
                        _concrete_base.read_value_recursive(_encap);
                        if (_concrete_base.kind().value() == TCKind._tk_null) {
                            _concrete_base = null;
                        }
                        _memberCount = _encap.read_long();
                        _memberNames = new String[_memberCount];
                        _memberTypes = new TypeCodeImpl[_memberCount];
                        _memberAccess = new short[_memberCount];
                        for (int i=0; i < _memberCount; i++) {
                            _memberNames[i] = _encap.read_string();
                            _memberTypes[i] = new TypeCodeImpl((ORB)is.orb());
                            _memberTypes[i].read_value_recursive(_encap);
                            _memberTypes[i].setParent(this);
                            _memberAccess[i] = _encap.read_short();
                        }
                    }
                    break;
                default:
                    throw wrapper.invalidTypecodeKindMarshal() ;
                }
                break;
            }
        }
    }
    public void write_value(OutputStream os) {
        if (os instanceof TypeCodeOutputStream) {
            this.write_value((TypeCodeOutputStream)os);
        } else {
            TypeCodeOutputStream wrapperOutStream = null;
            if (outBuffer == null) {
                wrapperOutStream = TypeCodeOutputStream.wrapOutputStream(os);
                this.write_value(wrapperOutStream);
                if (cachingEnabled) {
                    outBuffer = wrapperOutStream.getTypeCodeBuffer();
                }
            } else {
            }
            if (cachingEnabled && outBuffer != null) {
                os.write_long(_kind);
                os.write_octet_array(outBuffer, 0, outBuffer.length);
            } else {
                wrapperOutStream.writeRawBuffer(os, _kind);
            }
        }
    }
    public void write_value(TypeCodeOutputStream tcos) {
        if (_kind == TCKind._tk_native)
            throw wrapper.cannotMarshalNative() ;
        TypeCodeOutputStream topStream = tcos.getTopLevelStream();
        if (_kind == tk_indirect) {
            int pos = topStream.getPositionForID(_id);
            int topPos = tcos.getTopLevelPosition();
            tcos.writeIndirection(tk_indirect, pos);
            return;
        }
        tcos.write_long(_kind);
        topStream.addIDAtPosition(_id, tcos.getTopLevelPosition()-4);
        switch (typeTable[_kind]) {
        case EMPTY:
            break;
        case SIMPLE:
            switch (_kind) {
            case TCKind._tk_string:
            case TCKind._tk_wstring:
                tcos.write_long(_length);
                break;
            case TCKind._tk_fixed:
                tcos.write_ushort(_digits);
                tcos.write_short(_scale);
                break;
            default:
                throw wrapper.invalidSimpleTypecode() ;
            }
            break;
        case COMPLEX:
            {
                TypeCodeOutputStream _encap = tcos.createEncapsulation(tcos.orb());
                switch(_kind) {
                case TCKind._tk_objref:
                case TCKind._tk_abstract_interface:
                    {
                        _encap.write_string(_id);
                        _encap.write_string(_name);
                    }
                    break;
                case TCKind._tk_union:
                    {
                        _encap.write_string(_id);
                        _encap.write_string(_name);
                        _discriminator.write_value(_encap);
                        _encap.write_long(_defaultIndex);
                        _encap.write_long(_memberCount);
                        for (int i=0; i < _memberCount; i++) {
                            if (i == _defaultIndex)
                                _encap.write_octet(_unionLabels[i].extract_octet());
                            else {
                                switch (realType(_discriminator).kind().value()) {
                                case TCKind._tk_short:
                                    _encap.write_short(_unionLabels[i].extract_short());
                                    break;
                                case TCKind._tk_long:
                                    _encap.write_long(_unionLabels[i].extract_long());
                                    break;
                                case TCKind._tk_ushort:
                                    _encap.write_short(_unionLabels[i].extract_ushort());
                                    break;
                                case TCKind._tk_ulong:
                                    _encap.write_long(_unionLabels[i].extract_ulong());
                                    break;
                                case TCKind._tk_float:
                                    _encap.write_float(_unionLabels[i].extract_float());
                                    break;
                                case TCKind._tk_double:
                                    _encap.write_double(_unionLabels[i].extract_double());
                                    break;
                                case TCKind._tk_boolean:
                                    _encap.write_boolean(_unionLabels[i].extract_boolean());
                                    break;
                                case TCKind._tk_char:
                                    _encap.write_char(_unionLabels[i].extract_char());
                                    break;
                                case TCKind._tk_enum:
                                    _encap.write_long(_unionLabels[i].extract_long());
                                    break;
                                case TCKind._tk_longlong:
                                    _encap.write_longlong(_unionLabels[i].extract_longlong());
                                    break;
                                case TCKind._tk_ulonglong:
                                    _encap.write_longlong(_unionLabels[i].extract_ulonglong());
                                    break;
                                case TCKind._tk_wchar:
                                    _encap.write_wchar(_unionLabels[i].extract_wchar());
                                    break;
                                default:
                                    throw wrapper.invalidComplexTypecode() ;
                                }
                            }
                            _encap.write_string(_memberNames[i]);
                            _memberTypes[i].write_value(_encap);
                        }
                    }
                    break;
                case TCKind._tk_enum:
                    {
                        _encap.write_string(_id);
                        _encap.write_string(_name);
                        _encap.write_long(_memberCount);
                        for (int i=0; i < _memberCount; i++)
                            _encap.write_string(_memberNames[i]);
                    }
                    break;
                case TCKind._tk_sequence:
                    {
                        lazy_content_type().write_value(_encap);
                        _encap.write_long(_length);
                    }
                    break;
                case TCKind._tk_array:
                    {
                        _contentType.write_value(_encap);
                        _encap.write_long(_length);
                    }
                    break;
                case TCKind._tk_alias:
                case TCKind._tk_value_box:
                    {
                        _encap.write_string(_id);
                        _encap.write_string(_name);
                        _contentType.write_value(_encap);
                    }
                    break;
                case TCKind._tk_struct:
                case TCKind._tk_except:
                    {
                        _encap.write_string(_id);
                        _encap.write_string(_name);
                        _encap.write_long(_memberCount);
                        for (int i=0; i < _memberCount; i++) {
                            _encap.write_string(_memberNames[i]);
                            _memberTypes[i].write_value(_encap);
                        }
                    }
                    break;
                case TCKind._tk_value:
                    {
                        _encap.write_string(_id);
                        _encap.write_string(_name);
                        _encap.write_short(_type_modifier);
                        if (_concrete_base == null) {
                            _orb.get_primitive_tc(TCKind._tk_null).write_value(_encap);
                        } else {
                            _concrete_base.write_value(_encap);
                        }
                        _encap.write_long(_memberCount);
                        for (int i=0; i < _memberCount; i++) {
                            _encap.write_string(_memberNames[i]);
                            _memberTypes[i].write_value(_encap);
                            _encap.write_short(_memberAccess[i]);
                        }
                    }
                    break;
                default:
                    throw wrapper.invalidTypecodeKindMarshal() ;
                }
                _encap.writeOctetSequenceTo(tcos);
                break;
            }
        }
    }
    protected void copy(org.omg.CORBA.portable.InputStream src,
        org.omg.CORBA.portable.OutputStream dst)
    {
        switch (_kind) {
        case TCKind._tk_null:
        case TCKind._tk_void:
        case TCKind._tk_native:
        case TCKind._tk_abstract_interface:
            break;
        case TCKind._tk_short:
        case TCKind._tk_ushort:
            dst.write_short(src.read_short());
            break;
        case TCKind._tk_long:
        case TCKind._tk_ulong:
            dst.write_long(src.read_long());
            break;
        case TCKind._tk_float:
            dst.write_float(src.read_float());
            break;
        case TCKind._tk_double:
            dst.write_double(src.read_double());
            break;
        case TCKind._tk_longlong:
        case TCKind._tk_ulonglong:
            dst.write_longlong(src.read_longlong());
            break;
        case TCKind._tk_longdouble:
            throw wrapper.tkLongDoubleNotSupported() ;
        case TCKind._tk_boolean:
            dst.write_boolean(src.read_boolean());
            break;
        case TCKind._tk_char:
            dst.write_char(src.read_char());
            break;
        case TCKind._tk_wchar:
            dst.write_wchar(src.read_wchar());
            break;
        case TCKind._tk_octet:
            dst.write_octet(src.read_octet());
            break;
        case TCKind._tk_string:
            {
                String s;
                s = src.read_string();
                if ((_length != 0) && (s.length() > _length))
                    throw wrapper.badStringBounds( new Integer(s.length()),
                        new Integer(_length) ) ;
                dst.write_string(s);
            }
            break;
        case TCKind._tk_wstring:
            {
                String s;
                s = src.read_wstring();
                if ((_length != 0) && (s.length() > _length))
                    throw wrapper.badStringBounds( new Integer(s.length()),
                        new Integer(_length) ) ;
                dst.write_wstring(s);
            }
            break;
        case TCKind._tk_fixed:
            {
                dst.write_ushort(src.read_ushort());
                dst.write_short(src.read_short());
            }
            break;
        case TCKind._tk_any:
            {
                Any tmp =  ((CDRInputStream)src).orb().create_any();
                TypeCodeImpl t = new TypeCodeImpl((ORB)dst.orb());
                t.read_value((org.omg.CORBA_2_3.portable.InputStream)src);
                t.write_value((org.omg.CORBA_2_3.portable.OutputStream)dst);
                tmp.read_value(src, t);
                tmp.write_value(dst);
                break;
            }
        case TCKind._tk_TypeCode:
            {
                dst.write_TypeCode(src.read_TypeCode());
                break;
            }
        case TCKind._tk_Principal:
            {
                dst.write_Principal(src.read_Principal());
                break;
            }
        case TCKind._tk_objref:
            {
                dst.write_Object(src.read_Object());
                break;
            }
        case TCKind._tk_except:
            dst.write_string(src.read_string());
        case TCKind._tk_value:
        case TCKind._tk_struct:
            {
                for (int i=0; i < _memberTypes.length; i++) {
                    _memberTypes[i].copy(src, dst);
                }
                break;
            }
        case TCKind._tk_union:
            {
                Any tagValue = new AnyImpl( (ORB)src.orb());
                switch  (realType(_discriminator).kind().value()) {
                case TCKind._tk_short:
                    {
                        short value = src.read_short();
                        tagValue.insert_short(value);
                        dst.write_short(value);
                        break;
                    }
                case TCKind._tk_long:
                    {
                        int value = src.read_long();
                        tagValue.insert_long(value);
                        dst.write_long(value);
                        break;
                    }
                case TCKind._tk_ushort:
                    {
                        short value = src.read_short();
                        tagValue.insert_ushort(value);
                        dst.write_short(value);
                        break;
                    }
                case TCKind._tk_ulong:
                    {
                        int value = src.read_long();
                        tagValue.insert_ulong(value);
                        dst.write_long(value);
                        break;
                    }
                case TCKind._tk_float:
                    {
                        float value = src.read_float();
                        tagValue.insert_float(value);
                        dst.write_float(value);
                        break;
                    }
                case TCKind._tk_double:
                    {
                        double value = src.read_double();
                        tagValue.insert_double(value);
                        dst.write_double(value);
                        break;
                    }
                case TCKind._tk_boolean:
                    {
                        boolean value = src.read_boolean();
                        tagValue.insert_boolean(value);
                        dst.write_boolean(value);
                        break;
                    }
                case TCKind._tk_char:
                    {
                        char value = src.read_char();
                        tagValue.insert_char(value);
                        dst.write_char(value);
                        break;
                    }
                case TCKind._tk_enum:
                    {
                        int value = src.read_long();
                        tagValue.type(_discriminator);
                        tagValue.insert_long(value);
                        dst.write_long(value);
                        break;
                    }
                case TCKind._tk_longlong:
                    {
                        long value = src.read_longlong();
                        tagValue.insert_longlong(value);
                        dst.write_longlong(value);
                        break;
                    }
                case TCKind._tk_ulonglong:
                    {
                        long value = src.read_longlong();
                        tagValue.insert_ulonglong(value);
                        dst.write_longlong(value);
                        break;
                    }
                case TCKind._tk_wchar:
                    {
                        char value = src.read_wchar();
                        tagValue.insert_wchar(value);
                        dst.write_wchar(value);
                        break;
                    }
                default:
                    throw wrapper.illegalUnionDiscriminatorType() ;
                }
                int labelIndex;
                for (labelIndex = 0; labelIndex < _unionLabels.length; labelIndex++) {
                    if (tagValue.equal(_unionLabels[labelIndex])) {
                        _memberTypes[labelIndex].copy(src, dst);
                        break;
                    }
                }
                if (labelIndex == _unionLabels.length) {
                    if (_defaultIndex == -1)
                        throw wrapper.unexpectedUnionDefault() ;
                    else
                        _memberTypes[_defaultIndex].copy(src, dst);
                }
                break;
            }
        case TCKind._tk_enum:
            dst.write_long(src.read_long());
            break;
        case TCKind._tk_sequence:
            int seqLength = src.read_long();
            if ((_length != 0) && (seqLength > _length))
                throw wrapper.badSequenceBounds( new Integer(seqLength),
                    new Integer(_length) ) ;
            dst.write_long(seqLength);
            lazy_content_type(); 
            for (int i=0; i < seqLength; i++)
                _contentType.copy(src, dst);
            break;
        case TCKind._tk_array:
            for (int i=0; i < _length; i++)
                _contentType.copy(src, dst);
            break;
        case TCKind._tk_alias:
        case TCKind._tk_value_box:
            _contentType.copy(src, dst);
            break;
        case tk_indirect:
            indirectType().copy(src, dst);
            break;
        default:
            throw wrapper.invalidTypecodeKindMarshal() ;
        }
    }
    static protected short digits(java.math.BigDecimal value) {
        if (value == null)
            return 0;
        short length = (short)value.unscaledValue().toString().length();
        if (value.signum() == -1)
            length--;
        return length;
    }
    static protected short scale(java.math.BigDecimal value) {
        if (value == null)
            return 0;
        return (short)value.scale();
    }
    int currentUnionMemberIndex(Any discriminatorValue) throws BadKind {
        if (_kind != TCKind._tk_union)
            throw new BadKind();
        try {
            for (int i=0; i<member_count(); i++) {
                if (member_label(i).equal(discriminatorValue)) {
                    return i;
                }
            }
            if (_defaultIndex != -1) {
                return _defaultIndex;
            }
        } catch (BadKind bad) {
        } catch (org.omg.CORBA.TypeCodePackage.Bounds bounds) {
        }
        return -1;
    }
    public String description() {
        return "TypeCodeImpl with kind " + _kind + " and id " + _id;
    }
    public String toString() {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream(1024);
        PrintStream printOut = new PrintStream(byteOut, true);
        printStream(printOut);
        return super.toString() + " =\n" + byteOut.toString();
    }
    public void printStream(PrintStream s) {
        printStream(s, 0);
    }
    private void printStream(PrintStream s, int level) {
        if (_kind == tk_indirect) {
            s.print("indirect " + _id);
            return;
        }
        switch (_kind) {
            case TCKind._tk_null:
            case TCKind._tk_void:
            case TCKind._tk_short:
            case TCKind._tk_long:
            case TCKind._tk_ushort:
            case TCKind._tk_ulong:
            case TCKind._tk_float:
            case TCKind._tk_double:
            case TCKind._tk_boolean:
            case TCKind._tk_char:
            case TCKind._tk_octet:
            case TCKind._tk_any:
            case TCKind._tk_TypeCode:
            case TCKind._tk_Principal:
            case TCKind._tk_objref:
            case TCKind._tk_longlong:
            case TCKind._tk_ulonglong:
            case TCKind._tk_longdouble:
            case TCKind._tk_wchar:
            case TCKind._tk_native:
                s.print(kindNames[_kind] + " " + _name);
                break;
            case TCKind._tk_struct:
            case TCKind._tk_except:
            case TCKind._tk_value:
                s.println(kindNames[_kind] + " " + _name + " = {");
                for(int i=0; i<_memberCount; i++) {
                    s.print(indent(level + 1));
                    if (_memberTypes[i] != null)
                        _memberTypes[i].printStream(s, level + 1);
                    else
                        s.print("<unknown type>");
                    s.println(" " + _memberNames[i] + ";");
                }
                s.print(indent(level) + "}");
                break;
            case TCKind._tk_union:
                s.print("union " + _name + "...");
                break;
            case TCKind._tk_enum:
                s.print("enum " + _name + "...");
                break;
            case TCKind._tk_string:
                if (_length == 0)
                    s.print("unbounded string " + _name);
                else
                    s.print("bounded string(" + _length + ") " + _name);
                break;
            case TCKind._tk_sequence:
            case TCKind._tk_array:
                s.println(kindNames[_kind] + "[" + _length + "] " + _name + " = {");
                s.print(indent(level + 1));
                if (lazy_content_type() != null) {
                    lazy_content_type().printStream(s, level + 1);
                }
                s.println(indent(level) + "}");
                break;
            case TCKind._tk_alias:
                s.print("alias " + _name + " = " +
                    (_contentType != null ? _contentType._name : "<unresolved>"));
                break;
            case TCKind._tk_wstring:
                s.print("wstring[" + _length + "] " + _name);
                break;
            case TCKind._tk_fixed:
                s.print("fixed(" + _digits + ", " + _scale + ") " + _name);
                break;
            case TCKind._tk_value_box:
                s.print("valueBox " + _name + "...");
                break;
            case TCKind._tk_abstract_interface:
                s.print("abstractInterface " + _name + "...");
                break;
            default:
                s.print("<unknown type>");
                break;
        }
    }
    private String indent(int level) {
        String indent = "";
        for(int i=0; i<level; i++) {
            indent += "  ";
        }
        return indent;
    }
    protected void setCaching(boolean enableCaching) {
        cachingEnabled = enableCaching;
        if (enableCaching == false)
            outBuffer = null;
    }
}
