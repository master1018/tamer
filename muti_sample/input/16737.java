public class DynUnionImpl extends DynAnyConstructedImpl implements DynUnion
{
    DynAny discriminator = null;
    DynAny currentMember = null;
    int currentMemberIndex = NO_INDEX;
    private DynUnionImpl() {
        this(null, (Any)null, false);
    }
    protected DynUnionImpl(ORB orb, Any any, boolean copyValue) {
        super(orb, any, copyValue);
    }
    protected DynUnionImpl(ORB orb, TypeCode typeCode) {
        super(orb, typeCode);
    }
    protected boolean initializeComponentsFromAny() {
        try {
            InputStream input = any.create_input_stream();
            Any discriminatorAny = DynAnyUtil.extractAnyFromStream(discriminatorType(), input, orb);
            discriminator = DynAnyUtil.createMostDerivedDynAny(discriminatorAny, orb, false);
            currentMemberIndex = currentUnionMemberIndex(discriminatorAny);
            Any memberAny = DynAnyUtil.extractAnyFromStream(memberType(currentMemberIndex), input, orb);
            currentMember = DynAnyUtil.createMostDerivedDynAny(memberAny, orb, false);
            components = new DynAny[] {discriminator, currentMember};
        } catch (InconsistentTypeCode ictc) { 
        }
        return true;
    }
    protected boolean initializeComponentsFromTypeCode() {
        try {
            discriminator = DynAnyUtil.createMostDerivedDynAny(memberLabel(0), orb, false);
            index = 0;
            currentMemberIndex = 0;
            currentMember = DynAnyUtil.createMostDerivedDynAny(memberType(0), orb);
            components = new DynAny[] {discriminator, currentMember};
        } catch (InconsistentTypeCode ictc) { 
        }
        return true;
    }
    private TypeCode discriminatorType() {
        TypeCode discriminatorType = null;
        try {
            discriminatorType = any.type().discriminator_type();
        } catch (BadKind bad) {
        }
        return discriminatorType;
    }
    private int memberCount() {
        int memberCount = 0;
        try {
            memberCount = any.type().member_count();
        } catch (BadKind bad) {
        }
        return memberCount;
    }
    private Any memberLabel(int i) {
        Any memberLabel = null;
        try {
            memberLabel = any.type().member_label(i);
        } catch (BadKind bad) {
        } catch (Bounds bounds) {
        }
        return memberLabel;
    }
    private TypeCode memberType(int i) {
        TypeCode memberType = null;
        try {
            memberType = any.type().member_type(i);
        } catch (BadKind bad) {
        } catch (Bounds bounds) {
        }
        return memberType;
    }
    private String memberName(int i) {
        String memberName = null;
        try {
            memberName = any.type().member_name(i);
        } catch (BadKind bad) {
        } catch (Bounds bounds) {
        }
        return memberName;
    }
    private int defaultIndex() {
        int defaultIndex = -1;
        try {
            defaultIndex = any.type().default_index();
        } catch (BadKind bad) {
        }
        return defaultIndex;
    }
    private int currentUnionMemberIndex(Any discriminatorValue) {
        int memberCount = memberCount();
        Any memberLabel;
        for (int i=0; i<memberCount; i++) {
            memberLabel = memberLabel(i);
            if (memberLabel.equal(discriminatorValue)) {
                return i;
            }
        }
        if (defaultIndex() != -1) {
            return defaultIndex();
        }
        return NO_INDEX;
    }
    protected void clearData() {
        super.clearData();
        discriminator = null;
        currentMember.destroy();
        currentMember = null;
        currentMemberIndex = NO_INDEX;
    }
    public org.omg.DynamicAny.DynAny get_discriminator () {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        return (checkInitComponents() ? discriminator : null);
    }
    public void set_discriminator (org.omg.DynamicAny.DynAny newDiscriminator)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if ( ! newDiscriminator.type().equal(discriminatorType())) {
            throw new TypeMismatch();
        }
        newDiscriminator = DynAnyUtil.convertToNative(newDiscriminator, orb);
        Any newDiscriminatorAny = getAny(newDiscriminator);
        int newCurrentMemberIndex = currentUnionMemberIndex(newDiscriminatorAny);
        if (newCurrentMemberIndex == NO_INDEX) {
            clearData();
            index = 0;
        } else {
            checkInitComponents();
            if (currentMemberIndex == NO_INDEX || newCurrentMemberIndex != currentMemberIndex) {
                clearData();
                index = 1;
                currentMemberIndex = newCurrentMemberIndex;
                try {
                currentMember = DynAnyUtil.createMostDerivedDynAny(memberType(currentMemberIndex), orb);
                } catch (InconsistentTypeCode ictc) {}
                discriminator = newDiscriminator;
                components = new DynAny[] { discriminator, currentMember };
                representations = REPRESENTATION_COMPONENTS;
            }
        }
    }
    public void set_to_default_member ()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        int defaultIndex = defaultIndex();
        if (defaultIndex == -1) {
            throw new TypeMismatch();
        }
        try {
            clearData();
            index = 1;
            currentMemberIndex = defaultIndex;
            currentMember = DynAnyUtil.createMostDerivedDynAny(memberType(defaultIndex), orb);
            components = new DynAny[] {discriminator, currentMember};
            Any discriminatorAny = orb.create_any();
            discriminatorAny.insert_octet((byte)0);
            discriminator = DynAnyUtil.createMostDerivedDynAny(discriminatorAny, orb, false);
            representations = REPRESENTATION_COMPONENTS;
        } catch (InconsistentTypeCode ictc) {}
    }
    public void set_to_no_active_member ()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (defaultIndex() != -1) {
            throw new TypeMismatch();
        }
        checkInitComponents();
        Any discriminatorAny = getAny(discriminator);
        discriminatorAny.type(discriminatorAny.type());
        index = 0;
        currentMemberIndex = NO_INDEX;
        currentMember.destroy();
        currentMember = null;
        components[0] = discriminator;
        representations = REPRESENTATION_COMPONENTS;
    }
    public boolean has_no_active_member () {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (defaultIndex() != -1) {
            return false;
        }
        checkInitComponents();
        return (checkInitComponents() ? (currentMemberIndex == NO_INDEX) : false);
    }
    public org.omg.CORBA.TCKind discriminator_kind () {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        return discriminatorType().kind();
    }
    public org.omg.DynamicAny.DynAny member ()
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if ( ! checkInitComponents() || currentMemberIndex == NO_INDEX)
            throw new InvalidValue();
        return currentMember;
    }
    public String member_name ()
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if ( ! checkInitComponents() || currentMemberIndex == NO_INDEX)
            throw new InvalidValue();
        String memberName = memberName(currentMemberIndex);
        return (memberName == null ? "" : memberName);
    }
    public org.omg.CORBA.TCKind member_kind ()
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if ( ! checkInitComponents() || currentMemberIndex == NO_INDEX)
            throw new InvalidValue();
        return memberType(currentMemberIndex).kind();
    }
}
