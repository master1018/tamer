abstract class DynAnyComplexImpl extends DynAnyConstructedImpl
{
    String[] names = null;
    NameValuePair[] nameValuePairs = null;
    NameDynAnyPair[] nameDynAnyPairs = null;
    private DynAnyComplexImpl() {
        this(null, (Any)null, false);
    }
    protected DynAnyComplexImpl(ORB orb, Any any, boolean copyValue) {
        super(orb, any, copyValue);
    }
    protected DynAnyComplexImpl(ORB orb, TypeCode typeCode) {
        super(orb, typeCode);
        index = 0;
    }
    public String current_member_name ()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if( ! checkInitComponents() || index < 0 || index >= names.length) {
            throw new InvalidValue();
        }
        return names[index];
    }
    public TCKind current_member_kind ()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if( ! checkInitComponents() || index < 0 || index >= components.length) {
            throw new InvalidValue();
        }
        return components[index].type().kind();
    }
    public void set_members (org.omg.DynamicAny.NameValuePair[] value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (value == null || value.length == 0) {
            clearData();
            return;
        }
        Any memberAny;
        DynAny memberDynAny = null;
        String memberName;
        TypeCode expectedTypeCode = any.type();
        int expectedMemberCount = 0;
        try {
            expectedMemberCount = expectedTypeCode.member_count();
        } catch (BadKind badKind) { 
        }
        if (expectedMemberCount != value.length) {
            clearData();
            throw new InvalidValue();
        }
        allocComponents(value);
        for (int i=0; i<value.length; i++) {
            if (value[i] != null) {
                memberName = value[i].id;
                String expectedMemberName = null;
                try {
                    expectedMemberName = expectedTypeCode.member_name(i);
                } catch (BadKind badKind) { 
                } catch (Bounds bounds) { 
                }
                if ( ! (expectedMemberName.equals(memberName) || memberName.equals(""))) {
                    clearData();
                    throw new TypeMismatch();
                }
                memberAny = value[i].value;
                TypeCode expectedMemberType = null;
                try {
                    expectedMemberType = expectedTypeCode.member_type(i);
                } catch (BadKind badKind) { 
                } catch (Bounds bounds) { 
                }
                if (! expectedMemberType.equal(memberAny.type())) {
                    clearData();
                    throw new TypeMismatch();
                }
                try {
                    memberDynAny = DynAnyUtil.createMostDerivedDynAny(memberAny, orb, false);
                } catch (InconsistentTypeCode itc) {
                    throw new InvalidValue();
                }
                addComponent(i, memberName, memberAny, memberDynAny);
            } else {
                clearData();
                throw new InvalidValue();
            }
        }
        index = (value.length == 0 ? NO_INDEX : 0);
        representations = REPRESENTATION_COMPONENTS;
    }
    public void set_members_as_dyn_any (org.omg.DynamicAny.NameDynAnyPair[] value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (value == null || value.length == 0) {
            clearData();
            return;
        }
        Any memberAny;
        DynAny memberDynAny;
        String memberName;
        TypeCode expectedTypeCode = any.type();
        int expectedMemberCount = 0;
        try {
            expectedMemberCount = expectedTypeCode.member_count();
        } catch (BadKind badKind) { 
        }
        if (expectedMemberCount != value.length) {
            clearData();
            throw new InvalidValue();
        }
        allocComponents(value);
        for (int i=0; i<value.length; i++) {
            if (value[i] != null) {
                memberName = value[i].id;
                String expectedMemberName = null;
                try {
                    expectedMemberName = expectedTypeCode.member_name(i);
                } catch (BadKind badKind) { 
                } catch (Bounds bounds) { 
                }
                if ( ! (expectedMemberName.equals(memberName) || memberName.equals(""))) {
                    clearData();
                    throw new TypeMismatch();
                }
                memberDynAny = value[i].value;
                memberAny = getAny(memberDynAny);
                TypeCode expectedMemberType = null;
                try {
                    expectedMemberType = expectedTypeCode.member_type(i);
                } catch (BadKind badKind) { 
                } catch (Bounds bounds) { 
                }
                if (! expectedMemberType.equal(memberAny.type())) {
                    clearData();
                    throw new TypeMismatch();
                }
                addComponent(i, memberName, memberAny, memberDynAny);
            } else {
                clearData();
                throw new InvalidValue();
            }
        }
        index = (value.length == 0 ? NO_INDEX : 0);
        representations = REPRESENTATION_COMPONENTS;
    }
    private void allocComponents(int length) {
        components = new DynAny[length];
        names = new String[length];
        nameValuePairs = new NameValuePair[length];
        nameDynAnyPairs = new NameDynAnyPair[length];
        for (int i=0; i<length; i++) {
            nameValuePairs[i] = new NameValuePair();
            nameDynAnyPairs[i] = new NameDynAnyPair();
        }
    }
    private void allocComponents(org.omg.DynamicAny.NameValuePair[] value) {
        components = new DynAny[value.length];
        names = new String[value.length];
        nameValuePairs = value;
        nameDynAnyPairs = new NameDynAnyPair[value.length];
        for (int i=0; i<value.length; i++) {
            nameDynAnyPairs[i] = new NameDynAnyPair();
        }
    }
    private void allocComponents(org.omg.DynamicAny.NameDynAnyPair[] value) {
        components = new DynAny[value.length];
        names = new String[value.length];
        nameValuePairs = new NameValuePair[value.length];
        for (int i=0; i<value.length; i++) {
            nameValuePairs[i] = new NameValuePair();
        }
        nameDynAnyPairs = value;
    }
    private void addComponent(int i, String memberName, Any memberAny, DynAny memberDynAny) {
        components[i] = memberDynAny;
        names[i] = (memberName != null ? memberName : "");
        nameValuePairs[i].id = memberName;
        nameValuePairs[i].value = memberAny;
        nameDynAnyPairs[i].id = memberName;
        nameDynAnyPairs[i].value = memberDynAny;
        if (memberDynAny instanceof DynAnyImpl)
            ((DynAnyImpl)memberDynAny).setStatus(STATUS_UNDESTROYABLE);
    }
    protected boolean initializeComponentsFromAny() {
        TypeCode typeCode = any.type();
        TypeCode memberType = null;
        Any memberAny;
        DynAny memberDynAny = null;
        String memberName = null;
        int length = 0;
        try {
            length = typeCode.member_count();
        } catch (BadKind badKind) { 
        }
        InputStream input = any.create_input_stream();
        allocComponents(length);
        for (int i=0; i<length; i++) {
            try {
                memberName = typeCode.member_name(i);
                memberType = typeCode.member_type(i);
            } catch (BadKind badKind) { 
            } catch (Bounds bounds) { 
            }
            memberAny = DynAnyUtil.extractAnyFromStream(memberType, input, orb);
            try {
                memberDynAny = DynAnyUtil.createMostDerivedDynAny(memberAny, orb, false);
            } catch (InconsistentTypeCode itc) { 
            }
            addComponent(i, memberName, memberAny, memberDynAny);
        }
        return true;
    }
    protected boolean initializeComponentsFromTypeCode() {
        TypeCode typeCode = any.type();
        TypeCode memberType = null;
        Any memberAny;
        DynAny memberDynAny = null;
        String memberName;
        int length = 0;
        try {
            length = typeCode.member_count();
        } catch (BadKind badKind) { 
        }
        allocComponents(length);
        for (int i=0; i<length; i++) {
            memberName = null;
            try {
                memberName = typeCode.member_name(i);
                memberType = typeCode.member_type(i);
            } catch (BadKind badKind) { 
            } catch (Bounds bounds) { 
            }
            try {
                memberDynAny = DynAnyUtil.createMostDerivedDynAny(memberType, orb);
            } catch (InconsistentTypeCode itc) { 
            }
            memberAny = getAny(memberDynAny);
            addComponent(i, memberName, memberAny, memberDynAny);
        }
        return true;
    }
    protected void clearData() {
        super.clearData();
        names = null;
        nameValuePairs = null;
        nameDynAnyPairs = null;
    }
}
